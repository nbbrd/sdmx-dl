package sdmxdl.desktop;

import lombok.Getter;
import sdmxdl.HasPersistence;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public final class JValueAsText<T extends HasPersistence> extends JComponent implements HasModel<T> {

    @Getter
    private T model = null;

    public void setModel(T model) {
        firePropertyChange(MODEL_PROPERTY, this.model, this.model = model);
    }

    private final JTextArea textArea = new JTextArea();

    public JValueAsText() {
        initComponents();
    }

    private void initComponents() {
        textArea.setEditable(false);

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        addPropertyChangeListener(MODEL_PROPERTY, this::onModelChange);
    }

    private void onModelChange(PropertyChangeEvent evt) {
        updateText();
    }

    private void updateText() {
        textArea.setText(getValueAsText());
        textArea.setCaretPosition(0);
    }

    private String getValueAsText() {
        return model != null ? Sdmxdl.INSTANCE.formatAsJson((Class<T>) model.getClass(), model) : "No structure";
    }
}
