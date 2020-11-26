package org.jungrapht.visualization.layout.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

  public static final String PREFIX = "jungrapht.";

  private static boolean loaded;

  public static void load() {
    if (!loaded) {
      loadFromDefault();
      loadFromAppName();
      loaded = true;
    }
  }

  private PropertyLoader() {}

  private static final String PROPERTIES_FILE_NAME =
      System.getProperty("jungrapht.properties.file.name", PREFIX + "properties");

  private static boolean loadFromAppName() {
    try {
      String launchProgram = System.getProperty("sun.java.command");
      if (launchProgram != null && !launchProgram.isEmpty()) {
        launchProgram = launchProgram.substring(launchProgram.lastIndexOf('.') + 1) + ".properties";
        InputStream stream = PropertyLoader.class.getResourceAsStream("/" + launchProgram);
        System.getProperties().load(stream);

        Properties props = System.getProperties();
        return true;
      }
    } catch (Exception ex) {
    }
    return false;
  }

  private static boolean loadFromDefault() {
    try {
      InputStream stream = PropertyLoader.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
      System.getProperties().load(stream);
      return true;
    } catch (Exception ex) {
    }
    return false;
  }
}
