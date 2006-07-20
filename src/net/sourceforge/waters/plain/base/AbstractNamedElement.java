//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   AbstractNamedElement
//###########################################################################
//# $Id: AbstractNamedElement.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.base;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>An abstract implementation of the {@link NamedProxy} interface.</P>
 *
 * <P>This is an abstract skeleton used to implement elements with an
 * immutable name attribute of type {@link String}. Immutability means that
 * the name cannot be changed once an element is constructed. Therefore,
 * the name attribute can be used to calculate hash codes and ordering
 * without affecting any hash tables or ordered lists. While not actually
 * providing the name member, this abstract base class uses the {@link
 * #getName()} method to provide implementations for the {@link
 * #refequals(NamedProxy) refequals()}, {@link #equalsByContents(Proxy)
 * equals()}, {@link #hashCodeByContents()}, and {@link
 * #compareTo(NamedProxy) compareTo()} methods.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractNamedElement
  extends Element
  implements NamedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named element.
   */
  protected AbstractNamedElement()
  {
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected AbstractNamedElement(final NamedProxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Cloning
  public AbstractNamedElement clone()
  {
    return (AbstractNamedElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Equals and Hashcode
  /**
   * Checks whether two elements are equal. This method implements
   * content-based equality, i.e., two elements will be equal if their
   * contents are the same. In addition to calling the superclass
   * method, this method compares the names of the two elements.
   */
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final AbstractNamedElement named = (AbstractNamedElement) partner;
      return getName().equals(named.getName());
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return getName().hashCode();
  }

  /**
   * Computes a hash code based on this object's contents.
   * This method uses the element's name to calculate the hash code.
   */
  public int hashCodeByContents()
  {
    return super.hashCodeByContents() + 5 * getName().hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  /**
   * Compares this named element with another.
   * This method compares elements only based on their names, ignoring case.
   */
  public int compareTo(final NamedProxy partner)
  {
    final String name = getName();
    final String partnername = partner.getName();
    final int result = name.compareToIgnoreCase(partnername);
    if (result != 0) {
      return result;
    }
    return name.compareTo(partnername);
  }

}
