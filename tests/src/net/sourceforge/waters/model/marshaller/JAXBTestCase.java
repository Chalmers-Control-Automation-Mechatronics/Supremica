//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: JAXBTestCase.java,v 1.3 2005-12-14 13:13:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

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
    final D proxy = unmarshaller.unmarshal(filename);
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
    final File outfilename = new File(getOutputDirectory(), extname);
    return testMarshal(infilename, outfilename);
  }

  protected D testMarshal(final File infilename, final File outfilename)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    final ProxyMarshaller<D> marshaller = getProxyMarshaller();
    final ProxyUnmarshaller<D> unmarshaller = getProxyUnmarshaller();
    final D proxy1 = unmarshaller.unmarshal(infilename);
    marshaller.marshal(proxy1, outfilename);
    final D proxy2 = unmarshaller.unmarshal(outfilename);
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
    marshaller.marshal(handcrafted, outfilename);
    final D parsed1 = unmarshaller.unmarshal(outfilename);
    assertTrue("Constructed structure differs from parsed-back!",
               handcrafted.equals(parsed1));
    assertTrue("Constructed structure differs from parsed-back!",
               parsed1.equals(handcrafted));
    assertTrue("Constructed geometry info differs from parsed-back!",
               handcrafted.equalsWithGeometry(parsed1));
    assertTrue("Constructed geometry info differs from parsed-back!",
               parsed1.equalsWithGeometry(handcrafted));
    final D parsed2 = unmarshaller.unmarshal(infilename);
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
    final D proxy = unmarshaller.unmarshal(filename);
    final DocumentProxy cloneddoc = proxy.clone();
    final Class<D> clazz = unmarshaller.getDocumentClass();
    final D cloned = clazz.cast(cloneddoc);
    checkIntegrity(cloned);
    return cloned;
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProxyMarshaller<D> getProxyMarshaller();
  protected abstract ProxyUnmarshaller<D> getProxyUnmarshaller();
  protected abstract ProxyPrinter getPrinter();
  protected abstract void checkIntegrity(D document) throws Exception;

}
