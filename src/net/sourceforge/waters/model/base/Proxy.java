//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Proxy
//###########################################################################
//# $Id: Proxy.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

/**
 * <P>The common interface for all Waters elements.</P>
 *
 * @author Robi Malik
 */

public interface Proxy {

  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this object. This method supports the
   * general contract of the {@link java.lang.Object#clone() clone()}
   * method. Its precise semantics differs for different implementations of
   * the <CODE>Proxy</CODE> interface.
   */
  public Proxy clone();


  //#########################################################################
  //# Comparing
  /**
   * Checks whether two elements are equal and have the same geometry
   * information.  This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same. While the
   * standard {@link java.lang.Object#equals(Object) equals()} method only
   * considers structural contents, this method also takes the layout
   * information of graphical objects such as nodes and edges into
   * account. This method can be very slow for large structures and is
   * intended for testing purposes only.
   */
  public boolean equalsWithGeometry(Object partner);


  //#########################################################################
  //# Visitors
  /**
   * Calls a visitor on this object.
   */
  public Object acceptVisitor(ProxyVisitor visitor)
    throws VisitorException;

}
