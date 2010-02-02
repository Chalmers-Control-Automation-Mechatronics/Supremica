package net.sourceforge.waters.despot;


public class LargeSICPropertyVVerifierTest extends SICPropertyVVerifierTest
{

  public void testConflictChecker_rhone_subsystem1_ld_failsic5()
      throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld_failsic5", false);
  }

  public void testConflictChecker_rhone_subsystem1_ld() throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld", true);
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

}
