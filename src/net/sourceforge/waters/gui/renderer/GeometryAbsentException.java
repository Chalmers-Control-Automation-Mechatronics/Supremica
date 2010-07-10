package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.properties.Config;

public class GeometryAbsentException
  extends Exception
{

  //#########################################################################
  //# Constructors
  public GeometryAbsentException(final int numNodes)
  {
    mNumNodes = numNodes;
    mGroupNode = null;
  }

  public GeometryAbsentException(final GroupNodeProxy group)
  {
    mNumNodes = 0;
    mGroupNode = group;
  }


  //#########################################################################
  //# Messages
  public String getMessage()
  {
    return getMessage(null);
  }

  public String getMessage(final SimpleComponentProxy comp)
  {
    final StringBuffer buffer = new StringBuffer();
    if (mGroupNode != null) {
      buffer.append("There is no geometry information for group node ");
      buffer.append(mGroupNode.getName());
      buffer.append(" in ");
      appendComponentName(buffer, comp);
      buffer.append('.');
    } else {
      appendComponentName(buffer, comp);
      buffer.append(" has ");
      buffer.append(mNumNodes);
      buffer.append(" states. Graphs with more than ");
      final int max = Config.DOT_MAX_NBR_OF_STATES.get();
      buffer.append(max);
      buffer.append(" states cannot be displayed.");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void appendComponentName(final StringBuffer buffer,
                                   final SimpleComponentProxy comp)
  {
    if (comp != null) {
      final ComponentKind kind = comp.getKind();
      final String name = comp.getName();
      if (name.length() <= 64) {
        buffer.append(ModuleContext.getComponentKindToolTip(kind));
        buffer.append(' ');
        buffer.append(name);
      } else {
        if (buffer.length() > 0) {
          buffer.append("this ");
        } else {
          buffer.append("This ");
        }
        buffer.append(ModuleContext.getComponentKindToolTip(kind));
      }
    } else {
      if (buffer.length() > 0) {
        buffer.append("this graph");
      } else {
        buffer.append("This graph");
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final int mNumNodes;
  private final GroupNodeProxy mGroupNode;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
