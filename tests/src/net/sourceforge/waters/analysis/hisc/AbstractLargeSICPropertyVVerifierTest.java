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

import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;


public abstract class AbstractLargeSICPropertyVVerifierTest extends
    AbstractConflictCheckerTest
{

  public void testSICPropertyVVerifier_rhone_subsystem1_ld_failsic5()
      throws Exception
  {
    runModelVerifier("tests", "hisc", "aip1sub1ld_failsic5.wmod",
                     false);
  }

  public void testSICPropertyVVerifier_rhone_subsystem1_ld() throws Exception
  {
    runModelVerifier("tests", "hisc", "aip1sub1ld.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_syn_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_syn", "aip1.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_syn_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_syn", "aip2.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_syn_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_syn", "aip3.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_veri_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_veri", "aip1.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_veri_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_veri", "aip2.wmod", true);
  }

  public void testSICPropertyVVerifier_maip3_veri_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip3_veri", "aip3.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_syn_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_syn", "aip1.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_syn_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_syn", "aip2.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_syn_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_syn", "aip3.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_veri_as1() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_veri", "aip1.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_veri_as2() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_veri", "aip2.wmod", true);
  }

  public void testSICPropertyVVerifier_maip5_veri_as3() throws Exception
  {
    runModelVerifier("despot", "song_aip/maip5_veri", "aip3.wmod", true);
  }

}
