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
   * Returns the most specific proxy interface implemented by this object.
   * This method must return one of the leaf interfaces of the proxy
   * hierarchy, e.g. {@link net.sourceforge.waters.model.des.EventProxy
   * EventProxy}, not any specific implementation class. This is used by
   * the different equality methods to ensure that objects from different
   * implementations can be considered equal.
   */
  public Class<? extends Proxy> getProxyInterface();

  /**
   * Checks whether two elements are equal. This method implements
   * content-based equality, i.e., two elements will be equal if their
   * contents are the same. It considers all structural contents of the
   * object, except for geometry information.
   * @see #equalsWithGeometry(Proxy) equalsWithGeometry()
   * @see #hashCodeByContents()
   */
  public boolean equalsByContents(Proxy partner);

  /**
   * Checks whether two elements are equal and have the same geometry
   * information. This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same.  In contrast to
   * the {@link #equalsByContents(Proxy) equalsByContents()} method, this
   * method also takes the layout information of graphical objects such as
   * nodes and edges into account.
   * @see #equalsByContents(Proxy) equalsByContents()
   * @see #hashCodeWithGeometry()
   */
  public boolean equalsWithGeometry(Proxy partner);

  /**
   * Computes a hash code based on this object's contents. This method is
   * used to compute a hash code to match the equality defined by the
   * {@link #equalsByContents(Proxy) equalsByContents()} method. All
   * structural contents of the object, except for geometry information,
   * are taken into account.
   * @see #equalsByContents(Proxy) equalsByContents()
   * @see #hashCodeWithGeometry()
   */
  public int hashCodeByContents();

  /**
   * Computes a hash code based on this object's contents and geometry
   * information. This method is used to compute a hash code to match the
   * equality defined by the {@link #equalsWithGeometry(Proxy)
   * equalsWithGeometry()} method. All structural contents of the object
   * including geometry information, are taken into account.
   * @see #equalsWithGeometry(Proxy) equalsWithGeometry()
   * @see #hashCodeWithGeometry()
   */
  public int hashCodeWithGeometry();


  //#########################################################################
  //# Visitors
  /**
   * Calls a visitor on this object.
   */
  public Object acceptVisitor(ProxyVisitor visitor)
    throws VisitorException;

}
