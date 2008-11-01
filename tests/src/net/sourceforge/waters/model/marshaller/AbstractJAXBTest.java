//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AbstractJAXBTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.net.URL;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.junit.AbstractWatersTest;


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
    ensureParentDirectoryExists(outfilename);
    manager.saveAs(proxy1, outfilename);
    final URI outuri = outfilename.toURI();
    final DocumentProxy proxy2 = manager.load(outuri);
    assertTrue("Structure changed after marshalling!",
               proxy1.equalsByContents(proxy2));
    assertTrue("Structure changed after marshalling!",
               proxy2.equalsByContents(proxy1));
    assertTrue("Geometry information changed after marshalling!",
               proxy1.equalsWithGeometry(proxy2));
    assertTrue("Geometry information changed after marshalling!",
               proxy2.equalsWithGeometry(proxy1));
    final Class<D> clazz = Casting.toClass(proxy1.getClass());
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
    final File infile = new File(subdir, extname);
    final String infilename = "examples/" + infile.toString();
    final URL url = AbstractJAXBTest.class.getResource(infilename);
    final DocumentManager manager = getDocumentManager();
    final DocumentProxy proxy1 = manager.load(url);
    final File truefile = new File(getWatersInputRoot(), infile.toString());
    final DocumentProxy proxy2 = manager.load(truefile);
    assertTrue("Structure in JAR differs from file!",
               proxy1.equalsByContents(proxy2));
    assertTrue("Structure in JAR differs from file!",
               proxy2.equalsByContents(proxy1));
    assertTrue("Geometry information in JAR differs from file!",
               proxy1.equalsWithGeometry(proxy2));
    assertTrue("Geometry information in JAR differs from file!",
               proxy2.equalsWithGeometry(proxy1));
    final Class<D> clazz = Casting.toClass(proxy1.getClass());
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
    assertTrue("Constructed structure differs from parsed-back!",
               handcrafted.equalsByContents(parsed1));
    assertTrue("Constructed structure differs from parsed-back!",
               parsed1.equalsByContents(handcrafted));
    assertTrue("Constructed geometry info differs from parsed-back!",
               handcrafted.equalsWithGeometry(parsed1));
    assertTrue("Constructed geometry info differs from parsed-back!",
               parsed1.equalsWithGeometry(handcrafted));
    final D parsed2 = unmarshaller.unmarshal(inuri);
    assertTrue("Constructed structure differs from expected in file!",
               handcrafted.equalsByContents(parsed2));
    assertTrue("Constructed structure differs from expected in file!",
               parsed2.equalsByContents(handcrafted));
    assertTrue("Constructed geometry info differs from expected in file!",
               handcrafted.equalsWithGeometry(parsed2));
    assertTrue("Constructed geometry info differs from expected in file!",
               parsed2.equalsWithGeometry(handcrafted));
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
    assertTrue("Clone differs from original!",
               proxy.equalsWithGeometry(cloned));
    return cloned;
  }

  protected void checkIntegrity(final D document)
    throws Exception
  {
    final DocumentIntegrityChecker checker = getIntegrityChecker();
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
  protected abstract DocumentIntegrityChecker getIntegrityChecker();


  //#########################################################################
  //# Overrides for junit.framework.TestCase
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
