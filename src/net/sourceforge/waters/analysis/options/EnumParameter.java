package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComboBox;

public class EnumParameter<T> extends Parameter
{
  private List<T> mList;
  private T mData;

  public EnumParameter(final int id, final String name, final String description, final List<T> data)
  {
    super(id, name, description);
    mList = data;
    mData = mList.get(0);
  }

  public EnumParameter(final int id, final String name, final String description, final T[] data)
  {
    this(id, name, description, Arrays.asList(data));
  }

  public EnumParameter(final int id, final String name, final String description, final Set<T> data)
  {
    this(id, name, description, new ArrayList<T>(data));
  }

  @Override
  public Component createComponent()
  {
    final Vector<T> vector = new Vector<> (mList);
    final JComboBox<T> ret = new JComboBox<>(vector);
    return ret;
  }

  public T getValue()
  {
    return  mData;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboField = (JComboBox<T>) comp;
    mData = (T) comboField.getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<T> comboField = (JComboBox<T>) comp;
    comboField.setSelectedItem(mData);
  }
}
