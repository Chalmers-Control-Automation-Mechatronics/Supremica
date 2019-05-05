package net.sourceforge.waters.gui.options;

import java.awt.Component;

import javax.swing.JTextField;


public class StringParameter extends Parameter
{

  private String mValue;

  public StringParameter(final int id, final String name,
                         final String description, final String value)
  {
    super(id, name, description);
    mValue = value;
  }

  public StringParameter(final int id, final String name,
                         final String description)
  {
    this(id, name, description, "");
  }

  public String getValue()
  {
    return mValue;
  }

  public void setValue(final String val)
  {
    mValue = val;
  }

  @Override
  public Component createComponent()
  {
    final JTextField ret = new JTextField();
    ret.setText(mValue);
    ret.setColumns(20);
    return ret;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JTextField textField = (JTextField) comp;
    setValue(textField.getText());
  }

  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JTextField textField = (JTextField) comp;
    textField.setText(mValue);
  }
}
