//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Proxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

/**
 * <P>
 * The common interface for all Waters elements.
 * </P>
 *
 * <P>
 * This mainly is a marker interface to make it possible to treat all Waters
 * objects in a uniform way. Its main functionality is to provide a common entry
 * point for <I>visitors</I> ({@link ProxyVisitor}), which implement all kinds
 * of content-dependent functionality.
 * </P>
 *
 * <P>
 * Proxies do <I>not</I> override Java's {@link Object#equals(Object) equals()}
 * and {@link Object#hashCode() hashCode()} methods, so they are compared in the
 * standard way by object identity. Content-based equality is provided by
 * special visitors ({@link AbstractEqualityVisitor}).
 * </P>
 *
 * <P>
 * Some subtypes (most prominently {@link NamedProxy}) may implement the
 * {@link Comparable} interface to provide a more convenient ordering. These
 * implementations are not compatible with the standard equality.
 * </P>
 *
 * @see ProxyVisitor
 * @see AbstractEqualityVisitor
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
   * Returns the most specific proxy interface implemented by this object. This
   * method must return one of the leaf interfaces of the proxy hierarchy, e.g.
   * {@link net.sourceforge.waters.model.des.EventProxy EventProxy}, not any
   * specific implementation class. This can be used by equality visitors to
   * ensure that objects from different implementations are considered as
   * equal.
   */
  public Class<? extends Proxy> getProxyInterface();


  //#########################################################################
  //# Visitors
  /**
   * Calls a visitor on this object.
   */
  public Object acceptVisitor(ProxyVisitor visitor)
    throws VisitorException;

}
