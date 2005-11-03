//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleComponentElement
//###########################################################################
//# $Id: SimpleComponentElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An immutable implementation of the {@link SimpleComponentProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleComponentElement
  extends ComponentElement
  implements SimpleComponentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simple component.
   * @param identifier The identifier defining the name of the new simple component.
   * @param kind The kind of the new simple component.
   * @param graph The graph of the new simple component.
   */
  public SimpleComponentElement(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph)
  {
    super(identifier);
    mKind = kind;
    mGraph = graph;
  }


  //#########################################################################
  //# Cloning
  public SimpleComponentElement clone()
  {
    return (SimpleComponentElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SimpleComponentElement downcast = (SimpleComponentElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        mGraph.equals(downcast.mGraph);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final SimpleComponentElement downcast = (SimpleComponentElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        mGraph.equalsWithGeometry(downcast.mGraph);
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
    return downcast.visitSimpleComponentProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.SimpleComponentProxy
  public ComponentKind getKind()
  {
    return mKind;
  }

  public GraphProxy getGraph()
  {
    return mGraph;
  }


  //#########################################################################
  //# Data Members
  private final ComponentKind mKind;
  private final GraphProxy mGraph;

}
