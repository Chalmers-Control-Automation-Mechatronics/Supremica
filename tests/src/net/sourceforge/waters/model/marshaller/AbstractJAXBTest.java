//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractJAXBTest<D extends DocumentProxy>
  extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testParseAll()
    throws Exception
  {
    mIsPrinting = false;
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
      testParse(file);
    }
  }

  protected D testParse(final String name)
    throws Exception
  {
    return testParse(getWatersInputRoot(), name);
  }

  protected D testParse(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testParse(dir, name);
  }

  protected D testParse(final String dirname1,
                        final String dirname2,
                        final String name)
    throws Exception
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testParse(dir2, name);
  }

  protected D testParse(final File dir, final String name)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File filename = new File(dir, extname);
    return testParse(filename);
  }

  protected D testParse(final File filename)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final URI uri = filename.toURI();
    final D proxy = unmarshaller.unmarshal(uri);
    checkIntegrity(proxy);
    checkPrint(proxy);
    return proxy;
  }

  protected D testMarshal(final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    return testMarshal(getWatersInputRoot(), name);
  }

  protected D testMarshal(final String dirname, final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testMarshal(dir, name);
  }

  protected D testMarshal(final String dirname1,
                          final String dirname2,
                          final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testMarshal(dir2, name);
  }

  protected D testMarshal(final File dir, final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final String extname = name + marshaller.getDefaultExtension();
    final File infilename = new File(dir, extname);
    final URI inuri = infilename.toURI();
    final File outfilename = new File(getOutputDirectory(), extname);
    return testMarshal(inuri, outfilename);
  }

  protected D testMarshal(final URI inuri, final File outfilename)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final DocumentManager manager = getDocumentManager();
    final DocumentProxy proxy1 = manager.load(inuri);
    manager.saveAs(proxy1, outfilename);
    final URI outuri = outfilename.toURI();
    final DocumentProxy proxy2 = manager.load(outuri);
    assertProxyEquals("Structure changed after marshaling!", proxy1, proxy2);
    assertProxyEquals("Structure changed after marshaling!", proxy2, proxy1);
    @SuppressWarnings("unchecked")
    final Class<D> clazz = (Class<D>) proxy1.getClass();
    return clazz.cast(proxy1);
  }

  protected D testJar(final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir = new File("");
    return testJar(dir, name);
  }

  protected D testJar(final String dirname, final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir = new File(dirname);
    return testJar(dir, name);
  }

  protected D testJar(final String dirname1,
                      final String dirname2,
                      final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir1 = new File(dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testJar(dir2, name);
  }

  protected D testJar(final File subdir, final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final String extname = name + marshaller.getDefaultExtension();
    final StringBuilder buffer = new StringBuilder(extname);
    File current = subdir;
    while (current != null) {
      final String currentName = current.getName();
      buffer.insert(0, '/');
      buffer.insert(0, currentName);
      current = current.getParentFile();
    }
    final String infilename = "examples/" + buffer.toString();
    final URL url = AbstractJAXBTest.class.getResource(infilename);
    final DocumentManager manager = getDocumentManager();
    final DocumentProxy proxy1 = manager.load(url);
    final File truefile = new File(getWatersInputRoot(), buffer.toString());
    final DocumentProxy proxy2 = manager.load(truefile);
    assertProxyEquals("Structure in JAR differs from file!", proxy1, proxy2);
    assertProxyEquals("Structure in JAR differs from file!", proxy2, proxy1);
    @SuppressWarnings("unchecked")
    final Class<D> clazz = (Class<D>) proxy1.getClass();
    return clazz.cast(proxy1);
  }

  protected void testHandcraft(final String dirname, final D handcrafted)
    throws Exception
  {
    final File subdir = new File(getWatersInputRoot(), dirname);
    testHandcraft(subdir, handcrafted);
  }


  protected void testHandcraft(final String dirname1,
                               final String dirname2,
                               final D handcrafted)
    throws Exception
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    testHandcraft(dir2, handcrafted);
  }

  protected void testHandcraft(final File fullindir, final D handcrafted)
    throws Exception
  {
    checkIntegrity(handcrafted);
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final String name = handcrafted.getName();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File infilename = new File(fullindir, extname);
    final File outfilename = new File(getOutputDirectory(), extname);
    final URI inuri = infilename.toURI();
    final URI outuri = outfilename.toURI();
    marshaller.marshal(handcrafted, outfilename);
    final D parsed1 = unmarshaller.unmarshal(outuri);
    assertProxyEquals("Constructed structure differs from parsed-back!",
                      handcrafted, parsed1);
    assertProxyEquals("Constructed structure differs from parsed-back!",
                      parsed1, handcrafted);
    final D parsed2 = unmarshaller.unmarshal(inuri);
    assertProxyEquals("Constructed structure differs from expected in file!",
                      handcrafted, parsed2);
    assertProxyEquals("Constructed structure differs from expected in file!",
                      parsed2, handcrafted);
  }

  protected D testClone(final String name)
    throws Exception
  {
    return testClone(getWatersInputRoot(), name);
  }

  protected D testClone(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testClone(dir, name);
  }

  protected D testClone(final String dirname1,
                        final String dirname2,
                        final String name)
    throws Exception
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testClone(dir2, name);
  }

  protected D testClone(final File dir, final String name)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File filename = new File(dir, extname);
    return testClone(filename);
  }

  protected D testClone(final File filename)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final URI uri = filename.toURI();
    final D proxy = unmarshaller.unmarshal(uri);
    final DocumentProxy cloneddoc = proxy.clone();
    final Class<D> clazz = unmarshaller.getDocumentClass();
    final D cloned = clazz.cast(cloneddoc);
    checkIntegrity(cloned);
    assertProxyEquals("Clone differs from original!", proxy, cloned);
    return cloned;
  }

  protected D testSerialize(final String name)
    throws WatersUnmarshalException, IOException, ClassNotFoundException
  {
    return testSerialize(getWatersInputRoot(), name);
  }

  protected D testSerialize(final String dirname, final String name)
    throws WatersUnmarshalException, IOException, ClassNotFoundException
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testSerialize(dir, name);
  }

  protected D testSerialize(final String dirname1,
                            final String dirname2,
                            final String name)
    throws WatersUnmarshalException, IOException, ClassNotFoundException
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testSerialize(dir2, name);
  }

  protected D testSerialize(final File dir, final String name)
    throws WatersUnmarshalException, IOException, ClassNotFoundException
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final String extname = name + marshaller.getDefaultExtension();
    final File infilename = new File(dir, extname);
    final URI inuri = infilename.toURI();
    final String sername = name + ".ser";
    final File outfilename = new File(getOutputDirectory(), sername);
    return testSerialize(inuri, outfilename);
  }

  protected D testSerialize(final URI inuri, final File outfilename)
    throws WatersUnmarshalException, IOException, ClassNotFoundException
  {
    final DocumentManager manager = getDocumentManager();
    final DocumentProxy doc1 = manager.load(inuri);
    if (doc1 instanceof Serializable) {
      final FileOutputStream fos =  new FileOutputStream(outfilename);
      final ObjectOutputStream out = new ObjectOutputStream(fos);
      out.writeObject(doc1);
      out.close();
      final FileInputStream fis =  new FileInputStream(outfilename);
      final ObjectInputStream in = new ObjectInputStream(fis);
      final DocumentProxy doc2 = (DocumentProxy) in.readObject();
      in.close();
      assertProxyEquals("Structure changed after serialising!", doc1, doc2);
      assertProxyEquals("Structure changed after serialising!", doc2, doc1);
    }
    @SuppressWarnings("unchecked")
    final Class<D> clazz = (Class<D>) doc1.getClass();
    return clazz.cast(doc1);
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


  //#########################################################################
  //# Creating a Document Manager
  protected DocumentManager getDocumentManager()
  {
    if (mDocumentManager == null) {
      final ProxyMarshaller<D> marshaller = getProxyMarshaller();
      final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
      mDocumentManager = new DocumentManager();
      mDocumentManager.registerMarshaller(marshaller);
      mDocumentManager.registerUnmarshaller(unmarshaller);
    }
    return mDocumentManager;
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProxyMarshaller<D> getProxyMarshaller();
  protected abstract ProxyUnmarshaller<D> getProxyUnmarshaller();
  protected abstract ProxyPrinter getPrinter();
  protected abstract DocumentIntegrityChecker<D> getIntegrityChecker();


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void tearDown()
    throws Exception
  {
    mDocumentManager = null;
    super.tearDown();
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
  //# Data Members
  private DocumentManager mDocumentManager;
  private boolean mIsPrinting = true;

}
