package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.ComponentKind;

/**
 * A configurable parameter of a {@link ModelAnalyzer} of
 * <CODE>ComponentKind</CODE> type.
 *
 * @author Brandon Bassett
 */

public class ComponentKindParameter extends EnumParameter<ComponentKind>
{
  public ComponentKindParameter(final ComponentKindParameter template)
  {
    //template already has null added to list
    super(template, template.getList());
  }

  // TODO We do not need the 4th argument, which is always the same.
  public ComponentKindParameter(final int id,
                                final String name,
                                final String description,
                                final ComponentKind[] data)
  {
    super(id, name, description, ComponentKind.values());
    final ArrayList<ComponentKind> tmp = new ArrayList<>(mList);
    // TODO Do not allow null, but calculate a default value like George did
    // in the (now commented) method SynchronousProductDialog.getKind().
    tmp.add(null);
    mList = tmp;
  }

  @Override
  public Component createComponent(final ProductDESContext context)
  {
    mDESContext = context;
    @SuppressWarnings("unchecked")
    final
    JComboBox<ComponentKind> comboBox =
      (JComboBox<ComponentKind>) super.createComponent(context);
    final ComponentKindRenderer renderer= new ComponentKindRenderer();
    comboBox.setRenderer(renderer);
    return comboBox;
  }


  //#########################################################################
  //# Inner Class ComponentKindRenderer
  private class ComponentKindRenderer extends JLabel
    implements ListCellRenderer<ComponentKind>
  {
    private ComponentKindRenderer()
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
      final Icon image = mDESContext.getComponentKindIcon(value);
      if(image != null) {
        setIcon(image);
        setText(value.toString());
      } else {
        setIcon(null);
        setText("null");
      }
      return this;
    }

    private static final long serialVersionUID = 3036791589875590296L;
  }


  //#########################################################################
  //# Data Members
  private ProductDESContext mDESContext;

}
