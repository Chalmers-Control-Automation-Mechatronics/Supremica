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
    final String name = "aip0alps.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testRhoneTough() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "aip0tough.wmod";
    runModelVerifier(group, dir, name, false);
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

  public void testTbedNoderail() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
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
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__6() throws Exception
  {
    checkTransferline("transferline.wmod",6,true);
  }

  public void testTransferline__7() throws Exception
  {
    checkTransferline("transferline.wmod",7,true);
  }

  public void testTransferline__8() throws Exception
  {
    checkTransferline("transferline.wmod",8,true);
  }

  public void testTransferline__9() throws Exception
  {
    checkTransferline("transferline.wmod",9,true);
  }

  public void testTransferline__10() throws Exception
  {
    checkTransferline("transferline.wmod",10,true);
  }

  public void testTransferline__20() throws Exception
  {
    checkTransferline("transferline.wmod",20,true);
  }

}
