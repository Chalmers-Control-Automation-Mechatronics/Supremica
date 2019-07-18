package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.ComponentKind;

/**
 * A configurable parameter of a {@link ModelAnalyzer} of <CODE>ComponentKind</CODE> type.
 *
 * @author Brandon Bassett
 */
public class ComponentKindParameter extends EnumParameter<ComponentKind>
{
  public ComponentKindParameter(final ComponentKindParameter template)
  {
    //template already has null added to list
    super(template);
  }

  public ComponentKindParameter(final int id,
                       final String name,
                       final String description,
                       final ComponentKind[] data)
  {
    super(id, name, description, data, data[0]);

    final ArrayList<ComponentKind> tmp = new ArrayList<>(mList);
    tmp.add(null);
    mList = tmp;
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final Vector<ComponentKind> vector = new Vector<> (mList);
    final JComboBox<ComponentKind> ret = new JComboBox<>(vector);
    ret.setSelectedItem(mValue);

    DESContext = model;

    final ComponentKindRenderer renderer= new ComponentKindRenderer();
    ret.setRenderer(renderer);
    return ret;
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((ComponentKindParameter) p).getValue();
  }

  //#########################################################################
  //# Data Variables
  private ProductDESContext DESContext;

  //#########################################################################
  //# Private Class

  private class ComponentKindRenderer extends JLabel implements ListCellRenderer<ComponentKind>
  {
    private static final long serialVersionUID = 1L;

    public ComponentKindRenderer()
    {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends ComponentKind> list,
                                                  final ComponentKind value, final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      //Highlight when hover over
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      //Set the icon and text
      final Icon image = DESContext.getComponentKindIcon(value);

      if(image != null) {
        setIcon(image);
        setText(value.toString());
      }
      else {
        setIcon(null);
        setText("null");
      }

      return this;
    }
  }
}
