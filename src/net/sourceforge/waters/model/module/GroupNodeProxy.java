//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   GroupNodeProxy
//###########################################################################
//# $Id: GroupNodeProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.NoSuchElementException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.HashSetProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.SetProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.BoxGeometryType;
import net.sourceforge.waters.xsd.module.NodeRefType;
import net.sourceforge.waters.xsd.module.GroupNodeType;


/**
 * <P>A hierarchical node that can contain other nodes.</P>
 *
 * <P>Group nodes can be used to combine several nodes in a graph into a
 * single node, in order to reduce the number of edges in certain cases.
 * When translating a graph into an automaton, each edge that starts at a
 * group nodes is translated into transitions originating from each simple
 * node contained in that group node.</P>
 *
 * <P>In a nondeterministic graph, edges can also end in a group node.
 * In this case, the translated automaton contains transitions into each
 * simple node contained in that group node.</P>
 *
 * <P>Graphically, group nodes are represented as boxes. All nodes contained
 * in the area of the rectangle are considered as belonging to the group
 * node. Group nodes can overlap and be contained within each other.</P>
 *
 * <P>This group node class is only concerned with the logical grouping
 * structure. Its methods enable the user to access and change the set of
 * simple nodes or group nodes that are considered as immediately
 * contained. But it does not ensure that this information is consistent
 * with the geometric information.</P>
 *
 * @author Robi Malik
 */

public class GroupNodeProxy extends NodeProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a group node.
   * This constructor creates an empty group node that has no associated
   * geometry information.
   * @param  name        The name to be given to the new node.
   */
  public GroupNodeProxy(final String name)
  {
    super(name);
    mNodes = new HashSetProxy();
    mUnmodifiableNodes = Collections.unmodifiableSet(mNodes);
  }

  /**
   * Creates a group node.
   * This constructor creates a group node that has no associated geometry
   * information.
   * @param  name        The name to be given to the new node.
   * @param  children    The initial set of immediate successors of the new
   *                     group node.
   * @throws DuplicateNameException to indicate that the given collection
   *                     contains different nodes with the same name.
   */
  public GroupNodeProxy(final String name, final Collection children)
    throws DuplicateNameException
  {
    super(name);
    mNodes = new HashSetProxy(children);
    mUnmodifiableNodes = Collections.unmodifiableSet(mNodes);
  }

  /**
   * Creates a copy of a group node.
   * This constructor creates a copy of the given group node that contains
   * the same child node objects as the original.
   * @param  partner     The object to be copied from.
   */
  public GroupNodeProxy(final GroupNodeProxy partner)
  {
    super(partner);
    try {
      mNodes = new HashSetProxy(partner.mNodes);
      mUnmodifiableNodes = Collections.unmodifiableSet(mNodes);
      if (partner.mGeometry != null) {
	mGeometry = new BoxGeometryProxy(partner.mGeometry);
      }
    } catch (final DuplicateNameException caught) {
      final String msg = caught.getMessage();
      final IllegalArgumentException thrown =
	new IllegalArgumentException(msg);
      thrown.initCause(caught);
      throw thrown;
    }
  }

  /**
   * Creates a group node from a parsed XML structure.
   * @param  state       The parsed XML structure of the new node.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  GroupNodeProxy(final GroupNodeType state,
		 final NodeLookupFactory lookup)
    throws ModelException
  {
    super(state);
    final ProxyFactory factory = new NodeProxyFactory(lookup);
    mNodes = new HashSetProxy(state, factory);
    mUnmodifiableNodes = Collections.unmodifiableSet(mNodes);
    final BoxGeometryType geo = state.getBoxGeometry();
    if (geo != null) {
      mGeometry = new BoxGeometryProxy(geo);
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this group node.
   * This method creates a copy of this group node that contains
   * the same child node objects as this group node.
   */
  public Object clone()
  {
    return new GroupNodeProxy(this);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the set of immediate child nodes of this group node.
   * This method returns the set of simple nodes or group nodes
   * that are directly contained in this group node.
   * @return An unmodifiable set of nodes.
   *         Each element is of type {@link NodeProxy}.
   */
  public Set getImmediateChildNodes()
  {
    return mUnmodifiableNodes;
  }

  /**
   * Changes the set of immediate child nodes of this group node.
   * This method replaces the set of immediate child nodes for this group
   * node by the set of nodes contained in the given collection.
   * This operation may fail for various reasons-in case of failure,
   * the group node is left unchanged.
   * @param  children    The new set of immediate child nodes.
   *                     Each element should be of type {@link NodeProxy}.
   * @throws CyclicGroupNodeException to indicate that the proposed change
   *                     would cause a cyclic group node structure in the
   *                     graph containing this group node.
   * @throws DuplicateNameException to indicate that the given collection
   *                     contains different nodes with the same name.
   */
  public void setImmediateChildNodes(final Collection children)
    throws CyclicGroupNodeException, DuplicateNameException
  {
    try {
      final SetProxy newnodes = new HashSetProxy(children);
      final NodeSetProxy owner = (NodeSetProxy) getMap();
      mUnmodifiableNodes = Collections.unmodifiableSet(newnodes);
      if (owner != null) {
	owner.rearrangeGroupNodes();
      }
      mNodes = newnodes;
    } catch (final CyclicGroupNodeException exception) {
      mUnmodifiableNodes = Collections.unmodifiableSet(mNodes);
      exception.putOperation("Changing children of '" + getName() + "'");
      throw exception;
    }    
  }

  public Iterator getImmediateChildNodeIterator()
  {
    return mUnmodifiableNodes.iterator();
  }

  public Iterator getChildNodeIterator()
  {
    return new ChildNodeIterator();
  }

  public Iterator getSimpleChildNodeIterator()
  {
    return new SimpleChildNodeIterator();
  }

  /**
   * Gets the geometric information of this node.
   * @return A {@link BoxGeometryProxy} identifying the position and size
   *         of the box representing this group node in a graph.
   */
  public BoxGeometryProxy getGeometry()
  {
    return mGeometry;
  }

  /**
   * Sets the geometric information of this node.
   * @param  geo         A {@link PointGeometryProxy} identifying the position
   *                     and size of the box representing this group node in
   *                     a graph.
   */
  public void setGeometry(final BoxGeometryProxy geo)
  {
    mGeometry = geo;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GroupNodeProxy state = (GroupNodeProxy) partner;
      return mNodes.equals(state.mNodes);
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equals(partner)) {
      final GroupNodeProxy state = (GroupNodeProxy) partner;
      return
	mNodes.equalsWithGeometry(state.mNodes) &&
	GeometryProxy.equalGeometry(mGeometry, state.mGeometry);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print("superstate ");
    printer.print(getName());
    printer.print(" = ");
    mNodes.pprint(printer);
    super.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof GroupNodeType) {
      final GroupNodeType state = (GroupNodeType) element;
      final ElementFactory factory = new NodeRefElementFactory(state);
      mNodes.toJAXB(factory);
      if (mGeometry != null) {
	final BoxGeometryType geo = mGeometry.toBoxGeometryType();
	state.setBoxGeometry(geo);
      }
    }
  }


  //#########################################################################
  //# Local Class ChildNodeIterator
  /**
   * An iterator over all child nodes of a group node.
   * This iterator produces all descendants of a group node,
   * i.e., both subgroups and simple nodes, in prefix order.
   * The group node from which the iterator is invoked
   * is returned as the first element.
   */
  private class ChildNodeIterator implements Iterator
  {

    //#######################################################################
    //# Constructor
    ChildNodeIterator()
    {
      mListIterator = null;
      mInnerIterator = null;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mListIterator == null || mInnerIterator.hasNext();
    }

    public Object next()
    {
      if (mListIterator == null) {
	mListIterator = mNodes.iterator();
	advance();
	return GroupNodeProxy.this;
      } else if (mInnerIterator.hasNext()) {
	final Object result = mInnerIterator.next();
	advance();
	return result;
      } else {
	throw new NoSuchElementException
	  ("No more events in superstate's state iteration!");
      }
    }
	
    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from superstate's state iteration!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mInnerIterator == null || !mInnerIterator.hasNext()) {
	if (mListIterator.hasNext()) {
	  final NodeProxy state = (NodeProxy) mListIterator.next();
	  mInnerIterator = state.getChildNodeIterator();
	} else {
	  break;
	}
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * The top-level iterator, which iterates over the child nodes of the 
     * originating node. Initially, this is <CODE>null</CODE> to indicate
     * the originating node itself is to be returned.
     */
    private Iterator mListIterator;
    private Iterator mInnerIterator;

  }


  //#########################################################################
  //# Local Class SimpleChildNodeIterator
  /**
   * An iterator over all simple child nodes of a group node.
   * This iterator produces all descendants of a group node that are not
   * groups nodes.
   */
  private class SimpleChildNodeIterator implements Iterator
  {

    //#######################################################################
    //# Constructor
    SimpleChildNodeIterator()
    {
      mListIterator = mNodes.iterator();
      mInnerIterator = null;
      advance();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mListIterator != null;
    }

    public Object next()
    {
      if (mListIterator != null) {
	final Object result = mInnerIterator.next();
	advance();
	return result;
      } else {
	throw new NoSuchElementException
	  ("No more events in superstate's simple state iteration!");
      }
    }
	
    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from superstate's simple state iteration!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mInnerIterator == null || !mInnerIterator.hasNext()) {
	if (mListIterator.hasNext()) {
	  final NodeProxy state = (NodeProxy) mListIterator.next();
	  mInnerIterator = state.getSimpleChildNodeIterator();
	} else {
	  mListIterator = null;
	  mInnerIterator = null;
	  return;
	}
      }
    }

    //#######################################################################
    //# Data Members
    private Iterator mListIterator;
    private Iterator mInnerIterator;

  }


  //#########################################################################
  //# Local Class NodeProxyFactory
  private static class NodeProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Constructors
    NodeProxyFactory(final NodeLookupFactory lookup)
    {
      mLookup = lookup;
    }


    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final NodeRefType stateref = (NodeRefType) element;
      final String name = stateref.getName();
      return mLookup.findNode(name);
    }

    public List getList(final ElementType parent)
    {
      final GroupNodeType superstate = (GroupNodeType) parent;
      return superstate.getNodes();
    }


    //#######################################################################
    //# Data Members
    private final NodeLookupFactory mLookup;

  }


  //#########################################################################
  //# Local Class NodeRefElementFactory
  private static class NodeRefElementFactory
    extends ModuleElementFactory
  {

    //#######################################################################
    //# Constructor
    NodeRefElementFactory(final GroupNodeType state)
    {
      mGroupNode = state;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createNodeRef();
    }

    public ElementType createContainerElement()
    {
      return mGroupNode;
    }

    public List getElementList(final ElementType container)
    {
      final GroupNodeType list = (GroupNodeType) container;
      return list.getNodes();
    }

    //#######################################################################
    //# Data Members
    private final GroupNodeType mGroupNode;

  }


  //#########################################################################
  //# Data Members
  private SetProxy mNodes;
  private Set mUnmodifiableNodes;
  private BoxGeometryProxy mGeometry;

}
