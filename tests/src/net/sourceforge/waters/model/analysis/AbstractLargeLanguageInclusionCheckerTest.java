//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLargeLanguageInclusionCheckerTest
//###########################################################################
//# $Id: AbstractLargeLanguageInclusionCheckerTest.java,v 1.1 2006-11-15 05:20:02 robi Exp $
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


  //#########################################################################
  //# Test Cases --- incremental suite


  //#########################################################################
  //# Test Cases -- Parameterised

}
