package org.jungrapht.visualization.control.modal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModeControl implements Modal {

  public static class Builder {
    protected Mode mode;
    protected Set<Modal> modals = new LinkedHashSet<>();

    public Builder mode(Mode mode) {
      this.mode = mode;
      return this;
    }

    public Builder modals(Modal... modals) {
      this.modals = Stream.of(modals).collect(Collectors.toCollection(LinkedHashSet::new));
      return this;
    }

    public ModeControl build() {
      return new ModeControl(this);
    }
  }

  protected Mode mode;
  protected Set<Modal> modals;

  public static Builder builder() {
    return new Builder();
  }

  ModeControl(Builder builder) {
    this.mode = builder.mode;
    this.modals = builder.modals;
  }

  public boolean addModal(Modal modal) {
    return this.modals.add(modal);
  }

  public boolean removeModal(Modal modal) {
    return this.modals.remove(modal);
  }

  @Override
  public void setMode(Mode mode) {
    if (this.mode != mode) {
      this.mode = mode;
      modals.forEach(modal -> modal.setMode(mode));
    }
  }

  @Override
  public Mode getMode() {
    return this.mode;
  }
}
