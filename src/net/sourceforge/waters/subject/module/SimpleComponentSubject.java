//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   SimpleComponentSubject
//###########################################################################
//# $Id: SimpleComponentSubject.java,v 1.4 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
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
   * @param variables The variables of the new simple component.
   */
  public SimpleComponentSubject(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph,
                                final Collection<? extends VariableProxy> variables)
  {
    super(identifier);
    mKind = kind;
    mGraph = (GraphSubject) graph;
    mGraph.setParent(this);
    mVariables = new ArrayListSubject<VariableSubject>
      (variables, VariableSubject.class);
    mVariables.setParent(this);
  }

  /**
   * Creates a new simple component using default values.
   * This constructor creates a simple component with
   * an empty variables.
   * @param identifier The identifier defining the name of the new simple component.
   * @param kind The kind of the new simple component.
   * @param graph The graph of the new simple component.
   */
  public SimpleComponentSubject(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph)
  {
    this(identifier,
         kind,
         graph,
         emptyVariableProxyList());
  }


  //#########################################################################
  //# Cloning
  public SimpleComponentSubject clone()
  {
    final SimpleComponentSubject cloned = (SimpleComponentSubject) super.clone();
    cloned.mGraph = mGraph.clone();
    cloned.mGraph.setParent(cloned);
    cloned.mVariables = mVariables.clone();
    cloned.mVariables.setParent(cloned);
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
        mGraph.equals(downcast.mGraph) &&
        mVariables.equals(downcast.mVariables);
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

  public List<VariableProxy> getVariables()
  {
    final List<VariableProxy> downcast = Casting.toList(mVariables);
    return Collections.unmodifiableList(downcast);
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

  public ListSubject<VariableSubject> getVariablesModifiable()
  {
    return mVariables;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<VariableProxy> emptyVariableProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private ComponentKind mKind;
  private GraphSubject mGraph;
  private ListSubject<VariableSubject> mVariables;

}
