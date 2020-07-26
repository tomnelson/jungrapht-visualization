package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Map;

public interface Attributed<T> extends Map<String, String> {

  T getValue();

  Map<String, String> getAttributeMap();

  void set(String key, String value);
}
