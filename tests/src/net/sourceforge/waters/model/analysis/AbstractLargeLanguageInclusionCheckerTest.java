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

  public void testProfisafeI5Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeI5Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI5Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO5Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o5_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeO5Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o5_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO5Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o5_host.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI6Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i6_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeI6Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i6_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeI6Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i6_host.wmod";
    final String propname = "HOST__fv_timeout__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO6Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o6_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeO6Host__fv_crc_noinit() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o6_host.wmod";
    final String propname = "HOST__fv_crc_noinit__property";
    runModelVerifier(group, dir, name, true, propname);
  }

  public void testProfisafeO6Host__fv_timeout() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o6_host.wmod";
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
