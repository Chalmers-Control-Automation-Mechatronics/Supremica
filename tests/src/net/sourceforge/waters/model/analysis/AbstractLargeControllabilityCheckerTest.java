//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLargeControllabilityCheckerTest
//###########################################################################
//# $Id: AbstractLargeControllabilityCheckerTest.java,v 1.1 2006-11-13 03:58:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;


public abstract class AbstractLargeControllabilityCheckerTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Test Cases --- profisafe
  public void testProfisafeO4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases --- incremental suite
  public void testBigBmw() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testFZelle() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "fzelle.wmod";
    runModelVerifier(group, dir, name, true);
  }

  /*
   * Nondeterminism in original test suite !!!
  public void testProfisafeI4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "profisafe_i4.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "profisafe_o4.wmod";
    runModelVerifier(group, dir, name, true);
  }
   */

  public void testRhoneAlps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTbedCTCT() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTbedUncont() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_uncont.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTbedValid() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_valid.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testVerriegel4() throws Exception
  {
    final String group = "valid";
    final String dir = "central_locking";
    final String name = "verriegel4.wmod";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__6() throws Exception
  {
    checkTransferline(6);
  }

  public void testTransferline__7() throws Exception
  {
    checkTransferline(7);
  }

  public void testTransferline__8() throws Exception
  {
    checkTransferline(8);
  }

  public void testTransferline__9() throws Exception
  {
    checkTransferline(9);
  }

  public void testTransferline__10() throws Exception
  {
    checkTransferline(10);
  }

  public void testTransferline__20() throws Exception
  {
    checkTransferline(20);
  }

}
