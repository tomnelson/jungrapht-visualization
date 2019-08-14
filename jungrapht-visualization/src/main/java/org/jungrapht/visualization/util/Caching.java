/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 */
package org.jungrapht.visualization.util;

/**
 * Interface to provide external controls to an implementing class that manages a cache.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
public interface Caching {
  /** clear cache */
  void clear();
}