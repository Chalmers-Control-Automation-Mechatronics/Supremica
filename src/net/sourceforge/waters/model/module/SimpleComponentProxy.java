//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.decl
//# CLASS:   SimpleComponentProxy
//###########################################################################
//# $Id: SimpleComponentProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.SimpleComponentType;
import net.sourceforge.waters.xsd.module.GraphType;


/**
 * <P>A component representing a single finite-state machine.</P>
 *
 * <P>Simple components are the basic way of representing the <I>plants</I>,
 * <I>specifications</I>, and <I>properties</I> in a Waters module.
 * Each simple component contains of the following information.</P>
 *
 * <DL>
 * <DT><I>Name.</I></DT>
 * <DD>The name uniquely identifies the component in its module.
 * It is of type {@link IdentifierProxy}, to support structured names
 * as they may occur in parameterised structures.</DD>
 * <DT><I>Kind.</I></DT>
 * <DD>The kind of a component identifies it as a <I>plants</I>,
 * <I>specification</I>, or <I>properties</I>. It uses the enumerative
 * type {@link ComponentKind}.</DD>
 * <DT><I>Graph.</I></DT>
 * <DD>The graph shows the states and transitions of the finite-state
 * machine representing the component. It is of type {@link GraphProxy}.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class SimpleComponentProxy extends ComponentProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a simple component with an empty deterministic graph.
   * @param  ident       The name for the new component.
   * @param  kind        The type of the new component, one of
   *                     {@link ComponentKind#PLANT},
   *                     {@link ComponentKind#SPEC}, or
   *                     {@link ComponentKind#PROPERTY}.
   */
  public SimpleComponentProxy(final IdentifierProxy ident,
			      final ComponentKind kind)
  {
    this(ident, kind, new GraphProxy());
  }

  /**
   * Creates a simple component with a given graph.
   * @param  ident       The name for the new component.
   * @param  kind        The type of the new component, one of
   *                     {@link ComponentKind#PLANT},
   *                     {@link ComponentKind#SPEC}, or
   *                     {@link ComponentKind#PROPERTY}.
   * @param  graph       The initial graph.
   */
  public SimpleComponentProxy(final IdentifierProxy ident,
			      final ComponentKind kind,
			      final GraphProxy graph)
  {
    super(ident);
    mKind = kind;
    mGraph = graph;
  }

  /**
   * Creates a component from a parsed XML structure.
   * @param  comp        The parsed XML structure of the new component.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  SimpleComponentProxy(final SimpleComponentType comp)
    throws ModelException
  {
    super(comp);
    mKind = comp.getKind();
    mGraph = new GraphProxy(comp.getGraph());
  }


  //#########################################################################
  //# Getters and Setters
  public ComponentKind getKind()
  {
    return mKind;
  }

  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
  }

  public GraphProxy getGraph()
  {
    return mGraph;
  }

  public void setGraph(final GraphProxy graph)
  {
    mGraph = graph;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final SimpleComponentProxy comp = (SimpleComponentProxy) partner;
      return
	getKind() == comp.getKind() &&
	getGraph().equals(comp.getGraph());
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final SimpleComponentProxy comp = (SimpleComponentProxy) partner;
      return
	getKind() == comp.getKind() &&
	getGraph().equalsWithGeometry(comp.getGraph());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final ComponentKind kind = getKind();
    final String kindname = kind.toString();
    final String lowername = kindname.toLowerCase();
    final ExpressionProxy ident = getIdentifier();
    printer.print(lowername);
    printer.print(' ');
    ident.pprint(printer);
    printer.print(' ');
    mGraph.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final SimpleComponentType comp = (SimpleComponentType) element;
    comp.setKind(getKind());
    final GraphType graph = getGraph().toGraphType();
    comp.setGraph(graph);
  }


  //#########################################################################
  //# Data Members
  private ComponentKind mKind;
  private GraphProxy mGraph;

}
