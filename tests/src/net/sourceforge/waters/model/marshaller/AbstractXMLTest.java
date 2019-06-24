//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Arrays;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractXMLTest<D extends DocumentProxy>
  extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testParseAll()
    throws Exception
  {
    final FileFilter filter = new TestDirectoryFilter();
    final File root = getInputRoot();
    testDirectory(root, filter);
  }


  //#########################################################################
  //# Utilities
  protected void testDirectory(final File file, final FileFilter filter)
    throws Exception
  {
    if (file.isDirectory()) {
      final File[] children = file.listFiles(filter);
      Arrays.sort(children);
      for (final File child : children) {
        testDirectory(child, filter);
      }
    } else {
      System.out.println(file + " ...");
      testParse(file);
    }
  }

  protected D testParse(final String... path)
    throws Exception
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final String ext = marshaller.getDefaultExtension();
    final File file = getInputFile(path, ext);
    return testParse(file);
  }

  protected D testParse(final File file)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final URI uri = file.toURI();
    final D doc = unmarshaller.unmarshal(uri);
    checkIntegrity(doc);
    if (file.length() <= 5000000) {
      checkPrint(doc);
      final ProxyUnmarshaller<D> altUnmarshaller = getAltProxyUnmarshaller();
      if (altUnmarshaller != null) {
        final D expected = altUnmarshaller.unmarshal(uri);
        assertProxyEquals("Unexpected contents in unmarshalled document!",
                          doc, expected);
      }
    }
    return doc;
  }

  protected D testPrint(final String... path)
    throws Exception
  {
    try {
      mIsPrinting = true;
      return testParse(path);
    } finally {
      mIsPrinting = false;
    }
  }

  protected void checkIntegrity(final D document)
    throws Exception
  {
    final DocumentIntegrityChecker<D> checker = getIntegrityChecker();
    checker.check(document);
  }

  protected void checkPrint(final D proxy)
    throws Exception
  {
    if (mIsPrinting) {
      final ProxyPrinter printer = getPrinter();
      printer.pprint(proxy);
      printer.flush();
    }
  }

  protected void checkTime(final ProxyUnmarshaller<D> unmarshaller,
                           final URI uri)
    throws Exception
  {
    System.gc();
    final int NUM_RUNS = 3;
    double sum = 0.0;
    for (int i = 0; i < NUM_RUNS; i++) {
      final long start = System.currentTimeMillis();
      unmarshaller.unmarshal(uri);
      final long stop = System.currentTimeMillis();
      sum += (stop - start);
    }
    System.out.format("%s time: %.2f s\n",
                      ProxyTools.getShortClassName(unmarshaller),
                      (0.001 * sum) / NUM_RUNS);
  }


  //#########################################################################
  //# Inner Class TestDirectoryFilter
  private class TestDirectoryFilter implements FileFilter {

    //#######################################################################
    //# Interface java.io.FileFilter
    @Override
    public boolean accept(final File path)
    {
      if (path.isDirectory()) {
        return true;
      } else {
        final String name = path.getName();
        final int dotpos = name.lastIndexOf('.');
        if (dotpos < 0) {
          return false;
        }
        final String ext = name.substring(dotpos);
        final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
        final String dftext = unmarshaller.getDefaultExtension();
        return ext.equals(dftext);
      }
    }

  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProxyMarshaller<D> getProxyMarshaller();
  protected abstract ProxyUnmarshaller<D> getProxyUnmarshaller();
  protected ProxyUnmarshaller<D> getAltProxyUnmarshaller() {return null;}
  protected abstract ProxyPrinter getPrinter();
  protected abstract DocumentIntegrityChecker<D> getIntegrityChecker();


  //#########################################################################
  //# Data Members
  private boolean mIsPrinting = false;

}
