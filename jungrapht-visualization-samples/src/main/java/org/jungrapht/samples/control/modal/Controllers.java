package org.jungrapht.samples.control.modal;

import java.awt.*;
import javax.swing.*;
import org.jungrapht.visualization.control.ModeControls;
import org.jungrapht.visualization.control.modal.*;

/**
 * Just a demo to show that all of the ModePanels show the same Mode when any of them are changed.
 */
public class Controllers {

  public static void main(String[] args) {

    JPanel panel = new JPanel(new GridLayout(2, 2));
    //    JLabel label = new JLabel("Type 'p' or 't' here:");
    //    panel.add(label);
    //    panel.add(new JPanel());
    ModePanel panel1 =
        ModePanel.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .buttonSupplier(JRadioButton::new)
            .build();
    panel.add(panel1.buildUI());
    ModePanel panel2 =
        ModePanel.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .buttonSupplier(JToggleButton::new)
            .build();
    panel.add(panel2.buildUI());
    ModePanel panel3 =
        ModePanel.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .buttonSupplier(JCheckBox::new)
            .build();
    panel.add(panel3.buildUI());
    ModeComboBox panel4 =
        ModeComboBox.builder().modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING).build();
    panel.add(panel4.buildUI());

    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();

    ModeControl modeControl =
        ModeControl.builder().modals(panel1, panel2, panel3, panel4, modeMenu).build();

    panel1.addModal(modeControl);
    panel2.addModal(modeControl);
    panel3.addModal(modeControl);
    panel4.addModal(modeControl);
    modeMenu.addModal(modeControl);

    // make everyone see everyone
    //    panel1.addModals(panel2, panel3, panel4, modeMenu);
    //    panel2.addModals(panel1, panel3, panel4, modeMenu);
    //    panel3.addModals(panel1, panel2, panel4, modeMenu);
    //    panel4.addModals(panel1, panel2, panel3, modeMenu);
    //    modeMenu.addModals(panel1, panel2, panel3, panel4);
    // create a frame to hold the graph visualization
    final JFrame frame = new JFrame();
    frame.addKeyListener(new ModeControls.ModeKeyAdapter(modeControl));
    frame.setJMenuBar(new JMenuBar());
    frame.getJMenuBar().add(modeMenu.buildUI());
    frame.getContentPane().add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
