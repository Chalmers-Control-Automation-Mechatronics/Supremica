//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLargeLanguageInclusionCheckerTest
//###########################################################################
//# $Id: AbstractLargeLanguageInclusionCheckerTest.java,v 1.4 2006-11-28 04:28:33 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;


public abstract class AbstractLargeLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Test Cases --- profisafe
  public void testProfisafeI4Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI4Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO4Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO4Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI4__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeI4__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI4__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI4__slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    final String propname = "SLAVE__fv__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO4__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeO4__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO4__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO4__slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    final String propname = "SLAVE__fv__property";
    runModelVerifier(group, dir, name, true, propname);
  }


  //#########################################################################
  //# Test Cases --- incremental suite
  public void testBigBmw_cmft_kl50() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    final String propname = "prop_cmft_kl50";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testBigBmw_cmft_req() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    final String propname = "prop_cmft_req";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testBigBmw_fh_cmftreq0() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    final String propname = "prop_fh_cmftreq0";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testBigBmw_fh_cmftreq1() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    final String propname = "prop_fh_cmftreq1";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testFischertechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTbedNoCollision() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_nocoll.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testTbedNoDerailment() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testVerriegel4_ER() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    final String propname = "sicherheit_er";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testVerriegel4_VR() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    final String propname = "sicherheit_vr4";
    runModelVerifier(group, dir, name, true, propname);
  }


  //#########################################################################
  //# When you're looking for a REAL challenge ...
  /*
  public void testProfisafeInclusionI4_HostCRC3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_i4host.wmod";
    final String propname = "fv__host_crc__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionI4_HostTO3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_i4host.wmod";
    final String propname = "fv__host_to__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionI4_HostCRC3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_i4host.wmod";
    final String propname = "fv__host_crc__3r__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionI4_HostTO3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_i4host.wmod";
    final String propname = "fv__host_to__3r__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeInclusionO4_HostCRC3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4host.wmod";
    final String propname = "fv__host_crc__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionO4_HostTO3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4host.wmod";
    final String propname = "fv__host_to__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionO4_HostCRC3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4host.wmod";
    final String propname = "fv__host_crc__3r__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionO4_HostTO3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4host.wmod";
    final String propname = "fv__host_to__3r__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeInclusionO4_SlaveCRC3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4slave.wmod";
    final String propname = "fv__slave_crc__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionO4_SlaveTO3() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4slave.wmod";
    final String propname = "fv__slave_to__3__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeInclusionO4_SlaveCRC3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4slave.wmod";
    final String propname = "fv__slave_crc__3r__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeInclusionO4_SlaveTO3r() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_inclusion_o4slave.wmod";
    final String propname = "fv__slave_to__3r__property";
    runModelVerifier(group, dir, name, true, propname);
  }
  */

  
  //#########################################################################
  //# Test Cases -- Parameterised

}
