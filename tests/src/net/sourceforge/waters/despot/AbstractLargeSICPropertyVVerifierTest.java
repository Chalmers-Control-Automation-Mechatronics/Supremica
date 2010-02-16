package net.sourceforge.waters.despot;

import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;


public abstract class AbstractLargeSICPropertyVVerifierTest extends
    AbstractConflictCheckerTest
{

  public void testSICPropertyVVerifier_rhone_subsystem1_ld_failsic5()
      throws Exception
  {
    runModelVerifier("tests", "hisc", "rhone_subsystem1_ld_failsic5.wmod",
                     false);
  }

  public void testSICPropertyVVerifier_rhone_subsystem1_ld() throws Exception
  {
    runModelVerifier("tests", "hisc", "rhone_subsystem1_ld.wmod", true);
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
