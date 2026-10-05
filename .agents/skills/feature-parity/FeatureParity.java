import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read-only feature parity checker for the sdmx-dl flavors (api, cli, grpc, rest, mcp).
 * <p>
 * Extracts operations and parameters from the sources (Java syntax trees and proto files),
 * matches them using a hand-maintained parity map, and renders a Markdown report.
 * Never modifies the repository.
 * <p>
 * Requires JDK 17+ (some sources use records). Run with {@code java FeatureParity.java --help}.
 */
public final class FeatureParity {

    static final String USAGE = """
            Usage: java FeatureParity.java [options]
              --repo <dir>        repository root (default: current directory)
              --map <file>        parity map (default: <repo>/.agents/skills/feature-parity/parity-map.md)
              --out <file>        report file (default: standard output)
              --inventory <file>  also write the raw extracted inventory to this file
            """;

    static final List<String> FLAVORS = List.of("api", "cli", "grpc", "rest", "mcp");

    static final List<String> SOURCE_ROOTS = List.of(
            "sdmx-dl-api/src/main/java",
            "sdmx-dl-cli/src/main/java",
            "sdmx-dl-grpc/src/main/java");

    static final List<String> API_ENTRY_POINTS = List.of(
            "sdmxdl.web.SdmxWebManager",
            "sdmxdl.Provider",
            "sdmxdl.script.ScriptManager");

    static final String CLI_ROOT = "sdmxdl.cli.MainCommand";

    static final String SERVER_SOURCE_ROOT = "sdmx-dl-grpc/src/main/java";

    static final String GRPC_PROTO = "sdmx-dl-grpc/src/main/proto/sdmxdl_grpc_v2.proto";

    // framework-injected MCP method parameters that are not tool arguments
    static final Set<String> MCP_SPECIAL_TYPES = Set.of(
            "McpLog", "McpConnection", "RequestId", "Progress", "Roots", "Sampling", "Elicitation",
            "Cancellation", "RawMessage", "RequestUri", "Meta");

    public static void main(String[] args) throws IOException {
        Path repo = Path.of(".");
        Path mapFile = null;
        Path out = null;
        Path inventoryOut = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--repo" -> repo = Path.of(requireValue(args, ++i));
                case "--map" -> mapFile = Path.of(requireValue(args, ++i));
                case "--out" -> out = Path.of(requireValue(args, ++i));
                case "--inventory" -> inventoryOut = Path.of(requireValue(args, ++i));
                case "-h", "--help" -> {
                    System.out.print(USAGE);
                    return;
                }
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    System.err.print(USAGE);
                    System.exit(2);
                }
            }
        }
        repo = repo.toAbsolutePath().normalize();
        if (mapFile == null) {
            mapFile = repo.resolve(".agents/skills/feature-parity/parity-map.md");
        }

        SourceIndex index = SourceIndex.load(repo, SOURCE_ROOTS);
        Extraction extraction = new Extraction();
        extraction.put("api", new ApiExtractor(index, extraction).extract());
        extraction.put("cli", new CliExtractor(index, extraction).extract());
        extraction.put("grpc", GrpcExtractor.extract(repo.resolve(GRPC_PROTO)));
        extraction.put("rest", new RestExtractor(index).extract());
        extraction.put("mcp", new McpExtractor(index).extract());

        if (inventoryOut != null) {
            write(inventoryOut, InventoryRenderer.render(extraction));
            System.err.println("Inventory written to " + inventoryOut.toAbsolutePath());
        }

        ParityMap map = ParityMap.load(mapFile);
        String report = new Report(extraction, map, repo).render();
        if (out != null) {
            write(out, report);
            System.err.println("Report written to " + out.toAbsolutePath());
        } else {
            System.out.print(report);
        }
    }

    private static String requireValue(String[] args, int i) {
        if (i >= args.length) {
            System.err.print(USAGE);
            System.exit(2);
        }
        return args[i];
    }

    private static void write(Path file, String content) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    // ---------------------------------------------------------------------------------------------
    // Model
    // ---------------------------------------------------------------------------------------------

    record Param(String name, String display, String defaultValue, boolean constantDefault,
                 boolean required, String declaredIn, String note) {
    }

    record Op(String flavor, String name, String display, String declaredIn, List<Param> params) {
    }

    static final class Extraction {
        final Map<String, List<Op>> ops = new LinkedHashMap<>();
        final Set<String> skippedHidden = new TreeSet<>();
        final List<String> warnings = new ArrayList<>();

        void put(String flavor, List<Op> list) {
            ops.put(flavor, list);
        }
    }

    record Value(String text, boolean constant) {
        static final Value NONE = new Value(null, false);
    }

    // ---------------------------------------------------------------------------------------------
    // Java source index
    // ---------------------------------------------------------------------------------------------

    static final class ClassInfo {
        final String fqn;
        final String simpleName;
        final ClassTree tree;
        final CompilationUnitTree cu;
        final ClassInfo outer;
        final String sourceRoot;
        final Map<String, ClassInfo> nested = new LinkedHashMap<>();

        ClassInfo(String fqn, String simpleName, ClassTree tree, CompilationUnitTree cu, ClassInfo outer, String sourceRoot) {
            this.fqn = fqn;
            this.simpleName = simpleName;
            this.tree = tree;
            this.cu = cu;
            this.outer = outer;
            this.sourceRoot = sourceRoot;
        }

        String packageName() {
            return cu.getPackageName() == null ? "" : cu.getPackageName().toString();
        }

        boolean isInterface() {
            return tree.getKind() == Tree.Kind.INTERFACE || tree.getKind() == Tree.Kind.ANNOTATION_TYPE;
        }

        @Override
        public String toString() {
            return fqn;
        }
    }

    record FieldRef(VariableTree var, ClassInfo owner) {
    }

    static final class SourceIndex {
        final Map<String, ClassInfo> byFqn = new LinkedHashMap<>();
        final Map<String, List<ClassInfo>> bySimpleName = new HashMap<>();

        static SourceIndex load(Path repo, List<String> roots) throws IOException {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException("A JDK (not a JRE) is required");
            }
            SourceIndex result = new SourceIndex();
            for (String root : roots) {
                Path dir = repo.resolve(root);
                if (!Files.isDirectory(dir)) {
                    throw new IOException("Missing source root: " + dir);
                }
                List<Path> files;
                try (Stream<Path> stream = Files.walk(dir)) {
                    files = stream
                            .filter(p -> p.toString().endsWith(".java"))
                            .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                            .sorted()
                            .collect(Collectors.toList());
                }
                List<String> errors = new ArrayList<>();
                try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
                    JavacTask task = (JavacTask) compiler.getTask(null, fm,
                            d -> {
                                if (d.getKind() == Diagnostic.Kind.ERROR) {
                                    errors.add(d.getSource() + ":" + d.getLineNumber() + ": " + d.getMessage(Locale.ROOT));
                                }
                            },
                            List.of("-proc:none"), null, fm.getJavaFileObjectsFromPaths(files));
                    for (CompilationUnitTree cu : task.parse()) {
                        for (Tree decl : cu.getTypeDecls()) {
                            if (decl instanceof ClassTree ct) {
                                result.register(cu, ct, null, root);
                            }
                        }
                    }
                }
                if (!errors.isEmpty()) {
                    System.err.println("WARNING: " + errors.size() + " parse error(s) in " + root
                            + " (is the JDK 17+?), first: " + errors.get(0));
                }
            }
            return result;
        }

        private void register(CompilationUnitTree cu, ClassTree ct, ClassInfo outer, String root) {
            String name = ct.getSimpleName().toString();
            if (name.isEmpty()) {
                return;
            }
            String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString();
            String fqn = outer != null ? outer.fqn + "." + name : (pkg.isEmpty() ? name : pkg + "." + name);
            ClassInfo info = new ClassInfo(fqn, name, ct, cu, outer, root);
            byFqn.put(fqn, info);
            bySimpleName.computeIfAbsent(name, k -> new ArrayList<>()).add(info);
            if (outer != null) {
                outer.nested.put(name, info);
            }
            for (Tree member : ct.getMembers()) {
                if (member instanceof ClassTree inner) {
                    register(cu, inner, info, root);
                }
            }
        }

        List<ClassInfo> inRoot(String root) {
            return byFqn.values().stream().filter(c -> c.sourceRoot.equals(root)).collect(Collectors.toList());
        }

        ClassInfo resolve(String typeName, ClassInfo ctx) {
            String name = stripGenerics(typeName).trim();
            if (name.endsWith("[]") || name.isEmpty()) {
                return null;
            }
            ClassInfo direct = byFqn.get(name);
            if (direct != null) {
                return direct;
            }
            int dot = name.indexOf('.');
            if (dot > 0) {
                ClassInfo cur = resolve(name.substring(0, dot), ctx);
                if (cur == null) {
                    return null;
                }
                for (String part : name.substring(dot + 1).split("\\.")) {
                    cur = cur.nested.get(part);
                    if (cur == null) {
                        return null;
                    }
                }
                return cur;
            }
            for (ClassInfo c = ctx; c != null; c = c.outer) {
                if (c.simpleName.equals(name)) {
                    return c;
                }
                ClassInfo n = c.nested.get(name);
                if (n != null) {
                    return n;
                }
            }
            if (ctx != null) {
                for (ImportTree imp : ctx.cu.getImports()) {
                    String q = imp.getQualifiedIdentifier().toString();
                    if (!imp.isStatic() && q.endsWith("." + name) && byFqn.containsKey(q)) {
                        return byFqn.get(q);
                    }
                }
                String samePackage = ctx.packageName().isEmpty() ? name : ctx.packageName() + "." + name;
                if (byFqn.containsKey(samePackage)) {
                    return byFqn.get(samePackage);
                }
                for (ImportTree imp : ctx.cu.getImports()) {
                    String q = imp.getQualifiedIdentifier().toString();
                    if (!imp.isStatic() && q.endsWith(".*")) {
                        ClassInfo c = byFqn.get(q.substring(0, q.length() - 1) + name);
                        if (c != null) {
                            return c;
                        }
                    }
                }
            }
            List<ClassInfo> candidates = bySimpleName.getOrDefault(name, List.of());
            return candidates.size() == 1 ? candidates.get(0) : null;
        }

        /** The class followed by its scanned superclasses. */
        List<ClassInfo> superclassChain(ClassInfo c) {
            List<ClassInfo> result = new ArrayList<>();
            Set<ClassInfo> seen = new HashSet<>();
            ClassInfo cur = c;
            while (cur != null && seen.add(cur)) {
                result.add(cur);
                Tree ext = cur.tree.getExtendsClause();
                cur = ext == null ? null : resolve(ext.toString(), cur);
            }
            return result;
        }

        FieldRef findField(ClassInfo c, String name) {
            return findField(c, name, new HashSet<>());
        }

        private FieldRef findField(ClassInfo c, String name, Set<ClassInfo> visited) {
            if (c == null || !visited.add(c)) {
                return null;
            }
            for (Tree member : c.tree.getMembers()) {
                if (member instanceof VariableTree v && v.getName().contentEquals(name)) {
                    return new FieldRef(v, c);
                }
            }
            List<Tree> supers = new ArrayList<>();
            if (c.tree.getExtendsClause() != null) {
                supers.add(c.tree.getExtendsClause());
            }
            supers.addAll(c.tree.getImplementsClause());
            for (Tree s : supers) {
                FieldRef found = findField(resolve(s.toString(), c), name, visited);
                if (found != null) {
                    return found;
                }
            }
            return findField(c.outer, name, visited);
        }

        FieldRef lookupIdentifier(String name, ClassInfo ctx) {
            FieldRef local = findField(ctx, name);
            if (local != null) {
                return local;
            }
            for (ImportTree imp : ctx.cu.getImports()) {
                if (!imp.isStatic()) {
                    continue;
                }
                String q = imp.getQualifiedIdentifier().toString();
                int dot = q.lastIndexOf('.');
                String owner = q.substring(0, dot);
                String member = q.substring(dot + 1);
                if (member.equals(name) || member.equals("*")) {
                    FieldRef found = findField(byFqn.get(owner), name);
                    if (found != null) {
                        return found;
                    }
                }
            }
            return null;
        }

        Value eval(ExpressionTree e, ClassInfo ctx) {
            if (e == null) {
                return Value.NONE;
            }
            Object c = constant(e, ctx, 0);
            return c != null ? new Value(String.valueOf(c), true) : new Value(e.toString(), false);
        }

        String evalText(ExpressionTree e, ClassInfo ctx) {
            return eval(e, ctx).text();
        }

        private Object constant(ExpressionTree e, ClassInfo ctx, int depth) {
            if (e == null || depth > 32) {
                return null;
            }
            if (e instanceof LiteralTree l) {
                return l.getValue();
            }
            if (e instanceof ParenthesizedTree p) {
                return constant(p.getExpression(), ctx, depth + 1);
            }
            if (e instanceof UnaryTree u && u.getKind() == Tree.Kind.UNARY_MINUS) {
                Object v = constant(u.getExpression(), ctx, depth + 1);
                if (v instanceof Integer i) return -i;
                if (v instanceof Long l) return -l;
                if (v instanceof Double d) return -d;
                return null;
            }
            if (e instanceof BinaryTree b && b.getKind() == Tree.Kind.PLUS) {
                Object l = constant(b.getLeftOperand(), ctx, depth + 1);
                Object r = constant(b.getRightOperand(), ctx, depth + 1);
                if (l == null || r == null) return null;
                if (l instanceof String || r instanceof String) return String.valueOf(l) + r;
                if (l instanceof Integer a && r instanceof Integer c) return a + c;
                return null;
            }
            if (e instanceof IdentifierTree id) {
                return fieldConstant(lookupIdentifier(id.getName().toString(), ctx), depth);
            }
            if (e instanceof MemberSelectTree ms) {
                ClassInfo owner = resolve(ms.getExpression().toString(), ctx);
                return owner == null ? null : fieldConstant(findField(owner, ms.getIdentifier().toString()), depth);
            }
            return null;
        }

        private Object fieldConstant(FieldRef f, int depth) {
            if (f == null || f.var().getInitializer() == null) {
                return null;
            }
            boolean isFinal = f.var().getModifiers().getFlags().contains(Modifier.FINAL) || f.owner().isInterface();
            return isFinal ? constant(f.var().getInitializer(), f.owner(), depth + 1) : null;
        }
    }

    static String stripGenerics(String type) {
        int lt = type.indexOf('<');
        return lt < 0 ? type : type.substring(0, lt);
    }

    static AnnotationTree annotation(ModifiersTree mods, String simpleName) {
        for (AnnotationTree a : mods.getAnnotations()) {
            String t = a.getAnnotationType().toString();
            if (t.equals(simpleName) || t.endsWith("." + simpleName)) {
                return a;
            }
        }
        return null;
    }

    static Map<String, ExpressionTree> annotationArgs(AnnotationTree a) {
        Map<String, ExpressionTree> result = new LinkedHashMap<>();
        if (a == null) {
            return result;
        }
        for (ExpressionTree arg : a.getArguments()) {
            if (arg instanceof AssignmentTree as) {
                result.put(as.getVariable().toString(), as.getExpression());
            } else {
                result.put("value", arg);
            }
        }
        return result;
    }

    static List<ExpressionTree> elements(ExpressionTree e) {
        if (e == null) {
            return List.of();
        }
        if (e instanceof NewArrayTree na) {
            return na.getInitializers() == null ? List.of() : new ArrayList<>(na.getInitializers());
        }
        return List.of(e);
    }

    static boolean isTrue(SourceIndex index, ExpressionTree e, ClassInfo ctx) {
        return e != null && "true".equals(index.evalText(e, ctx));
    }

    static List<MethodTree> methods(ClassInfo c) {
        List<MethodTree> result = new ArrayList<>();
        for (Tree member : c.tree.getMembers()) {
            if (member instanceof MethodTree m && m.getReturnType() != null) {
                result.add(m);
            }
        }
        return result;
    }

    static List<VariableTree> fields(ClassInfo c) {
        List<VariableTree> result = new ArrayList<>();
        for (Tree member : c.tree.getMembers()) {
            if (member instanceof VariableTree v) {
                result.add(v);
            }
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------------
    // Extractors
    // ---------------------------------------------------------------------------------------------

    /** Public methods of the API entry points; builder-style parameter objects are expanded into their fields. */
    record ApiExtractor(SourceIndex index, Extraction extraction) {

        List<Op> extract() {
            Map<String, Op> result = new LinkedHashMap<>();
            for (String fqn : API_ENTRY_POINTS) {
                ClassInfo entry = index.byFqn.get(fqn);
                if (entry == null) {
                    extraction.warnings.add("api: entry point not found: " + fqn);
                    continue;
                }
                for (ClassInfo c : index.superclassChain(entry)) {
                    for (MethodTree m : methods(c)) {
                        Set<Modifier> flags = m.getModifiers().getFlags();
                        boolean isPublic = flags.contains(Modifier.PUBLIC) || c.isInterface();
                        if (!isPublic || flags.contains(Modifier.STATIC) || flags.contains(Modifier.PRIVATE)) {
                            continue;
                        }
                        String name = m.getName().toString();
                        String display = entry.simpleName + "." + name;
                        Op op = result.computeIfAbsent(name, k -> new Op("api", name, display, c.simpleName, new ArrayList<>()));
                        if (c.simpleName.equals("Provider") && op.params().isEmpty()) {
                            // a Provider is bound to a source by SdmxWebManager.usingName(source)
                            op.params().add(new Param("source", "source", null, false, true, "SdmxWebManager.usingName", "String"));
                        }
                        for (VariableTree p : m.getParameters()) {
                            for (Param param : expand(p, c)) {
                                if (op.params().stream().noneMatch(x -> x.name().equals(param.name()))) {
                                    op.params().add(param);
                                }
                            }
                        }
                    }
                }
            }
            return new ArrayList<>(result.values());
        }

        private List<Param> expand(VariableTree p, ClassInfo ctx) {
            String type = p.getType().toString();
            ClassInfo t = index.resolve(type, ctx);
            if (t != null && !t.isInterface() && annotation(t.tree.getModifiers(), "Builder") != null
                    && (t.simpleName.endsWith("Request") || t.simpleName.endsWith("Options"))) {
                List<Param> result = new ArrayList<>();
                for (VariableTree f : fields(t)) {
                    if (f.getModifiers().getFlags().contains(Modifier.STATIC)) {
                        continue;
                    }
                    boolean hasDefault = annotation(f.getModifiers(), "Default") != null && f.getInitializer() != null;
                    Value def = hasDefault ? index.eval(f.getInitializer(), t) : Value.NONE;
                    boolean required = !hasDefault
                            && annotation(f.getModifiers(), "NonNull") != null
                            && annotation(f.getModifiers(), "Singular") == null;
                    String fieldName = f.getName().toString();
                    result.add(new Param(fieldName, fieldName, def.text(), def.constant(), required,
                            t.simpleName, stripGenerics(f.getType().toString())));
                }
                return result;
            }
            String name = p.getName().toString();
            boolean nullable = annotation(p.getModifiers(), "Nullable") != null;
            return List.of(new Param(name, name, null, false, !nullable, ctx.simpleName, type));
        }
    }

    /** Leaf commands of the picocli tree, with options and positional parameters from fields, mixins and arg groups. */
    record CliExtractor(SourceIndex index, Extraction extraction) {

        List<Op> extract() {
            List<Op> result = new ArrayList<>();
            ClassInfo root = index.byFqn.get(CLI_ROOT);
            if (root == null) {
                extraction.warnings.add("cli: root command not found: " + CLI_ROOT);
                return result;
            }
            walk(root, List.of(), false, result);
            return result;
        }

        private void walk(ClassInfo cmd, List<String> path, boolean hidden, List<Op> out) {
            Map<String, ExpressionTree> args = annotationArgs(annotation(cmd.tree.getModifiers(), "Command"));
            List<ClassInfo> subs = new ArrayList<>();
            for (ExpressionTree e : elements(args.get("subcommands"))) {
                String s = e.toString().replaceAll("\\.class$", "");
                ClassInfo sub = index.resolve(s, cmd);
                if (sub != null) {
                    subs.add(sub);
                } else {
                    extraction.warnings.add("cli: cannot resolve subcommand " + s + " in " + cmd.fqn);
                }
            }
            List<MethodTree> methodCommands = methods(cmd).stream()
                    .filter(m -> annotation(m.getModifiers(), "Command") != null)
                    .collect(Collectors.toList());

            if (subs.isEmpty() && methodCommands.isEmpty()) {
                addLeaf(path, hidden, cmd.simpleName, hidden ? List.of() : collectClassParams(cmd), out);
                return;
            }
            for (ClassInfo sub : subs) {
                Map<String, ExpressionTree> subArgs = annotationArgs(annotation(sub.tree.getModifiers(), "Command"));
                String name = subArgs.containsKey("name") ? index.evalText(subArgs.get("name"), sub) : sub.simpleName;
                walk(sub, append(path, name), hidden || isTrue(index, subArgs.get("hidden"), sub), out);
            }
            for (MethodTree m : methodCommands) {
                Map<String, ExpressionTree> mArgs = annotationArgs(annotation(m.getModifiers(), "Command"));
                String name = mArgs.containsKey("name") ? index.evalText(mArgs.get("name"), cmd) : m.getName().toString();
                List<Param> params = new ArrayList<>();
                for (VariableTree p : m.getParameters()) {
                    collectElement(p, cmd, params, new HashSet<>());
                }
                addLeaf(append(path, name), hidden || isTrue(index, mArgs.get("hidden"), cmd),
                        cmd.simpleName + "#" + m.getName(), params, out);
            }
        }

        private void addLeaf(List<String> path, boolean hidden, String declaredIn, List<Param> params, List<Op> out) {
            String name = String.join(" ", path);
            if (hidden) {
                extraction.skippedHidden.add("cli: command `" + name + "`");
                return;
            }
            out.add(new Op("cli", name, "sdmx-dl " + name, declaredIn, params));
        }

        private static List<String> append(List<String> path, String name) {
            List<String> result = new ArrayList<>(path);
            result.add(name);
            return result;
        }

        private List<Param> collectClassParams(ClassInfo c) {
            List<Param> result = new ArrayList<>();
            collectClass(c, result, new HashSet<>());
            return result;
        }

        private void collectClass(ClassInfo c, List<Param> out, Set<ClassInfo> visited) {
            List<ClassInfo> chain = new ArrayList<>(index.superclassChain(c));
            Collections.reverse(chain);
            for (ClassInfo k : chain) {
                if (!visited.add(k)) {
                    continue;
                }
                for (VariableTree f : fields(k)) {
                    collectElement(f, k, out, visited);
                }
                for (MethodTree m : methods(k)) {
                    // annotated setter methods, e.g. @Option void setProperty(...)
                    if (m.getParameters().size() == 1) {
                        Param p = optionOrPositional(m.getModifiers(), m.getName().toString(), k);
                        if (p != null) {
                            out.add(p);
                        }
                    }
                }
            }
        }

        private void collectElement(VariableTree v, ClassInfo ctx, List<Param> out, Set<ClassInfo> visited) {
            ModifiersTree mods = v.getModifiers();
            if (annotation(mods, "Mixin") != null || annotation(mods, "ArgGroup") != null) {
                ClassInfo type = index.resolve(v.getType().toString(), ctx);
                if (type != null) {
                    collectClass(type, out, visited);
                } else {
                    extraction.warnings.add("cli: cannot resolve mixin/arg group type " + v.getType() + " in " + ctx.fqn);
                }
                return;
            }
            Param p = optionOrPositional(mods, v.getName().toString(), ctx);
            if (p != null) {
                out.add(p);
            }
        }

        private Param optionOrPositional(ModifiersTree mods, String elementName, ClassInfo ctx) {
            AnnotationTree option = annotation(mods, "Option");
            if (option != null) {
                Map<String, ExpressionTree> a = annotationArgs(option);
                List<String> names = elements(a.get("names")).stream()
                        .map(e -> index.evalText(e, ctx))
                        .collect(Collectors.toList());
                if (names.isEmpty()) {
                    names = List.of(elementName);
                }
                String name = names.stream().max(Comparator.comparingInt(String::length)).orElse(elementName);
                if (isTrue(index, a.get("hidden"), ctx)) {
                    extraction.skippedHidden.add("cli: option `" + name + "` (" + ctx.simpleName + ")");
                    return null;
                }
                Value def = a.containsKey("defaultValue") ? index.eval(a.get("defaultValue"), ctx) : Value.NONE;
                return new Param(name, String.join(", ", names), def.text(), def.constant(),
                        isTrue(index, a.get("required"), ctx), ctx.simpleName, null);
            }
            AnnotationTree positional = annotation(mods, "Parameters");
            if (positional != null) {
                Map<String, ExpressionTree> a = annotationArgs(positional);
                String label = a.containsKey("paramLabel") ? index.evalText(a.get("paramLabel"), ctx) : "<" + elementName + ">";
                String name = label.replaceAll("[<>]", "");
                if (isTrue(index, a.get("hidden"), ctx)) {
                    extraction.skippedHidden.add("cli: parameter `" + label + "` (" + ctx.simpleName + ")");
                    return null;
                }
                String arity = a.containsKey("arity") ? index.evalText(a.get("arity"), ctx) : "1";
                Value def = a.containsKey("defaultValue") ? index.eval(a.get("defaultValue"), ctx) : Value.NONE;
                String position = a.containsKey("index") ? index.evalText(a.get("index"), ctx) : "?";
                return new Param(name, label, def.text(), def.constant(), !arity.startsWith("0"),
                        ctx.simpleName, "positional #" + position);
            }
            return null;
        }
    }

    /** JAX-RS resource methods of the server module. */
    record RestExtractor(SourceIndex index) {

        static final List<String> HTTP_METHODS = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD");
        static final List<String> PARAM_KINDS = List.of("PathParam", "QueryParam", "HeaderParam", "FormParam", "CookieParam", "MatrixParam");

        List<Op> extract() {
            List<Op> result = new ArrayList<>();
            for (ClassInfo c : index.inRoot(SERVER_SOURCE_ROOT)) {
                AnnotationTree classPath = annotation(c.tree.getModifiers(), "Path");
                if (classPath == null || c.outer != null) {
                    continue;
                }
                String base = index.evalText(annotationArgs(classPath).get("value"), c);
                for (MethodTree m : methods(c)) {
                    String http = HTTP_METHODS.stream()
                            .filter(h -> annotation(m.getModifiers(), h) != null)
                            .findFirst().orElse(null);
                    if (http == null) {
                        continue;
                    }
                    AnnotationTree methodPath = annotation(m.getModifiers(), "Path");
                    String path = methodPath == null ? "" : index.evalText(annotationArgs(methodPath).get("value"), c);
                    List<Param> params = new ArrayList<>();
                    for (VariableTree p : m.getParameters()) {
                        if (annotation(p.getModifiers(), "Context") != null) {
                            continue;
                        }
                        String kind = PARAM_KINDS.stream()
                                .filter(k -> annotation(p.getModifiers(), k) != null)
                                .findFirst().orElse(null);
                        AnnotationTree defaultValue = annotation(p.getModifiers(), "DefaultValue");
                        Value def = defaultValue == null ? Value.NONE : index.eval(annotationArgs(defaultValue).get("value"), c);
                        String type = stripGenerics(p.getType().toString());
                        if (kind == null) {
                            params.add(new Param("body", "body", null, false, true, c.simpleName, type));
                            continue;
                        }
                        String name = index.evalText(annotationArgs(annotation(p.getModifiers(), kind)).get("value"), c);
                        String display = switch (kind) {
                            case "PathParam" -> "{" + name + "}";
                            case "QueryParam" -> name;
                            default -> kind.replace("Param", "").toLowerCase(Locale.ROOT) + ":" + name;
                        };
                        params.add(new Param(name, display, def.text(), def.constant(), kind.equals("PathParam"),
                                c.simpleName, type));
                    }
                    result.add(new Op("rest", m.getName().toString(), http + " " + path, c.simpleName + " (" + base + ")", params));
                }
            }
            return result;
        }
    }

    /** Methods annotated with {@code @Tool} in the server module. */
    record McpExtractor(SourceIndex index) {

        List<Op> extract() {
            List<Op> result = new ArrayList<>();
            for (ClassInfo c : index.inRoot(SERVER_SOURCE_ROOT)) {
                for (MethodTree m : methods(c)) {
                    AnnotationTree tool = annotation(m.getModifiers(), "Tool");
                    if (tool == null) {
                        continue;
                    }
                    Map<String, ExpressionTree> toolArgs = annotationArgs(tool);
                    String name = toolArgs.containsKey("name") ? index.evalText(toolArgs.get("name"), c) : m.getName().toString();
                    List<Param> params = new ArrayList<>();
                    for (VariableTree p : m.getParameters()) {
                        String type = stripGenerics(p.getType().toString());
                        AnnotationTree toolArg = annotation(p.getModifiers(), "ToolArg");
                        if (toolArg == null && MCP_SPECIAL_TYPES.contains(type)) {
                            continue;
                        }
                        Map<String, ExpressionTree> a = annotationArgs(toolArg);
                        String argName = a.containsKey("name") ? index.evalText(a.get("name"), c) : p.getName().toString();
                        Value def = a.containsKey("defaultValue") ? index.eval(a.get("defaultValue"), c) : Value.NONE;
                        boolean required = !a.containsKey("required") || isTrue(index, a.get("required"), c);
                        params.add(new Param(argName, argName, def.text(), def.constant(), required && def.text() == null,
                                c.simpleName, type));
                    }
                    result.add(new Op("mcp", name, name, c.simpleName, params));
                }
            }
            return result;
        }
    }

    /** RPCs of the gRPC service; request message fields are flattened (nested messages included). */
    static final class GrpcExtractor {

        record Field(String label, String type, String name, String note) {
        }

        static final Pattern RPC = Pattern.compile(
                "rpc\\s+(\\w+)\\s*\\(\\s*(stream\\s+)?([\\w.]+)\\s*\\)\\s*returns\\s*\\(\\s*(stream\\s+)?([\\w.]+)\\s*\\)");
        static final Pattern MESSAGE = Pattern.compile("message\\s+(\\w+)\\s*\\{");
        static final Pattern FIELD = Pattern.compile(
                "^\\s*(optional\\s+|repeated\\s+)?(map\\s*<[^>]+>|[\\w.]+)\\s+(\\w+)\\s*=\\s*\\d+\\s*(?:\\[[^]]*])?\\s*;\\s*(?:/\\*(.*?)\\*/|//(.*))?\\s*$");

        static List<Op> extract(Path proto) throws IOException {
            String text = Files.readString(proto, StandardCharsets.UTF_8);
            Map<String, List<Field>> messages = parseMessages(text);
            List<Op> result = new ArrayList<>();
            Matcher m = RPC.matcher(text);
            while (m.find()) {
                String name = m.group(1);
                String input = simple(m.group(3));
                String output = (m.group(4) != null ? "stream " : "") + simple(m.group(5));
                List<Param> params = new ArrayList<>();
                flatten(messages, input, "", params, new HashSet<>());
                result.add(new Op("grpc", name, "rpc " + name + "(" + input + ") → " + output, proto.getFileName().toString(), params));
            }
            return result;
        }

        private static String simple(String type) {
            return type.substring(type.lastIndexOf('.') + 1);
        }

        private static Map<String, List<Field>> parseMessages(String text) {
            Map<String, List<Field>> result = new LinkedHashMap<>();
            Matcher m = MESSAGE.matcher(text);
            while (m.find()) {
                int start = m.end();
                int depth = 1;
                int i = start;
                while (i < text.length() && depth > 0) {
                    char ch = text.charAt(i++);
                    if (ch == '{') depth++;
                    else if (ch == '}') depth--;
                }
                List<Field> fields = new ArrayList<>();
                for (String line : text.substring(start, i - 1).split("\\R")) {
                    Matcher f = FIELD.matcher(line);
                    if (f.matches()) {
                        String note = f.group(4) != null ? f.group(4) : f.group(5);
                        fields.add(new Field(f.group(1) == null ? "" : f.group(1).trim(), f.group(2).replace(" ", ""),
                                f.group(3), note == null ? null : note.trim()));
                    }
                }
                result.put(m.group(1), fields);
            }
            return result;
        }

        private static void flatten(Map<String, List<Field>> messages, String message, String prefix, List<Param> out, Set<String> visited) {
            if (!visited.add(message)) {
                return;
            }
            for (Field f : messages.getOrDefault(message, List.of())) {
                String type = simple(f.type());
                boolean scalarLike = f.type().startsWith("map<") || f.label().equals("repeated") || !messages.containsKey(type);
                if (!scalarLike) {
                    flatten(messages, type, prefix + f.name() + ".", out, visited);
                    continue;
                }
                boolean required = f.label().isEmpty() && !f.type().startsWith("map<");
                String label = f.label().isEmpty() ? "" : f.label() + " ";
                out.add(new Param(f.name(), prefix + f.name(), null, false, required, message,
                        label + f.type() + (f.note() != null ? " — " + f.note() : "")));
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Parity map
    // ---------------------------------------------------------------------------------------------

    record Cell(String raw) {
        boolean isAuto() {
            return raw.isEmpty();
        }

        boolean isNa() {
            return raw.toLowerCase(Locale.ROOT).startsWith("n/a");
        }

        String reason() {
            int colon = raw.indexOf(':');
            return colon < 0 ? "" : raw.substring(colon + 1).trim();
        }

        /** Names separated by {@code sep}, or {@code fallback} when automatic. */
        List<String> names(String sep, String fallback) {
            if (isAuto()) {
                return List.of(fallback);
            }
            return Stream.of(raw.split(Pattern.quote(sep))).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        }
    }

    record Waiver(String op, String param, String flavor, String status, String reason) {
        boolean matches(String op, String param, String flavor, String status) {
            return (this.op.equals("*") || norm(this.op).equals(norm(op)))
                    && norm(this.param).equals(norm(param))
                    && (this.flavor.equals("*") || this.flavor.equalsIgnoreCase(flavor))
                    && this.status.equalsIgnoreCase(status);
        }
    }

    record Ignore(String flavor, String kind, String pattern, String reason, Pattern regex) {
        boolean matches(String flavor, String kind, String name, String display, String declaredIn) {
            return (this.flavor.equals("*") || this.flavor.equalsIgnoreCase(flavor))
                    && this.kind.equalsIgnoreCase(kind)
                    && (regex.matcher(name).matches() || regex.matcher(display).matches()
                    || (declaredIn != null && regex.matcher("@" + declaredIn).matches()));
        }

        static Ignore of(String flavor, String kind, String pattern, String reason) {
            StringBuilder re = new StringBuilder();
            for (char ch : pattern.toCharArray()) {
                switch (ch) {
                    case '*' -> re.append(".*");
                    case '?' -> re.append('.');
                    default -> re.append(Pattern.quote(String.valueOf(ch)));
                }
            }
            return new Ignore(flavor, kind, pattern, reason, Pattern.compile(re.toString(), Pattern.CASE_INSENSITIVE));
        }
    }

    static final class ParityMap {
        final Map<String, Map<String, Cell>> operations = new LinkedHashMap<>();
        final Map<String, Map<String, Cell>> parameters = new LinkedHashMap<>();
        /** Parameter rows restricted to one operation, keyed by normalized operation id. */
        final Map<String, Map<String, Map<String, Cell>>> scopedParameters = new LinkedHashMap<>();
        final List<Waiver> waivers = new ArrayList<>();
        final List<Ignore> ignores = new ArrayList<>();

        static ParityMap load(Path file) throws IOException {
            ParityMap result = new ParityMap();
            if (!Files.exists(file)) {
                System.err.println("WARNING: parity map not found: " + file + " (everything will be matched by name)");
                return result;
            }
            Map<String, List<Map<String, String>>> tables = parseTables(Files.readAllLines(file, StandardCharsets.UTF_8));
            readMatrix(tables.getOrDefault("operations", List.of()), result.operations);
            for (Map<String, String> row : tables.getOrDefault("parameters", List.of())) {
                String scope = row.getOrDefault("operation", "");
                readMatrix(List.of(row), scope.isEmpty()
                        ? result.parameters
                        : result.scopedParameters.computeIfAbsent(norm(scope), k -> new LinkedHashMap<>()));
            }
            for (Map<String, String> row : tables.getOrDefault("waivers", List.of())) {
                result.waivers.add(new Waiver(row.getOrDefault("operation", "*"), row.getOrDefault("parameter", ""),
                        row.getOrDefault("flavor", "*"), row.getOrDefault("status", "n/a"), row.getOrDefault("reason", "")));
            }
            for (Map<String, String> row : tables.getOrDefault("ignore", List.of())) {
                result.ignores.add(Ignore.of(row.getOrDefault("flavor", "*"), row.getOrDefault("kind", "operation"),
                        row.getOrDefault("pattern", ""), row.getOrDefault("reason", "")));
            }
            return result;
        }

        /** Parameter rows applicable to an operation, scoped rows first. */
        List<Map.Entry<String, Map<String, Cell>>> parameterRows(String op) {
            List<Map.Entry<String, Map<String, Cell>>> result =
                    new ArrayList<>(scopedParameters.getOrDefault(norm(op), Map.of()).entrySet());
            result.addAll(parameters.entrySet());
            return result;
        }

        private static void readMatrix(List<Map<String, String>> rows, Map<String, Map<String, Cell>> target) {
            for (Map<String, String> row : rows) {
                String id = row.values().iterator().next();
                if (id.isEmpty()) {
                    continue;
                }
                Map<String, Cell> cells = new LinkedHashMap<>();
                for (String flavor : FLAVORS) {
                    cells.put(flavor, new Cell(row.getOrDefault(flavor, "")));
                }
                target.put(id, cells);
            }
        }

        /** Markdown tables keyed by the lower-cased first word of the preceding "## " heading. */
        private static Map<String, List<Map<String, String>>> parseTables(List<String> lines) {
            Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
            String section = null;
            List<String> header = null;
            boolean inComment = false;
            for (String line : lines) {
                String t = line.trim();
                if (inComment) {
                    inComment = !t.contains("-->");
                    continue;
                }
                if (t.startsWith("<!--")) {
                    inComment = !t.contains("-->");
                    continue;
                }
                if (t.startsWith("## ")) {
                    section = t.substring(3).trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
                    header = null;
                    continue;
                }
                if (section == null || !t.startsWith("|")) {
                    header = null;
                    continue;
                }
                List<String> cells = splitRow(t);
                if (cells.stream().allMatch(c -> c.matches(":?-+:?"))) {
                    continue;
                }
                if (header == null) {
                    header = cells.stream().map(c -> c.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < header.size(); i++) {
                    row.put(header.get(i), i < cells.size() ? cells.get(i) : "");
                }
                result.computeIfAbsent(section, k -> new ArrayList<>()).add(row);
            }
            return result;
        }

        private static List<String> splitRow(String line) {
            String body = line.substring(1, line.endsWith("|") && line.length() > 1 ? line.length() - 1 : line.length());
            List<String> result = new ArrayList<>();
            for (String cell : body.split("(?<!\\\\)\\|", -1)) {
                result.add(cell.replace("\\|", "|").replace("`", "").trim());
            }
            return result;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Report
    // ---------------------------------------------------------------------------------------------

    static final class Report {
        final Extraction extraction;
        final ParityMap map;
        final Path repo;

        final Map<String, List<Op>> ops = new LinkedHashMap<>();
        final Set<Op> consumedOps = new HashSet<>();
        final Set<Ignore> usedIgnores = new HashSet<>();
        final Set<String> usedParamAliases = new HashSet<>();
        final List<String> stale = new ArrayList<>();
        final Map<String, List<String>> missingOps = new LinkedHashMap<>();
        final Map<String, List<String>> missingParams = new LinkedHashMap<>();
        final List<String> defaultDiffs = new ArrayList<>();
        final List<String> singleFlavorParams = new ArrayList<>();
        final StringBuilder matrices = new StringBuilder();
        int ignoredParams;

        Report(Extraction extraction, ParityMap map, Path repo) {
            this.extraction = extraction;
            this.map = map;
            this.repo = repo;
            extraction.ops.forEach((flavor, list) -> ops.put(flavor, list.stream()
                    .filter(op -> !ignored(flavor, "operation", op.name(), op.display(), op.declaredIn()))
                    .collect(Collectors.toList())));
        }

        private boolean ignored(String flavor, String kind, String name, String display, String declaredIn) {
            for (Ignore ignore : map.ignores) {
                if (ignore.matches(flavor, kind, name, display, declaredIn)) {
                    usedIgnores.add(ignore);
                    return true;
                }
            }
            return false;
        }

        private List<Op> find(String flavor, String name) {
            String key = norm(name);
            return ops.get(flavor).stream()
                    .filter(op -> norm(op.name()).equals(key) || norm(op.display()).equals(key))
                    .collect(Collectors.toList());
        }

        String render() {
            Map<String, Map<String, List<Op>>> matched = new LinkedHashMap<>();
            Map<String, Map<String, String>> naReasons = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Cell>> e : map.operations.entrySet()) {
                String id = e.getKey();
                Map<String, List<Op>> byFlavor = new LinkedHashMap<>();
                Map<String, String> na = new LinkedHashMap<>();
                for (String flavor : FLAVORS) {
                    Cell cell = e.getValue().get(flavor);
                    if (cell.isNa()) {
                        na.put(flavor, cell.reason());
                        List<Op> hits = find(flavor, id);
                        if (!hits.isEmpty()) {
                            stale.add("operations: `" + id + "` is n/a in " + flavor + " but `" + hits.get(0).name() + "` exists");
                        }
                        continue;
                    }
                    List<Op> found = new ArrayList<>();
                    boolean complete = true;
                    for (String n : cell.names("+", id)) {
                        List<Op> hits = find(flavor, n);
                        if (hits.isEmpty()) {
                            complete = false;
                            if (!cell.isAuto()) {
                                stale.add("operations: `" + id + "` → " + flavor + " `" + n + "` not found");
                            }
                        }
                        found.addAll(hits);
                    }
                    consumedOps.addAll(found);
                    if (complete) {
                        byFlavor.put(flavor, found);
                    } else {
                        missingOps.computeIfAbsent(id, k -> new ArrayList<>()).add(flavor);
                    }
                }
                matched.put(id, byFlavor);
                naReasons.put(id, na);
            }

            for (String id : map.operations.keySet()) {
                renderParams(id, matched.get(id));
            }

            List<Map.Entry<String, Map<String, Cell>>> allParamRows = new ArrayList<>(map.parameters.entrySet());
            map.scopedParameters.values().forEach(m -> allParamRows.addAll(m.entrySet()));
            for (Map.Entry<String, Map<String, Cell>> e : allParamRows) {
                for (String flavor : FLAVORS) {
                    Cell cell = e.getValue().get(flavor);
                    if (!cell.isAuto() && !cell.isNa()) {
                        for (String alias : cell.names(",", e.getKey())) {
                            if (!usedParamAliases.contains(flavor + ":" + norm(alias))) {
                                stale.add("parameters: `" + e.getKey() + "` → " + flavor + " `" + alias + "` never seen");
                            }
                        }
                    }
                }
            }
            for (Waiver x : map.waivers) {
                if (!x.op().equals("*") && map.operations.keySet().stream().noneMatch(id -> norm(id).equals(norm(x.op())))) {
                    stale.add("waivers: unknown operation `" + x.op() + "`");
                }
            }
            for (Ignore ignore : map.ignores) {
                if (!usedIgnores.contains(ignore)) {
                    stale.add("ignore: " + ignore.flavor() + " " + ignore.kind() + " `" + ignore.pattern() + "` matched nothing");
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("# Feature parity report\n\n");
            sb.append("- Generated: ").append(LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append('\n');
            sb.append("- Repository: `").append(repo).append("`").append(gitInfo()).append('\n');
            sb.append("- Flavors: ").append(String.join(", ", FLAVORS)).append('\n');
            sb.append("- Legend: `name=default`, `*` required, ❌ missing (gap), ➖ not applicable (by design), ○ only one flavor has it (see below), · operation unavailable\n\n");

            sb.append("## Summary\n\n");
            sb.append("| flavor | operations | missing operations | missing parameters | unmapped operations |\n");
            sb.append("|---|---|---|---|---|\n");
            for (String flavor : FLAVORS) {
                long available = matched.values().stream().filter(m -> m.containsKey(flavor)).count();
                long expected = naReasons.values().stream().filter(m -> !m.containsKey(flavor)).count();
                long missing = missingOps.values().stream().filter(l -> l.contains(flavor)).count();
                long params = missingParams.values().stream().flatMap(List::stream).filter(s -> s.startsWith("**" + flavor + "**")).count();
                long unmapped = ops.get(flavor).stream().filter(op -> !consumedOps.contains(op)).count();
                sb.append("| ").append(flavor).append(" | ").append(available).append('/').append(expected)
                        .append(" | ").append(missing).append(" | ").append(params).append(" | ").append(unmapped).append(" |\n");
            }
            sb.append('\n');

            sb.append("## Gaps\n\n");
            sb.append("### Missing operations\n\n");
            appendList(sb, missingOps.entrySet().stream()
                    .map(e -> "`" + e.getKey() + "` is missing in " + e.getValue().stream().map(f -> "**" + f + "**").collect(Collectors.joining(", ")))
                    .collect(Collectors.toList()));
            sb.append("### Missing parameters\n\n");
            if (missingParams.isEmpty()) {
                sb.append("_None._\n\n");
            } else {
                missingParams.forEach((op, list) -> sb.append("- `").append(op).append("`: ").append(String.join("; ", list)).append('\n'));
                sb.append('\n');
            }

            sb.append("## Default value differences\n\n");
            appendList(sb, defaultDiffs);

            sb.append("## Parameters found in a single flavor\n\n");
            sb.append("_Either a flavor-specific feature, or a naming mismatch that needs an alias in the parity map._\n\n");
            appendList(sb, singleFlavorParams);

            sb.append("## Unmapped operations\n\n");
            sb.append("_Extracted operations not referenced by the parity map (new features, or entries to add to the map)._\n\n");
            List<String> unmapped = new ArrayList<>();
            for (String flavor : FLAVORS) {
                for (Op op : ops.get(flavor)) {
                    if (!consumedOps.contains(op)) {
                        unmapped.add("**" + flavor + "**: `" + op.name() + "` (" + op.display() + ")");
                    }
                }
            }
            appendList(sb, unmapped);

            sb.append("## Parity map maintenance\n\n");
            appendList(sb, stale);

            sb.append("## Operations\n\n");
            sb.append("| operation |");
            FLAVORS.forEach(f -> sb.append(' ').append(f).append(" |"));
            sb.append("\n|---|").append("---|".repeat(FLAVORS.size())).append('\n');
            for (String id : map.operations.keySet()) {
                sb.append("| **").append(id).append("** |");
                for (String flavor : FLAVORS) {
                    List<Op> found = matched.get(id).get(flavor);
                    String cell;
                    if (found != null) {
                        cell = found.stream().map(op -> "`" + op.display().replace("|", "\\|") + "`")
                                .distinct().collect(Collectors.joining(" + "));
                    } else if (naReasons.get(id).containsKey(flavor)) {
                        String reason = naReasons.get(id).get(flavor);
                        cell = "➖" + (reason.isEmpty() ? "" : " " + reason);
                    } else {
                        cell = "❌";
                    }
                    sb.append(' ').append(cell).append(" |");
                }
                sb.append('\n');
            }
            sb.append('\n');

            sb.append("## Parameters by operation\n\n");
            sb.append(matrices);

            sb.append("## Notes\n\n");
            List<String> notes = new ArrayList<>(extraction.warnings);
            notes.add(ignoredParams + " parameter occurrence(s) ignored by the parity map");
            notes.add(extraction.skippedHidden.size() + " hidden CLI element(s) skipped: "
                    + extraction.skippedHidden.stream().map(s -> s.replace("cli: ", "")).collect(Collectors.joining(", ")));
            appendList(sb, notes);
            return sb.toString();
        }

        private void renderParams(String id, Map<String, List<Op>> byFlavor) {
            // canonical param key -> flavor -> param
            Map<String, Map<String, Param>> rows = new LinkedHashMap<>();
            Map<String, String> labels = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Cell>> e : map.parameterRows(id)) {
                labels.putIfAbsent(norm(e.getKey()), e.getKey());
            }
            for (String flavor : FLAVORS) {
                List<Op> found = byFlavor.get(flavor);
                if (found == null) {
                    continue;
                }
                for (Op op : found) {
                    for (Param p : op.params()) {
                        if (ignored(flavor, "parameter", p.name(), p.display(), p.declaredIn())) {
                            ignoredParams++;
                            continue;
                        }
                        String key = canonicalParam(id, flavor, p);
                        labels.putIfAbsent(key, p.name().replaceAll("^-+|[<>{}]", ""));
                        rows.computeIfAbsent(key, k -> new LinkedHashMap<>()).putIfAbsent(flavor, p);
                    }
                }
            }
            List<String> orderedKeys = new ArrayList<>();
            for (String canonical : map.parameterRows(id).stream().map(Map.Entry::getKey).collect(Collectors.toList())) {
                if (rows.containsKey(norm(canonical)) && !orderedKeys.contains(norm(canonical))) {
                    orderedKeys.add(norm(canonical));
                }
            }
            rows.keySet().stream().filter(k -> !orderedKeys.contains(k)).sorted().forEach(orderedKeys::add);

            matrices.append("### ").append(id).append("\n\n");
            if (rows.isEmpty()) {
                matrices.append("_No parameters._\n\n");
                return;
            }
            matrices.append("| parameter |");
            FLAVORS.forEach(f -> matrices.append(' ').append(f).append(" |"));
            matrices.append("\n|---|").append("---|".repeat(FLAVORS.size())).append('\n');
            long applicable = FLAVORS.stream().filter(byFlavor::containsKey).count();
            for (String key : orderedKeys) {
                String label = labels.get(key);
                Map<String, Param> row = rows.get(key);
                boolean single = row.size() == 1 && applicable > 1;
                matrices.append("| ").append(label).append(" |");
                for (String flavor : FLAVORS) {
                    String cell;
                    Param p = row.get(flavor);
                    if (!byFlavor.containsKey(flavor)) {
                        cell = "·";
                    } else if (p != null) {
                        cell = "`" + (p.display() + (p.required() ? "*" : "")
                                + (p.defaultValue() != null ? "=" + p.defaultValue() : "")).replace("|", "\\|") + "`";
                    } else if (isParamNa(id, label, flavor)) {
                        cell = "➖";
                    } else if (single) {
                        cell = "○";
                    } else {
                        cell = "❌";
                        missingParams.computeIfAbsent(id, k -> new ArrayList<>()).add("**" + flavor + "** lacks `" + label + "`");
                    }
                    matrices.append(' ').append(cell).append(" |");
                }
                matrices.append('\n');

                if (single) {
                    Map.Entry<String, Param> only = row.entrySet().iterator().next();
                    singleFlavorParams.add("`" + id + "`: **" + only.getKey() + "** `" + only.getValue().display() + "`");
                }

                Map<String, String> defaults = new TreeMap<>();
                row.forEach((flavor, p) -> {
                    if (p.constantDefault() && p.defaultValue() != null && !p.defaultValue().contains("${")
                            && map.waivers.stream().noneMatch(x -> x.matches(id, label, flavor, "default"))) {
                        defaults.put(flavor, p.defaultValue());
                    }
                });
                Set<String> distinct = defaults.values().stream()
                        .map(v -> v.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (distinct.size() > 1) {
                    defaultDiffs.add("`" + id + "`.`" + label + "`: " + defaults.entrySet().stream()
                            .map(x -> x.getKey() + "=`" + x.getValue() + "`").collect(Collectors.joining(", ")));
                }
            }
            matrices.append('\n');
        }

        private boolean isParamNa(String op, String param, String flavor) {
            Map<String, Cell> row = map.parameterRows(op).stream()
                    .filter(e -> norm(e.getKey()).equals(norm(param)))
                    .map(Map.Entry::getValue).findFirst().orElse(null);
            if (row != null && row.get(flavor).isNa()) {
                return true;
            }
            return map.waivers.stream().anyMatch(x -> x.matches(op, param, flavor, "n/a"));
        }

        private String canonicalParam(String op, String flavor, Param p) {
            String key = norm(p.name());
            for (Map.Entry<String, Map<String, Cell>> e : map.parameterRows(op)) {
                Cell cell = e.getValue().get(flavor);
                if (!cell.isAuto() && !cell.isNa()) {
                    for (String alias : cell.names(",", e.getKey())) {
                        if (norm(alias).equals(key)) {
                            usedParamAliases.add(flavor + ":" + key);
                            return norm(e.getKey());
                        }
                    }
                }
            }
            for (Map.Entry<String, Map<String, Cell>> e : map.parameters.entrySet()) {
                Cell cell = e.getValue().get(flavor);
                if (cell.isNa() && norm(e.getKey()).equals(key)) {
                    stale.add("parameters: `" + e.getKey() + "` is n/a in " + flavor + " but `" + p.name() + "` exists");
                }
            }
            return key;
        }

        private static void appendList(StringBuilder sb, List<String> items) {
            if (items.isEmpty()) {
                sb.append("_None._\n\n");
                return;
            }
            new LinkedHashSet<>(items).forEach(item -> sb.append("- ").append(item).append('\n'));
            sb.append('\n');
        }

        private String gitInfo() {
            try {
                Path git = repo.resolve(".git");
                if (!Files.isDirectory(git)) {
                    return "";
                }
                String head = Files.readString(git.resolve("HEAD")).trim();
                if (!head.startsWith("ref: ")) {
                    return " @ " + head.substring(0, Math.min(10, head.length()));
                }
                String ref = head.substring(5);
                String branch = ref.replaceFirst("^refs/heads/", "");
                Path refFile = git.resolve(ref);
                String sha = null;
                if (Files.exists(refFile)) {
                    sha = Files.readString(refFile).trim();
                } else if (Files.exists(git.resolve("packed-refs"))) {
                    sha = Files.readAllLines(git.resolve("packed-refs")).stream()
                            .filter(l -> l.endsWith(" " + ref)).map(l -> l.split(" ")[0]).findFirst().orElse(null);
                }
                return " (branch `" + branch + "`" + (sha != null ? " @ " + sha.substring(0, Math.min(10, sha.length())) : "") + ")";
            } catch (IOException ex) {
                return "";
            }
        }
    }

    static final class InventoryRenderer {
        static String render(Extraction extraction) {
            StringBuilder sb = new StringBuilder("# Feature inventory\n\n");
            sb.append("_Raw extraction, before applying the parity map. `*` = required._\n\n");
            extraction.ops.forEach((flavor, list) -> {
                sb.append("## ").append(flavor).append(" (").append(list.size()).append(" operations)\n\n");
                for (Op op : list) {
                    sb.append("- **").append(op.name()).append("** — `").append(op.display()).append("` (").append(op.declaredIn()).append(")\n");
                    for (Param p : op.params()) {
                        sb.append("  - `").append(p.display()).append(p.required() ? "*" : "");
                        if (p.defaultValue() != null) {
                            sb.append("=").append(p.defaultValue());
                        }
                        sb.append("` @").append(p.declaredIn());
                        if (p.note() != null) {
                            sb.append(" — ").append(p.note());
                        }
                        sb.append('\n');
                    }
                }
                sb.append('\n');
            });
            if (!extraction.skippedHidden.isEmpty()) {
                sb.append("## Skipped hidden elements\n\n");
                extraction.skippedHidden.forEach(s -> sb.append("- ").append(s).append('\n'));
                sb.append('\n');
            }
            if (!extraction.warnings.isEmpty()) {
                sb.append("## Warnings\n\n");
                extraction.warnings.forEach(s -> sb.append("- ").append(s).append('\n'));
            }
            return sb.toString();
        }
    }
}
