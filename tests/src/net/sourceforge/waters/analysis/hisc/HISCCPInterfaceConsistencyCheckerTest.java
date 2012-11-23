//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters HISC
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   HISCCPInterfaceConsistencyCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * @author Robi Malik
 */

public class HISCCPInterfaceConsistencyCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(HISCCPInterfaceConsistencyCheckerTest.class);
    return suite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ModelVerifier createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new HISCCPInterfaceConsistencyChecker(factory);
  }

  @Override
  protected HISCCPInterfaceConsistencyChecker getModelVerifier()
  {
    return (HISCCPInterfaceConsistencyChecker) super.getModelVerifier();
  }

  @Override
  protected void precheckCounterExample(final TraceProxy trace)
  {
  }

  @Override
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace) throws Exception
  {
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final List<String> accepting =
      Collections.singletonList(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(accepting);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
  }


  //#########################################################################
  //# Test Cases
  // testHISC, low levels
  public void testHISCCP_hisc0_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low1.wmod", true);
  }

  public void testHISCCP_hisc0_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_low2.wmod", true);
  }

  public void testHISCCP_hisc1_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low1.wmod", true);
  }

  public void testHISCCP_hisc1_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_low2.wmod", true);
  }

  public void testHISCCP_hisc2_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low1.wmod", false);
  }

  public void testHISCCP_hisc2_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc2_low2.wmod", true);
  }

  public void testHISCCP_hisc3_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc3_low2.wmod", true);
  }

  public void testHISCCP_hisc7_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc7_low2.wmod", false);
  }

  public void testHISCCP_hisc8_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc8_low2.wmod", false);
  }

  public void testHISCCP_hisc9_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc9_low2.wmod", false);
  }

  public void testHISCCP_hisc10_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc10_low1.wmod", false);
  }

  public void testHISCCP_hisc12_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_low2.wmod", false);
  }

  public void testHISCCP_hisc13_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc13_low1.wmod", false);
  }

  public void testHISCCP_hisc13_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc13_low2.wmod", false);
  }

  public void testHISCCP_hisc14_low1() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low1.wmod", false);
  }

  public void testHISCCP_hisc14_low2() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_low2.wmod", false);
  }


  // testHISC, high levels
  public void testHISCCP_hisc0_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc0_high.wmod", false);
  }

  public void testHISCCP_hisc1_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc1_high.wmod", true);
  }

  public void testHISCCP_hisc4_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc4_high.wmod", false);
  }

  public void testHISCCP_hisc5_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc5_high.wmod", true);
  }

  public void testHISCCP_hisc6_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc6_high.wmod", false);
  }

  public void testHISCCP_hisc8_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc8_high.wmod", true);
  }

  public void testHISCCP_hisc10_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc10_high.wmod", false);
  }

  public void testHISCCP_hisc11_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc11_high.wmod", true);
  }

  public void testHISCCP_hisc12_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc12_high.wmod", true);
  }

  public void testHISCCP_hisc13_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc13_high.wmod", true);
  }

  public void testHISCCP_hisc14_high() throws Exception
  {
    runModelVerifier("despot", "testHISC", "hisc14_high.wmod", true);
  }


  // ParallelManufacturingExample
  public void testHISCCP_parManEg_I_mfb_lowlevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_lowlevel.wmod", true);
  }

  public void testHISCCP_parManEg_I_mfb_lowlevel_multiAnswers()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers.wmod", false);
  }

  public void testHISCCP_parManEg_I_mfb_parManEg_I_mfb_lowlevel_multiAnswers_noInterface()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "parManEg_I_mfb_lowlevel_multiAnswers_noInterface.wmod",
                     false);
  }

  public void testHISCCP_parManEg_I_mfb_middlelevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_middlelevel.wmod", true);
  }

  public void testHISCCP_parManEg_I_mfb_highlevel()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "parManEg_I_mfb_highlevel.wmod", true);
  }

  public void testHISCCP_parManEg_node0()
  throws Exception
  {
    runModelVerifier("despot", "parallelManufacturingExample", "Node0.wmod", true);
  }

  public void testHISCCP_parManEg_node1()
  throws Exception
  {
    runModelVerifier("despot", "parallelManufacturingExample", "Node1.wmod", true);
  }

  public void testHISCCP_parManEg_node4()
  throws Exception
  {
    runModelVerifier("despot", "parallelManufacturingExample", "Node4.wmod", false);
  }


  // Central Locking
  public void testHISCCP_verriegel4ft()
  throws Exception
  {
    runModelVerifier("hisccp", "central_locking", "verriegel4ft.wmod", true);
  }

  public void testHISCCP_verriegel4ftbad()
  throws Exception
  {
    runModelVerifier("hisccp", "central_locking", "verriegel4ft_bad.wmod", false);
  }

  public void testHISCCP_verriegel4ht()
  throws Exception
  {
    runModelVerifier("hisccp", "central_locking", "verriegel4ht.wmod", true);
  }

  public void testHISCCP_verriegel4hisc()
  throws Exception
  {
    runModelVerifier("hisccp", "central_locking", "verriegel4hisc.wmod", true);
  }


  // SimpleManufacturingExample
  public void testHISCCP_SimpleManufMultiLD0()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_multi_ld",
                     "simple_manuf_multi_ld.wmod", true);
  }

  public void testHISCCP_SimpleManufMultiLD1()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_multi_ld",
                     "subsystem.wmod", true);
  }

  public void testHISCCP_SimpleManufMultiLD02()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_multi_ld",
                     "assembly.wmod", true);
  }


  public void testHISCCP_SimpleManufHISCCP0()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_hisccp",
                     "simple_manuf_hisccp.wmod", true);
  }

  public void testHISCCP_SimpleManufHISCCP1()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_hisccp",
                     "subsystem.wmod", true);
  }

  public void testHISCCP_SimpleManufHISCCP1bad()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_hisccp",
                     "subsystem_bad.wmod", false);
  }

  public void testHISCCP_SimpleManufHISCCP2am()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_hisccp",
                     "assembly_machine.wmod", true);
  }

  public void testHISCCP_SimpleManufHISCCP2pm()
  throws Exception
  {
    runModelVerifier("hisccp", "simple_manuf_hisccp",
                     "polishing_machine.wmod", true);
  }


  public void testHISCCP_ManufCell()
  throws Exception
  {
    runModelVerifier("despot", "simpleManufacturingExample",
                     "Manuf-Cells.wmod", false);
  }

  public void testHISCCP_ManufCellCP()
  throws Exception
  {
    runModelVerifier("despot", "simpleManufacturingExample",
                     "manuf_cell_cp.wmod", true);
  }


  // song_aip
  public void testHISCCP_aip3syn_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "as1.wmod", false);
  }

  public void testHISCCP_aip3syn_as1cp() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "as1_cp.wmod", true);
  }

  public void testHISCCP_aip3syn_io() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "io.wmod", false);
  }

  public void testHISCCP_aip3syn_tu1() throws Exception
  {
    runModelVerifier("despot", "song_aip", "aip3_syn", "tu1.wmod", false);
  }


  // tbed_hisc
  public void testHISCCP_tbed_hisc_crane1()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane1.wmod", true);
  }

  public void testHISCCP_tbed_hisc_crane2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane2.wmod", true);
  }

  public void testHISCCP_tbed_hisc_crane3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Crane3.wmod", true);
  }

  public void testHISCCP_tbed_hisc_ll2()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level II.wmod", true);
  }

  public void testHISCCP_tbed_hisc_ll46()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Sec4-6.wmod", true);
  }

  public void testHISCCP_tbed_hisc_ll57()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Sec5-7.wmod", true);
  }

  public void testHISCCP_tbed_hisc_switch3()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Switch3.wmod", true);
  }

  public void testHISCCP_tbed_hisc_switch8()
  throws Exception
  {
    runModelVerifier("despot", "tbed_hisc", "Low Level Switch8.wmod", true);
  }

  // rhone_subsystem1
  public void testHISCCP_rhone_subsystem1_ld()
  throws Exception
  {
    runModelVerifier("tests", "hisc", "rhone_subsystem1_ld.wmod", false);
  }

  public void testHISCCP_rhone_subsystem1_ld_failsic5()
  throws Exception
  {
    runModelVerifier("tests", "hisc",
                     "rhone_subsystem1_ld_failsic5.wmod", false);
  }

}
