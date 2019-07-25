package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import net.sourceforge.waters.model.analysis.EnumFactory;

public class JListParameter<T> extends Parameter
{
  //#########################################################################
  //# Constructors
  public JListParameter(final JListParameter<T> template)
  {
    this(template.getID(), template.getName(),
         template.getDescription(), template.getList(), template.getValue());
  }

  JListParameter(final int id,
                 final String name,
                 final String description,
                 final List<? extends T> data,
                 final List<? extends T> defaultValue)
   {
     super(id, name, description);
     mList = data;
     mValue = defaultValue;
   }

  JListParameter(final int id,
                final String name,
                final String description,
                final List<? extends T> data,
                final T defaultValue)
  {
    super(id, name, description);
    mList = data;
    //T[] spam = new T[] {defaultValue};
    @SuppressWarnings("unchecked")
    final T[] arr = (T[])new Object[1];
    arr[0] = defaultValue;
    final List<T> list = Arrays.asList(arr);
    mValue = list;
  }

  public JListParameter(final int id,
                final String name,
                final String description,
                final T[] data)
  {
    this(id, name, description, data, data[0]);
  }

  public JListParameter(final int id,
                final String name,
                final String description,
                final T[] data,
                final T defaultValue)
  {
    this(id, name, description, Arrays.asList(data), defaultValue);
  }

  JListParameter(final int id,
                final String name,
                final String description,
                final EnumFactory<? extends T> factory)
  {
    this(id, name, description,
         factory.getEnumConstants(), factory.getDefaultValue());
  }

  JListParameter(final int id,
                final String name,
                final String description)
  {
    super(id, name, description);
    mList = null;
    mValue = null;
  }

  JListParameter(final int id,
                final EnumParameter<T> template,
                final EnumFactory<? extends T> factory)
  {
    this(id, template.getName(), template.getDescription(), factory);
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Parameter
  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final Vector<T> vector = new Vector<> (mList);
    final JList<T> ret = new JList<>(vector);
    ret.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    if(getIndex() != null)
      ret.setSelectedIndices(getIndex());
    ret.setLayoutOrientation(JList.VERTICAL);
    return ret;
  }

  public int[] getIndex() {

    if(mValue == null)
      return null;

    final List<Integer> tmpInt = new ArrayList<Integer>();

    for(final T a: mValue)
      tmpInt.add(mList.indexOf(a));

    final int[] arr = tmpInt.stream().mapToInt(i -> i).toArray();

    return arr;
}

  public List<? extends T> getValue()
  {
    return mValue;
  }

  public List<? extends T> getList()
  {
    return mList;
  }

  public void setValue(final List<? extends T> value)
  {
    mValue = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JList<T> list = (JList<T>) comp;
    mValue = list.getSelectedValuesList();
  }

  @Override
  public void displayInGUI(final ParameterPanel panel)
  {

  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((JListParameter<T>) p).getValue();
  }

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() +" Value: " + getValue());
  }

  //#########################################################################
  //# Data Members
  private final List<? extends T> mList;
  private List<? extends T> mValue;
}
