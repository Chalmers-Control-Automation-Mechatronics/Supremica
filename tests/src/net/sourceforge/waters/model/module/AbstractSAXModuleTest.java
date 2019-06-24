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

package net.sourceforge.waters.model.module;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.AbstractXMLTest;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractSAXModuleTest
  extends AbstractXMLTest<ModuleProxy>
{

  //#########################################################################
  //# Printing Test Cases
  public void testPrint_hisc0_high()
    throws Exception
  {
    testPrint("despot", "testHISC", "hisc0_high");
  }

  public void testPrint_buffer_norm()
    throws Exception
  {
    testPrint("efa", "buffer_norm");
  }

  public void testPrint_transferline_efa()
    throws Exception
  {
    testPrint("efa", "transferline_efa");
  }

  public void testPrint_buffer_sf1()
    throws Exception
  {
    testPrint("handwritten", "buffer_sf1");
  }

  public void testPrint_cell()
    throws Exception
  {
    testPrint("handwritten", "cell");
  }

  public void testPrint_machine()
    throws Exception
  {
    testPrint("handwritten", "machine");
  }

  public void testPrint_sensoractuator1()
    throws Exception
  {
    testPrint("handwritten", "sensoractuator1");
  }

  public void testPrint_small_factory_2()
    throws Exception
  {
    testPrint("handwritten", "small_factory_2");
  }

  public void testPrint_small_factory_n()
    throws Exception
  {
    testPrint("handwritten", "small_factory_n");
  }

  public void testPrint_tictactoe()
    throws Exception
  {
    testPrint("handwritten", "tictactoe");
  }

  public void testPrint_tictactoe_incomplete()
    throws Exception
  {
    testPrint("handwritten", "tictactoe_incomplete");
  }

  public void testPrint_error7_small()
    throws Exception
  {
    testPrint("tests", "compiler", "efsm", "error7_small");
  }

  public void testPrint_colours()
    throws Exception
  {
    testPrint("tests", "compiler", "graph", "colours");
  }

  public void testPrint_nodegroup1()
    throws Exception
  {
    testPrint("tests", "compiler", "groupnode", "nodegroup1");
  }

  public void testPrint_nodegroup2()
    throws Exception
  {
    testPrint("tests", "compiler", "groupnode", "nodegroup2");
  }

  public void testPrint_batch_tank_vout()
    throws Exception
  {
    testPrint("tests", "efa", "batch_tank_vout");
  }

  public void testPrint_eventaliases()
    throws Exception
  {
    testPrint("tests", "nasty", "eventaliases");
  }

  public void testPrint_marked_value()
    throws Exception
  {
    testPrint("tests", "nasty", "marked_value");
  }


  public void testTime_2linkalt_batch()
    throws Exception
  {
    testTime("tests", "supervisor_reduction", "2linkalt_batch.wmod");
  }

  public void testTime_IPC_cswitch()
    throws Exception
  {
    testTime("tests", "supervisor_reduction", "IPC_cswitch.wmod");
  }

  public void testTime_ims()
    throws Exception
  {
    testTime("tests", "supervisor_reduction", "ims.wmod");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.AbstractXMLTest
  @Override
  protected ProxyMarshaller<ModuleProxy> getProxyMarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyUnmarshaller<ModuleProxy> getProxyUnmarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyUnmarshaller<ModuleProxy> getAltProxyUnmarshaller()
  {
    return mAltMarshaller;
  }

  @Override
  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  @Override
  protected ModuleIntegrityChecker getIntegrityChecker()
  {
    return ModuleIntegrityChecker.getModuleIntegrityCheckerInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mMarshaller = new SAXModuleMarshaller(factory, optable);
    mAltMarshaller = new JAXBModuleMarshaller(factory, optable);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ModuleProxyPrinter(writer);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mMarshaller = null;
    mAltMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Timing
  private void testTime(final String... path)
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final SAXModuleMarshaller saxMarshaller =
      new SAXModuleMarshaller(factory, optable);
    final JAXBModuleMarshaller jaxbMarshaller =
      new JAXBModuleMarshaller(factory, optable);
    final String ext = saxMarshaller.getDefaultExtension();
    final File file = getInputFile(path, ext);
    System.out.println(file.getName() + " ...");
    final URI uri = file.toURI();
    saxMarshaller.unmarshal(uri);
    jaxbMarshaller.unmarshal(uri);
    checkTime(saxMarshaller, uri);
    checkTime(jaxbMarshaller, uri);
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getModuleProxyFactory();
  protected abstract ModuleProxyFactory getAlternateModuleProxyFactory();


  //#########################################################################
  //# Data Members
  private SAXModuleMarshaller mMarshaller;
  private JAXBModuleMarshaller mAltMarshaller;
  private ProxyPrinter mPrinter;

}
