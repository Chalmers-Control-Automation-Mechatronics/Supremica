//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   UniqueElementProxy
//###########################################################################
//# $Id: UniqueElementProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


/**
 * <P>The common base class of all Waters elements that are uniquely identified
 * by a name.</P>
 *
 * <P>Some elements in Waters have got a name that uniquely identifies them
 * within a context. The event declarations of a module, or the states of
 * an automaton are examples of such elements. In these cases, there exists
 * a <I>symbol table</I>, by which the element can be retrieved by giving
 * its name.</P>
 *
 * <P>This class supports objects that are identified, hashed, and
 * compared by their name. The symbol table is registered with these
 * objects, so it can be updated if the name is changed by the
 * {@link #setName(String) setName()} method.</P>
 *
 * @see ImmutableNamedProxy
 * @see MutableNamedProxy
 *
 * @author Robi Malik
 */

public abstract class UniqueElementProxy
  extends ElementProxy
  implements NamedProxy, Comparable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a unique element with a given name.
   * @param  name        The name of the new element.
   */
  protected UniqueElementProxy(final String name)
  {
    mName = name;
  }

  /**
   * Creates a unique element from a parsed XML structure.
   * @param  element     The parsed XML structure representing the new
   *                     element.
   */
  protected UniqueElementProxy(final NamedType element)
  {
    mName = element.getName();
  }

  /**
   * Creates a copy of a unique element.
   * The copy is assumed to belong to no symbol table.
   * @param  partner     The object to be copied from.
   */
  protected UniqueElementProxy(final UniqueElementProxy partner)
  {
    super(partner);
    mName = partner.mName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mName;
  }

  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final UniqueElementProxy named = (UniqueElementProxy) partner;
      return mName.equals(named.mName);
    } else {
      return false;
    }
  }

  /**
   * Returns a hash code value for this element.
   * This method uses the element's name to calculate the hash code.
   */
  public int hashCode()
  {
    return mName.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  /**
   * Compares this named element with another.
   * This method compares elements only based on their names.
   */
  public int compareTo(final Object partner)
  {
    final NamedProxy named = (NamedProxy) partner;
    final String name = named.getName();
    final int result = mName.compareToIgnoreCase(name);
    if (result != 0) {
      return result;
    }
    return mName.compareTo(name);
  }


  //#########################################################################
  //# Changing the Name
  /**
   * Changes the name of this element.
   * This method sets the name of this element to the given value,
   * and reinserts the element into its symbol table. This ensures
   * that the consistency of this one symbol table. If the element
   * has been added to other hash tables or sorted maps, it is the
   * user's responsibility to change them.
   * @param  name        The new name.
   * @throws DuplicateNameException to indicate that the name could not
   *                     be changed because the symbol table owning the
   *                     item already contains another element with the
   *                     same name.
   */
  public void setName(final String name)
    throws DuplicateNameException
  {
    final IndexedCollectionProxy map = mMap;
    if (map == null) {
      mName = name;
    } else if (map.containsName(name)) {
      throw new DuplicateNameException
	("Name '" + name + "' is already taken!");
    } else {
      try {
	map.remove(this);
	mName = name;
	map.insert(this);
      } catch (final ModelException exception) {
	throw new UnexpectedWatersException(exception);
      }
    }
  }


  //#########################################################################
  //# Setting the Map Backpointer
  /**
   * Gets the symbol table containing this element.
   * @return The map which is used to uniquely identify this element
   *         based on its name, or <CODE>null</CODE> if the element has
   *         not yet been inserted into any map.
   */
  public IndexedCollectionProxy getMap()
  {
    return mMap;
  }

  /**
   * Records the symbol table containing this element.
   * This method is called by symbol table implementations after
   * adding the element to its symbol table. It is only intended to
   * be used internally in this way.
   * @param  map   The new symbol table containing this element.
   * @throws IllegalStateException to indicate that this element already
   *               belongs to another symbol table.
   */
  public void joinMap(final IndexedCollectionProxy map)
  {
    if (mMap == null) {
      mMap = map;
    } else {
      throw new IllegalStateException
	("Trying to add item '" + getName() + "' of class " +
	 getClass().getName() + " to a second primary map!");
    }
  }

  /**
   * Clears the symbol table containing this element.
   * This method is called by symbol table implementations prior to
   * removing the element to its symbol table. It is only intended to
   * be used internally in this way.
   */
  public void leaveMap()
  {
    mMap = null;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final NamedType named = (NamedType) element;
    named.setName(mName);
  }


  //#########################################################################
  //# Data Members
  /**
   * The name of this element.
   */
  private String mName;
  /**
   * The symbol table containing this item.
   */
  private IndexedCollectionProxy mMap = null;

}
