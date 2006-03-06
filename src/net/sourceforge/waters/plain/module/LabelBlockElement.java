//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   LabelBlockElement
//###########################################################################
//# $Id: LabelBlockElement.java,v 1.4 2006-03-06 17:08:45 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;


/**
 * An immutable implementation of the {@link LabelBlockProxy} interface.
 *
 * @author Robi Malik
 */

public final class LabelBlockElement
  extends EventListExpressionElement
  implements LabelBlockProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new label block.
   * @param eventList The list of events of the new label block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new label block, or <CODE>null</CODE>.
   */
  public LabelBlockElement(final Collection<? extends Proxy> eventList,
                           final LabelGeometryProxy geometry)
  {
    super(eventList);
    mGeometry = geometry;
  }

  /**
   * Creates a new label block using default values.
   * This constructor creates a label block with
   * an empty list of events and
   * the geometry set to <CODE>null</CODE>.
   */
  public LabelBlockElement()
  {
    this(emptyProxyList(),
         null);
  }


  //#########################################################################
  //# Cloning
  public LabelBlockElement clone()
  {
    return (LabelBlockElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final LabelBlockElement downcast = (LabelBlockElement) partner;
      return
        Geometry.equalGeometry(mGeometry, downcast.mGeometry);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitLabelBlockProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.LabelBlockProxy
  public LabelGeometryProxy getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final LabelGeometryProxy mGeometry;

}
