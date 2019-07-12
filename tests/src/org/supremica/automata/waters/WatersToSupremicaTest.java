//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.IO.ProjectBuildFromWaters;


/**
 * A test for the conversion of Waters models to Supremica and back.
 * This test uses {@link ProjectBuildFromWaters} and {@link AutomataToWaters}
 * to convert {@link ProductDESProxy} objects to {@link Automata} objects and
 * back, and checks whether the result is unchanged.
 *
 * @author Robi Malik
 */

public class WatersToSupremicaTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Test Cases
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxy des = mFactory.createProductDESProxy("empty");
    testSupremica(des);
  }

  public void testBuffer()
    throws Exception
  {
    testSupremica("handwritten", "buffer_sf1.wdes");
  }

  public void testDebounce()
    throws Exception
  {
    testSupremica("valid", "debounce", "debounce.wdes");
  }

  public void testForbidden()
    throws Exception
  {
    testSupremica("tests", "nasty", "forbidden.wmod");
  }

  public void testJustProperty()
    throws Exception
  {
    testSupremica("tests", "nasty", "just_property.wdes");
  }

  public void testProfisafeI4Slave()
    throws Exception
  {
    testSupremica("tests", "profisafe", "profisafe_i4_slave.wdes");
  }

  public void testProfisafeO4Host()
    throws Exception
  {
    testSupremica("tests", "profisafe", "profisafe_o4_host.wdes");
  }

  public void testSmallFactory()
    throws Exception
  {
    testSupremica("handwritten", "small_factory_2.wdes");
  }

  public void testTransferline()
    throws Exception
  {
    testSupremica("handwritten", "transferline.wmod");
  }


  //#########################################################################
  //# Utilities
  protected void testSupremica(final String... path)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(path);
    testSupremica(des);
  }

  protected void testSupremica(final ProductDESProxy des)
    throws Exception
  {
    final Automata automata = mExporter.build(des);
    final ProductDESProxy backImported = mImporter.convertAutomata(automata);
    mIntegrityChecker.check(backImported);
    assertProductDESProxyEquals
      ("Product DES changed after exporting to Supremica and importing back!",
       backImported, des);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mFactory = ProductDESElementFactory.getInstance();
    mExporter = new ProjectBuildFromWaters(null);
    mExporter.setIncludesProperties(true);
    mImporter = new AutomataToWaters(mFactory);
    mImporter.setSuppressesRedundantSelfloops(true);
    mIntegrityChecker = ProductDESIntegrityChecker.getInstance();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mFactory = null;
    mExporter = null;
    mImporter = null;
    mIntegrityChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mFactory;
  private ProjectBuildFromWaters mExporter;
  private AutomataToWaters mImporter;
  private ProductDESIntegrityChecker mIntegrityChecker;

}
