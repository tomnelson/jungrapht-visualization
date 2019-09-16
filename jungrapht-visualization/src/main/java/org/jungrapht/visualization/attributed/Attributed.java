package org.jungrapht.visualization.attributed;

import java.util.Map;

public interface Attributed<K, V> extends Map<K, V> {

  Map<K, V> getAttributeMap();
}
