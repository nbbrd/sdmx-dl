package sdmxdl.swing;

import com.formdev.flatlaf.FlatLightLaf;
import nbbrd.design.Demo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Interactive demo for {@link SdmxLogo} that lets you explore:
 * <ul>
 *   <li><b>Size</b> – a slider from 8 to 256 px, plus a live readout label.</li>
 *   <li><b>Rotation</b> – a slider from 0° to 360° applied via {@link AffineTransform}.</li>
 *   <li><b>Background</b> – toggle between white, light-grey, dark-grey, and black to
 *       check contrast and transparency.</li>
 *   <li><b>Preset gallery</b> – a row of fixed-size icons (8, 16, 24, 32, 48, 64, 128)
 *       for a quick visual check at typical UI sizes.</li>
 * </ul>
 *
 * <p>Run {@link #main(String[])} to launch a standalone {@link JFrame}.</p>
 */
final class SdmxLogoDemo {

    private static final int[] PRESET_SIZES = {8, 16, 24, 32, 48, 64, 128};

    private static final Color[] BACKGROUNDS = {
            Color.WHITE,
            new Color(0xF0, 0xF0, 0xF0),
            new Color(0x40, 0x40, 0x40),
            Color.BLACK
    };
    private static final String[] BACKGROUND_NAMES = {"White", "Light grey", "Dark grey", "Black"};

    private SdmxLogoDemo() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Canvas ====================

    /**
     * A component that renders one {@link SdmxLogo} at the centre, applying a
     * configurable rotation, on top of a configurable background colour.
     */
    private static final class IconCanvas extends JComponent {

        private int iconSize = 64;
        private double rotationDegrees = 0.0;
        private Color background = Color.WHITE;

        void setIconSize(int iconSize) {
            this.iconSize = iconSize;
            repaint();
        }

        void setRotationDegrees(double rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
            repaint();
        }

        @Override
        public void setBackground(Color background) {
            this.background = background;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                // Background
                g2.setColor(background);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Checkerboard pattern to visualise transparency
                int cs = 8;
                Color checker = mixColors(new Color(0xCC, 0xCC, 0xCC), background);
                for (int row = 0; row * cs < getHeight(); row++) {
                    for (int col = 0; col * cs < getWidth(); col++) {
                        if ((row + col) % 2 == 0) {
                            g2.setColor(checker);
                            g2.fillRect(col * cs, row * cs, cs, cs);
                        }
                    }
                }
                // Re-fill solid background on top so the checker is subtle
                g2.setColor(new Color(background.getRed(), background.getGreen(), background.getBlue(), 220));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Centre of the canvas
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Apply rotation around the canvas centre
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.rotate(Math.toRadians(rotationDegrees), cx, cy);

                // Paint icon centred
                SdmxLogo icon = new SdmxLogo(iconSize);
                int x = cx - iconSize / 2;
                int y = cy - iconSize / 2;
                icon.paintIcon(this, g2, x, y);

                // Crosshair at centre (helpful for checking rotation pivot)
                g2.setTransform(new AffineTransform()); // reset transform
                g2.setColor(new Color(255, 0, 0, 80));
                g2.drawLine(cx - 8, cy, cx + 8, cy);
                g2.drawLine(cx, cy - 8, cx, cy + 8);
            } finally {
                g2.dispose();
            }
        }

        /** Mix {@code color} with {@code base} at blend weight 0.35 (35% color, 65% base). */
        private static Color mixColors(Color color, Color base) {
            float w = 0.35f;
            int r = Math.round(base.getRed() * (1 - w) + color.getRed() * w);
            int g = Math.round(base.getGreen() * (1 - w) + color.getGreen() * w);
            int b = Math.round(base.getBlue() * (1 - w) + color.getBlue() * w);
            return new Color(r, g, b);
        }
    }

    // ==================== Preset gallery ====================

    /**
     * A horizontal strip that always shows the icon at each entry of {@link #PRESET_SIZES}.
     */
    private static final class PresetGallery extends JComponent {

        private Color background = Color.WHITE;

        @Override
        public void setBackground(Color background) {
            this.background = background;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setColor(background);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int margin = 12;
                int x = margin;
                int cy = getHeight() / 2;

                for (int size : PRESET_SIZES) {
                    SdmxLogo icon = new SdmxLogo(size);
                    int y = cy - size / 2;
                    icon.paintIcon(this, g2, x, y);

                    // Label below
                    g2.setColor(background.getRed() + background.getGreen() + background.getBlue() > 382
                            ? new Color(0x44, 0x44, 0x44)
                            : new Color(0xCC, 0xCC, 0xCC));
                    g2.setFont(g2.getFont().deriveFont(9f));
                    String label = size + "px";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(label, x + size / 2 - fm.stringWidth(label) / 2, cy + size / 2 + fm.getAscent() + 2);

                    x += size + margin;
                }
            } finally {
                g2.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int margin = 12;
            int totalWidth = margin;
            int maxSize = 0;
            for (int size : PRESET_SIZES) {
                totalWidth += size + margin;
                maxSize = Math.max(maxSize, size);
            }
            return new Dimension(totalWidth, maxSize + 30);
        }
    }

    // ==================== Demo panel ====================

    static JComponent createDemoPanel() {
        IconCanvas canvas = new IconCanvas();
        canvas.setPreferredSize(new Dimension(320, 320));
        canvas.setBorder(BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)));

        PresetGallery gallery = new PresetGallery();

        // ---- Size controls ----
        JSlider sizeSlider = new JSlider(8, 256, 64);
        sizeSlider.setMajorTickSpacing(64);
        sizeSlider.setMinorTickSpacing(8);
        sizeSlider.setPaintTicks(true);
        JLabel sizeLabel = new JLabel("64 px");
        sizeLabel.setPreferredSize(new Dimension(50, sizeLabel.getPreferredSize().height));

        sizeSlider.addChangeListener(e -> {
            int v = sizeSlider.getValue();
            sizeLabel.setText(v + " px");
            canvas.setIconSize(v);
        });

        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 512, 1));
        sizeSpinner.addChangeListener(e -> {
            int v = (Integer) sizeSpinner.getValue();
            if (v != sizeSlider.getValue()) {
                sizeSlider.setValue(Math.min(256, Math.max(8, v)));
            }
            canvas.setIconSize(v);
            sizeLabel.setText(v + " px");
        });
        sizeSlider.addChangeListener(e -> sizeSpinner.setValue(sizeSlider.getValue()));

        JPanel sizeRow = new JPanel(new BorderLayout(6, 0));
        sizeRow.add(new JLabel("Size:"), BorderLayout.WEST);
        sizeRow.add(sizeSlider, BorderLayout.CENTER);
        sizeRow.add(sizeSpinner, BorderLayout.EAST);

        // ---- Rotation controls ----
        JSlider rotSlider = new JSlider(0, 360, 0);
        rotSlider.setMajorTickSpacing(90);
        rotSlider.setMinorTickSpacing(15);
        rotSlider.setPaintTicks(true);
        JLabel rotLabel = new JLabel("0°");
        rotLabel.setPreferredSize(new Dimension(38, rotLabel.getPreferredSize().height));

        rotSlider.addChangeListener(e -> {
            int v = rotSlider.getValue();
            rotLabel.setText(v + "°");
            canvas.setRotationDegrees(v);
        });

        JButton resetRotBtn = new JButton("Reset");
        resetRotBtn.addActionListener(e -> rotSlider.setValue(0));

        JPanel rotRow = new JPanel(new BorderLayout(6, 0));
        rotRow.add(new JLabel("Rotation:"), BorderLayout.WEST);
        rotRow.add(rotSlider, BorderLayout.CENTER);
        JPanel rotRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rotRight.add(rotLabel);
        rotRight.add(resetRotBtn);
        rotRow.add(rotRight, BorderLayout.EAST);

        // ---- Background controls ----
        ButtonGroup bgGroup = new ButtonGroup();
        JPanel bgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bgRow.add(new JLabel("Background:"));
        for (int i = 0; i < BACKGROUNDS.length; i++) {
            final Color bg = BACKGROUNDS[i];
            JRadioButton rb = new JRadioButton(BACKGROUND_NAMES[i]);
            rb.setSelected(i == 0);
            rb.addActionListener(e -> {
                canvas.setBackground(bg);
                gallery.setBackground(bg);
            });
            bgGroup.add(rb);
            bgRow.add(rb);
        }

        // ---- Controls panel ----
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(new TitledBorder("Controls"));
        controls.add(sizeRow);
        controls.add(Box.createVerticalStrut(6));
        controls.add(rotRow);
        controls.add(Box.createVerticalStrut(6));
        controls.add(bgRow);

        // ---- Gallery panel ----
        JPanel galleryWrapper = new JPanel(new BorderLayout());
        galleryWrapper.setBorder(new TitledBorder("Preset sizes"));
        galleryWrapper.add(gallery, BorderLayout.CENTER);

        // ---- Canvas panel ----
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBorder(new TitledBorder("Preview"));
        canvasWrapper.add(canvas, BorderLayout.CENTER);

        // ---- Assemble ----
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(canvasWrapper, BorderLayout.CENTER);
        top.add(controls, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(top, BorderLayout.CENTER);
        root.add(galleryWrapper, BorderLayout.SOUTH);

        return root;
    }

    // ==================== Demo main ====================

    @Demo
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            JFrame frame = new JFrame("SdmxLogoIcon — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(createDemoPanel());
            frame.pack();
            frame.setMinimumSize(new Dimension(500, frame.getHeight()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}







