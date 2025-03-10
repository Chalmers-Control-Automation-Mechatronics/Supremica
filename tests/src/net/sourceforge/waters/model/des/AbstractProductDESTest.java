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

package net.sourceforge.waters.model.des;

import java.io.PrintWriter;

import net.sourceforge.waters.model.marshaller.AbstractXMLTest;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractProductDESTest
  extends AbstractXMLTest<ProductDESProxy>
{

  //#########################################################################
  //# Test Cases
  public void testParse_small_factory_2()
    throws Exception
  {
    testParse("handwritten", "small_factory_2");
  }

  public void testParse_blocked_event_opt()
    throws Exception
  {
    testParse("tests", "compiler", "efsm", "blocked_event-opt");
  }


  public void testJar_small_factory_2()
    throws Exception
  {
    testParse("handwritten", "small_factory_2");
  }


  public void testMarshal_small_factory_2()
    throws Exception
  {
    testMarshal("handwritten", "small_factory_2");
  }

  public void testMarshal_blocked_event_opt()
    throws Exception
  {
    testMarshal("tests", "compiler", "efsm", "blocked_event-opt");
  }

  public void testMarshal_koordwsp()
    throws Exception
  {
    testMarshal("valid", "central_locking", "koordwsp");
  }


  public void testSerialize_small_factory_2()
    throws Exception
  {
    testSerialize("handwritten", "small_factory_2");
  }

  public void testSerialize_blocked_event_opt()
    throws Exception
  {
    testSerialize("tests", "compiler", "efsm", "blocked_event-opt");
  }

  public void testSerialize_koordwsp()
    throws Exception
  {
    testSerialize("valid", "central_locking", "koordwsp");
  }


  public void testClone_small_factory_2()
    throws Exception
  {
    testClone("handwritten", "small_factory_2");
  }

  public void testClone_blocked_event_opt()
    throws Exception
  {
    testClone("tests", "compiler", "efsm", "blocked_event-opt");
  }

  public void testClone_koordwsp()
    throws Exception
  {
    testClone("valid", "central_locking", "koordwsp");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class AbstractXMLTest<ProductDESProxy>
  @Override
  protected ProxyMarshaller<ProductDESProxy> getProxyMarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyUnmarshaller<ProductDESProxy> getProxyUnmarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  @Override
  protected ProductDESIntegrityChecker getIntegrityChecker()
  {
    return ProductDESIntegrityChecker.getInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mMarshaller = new SAXProductDESMarshaller(factory);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ProductDESProxyPrinter(writer);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ProductDESProxyFactory getProductDESProxyFactory();


  //#########################################################################
  //# Data Members
  private SAXProductDESMarshaller mMarshaller;
  private ProxyPrinter mPrinter;

}
