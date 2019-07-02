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

package net.sourceforge.waters.model.des;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import net.sourceforge.waters.model.marshaller.AbstractXMLTest;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXCounterExampleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractXMLCounterExampleTest
  extends AbstractXMLTest<CounterExampleProxy>
{

  //#########################################################################
  //# Test Cases
  public void testParse_emptytrace()
    throws Exception
  {
    final ProductDESProxy des = getProductDES("handwritten", "small_factory_2");
    testParseCounterexample(des, "handwritten", "emptytrace");
  }

  public void testMarshal_emptytrace()
    throws Exception
  {
    final ProductDESProxy des = getProductDES("handwritten", "small_factory_2");
    testMarshalCounterexample(des, "handwritten", "emptytrace");
  }

  public void testFail_emptytrace()
    throws Exception
  {
    try {
      final ProductDESProxy des = getProductDES("handwritten", "machine");
      testParseCounterexample(des, "handwritten", "emptytrace");
      fail("Expected WatersUnmarshalException not caught!");
    } catch (final WatersUnmarshalException exception) {
      // OK
    }
  }

  public void testParse_small_factory_2__uncont1()
    throws Exception
  {
    final ProductDESProxy des = getProductDES("handwritten", "small_factory_2");
    testParseCounterexample(des, "handwritten", "small_factory_2-uncont1");
  }

  public void testMarshal_small_factory_2__uncont1()
    throws Exception
  {
    final ProductDESProxy des = getProductDES("handwritten", "small_factory_2");
    testMarshalCounterexample(des, "handwritten", "small_factory_2-uncont1");
  }

  public void testParse_loop()
    throws Exception
  {
    testParseCounterexample("tests", "nasty", "the_vicious_loop1");
  }

  public void testMarshal_loop()
    throws Exception
  {
    testMarshalCounterexample("tests", "nasty", "the_vicious_loop1");
  }

  public void testParse_dual()
    throws Exception
  {
    testParseCounterexample("tests", "diagnosability", "notDiag_2");
  }

  public void testMarshal_dual()
    throws Exception
  {
    testMarshalCounterexample("tests", "diagnosability", "notDiag_2");
  }

  @Override
  public void testParseAll()
  {
  }

  @Override
  public void testMarshalAll()
  {
  }


  //#########################################################################
  //# Counterexample Test Framework
  protected void testParseCounterexample(final String... path)
    throws Exception
  {
    final ProductDESProxy des = getProductDES(path);
    testParseCounterexample(des, path);

  }

  protected CounterExampleProxy testParseCounterexample
    (final ProductDESProxy des, final String... path)
    throws Exception
  {
    final File file = getInputFile(path);
    final URI uri = file.toURI();
    final CounterExampleProxy ce =
      mCounterExampleMarshaller.unmarshal(uri, des);
    checkIntegrity(ce);
    checkPrint(ce);
    return ce;
  }

  protected void testMarshalCounterexample(final String... path)
    throws Exception
  {
    final ProductDESProxy des = getProductDES(path);
    testMarshalCounterexample(des, path);
  }

  protected void testMarshalCounterexample
    (final ProductDESProxy des, final String... path)
    throws Exception
  {
    final File inFile = getInputFile(path);
    final URI inURI = inFile.toURI();
    final String name = inFile.getName();
    final File outFile = new File(getOutputDirectory(), name);
    final CounterExampleProxy inCE =
      mCounterExampleMarshaller.unmarshal(inURI, des);
    checkIntegrity(inCE);
    mCounterExampleMarshaller.marshal(inCE, outFile);
    final URI outURI = outFile.toURI();
    final CounterExampleProxy outCE =
      mCounterExampleMarshaller.unmarshal(outURI, des);
    checkIntegrity(outCE);
    assertProxyEquals("Structure changed after marshalling!", inCE, outCE);
    assertProxyEquals("Structure changed after marshalling!", outCE, inCE);
  }

  protected ProductDESProxy getProductDES(final String... path)
    throws WatersUnmarshalException, IOException
  {
    final String ext = mProductDESMarshaller.getDefaultExtension();
    final File file = getInputFile(path, ext);
    final URI uri = file.toURI();
    return mProductDESMarshaller.unmarshal(uri);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  @Override
  protected ProxyMarshaller<CounterExampleProxy> getProxyMarshaller()
  {
    return mCounterExampleMarshaller;
  }

  @Override
  protected ProxyUnmarshaller<CounterExampleProxy> getProxyUnmarshaller()
  {
    return mCounterExampleMarshaller;
  }

  @Override
  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  @Override
  protected CounterExampleIntegrityChecker getIntegrityChecker()
  {
    return CounterExampleIntegrityChecker.getInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mCounterExampleMarshaller = new SAXCounterExampleMarshaller(factory);
    mProductDESMarshaller = new SAXProductDESMarshaller(factory);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ProductDESProxyPrinter(writer);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mCounterExampleMarshaller = null;
    mProductDESMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private SAXCounterExampleMarshaller mCounterExampleMarshaller;
  private SAXProductDESMarshaller mProductDESMarshaller;
  private ProxyPrinter mPrinter;

}
