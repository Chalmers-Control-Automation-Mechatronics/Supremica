package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.base.ComponentKind;

/**
 * A configurable parameter of a {@link ComponentKind}.
 *
 * @author Brandon Bassett
 */
public class ComponentKindParameter extends EnumParameter<ComponentKind>
{
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

    //Using IconAndFontLoader causes ant initialize to fail

    //final ComponentKindRenderer renderer= new ComponentKindRenderer();
    //ret.setRenderer(renderer);
    return ret;
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((ComponentKindParameter) p).getValue();
  }

  //#########################################################################
  //# Private Class
  /*
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
      if(value == ComponentKind.PLANT) {
        setIcon(IconAndFontLoader.ICON_PLANT);
        setText(ComponentKind.PLANT.toString());
      }
      else if(value == ComponentKind.PROPERTY) {
        setIcon(IconAndFontLoader.ICON_PROPERTY);
        setText(ComponentKind.PROPERTY.toString());
      }
      else if(value == ComponentKind.SPEC) {
        setIcon(IconAndFontLoader.ICON_SPEC);
        setText(ComponentKind.SPEC.toString());
      }
      else if(value == ComponentKind.SUPERVISOR) {
        setIcon(IconAndFontLoader.ICON_SUPERVISOR);
        setText(ComponentKind.SUPERVISOR.toString());
      }
      else {
        setIcon(null);
        setText("null");
      }

      return this;
    }
  }*/
}
