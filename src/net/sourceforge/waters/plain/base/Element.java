//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   Element
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.base;

import java.io.Serializable;

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
  implements Proxy, Cloneable, Serializable
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
  //# Comparing
  /**
   * Checks whether two elements are equal. This method implements
   * content-based equality, i.e., two elements will be equal if their
   * contents are the same. Since elements have no contents by themselves,
   * this default implementation considers two elements as equal if they
   * have the same proxy interface.
   */
  public boolean equalsByContents(final Proxy partner)
  {
    return
      partner != null && getProxyInterface() == partner.getProxyInterface();
  }

  /**
   * Checks whether two elements are equal and have the same geometry
   * information. This method implements content-based equality, i.e., two
   * elements will be equal if their contents and geometry information are
   * the same. The default implementation simply calls {@link
   * #equalsByContents(Proxy) equalsByContents()}.
   */
  public boolean equalsWithGeometry(final Proxy partner)
  {
    return equalsByContents(partner);
  }

  /**
   * Computes a hash code based on this object's contents. This method is
   * used to compute a hash code to match the equality defined by the
   * {@link #equalsByContents(Proxy) equalsByContents()} method. The
   * default implementation for elements computes the hash code based only
   * on the object's proxy interface.
   */
  public int hashCodeByContents()
  {
    return getProxyInterface().hashCode();
  }

  /**
   * Computes a hash code based on this object's contents and geometry
   * information. This method is used to compute a hash code to match the
   * equality defined by the {@link #equalsWithGeometry(Proxy)
   * equalsWithGeometry()} method. The default implementation for elements
   * simply calls {@link #hashCodeByContents()}.
   */
  public int hashCodeWithGeometry()
  {
    return hashCodeByContents();
  }


  //#########################################################################
  //# Printing
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }

}
