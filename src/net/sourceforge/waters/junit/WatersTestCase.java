//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.junit
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: WatersTestCase.java,v 1.1 2005-02-18 01:32:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import junit.framework.TestCase;


abstract class WatersTestCase
  extends TestCase
{

  //#########################################################################
  //# Constructors
  WatersTestCase()
  {
    final String inputprop = System.getProperty("waters.test.inputdir");
    final String outputprop = System.getProperty("waters.test.outputdir");
    mInputRoot = new File(inputprop);
    mOutputRoot = new File(outputprop);
  }


  //#########################################################################
  //# Simple Access
  File getInputRoot()
  {
    return mInputRoot;
  }

  File getOutputRoot()
  {
    return mOutputRoot;
  }


  //#########################################################################
  //# Data Members
  private final File mInputRoot;
  private final File mOutputRoot;

}
