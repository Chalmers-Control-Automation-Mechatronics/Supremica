//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   NodeProxy
//###########################################################################
//# $Id: NodeProxy.java,v 1.2 2005-06-02 12:18:03 flordal Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.UniqueElementProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.SimpleNodeType;
import net.sourceforge.waters.xsd.module.NodeListType;
import net.sourceforge.waters.xsd.module.NodeType;
import net.sourceforge.waters.xsd.module.GroupNodeType;


/**
 * <P>The abstract base class for all nodes.</P>
 *
 * <P>This class serves as a common base for simple nodes ({@link
 * SimpleNodeProxy}) and group nodes ({@link GroupNodeProxy}) and
 * implements some functionality common to both classes.</P>
 *
 * <P>All nodes can be associated with <I>propositions</I>. Propositions
 * are a particular type of event used to define properties of nodes.  The
 * common application to define <I>marked</I> or <I>terminal</I> states is
 * achieved by associating each node to be marked with a particular
 * proposition, e.g., <CODE>:omega</CODE>. The general node structure
 * supports a list of proposition in order to facilitate several marking
 * conditions to check a model for mutual nonblocking conditions or to
 * perform CTL model checking.</P>
 *
 * @author Robi Malik
 */

public abstract class NodeProxy extends UniqueElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new node.
   * @param  name        The name of the new node.
   */
  NodeProxy(final String name)
  {
    super(name);
    mPropositionListProxy = new EventListProxy();
  }

  /**
   * Creates a copy of a node.
   * This constructor creates a copy of the given group node that contains
   * the same proposition objects as the original.
   * @param  partner     The object to be copied from.
   */
  NodeProxy(final NodeProxy partner)
  {
    super(partner);
    mPropositionListProxy = new EventListProxy(partner.mPropositionListProxy);
  }

  /**
   * Creates a node from a parsed XML structure.
   * @param  state       The parsed XML structure representing the
   *                     node to be created.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
   NodeProxy(final NodeType state)
    throws ModelException
  {
    super(state);
    mPropositionListProxy = new EventListProxy(state.getPropositions());
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final NodeProxy state = (NodeProxy) partner;
      return getPropositions().equals(state.getPropositions());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of propositions of this node.
   * @return An event list that defines the proposition events for this
   *         node.
   */
  public EventListProxy getPropositions()
  {
    return mPropositionListProxy;
  }

  /**
   * Gets an iterator over the immediate child nodes of this node.
   * The iterator returned by this method produces all nodes
   * directly contained in a group node. For a simple node,
   * it is empty.
   */
  public abstract Iterator getImmediateChildNodeIterator();

  /**
   * Gets an iterator over all child nodes of this node.  
   * The iterator returned by this method produces all simple or group
   * nodes directly or indirectly contained in this node, including the
   * node itself.
   */
  public abstract Iterator getChildNodeIterator();

  /**
   * Gets an iterator over all simple child nodes of this node.  
   * The iterator returned by this method produces all simple nodes
   * contained directly or indirectly in this node, possibly including the
   * node itself.
   */
  public abstract Iterator getSimpleChildNodeIterator();


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (getPropositions().size() > 0) {
      printer.print(' ');
      mPropositionListProxy.pprint(printer);
    }
   }



  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof NodeType) {
      final NodeType state = (NodeType) element;
      final EventListType list = mPropositionListProxy.toEventListType();
      state.setPropositions(list);
    }
  }


  //#########################################################################
  //# Local Class NodeProxyFactory
  static class NodeProxyFactory implements ProxyFactory
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
      if (element instanceof SimpleNodeType) {
	final SimpleNodeType state = (SimpleNodeType) element;
	return new SimpleNodeProxy(state);
      } else if (element instanceof GroupNodeType) {
	final GroupNodeType state = (GroupNodeType) element;
	return new GroupNodeProxy(state, mLookup);
      } else {
	throw new ClassCastException
	  ("Can't create state proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(final ElementType parent)
    {
      final NodeListType list = (NodeListType) parent;
      return list.getList();
    }


    //#######################################################################
    //# Data Members
    private final NodeLookupFactory mLookup;

  }


  //#########################################################################
  //# Local Class NodeElementFactory
  static class NodeElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof SimpleNodeProxy) {
	return getFactory().createSimpleNode();
      } else if (proxy instanceof GroupNodeProxy) {
	return getFactory().createGroupNode();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " as state!");
      }
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createNodeList();
    }

    public List getElementList(final ElementType container)
    {
      final NodeListType list = (NodeListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Data Members
  private final EventListProxy mPropositionListProxy;

}
