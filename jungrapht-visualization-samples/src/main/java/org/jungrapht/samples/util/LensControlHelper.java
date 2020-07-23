package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.jungrapht.visualization.transform.LensSupport;

/** Helper class to manage buttons to activate Lens controls */
public class LensControlHelper {

  /** Builder for {@code LensControlHelper} */
  public static class Builder {

    /** {@code Map} of String button names to {@code LensSupport} instances */
    private final Map<String, LensSupport> map;

    /** optional title for the group of buttons (or for JMenu) */
    String title;

    /** a {@code JComponent} container for the group of activation buttons */
    private JComponent container = Box.createHorizontalBox();

    /** supplier for activation control buttons (typically {@link JButton} or {@link JMenuItem}) */
    private Supplier<AbstractButton> buttonSupplier = JToggleButton::new;

    /**
     * create a Builder with required {@code Map} of String button names to {@code LensSupport}
     * instances
     *
     * @param map
     */
    private Builder(Map<String, LensSupport> map) {
      this.map = map;
    }

    /**
     * @param containerSupplier a {@link Supplier} for the desired container type
     * @return this Builder
     */
    public Builder containerSupplier(Supplier<JComponent> containerSupplier) {
      this.container = containerSupplier.get();
      return this;
    }

    /**
     * @param containerLayoutManager a {@link LayoutManager} for the container
     * @return this Builder
     */
    public Builder containerLayoutManager(LayoutManager containerLayoutManager) {
      this.container.setLayout(containerLayoutManager);
      return this;
    }

    /**
     * @param buttonSupplier a {@link Supplier} for the activation control buttons
     * @return
     */
    public Builder buttonSupplier(Supplier<AbstractButton> buttonSupplier) {
      this.buttonSupplier = buttonSupplier;
      return this;
    }

    /**
     * @param title optional title for the Container
     * @return this Builder
     */
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    /** @return a configured instance of the Builder */
    public LensControlHelper build() {
      return new LensControlHelper(this);
    }
  }

  /**
   * @param map required {@code Map} of String button names to {@code LensSupport} instances
   * @return a new Builder
   */
  public static Builder builder(Map<String, LensSupport> map) {
    return new Builder(map);
  }

  /** @param builder {@code Builder} with configurations for {@code LensControlHelper} */
  private LensControlHelper(Builder builder) {
    this(builder.container, builder.map, builder.title, builder.buttonSupplier);
  }

  /** the container to hold the lens activation buttons */
  private JComponent container;

  /** {@link Supplier} for activation control buttons */
  private Supplier<AbstractButton> buttonSupplier;

  /** optional title for the button group container */
  private String title;

  private ButtonGroup radio =
      new ButtonGroup() {

        @Override
        public void setSelected(ButtonModel model, boolean selected) {
          if (selected) {
            super.setSelected(model, selected);
          } else {
            clearSelection();
          }
        }
      };

  /**
   * @param <T> the container type
   * @return the original container, possible with a title
   */
  public <T extends JComponent> T container() {
    if (title != null) {
      if (container instanceof AbstractButton) { // true if the container is a JMenu
        ((AbstractButton) container).setText(title);
      } else {
        container.setBorder(new TitledBorder(title));
      }
    }
    return (T) container;
  }

  /**
   * Populate the supplied container with lens activation buttons. If there are more than 3 entries
   * in the map, create a horizontal row for every 2 buttons. Create and add a 'None' (turn off
   * lenses) button as the last item.
   *
   * @param container a Container to hold the collection of lens activation buttons
   * @param map of button title to {@link LensSupport} implementation
   * @param buttonSupplier a supplier for buttons (e.g. JButton or JMenuButton)
   */
  private LensControlHelper(
      JComponent container,
      Map<String, LensSupport> map,
      String title,
      Supplier<AbstractButton> buttonSupplier) {
    this.container = container;
    this.title = title;
    this.buttonSupplier = buttonSupplier;
    map.entrySet().forEach(entry -> addControls(entry, map.values(), this.container));
  }

  /**
   * Add an activation {@link JButton} for a {@link LensSupport} implementation. Add an {@code
   * actionListener} to activate the {@code LensSupport} after deactivating all {@code LensSupport}
   * instance in the {@code lenses Collection} add a {@code JButton} activation control for the
   * provided {@code Map.Entry}
   *
   * @param entry a String mapped to a {@link LensSupport} implementation
   * @param lenses a {@code Collection} of all of the {@link LensSupport} instances
   * @param container to hold the activation button
   */
  private void addControls(
      Map.Entry<String, LensSupport> entry, Collection<LensSupport> lenses, JComponent container) {
    AbstractButton button = buttonSupplier.get();
    radio.add(button);
    button.setText(entry.getKey());
    button.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            entry.getValue().activate();
          } else {
            entry.getValue().deactivate();
          }
        });
    entry
        .getValue()
        .addItemListener(
            e -> {
              if (e.getStateChange() == ItemEvent.DESELECTED) {
                radio.clearSelection();
              }
            });
    container.add(button);
  }
}
