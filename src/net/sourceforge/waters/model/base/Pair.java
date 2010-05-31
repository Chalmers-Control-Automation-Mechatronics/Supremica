//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Pair
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * An ordered pair of two elements from different classes.
 *
 * @author Andrew Holland
 */

public class Pair<A, B>
{

  //#################################################################################
  //# Constructor
  public Pair(final A a, final B b)
  {
    mA = a;
    mB = b;
  }


  // #################################################################################
  // # Simple Access
  public A getFirst()
  {
    return mA;
  }

  public B getSecond()
  {
    return mB;
  }

  public String toString()
  {
    return "{" + mA + "," + mB + "}";
  }


  //#################################################################################
  //# Data Members
  private final A mA;
  private final B mB;

}
