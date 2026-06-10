package internal.sdmxdl.desktop.experiments;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLightLaf;
import internal.sdmxdl.desktop.SdmxAutoCompletion;
import internal.sdmxdl.desktop.util.Ikons;
import lombok.NonNull;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import sdmxdl.*;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.format.FlowEntry;
import sdmxdl.format.Search;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static internal.sdmxdl.desktop.util.Documents.documentListenerOf;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_ALERT;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_CLOSE;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_LOCK;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_REFRESH;

/**
 * A single-panel flow browser that aggregates flows from <em>all</em> registered
 * SDMX sources into one searchable list.
 *
 * <p>Loading is triggered explicitly by the user (refresh button) because fetching
 * flows from every source is potentially a long network operation.
 * Results are appended <em>incrementally</em> as each source finishes, so search
 * can start before all sources are done.</p>
 *
 * <p>The confirmed selection is exposed through the bound {@link #SELECTION_PROPERTY}
 * as a {@link DataSourceRef}.</p>
 */
public final class MegaFlowSearchPanel extends JComponent {

    public static final String SELECTION_PROPERTY = "selection";

    // ==================== Colors ====================
    private static final Color CHIP_BORDER = new Color(0xE0, 0xE0, 0xE0);

    // ==================== Selection ====================
    @lombok.Getter
    private DataSourceRef selection = null;

    private void setSelection(DataSourceRef selection) {
        firePropertyChange(SELECTION_PROPERTY, this.selection, this.selection = selection);
    }

    // ==================== State (EDT-only) ====================
    private final List<FlowEntry> allEntries = new ArrayList<>();
    private int totalSources = 0;
    private int loadedSources = 0;
    private boolean loading = false;

    /** Cached search index — rebuilt off-EDT whenever {@link #allEntries} grows. */
    private Search<FlowEntry> entrySearch = null;

    /**
     * Generation counter for index rebuilds. Incremented on every rebuild request;
     * a worker that finds its generation stale simply discards its result without
     * touching the EDT — no interrupt needed.
     */
    private final AtomicInteger indexGeneration = new AtomicInteger(0);

    /**
     * Generation counter for search queries. Incremented on every {@link #applyFilter()}
     * call; the worker checks before posting results to the EDT.
     */
    private final AtomicInteger searchGeneration = new AtomicInteger(0);

    /**
     * Single-thread executor for index rebuilds.  Serial execution ensures each
     * rebuild sees the latest snapshot; the generation counter discards superseded results.
     */
    private final ExecutorService indexExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MegaFlowSearch-index");
        t.setDaemon(true);
        return t;
    });

    /**
     * Single-thread executor for search queries.  Serial execution prevents
     * multiple concurrent BM25/trigram scans from saturating the CPU.
     */
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MegaFlowSearch-query");
        t.setDaemon(true);
        return t;
    });

    // ==================== UI ====================
    private final JTextField searchField = new JTextField();
    /** ✕ button embedded in the search field trailing area (#5). */
    private final JButton clearButton = new JButton();
    /** Flow-count badge embedded in the search field trailing area (#8). */
    private final JLabel countLabel = new JLabel();
    /** Toggle: when selected, list is filtered to non-public sources only (#6). */
    private final JToggleButton confidentialityToggle = new JToggleButton();
    private final JButton loadButton = new JButton();
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel statusLabel = new JLabel(" ");
    /** Amber warning strip shown while loading is still in progress and a query is active. */
    private final JLabel partialBanner = new JLabel();
    /** Centered message shown in place of the list when the model is empty. */
    private final JLabel emptyLabel = new JLabel();
    private final CardLayout listCards = new CardLayout();
    private final JPanel listCardPanel = new JPanel(listCards);
    private final BulkListModel<FlowEntry> listModel = new BulkListModel<>();
    private final JList<FlowEntry> flowList = new JList<>(listModel);

    public MegaFlowSearchPanel() {
        initComponents();
    }

    private void initComponents() {
        // --- Auto-start: trigger loading the first time the component becomes visible ---
        addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                        && isShowing()) {
                    removeHierarchyListener(this);
                    startLoading();
                }
            }
        });

        // --- Search field with embedded clear button (#5) and count badge (#8) ---
        searchField.putClientProperty("JTextField.placeholderText", "Search flows…");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CHIP_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        searchField.getDocument().addDocumentListener(debouncedListener(200, this::applyFilter));
        // Update clear-button visibility on every keystroke (no debounce needed)
        searchField.getDocument().addDocumentListener(documentListenerOf(
                e -> clearButton.setVisible(!searchField.getText().isEmpty())));

        clearButton.setIcon(Ikons.of(MDI_CLOSE, 12, FlatIconColors.ACTIONS_GREYINLINE.key));
        clearButton.setToolTipText("Clear search");
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setVisible(false);
        clearButton.addActionListener(e -> searchField.setText(""));

        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN, 10f));
        countLabel.setForeground(new Color(0x99, 0x99, 0x99));
        countLabel.setVisible(false);

        JPanel trailingPanel = new JPanel();
        trailingPanel.setOpaque(false);
        trailingPanel.setLayout(new BoxLayout(trailingPanel, BoxLayout.X_AXIS));
        trailingPanel.add(countLabel);
        trailingPanel.add(Box.createHorizontalStrut(4));
        trailingPanel.add(clearButton);
        searchField.putClientProperty("JTextField.trailingComponent", trailingPanel);

        // --- Confidentiality toggle: filter to non-public sources only (#6) ---
        confidentialityToggle.setIcon(Ikons.of(MDI_LOCK, 14, FlatIconColors.ACTIONS_GREYINLINE.key));
        confidentialityToggle.setSelectedIcon(Ikons.of(MDI_LOCK, 14, new Color(0xFF, 0x8C, 0x00)));
        confidentialityToggle.setToolTipText("Show non-public sources only");
        confidentialityToggle.setFocusPainted(false);
        confidentialityToggle.addActionListener(e -> applyFilter());

        // --- Load button: asks for confirmation when data is already loaded (#7) ---
        loadButton.setIcon(Ikons.of(MDI_REFRESH, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        loadButton.setToolTipText("Reload flows from all sources");
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(e -> {
            if (!allEntries.isEmpty()) {
                int answer = JOptionPane.showConfirmDialog(
                        MegaFlowSearchPanel.this,
                        "Reload will clear the current " + allEntries.size()
                                + " flows and restart loading from scratch.\nContinue?",
                        "Reload",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) return;
            }
            startLoading();
        });

        // --- Progress bar (hidden until first load) ---
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new java.awt.Dimension(0, 4));
        progressBar.setVisible(false);

        // --- Status label ---
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
        updateStatus();

        // --- Partial results banner: visible when loading + active search query ---
        partialBanner.setIcon(Ikons.of(MDI_ALERT, 13, new Color(0x85, 0x64, 0x00)));
        partialBanner.setText("Results may be partial — sources still loading");
        partialBanner.setForeground(new Color(0x85, 0x64, 0x00));
        partialBanner.setBackground(new Color(0xFF, 0xF3, 0xCD));
        partialBanner.setOpaque(true);
        partialBanner.setFont(partialBanner.getFont().deriveFont(Font.PLAIN, 11f));
        partialBanner.setBorder(new EmptyBorder(4, 8, 4, 8));
        partialBanner.setVisible(false);

        // --- Empty state label (shown instead of list when model is empty) ---
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setVerticalAlignment(SwingConstants.CENTER);
        emptyLabel.setForeground(new Color(0xAA, 0xAA, 0xAA));
        emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC, 13f));

        // --- Flow list ---
        flowList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flowList.setFixedCellHeight(54);
        flowList.setCellRenderer(new ListItemRenderer<>(
                (entry, repaint) -> {
                    Icon base = entry.getSource().getWebsite() != null
                            ? SdmxAutoCompletion.FAVICONS.getOrDefault(
                                    FaviconRef.of(DomainName.of(entry.getSource().getWebsite()), 32),
                                    repaint,
                                    SdmxAutoCompletion.getDefaultIcon(32))
                            : SdmxAutoCompletion.getDefaultIcon(32);
                    return withConfidentialityBadge(base, entry.getSource().getConfidentiality());
                },
                entry -> entry.getSource().getId() + "  ›  " + entry.getFlow().getRef().toShortString(),
                entry -> entry.getFlow().getName(),
                MegaFlowSearchPanel::buildEntryTooltip));

        // Single click = highlight only; double-click or Enter = confirm (#4)
        flowList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && flowList.getSelectedIndex() >= 0) {
                    confirmSelection();
                }
            }
        });
        flowList.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_ACTION");
        flowList.getActionMap().put("ENTER_ACTION", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmSelection();
            }
        });
        // Escape on the list → jump back to the search field (#9)
        flowList.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_TO_SEARCH");
        flowList.getActionMap().put("ESCAPE_TO_SEARCH", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
            }
        });
        flowList.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (flowList.getSelectedIndex() < 0 && flowList.getModel().getSize() > 0) {
                    flowList.setSelectedIndex(0);
                }
            }
        });

        // Ctrl+F from anywhere in the panel → focus + select-all in search field (#9)
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                        java.awt.event.InputEvent.CTRL_DOWN_MASK), "FOCUS_SEARCH");
        getActionMap().put("FOCUS_SEARCH", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });

        // --- List card panel: switches between real list and empty-state message (#2) ---
        JPanel emptyCard = new JPanel(new BorderLayout());
        emptyCard.add(emptyLabel, BorderLayout.CENTER);
        listCardPanel.add(emptyCard, "empty");
        listCardPanel.add(new JScrollPane(flowList), "list");

        // --- Layout ---
        JPanel buttonGroup = new JPanel(new GridLayout(1, 0, 2, 0));
        buttonGroup.setOpaque(false);
        buttonGroup.add(confidentialityToggle);
        buttonGroup.add(loadButton);

        JPanel toolbar = new JPanel(new BorderLayout(4, 0));
        toolbar.setBorder(new EmptyBorder(8, 8, 4, 8));
        toolbar.add(searchField, BorderLayout.CENTER);
        toolbar.add(buttonGroup, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbar, BorderLayout.NORTH);
        topPanel.add(progressBar, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(partialBanner, BorderLayout.NORTH);  // #3
        centerPanel.add(listCardPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(searchField::requestFocusInWindow);
    }

    // ==================== Loading ====================

    private void startLoading() {
        if (loading) return;
        loading = true;
        allEntries.clear();
        listModel.setData(Collections.emptyList());
        loadedSources = 0;
        setSelection(null);
        loadButton.setEnabled(false);

        SdmxWebManager manager = Sdmxdl.INSTANCE.getSdmxManager();
        Languages languages = Sdmxdl.INSTANCE.getLanguages();

        List<WebSource> sources = manager.getSources().values().stream()
                .filter(s -> !s.isAlias())
                .sorted(Comparator.comparing(WebSource::getId))
                .collect(Collectors.toList());

        totalSources = sources.size();
        if (totalSources == 0) {
            loading = false;
            loadButton.setEnabled(true);
            updateStatus();
            return;
        }

        progressBar.setMaximum(totalSources);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        updateStatus();

        for (WebSource source : sources) {
            new SwingWorker<List<FlowEntry>, Void>() {
                @Override
                protected List<FlowEntry> doInBackground() {
                    return loadEntriesFor(source, manager, languages);
                }

                @Override
                protected void done() {
                    loadedSources++;
                    try {
                        allEntries.addAll(get());
                    } catch (InterruptedException | ExecutionException ignored) {
                        // source failed — silently skip it
                    }
                    progressBar.setValue(loadedSources);
                    if (loadedSources >= totalSources) {
                        loading = false;
                        loadButton.setEnabled(true);
                        // Completion flash: green fill for 700 ms, then hide (#10)
                        progressBar.setForeground(new Color(0x14, 0x85, 0x29));
                        javax.swing.Timer hideTimer = new javax.swing.Timer(700, evt -> {
                            progressBar.setForeground(null); // restore default color
                            progressBar.setVisible(false);
                        });
                        hideTimer.setRepeats(false);
                        hideTimer.start();
                    }
                    updateStatus();
                    rebuildIndexAsync();
                }
            }.execute();
        }
    }

    private static List<FlowEntry> loadEntriesFor(
            @NonNull WebSource source,
            @NonNull SdmxWebManager manager,
            @NonNull Languages languages) {
        List<FlowEntry> entries = new ArrayList<>();
        try {
            Collection<Database> databases = manager.using(source)
                    .getDatabases(SourceRequest.builder().languages(languages).build());

            Set<DatabaseRef> dbRefs = new LinkedHashSet<>();
            for (Database db : databases) {
                dbRefs.add(db.getRef());
            }
            if (dbRefs.isEmpty()) {
                dbRefs.add(DatabaseRef.NO_DATABASE);
            }

            for (DatabaseRef db : dbRefs) {
                try {
                    Collection<Flow> flows = manager.using(source)
                            .getFlows(DatabaseRequest.builder()
                                    .database(db)
                                    .languages(languages)
                                    .build());
                    for (Flow flow : flows) {
                        entries.add(new FlowEntry(source, db, flow));
                    }
                } catch (IOException ignored) {
                    // one database failed — skip it, continue with others
                }
            }
        } catch (IOException ignored) {
            // entire source failed — skip it
        }
        return entries;
    }

    // ==================== Selection ====================

    private void confirmSelection() {
        FlowEntry entry = flowList.getSelectedValue();
        if (entry != null) {
            setSelection(DataSourceRef.builder()
                    .source(entry.getSource().getId())
                    .database(entry.getDatabase())
                    .flow(entry.getFlow().getRef().toString())
                    .languages(Sdmxdl.INSTANCE.getLanguages())
                    .build());
        }
    }

    // ==================== Search ====================

    /** Rebuilds the search index off-EDT, then applies the current filter. */
    private void rebuildIndexAsync() {
        // Bump generation — any already-queued or running rebuild will see a stale generation
        // and discard its result without touching EDT state.
        final int myGeneration = indexGeneration.incrementAndGet();

        // Snapshot current data on the EDT so the worker is self-contained
        List<FlowEntry> snapshot = new ArrayList<>(allEntries);
        Languages languages = Sdmxdl.INSTANCE.getLanguages();

        indexExecutor.submit(() -> {
            Search<FlowEntry> newSearch = Search.ofFlowEntries(snapshot, languages);
            SwingUtilities.invokeLater(() -> {
                if (indexGeneration.get() == myGeneration) {
                    entrySearch = newSearch;
                    applyFilter();
                }
            });
        });
    }

    private void applyFilter() {
        String text = searchField.getText().trim();

        if (text.isEmpty() || entrySearch == null) {
            // Fast path: no scoring needed — update the model synchronously on the EDT
            listModel.setData(applyConfidentialityFilter(new ArrayList<>(allEntries)));
            updateStatus();
            return;
        }

        // Bump generation — any already-queued search will bail out early
        final int myGeneration = searchGeneration.incrementAndGet();
        Search<FlowEntry> currentSearch = entrySearch;
        int maxResults = allEntries.size();

        searchExecutor.submit(() -> {
            // Early-out: a newer request has already been queued
            if (searchGeneration.get() != myGeneration) return;

            List<FlowEntry> results = currentSearch.search(text, maxResults)
                    .stream()
                    .map(Search.Result::getItem)
                    .collect(Collectors.toList());

            SwingUtilities.invokeLater(() -> {
                // Final stale-result guard before touching the model
                if (searchGeneration.get() == myGeneration) {
                    listModel.setData(applyConfidentialityFilter(results));
                    updateStatus();
                }
            });
        });
    }

    /** Post-filters entries to non-public sources when the confidentiality toggle is active (#6). */
    private List<FlowEntry> applyConfidentialityFilter(@NonNull List<FlowEntry> entries) {
        if (!confidentialityToggle.isSelected()) return entries;
        return entries.stream()
                .filter(e -> e.getSource().getConfidentiality() != Confidentiality.PUBLIC)
                .collect(Collectors.toList());
    }

    // ==================== UI helpers ====================

    private void updateStatus() {
        // Status text
        if (loading) {
            statusLabel.setText("Loading…  " + loadedSources + " / " + totalSources
                    + " sources  ·  " + allEntries.size() + " flows found");
        } else if (totalSources == 0) {
            statusLabel.setText("Loading flows from all sources…");
        } else {
            int showing = listModel.getSize();
            int total = allEntries.size();
            if (showing < total) {
                statusLabel.setText("Showing " + showing + " of " + total
                        + " flows  ·  " + totalSources + " sources");
            } else {
                statusLabel.setText(total + " flows  ·  " + totalSources + " sources");
            }
        }

        // Count badge inside search field (#8)
        int count = listModel.getSize();
        countLabel.setText(count > 0 ? String.valueOf(count) : "");
        countLabel.setVisible(count > 0);

        // Partial results banner (#3): warn when a query is active but loading is still in progress
        String query = searchField.getText().trim();
        partialBanner.setVisible(loading && !query.isEmpty());

        // Empty state card (#2)
        if (listModel.getSize() == 0) {
            if (loading) {
                emptyLabel.setText("Loading flows…");
            } else if (!query.isEmpty()) {
                emptyLabel.setText("<html><center>No flows match<br><b>"
                        + escapeHtml(query) + "</b></center></html>");
            } else if (totalSources > 0) {
                emptyLabel.setText("No flows found");
            } else {
                emptyLabel.setText("<html><center>Loading flows from all sources…</center></html>");
            }
            listCards.show(listCardPanel, "empty");
        } else {
            listCards.show(listCardPanel, "list");
        }
    }

    // ==================== Tooltip ====================

    private static String buildEntryTooltip(FlowEntry entry) {
        StringBuilder sb = new StringBuilder("<html><body style='width:300px'>");
        sb.append("<b>").append(escapeHtml(entry.getFlow().getRef().toString())).append("</b>");
        String name = entry.getFlow().getName();
        if (name != null && !name.isEmpty()) {
            sb.append("<br>").append(escapeHtml(name));
        }
        String description = entry.getFlow().getDescription();
        if (description != null && !description.isEmpty()) {
            sb.append("<hr>").append(escapeHtml(description));
        }
        sb.append("<hr><small>Source: ").append(escapeHtml(entry.getSource().getId()));
        if (!DatabaseRef.NO_DATABASE.equals(entry.getDatabase())) {
            sb.append("  ·  ").append(escapeHtml(entry.getDatabase().toString()));
        }
        Confidentiality confidentiality = entry.getSource().getConfidentiality();
        if (confidentiality != Confidentiality.PUBLIC) {
            sb.append("  ·  ").append(escapeHtml(confidentiality.name()));
        }
        sb.append("</small>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Wraps {@code base} with a small colored dot in the bottom-right corner
     * when the source is not fully public.  The dot color matches the ECB
     * confidentiality regime: blue → unrestricted, amber → restricted,
     * orange → confidential, red → secret.
     */
    private static Icon withConfidentialityBadge(@NonNull Icon base, @NonNull Confidentiality confidentiality) {
        if (confidentiality == Confidentiality.PUBLIC) return base;
        Color badgeColor = confidentialityBadgeColor(confidentiality);
        return new Icon() {
            @Override public int getIconWidth()  { return base.getIconWidth(); }
            @Override public int getIconHeight() { return base.getIconHeight(); }
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                base.paintIcon(c, g, x, y);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int d = 10;
                    int bx = x + base.getIconWidth()  - d;
                    int by = y + base.getIconHeight() - d;
                    // White border so the dot stands out on any background
                    g2.setColor(Color.WHITE);
                    g2.fillOval(bx - 1, by - 1, d + 2, d + 2);
                    g2.setColor(badgeColor);
                    g2.fillOval(bx, by, d, d);
                } finally {
                    g2.dispose();
                }
            }
        };
    }

    private static Color confidentialityBadgeColor(@NonNull Confidentiality confidentiality) {
        switch (confidentiality) {
            case UNRESTRICTED: return new Color(0x00, 0x78, 0xD4); // blue
            case RESTRICTED:   return new Color(0xFF, 0xB9, 0x00); // amber
            case CONFIDENTIAL: return new Color(0xFF, 0x8C, 0x00); // orange
            case SECRET:       return new Color(0xD1, 0x0F, 0x0F); // red
            default:           return Color.GRAY;
        }
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

        Sdmxdl.INSTANCE.setSdmxManager(manager);

        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            MegaFlowSearchPanel panel = new MegaFlowSearchPanel();

            JLabel selectionLabel = new JLabel("selection: <none>");
            selectionLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
            selectionLabel.setFont(selectionLabel.getFont().deriveFont(Font.PLAIN, 11f));
            panel.addPropertyChangeListener(SELECTION_PROPERTY, evt -> {
                DataSourceRef ref = (DataSourceRef) evt.getNewValue();
                selectionLabel.setText(ref == null
                        ? "selection: <none>"
                        : "selection: source=" + ref.getSource()
                                + "  database=" + ref.getDatabase()
                                + "  flow=" + ref.getFlow());
            });

            JPanel content = new JPanel(new BorderLayout(0, 4));
            content.setBorder(new EmptyBorder(8, 8, 8, 8));
            content.add(panel, BorderLayout.CENTER);
            content.add(selectionLabel, BorderLayout.SOUTH);

            JFrame frame = new JFrame("MegaFlowSearchPanel — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(content);
            frame.setSize(700, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // ==================== BulkListModel ====================

    /**
     * A list model that replaces its entire contents in two events
     * (one removal + one addition), regardless of how many items changed.
     * <p>
     * Using {@link DefaultListModel} fires one event per {@code addElement()} call,
     * which causes thousands of repaint/revalidate cycles on the EDT when the corpus
     * is large.
     * </p>
     */
    private static final class BulkListModel<T> extends AbstractListModel<T> {

        private List<T> data = Collections.emptyList();

        void setData(@NonNull List<T> newData) {
            int oldSize = data.size();
            data = new ArrayList<>(newData);
            if (oldSize > 0) {
                fireIntervalRemoved(this, 0, oldSize - 1);
            }
            if (!data.isEmpty()) {
                fireIntervalAdded(this, 0, data.size() - 1);
            }
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public T getElementAt(int index) {
            return data.get(index);
        }
    }
}
