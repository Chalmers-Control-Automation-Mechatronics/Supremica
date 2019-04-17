package net.sourceforge.waters.gui.options;

import java.awt.Component;

import javax.swing.JCheckBox;


public class BoolParameter extends Parameter
{
  @SuppressWarnings("unused")
  private final boolean mValue;
  private final JCheckBox component;

  public BoolParameter(final int id, final String name,
                       final String description, final boolean value)
  {
    super(id, name, description);
    mValue = value;
    component = new JCheckBox(name, value);
    component.setToolTipText(description);
  }

  @Override
  public Component getComponent()
  {
    return component;
  }
}
