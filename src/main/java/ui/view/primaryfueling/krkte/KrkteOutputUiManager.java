package ui.view.primaryfueling.krkte;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

public class KrkteOutputUiManager {

    private JFormattedTextField textField;

    public void setKrkte(double krkte) {
        textField.setValue(krkte);
    }

    public JPanel getKrktePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(-32,0,0,0);

        JLabel krkteTitle = new JLabel("Corrected KRKTE");
        panel.add(krkteTitle, c);

        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,0,0);

        panel.add(getKrkteTextPanel(), c);

        return panel;
    }

    private JPanel getKrkteTextPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;

        JFormattedTextField textField = getKrkteTextField();
        panel.add(textField, c);

        c.gridx = 1;
        c.gridy = 0;

        JLabel krkteLabel = new JLabel("ms/%");
        krkteLabel.setFont(krkteLabel.getFont().deriveFont(krkteLabel.getFont().getStyle() & ~Font.BOLD));
        panel.add(krkteLabel, c);

        return panel;
    }

    private JFormattedTextField getKrkteTextField() {
        DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        NumberFormatter decimalFormatter = new NumberFormatter(decimalFormat);
        decimalFormatter.setOverwriteMode(true);
        decimalFormatter.setAllowsInvalid(true);
        decimalFormatter.setMinimum(0d);

        textField = new JFormattedTextField(decimalFormat);
        textField.setEditable(false);
        textField.setColumns(6);
        textField.setValue(0);

        return textField;
    }
}