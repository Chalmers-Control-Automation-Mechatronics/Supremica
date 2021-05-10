//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
