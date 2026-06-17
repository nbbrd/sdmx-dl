package sdmxdl.swing;

import internal.sdmxdl.swing.ListItemRenderer;
import internal.sdmxdl.swing.MoreSwing;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.Search;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static internal.sdmxdl.swing.MoreSwing.escapeHtml;

/**
 * Step-by-step SDMX flow browser: Sources → Databases → Flows.
 * Visually matches BrowsePanel: dark-blue header with a back button,
 * a breadcrumb bar, {@link ListItemRenderer} cells with favicons / identicons,
 * and styled search fields.
 * Exposes the confirmed selection via the bound {@link #SELECTION_PROPERTY}.
 */
public final class FlowBrowserPanel extends JComponent {

    // ==================== Card names ====================
    private static final String CARD_LOADING = "loading";
    private static final String CARD_ERROR = "error";
    private static final String CARD_SOURCES = "sources";
    private static final String CARD_DATABASES = "databases";
    private static final String CARD_FLOWS = "flows";

    // ==================== Colors ====================
    private static final Color PRIMARY = new Color(0x00, 0x3D, 0x6A);
    private static final Color CHIP_BORDER = new Color(0xE0, 0xE0, 0xE0);

    // ==================== Properties ====================
    public static final String SELECTION_PROPERTY = "selection";

    @lombok.Getter
    private WebFlowRequest selection = null;

    private void setSelection(WebFlowRequest selection) {
        firePropertyChange(SELECTION_PROPERTY, this.selection, this.selection = selection);
    }

    public static final String SOURCE_ICON_PROVIDER_PROPERTY = "sourceIconProvider";

    @lombok.Getter
    private BiFunction<WebSource, Runnable, Icon> sourceIconProvider = (src, repaint) -> new SdmxLogo(32);

    public void setSourceIconProvider(BiFunction<WebSource, Runnable, Icon> sourceIconProvider) {
        firePropertyChange(SOURCE_ICON_PROVIDER_PROPERTY, this.sourceIconProvider, this.sourceIconProvider = sourceIconProvider);
    }

    public static final String MANAGER_PROPERTY = "manager";

    @lombok.Getter
    private SdmxWebManager manager = SdmxWebManager.ofServiceLoader();

    public void setManager(SdmxWebManager manager) {
        firePropertyChange(MANAGER_PROPERTY, this.manager, this.manager = manager);
    }

    public static final String LANGUAGES_PROPERTY = "languages";

    @lombok.Getter
    private Languages languages = Languages.ANY;

    public void setLanguages(Languages languages) {
        firePropertyChange(LANGUAGES_PROPERTY, this.languages, this.languages = languages);
    }

    // ==================== State ====================
    private WebSource currentSource = null;
    private DatabaseRef currentDatabase = DatabaseRef.NO_DATABASE;
    private java.util.List<WebSource> allSources = Collections.emptyList();
    private java.util.List<Database> allDatabases = Collections.emptyList();
    private java.util.List<Flow> allFlows = Collections.emptyList();

    // Back-navigation stack (stores the card we came FROM)
    private final java.util.List<String> navHistory = new ArrayList<>();

    // ==================== Header ====================
    private final JButton backButton = new JButton();
    private final JLabel titleLabel = new JLabel("Select Source");
    private final JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

    // ==================== Content ====================
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JLabel loadingLabel = new JLabel("Loading…", SwingConstants.CENTER);
    private final JLabel errorLabel = new JLabel("", SwingConstants.CENTER);

    // Sources
    private final JTextField sourcesSearch = new JTextField();
    private final DefaultListModel<WebSource> sourcesModel = new DefaultListModel<>();
    private final JList<WebSource> sourcesList = new JList<>(sourcesModel);

    // Databases
    private final JTextField dbSearch = new JTextField();
    private final DefaultListModel<Database> dbModel = new DefaultListModel<>();
    private final JList<Database> dbList = new JList<>(dbModel);

    // Flows
    private final JTextField flowsSearch = new JTextField();
    private final DefaultListModel<Flow> flowsModel = new DefaultListModel<>();
    private final JList<Flow> flowsList = new JList<>(flowsModel);

    public FlowBrowserPanel() {
        initComponents();
    }

    private void initComponents() {
        // --- Header ---
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(Color.WHITE);

        styleNavButton(backButton, "←");
        backButton.setToolTipText("Back");
        backButton.setEnabled(false);
        backButton.addActionListener(e -> goBack());

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        navLeft.setOpaque(false);
        navLeft.add(backButton);

        JPanel navBar = new JPanel(new BorderLayout(4, 0));
        navBar.setBackground(PRIMARY);
        navBar.setBorder(new EmptyBorder(6, 8, 6, 8));
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

        // --- Cell renderers ---
        sourcesList.setCellRenderer(new ListItemRenderer<WebSource>(
                (src, u) -> sourceIconProvider.apply(src, u),
                WebSource::getId,
                src -> src.getName(languages),
                FlowBrowserPanel::buildSourceTooltip));
        sourcesList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && sourcesList.getSelectedIndex() >= 0) {
                    onSourceSelected(sourcesList.getSelectedValue());
                }
            }
        });
        sourcesList.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_ACTION");
        sourcesList.getActionMap().put("ENTER_ACTION", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (sourcesList.getSelectedIndex() >= 0) {
                    onSourceSelected(sourcesList.getSelectedValue());
                }
            }
        });

        dbList.setCellRenderer(new ListItemRenderer<Database>(
                (db, repaint) -> null,
                db -> db.getRef().getId(),
                Database::getName,
                FlowBrowserPanel::buildDatabaseTooltip));
        dbList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && dbList.getSelectedIndex() >= 0) {
                    onDatabaseSelected(dbList.getSelectedValue());
                }
            }
        });
        dbList.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_ACTION");
        dbList.getActionMap().put("ENTER_ACTION", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (dbList.getSelectedIndex() >= 0) {
                    onDatabaseSelected(dbList.getSelectedValue());
                }
            }
        });

        flowsList.setCellRenderer(new ListItemRenderer<Flow>(
                (flow, repaint) -> internal.sdmxdl.swing.IdenticonFactory.getIcon(flow.getRef().toString()),
                flow -> flow.getRef().toShortString(),
                Flow::getName,
                FlowBrowserPanel::buildFlowTooltip));
        flowsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1 && flowsList.getSelectedIndex() >= 0) {
                    onFlowSelected(flowsList.getSelectedValue());
                }
            }
        });
        flowsList.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_ACTION");
        flowsList.getActionMap().put("ENTER_ACTION", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (flowsList.getSelectedIndex() >= 0) {
                    onFlowSelected(flowsList.getSelectedValue());
                }
            }
        });

        // --- Search listeners ---
        sourcesSearch.getDocument().addDocumentListener(MoreSwing.debouncedDocumentListenerOf(200, () -> {
            String text2 = sourcesSearch.getText().trim();
            updateModel(sourcesModel, text2.isEmpty()
                    ? allSources
                    : Search.ofSources(allSources, languages).search(text2, allSources.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
        }));
        dbSearch.getDocument().addDocumentListener(MoreSwing.debouncedDocumentListenerOf(200, () -> {
            String text1 = dbSearch.getText().trim();
            updateModel(dbModel, text1.isEmpty()
                    ? allDatabases
                    : Search.ofDatabases(allDatabases).search(text1, allDatabases.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
        }));
        flowsSearch.getDocument().addDocumentListener(MoreSwing.debouncedDocumentListenerOf(200, () -> {
            String text = flowsSearch.getText().trim();
            updateModel(flowsModel, text.isEmpty()
                    ? allFlows
                    : Search.ofFlows(allFlows).search(text, allFlows.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
        }));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        addPropertyChangeListener(event -> {
            switch (event.getPropertyName()) {
                case SOURCE_ICON_PROVIDER_PROPERTY:
                    repaint();
                    break;
                case MANAGER_PROPERTY:
                case LANGUAGES_PROPERTY:
                    loadSources();
                    break;
            }
        });

        loadSources();
    }

    private static void styleNavButton(JButton btn, String symbol) {
        btn.setText(symbol);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 16f));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(28, 28));
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
        list.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (list.getSelectedIndex() < 0 && list.getModel().getSize() > 0) {
                    list.setSelectedIndex(0);
                }
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout());
        card.add(searchPanel, BorderLayout.NORTH);
        card.add(new JScrollPane(list), BorderLayout.CENTER);
        return card;
    }

    // ==================== Data loading ====================

    private void loadSources() {
        showLoading("Loading sources…");
        new SwingWorker<java.util.List<WebSource>, Void>() {
            @Override
            protected java.util.List<WebSource> doInBackground() {
                return manager.getSources().values().stream()
                        .filter(s -> !s.isAlias())
                        .sorted(java.util.Comparator.comparing(s -> Objects.toString(s.getName(languages))))
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    allSources = get();
                    sourcesSearch.setText("");
                    updateModel(sourcesModel, allSources);
                    showCard(CARD_SOURCES);
                    updateTitle("Select Source");
                    updateBreadcrumb(null, null, null);
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onSourceSelected(WebSource source) {
        currentSource = source;
        currentDatabase = DatabaseRef.NO_DATABASE;
        allDatabases = Collections.emptyList();
        allFlows = Collections.emptyList();
        setSelection(null);
        pushCard(CARD_SOURCES);
        showLoading("Loading databases…");
        updateTitle(source.getName(languages));
        updateBreadcrumb(source, null, null);

        new SwingWorker<java.util.List<Database>, Void>() {
            @Override
            protected java.util.List<Database> doInBackground() throws IOException {
                return new ArrayList<>(manager.using(source)
                        .getDatabases(SourceRequest.builder().languages(languages).build()));
            }

            @Override
            protected void done() {
                try {
                    allDatabases = get();
                    if (allDatabases.isEmpty()) {
                        loadFlows();
                    } else {
                        dbSearch.setText("");
                        updateModel(dbModel, allDatabases);
                        showCard(CARD_DATABASES);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onDatabaseSelected(Database database) {
        currentDatabase = database.getRef();
        allFlows = Collections.emptyList();
        setSelection(null);
        pushCard(CARD_DATABASES);
        updateTitle(database.getName());
        updateBreadcrumb(currentSource, database, null);
        loadFlows();
    }

    private void loadFlows() {
        showLoading("Loading flows…");
        final WebSource src = currentSource;
        final DatabaseRef db = currentDatabase;

        new SwingWorker<java.util.List<Flow>, Void>() {
            @Override
            protected java.util.List<Flow> doInBackground() throws IOException {
                return new ArrayList<>(manager.using(src)
                        .getFlows(DatabaseRequest.builder()
                                .database(db)
                                .languages(languages)
                                .build()));
            }

            @Override
            protected void done() {
                try {
                    allFlows = get();
                    flowsSearch.setText("");
                    updateModel(flowsModel, allFlows);
                    showCard(CARD_FLOWS);
                } catch (InterruptedException | ExecutionException ex) {
                    showError(getRootMessage(ex));
                }
            }
        }.execute();
    }

    private void onFlowSelected(Flow flow) {
        setSelection(WebFlowRequest
                .builder()
                .source(currentSource.getId())
                .request(FlowRequest
                        .builder()
                        .languages(languages)
                        .database(currentDatabase)
                        .flow(flow.getRef())
                        .build())
                .build());
        updateTitle(flow.getName());
        updateBreadcrumb(currentSource, null, flow);
    }

    // ==================== Navigation ====================

    private void pushCard(String card) {
        navHistory.add(card);
        backButton.setEnabled(true);
    }

    private void goBack() {
        if (navHistory.isEmpty()) return;
        String prev = navHistory.remove(navHistory.size() - 1);
        switch (prev) {
            case CARD_SOURCES:
                currentSource = null;
                currentDatabase = DatabaseRef.NO_DATABASE;
                setSelection(null);
                sourcesSearch.setText("");
                updateModel(sourcesModel, allSources);
                showCard(CARD_SOURCES);
                updateTitle("Select Source");
                updateBreadcrumb(null, null, null);
                break;
            case CARD_DATABASES:
                currentDatabase = DatabaseRef.NO_DATABASE;
                setSelection(null);
                dbSearch.setText("");
                updateModel(dbModel, allDatabases);
                showCard(CARD_DATABASES);
                updateTitle(currentSource != null ? currentSource.getName(languages) : "");
                updateBreadcrumb(currentSource, null, null);
                break;
            default:
                break;
        }
        backButton.setEnabled(!navHistory.isEmpty());
    }

    // ==================== UI helpers ====================

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
        switch (card) {
            case CARD_SOURCES:
                SwingUtilities.invokeLater(sourcesSearch::requestFocusInWindow);
                break;
            case CARD_DATABASES:
                SwingUtilities.invokeLater(dbSearch::requestFocusInWindow);
                break;
            case CARD_FLOWS:
                SwingUtilities.invokeLater(flowsSearch::requestFocusInWindow);
                break;
        }
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

    private void updateBreadcrumb(WebSource source, Database database, Flow flow) {
        breadcrumb.removeAll();

        String currentCard = source == null ? CARD_SOURCES
                : database != null ? CARD_DATABASES
                  : CARD_FLOWS;

        String[][] segments;
        if (source == null) {
            segments = new String[][]{{"Sources", CARD_SOURCES}};
        } else if (database != null) {
            segments = new String[][]{
                    {"Sources", CARD_SOURCES},
                    {source.getName(languages), CARD_DATABASES}
            };
            currentCard = CARD_DATABASES;
        } else if (flow != null) {
            segments = new String[][]{
                    {"Sources", CARD_SOURCES},
                    {source.getName(languages), CARD_FLOWS},
                    {flow.getRef().toShortString(), CARD_FLOWS}
            };
            currentCard = CARD_FLOWS;
        } else {
            segments = new String[][]{
                    {"Sources", CARD_SOURCES},
                    {source.getName(languages), CARD_FLOWS}
            };
            currentCard = CARD_FLOWS;
        }

        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                JLabel sep = new JLabel(" › ");
                sep.setForeground(new Color(255, 255, 255, 140));
                sep.setFont(sep.getFont().deriveFont(11f));
                breadcrumb.add(sep);
            }
            boolean isCurrent = (i == segments.length - 1);
            JLabel link = new JLabel(segments[i][0]);
            link.setFont(link.getFont().deriveFont(11f));
            link.setForeground(isCurrent
                    ? new Color(255, 255, 255, 180)
                    : Color.WHITE);
            breadcrumb.add(link);
        }

        breadcrumb.revalidate();
        breadcrumb.repaint();
    }

    private static <T> void updateModel(DefaultListModel<T> model, List<T> items) {
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

    // ==================== Tooltip builders ====================

    private static String buildSourceTooltip(WebSource src) {
        StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
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
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String buildDatabaseTooltip(Database db) {
        StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
        sb.append("<b>").append(escapeHtml(db.getRef().getId())).append("</b>");
        String name = db.getName();
        if (!name.isEmpty() && !name.equals(db.getRef().getId())) {
            sb.append("<br>").append(escapeHtml(name));
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String buildFlowTooltip(Flow flow) {
        StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
        sb.append("<b>").append(escapeHtml(flow.getRef().toString())).append("</b>");
        String name = flow.getName();
        if (!name.isEmpty()) {
            sb.append("<br>").append(escapeHtml(name));
        }
        String description = flow.getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append("<hr>").append(escapeHtml(description));
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}
