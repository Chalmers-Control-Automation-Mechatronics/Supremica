//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.junit
//# CLASS:   ProductDESTest
//###########################################################################
//# $Id: ProductDESTest.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.des.ProductDESMarshaller;


public class ProductDESTest extends JAXBTestCase
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(ProductDESTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testParse_small_factory_2()
    throws JAXBException, ModelException, IOException
  {
    testParse("small_factory_2");
  }

  public void testMarshal_small_factory_2()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("small_factory_2");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  ProxyMarshaller getProxyMarshaller()
  {
    return mMarshaller;
  }

  File getInputDirectory()
  {
    return mInputDirectory;
  }

  File getOutputDirectory()
  {
    return mOutputDirectory;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  { 
    super.setUp();
    mMarshaller = new ProductDESMarshaller();
    mInputDirectory = new File("examples", "handwritten");
    mOutputDirectory = new File("logs", "des");
  }

  protected void tearDown()
  {
    mMarshaller = null;
    mInputDirectory = null;
    mOutputDirectory = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ProxyMarshaller mMarshaller;
  private File mInputDirectory;
  private File mOutputDirectory;

}
