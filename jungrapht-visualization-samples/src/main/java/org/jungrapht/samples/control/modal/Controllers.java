package org.jungrapht.samples.control.modal;

import java.awt.*;
import javax.swing.*;
import org.jungrapht.visualization.control.modal.*;
import org.jungrapht.visualization.control.modal.Modal.Mode;

/**
 * Just a demo to show that all of the ModePanels show the same Mode when any of them are changed.
 */
public class Controllers {

  public static void main(String[] args) {

    JPanel panel = new JPanel(new GridLayout(2, 2));
    ModePanel panel1 =
        ModePanel.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .buttonSupplier(JRadioButton::new)
            .build();
    panel.add(panel1.buildUI());
    ModePanel panel2 =
        ModePanel.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .buttonSupplier(JToggleButton::new)
            .build();
    panel.add(panel2.buildUI());
    ModePanel panel3 =
        ModePanel.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .buttonSupplier(JCheckBox::new)
            .build();
    panel.add(panel3.buildUI());
    ModeComboBox panel4 = ModeComboBox.builder().modes(Mode.TRANSFORMING, Mode.PICKING).build();
    panel.add(panel4.buildUI());

    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();

    // make everyone see everyone
    panel1.addModals(panel2, panel3, panel4, modeMenu);
    panel2.addModals(panel1, panel3, panel4, modeMenu);
    panel3.addModals(panel1, panel2, panel4, modeMenu);
    panel4.addModals(panel1, panel2, panel3, modeMenu);
    modeMenu.addModals(panel1, panel2, panel3, panel4);
    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.setJMenuBar(new JMenuBar());
    frame.getJMenuBar().add(modeMenu.buildUI());
    frame.getContentPane().add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
