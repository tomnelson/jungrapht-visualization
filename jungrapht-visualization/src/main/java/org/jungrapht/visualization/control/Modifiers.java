package org.jungrapht.visualization.control;

import static java.awt.event.MouseEvent.*;
import static java.util.Map.entry;

import java.awt.Toolkit;
import java.util.Map;
import java.util.stream.Collectors;

public class Modifiers {

  private static int MENU_SHORTCUT = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

  public static final String MB1 = "MB1";
  public static final String MB2 = "MB2";
  public static final String MB3 = "MB3";

  //  public static final String CTRL = "CTRL";
  public static final String ALT = "ALT";
  public static final String SHIFT = "SHIFT";
  public static final String MENU = "MENU";
  public static final String SHIFT_MENU = "SHIFT_MENU";
  public static final String MB1_SHIFT = "MB1_SHIFT";
  public static final String MB1_CTRL = "MB1_CTRL";
  public static final String MB1_ALT = "MB1_ALT";
  public static final String MB1_MENU = "MB1_MENU";
  public static final String MB1_SHIFT_MENU = "MB1_SHIFT_MENU";

  public static final String NONE = "NONE";

  public static Map<String, Integer> masks =
      Map.ofEntries(
          entry(MB1, BUTTON1_DOWN_MASK),
          entry(MB2, BUTTON2_DOWN_MASK),
          entry(MB3, BUTTON3_DOWN_MASK),
          //          entry(CTRL, CTRL_DOWN_MASK),
          entry(ALT, ALT_DOWN_MASK),
          entry(SHIFT, SHIFT_DOWN_MASK),
          entry(MENU, MENU_SHORTCUT),
          entry(SHIFT_MENU, SHIFT_DOWN_MASK | MENU_SHORTCUT),
          entry(MB1_SHIFT, BUTTON1_DOWN_MASK | SHIFT_DOWN_MASK),
          //          entry(MB1_CTRL, BUTTON1_DOWN_MASK | CTRL_DOWN_MASK),
          entry(MB1_ALT, BUTTON1_DOWN_MASK | ALT_DOWN_MASK),
          entry(MB1_MENU, BUTTON1_DOWN_MASK | MENU_SHORTCUT),
          entry(MB1_SHIFT_MENU, BUTTON1_DOWN_MASK | SHIFT_DOWN_MASK | MENU_SHORTCUT),
          entry(NONE, 0));

  public static Map<Integer, String> maskStrings =
      masks.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
}
