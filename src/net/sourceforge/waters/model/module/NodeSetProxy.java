//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   NodeSetProxy
//###########################################################################
//# $Id: NodeSetProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.SetProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.NodeListType;


/**
 * <P>An implementation of the
 * {@link net.sourceforge.waters.model.base.IndexedCollectionProxy}
 * interface that can handle the set of nodes of a graph.</P>
 *
 * <P>This implementation supports the hierarchical structure of node
 * sets and ensures that no cyclical dependencies can be created when
 * group nodes are added or modified.</P>
 *
 * @author Robi Malik
 */

class NodeSetProxy
  extends AbstractSet
  implements SetProxy
{

  //#########################################################################
  //# Constructors
  NodeSetProxy()
  {
    mSimpleNodes = new LinkedList();
    mGroupNodes = new LinkedList();
    mNameMap = new HashMap();
    mUnmodifiableNameMap = Collections.unmodifiableMap(mNameMap);
  }

  NodeSetProxy(final Collection input)
    throws CyclicGroupNodeException, DuplicateNameException
  {
    mSimpleNodes = new LinkedList();
    mGroupNodes = new LinkedList();
    mNameMap = new HashMap(input.size());
    mUnmodifiableNameMap = Collections.unmodifiableMap(mNameMap);
    insertAll(input);
  }

  NodeSetProxy(final NodeListType nodelist)
    throws ModelException
  {
    final NodeLookupFactory lookup = new NodeLookupFactory(this);
    final ProxyFactory factory = new NodeProxy.NodeProxyFactory(lookup);
    final List elist = nodelist.getList();
    mSimpleNodes = new LinkedList();
    mGroupNodes = new LinkedList();
    mNameMap = new HashMap(elist.size());
    mUnmodifiableNameMap = Collections.unmodifiableMap(mNameMap);
    init(elist, factory);
  }


  //#########################################################################
  //# Interface java.util.Set
  public boolean add(final Object item)
  {
    final NodeProxy node = (NodeProxy) item;
    return add(node);
  }

  public boolean addAll(final Collection items)
  {
    try {
      return insertAll(items);
    } catch (final ModelException caught) {
      final String msg = caught.getMessage();
      final IllegalArgumentException thrown =
	new IllegalArgumentException(msg);
      thrown.initCause(caught);
      throw thrown;
    }
  }

  public boolean contains(final Object item)
  {
    final NodeProxy node = (NodeProxy) item;
    return contains(node);
  }

  public Iterator iterator()
  {
    return new NodeSetIterator();
  }

  public boolean remove(final Object item)
  {
    final NodeProxy node = (NodeProxy) item;
    return remove(node);
  }

  public int size()
  {
    return mNameMap.size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.IndexedCollectionProxy 
  public void init(final Collection elist, final ProxyFactory factory)
    throws ModelException
  {
    final Iterator iter = elist.iterator();
    while (iter.hasNext()) {
      final ElementType element = (ElementType) iter.next();
      final NodeProxy node = (NodeProxy) factory.createProxy(element);
      insertPreOrderedNode(node);
    }
  }

  public boolean add(final NamedProxy node)
  {
    try {
      return insert(node) == node;
    } catch (final ModelException caught) {
      final String msg = caught.getMessage();
      final IllegalArgumentException thrown =
	new IllegalArgumentException(msg);
      thrown.initCause(caught);
      throw thrown;
    }
  }

  public boolean containsName(final String name)
  {
    return mNameMap.containsKey(name);
  }

  public boolean contains(final NamedProxy node)
  {
    final String name = node.getName();
    final NamedProxy found = get(name);
    return found.equals(node);
  }

  public NamedProxy find(final String name)
    throws NameNotFoundException
  {
    final NamedProxy found = get(name);
    if (found == null) {
      throw createNameNotFound(name);
    }
    return found;
  }

  public NamedProxy get(final String name)
  {
    return (NamedProxy) mNameMap.get(name);
  }

  public NamedProxy insert(final NamedProxy proxy)
    throws CyclicGroupNodeException, DuplicateNameException
  {
    final NodeProxy node = (NodeProxy) proxy;
    return insert(node);
  }

  public boolean insertAll(final Collection input)
    throws CyclicGroupNodeException, DuplicateNameException
  {
    boolean changed = false;
    final Iterator iter = input.iterator();
    final Collection groups = new LinkedList();
    while (iter.hasNext()) {
      final NodeProxy node = (NodeProxy) iter.next();
      final String name = node.getName();
      final NodeProxy found = getNode(name);
      if (found != null) {
	if (!found.equals(node)) {
	  throw createDuplicateName(name);
	}
      } else if (node instanceof SimpleNodeProxy) {
	final SimpleNodeProxy simple = (SimpleNodeProxy) node;
	insertSimpleNode(simple);
	changed = true;
      } else if (node instanceof GroupNodeProxy) {
	groups.add(node);
	changed = true;
      } else {
	throw createBadType(node);
      }
    }
    insertGroupNodes(groups);
    return changed;
  }


  public NamedProxy removeName(final String name)
  {
    final NodeProxy victim = getNode(name);
    if (victim != null) {
      removeContainedNode(victim);
    }
    return victim;
  }

  public boolean remove(final NamedProxy proxy)
  {
    final NodeProxy node = (NodeProxy) proxy;
    return remove(node);
  }

  public Map getMap()
  {
    return mUnmodifiableNameMap;
  }


  //#########################################################################
  //# Specific Access Methods for Nodes
  public NodeProxy getNode(final String name)
  {
    return (NodeProxy) mNameMap.get(name);
  }

  public NodeProxy insert(final NodeProxy node)
    throws CyclicGroupNodeException, DuplicateNameException
  {
    final String name = node.getName();
    final NodeProxy found = getNode(name);
    if (found != null) {
      if (found.equals(node)) {
	return found;
      } else {
	throw createDuplicateName(name);
      }
    }
    if (node instanceof SimpleNodeProxy) {
      final SimpleNodeProxy simple = (SimpleNodeProxy) node;
      insertSimpleNode(simple);
    } else if (node instanceof GroupNodeProxy) {
      try {
	final GroupNodeProxy group = (GroupNodeProxy) node;
	insertGroupNode(group);
      } catch (final CyclicGroupNodeException exception) {
	exception.putOperation("Insertion of '" + name + "'");
	throw exception;
      }
    } else {
      throw createBadType(node);
    }
    return node;
  }

  public boolean remove(final NodeProxy node)
  {
    final String name = node.getName();
    final NodeProxy victim = getNode(name);
    if (victim != null && victim.equals(node)) {
      removeContainedNode(victim);
      return true;
    } else {
      return false;
    }
  }

  void rearrangeGroupNodes()
    throws CyclicGroupNodeException
  {
    final GroupNodeIterator iter = new GroupNodeIterator();
    final Collection newgroups = new LinkedList();
    while (iter.hasNext()) {
      newgroups.add(iter.next());
    }
    mGroupNodes = newgroups;
  }


  //#########################################################################
  //# Comparing
  public boolean equalsWithGeometry(final Object partner)
  {
    if (!(partner instanceof Set)) {
      return false;
    }
    final Set set = (Set) partner;
    if (size() != set.size()) {
      return false;
    }
    final Iterator iter = set.iterator();
    while (iter.hasNext()) {
      final Object item2 = iter.next();
      if (!(item2 instanceof NodeProxy)) {
	return false;
      }
      final NodeProxy node2 = (NodeProxy) item2;
      final String name = node2.getName();
      final NodeProxy node1 = getNode(name);
      if (node1 == null || !node1.equalsWithGeometry(node2)) {
	return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final Iterator iter = iterator();
    if (iter.hasNext()) {
      if (getPPrintName() != null) {
	printer.print(getPPrintName());
	printer.print(' ');
      }
      printer.println('{');
      printer.indentIn();
      while (iter.hasNext()) {
	final NodeProxy node = (NodeProxy) iter.next();
	node.pprintln(printer);
      }
      printer.indentOut();
      printer.println('}');
    }
  }

  public void pprintln(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer);
    printer.println(';');
  }

  protected String getPPrintName()
  {
    return "NODES";
  }


  //#########################################################################
  //# Marshalling
  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final Iterator iter = iterator();
    if (iter.hasNext()) {
      final ElementType container = factory.createContainerElement();
      final List elist = factory.getElementList(container);
      final ElementFactory nextfactory = factory.getNextFactory();
      while (iter.hasNext()) {
	final Proxy proxy = (Proxy) iter.next();
	final ElementType element = proxy.toJAXB(nextfactory);
	elist.add(element);
      }
      return container;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void insertSimpleNode(final SimpleNodeProxy node)
  {
    final String name = node.getName();
    node.joinMap(this);
    mSimpleNodes.add(node);
    mNameMap.put(name, node);
  }

  private void insertGroupNode(final GroupNodeProxy group)
    throws CyclicGroupNodeException
  {
    final Collection groups = Collections.singletonList(group);
    insertGroupNodes(groups);
  }

  private void insertGroupNodes(final Collection groups)
    throws CyclicGroupNodeException
  {
    final Iterator iter1 = groups.iterator();
    while (iter1.hasNext()) {
      final GroupNodeProxy group = (GroupNodeProxy) iter1.next();
      if (group.getMap() != null) {
	throw new IllegalArgumentException
	  ("Trying to add group node '" + group.getName() +
	   " to a second graph!");
      }
    }
    final GroupNodeIterator iter2 = new GroupNodeIterator(groups);
    final Collection newgroups = new LinkedList();
    while (iter2.hasNext()) {
      newgroups.add(iter2.next());
    }
    final Iterator iter3 = groups.iterator();
    while (iter3.hasNext()) {
      final GroupNodeProxy group = (GroupNodeProxy) iter3.next();
      final String name = group.getName();
      group.joinMap(this);
      mNameMap.put(name, group);
    }
    mGroupNodes = newgroups;
  }

  private void insertPreOrderedNode(final NodeProxy node)
    throws DuplicateNameException
  {
    boolean issimple;
    final String name = node.getName();
    if (mNameMap.containsKey(name)) {
      throw createDuplicateName(name);
    } else if (node instanceof SimpleNodeProxy) {
      issimple = true;
    } else if (node instanceof GroupNodeProxy) {
      issimple = false;
    } else {
      throw createBadType(node);
    }
    node.joinMap(this);
    if (issimple) {
      mSimpleNodes.add(node);
    } else {
      mGroupNodes.add(node);
    }
    mNameMap.put(name, node);
  }

  private void removeContainedNode(final NodeProxy victim)
  {
    if (victim instanceof SimpleNodeProxy) {
      mSimpleNodes.remove(victim);
    } else if (victim instanceof GroupNodeProxy) {
      mGroupNodes.remove(victim);
    } else {
      throw createBadType(victim);
    }
    mNameMap.remove(victim.getName());
    victim.leaveMap();
  }


  //#########################################################################
  //# Exceptions
  private NameNotFoundException createNameNotFound(final String name)
  {
    return new NameNotFoundException
      ("Graph does not contain any node named '" + name + "'!");
  }

  private DuplicateNameException createDuplicateName(final String name)
  {
    return new DuplicateNameException
      ("Graph already contains a node named '" + name + "'!");
  }

  private ClassCastException createBadType(final NodeProxy node)
  {
    return new ClassCastException
      ("Unknown node class " + node.getClass().getName() + "!");
  }

  private ClassCastException createBadType(final Object item)
  {
    return new ClassCastException
      ("Can't add object of class " + item.getClass().getName() +
       " to node set!");
  }


  //#########################################################################
  //# Local Class NodeSetIterator
  private class NodeSetIterator implements Iterator
  {

    //#######################################################################
    //# Constructors
    private NodeSetIterator()
    {
      mSimplePart = true;
      mIterator = mSimpleNodes.iterator();
      mVictim = null;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      if (mIterator.hasNext()) {
	return true;
      } else if (mSimplePart) {
	mSimplePart = false;
	mIterator = mGroupNodes.iterator();
	return mIterator.hasNext();
      } else {
	return false;
      }
    }

    public Object next()
    {
      if (!mIterator.hasNext() && mSimplePart) {
	mSimplePart = false;
	mIterator = mGroupNodes.iterator();
      }
      mVictim = mIterator.next();
      return mVictim;
    }

    public void remove()
    {
      final NodeProxy node = (NodeProxy) mVictim;
      mIterator.remove();
      mNameMap.remove(node.getName());
      node.leaveMap();
      mVictim = null;
    }

    //#######################################################################
    //# Data Members
    private Iterator mIterator;
    private boolean mSimplePart;
    private Object mVictim;

  }


  //#########################################################################
  //# Local Class GroupNodeIterator
  private class GroupNodeIterator
  {

    //#######################################################################
    //# Constructors
    private GroupNodeIterator()
    {
      mUnvisited = new LinkedList(mGroupNodes);
      mVisited = new IdentityHashMap(mGroupNodes.size());
      mIterator = mUnvisited.iterator();
      mNextNode = null;
      mGotNode = false;
    }

    private GroupNodeIterator(final Collection additional)
    {
      mUnvisited = new LinkedList(mGroupNodes);
      mUnvisited.addAll(additional);
      mVisited = new IdentityHashMap(mGroupNodes.size());
      mIterator = mUnvisited.iterator();
      mNextNode = null;
      mGotNode = false;
    }

 
    //#######################################################################
    //# Iteration
    private boolean hasNext()
      throws CyclicGroupNodeException
    {
      advance();
      return mNextNode != null;
    }

    private GroupNodeProxy next()
      throws CyclicGroupNodeException
    {
      advance();
      final GroupNodeProxy result = mNextNode;
      if (result != null) {
	mNextNode = null;
	return result;
      } else {
	throw new NoSuchElementException
	  ("End of group node iteration reached!");
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
      throws CyclicGroupNodeException
    {
      while (mNextNode == null) {
	if (!mIterator.hasNext()) {
	  if (mUnvisited.isEmpty()) {
	    return;
	  } else if (mGotNode) {
	    mGotNode = false;
	    mIterator = mUnvisited.iterator();
	  } else {
	    final Iterator iter = mUnvisited.iterator();
	    final GroupNodeProxy cyclic = (GroupNodeProxy) iter.next();
	    throw new CyclicGroupNodeException
	      ("Presence of '" + cyclic.getName() + "'");
	  }
	}
	final GroupNodeProxy node = (GroupNodeProxy) mIterator.next();
	if (childrenVisited(node)) {
	  mVisited.put(node, node);
	  mIterator.remove();
	  mNextNode = node;
	  mGotNode = true;
	}
      }
    }

    private boolean childrenVisited(final GroupNodeProxy node)
    {
      final Iterator iter = node.getImmediateChildNodeIterator();
      while (iter.hasNext()) {
	final Object child = iter.next();
	if (child instanceof GroupNodeProxy && !mVisited.containsKey(child)) {
	  return false;
	}
      }
      return true;
    }


    //#######################################################################
    //# Data Members
    private final Collection mUnvisited;
    private final Map mVisited;
    private Iterator mIterator;
    private GroupNodeProxy mNextNode;
    private boolean mGotNode;

  }

  //#########################################################################
  //# Data Members
  private Collection mSimpleNodes;
  private Collection mGroupNodes;
  private Map mNameMap;
  private Map mUnmodifiableNameMap;

}
