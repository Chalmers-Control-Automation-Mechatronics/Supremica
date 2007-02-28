//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   SimpleComponentElement
//###########################################################################
//# $Id: SimpleComponentElement.java,v 1.11 2007-02-28 00:03:24 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableProxy;

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
   * @param variables The variables of the new simple component, or <CODE>null</CODE> if empty.
   */
  public SimpleComponentElement(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph,
                                final Collection<? extends VariableProxy> variables)
  {
    super(identifier);
    mKind = kind;
    mGraph = graph;
    if (variables == null) {
      mVariables = Collections.emptyList();
    } else {
      final List<VariableProxy> variablesModifiable =
        new ArrayList<VariableProxy>(variables);
      mVariables =
        Collections.unmodifiableList(variablesModifiable);
    }
  }

  /**
   * Creates a new simple component using default values.
   * This constructor creates a simple component with
   * an empty variables.
   * @param identifier The identifier defining the name of the new simple component.
   * @param kind The kind of the new simple component.
   * @param graph The graph of the new simple component.
   */
  public SimpleComponentElement(final IdentifierProxy identifier,
                                final ComponentKind kind,
                                final GraphProxy graph)
  {
    this(identifier,
         kind,
         graph,
         null);
  }


  //#########################################################################
  //# Cloning
  public SimpleComponentElement clone()
  {
    return (SimpleComponentElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final SimpleComponentElement downcast = (SimpleComponentElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        mGraph.equalsByContents(downcast.mGraph) &&
        ProxyTools.isEqualListByContents
          (mVariables, downcast.mVariables);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final SimpleComponentElement downcast = (SimpleComponentElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        mGraph.equalsWithGeometry(downcast.mGraph) &&
        ProxyTools.isEqualListWithGeometry
          (mVariables, downcast.mVariables);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    result += mGraph.hashCodeByContents();
    result *= 5;
    result += ProxyTools.getListHashCodeByContents(mVariables);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    result += mGraph.hashCodeWithGeometry();
    result *= 5;
    result += ProxyTools.getListHashCodeWithGeometry(mVariables);
    return result;
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

  public List<VariableProxy> getVariables()
  {
    return mVariables;
  }


  //#########################################################################
  //# Data Members
  private final ComponentKind mKind;
  private final GraphProxy mGraph;
  private final List<VariableProxy> mVariables;

}
