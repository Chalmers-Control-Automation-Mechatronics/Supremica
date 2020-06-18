//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
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
    parseDirectory(root, filter);
  }

  public void testMarshalAll()
    throws Exception
  {
    final FileFilter filter = new TestDirectoryFilter();
    final File root = getInputRoot();
    marshalDirectory(root, filter);
  }


  //#########################################################################
  //# Utilities
  protected void parseDirectory(final File file, final FileFilter filter)
    throws Exception
  {
    if (file.isDirectory()) {
      final File[] children = file.listFiles(filter);
      Arrays.sort(children);
      for (final File child : children) {
        parseDirectory(child, filter);
      }
    } else {
      System.out.println(file + " ...");
      testParse(file);
    }
  }

  protected void marshalDirectory(final File file, final FileFilter filter)
    throws Exception
  {
    if (file.isDirectory()) {
      final File[] children = file.listFiles(filter);
      Arrays.sort(children);
      for (final File child : children) {
        marshalDirectory(child, filter);
      }
    } else if (file.length() <= FILE_SIZE_LIMIT) {
      System.out.println(file + " ...");
      testMarshal(file);
    }
  }


  protected D testParse(final String... path)
    throws Exception
  {
    final File file = getInputFile(path);
    return testParse(file);
  }

  protected D testParse(final File file)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final URI uri = file.toURI();
    final D doc = unmarshaller.unmarshal(uri);
    checkIntegrity(doc);
    checkPrint(doc);
    return doc;
  }

  protected void testHandcraft(final D handcrafted, final String... path)
    throws Exception
  {
    checkIntegrity(handcrafted);
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final String name = handcrafted.getName();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File dir = getInputDirectory(path);
    final File inFile = new File(dir, extname);
    final File outFile = new File(getOutputDirectory(), extname);
    final URI inURI = inFile.toURI();
    final URI outURI = outFile.toURI();
    marshaller.marshal(handcrafted, outFile);
    final D parsed1 = unmarshaller.unmarshal(outURI);
    assertProxyEquals("Constructed structure differs from parsed-back!",
                      handcrafted, parsed1);
    assertProxyEquals("Constructed structure differs from parsed-back!",
                      parsed1, handcrafted);
    final D parsed2 = unmarshaller.unmarshal(inURI);
    assertProxyEquals("Constructed structure differs from expected in file!",
                      handcrafted, parsed2);
    assertProxyEquals("Constructed structure differs from expected in file!",
                      parsed2, handcrafted);
  }

  protected D testJar(final String... path)
    throws Exception
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    boolean hasExtension = false;
    final StringBuilder builder = new StringBuilder("examples");
    for (final String part : path) {
      builder.append('/');
      builder.append(part);
      hasExtension = part.contains(".");
    }
    if (!hasExtension) {
      final String ext = marshaller.getDefaultExtension();
      builder.append(ext);
    }
    final String name = builder.toString();
    final URL url = AbstractJAXBTest.class.getResource(name);
    final URI jarURI = url.toURI();
    final D jarDoc = unmarshaller.unmarshal(jarURI);
    checkIntegrity(jarDoc);
    final File file = getInputFile(path);
    final URI fileURI = file.toURI();
    final D fileDoc = unmarshaller.unmarshal(fileURI);
    checkIntegrity(fileDoc);
    assertProxyEquals("Structure in JAR differs from file!", jarDoc, fileDoc);
    return jarDoc;
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

  protected void testMarshal(final String... path)
    throws Exception
  {
    final File file = getInputFile(path);
    testMarshal(file);
  }

  protected void testMarshal(final File inFile)
    throws Exception
  {
    final URI inURI = inFile.toURI();
    final String name = inFile.getName();
    final File outFile = new File(getOutputDirectory(), name);
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final D inDoc = unmarshaller.unmarshal(inURI);
    checkIntegrity(inDoc);
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    marshaller.marshal(inDoc, outFile);
    final URI outURI = outFile.toURI();
    final D outDoc = unmarshaller.unmarshal(outURI);
    checkIntegrity(outDoc);
    assertProxyEquals("Structure changed after marshalling!", inDoc, outDoc);
    assertProxyEquals("Structure changed after marshalling!", outDoc, inDoc);
  }


  protected D testSerialize(final String... path)
    throws Exception
  {
    final File inFile = getInputFile(path);
    final URI inURI = inFile.toURI();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final D doc1 = unmarshaller.unmarshal(inURI);
    final String name = path[path.length - 1] + ".ser";
    final File outFile = new File(getOutputDirectory(), name);
    final FileOutputStream fos =  new FileOutputStream(outFile);
    final ObjectOutputStream out = new ObjectOutputStream(fos);
    out.writeObject(doc1);
    out.close();
    final FileInputStream fis =  new FileInputStream(outFile);
    final ObjectInputStream in = new ObjectInputStream(fis);
    final DocumentProxy doc2 = (DocumentProxy) in.readObject();
    in.close();
    assertProxyEquals("Structure changed after serialising!", doc1, doc2);
    return doc1;
  }


  protected D testClone(final String... path)
    throws Exception
  {
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final File file = getInputFile(path);
    final URI uri = file.toURI();
    final D proxy = unmarshaller.unmarshal(uri);
    final D cloned = ProxyTools.clone(proxy);
    checkIntegrity(cloned);
    assertProxyEquals("Clone differs from original!", proxy, cloned);
    return cloned;
  }


  protected D testCrossClone(final ProxyUnmarshaller<D> unmarshaller,
                             final ProxyCloner cloner,
                             final String... path)
    throws Exception
  {
    final File file = getInputFile(path);
    final URI uri = file.toURI();
    final D proxy = unmarshaller.unmarshal(uri);
    final Proxy clonedDoc = cloner.getClone(proxy);
    final Class<D> clazz = unmarshaller.getDocumentClass();
    final D cloned = clazz.cast(clonedDoc);
    checkIntegrity(cloned);
    assertProxyEquals("Clone differs from original!", proxy, cloned);
    return cloned;
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

  protected File getInputFile(final String... path)
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final String ext = marshaller.getDefaultExtension();
    return getInputFile(path, ext);
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProxyMarshaller<D> getProxyMarshaller();
  protected abstract ProxyUnmarshaller<D> getProxyUnmarshaller();
  protected abstract ProxyPrinter getPrinter();
  protected abstract DocumentIntegrityChecker<D> getIntegrityChecker();


  //#########################################################################
  //# Inner Class TestDirectoryFilter
  private class TestDirectoryFilter implements FileFilter
  {
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
  private boolean mIsPrinting = false;


  //#########################################################################
  //# Class Constants
  private static final int FILE_SIZE_LIMIT = 1 << 20;

}
