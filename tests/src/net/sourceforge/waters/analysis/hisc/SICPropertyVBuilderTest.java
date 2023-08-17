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

package net.sourceforge.waters.analysis.hisc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class SICPropertyVBuilderTest extends AbstractWatersTest
{

  // #########################################################################
  // # tests the model built compared to a manually created model as well as
  // testing the integrity

  public void testBuild_hisc0_low1() throws Exception
  {
    testBuild("despot", "testHISC", "hisc0_low1");
  }

  // this model only has request and answer events so does not require the 3rd
  // state of the "test" automaton which must be created
  public void testBuild_hisc0_low2() throws Exception
  {
    testBuild("despot", "testHISC", "hisc0_low2");
  }

  public void testBuild_hisc1_low1() throws Exception
  {
    testBuild("despot", "testHISC", "hisc1_low1");
  }

  // SimpleManufacturingExample
  public void testIntegrity_Manuf_Cells() throws Exception
  {
    testIntegrity("despot", "simpleManufacturingExample", "Manuf-Cells");
  }

  // testHISC
  public void testIntegrity_hisc0_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc0_low1");
  }

  public void testIntegrity_hisc0_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc0_low2");
  }

  // testHISC1
  public void testIntegrity_hisc1_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc1_low1");
  }

  public void testIntegrity_hisc1_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc1_low2");
  }

  // testHISC10
  public void testIntegrity_hisc10_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc10_low1");
  }

  public void testIntegrity_hisc12_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc12_low1");
  }

  public void testIntegrity_hisc12_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc12_low2");
  }

  public void testIntegrity_hisc13_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc13_low1");
  }

  public void testIntegrity_hisc13_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc13_low2");
  }

  public void testIntegrity_hisc14_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc14_low1");
  }

  public void testIntegrity_hisc14_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc14_low2");
  }

  public void testIntegrity_hisc2_low1() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc2_low1");
  }

  public void testIntegrity_hisc2_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc2_low2");
  }

  public void testIntegrity_hisc3_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc3_low2");
  }

  public void testIntegrity_hisc7_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc7_low2");
  }

  public void testIntegrity_hisc8_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc8_low2");
  }

  public void testIntegrity_hisc9_low2() throws Exception
  {
    testIntegrity("despot", "testHISC", "hisc9_low2");
  }

  public void testIntegrity_parManEg_I_mfb_lowlevel() throws Exception
  {
    testIntegrity("tests", "hisc", "parManEg_I_mfb_lowlevel");
  }

  public void testIntegrity_parManEg_I_mfb_lowlevel_multiAnswers_noInterface()
    throws Exception
  {
    testIntegrity("tests", "hisc",
                  "parManEg_I_mfb_lowlevel_multiAnswers_noInterface");
  }

  public void testIntegrity_aip1sub1ld_failsic5() throws Exception
  {
    testIntegrity("tests", "hisc", "aip1sub1ld_failsic5");
  }

  public void testIntegrity_aip1sub1ld() throws Exception
  {
    testIntegrity("tests", "hisc", "aip1sub1ld");
  }

  public void testIntegrity_as1() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "as1");
  }

  public void testIntegrity_as2() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "as2");
  }

  public void testIntegrity_as3() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "as3");
  }

  public void testIntegrity_tu1() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "tu1");
  }

  public void testIntegrity_tu2() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "tu2");
  }

  public void testIntegrity_tu3() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "tu3");
  }

  public void testIntegrity_tu4() throws Exception
  {
    testIntegrity("despot", "output-aip_leducversion-old", "tu4");
  }

  public void testIntegrity_aip3_syn_as1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "as1");
  }

  public void testIntegrity_aip3_syn_as2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "as2");
  }

  public void testIntegrity_aip3_syn_as3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "as3");
  }

  public void testIntegrity_aip3_syn_io() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "io");
  }

  public void testIntegrity_aip3_syn_tu1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu1");
  }

  public void testIntegrity_aip3_syn_tu2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu2");
  }

  public void testIntegrity_aip3_syn_tu3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu3");
  }

  public void testIntegrity_aip3_syn_tu4() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu4");
  }

  // #########################################################################

  void testIntegrity(final String group, final String subdir, final String name)
      throws Exception
  {
    final String wdesext = mProductDESMarshaller.getDefaultExtension();
    final String wmodext = mModuleMarshaller.getDefaultExtension();
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createSIC5Model(answer);
      final String fname = name.replace(':', '_') + "_" +
                           answer.getName().replace(':', '_');
      final File desfilename =
          new File(mOutputDirectory, fname + wdesext);
      mProductDESMarshaller.marshal(modifiedDES, desfilename);
      final ModuleProxy module = mImporter.importModule(modifiedDES);
      final File modfilename =
        new File(mOutputDirectory, fname + wmodext);
      mModuleMarshaller.marshal(module, modfilename);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);
    }
  }

  void testBuild(final String group, final String subdir, final String name)
      throws Exception
  {
    final File groupname = new File(mInputDirectory, group);
    final File indirname = new File(groupname, subdir);
    final String wmodext = mModuleMarshaller.getDefaultExtension();
    final String wdesext = mProductDESMarshaller.getDefaultExtension();
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);

    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createSIC5Model(answer);

      final File outfilename =
          new File(mOutputDirectory, name + "_" + answer.getName() + wdesext);
      mProductDESMarshaller.marshal(modifiedDES, outfilename);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);

      final File expectfilename =
          new File(indirname, name + "_EXPECTED_" + answer.getName() + wmodext);
      final URI expecteddesURI = expectfilename.toURI();
      final ProductDESProxy expectedDES = loadProductDES(expecteddesURI);

      final File expectDESfilename =
          new File(mOutputDirectory, name + "_EXPECTED_" + answer.getName()
              + wdesext);
      mProductDESMarshaller.marshal(expectedDES, expectDESfilename);
      assertProductDESProxyEquals("Unexpected contents for product DES '"
          + name + "' with answer '" + answer.getName() + "'!", modifiedDES,
                                  expectedDES);
    }
  }

  protected ProductDESProxy loadProductDES(final URI unmodifiedDESURI)
      throws WatersUnmarshalException, IOException, EvalException
  {
    final DocumentProxy doc = mDocumentManager.load(unmodifiedDESURI);
    ProductDESProxy des;
    if (doc instanceof ProductDESProxy) {
      des = (ProductDESProxy) doc;
    } else {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
          new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
      des = compiler.compile();
    }
    return des;
  }

  protected Collection<EventProxy> getModelAnswerEvents(final String group,
                                                        final String subdir,
                                                        final String name)
      throws Exception
  {
    final File groupname = new File(mInputDirectory, group);
    final File indirname = new File(groupname, subdir);
    final String wmodext = mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(indirname, name + wmodext);
    final URI unmodifiedDESURI = infilename.toURI();

    final ProductDESProxy originalDES = loadProductDES(unmodifiedDESURI);
    mBuilder.setInputModel(originalDES);

    return mBuilder.getAnswerEvents();
  }

  // #########################################################################
  // # Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = getWatersInputRoot();
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new SAXModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new SAXProductDESMarshaller(mProductDESFactory);
    mImporter = new ProductDESImporter(moduleFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBuilder = new SICPropertyBuilder(mProductDESFactory);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mBuilder = null;
    super.tearDown();
  }

  // #########################################################################
  // # Data Members

  private File mInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private SAXModuleMarshaller mModuleMarshaller;
  private SAXProductDESMarshaller mProductDESMarshaller;
  private ProductDESImporter mImporter;
  private DocumentManager mDocumentManager;
  private SICPropertyBuilder mBuilder;
}
