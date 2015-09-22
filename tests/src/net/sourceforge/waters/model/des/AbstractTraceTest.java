//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.io.IOException;
import java.io.PrintWriter;

import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.AbstractJAXBTest;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractTraceTest extends AbstractJAXBTest<TraceProxy>
{

  //#########################################################################
  //# Test Cases
  public void testParse_emptytrace()
    throws Exception
  {
    testParse("handwritten", "emptytrace");
  }

  public void testJar_emptytrace()
    throws Exception
  {
    testParse("handwritten", "emptytrace");
  }

  public void testMarshal_emptytrace()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("handwritten", "emptytrace");
  }

  public void testParse_small_factory_2__uncont1()
    throws Exception
  {
    testParse("handwritten", "small_factory_2-uncont1");
  }

  public void testJar_small_factory_2__uncont1()
    throws Exception
  {
    testParse("handwritten", "small_factory_2-uncont1");
  }

  public void testMarshal_small_factory_2__uncont1()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("handwritten", "small_factory_2-uncont1");
  }

  public void testParse_looptrace()
    throws Exception
  {
    testParse("tests", "nasty", "the_vicious_loop1");
  }

  public void testMarshal_looptrace()
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    testMarshal("tests", "nasty", "the_vicious_loop1");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  protected ProxyMarshaller<TraceProxy> getProxyMarshaller()
  {
    return mTraceMarshaller;
  }

  protected ProxyUnmarshaller<TraceProxy> getProxyUnmarshaller()
  {
    return mTraceMarshaller;
  }

  protected DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  protected TraceIntegrityChecker getIntegrityChecker()
  {
    return TraceIntegrityChecker.getInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mTraceMarshaller = new JAXBTraceMarshaller(factory);
    mProductDESMarshaller = new JAXBProductDESMarshaller(factory);
    final PrintWriter writer = new PrintWriter(System.out);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mTraceMarshaller);
    mDocumentManager.registerUnmarshaller(mTraceMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mPrinter = new ProductDESProxyPrinter(writer);
  }

  protected void tearDown()
    throws Exception
  {
    mTraceMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private JAXBTraceMarshaller mTraceMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private ProxyPrinter mPrinter;

}








