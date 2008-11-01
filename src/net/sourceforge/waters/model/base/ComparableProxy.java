//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ComparableProxy
//###########################################################################
//# $Id$
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
