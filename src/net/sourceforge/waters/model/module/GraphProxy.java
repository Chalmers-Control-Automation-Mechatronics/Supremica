//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   GraphProxy
//###########################################################################
//# $Id: GraphProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.IndexedArrayListProxy;
import net.sourceforge.waters.model.base.IndexedListProxy;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.TopLevelListProxy;
import net.sourceforge.waters.model.base.SetProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EdgeListType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.GraphType;
import net.sourceforge.waters.xsd.module.NodeListType;


/**
 * A graph representing a finite-state machine.
 *
 * @author Robi Malik
 */

public class GraphProxy extends ElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty deterministic graph.
   */
  public GraphProxy()
  {
    this(true);
  }

  /**
   * Creates an empty graph.
   * @param  deterministic A flag, indicating whether the graph should be
   *                       a deterministic or a nondeterministic automaton.
   *                       If <CODE>true</CODE>, the graph will be
   *                       deterministic.
   */
  public GraphProxy(final boolean deterministic)
  {
    mBlockedEvents = new BlockedEventListProxy();
    mNodes = new NodeSetProxy();
    mEdges = new EdgeListProxy();
    mDeterministic = deterministic;
  }

  /**
   * Creates a graph from a parsed XML structure.
   * @param  graph       The parsed XML structure of the new graph.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  GraphProxy(final GraphType graph)
    throws ModelException
  {
    mBlockedEvents = new BlockedEventListProxy(graph);
    mNodes = new NodeSetProxy(graph.getNodeList());
    final NodeLookupFactory factory = new NodeLookupFactory(mNodes);
    mEdges = new EdgeListProxy(graph, factory);
    mDeterministic = graph.isDeterministic();
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the determinism status of this graph.
   * A graph can be marked as <I>deterministic</I> or <I>nondeterministic</I>.
   * In a deterministic graph, there can be only one initial node,
   * and nodes can have at most one outgoing edge for each event.
   * These conditions can only be checked by a compiler with full accuracy,
   * but editors can also use this flag to perform some preliminary checks.
   * @return <CODE>true</CODE> if the graph is to produce a deterministic
   *         finite-state machine, <CODE>false</CODE> otherwise.
   */
  public boolean isDeterministic()
  {
    return mDeterministic;
  }

  /**
   * Sets the determinism status of this graph.
   * @param  deterministic <CODE>true</CODE> if the graph is to produce a
   *                       deterministic finite-state machine,
   *                       <CODE>false</CODE> otherwise.
   * @see #isDeterministic()
   */
  public void setDeterministic(final boolean deterministic)
  {
    mDeterministic = deterministic;
  }

  /**
   * Gets the list of blocked events of this graph.
   * The blocked event list of an automaton defines a list of additional
   * events which the automaton synchronises on, but which are not
   * necessarily enabled in any of its states. This makes it possible to
   * specify that certain events globally disabled in any system where an
   * automaton is used.
   * @return The (modifiable) event list.
   *         Each element is of type
   *         {@link net.sourceforge.waters.model.expr.IdentifierProxy}.
   */
  public List getBlockedEvents()
  {
    return mBlockedEvents;
  }

  /**
   * Gets the list of nodes of this graph.
   * @return The (modifiable) node set.
   *         Each element is of type {@link NodeProxy}.
   */
  public SetProxy getNodes()
  {
    return mNodes;
  }

  /**
   * Gets the list of edges of this graph.
   * @return The (modifiable) node list.
   *         Each element is of type {@link EdgeProxy}.
   */
  public List getEdges()
  {
    return mEdges;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GraphProxy graph = (GraphProxy) partner;
      return
	mBlockedEvents.equals(graph.mBlockedEvents) &&
	mNodes.equals(graph.mNodes) &&
	mEdges.equals(graph.mEdges);
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equals(partner)) {
      final GraphProxy graph = (GraphProxy) partner;
      return
	mBlockedEvents.equals(graph.mBlockedEvents) &&
	mNodes.equalsWithGeometry(graph.mNodes) &&
	mEdges.equalsWithGeometry(graph.mEdges);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Accessing the Node List
  /**
   * Finds a node with given name.
   * @param  name   The name of the node to be found.
   * @return The corresponding node object.
   * @throws NameNotFoundException to indicate that the graph does not
   *                contain any node with the given name.
   */
  public NodeProxy findNode(final String name)
    throws NameNotFoundException
  {
    return (NodeProxy) mNodes.find(name);
  }

  /**
   * Tries to find a node with given name.
   * @param  name   The name of the node to be found.
   * @return The corresponding node object, or <CODE>null</CODE> if none
   *         was found.
   */
  public NodeProxy getNode(final String name)
  {
    return (NodeProxy) mNodes.get(name);
  }  


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.println('{');
    printer.indentIn();
    if (mBlockedEvents.size() > 0) {
      mBlockedEvents.pprint(printer);
      printer.println();
    }
    mNodes.pprint(printer);
    mEdges.pprint(printer);
    printer.indentOut();
    printer.print('}');
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final GraphType graph = (GraphType) element;
    final EventListType blockedlist = mBlockedEvents.toEventListType();
    graph.setBlockedEvents(blockedlist);
    final ElementFactory statefactory =
      new NodeProxy.NodeElementFactory();
    final NodeListType statelist =
      (NodeListType) mNodes.toJAXB(statefactory);
    graph.setNodeList(statelist);
    final ElementFactory edgefactory =
      new EdgeProxy.EdgeElementFactory();
    final EdgeListType edgelist =
      (EdgeListType) mEdges.toJAXB(edgefactory);
    graph.setEdgeList(edgelist);
    graph.setDeterministic(mDeterministic);
  }

  /**
   * Creates a JAXB element representing the contents of this graph.
   * This method is used internally for marshalling and should not be
   * called directly.
   */
  GraphType toGraphType()
    throws JAXBException
  {
    final ElementFactory factory = new GraphElementFactory();
    return (GraphType) toJAXB(factory);
  }


  //#########################################################################
  //# Local Class GraphElementFactory
  static class GraphElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createGraph();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("Graph has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("Graph has no containing list!");
    }

  }


  //#########################################################################
  //# Local Class BlockedEventListProxy
  private static class BlockedEventListProxy extends EventListProxy {

    //#######################################################################
    //# Constructors
    private BlockedEventListProxy()
    {
    }

    private BlockedEventListProxy(final GraphType graph)
      throws ModelException
    {
      super(graph.getBlockedEvents());
    }

    //#######################################################################
    //# Overrides from abstract class ListProxy
    protected boolean getShortPrint()
    {
      return false;
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "BLOCKED";
    }

  }


  //#########################################################################
  //# Local Class EdgeListProxy
  private static class EdgeListProxy extends TopLevelListProxy {

    //#######################################################################
    //# Constructors
    private EdgeListProxy()
    {
    }

    private EdgeListProxy(final GraphType graph,
			  final NodeLookupFactory factory)
      throws ModelException
    {
      super(graph.getEdgeList(), new EdgeProxy.EdgeProxyFactory(factory));
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "EDGES";
    }
  
  }


  //#########################################################################
  //# Data Members
  private final EventListProxy mBlockedEvents;
  private final SetProxy mNodes;
  private final ListProxy mEdges;
  private boolean mDeterministic;

}
