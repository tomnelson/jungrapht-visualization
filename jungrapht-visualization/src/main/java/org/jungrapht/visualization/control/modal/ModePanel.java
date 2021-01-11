package org.jungrapht.visualization.control.modal;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModePanel extends JPanel implements Modal {

  private static final Logger log = LoggerFactory.getLogger(ModePanel.class);

  public static class Builder {
    protected Supplier<AbstractButton> buttonSupplier;
    protected Mode[] modes;
    protected Set<Modal> modals = new LinkedHashSet<>();
    protected Mode mode;

    public Builder buttonSupplier(Supplier<AbstractButton> buttonSupplier) {
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

    public Builder mode(Mode mode) {
      this.mode = mode;
      return this;
    }

    public ModePanel build() {
      return new ModePanel(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected Supplier<AbstractButton> buttonSupplier;
  protected Mode[] modes;
  protected Set<Modal> modals;
  protected ButtonGroup buttonGroup = new ButtonGroup();
  protected Mode mode;
  protected Map<Mode, AbstractButton> modeMap = new HashMap<>();

  ModePanel(Builder builder) {
    this(builder.mode, builder.buttonSupplier, builder.modes, builder.modals);
  }

  private ModePanel(
      Mode mode, Supplier<AbstractButton> buttonSupplier, Mode[] modes, Set<Modal> modals) {
    setLayout(new GridLayout(0, 1));
    this.mode = mode;
    this.buttonSupplier = buttonSupplier;
    this.modes = modes;
    this.modals = modals;
    if (this.mode == null) {
      this.mode = modes[0];
    }
  }

  public boolean addModal(Modal modal) {
    return this.modals.add(modal);
  }

  public boolean removeModal(Modal modal) {
    return this.modals.remove(modal);
  }

  public void addModals(Modal... modals) {
    Stream.of(modals).forEach(this.modals::add);
  }

  public void removeModal(Modal... modals) {
    Stream.of(modals).forEach(this.modals::remove);
  }

  public ModePanel buildUI() {
    Stream.of(modes)
        .forEach(
            m -> {
              AbstractButton button = buttonSupplier.get();
              button.setText(m.name());
              buttonGroup.add(button);
              modeMap.put(m, button);
              this.add(button);
              button.addItemListener(
                  e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                      String text = ((AbstractButton) e.getItem()).getText();
                      Mode mode = Mode.valueOf(text);
                      log.info("setting mode to {} for {}", mode, modals);
                      modals.forEach(modal -> modal.setMode(mode));
                    }
                  });
            });
    modals.forEach(modal -> modal.setMode(mode));
    setMode(this.mode);

    return this;
  }

  @Override
  public void setMode(Mode mode) {
    log.info("changing mode from {} to {}", this.mode, mode);
    AbstractButton button = modeMap.get(mode);
    if (!button.isSelected()) {
      button.setSelected(true);
    }
  }

  @Override
  public Mode getMode() {
    return this.mode;
  }
}
