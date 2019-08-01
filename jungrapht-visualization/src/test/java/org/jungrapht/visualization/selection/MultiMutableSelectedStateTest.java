package org.jungrapht.visualization.selection;

import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiMutableSelectedStateTest {

  private static final Logger log = LoggerFactory.getLogger(MultiMutableSelectedStateTest.class);

  private MultiMutableSelectedState<String> multiMutableSelectedState =
      new MultiMutableSelectedState<>();

  @Before
  public void setup() {
    multiMutableSelectedState.clear();
  }

  /**
   * Test selection of one new item. Should become a member of the selected set and should not fire
   * a deselected event
   */
  @Test
  public void testSelectOne() {
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::selected, this::forbidden));
    multiMutableSelectedState.select("A");
    Assert.assertTrue(multiMutableSelectedState.getSelected().contains("A"));
    Assert.assertEquals(multiMutableSelectedState.getSelected(), Collections.singleton("A"));
  }

  /**
   * Select 'reselection' of one item. Should be removed from selected set and should not fire a
   * selected event
   */
  @Test
  public void testToggleSelectionOfOne() {
    multiMutableSelectedState.select("A");
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::forbidden, this::deselected));
    // pick the already selected item. Should be removed and should fire event
    // to deselect. should not fire an event to select
    multiMutableSelectedState.select("A");
    Assert.assertFalse(multiMutableSelectedState.getSelected().contains("A"));
    Assert.assertTrue(multiMutableSelectedState.getSelected().isEmpty());
  }

  /**
   * Test new selection of several items Should become the selected items and should not fire a
   * deselection event
   */
  @Test
  public void testSelectMany() {
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::selected, this::forbidden));
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    Assert.assertEquals(multiMutableSelectedState.getSelected(), Sets.newHashSet("A", "B", "C"));
  }

  /**
   * Test re-selection of many that were already selected Should all be removed from the selected
   * set and should not fire a selected event
   */
  @Test
  public void testToggleSelectionOfMany() {
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::forbidden, this::deselected));
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    Assert.assertFalse(
        multiMutableSelectedState.getSelected().containsAll(Sets.newHashSet("A", "B", "C")));
    Assert.assertTrue(multiMutableSelectedState.getSelected().isEmpty());
  }

  /**
   * Test add one new item to those already selected Should be added to the selected items and
   * should not fire a deselected event
   */
  @Test
  public void testAddOneToExistingSelection() {
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::selected, this::forbidden));
    multiMutableSelectedState.add("D");
    Assert.assertTrue(multiMutableSelectedState.getSelected().contains("D"));
    Assert.assertEquals(
        multiMutableSelectedState.getSelected(), Sets.newHashSet("A", "B", "C", "D"));
  }

  /**
   * Test select one new item when some are already selected. Selected items should be only the new
   * item. Should fire a selected event for "D" and a deselected event for "A", "B", "C"
   */
  @Test
  public void testSelectOneWithExistingSelection() {
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::selected, this::deselected));
    multiMutableSelectedState.select("D");
    Assert.assertTrue(multiMutableSelectedState.getSelected().contains("D"));
    Assert.assertEquals(multiMutableSelectedState.getSelected(), Sets.newHashSet("D"));
  }

  /**
   * Test reselect of one item of many that are already selected Should remove that item from the
   * selected set and should not fire a selected event
   */
  @Test
  public void testRemoveOneFromExistingSelection() {
    multiMutableSelectedState.select(Sets.newHashSet("A", "B", "C"));
    multiMutableSelectedState.addItemListener(
        new SelectedState.StateChangeListener<>(this::forbidden, this::deselected));
    multiMutableSelectedState.add("A");
    Assert.assertFalse(multiMutableSelectedState.getSelected().contains("A"));
    Assert.assertEquals(multiMutableSelectedState.getSelected(), Sets.newHashSet("B", "C"));
  }

  private void selected(Object item) {
    String methodName = Thread.currentThread().getStackTrace()[6].getMethodName();
    log.info("{} selected {}", methodName, item);
  }

  private void deselected(Object item) {
    String methodName = Thread.currentThread().getStackTrace()[6].getMethodName();
    log.info("{} deselected {}", methodName, item);
  }

  private void forbidden(Object item) {
    Assert.fail("Should not have gotten " + item);
  }
}
