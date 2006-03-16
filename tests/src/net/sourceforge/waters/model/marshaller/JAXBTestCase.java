//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: JAXBTestCase.java,v 1.5 2006-03-16 04:44:46 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.net.URL;
import java.io.File;
import java.io.IOException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.junit.WatersTestCase;


public abstract class JAXBTestCase<D extends DocumentProxy>
  extends WatersTestCase
{

  //#########################################################################
  //# Utilities
  protected D testParse(final String name)
    throws Exception
  {
    return testParse(getInputDirectory(), name);
  }

  protected D testParse(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getInputDirectory(), dirname);
    return testParse(dir, name);
  }

  protected D testParse(final String dirname1,
                        final String dirname2,
                        final String name)
    throws Exception
  {
    final File dir1 = new File(getInputDirectory(), dirname1);
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
    final ProxyPrinter printer = getPrinter();
    printer.pprint(proxy);
    printer.flush();
    return proxy;
  }

  protected D testMarshal(final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    return testMarshal(getInputDirectory(), name);
  }

  protected D testMarshal(final String dirname, final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir = new File(getInputDirectory(), dirname);
    return testMarshal(dir, name);
  }

  protected D testMarshal(final String dirname1,
                          final String dirname2,
                          final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final File dir1 = new File(getInputDirectory(), dirname1);
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
    final DocumentManager<D> manager = getDocumentManager();
    final D proxy1 = manager.load(inuri);
    manager.saveAs(proxy1, outfilename);
    final URI outuri = outfilename.toURI();
    final D proxy2 = manager.load(outuri);
    assertTrue("Structure changed after marshalling!",
               proxy1.equals(proxy2));
    assertTrue("Structure changed after marshalling!",
               proxy2.equals(proxy1));
    assertTrue("Geometry information changed after marshalling!",
               proxy1.equalsWithGeometry(proxy2));
    assertTrue("Geometry information changed after marshalling!",
               proxy2.equalsWithGeometry(proxy1));
    return proxy1;
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
    final URL url = JAXBTestCase.class.getResource(infilename);
    final DocumentManager<D> manager = getDocumentManager();
    final D proxy1 = manager.load(url);
    final File truefile = new File(getInputDirectory(), infile.toString());
    final D proxy2 = manager.load(truefile);
    assertTrue("Structure in JAR differs from file!",
               proxy1.equals(proxy2));
    assertTrue("Structure in JAR differs from file!",
               proxy2.equals(proxy1));
    assertTrue("Geometry information in JAR differs from file!",
               proxy1.equalsWithGeometry(proxy2));
    assertTrue("Geometry information in JAR differs from file!",
               proxy2.equalsWithGeometry(proxy1));
    return proxy1;
  }

  protected void testHandcraft(final String subdirname, final D handcrafted)
    throws Exception
  {
    checkIntegrity(handcrafted);
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final String name = handcrafted.getName();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File fullindir = new File(getInputDirectory(), subdirname);
    final File infilename = new File(fullindir, extname);
    final File outfilename = new File(getOutputDirectory(), extname);
    final URI inuri = infilename.toURI();
    final URI outuri = outfilename.toURI();
    marshaller.marshal(handcrafted, outfilename);
    final D parsed1 = unmarshaller.unmarshal(outuri);
    assertTrue("Constructed structure differs from parsed-back!",
               handcrafted.equals(parsed1));
    assertTrue("Constructed structure differs from parsed-back!",
               parsed1.equals(handcrafted));
    assertTrue("Constructed geometry info differs from parsed-back!",
               handcrafted.equalsWithGeometry(parsed1));
    assertTrue("Constructed geometry info differs from parsed-back!",
               parsed1.equalsWithGeometry(handcrafted));
    final D parsed2 = unmarshaller.unmarshal(inuri);
    assertTrue("Constructed structure differs from expected in file!",
               handcrafted.equals(parsed2));
    assertTrue("Constructed structure differs from expected in file!",
               parsed2.equals(handcrafted));
    assertTrue("Constructed geometry info differs from expected in file!",
               handcrafted.equalsWithGeometry(parsed2));
    assertTrue("Constructed geometry info differs from expected in file!",
               parsed2.equalsWithGeometry(handcrafted));
  }

  protected D testClone(final String name)
    throws Exception
  {
    return testClone(getInputDirectory(), name);
  }

  protected D testClone(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getInputDirectory(), dirname);
    return testClone(dir, name);
  }

  protected D testClone(final String dirname1,
                        final String dirname2,
                        final String name)
    throws Exception
  {
    final File dir1 = new File(getInputDirectory(), dirname1);
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
    return cloned;
  }


  //#########################################################################
  //# Creating a Document Manager
  protected DocumentManager<D> getDocumentManager()
  {
    if (mDocumentManager == null) {
      final ProxyMarshaller<D> marshaller = getProxyMarshaller();
      final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
      mDocumentManager = new DocumentManager<D>();
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
  protected abstract void checkIntegrity(D document) throws Exception;


  //#########################################################################
  //# Provided by Subclasses
  private DocumentManager<D> mDocumentManager;

}
