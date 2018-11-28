//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.junit;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import net.sourceforge.waters.model.base.AbstractEqualityVisitor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;


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
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final File dir = getOutputDirectory();
    mConfigurationFactory = new WatersLogConfigurationFactory(dir);
    ConfigurationFactory.setConfigurationFactory(mConfigurationFactory);
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

  protected String getLogFileName()
  {
    return mConfigurationFactory.getFileName();
  }


  //#########################################################################
  //# Proxy Equality Assertions
  protected void assertProxyEquals(final Proxy proxy, final Proxy expected)
  {
    assertProxyEquals(null, proxy, expected);
  }

  protected void assertProxyEquals(final String msg,
                                   final Proxy proxy,
                                   final Proxy expected)
  {
    final Class<?> clazz = proxy.getClass();
    final Package pack = clazz.getPackage();
    final String packname = pack.getName();
    final int dotpos = packname.lastIndexOf('.');
    final String lastpart = packname.substring(dotpos + 1);
    if (lastpart.equals("des")) {
      assertProductDESProxyEquals(msg, proxy, expected);
    } else if (lastpart.equals("module")) {
      assertModuleProxyEquals(msg, proxy, expected);
    } else {
      fail("Unsupported Proxy class " + clazz.getName() + "!");
    }
  }
  protected void assertProductDESProxyEquals(final Proxy proxy,
                                             final Proxy expected)
  {
    assertProductDESProxyEquals(null, proxy, expected);
  }

  protected void assertProductDESProxyEquals(final String msg,
                                             final Proxy proxy,
                                             final Proxy expected)
  {
    final ProductDESEqualityVisitor eq = new ProductDESEqualityVisitor(true);
    assertProxyEquals(eq, msg, proxy, expected);
  }

  protected void assertModuleProxyEquals(final Proxy proxy,
                                         final Proxy expected)
  {
    assertModuleProxyEquals(null, proxy, expected);
  }

  protected void assertModuleProxyEquals(final String msg,
                                         final Proxy proxy,
                                         final Proxy expected)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, true);
    assertProxyEquals(eq, msg, proxy, expected);
  }

  protected void assertProxyEquals(final AbstractEqualityVisitor eq,
                                   final String msg,
                                   final Proxy proxy,
                                   final Proxy expected)
  {
    if (!eq.equals(proxy, expected)) {
      String diagnostics = eq.getDiagnostics(msg);
      getLogger().error(diagnostics);
      if (System.getProperty("waters.test.ant") != null) {
        // Funny thing, when run from ANT, the text after the first newline
        // in the argument passed to fail() gets printed on the console.
        // So we suppress the output and refer the programmer to the log file.
        diagnostics = msg + " (See " + getLogFileName() + " for details.)";
      }
      fail(diagnostics);
    }
  }

  protected void assertProxyListEquals(final AbstractEqualityVisitor eq,
                                       final String msg,
                                       final List<? extends Proxy> list,
                                       final List<? extends Proxy> expected)
  {
    assertEquals("Unexpected list length!", expected.size(), list.size());
    final Iterator<? extends Proxy> iter1 = list.iterator();
    final Iterator<? extends Proxy> iter2 = expected.iterator();
    while (iter1.hasNext()) {
      final Proxy proxy1 = iter1.next();
      final Proxy proxy2 = iter2.next();
      assertProxyEquals(eq, msg, proxy1, proxy2);
    }
  }


  //#########################################################################
  //# Exception Checking Assertions
  /**
   * Asserts that an exception message mentions a several phrases.
   * @param  exception  The exception to be checked.
   * @param  culprits   Phrases to be searched for in the exception
   *                    message. If the message does not textually include
   *                    each of these strings, then an {@link
   *                    AssertionFailedError} will result.
   */
  public void assertMentionsAll(final WatersException exception,
                                final String... culprits)
  {
    final String msg = exception.getMessage();
    for (final String culprit : culprits) {
      assertTrue("Caught " + exception.getClass().getSimpleName() +
                 " as expected, but message '" + msg +
                 "' does not mention culprit: " + culprit + "!",
                 msg.contains(culprit));
    }
  }


  //#########################################################################
  //# Logging
  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return LogManager.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final File mInputRoot;
  private final File mOutputRoot;
  private WatersLogConfigurationFactory mConfigurationFactory;

}
