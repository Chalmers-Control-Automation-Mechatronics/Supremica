//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AbstractWatersTest
//###########################################################################
//# $Id: AbstractWatersTest.java,v 1.6 2007-05-28 07:07:08 robi Exp $
//###########################################################################

package net.sourceforge.waters.junit;

import java.io.File;
import java.lang.reflect.Method;
import junit.framework.TestCase;

public abstract class AbstractWatersTest
  extends TestCase
{

  //#########################################################################
  //# Constructors
  public AbstractWatersTest()
  {
    this(null);
  }

  public AbstractWatersTest(final String name)
  {
    super(name);
    final String inputprop = System.getProperty("waters.test.inputdir");
    final String outputprop = System.getProperty("waters.test.outputdir");
    mInputRoot = new File(inputprop);
    mOutputRoot = new File(outputprop);
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();
    final ClassLoader loader = AbstractWatersTest.class.getClassLoader();
    try {
      final Class<?> lclazz = loader.loadClass(LOGGERFACTORY);
      final Method method = lclazz.getMethod("logToFile", File.class);
      final File dir = getOutputDirectory();
      final String name = "log4j.log";
      mLogFile = new File(dir, name);
      method.invoke(null, mLogFile);
    } catch (final ClassNotFoundException exception) {
      // No loggers---no trouble ...
    }
  }

  protected void tearDown() throws Exception
  {
    if (mLogFile != null) {
      final ClassLoader loader = AbstractWatersTest.class.getClassLoader();
      final Class<?> lclazz = loader.loadClass(LOGGERFACTORY);
      final Method method = lclazz.getMethod("cancelLogToFile", File.class);
      method.invoke(null, mLogFile);
    }
    super.tearDown();
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

  private File mLogFile;


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";

}
