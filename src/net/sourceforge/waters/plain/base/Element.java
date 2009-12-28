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
  //# Printing
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
