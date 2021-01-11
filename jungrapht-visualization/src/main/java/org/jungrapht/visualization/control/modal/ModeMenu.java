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
import javax.swing.plaf.basic.BasicIconFactory;

public class ModeMenu extends JMenu implements Modal {

  public static class Builder {
    protected String menuText;
    protected Supplier<AbstractButton> buttonSupplier = JRadioButtonMenuItem::new;
    protected Mode[] modes;
    protected Set<Modal> modals = new LinkedHashSet<>();
    protected Mode mode;

    public Builder menuText(String menuText) {
      this.menuText = menuText;
      return this;
    }

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

    public ModeMenu build() {
      return new ModeMenu(this);
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

  ModeMenu(Builder builder) {
    this(builder.menuText, builder.mode, builder.buttonSupplier, builder.modes, builder.modals);
  }

  private ModeMenu(
      String menuText,
      Mode mode,
      Supplier<AbstractButton> buttonSupplier,
      Mode[] modes,
      Set<Modal> modals) {
    super(menuText);
    this.mode = mode;
    this.buttonSupplier = buttonSupplier;
    this.modes = modes;
    this.modals = modals;
    if (menuText == null) {
      Icon icon = BasicIconFactory.getMenuArrowIcon();
      this.setIcon(BasicIconFactory.getMenuArrowIcon());
      this.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));
    }
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

  public ModeMenu buildUI() {
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
    //    if (this.mode != mode) {
    AbstractButton button = modeMap.get(mode);
    if (!button.isSelected()) {
      button.setSelected(true);
    }
    //    }
    modals.forEach(modal -> modal.setMode(mode));
  }

  @Override
  public Mode getMode() {
    return this.mode;
  }
}
