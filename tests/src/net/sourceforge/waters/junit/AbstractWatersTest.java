//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AbstractWatersTest
//###########################################################################
//# $Id: AbstractWatersTest.java,v 1.3 2006-09-14 11:31:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.junit;

import java.io.File;
import junit.framework.TestCase;

public abstract class AbstractWatersTest
  extends TestCase
{

  //#########################################################################
  //# Constructors
  public AbstractWatersTest()
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
    final String classname = getClass().getName();
    final String[] parts = classname.split("\\.");
    File result = mOutputRoot;
    for (int i = 3; i < parts.length; i++) {
      result = new File(result, parts[i]);
    }
    ensureDirectoryExists(result);
    return result;
  }

  protected void ensureDirectoryExists(final File dirname)
  {
    if (!dirname.isDirectory()) {
      final boolean success = dirname.mkdirs();
      assertTrue("Could not create output directory '" + dirname + "'!",
                 success);
    }
  }

  protected void ensureParentDirectoryExists(final File outfile)
  {
    final File dirname = outfile.getParentFile();
    ensureDirectoryExists(dirname);
  }


  //#########################################################################
  //# Data Members
  private final File mInputRoot;
  private final File mOutputRoot;

}
