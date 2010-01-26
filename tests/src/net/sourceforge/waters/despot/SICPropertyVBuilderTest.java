//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   DESpotImporterTest
//###########################################################################
//# $Id: DESpotImporterTest.java 5044 2009-12-30 20:14:13Z robi $
//###########################################################################

package net.sourceforge.waters.despot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
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

  public void testConflictChecker_parManEg_I_mfb_lowlevel() throws Exception
  {
    testConflictChecker("tests", "hisc", "parManEg_I_mfb_lowlevel", true);
  }

  public void testConflictChecker_rhone_subsystem1_ld_failsic5()
      throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld_failsic5", false);
  }

  public void testConflictChecker_rhone_subsystem1_ld() throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld", true);
  }

  // SimpleManufacturingExample
  public void testConflictChecker_Manuf_Cells() throws Exception
  {
    testConflictChecker("despot", "simpleManufacturingExample", "Manuf-Cells",
                        true);
  }

  // testHISC
  public void testConflictChecker_hisc0_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc0_low1", true);
  }

  public void testConflictChecker_hisc0_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc0_low2", true);
  }

  // testHISC1
  public void testConflictChecker_hisc1_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc1_low1", true);
  }

  public void testConflictChecker_hisc1_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc1_low2", true);
  }

  // testHISC10
  public void testConflictChecker_hisc10_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc10_low1", true);
  }

  public void testConflictChecker_hisc12_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc12_low1", true);
  }

  public void testConflictChecker_hisc12_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc12_low2", true);
  }

  /*
   * public void testConflictChecker_hisc13_low1() throws Exception {
   * testConflictChecker("despot", "testHISC", "hisc13_low1", false); }
   *
   * public void testConflictChecker_hisc13_low2() throws Exception {
   * testConflictChecker("despot", "testHISC", "hisc13_low2", true); }
   */

  public void testConflictChecker_hisc14_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc14_low1", true);
  }

  public void testConflictChecker_hisc14_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc14_low2", true);
  }

  public void testConflictChecker_hisc2_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc2_low1", true);
  }

  public void testConflictChecker_hisc2_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc2_low2", true);
  }

  public void testConflictChecker_hisc3_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc3_low2", true);
  }

  public void testConflictChecker_hisc7_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc7_low2", true);
  }

  public void testConflictChecker_hisc8_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc8_low2", true);
  }

  public void testConflictChecker_hisc9_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc9_low2", false);
  }

  public void testConflictChecker_aip3_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as1", true);
  }

  public void testConflictChecker_aip3_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as2", true);
  }

  public void testConflictChecker_aip3_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as3", true);
  }

  public void testConflictChecker_aip3_syn_io() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "io", true);
  }

  public void testConflictChecker_aip3_syn_tu1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu1", true);
  }

  public void testConflictChecker_aip3_syn_tu2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu2", true);
  }

  public void testConflictChecker_aip3_syn_tu3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu3", true);
  }

  public void testConflictChecker_aip3_syn_tu4() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu4", true);
  }

  public void testConflictChecker_aip3_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "as1", true);
  }

  public void testConflictChecker_aip3_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "as2", true);
  }

  public void testConflictChecker_aip3_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "as3", true);
  }

  public void testConflictChecker_aip3_veri_io() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "io", true);
  }

  public void testConflictChecker_aip3_veri_tu1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "tu1", true);
  }

  public void testConflictChecker_aip3_veri_tu2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "tu2", true);
  }

  public void testConflictChecker_aip3_veri_tu3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_veri", "tu3", true);
  }

  public void testConflictChecker_aip3_veri_tu4() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu4", true);
  }

  public void testConflictChecker_aip5_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "as1", true);
  }

  public void testConflictChecker_aip5_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "as2", true);
  }

  public void testConflictChecker_aip5_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "as3", true);
  }

  public void testConflictChecker_aip5_syn_io() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "io", true);
  }

  public void testConflictChecker_aip5_syn_tu1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "tu1", true);
  }

  public void testConflictChecker_aip5_syn_tu2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "tu2", true);
  }

  public void testConflictChecker_aip5_syn_tu3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_syn", "tu3", true);
  }

  public void testConflictChecker_aip5_syn_tu4() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu4", true);
  }

  public void testConflictChecker_aip5_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "as1", true);
  }

  public void testConflictChecker_aip5_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "as2", true);
  }

  public void testConflictChecker_aip5_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "as3", true);
  }

  public void testConflictChecker_aip5_veri_io() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "io", true);
  }

  public void testConflictChecker_aip5_veri_tu1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "tu1", true);
  }

  public void testConflictChecker_aip5_veri_tu2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "tu2", true);
  }

  public void testConflictChecker_aip5_veri_tu3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "tu3", true);
  }

  public void testConflictChecker_aip5_veri_tu4() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip5_veri", "tu4", true);
  }

  public void testConflictChecker_maip3_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip1", true);
  }

  public void testConflictChecker_maip3_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip2", true);
  }

  public void testConflictChecker_maip3_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip3", true);
  }

  public void testConflictChecker_maip3_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip1", true);
  }

  public void testConflictChecker_maip3_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip2", true);
  }

  public void testConflictChecker_maip3_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip3", true);
  }

  public void testConflictChecker_maip5_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip1", true);
  }

  public void testConflictChecker_maip5_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip2", true);
  }

  public void testConflictChecker_maip5_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip3", true);
  }

  public void testConflictChecker_maip5_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip1", true);
  }

  public void testConflictChecker_maip5_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip2", true);
  }

  public void testConflictChecker_maip5_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip3", true);
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

  public void testIntegrity_rhone_subsystem1_ld_failsic5() throws Exception
  {
    testIntegrity("tests", "hisc", "rhone_subsystem1_ld_failsic5");
  }

  public void testIntegrity_rhone_subsystem1_ld() throws Exception
  {
    testIntegrity("tests", "hisc", "rhone_subsystem1_ld");
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

  public void testIntegrity_aip3_veri_as1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "as1");
  }

  public void testIntegrity_aip3_veri_as2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "as2");
  }

  public void testIntegrity_aip3_veri_as3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "as3");
  }

  public void testIntegrity_aip3_veri_io() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "io");
  }

  public void testIntegrity_aip3_veri_tu1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "tu1");
  }

  public void testIntegrity_aip3_veri_tu2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "tu2");
  }

  public void testIntegrity_aip3_veri_tu3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_veri", "tu3");
  }

  public void testIntegrity_aip3_veri_tu4() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu4");
  }

  public void testIntegrity_aip5_syn_as1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "as1");
  }

  public void testIntegrity_aip5_syn_as2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "as2");
  }

  public void testIntegrity_aip5_syn_as3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "as3");
  }

  public void testIntegrity_aip5_syn_io() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "io");
  }

  public void testIntegrity_aip5_syn_tu1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "tu1");
  }

  public void testIntegrity_aip5_syn_tu2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "tu2");
  }

  public void testIntegrity_aip5_syn_tu3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_syn", "tu3");
  }

  public void testIntegrity_aip5_syn_tu4() throws Exception
  {
    testIntegrity("despot", "song_aip/aip3_syn", "tu4");
  }

  public void testIntegrity_aip5_veri_as1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "as1");
  }

  public void testIntegrity_aip5_veri_as2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "as2");
  }

  public void testIntegrity_aip5_veri_as3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "as3");
  }

  public void testIntegrity_aip5_veri_io() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "io");
  }

  public void testIntegrity_aip5_veri_tu1() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "tu1");
  }

  public void testIntegrity_aip5_veri_tu2() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "tu2");
  }

  public void testIntegrity_aip5_veri_tu3() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "tu3");
  }

  public void testIntegrity_aip5_veri_tu4() throws Exception
  {
    testIntegrity("despot", "song_aip/aip5_veri", "tu4");
  }

  // #########################################################################

  void testIntegrity(final String group, final String subdir, final String name)
      throws Exception
  {
    final String wdesext = mProductDESMarshaller.getDefaultExtension();
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      final File outfilename =
          new File(mOutputDirectory, name.replace(':', '_') + "_"
              + answer.getName().replace(':', '_') + wdesext);
      mProductDESMarshaller.marshal(modifiedDES, outfilename);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);
    }
  }

  void testConflictChecker(final String group, final String subdir,
                           final String name, final boolean expectedResult)
      throws Exception
  {
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      final EventProxy defaultMark = mBuilder.getMarkingProposition();
      final EventProxy preconditionMark = mBuilder.getGeneralisedPrecondition();
      final MonolithicConflictChecker conflictChecker =
          new MonolithicConflictChecker(modifiedDES, defaultMark,
              preconditionMark, mProductDESFactory);
      final boolean result = conflictChecker.run();

      if (!result) {
        System.out.println("CONFLICTING");
        System.out.println("Counterexample:");
        final ConflictTraceProxy counterex =
            conflictChecker.getCounterExample();
        System.out.println(counterex.toString());
      }
      assertEquals("Wrong result from model checker: got " + result
          + " but should have been " + expectedResult + "!", expectedResult,
                   result);

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
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);

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

  private ProductDESProxy loadProductDES(final URI unmodifiedDESURI)
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

  private Collection<EventProxy> getModelAnswerEvents(final String group,
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
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = getWatersInputRoot();
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBuilder = new SICPropertyVBuilder(mProductDESFactory);
  }

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
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;
  private SICPropertyVBuilder mBuilder;
}
