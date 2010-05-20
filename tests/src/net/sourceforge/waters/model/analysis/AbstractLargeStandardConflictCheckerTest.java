//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLargeStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractLargeStandardConflictCheckerTest extends
    AbstractStandardConflictCheckerTest
{

  // #########################################################################
  // # Test Cases --- incremental suite
  public void testBigBmw() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testFischertechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testFZelle() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "fzelle.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeI4() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testRhoneAlps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_alps.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testRhoneTough() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "rhone_tough.wmod";
    runModelVerifier(group, dir, name, false); // not sure about result ...
  }

  public void testTbedCTCT() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModelVerifier(group, dir, name, false);
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
    final String name = "tbed_noderail.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testVerriegel4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testVerriegel4B() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModelVerifier(group, dir, name, false);
  }

  // #########################################################################
  // # Test Cases --- profisafe
  public void testProfisafeI3HostEFA() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    runModelVerifier(group, dir, name, bindings, true);
  }

  public void testProfisafeI4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

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

  public void testProfisafeI5Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO5Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o5_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeI6Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i6_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO6Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o6_host.wmod";
    runModelVerifier(group, dir, name, true);
  }

  // #########################################################################
  // # Test Cases -- Tip
  public void testTip3() throws Exception
  {
    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTip3Bad() throws Exception
  {
    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3_bad.wmod";
    runModelVerifier(group, dir, name, false);
  }

  // #########################################################################
  // # Test Cases -- Parameterised
  public void testTransferline__5() throws Exception
  {
    checkTransferline(5);
  }

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
