//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: WatersTestCase.java,v 1.3 2006-03-02 12:12:50 martin Exp $
//###########################################################################

package net.sourceforge.waters.junit;

import java.io.File;
import junit.framework.TestCase;

public abstract class WatersTestCase
  extends TestCase
{

  //#########################################################################
  //# Constructors
  public WatersTestCase()
  {
    final String inputprop = System.getProperty("waters.test.inputdir");
    final String outputprop = System.getProperty("waters.test.outputdir");
    mInputRoot = new File(inputprop);
    mOutputRoot = new File(outputprop);
  }


  //#########################################################################
  //# Simple Access
  protected File getInputRoot()
  {
    return mInputRoot;
  }

  protected File getOutputRoot()
  {
    return mOutputRoot;
  }

  protected File getInputDirectory()
  {
    return getInputRoot();
  }

  protected File getOutputDirectory()
  {
    final String packname = getClass().getPackage().getName();
    final String[] parts = packname.split("\\.");
    File result = mOutputRoot;
    for (int i = 3; i < parts.length; i++) {
      result = new File(result, parts[i]);
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final File mInputRoot;
  private final File mOutputRoot;

}
