package _test;

import com.formdev.flatlaf.FlatLightLaf;
import ec.util.chart.TimeSeriesChart;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.Documents;
import internal.sdmxdl.desktop.util.Ikons;
import internal.sdmxdl.desktop.util.SystemLafColorScheme;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import lombok.NonNull;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import sdmxdl.*;
import sdmxdl.format.Search;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Swing panel that mirrors the functionality of {@code browse.html}: a multi-step
 * SDMX data browser that navigates through sources → databases → flows →
 * dimension picker → data results.
 *
 * <h2>Navigation views</h2>
 * <ol>
 *   <li><b>Sources</b> – searchable list of all registered, non-alias {@link sdmxdl.web.WebSource} entries.</li>
 *   <li><b>Databases</b> – sub-databases for the selected source (skipped automatically when none exist).</li>
 *   <li><b>Flows</b> – searchable list of {@link sdmxdl.Flow} objects for the selected source/database.</li>
 *   <li><b>Dimensions</b> – step-by-step key builder: one chip per {@link sdmxdl.Dimension},
 *       with available codes loaded on demand via
 *       {@link sdmxdl.Connection#getAvailableDimensionCodes}.</li>
 *   <li><b>Data</b> – {@link ec.util.chart.swing.JTimeSeriesChart} above a
 *       per-series {@link javax.swing.JTable} (one tab each).</li>
 * </ol>
 *
 * <h2>Key features</h2>
 * <ul>
 *   <li><b>Fully asynchronous</b> – every API call runs in a {@link javax.swing.SwingWorker};
 *       the EDT is never blocked.</li>
 *   <li><b>Direct Java API</b> – uses {@link sdmxdl.web.SdmxWebManager} directly; no HTTP
 *       round-trip to an external server is required.</li>
 *   <li><b>Live search</b> – debounced {@link javax.swing.event.DocumentListener} filters
 *       sources, databases and flows as the user types.</li>
 *   <li><b>Auto-advance</b> – after selecting a code the picker moves automatically to
 *       the next unfilled dimension.</li>
 *   <li><b>Key display</b> – the SDMX key (e.g. {@code A.BE.EUR}) is kept in sync and
 *       can be copied to the clipboard at any time.</li>
 *   <li><b>Back / Forward / Home</b> – full browser-style history stack so any previously
 *       visited view can be revisited without re-fetching.</li>
 *   <li><b>Breadcrumb</b> – clickable breadcrumb bar reflects the current navigation
 *       depth and allows jumping back to any ancestor view.</li>
 *   <li><b>Responsive code grid</b> – codes are laid out with an internal {@code WrapLayout}
 *       that re-flows on resize, matching the CSS grid used in the HTML version.</li>
 *   <li><b>Demoable</b> – the {@link #main(String[])} entry point enables the built-in
 *       RNG driver and launches a standalone {@link javax.swing.JFrame} with no external
 *       dependencies.</li>
 * </ul>
 */
public final class BrowsePanel extends JComponent {

    // ==================== Card names ====================
    private static final String CARD_LOADING = "loading";
    private static final String CARD_ERROR = "error";
    private static final String CARD_SOURCES = "sources";
    private static final String CARD_DATABASES = "databases";
    private static final String CARD_FLOWS = "flows";
    private static final String CARD_DIMENSIONS = "dimensions";
    private static final String CARD_DATA = "data";

    // ==================== Colors ====================
    private static final Color PRIMARY = new Color(0x00, 0x3D, 0x6A);
    private static final Color ACCENT_GREEN = new Color(0x28, 0xA7, 0x45);
    private static final Color CHIP_BORDER = new Color(0xE0, 0xE0, 0xE0);

    // ==================== State ====================
    private final SdmxWebManager manager;
    private WebSource currentSource = null;
    private DatabaseRef currentDatabase = DatabaseRef.NO_DATABASE;
    private Flow currentFlow = null;
    private Structure currentStructure = null;
    private int selectedDimension = 0;
    private String[] dimensionValues = new String[0];
    private List<String> availableCodes = Collections.emptyList();
    private List<Series> currentData = Collections.emptyList();

    // Navigation
    private final List<String> navHistory = new ArrayList<>();
    private int navIndex = -1;

    // Source/flow data
    private List<WebSource> allSources = Collections.emptyList();
    private List<Database> allDatabases = Collections.emptyList();
    private List<Flow> allFlows = Collections.emptyList();

    // ==================== Header ====================
    private final JButton homeButton = new JButton();
    private final JButton backButton = new JButton();
    private final JButton forwardButton = new JButton();
    private final JLabel titleLabel = new JLabel("Data Browser");
    private final JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

    // ==================== Content ====================
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // Loading card
    private final JLabel loadingLabel = new JLabel("Loading…", SwingConstants.CENTER);

    // Error card
    private final JLabel errorLabel = new JLabel("", SwingConstants.CENTER);

    // Sources card
    private final JTextField sourcesSearch = new JTextField();
    private final DefaultListModel<WebSource> sourcesModel = new DefaultListModel<>();
    private final JList<WebSource> sourcesList = new JList<>(sourcesModel);

    // Databases card
    private final JTextField dbSearch = new JTextField();
    private final DefaultListModel<Database> dbModel = new DefaultListModel<>();
    private final JList<Database> dbList = new JList<>(dbModel);

    // Flows card
    private final JTextField flowsSearch = new JTextField();
    private final DefaultListModel<Flow> flowsModel = new DefaultListModel<>();
    private final JList<Flow> flowsList = new JList<>(flowsModel);

    // Dimensions card
    private final JPanel dimChipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
    private final JPanel codeListPanel = new JPanel(new WrapLayout(6, 6));
    private final JLabel keyLabel = new JLabel("Key: *");
    private final JButton fetchButton = new JButton("Fetch Data ▶");
    private final JButton copyKeyButton = new JButton("⎘ Copy Key");

    // Data card
    private final JTimeSeriesChart dataChart = new JTimeSeriesChart();
    private final JTabbedPane seriesTabs = new JTabbedPane(JTabbedPane.BOTTOM);

    public BrowsePanel(@NonNull SdmxWebManager manager) {
        this.manager = manager;
        initComponents();
    }

    // ==================== Initialisation ====================

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Header ---
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(Color.WHITE);

        homeButton.setToolTipText("Home");
        backButton.setToolTipText("Back");
        forwardButton.setToolTipText("Forward");
        styleNavButton(homeButton, MaterialDesign.MDI_HOME);
        styleNavButton(backButton, MaterialDesign.MDI_ARROW_LEFT);
        styleNavButton(forwardButton, MaterialDesign.MDI_ARROW_RIGHT);

        homeButton.addActionListener(e -> goHome());
        backButton.addActionListener(e -> goBack());
        forwardButton.addActionListener(e -> goForward());

        JPanel navBar = new JPanel(new BorderLayout(4, 0));
        navBar.setBackground(PRIMARY);
        navBar.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        navLeft.setOpaque(false);
        navLeft.add(homeButton);
        navLeft.add(backButton);
        navLeft.add(forwardButton);

        navBar.add(navLeft, BorderLayout.WEST);
        navBar.add(titleLabel, BorderLayout.CENTER);

        breadcrumb.setBackground(PRIMARY.darker());
        breadcrumb.setBorder(new EmptyBorder(2, 8, 2, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.add(navBar, BorderLayout.NORTH);
        header.add(breadcrumb, BorderLayout.SOUTH);

        // --- Content cards ---
        contentPanel.add(buildLoadingCard(), CARD_LOADING);
        contentPanel.add(buildErrorCard(), CARD_ERROR);
        contentPanel.add(buildListCard(sourcesSearch, sourcesList, "Search sources…"), CARD_SOURCES);
        contentPanel.add(buildListCard(dbSearch, dbList, "Search databases…"), CARD_DATABASES);
        contentPanel.add(buildListCard(flowsSearch, flowsList, "Search flows…"), CARD_FLOWS);
        contentPanel.add(buildDimensionsCard(), CARD_DIMENSIONS);
        contentPanel.add(buildDataCard(), CARD_DATA);

        // --- List renderers & listeners ---
        sourcesList.setCellRenderer(new ListItemRenderer<WebSource>(
                (src, repaint) -> src.getWebsite() != null
                        ? SdmxAutoCompletion.FAVICONS.getOrDefault(
                                FaviconRef.of(DomainName.of(src.getWebsite()), 32),
                                repaint,
                                SdmxAutoCompletion.getDefaultIcon())
                        : SdmxAutoCompletion.getDefaultIcon(),
                WebSource::getId,
                src -> src.getName(Languages.ANY),
                BrowsePanel::buildSourceTooltip));
        sourcesList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && sourcesList.getSelectedIndex() >= 0) {
                    onSourceSelected(sourcesList.getSelectedValue());
                }
            }
        });

        dbList.setCellRenderer(new ListItemRenderer<Database>(
                (db, repaint) -> null,
                db -> db.getRef().getId(),
                Database::getName,
                BrowsePanel::buildDatabaseTooltip));
        dbList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && dbList.getSelectedIndex() >= 0) {
                    onDatabaseSelected(dbList.getSelectedValue());
                }
            }
        });

        flowsList.setCellRenderer(new ListItemRenderer<Flow>(
                (flow, repaint) -> createFlowIcon(flow.getRef().toString()),
                flow -> flow.getRef().toString(),
                Flow::getName,
                BrowsePanel::buildFlowTooltip));
        flowsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && flowsList.getSelectedIndex() >= 0) {
                    onFlowSelected(flowsList.getSelectedValue());
                }
            }
        });

        // Search listeners — debounced + hybrid BM25/trigram search via Search API
        sourcesSearch.getDocument().addDocumentListener(debouncedDocumentListener(200, () -> {
            String text = sourcesSearch.getText().trim();
            if (text.isEmpty()) {
                updateListModel(sourcesModel, allSources);
            } else {
                updateListModel(sourcesModel,
                        Search.ofSources(allSources, Languages.ANY)
                                .search(text, allSources.size())
                                .stream().map(Search.Result::getItem)
                                .collect(Collectors.toList()));
            }
        }));
        dbSearch.getDocument().addDocumentListener(debouncedDocumentListener(200, () -> {
            String text = dbSearch.getText().trim();
            if (text.isEmpty()) {
                updateListModel(dbModel, allDatabases);
            } else {
                updateListModel(dbModel,
                        Search.ofDatabases(allDatabases)
                                .search(text, allDatabases.size())
                                .stream().map(Search.Result::getItem)
                                .collect(Collectors.toList()));
            }
        }));
        flowsSearch.getDocument().addDocumentListener(debouncedDocumentListener(200, () -> {
            String text = flowsSearch.getText().trim();
            if (text.isEmpty()) {
                updateListModel(flowsModel, allFlows);
            } else {
                updateListModel(flowsModel,
                        Search.ofFlows(allFlows)
                                .search(text, allFlows.size())
                                .stream().map(Search.Result::getItem)
                                .collect(Collectors.toList()));
            }
        }));

        // Fetch / copy buttons
        fetchButton.setBackground(PRIMARY);
        fetchButton.setForeground(Color.WHITE);
        fetchButton.setFocusPainted(false);
        fetchButton.addActionListener(e -> fetchAndShowData());
        copyKeyButton.addActionListener(e -> copyCurrentKey());

        // Chart
        dataChart.setElementVisible(TimeSeriesChart.Element.LEGEND, true);
        dataChart.setElementVisible(TimeSeriesChart.Element.CROSSHAIR, true);
        dataChart.setElementVisible(TimeSeriesChart.Element.TOOLTIP, true);
        dataChart.setCrosshairOrientation(TimeSeriesChart.CrosshairOrientation.BOTH);
        dataChart.setCrosshairTrigger(TimeSeriesChart.DisplayTrigger.SELECTION);
        dataChart.setLineThickness(2);
        dataChart.setColorSchemeSupport(SwingColorSchemeSupport.from(new SystemLafColorScheme()));

        // Assemble
        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        updateNavButtons();
        loadSources();
    }

    private static void styleNavButton(JButton btn, org.kordamp.ikonli.Ikon ikon) {
        btn.setIcon(Ikons.of(ikon, 18, Color.WHITE));
        btn.setDisabledIcon(Ikons.of(ikon, 18, new Color(255, 255, 255, 70)));
        btn.setText("");
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new java.awt.Dimension(28, 28));
    }

    private JPanel buildLoadingCard() {
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(14f));
        JPanel p = new JPanel(new BorderLayout());
        p.add(loadingLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildErrorCard() {
        errorLabel.setForeground(Color.RED.darker());
        errorLabel.setFont(errorLabel.getFont().deriveFont(13f));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        p.add(errorLabel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildListCard(JTextField searchField, JList<?> list, String placeholder) {
        searchField.putClientProperty("JTextField.placeholderText", placeholder);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CHIP_BORDER),
                new EmptyBorder(6, 8, 6, 8)));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(54);

        JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
        searchPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout());
        card.add(searchPanel, BorderLayout.NORTH);
        card.add(new JScrollPane(list), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDimensionsCard() {
        // Chips row
        JScrollPane chipsScroll = new JScrollPane(dimChipsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chipsScroll.setBorder(null);
        chipsScroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 4));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(8, 8, 4, 8));
        topPanel.add(chipsScroll, BorderLayout.CENTER);

        // Code list in a scroll pane
        JScrollPane codeScroll = new JScrollPane(codeListPanel);
        codeScroll.setBorder(null);

        // Footer
        keyLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        keyLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, PRIMARY),
                new EmptyBorder(4, 8, 4, 8)));

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBorder(new EmptyBorder(8, 8, 8, 8));
        footer.add(keyLabel, BorderLayout.CENTER);

        JPanel footerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        footerButtons.add(copyKeyButton);
        footerButtons.add(fetchButton);
        footer.add(footerButtons, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout());
        card.add(topPanel, BorderLayout.NORTH);
        card.add(codeScroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildDataCard() {
        dataChart.setPreferredSize(new java.awt.Dimension(600, 250));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(dataChart), seriesTabs);
        split.setResizeWeight(0.4);

        JPanel card = new JPanel(new BorderLayout());
        card.add(split, BorderLayout.CENTER);
        return card;
    }

    // ==================== Data loading ====================

    private void loadSources() {
        showLoading("Loading sources…");
        new SwingWorker<List<WebSource>, Void>() {
            @Override
            protected List<WebSource> doInBackground() {
                return manager.getSources().values().stream()
                        .filter(s -> !s.isAlias())
                        .sorted(Comparator.comparing(WebSource::getId))
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    allSources = get();
                    pushView(CARD_SOURCES);
                    sourcesSearch.setText("");
                    updateListModel(sourcesModel, allSources);
                    showCard(CARD_SOURCES);
                    updateTitle("Data Browser");
                    updateBreadcrumb();
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onSourceSelected(WebSource source) {
        currentSource = source;
        currentDatabase = DatabaseRef.NO_DATABASE;
        currentFlow = null;
        currentStructure = null;
        dimensionValues = new String[0];
        currentData = Collections.emptyList();
        allDatabases = Collections.emptyList();
        allFlows = Collections.emptyList();
        showLoading("Loading databases…");
        updateTitle(source.getName(Languages.ANY));
        updateBreadcrumb();

        new SwingWorker<List<Database>, Void>() {
            @Override
            protected List<Database> doInBackground() throws IOException {
                return new ArrayList<>(manager.using(source).getDatabases(
                        SourceRequest.builder().languages(Languages.ANY).build()));
            }

            @Override
            protected void done() {
                try {
                    allDatabases = get();
                    if (allDatabases.isEmpty()) {
                        loadFlows();
                    } else {
                        pushView(CARD_DATABASES);
                        dbSearch.setText("");
                        updateListModel(dbModel, allDatabases);
                        showCard(CARD_DATABASES);
                        updateBreadcrumb();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onDatabaseSelected(Database database) {
        currentDatabase = database.getRef();
        currentFlow = null;
        currentStructure = null;
        dimensionValues = new String[0];
        currentData = Collections.emptyList();
        allFlows = Collections.emptyList();
        updateTitle(database.getName());
        updateBreadcrumb();
        loadFlows();
    }

    private void loadFlows() {
        showLoading("Loading flows…");
        final DatabaseRef db = currentDatabase;
        final WebSource src = currentSource;

        new SwingWorker<List<Flow>, Void>() {
            @Override
            protected List<Flow> doInBackground() throws IOException {
                return new ArrayList<>(manager.using(src).getFlows(
                        DatabaseRequest.builder()
                                .database(db)
                                .languages(Languages.ANY)
                                .build()));
            }

            @Override
            protected void done() {
                try {
                    allFlows = get();
                    pushView(CARD_FLOWS);
                    flowsSearch.setText("");
                    updateListModel(flowsModel, allFlows);
                    showCard(CARD_FLOWS);
                    updateBreadcrumb();
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onFlowSelected(Flow flow) {
        currentFlow = flow;
        currentStructure = null;
        dimensionValues = new String[0];
        currentData = Collections.emptyList();
        updateTitle("Select Data");
        showLoading("Loading structure…");

        final WebSource src = currentSource;
        final DatabaseRef db = currentDatabase;
        final FlowRef flowRef = flow.getRef();

        new SwingWorker<Structure, Void>() {
            @Override
            protected Structure doInBackground() throws IOException {
                try (Connection conn = manager.getConnection(src, Languages.ANY)) {
                    return conn.getMeta(db, flowRef).getStructure();
                }
            }

            @Override
            protected void done() {
                try {
                    currentStructure = get();
                    dimensionValues = new String[currentStructure.getDimensions().size()];
                    Arrays.fill(dimensionValues, "");
                    selectedDimension = 0;
                    pushView(CARD_DIMENSIONS);
                    showDimensionsView();
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void showDimensionsView() {
        refreshDimChips();
        loadCodesForCurrentDimension();
        updateKeyLabel();
        fetchButton.setEnabled(isKeyComplete());
        showCard(CARD_DIMENSIONS);
        updateBreadcrumb();
    }

    private void refreshDimChips() {
        dimChipsPanel.removeAll();
        if (currentStructure == null) return;

        List<sdmxdl.Dimension> dims = currentStructure.getDimensions();
        for (int i = 0; i < dims.size(); i++) {
            final int idx = i;
            sdmxdl.Dimension dim = dims.get(i);
            boolean hasValue = dimensionValues[i] != null && !dimensionValues[i].isEmpty();
            boolean isActive = i == selectedDimension;

            JButton chip = new JButton();
            String label = (idx + 1) + ". " + dim.getName();
            if (hasValue) {
                label += " ✓";
            }
            chip.setText(label);
            chip.setFont(chip.getFont().deriveFont(11f));
            chip.setFocusPainted(false);

            if (isActive) {
                chip.setBackground(PRIMARY);
                chip.setForeground(Color.WHITE);
                chip.setBorderPainted(false);
                chip.setOpaque(true);
            } else if (hasValue) {
                chip.setBackground(ACCENT_GREEN.brighter());
                chip.setForeground(ACCENT_GREEN.darker());
                chip.setBorderPainted(true);
                chip.setOpaque(true);
            } else {
                chip.setOpaque(false);
            }

            chip.addActionListener(e -> {
                selectedDimension = idx;
                showDimensionsView();
            });
            dimChipsPanel.add(chip);
        }
        dimChipsPanel.revalidate();
        dimChipsPanel.repaint();
    }

    private void loadCodesForCurrentDimension() {
        codeListPanel.removeAll();
        JLabel waiting = new JLabel("Loading codes…", SwingConstants.CENTER);
        waiting.setForeground(Color.GRAY);
        codeListPanel.add(waiting);
        codeListPanel.revalidate();
        codeListPanel.repaint();

        final WebSource src = currentSource;
        final DatabaseRef db = currentDatabase;
        final FlowRef flowRef = currentFlow.getRef();
        final Key constraintKey = buildCurrentKey();
        final int dimIdx = selectedDimension;
        final Structure structure = currentStructure;

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws IOException {
                try (Connection conn = manager.getConnection(src, Languages.ANY)) {
                    return new ArrayList<>(conn.getAvailableDimensionCodes(db, flowRef, constraintKey, dimIdx));
                }
            }

            @Override
            protected void done() {
                try {
                    availableCodes = get();
                    showCodeChips(availableCodes, structure, dimIdx);
                } catch (InterruptedException | ExecutionException ex) {
                    codeListPanel.removeAll();
                    JLabel err = new JLabel("Could not load codes: " + getRootMessage(ex));
                    err.setForeground(Color.RED.darker());
                    err.setBorder(new EmptyBorder(8, 8, 8, 8));
                    codeListPanel.add(err);
                    codeListPanel.revalidate();
                    codeListPanel.repaint();
                }
            }
        }.execute();
    }

    private void showCodeChips(List<String> codes, Structure structure, int dimIdx) {
        codeListPanel.removeAll();
        if (codes.isEmpty()) {
            JLabel empty = new JLabel("No codes available", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            codeListPanel.add(empty);
        } else {
            sdmxdl.Dimension dim = structure.getDimensions().get(dimIdx);
            Map<String, String> codeNames = dim.isCoded() ? dim.getCodes() : Collections.emptyMap();
            String selected = dimensionValues[dimIdx];

            for (String code : codes) {
                String name = codeNames.getOrDefault(code, "");
                JButton chip = buildCodeChip(code, name, code.equals(selected), dimIdx);
                codeListPanel.add(chip);
            }
        }
        codeListPanel.revalidate();
        codeListPanel.repaint();
    }

    private JButton buildCodeChip(String code, String name, boolean selected, int dimIdx) {
        String label = "<html><b>" + code + "</b>"
                + (name.isEmpty() ? "" : "<br><small>" + name + "</small>") + "</html>";
        JButton chip = new JButton(label);
        chip.setFont(chip.getFont().deriveFont(11f));
        chip.setFocusPainted(false);
        chip.setPreferredSize(new java.awt.Dimension(130, 54));
        chip.setHorizontalAlignment(SwingConstants.CENTER);

        if (selected) {
            chip.setBackground(PRIMARY);
            chip.setForeground(Color.WHITE);
            chip.setBorderPainted(false);
            chip.setOpaque(true);
        } else {
            chip.setBorder(BorderFactory.createLineBorder(CHIP_BORDER));
        }

        chip.addActionListener(e -> onCodeSelected(dimIdx, code));
        return chip;
    }

    private void onCodeSelected(int dimIdx, String code) {
        dimensionValues[dimIdx] = code;
        // Auto-advance to next unfilled dimension
        if (dimIdx < dimensionValues.length - 1) {
            selectedDimension = dimIdx + 1;
        } else {
            selectedDimension = dimIdx;
        }
        updateKeyLabel();
        fetchButton.setEnabled(isKeyComplete());
        showDimensionsView();
    }

    private void fetchAndShowData() {
        if (!isKeyComplete()) {
            JOptionPane.showMessageDialog(this, "Please select a value for all dimensions.");
            return;
        }

        showLoading("Fetching data…");
        updateTitle("Data Results");

        final WebSource src = currentSource;
        final DatabaseRef db = currentDatabase;
        final FlowRef flowRef = currentFlow.getRef();
        final Key key = buildCurrentKey();

        new SwingWorker<List<Series>, Void>() {
            @Override
            protected List<Series> doInBackground() throws IOException {
                Query query = Query.builder().key(key).detail(Detail.FULL).build();
                try (Connection conn = manager.getConnection(src, Languages.ANY);
                     Stream<Series> stream = conn.getDataStream(db, flowRef, query)) {
                    return stream.collect(Collectors.toList());
                }
            }

            @Override
            protected void done() {
                try {
                    currentData = get();
                    pushView(CARD_DATA);
                    showDataView(currentData);
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void showDataView(List<Series> data) {
        // Build chart dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (Series series : data) {
            TimeSeries ts = new TimeSeries(series.getKey().toString());
            for (Obs obs : series.getObs()) {
                try {
                    Millisecond period = new Millisecond(
                            Timestamp.valueOf(obs.getPeriod().getStart()));
                    ts.addOrUpdate(period, obs.getValue());
                } catch (Exception ignored) {
                    // skip unparseable obs
                }
            }
            if (!ts.isEmpty()) {
                dataset.addSeries(ts);
            }
        }
        dataChart.setDataset(dataset);

        // Build series tabs
        seriesTabs.removeAll();
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        for (Series series : data) {
            List<Obs> obs = new ArrayList<>(series.getObs());
            String[] columns = {"Period", "Value"};
            Object[][] rows = new Object[obs.size()][2];
            for (int i = 0; i < obs.size(); i++) {
                rows[i][0] = obs.get(i).getPeriod().getStartAsShortString();
                rows[i][1] = nf.format(obs.get(i).getValue());
            }
            JTable table = new JTable(new DefaultTableModel(rows, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            table.setAutoCreateRowSorter(true);
            table.getColumnModel().getColumn(1).setCellRenderer(
                    new RightAlignedRenderer());

            String tabTitle = series.getKey().toString();
            if (tabTitle.length() > 40) {
                tabTitle = tabTitle.substring(0, 37) + "…";
            }
            seriesTabs.addTab(tabTitle, new JScrollPane(table));
            seriesTabs.setToolTipTextAt(seriesTabs.getTabCount() - 1,
                    series.getKey() + " (" + obs.size() + " obs)");
        }

        showCard(CARD_DATA);
        updateBreadcrumb();
    }

    // ==================== Navigation ====================

    private void pushView(String card) {
        // Truncate forward history
        while (navHistory.size() > navIndex + 1) {
            navHistory.remove(navHistory.size() - 1);
        }
        navHistory.add(card);
        navIndex = navHistory.size() - 1;
        updateNavButtons();
    }

    private void goBack() {
        if (navIndex <= 0) return;
        navIndex--;
        navigateToView(navHistory.get(navIndex));
        updateNavButtons();
    }

    private void goForward() {
        if (navIndex >= navHistory.size() - 1) return;
        navIndex++;
        navigateToView(navHistory.get(navIndex));
        updateNavButtons();
    }

    private void goHome() {
        currentSource = null;
        currentDatabase = DatabaseRef.NO_DATABASE;
        currentFlow = null;
        currentStructure = null;
        dimensionValues = new String[0];
        currentData = Collections.emptyList();
        navHistory.clear();
        navIndex = -1;
        allDatabases = Collections.emptyList();
        allFlows = Collections.emptyList();
        updateNavButtons();
        loadSources();
    }

    private void navigateToView(String card) {
        switch (card) {
            case CARD_SOURCES:
                sourcesSearch.setText("");
                updateListModel(sourcesModel, allSources);
                showCard(CARD_SOURCES);
                updateTitle("Data Browser");
                break;
            case CARD_DATABASES:
                dbSearch.setText("");
                updateListModel(dbModel, allDatabases);
                showCard(CARD_DATABASES);
                if (currentSource != null) updateTitle(currentSource.getName(Languages.ANY));
                break;
            case CARD_FLOWS:
                flowsSearch.setText("");
                updateListModel(flowsModel, allFlows);
                showCard(CARD_FLOWS);
                break;
            case CARD_DIMENSIONS:
                if (currentStructure != null) {
                    showDimensionsView();
                }
                break;
            case CARD_DATA:
                if (!currentData.isEmpty()) {
                    showDataView(currentData);
                }
                break;
            default:
                break;
        }
        updateBreadcrumb();
    }

    private void updateNavButtons() {
        homeButton.setEnabled(navIndex > 0);
        backButton.setEnabled(navIndex > 0);
        forwardButton.setEnabled(navIndex < navHistory.size() - 1);
    }

    // ==================== UI helpers ====================

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
    }

    private void showLoading(String message) {
        loadingLabel.setText(message);
        showCard(CARD_LOADING);
    }

    private void showError(String message) {
        errorLabel.setText("<html><b>Error:</b> " + message + "</html>");
        showCard(CARD_ERROR);
    }

    private void updateTitle(String title) {
        titleLabel.setText(title);
    }

    private void updateBreadcrumb() {
        breadcrumb.removeAll();
        List<String[]> segments = new ArrayList<>();
        segments.add(new String[]{"Sources", CARD_SOURCES});
        if (currentSource != null) {
            segments.add(new String[]{currentSource.getName(Languages.ANY), CARD_DATABASES});
        }
        if (currentFlow != null) {
            String name = currentFlow.getName();
            if (name.length() > 35) name = name.substring(0, 32) + "…";
            segments.add(new String[]{name, CARD_DIMENSIONS});
        }
        if (!currentData.isEmpty()) {
            segments.add(new String[]{"Data", CARD_DATA});
        }

        String currentCard = navIndex >= 0 && navIndex < navHistory.size()
                ? navHistory.get(navIndex) : CARD_SOURCES;

        for (int i = 0; i < segments.size(); i++) {
            String[] seg = segments.get(i);
            if (i > 0) {
                JLabel sep = new JLabel(" › ");
                sep.setForeground(new Color(255, 255, 255, 140));
                sep.setFont(sep.getFont().deriveFont(11f));
                breadcrumb.add(sep);
            }
            boolean isCurrent = seg[1].equals(currentCard);
            JLabel link = new JLabel(seg[0]);
            link.setFont(link.getFont().deriveFont(11f));
            link.setForeground(isCurrent
                    ? new Color(255, 255, 255, 180)
                    : Color.WHITE);
            if (!isCurrent) {
                link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final String targetCard = seg[1];
                link.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        navigateBreadcrumb(targetCard);
                    }
                });
            }
            breadcrumb.add(link);
        }
        breadcrumb.revalidate();
        breadcrumb.repaint();
    }

    private void navigateBreadcrumb(String targetCard) {
        for (int i = navHistory.size() - 1; i >= 0; i--) {
            if (navHistory.get(i).equals(targetCard)) {
                navIndex = i;
                navigateToView(targetCard);
                updateNavButtons();
                return;
            }
        }
    }

    private void updateKeyLabel() {
        StringBuilder sb = new StringBuilder("Key: ");
        for (int i = 0; i < dimensionValues.length; i++) {
            if (i > 0) sb.append('.');
            String v = dimensionValues[i];
            sb.append((v == null || v.isEmpty()) ? "*" : v);
        }
        keyLabel.setText(sb.toString());
    }

    private boolean isKeyComplete() {
        if (dimensionValues.length == 0) return false;
        for (String v : dimensionValues) {
            if (v == null || v.isEmpty()) return false;
        }
        return true;
    }

    private Key buildCurrentKey() {
        return Key.of(Arrays.asList(dimensionValues));
    }

    private void copyCurrentKey() {
        String key = buildCurrentKey().toString();
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(key), null);
    }

    private static <T> void updateListModel(DefaultListModel<T> model, List<T> items) {
        model.clear();
        for (T item : items) {
            model.addElement(item);
        }
    }

    private static String getRootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        String msg = cause.getMessage();
        return (msg != null && !msg.isEmpty()) ? msg : cause.getClass().getSimpleName();
    }

    /**
     * Returns a {@link javax.swing.event.DocumentListener} that fires {@code action} only after
     * the user has stopped typing for {@code delayMs} milliseconds (debounce).
     * Each new document event restarts the countdown, so the action is never
     * called more frequently than once per {@code delayMs} window.
     */
    private static javax.swing.event.DocumentListener debouncedDocumentListener(int delayMs, Runnable action) {
        javax.swing.Timer[] slot = {null};
        return Documents.documentListenerOf(e -> {
            if (slot[0] != null) slot[0].stop();
            slot[0] = new javax.swing.Timer(delayMs, evt -> action.run());
            slot[0].setRepeats(false);
            slot[0].start();
        });
    }

    // ==================== Tooltip builders ====================

    private static String buildSourceTooltip(WebSource src) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>").append(src.getId()).append("</b>");
        String name = src.getName(Languages.ANY);
        if (name != null && !name.isEmpty() && !name.equals(src.getId())) {
            sb.append("<br>").append(escapeHtml(name));
        }
        sb.append("<br><small>Driver: ").append(escapeHtml(src.getDriver())).append("</small>");
        sb.append("<br><small>Endpoint: ").append(escapeHtml(src.getEndpoint().toString())).append("</small>");
        if (src.getWebsite() != null) {
            sb.append("<br><small>Website: ").append(escapeHtml(src.getWebsite().toString())).append("</small>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    private static String buildDatabaseTooltip(Database db) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>").append(escapeHtml(db.getRef().getId())).append("</b>");
        String name = db.getName();
        if (name != null && !name.isEmpty() && !name.equals(db.getRef().getId())) {
            sb.append("<br>").append(escapeHtml(name));
        }
        sb.append("</html>");
        return sb.toString();
    }

    private static String buildFlowTooltip(Flow flow) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>").append(escapeHtml(flow.getRef().toString())).append("</b>");
        String name = flow.getName();
        if (name != null && !name.isEmpty()) {
            sb.append("<br>").append(escapeHtml(name));
        }
        String description = flow.getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append("<br><small>").append(escapeHtml(description)).append("</small>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ==================== Flow identicons ====================

    /** Color palette matching the one in {@code getFlowIcon()} of browse.html. */
    private static final Color[] IDENTICON_COLORS = {
            new Color(0x09, 0x69, 0xDA), new Color(0x1F, 0x88, 0x3D),
            new Color(0xCF, 0x22, 0x2E), new Color(0x82, 0x50, 0xDF),
            new Color(0xBF, 0x39, 0x89), new Color(0xFB, 0x85, 0x00),
            new Color(0x05, 0x50, 0xAE), new Color(0x11, 0x63, 0x29),
            new Color(0xA4, 0x0E, 0x26), new Color(0x66, 0x39, 0xBA),
            new Color(0xD1, 0x24, 0x2F), new Color(0x09, 0x69, 0xDA),
            new Color(0x21, 0x8B, 0xFF), new Color(0x7D, 0x4E, 0x57),
            new Color(0x95, 0x38, 0x00), new Color(0x66, 0x39, 0xBA),
            new Color(0x8B, 0x5C, 0xF6), new Color(0x3B, 0x82, 0xF6),
            new Color(0x10, 0xB9, 0x81), new Color(0xF5, 0x9E, 0x0B)
    };

    private static final Map<String, Icon> FLOW_ICON_CACHE = new HashMap<>();

    private static Icon createFlowIcon(String id) {
        return FLOW_ICON_CACHE.computeIfAbsent(id, FlowIdenticonFactory::create);
    }

    /**
     * Generates a 30×30 identicon that mimics the symmetric 5×5 pixel-art
     * produced by {@code getFlowIcon()} in browse.html:
     * <ul>
     *   <li>Hash the flow ref string with the same JS-equivalent algorithm.</li>
     *   <li>Select a base color from the palette by {@code hash % palette.length}.</li>
     *   <li>For each of the 5×5 cells, use a seeded pseudo-random value ({@code sin}-based)
     *       to decide fill/no-fill, then mirror left↔right so the pattern is symmetric.</li>
     *   <li>Each filled cell gets a variable opacity in [0.7, 1.0] for depth.</li>
     * </ul>
     */
    private static final class FlowIdenticonFactory {

        private static final int SIZE = 30;   // icon pixel size
        private static final int GRID = 5;    // cells per side
        private static final int CELL = SIZE / GRID; // 6 px per cell

        static Icon create(String id) {
            int hash = flowHash(id);
            int absHash = (hash == Integer.MIN_VALUE) ? 0 : Math.abs(hash);
            Color base = IDENTICON_COLORS[absHash % IDENTICON_COLORS.length];

            BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background: #f6f8fa (same as HTML)
            g.setColor(new Color(0xF6, 0xF8, 0xFA));
            g.fillRoundRect(0, 0, SIZE, SIZE, 4, 4);

            // Symmetric 5×5 grid: generate left half + centre, mirror to right
            int halfCols = (GRID + 1) / 2; // ceil(5/2) = 3 → cols 0,1,2
            for (int row = 0; row < GRID; row++) {
                for (int col = 0; col < halfCols; col++) {
                    int seed = absHash + row * GRID + col;
                    if (seededRandom(seed) > 0.5) {
                        double opacity = 0.7 + seededRandom(seed + 100) * 0.3;
                        int alpha = Math.min(255, (int) (opacity * 255));
                        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
                        g.fillRect(col * CELL, row * CELL, CELL, CELL);
                        // Mirror left↔right (skip the middle column)
                        if (col < GRID / 2) {
                            g.fillRect((GRID - 1 - col) * CELL, row * CELL, CELL, CELL);
                        }
                    }
                }
            }
            g.dispose();
            return new ImageIcon(img);
        }

        /** Same hash as the JS {@code hashCode(str)} in browse.html. */
        private static int flowHash(String str) {
            int hash = 0;
            for (int i = 0; i < str.length(); i++) {
                hash = ((hash << 5) - hash) + str.charAt(i);
            }
            return hash;
        }

        /** Same PRNG as the JS {@code seededRandom(seed)} in browse.html. */
        private static double seededRandom(int seed) {
            double x = Math.sin(seed) * 10_000;
            return x - Math.floor(x);
        }
    }

    // ==================== Cell Renderers ====================

    /**
     * Icon provider for {@link ListItemRenderer}: receives the item and a {@code repaint}
     * callback so async icons (e.g. favicons) can trigger a list repaint when they arrive.
     */
    @FunctionalInterface
    public interface IconProvider<T> {
        Icon getIcon(T item, Runnable repaint);
    }

    /**
     * Generic list cell renderer that paints an icon, a main (bold) line and a secondary (muted)
     * line directly via {@link Graphics2D} — no nested Swing components are composed.
     * Long text is clipped to the available width with a trailing ellipsis,
     * computed with a binary search over {@link FontMetrics#stringWidth}.
     */
    private static final class ListItemRenderer<T> extends JComponent implements ListCellRenderer<T> {

        private static final int PAD_H = 10;
        private static final int PAD_V = 6;
        private static final int ICON_GAP = 8;
        private static final int ACCENT_W = 3;
        private static final int TEXT_GAP = 2;
        private static final String ELLIPSIS = "…";

        private final IconProvider<T> iconProvider;
        private final java.util.function.Function<T, String> mainTextFn;
        private final java.util.function.Function<T, String> secondaryTextFn;
        private final java.util.function.Function<T, String> tooltipFn;

        // Mutable rendering state — set in getListCellRendererComponent, read in paintComponent
        private Icon currentIcon;
        private String currentMain = "";
        private String currentSecondary = "";
        private boolean isSelected;

        ListItemRenderer(
                IconProvider<T> iconProvider,
                java.util.function.Function<T, String> mainTextFn,
                java.util.function.Function<T, String> secondaryTextFn,
                java.util.function.Function<T, String> tooltipFn) {
            this.iconProvider = iconProvider;
            this.mainTextFn = mainTextFn;
            this.secondaryTextFn = secondaryTextFn;
            this.tooltipFn = tooltipFn;
            setOpaque(true);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<? extends T> list, T value, int index, boolean selected, boolean cellHasFocus) {
            this.currentIcon = (value != null) ? iconProvider.getIcon(value, list::repaint) : null;
            this.currentMain = (value != null) ? mainTextFn.apply(value) : "";
            this.currentSecondary = (value != null) ? secondaryTextFn.apply(value) : "";
            this.isSelected = selected;
            setBackground(selected ? list.getSelectionBackground() : list.getBackground());
            setForeground(selected ? list.getSelectionForeground() : list.getForeground());
            setFont(list.getFont());
            setToolTipText((value != null && tooltipFn != null) ? tooltipFn.apply(value) : null);
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Background
                g.setColor(getBackground());
                g.fillRect(0, 0, w, h);

                // Left accent bar
                g.setColor(PRIMARY);
                g.fillRect(0, 0, ACCENT_W, h);

                // Icon (vertically centred)
                int contentX = ACCENT_W + PAD_H;
                if (currentIcon != null) {
                    int iconY = (h - currentIcon.getIconHeight()) / 2;
                    currentIcon.paintIcon(this, g, contentX, iconY);
                    contentX += currentIcon.getIconWidth() + ICON_GAP;
                }

                int availW = w - contentX - PAD_H;

                // Fonts & metrics
                Font mainFont = getFont().deriveFont(Font.BOLD, 13f);
                Font secFont = getFont().deriveFont(Font.PLAIN, 11f);
                FontMetrics mainFm = g.getFontMetrics(mainFont);
                FontMetrics secFm = g.getFontMetrics(secFont);

                // Vertical layout: centre the two-line block
                int blockH = mainFm.getHeight() + TEXT_GAP + secFm.getHeight();
                int blockTop = Math.max(PAD_V, (h - blockH) / 2);

                // Main text
                g.setFont(mainFont);
                g.setColor(getForeground());
                g.drawString(ellipsize(currentMain, mainFm, availW),
                        contentX, blockTop + mainFm.getAscent());

                // Secondary text (muted when not selected)
                g.setFont(secFont);
                g.setColor(isSelected ? getForeground() : new Color(0x66, 0x66, 0x66));
                g.drawString(ellipsize(currentSecondary, secFm, availW),
                        contentX, blockTop + mainFm.getHeight() + TEXT_GAP + secFm.getAscent());

                // Bottom separator (only when not selected)
                if (!isSelected) {
                    g.setColor(CHIP_BORDER);
                    g.drawLine(ACCENT_W, h - 1, w - 1, h - 1);
                }
            } finally {
                g.dispose();
            }
        }

        /** Clips {@code text} to {@code maxWidth} px using binary search, appending "…" if needed. */
        private static String ellipsize(String text, FontMetrics fm, int maxWidth) {
            if (text == null || text.isEmpty()) return "";
            if (fm.stringWidth(text) <= maxWidth) return text;
            int ellipsisW = fm.stringWidth(ELLIPSIS);
            int available = maxWidth - ellipsisW;
            if (available <= 0) return ELLIPSIS;
            int lo = 0, hi = text.length();
            while (lo < hi) {
                int mid = (lo + hi + 1) >>> 1;
                if (fm.stringWidth(text.substring(0, mid)) <= available) lo = mid;
                else hi = mid - 1;
            }
            return text.substring(0, lo) + ELLIPSIS;
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(200, 54);
        }
    }

    private static final class RightAlignedRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.TRAILING);
            return label;
        }
    }

    // ==================== WrapLayout inner class ====================

    /**
     * A simple wrap-layout that wraps components like CSS flexbox (left-aligned).
     */
    private static final class WrapLayout extends FlowLayout {
        WrapLayout(int hgap, int vgap) {
            super(FlowLayout.LEFT, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                Container container = target;
                while (container.getSize().width == 0 && container.getParent() != null) {
                    container = container.getParent();
                }
                int targetWidth = container.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) rowWidth += hgap;
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }

        private static void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += /* vgap */ 6;
            dim.height += rowHeight;
        }
    }

    // ==================== Demo main ====================

    public static void main(String[] args) {
        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(source -> (marker, message) ->
                        System.out.printf(Locale.ROOT, "[%s] (%s) %s%n", source.getId(), marker, message))
                .onError(source -> (marker, message, error) ->
                        System.err.printf(Locale.ROOT, "[%s] (%s) %s: %s%n", source.getId(), marker, message, error.getMessage()))
                .build()
                .warmupAsync();

        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            JFrame frame = new JFrame("SDMX Data Browser");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new BrowsePanel(manager));
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
















