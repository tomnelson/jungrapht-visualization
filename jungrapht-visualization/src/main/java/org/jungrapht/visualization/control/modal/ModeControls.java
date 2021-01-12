package org.jungrapht.visualization.control.modal;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.modal.Modal.Mode;

public class ModeControls {

  public static class ModeKeyAdapter extends KeyAdapter {
    private char t = 't';
    private char p = 'p';
    protected ModeControl modeControl;

    public ModeKeyAdapter(ModeControl modeControl) {
      this.modeControl = modeControl;
    }

    public ModeKeyAdapter(char t, char p, ModeControl modeControl) {
      this.t = t;
      this.p = p;
      this.modeControl = modeControl;
    }

    public void keyTyped(KeyEvent event) {
      char keyChar = event.getKeyChar();
      if (keyChar == t) {
        ((Component) event.getSource())
            .setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        modeControl.setMode(Mode.TRANSFORMING);
      } else if (keyChar == p) {
        ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        modeControl.setMode(Mode.PICKING);
      }
    }
  }

  public static JComboBox getStandardModeComboBox() {
    return ModeComboBox.builder().modes(Mode.TRANSFORMING, Mode.PICKING).build().buildUI();
  }

  public static JComboBox getStandardModeComboBox(Mode mode, ModalGraphMouse... graphMice) {
    ModeComboBox comboBox =
        ModeComboBox.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .modals(graphMice)
            .mode(mode)
            .build()
            .buildUI();
    return comboBox;
  }

  public static JComboBox getStandardModeComboBox(ModalGraphMouse... graphMice) {
    return getStandardModeComboBox(Mode.TRANSFORMING, graphMice);
  }

  public static JComboBox getEditingModeComboBox(ModalGraphMouse... graphMice) {
    ModeComboBox comboBox =
        ModeComboBox.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING, Mode.ANNOTATING)
            .mode(Mode.EDITING)
            .modals(graphMice)
            .build()
            .buildUI();
    return comboBox;
  }

  public static JComboBox getAnnotationModeComboBox(ModalGraphMouse... graphMice) {
    ModeComboBox comboBox =
        ModeComboBox.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING, Mode.ANNOTATING)
            .mode(Mode.EDITING)
            .modals(graphMice)
            .build()
            .buildUI();
    return comboBox;
  }

  public static JMenu getStandardModeMenu() {
    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();
    return modeMenu.buildUI();
  }

  public static JMenu getStandardModeMenu(Mode mode, ModalGraphMouse... graphMice) {
    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING)
            .modals(graphMice)
            .mode(mode)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();
    return modeMenu.buildUI();
  }

  public static JMenu getStandardModeMenu(ModalGraphMouse... graphMice) {
    return getStandardModeMenu(Mode.TRANSFORMING, graphMice);
  }

  public static JMenu getEditingModeMenu(ModalGraphMouse... graphMice) {
    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING, Mode.EDITING)
            .mode(Mode.EDITING)
            .modals(graphMice)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();
    return modeMenu.buildUI();
  }

  public static JMenu getAnnotatingModeMenu(ModalGraphMouse... graphMice) {
    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Mode.TRANSFORMING, Mode.PICKING, Mode.ANNOTATING, Mode.EDITING)
            .mode(Mode.ANNOTATING)
            .modals(graphMice)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();
    return modeMenu.buildUI();
  }
}
