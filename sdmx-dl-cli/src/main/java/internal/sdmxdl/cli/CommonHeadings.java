package internal.sdmxdl.cli;

import picocli.CommandLine;

@CommandLine.Command(
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        headerHeading = "%n"
)
public class CommonHeadings {

    public static void register(CommandLine cmd) {
        addMixinRecursively(cmd, new CommonHeadings());
    }

    public static void addMixinRecursively(CommandLine cmd, Object mixin) {
        cmd.addMixin(mixin.getClass().getName(), mixin);
        cmd.getSubcommands().forEach((k, v) -> addMixinRecursively(v, mixin));
    }
}
