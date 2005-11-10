//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   Element
//###########################################################################
//# $Id: Element.java,v 1.3 2005-11-10 21:54:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.base;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * <P>The common base class for all Waters elements in the <I>plain</I>
 * implementation.</P>
 *
 * <P>This is the abstract base class of all non-aggregrate Waters elements
 * in the <I>plain</I> implementation. It provides the basic functionality
 * of an immutable object.</P>
 * 
 * @author Robi Malik
 */

public abstract class Element
  implements Proxy, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected Element()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected Element(final Proxy partner)
  {
  }


  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this element.
   * Immutable objects of the plain implementation are cloned by shallow
   * copying. All contained objects are shared between an element and its
   * clones.
   */
  public Element clone()
  {
    try {
      return (Element) super.clone();
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Equals and Hashcode
  /**
   * Checks whether two elements are equal.
   * This method implements content-based equality, i.e., two elements
   * will be equal if their contents are the same. This method can
   * be slow for large structures and therefore should be used with
   * care.
   * @see #equalsWithGeometry(Object) equalsWithGeometry()
   */
  public boolean equals(final Object partner)
  {
    return partner != null && getClass() == partner.getClass();
  }

  /**
   * Checks whether two elements are equal and have the same geometry
   * information. This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same. While the
   * standard {@link #equals(Object) equals()} method only considers structural
   * contents, this method also takes the layout information of graphical
   * objects such as nodes and edges into account. This method is very slow
   * for large structures and so far is only used for testing purposes.
   */
  public boolean equalsWithGeometry(final Object partner)
  {
    return equals(partner);
  }

  /**
   * Returns a hash code value for this element.
   * This is an implementation of the hashCode() function as documented in
   * the Java API. Care has been taken to satisfy the general hashCode()
   * contract, so the hash code does only depend on the immutable members
   * of an element. As a consequence, the method is not always as effective
   * as might be desired.
   */
  public int hashCode()
  {
    return getClass().hashCode();
  }


  //#########################################################################
  //# Printing
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }

}
