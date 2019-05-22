package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.analysis.EnumFactory;


public class EnumParameter<T> extends Parameter
{

  //#########################################################################
  //# Constructors
  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final List<? extends T> data)
  {
    super(id, name, description);
    mList = data;
    mValue = mList.get(0);
  }

  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final T[] data)
  {
    this(id, name, description, Arrays.asList(data));
  }

  public EnumParameter(final int id,
                       final String name,
                       final String description,
                       final EnumFactory<? extends T> factory)
  {
    this(id, name, description, factory.getEnumConstants());
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Parameter
  @Override
  public Component createComponent()
  {
    final Vector<T> vector = new Vector<> (mList);
    final JComboBox<T> ret = new JComboBox<>(vector);
    return ret;
  }

  public T getValue()
  {
    return  mValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboBox = (JComboBox<T>) comp;
    final int index = comboBox.getSelectedIndex();
    mValue = comboBox.getItemAt(index);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboBox = (JComboBox<T>) comp;
    comboBox.setSelectedItem(mValue);
  }


  //#########################################################################
  //# Data Members
  private List<? extends T> mList;
  private T mValue;

}
