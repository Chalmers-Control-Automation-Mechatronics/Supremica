//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ImmutableNamedProxy
//###########################################################################
//# $Id: ImmutableNamedProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


/**
 * <P>An immutable implementation of the {@link NamedProxy} interface.</P>
 *
 * <P>This abstract base class can be used to implement elements with an
 * immutable name attribute of type {@link String}. Immutability means that
 * the name cannot be changed once an element is constructed. Therefore,
 * the name attribute can be used to calculate hash codes ordering without
 * affecting any hash tables or ordered lists.</P>
 *
 * @see MutableNamedProxy
 * @see UniqueElementProxy
 *
 * @author Robi Malik
 */

public abstract class ImmutableNamedProxy
  extends ElementProxy
  implements NamedProxy, Comparable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named element.
   * @param  name        The name of the new element.
   */
  protected ImmutableNamedProxy(final String name)
  {
    mName = name;
  }

  /**
   * Creates a named element from a parsed XML structure.
   * @param  element     The parsed XML structure representing the
   *                     element to be created.
   */
  protected ImmutableNamedProxy(final NamedType element)
  {
    mName = element.getName();
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected ImmutableNamedProxy(final ImmutableNamedProxy partner)
  {
    super(partner);
    mName = partner.mName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  /**
   * Returns the name of this element.
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Checks whether two elements have the same name.
   */
  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ImmutableNamedProxy named = (ImmutableNamedProxy) partner;
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
  private final String mName;

}
