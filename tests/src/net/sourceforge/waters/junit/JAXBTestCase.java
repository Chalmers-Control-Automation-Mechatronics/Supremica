//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.junit
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: JAXBTestCase.java,v 1.3 2005-05-08 00:27:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.ProxyMarshaller;


abstract class JAXBTestCase
  extends WatersTestCase
{

  //#########################################################################
  //# Utilities
  DocumentProxy testParse(final String name)
    throws Exception
  {
    final ProxyMarshaller marshaller = getProxyMarshaller();
    final String extname = name + marshaller.getDefaultExtension();
    final File filename = new File(getInputDirectory(), extname);
    return testParse(filename);
  }

  DocumentProxy testParse(final File filename)
    throws Exception
  {
    final ProxyMarshaller marshaller = getProxyMarshaller();
    final DocumentProxy proxy = marshaller.unmarshal(filename);
    proxy.pprint(mPrinter);
    mPrinter.flush();
    return proxy;
  }

  DocumentProxy testMarshal(final String name)
    throws Exception
  {
    final ProxyMarshaller marshaller = getProxyMarshaller();
    final String extname = name + marshaller.getDefaultExtension();
    final File infilename = new File(getInputDirectory(), extname);
    final File outfilename = new File(getOutputDirectory(), extname);
    return testMarshal(infilename, outfilename);
  }

  DocumentProxy testMarshal(final File infilename, final File outfilename)
    throws Exception
  {
    final ProxyMarshaller marshaller = getProxyMarshaller();
    final DocumentProxy proxy1 = marshaller.unmarshal(infilename);
    marshaller.marshal(proxy1, outfilename);
    final DocumentProxy proxy2 = marshaller.unmarshal(outfilename);
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

  void testHandcraft(final DocumentProxy handcrafted)
    throws Exception
  {
    final ProxyMarshaller marshaller = getProxyMarshaller();
    final String name = handcrafted.getName();
    final String extname = name + marshaller.getDefaultExtension();
    final File infilename = new File(getInputDirectory(), extname);
    final File outfilename = new File(getOutputDirectory(), extname);
    marshaller.marshal(handcrafted, outfilename);
    final DocumentProxy parsed = marshaller.unmarshal(infilename);
    assertTrue("Constructed structure differs from expected in file!",
	       handcrafted.equals(parsed));
    assertTrue("Constructed structure differs from expected in file!",
	       parsed.equals(handcrafted));
    assertTrue("Constructed geometry info differs from expected in file!",
	       handcrafted.equalsWithGeometry(parsed));
    assertTrue("Constructed geometry info differs from expected in file!",
	       parsed.equalsWithGeometry(handcrafted));
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract ProxyMarshaller getProxyMarshaller();
  abstract File getInputDirectory();
  abstract File getOutputDirectory();


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  { 
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ModelPrinter(writer);
  }

  protected void tearDown()
  { 
    mPrinter = null;
  }


  //#########################################################################
  //# Data Members
  private ModelPrinter mPrinter;

}
