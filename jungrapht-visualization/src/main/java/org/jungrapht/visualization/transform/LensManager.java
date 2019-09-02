package org.jungrapht.visualization.transform;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LensManager<T extends LensSupport> {

  private static final Logger log = LoggerFactory.getLogger(LensManager.class);

  Set<T> lensSet = new HashSet<>();

  public LensManager(T... abstractLensSupport) {
    Collections.addAll(lensSet, abstractLensSupport);
    Arrays.stream(abstractLensSupport).forEach(a -> a.setManager(this::isolate));
  }

  public void add(T lensSupport) {
    lensSet.add(lensSupport);
  }

  public void isolate() {
    T activeOne = lensSet.stream().filter(LensSupport::isActive).findFirst().get();
    if (activeOne != null) {
      lensSet.stream().filter(l -> !activeOne.equals(l)).forEach(LensSupport::deactivate);
    }
    log.info(
        "active Lens: {}",
        lensSet.stream().filter(LensSupport::isActive).collect(Collectors.toSet()));
  }
}
