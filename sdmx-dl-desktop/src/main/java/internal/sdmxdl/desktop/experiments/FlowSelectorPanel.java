package internal.sdmxdl.desktop.experiments;

import com.formdev.flatlaf.FlatIconColors;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.Ikons;
import lombok.NonNull;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.*;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.format.Search;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static internal.sdmxdl.desktop.util.Documents.documentListenerOf;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_CHEVRON_RIGHT;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_DATABASE;

/**
 * A reusable Swing component that displays the currently selected SDMX dataflow
 * and allows the user to pick a new one through a step-by-step browser dialog:
 * <ol>
 *   <li><b>Sources</b> – searchable list of all non-alias {@link WebSource} entries.</li>
 *   <li><b>Databases</b> – sub-databases for the chosen source
 *       (skipped automatically when none exist).</li>
 *   <li><b>Flows</b> – searchable list of {@link Flow} objects; double-clicking
 *       a flow confirms the selection.</li>
 * </ol>
 *
 * <p>The selected flow is exposed through the bound {@link #MODEL_PROPERTY} as a
 * {@link DataSourceRef} that carries the source id, database ref, and flow ref.</p>
 *
 * <p>The component integrates with {@link Sdmxdl#INSTANCE} to obtain the
 * {@link SdmxWebManager} and the configured {@link Languages}.</p>
 */
public final class FlowSelectorPanel extends JComponent {

    public static final String MODEL_PROPERTY = "model";

    @lombok.Getter
    private DataSourceRef model;

    public void setModel(DataSourceRef model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JLabel summaryLabel = new JLabel();
    private final JButton browseButton = new JButton();

    public FlowSelectorPanel() {
        initComponents();
    }

    private void initComponents() {
        summaryLabel.setText("No flow selected");
        summaryLabel.setIcon(Ikons.of(MDI_DATABASE, 16, FlatIconColors.ACTIONS_GREYINLINE.key));

        browseButton.setText("Browse…");
        browseButton.setIcon(Ikons.of(MDI_CHEVRON_RIGHT, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        browseButton.addActionListener(e -> showBrowseDialog());

        setLayout(new BorderLayout(4, 0));
        setBorder(new EmptyBorder(2, 2, 2, 2));
        add(summaryLabel, BorderLayout.CENTER);
        add(browseButton, BorderLayout.EAST);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        DataSourceRef ref = (DataSourceRef) evt.getNewValue();
        if (ref == null || ref.getSource().isEmpty()) {
            summaryLabel.setText("No flow selected");
            summaryLabel.setIcon(Ikons.of(MDI_DATABASE, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        } else {
            StringBuilder sb = new StringBuilder(ref.getSource());
            if (!ref.getDatabase().equals(DatabaseRef.NO_DATABASE)) {
                sb.append(" / ").append(ref.getDatabase());
            }
            if (!ref.getFlow().isEmpty()) {
                sb.append(" / ").append(ref.getFlow());
            }
            summaryLabel.setText(sb.toString());
            SdmxWebManager manager = Sdmxdl.INSTANCE.getSdmxManager();
            WebSource source = manager.getSources().get(ref.getSource());
            summaryLabel.setIcon(source != null
                    ? SdmxAutoCompletion.getFavicon(source.getWebsite())
                    : Ikons.of(MDI_DATABASE, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        }
    }

    private void showBrowseDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = owner instanceof Frame
                ? new JDialog((Frame) owner, "Select Flow", true)
                : new JDialog((Dialog) owner, "Select Flow", true);

        FlowBrowserPanel browser = new FlowBrowserPanel(
                Sdmxdl.INSTANCE.getSdmxManager(),
                Sdmxdl.INSTANCE.getLanguages());

        JButton selectButton = new JButton("Select");
        selectButton.setEnabled(false);
        JButton cancelButton = new JButton("Cancel");

        browser.addPropertyChangeListener(FlowBrowserPanel.SELECTION_PROPERTY, evt -> selectButton.setEnabled(browser.getSelection() != null));

        selectButton.addActionListener(e -> {
            DataSourceRef selection = browser.getSelection();
            if (selection != null) {
                setModel(selection);
                dialog.dispose();
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonPanel.add(cancelButton);
        buttonPanel.add(selectButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(browser, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(600, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==================== Inner browser panel ====================

    /**
     * Step-by-step SDMX flow browser: Sources → Databases → Flows.
     * Visually matches {@link BrowsePanel}: dark-blue header with a back button,
     * a breadcrumb bar, {@link ListItemRenderer} cells with favicons / identicons,
     * and styled search fields.
     * Exposes the confirmed selection via the bound {@link #SELECTION_PROPERTY}.
     */
    static final class FlowBrowserPanel extends JComponent {

        public static final String SELECTION_PROPERTY = "selection";

        // ==================== Card names ====================
        private static final String CARD_LOADING = "loading";
        private static final String CARD_ERROR = "error";
        private static final String CARD_SOURCES = "sources";
        private static final String CARD_DATABASES = "databases";
        private static final String CARD_FLOWS = "flows";

        // ==================== Colors ====================
        private static final Color PRIMARY = new Color(0x00, 0x3D, 0x6A);
        private static final Color CHIP_BORDER = new Color(0xE0, 0xE0, 0xE0);

        // ==================== Selection ====================
        @lombok.Getter
        private DataSourceRef selection = null;

        private void setSelection(DataSourceRef selection) {
            firePropertyChange(SELECTION_PROPERTY, this.selection, this.selection = selection);
        }

        private final SdmxWebManager manager;
        private final Languages languages;

        // ==================== State ====================
        private WebSource currentSource = null;
        private DatabaseRef currentDatabase = DatabaseRef.NO_DATABASE;
        private List<WebSource> allSources = Collections.emptyList();
        private List<Database> allDatabases = Collections.emptyList();
        private List<Flow> allFlows = Collections.emptyList();

        // Back-navigation stack (stores the card we came FROM)
        private final List<String> navHistory = new ArrayList<>();

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

        FlowBrowserPanel(@NonNull SdmxWebManager manager, @NonNull Languages languages) {
            this.manager = manager;
            this.languages = languages;
            initComponents();
        }

        private void initComponents() {
            // --- Header ---
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
            titleLabel.setForeground(Color.WHITE);

            styleNavButton(backButton, MaterialDesign.MDI_ARROW_LEFT);
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
                    (src, repaint) -> src.getWebsite() != null
                            ? SdmxAutoCompletion.FAVICONS.getOrDefault(
                                    FaviconRef.of(DomainName.of(src.getWebsite()), 32),
                                    repaint,
                                    SdmxAutoCompletion.getDefaultIcon(32))
                            : SdmxAutoCompletion.getDefaultIcon(32),
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
                    (flow, repaint) -> FlowIdenticonFactory.getIcon(flow.getRef().toString()),
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
            sourcesSearch.getDocument().addDocumentListener(debouncedListener(200, () -> {
                String text = sourcesSearch.getText().trim();
                updateModel(sourcesModel, text.isEmpty()
                        ? allSources
                        : Search.ofSources(allSources, languages).search(text, allSources.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
            }));
            dbSearch.getDocument().addDocumentListener(debouncedListener(200, () -> {
                String text = dbSearch.getText().trim();
                updateModel(dbModel, text.isEmpty()
                        ? allDatabases
                        : Search.ofDatabases(allDatabases).search(text, allDatabases.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
            }));
            flowsSearch.getDocument().addDocumentListener(debouncedListener(200, () -> {
                String text = flowsSearch.getText().trim();
                updateModel(flowsModel, text.isEmpty()
                        ? allFlows
                        : Search.ofFlows(allFlows).search(text, allFlows.size()).stream().map(Search.Result::getItem).collect(Collectors.toList()));
            }));

            setLayout(new BorderLayout());
            add(header, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);

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
            new SwingWorker<List<WebSource>, Void>() {
                @Override
                protected List<WebSource> doInBackground() {
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

            new SwingWorker<List<Database>, Void>() {
                @Override
                protected List<Database> doInBackground() throws IOException {
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

            new SwingWorker<List<Flow>, Void>() {
                @Override
                protected List<Flow> doInBackground() throws IOException {
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
            setSelection(DataSourceRef.builder()
                    .source(currentSource.getId())
                    .database(currentDatabase)
                    .flow(flow.getRef().toString())
                    .languages(languages)
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

        private static DocumentListener debouncedListener(int delayMs, Runnable action) {
            javax.swing.Timer[] slot = {null};
            return documentListenerOf(e -> {
                if (slot[0] != null) slot[0].stop();
                slot[0] = new javax.swing.Timer(delayMs, evt -> action.run());
                slot[0].setRepeats(false);
                slot[0].start();
            });
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
            if (name != null && !name.isEmpty() && !name.equals(db.getRef().getId())) {
                sb.append("<br>").append(escapeHtml(name));
            }
            sb.append("</body></html>");
            return sb.toString();
        }

        private static String buildFlowTooltip(Flow flow) {
            StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
            sb.append("<b>").append(escapeHtml(flow.getRef().toString())).append("</b>");
            String name = flow.getName();
            if (name != null && !name.isEmpty()) {
                sb.append("<br>").append(escapeHtml(name));
            }
            String description = flow.getDescription();
            if (description != null && !description.isEmpty()) {
                sb.append("<hr>").append(escapeHtml(description));
            }
            sb.append("</body></html>");
            return sb.toString();
        }

        private static String escapeHtml(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    // ==================== Demo main ====================

    public static void main(String[] args) {
        SdmxWebManager manager = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(source -> (marker, message) ->
                        System.out.printf(java.util.Locale.ROOT, "[%s] (%s) %s%n", source.getId(), marker, message))
                .onError(source -> (marker, message, error) ->
                        System.err.printf(java.util.Locale.ROOT, "[%s] (%s) %s: %s%n", source.getId(), marker, message, error.getMessage()))
                .build()
                .warmupAsync();

        Sdmxdl.INSTANCE.setSdmxManager(manager);

        SwingUtilities.invokeLater(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();

            FlowSelectorPanel selector = new FlowSelectorPanel();

            JLabel modelLabel = new JLabel("model: <none>");
            modelLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
            modelLabel.setFont(modelLabel.getFont().deriveFont(Font.PLAIN, 11f));
            selector.addPropertyChangeListener(MODEL_PROPERTY, evt -> {
                DataSourceRef ref = (DataSourceRef) evt.getNewValue();
                modelLabel.setText(ref == null
                        ? "model: <none>"
                        : "model: source=" + ref.getSource()
                                + "  database=" + ref.getDatabase()
                                + "  flow=" + ref.getFlow());
            });

            JPanel content = new JPanel(new BorderLayout(0, 4));
            content.setBorder(new EmptyBorder(12, 12, 12, 12));
            content.add(selector, BorderLayout.NORTH);
            content.add(modelLabel, BorderLayout.CENTER);

            JFrame frame = new JFrame("FlowSelectorPanel — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(content);
            frame.pack();
            frame.setMinimumSize(new java.awt.Dimension(450, frame.getHeight()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}


