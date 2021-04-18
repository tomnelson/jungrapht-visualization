package org.jungrapht.visualization.control.modal;

import java.awt.event.ItemEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import org.jungrapht.visualization.control.modal.Modal.Mode;

public class ModeContainer {

  public static class Builder {
    protected Supplier<JComponent> containerSupplier;
    protected Supplier<JButton> buttonSupplier;
    protected Mode[] modes;
    protected Set<Modal> modals = new LinkedHashSet<>();

    public Builder containerSupplier(Supplier<JComponent> containerSupplier) {
      this.containerSupplier = containerSupplier;
      return this;
    }

    public Builder buttonSupplier(Supplier<JButton> buttonSupplier) {
      this.buttonSupplier = buttonSupplier;
      return this;
    }

    public Builder modes(Mode... modes) {
      this.modes = modes;
      return this;
    }

    public Builder modals(Modal... modals) {
      this.modals = Stream.of(modals).collect(Collectors.toCollection(LinkedHashSet::new));
      return this;
    }

    public ModeContainer build() {
      return new ModeContainer(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected JComponent container;
  protected Supplier<JComponent> containerSupplier;
  protected Supplier<JButton> buttonSupplier;
  protected Mode[] modes;
  protected Set<Modal> modals;

  ModeContainer(Builder builder) {
    this(builder.containerSupplier, builder.buttonSupplier, builder.modes, builder.modals);
  }

  private ModeContainer(
      Supplier<JComponent> containerSupplier,
      Supplier<JButton> buttonSupplier,
      Mode[] modes,
      Set<Modal> modals) {
    this.containerSupplier = containerSupplier;
    this.buttonSupplier = buttonSupplier;
    this.modes = modes;
    this.modals = modals;
  }

  public JComponent getContainer() {
    return this.container;
  }

  public boolean addModal(Modal modal) {
    return this.modals.add(modal);
  }

  public boolean removeModal(Modal modal) {
    return this.modals.remove(modal);
  }

  protected final void buildUI() {
    this.container = containerSupplier.get();
    Stream.of(modes)
        .forEach(
            m -> {
              JButton button = buttonSupplier.get();
              button.setText(m.name());
              container.add(button);
              button.addItemListener(
                  e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                      Mode mode = Mode.valueOf(e.getItem().toString());
                      modals.forEach(modal -> modal.setMode(mode));
                    }
                  });
            });
  }
}
