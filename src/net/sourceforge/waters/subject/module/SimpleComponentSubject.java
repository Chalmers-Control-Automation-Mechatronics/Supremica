//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleComponentSubject
//###########################################################################
//# $Id: SimpleComponentSubject.java,v 1.3 2005-12-03 21:30:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;

import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * The subject implementation of the {@link SimpleComponentProxy} interface.
 *
 * @author Robi Malik
 */

public final class SimpleComponentSubject
  extends ComponentSubject
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
  public SimpleComponentSubject(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph)
  {
    super(identifier);
    mKind = kind;
    mGraph = (GraphSubject) graph;
    mGraph.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public SimpleComponentSubject clone()
  {
    final SimpleComponentSubject cloned = (SimpleComponentSubject) super.clone();
    cloned.mGraph = mGraph.clone();
    cloned.mGraph.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final SimpleComponentSubject downcast = (SimpleComponentSubject) partner;
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
      final SimpleComponentSubject downcast = (SimpleComponentSubject) partner;
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

  public GraphSubject getGraph()
  {
    return mGraph;
  }


  //#########################################################################
  //# Setters
  public void setKind(final ComponentKind kind)
  {
    if (mKind.equals(kind)) {
      return;
    }
    mKind = kind;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  public void setGraph(final GraphSubject graph)
  {
    if (mGraph == graph) {
      return;
    }
    graph.setParent(this);
    mGraph.setParent(null);
    mGraph = graph;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private ComponentKind mKind;
  private GraphSubject mGraph;

}
