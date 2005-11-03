//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ComparableProxy
//###########################################################################
//# $Id: ComparableProxy.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

/**
 * <P>A marker interface for implementations of the {@link Proxy}
 * interface that are also comparable.</P>
 *
 * @author Robi Malik
 */

public interface ComparableProxy<T>
  extends Proxy, Comparable<T>
{

  //#########################################################################
  //# Cloning
  public ComparableProxy<T> clone();

}
