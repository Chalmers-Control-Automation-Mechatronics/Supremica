//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDPackage
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;


/**
 * @author Robi Malik
 */

public enum BDDPackage
{

  //#########################################################################
  //# Enumeration
  BUDDY(true),
  CUDD(true),
  //CAL(false),
  J(false),
  JAVA(false),
  JDD(false),
  TEST(false),
  TYPED(false);


  //#########################################################################
  //# Constructor
  private BDDPackage(final boolean reorder)
  {
    mName = toString().toLowerCase();
    mIsReorderingSupported = reorder;
  }


  //#########################################################################
  //# Simple Access
  String getBDDPackageName()
  {
    return mName;
  }

  boolean isReorderingSupported()
  {
    return mIsReorderingSupported;
  }


  //#########################################################################
  //# Simple Access
  private final String mName;
  private final boolean mIsReorderingSupported;

}
