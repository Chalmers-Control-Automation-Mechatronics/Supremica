package net.sourceforge.waters.analysis.options;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;

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
     super(template);
     mValue = null;
  }

  public ComponentKindParameter(final int id,
                                final String name,
                                final String description)
  {
    super(id, name, description, ComponentKind.values());
    mValue = null;
  }

  @Override
  public Component createComponent(final ProductDESContext context)
  {
    mDESContext = context;
    if(mValue == null) {
      mValue = getKind();
    }
    @SuppressWarnings("unchecked")
    final
    JComboBox<ComponentKind> comboBox =
      (JComboBox<ComponentKind>) super.createComponent(context);
    final ComponentKindRenderer renderer= new ComponentKindRenderer();
    comboBox.setRenderer(renderer);

    return comboBox;
  }

  private ComponentKind getKind()
  {
    int plantCount = 0;
    int propCount = 0;
    int specCount = 0;
    for (final AutomatonProxy aut : mDESContext.getActiveAutomata()) {
      switch (aut.getKind()) {
      case PLANT:
        plantCount++;
        break;
      case PROPERTY:
        propCount++;
        break;
      case SPEC:
        specCount++;
        break;
      case SUPERVISOR:
        break;
      }
    }
    if (plantCount > 0) {
      return ComponentKind.PLANT;
    } else if (propCount > 0) {
      return ComponentKind.PROPERTY;
    } else if (specCount > 0) {
      return ComponentKind.SPEC;
    } else {
      return ComponentKind.SUPERVISOR;
    }
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
      setIcon(mDESContext.getComponentKindIcon(value));
      setText(mDESContext.getComponentKindText(value));

      return this;
    }

    private static final long serialVersionUID = 3036791589875590296L;
  }

  //#########################################################################
  //# Data Members
  private ProductDESContext mDESContext;

}
