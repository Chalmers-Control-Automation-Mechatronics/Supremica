//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AbstractWatersTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.junit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;


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
    final File dir = getOutputDirectory();
    final String name = "log4j.log";
    mLogFile = new File(dir, name);
    final OutputStream stream = new FileOutputStream(mLogFile, true);
    final PrintWriter writer = new PrintWriter(stream);
    final PatternLayout layout = new PatternLayout("%-5p %m%n");
    final Appender appender = new WriterAppender(layout, writer);
    final String fullname = mLogFile.toString();
    appender.setName(fullname);
    final Logger root = Logger.getRootLogger();
    root.addAppender(appender);
  }

  protected void tearDown() throws Exception
  {
    if (mLogFile != null) {
      final Logger root = Logger.getRootLogger();
      final String fullname = mLogFile.toString();
      final Appender appender = root.getAppender(fullname);
      root.removeAppender(appender);
      appender.close();
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

  protected File getWatersInputRoot()
  {
    return new File(getInputRoot(), "waters");
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
  //# Logging
  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final File mInputRoot;
  private final File mOutputRoot;

  private File mLogFile;

}
