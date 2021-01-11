package org.jungrapht.visualization.control.modal;

import java.awt.event.ItemEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import org.jungrapht.visualization.control.modal.Modal.Mode;

public class ModeComboBox extends JComboBox<Mode> implements Modal {

  public static class Builder {
    //        protected Supplier<AbstractButton> buttonSupplier;
    protected Mode[] modes;
    protected Set<Modal> modals = new LinkedHashSet<>();
    protected Mode mode;
    //        public Builder buttonSupplier(Supplier<AbstractButton> buttonSupplier) {
    //            this.buttonSupplier = buttonSupplier;
    //            return this;
    //        }
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

    public ModeComboBox build() {
      return new ModeComboBox(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
  //    protected Supplier<AbstractButton> buttonSupplier;
  protected Mode[] modes;
  protected Set<Modal> modals;
  //    protected ButtonGroup buttonGroup = new ButtonGroup();
  protected Mode mode;
  //    protected Map<Mode, AbstractButton> modeMap
  //            = new HashMap<>();
  ModeComboBox(Builder builder) {
    this(builder.mode, builder.modals, builder.modes);
  }

  private ModeComboBox(Mode mode, Set<Modal> modals, Mode[] modes) {
    super(modes);
    this.mode = mode;
    //        this.buttonSupplier = buttonSupplier;
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

  public ModeComboBox buildUI() {
    this.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            Mode mode = (Mode) e.getItem();
            setMode(mode);
            modals.forEach(modal -> modal.setMode(mode));
          }
        });
    modals.forEach(modal -> modal.setMode(mode));
    setMode(this.mode);
    return this;
  }

  @Override
  public void setMode(Mode mode) {
    if (this.mode != mode) {
      this.mode = mode;
      setSelectedItem(mode);
    }
  }

  @Override
  public Mode getMode() {
    return this.mode;
  }
}
