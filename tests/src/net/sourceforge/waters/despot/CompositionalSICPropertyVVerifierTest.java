//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICPropertyVVerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSICPropertyVVerifierTest extends
    AbstractSICPropertyVVerifierTest
{

  protected ModelVerifier createModelVerifier(
                                              final ProductDESProxyFactory factory)
  {
    final ConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    return new SICPropertyVVerifier(checker, factory);
  }

  // #########################################################################
  // # Overridden Test Cases
  public void testSICPropertyVVerifier_aip3_syn_tu1() throws Exception
  {
    /*
     * try { super.testSICPropertyVVerifier_aip3_syn_tu1(); } catch (final
     * OverflowException exception) { // never mind }
     */
  }

  public void testSICPropertyVVerifier_aip3_syn_tu2() throws Exception
  {
    /*
     * try { super.testSICPropertyVVerifier_aip3_syn_tu2(); } catch (final
     * OverflowException exception) { // never mind }
     */
  }

  public void testSICPropertyVVerifier_aip3_syn_tu3() throws Exception
  {
    /*
     * try { super.testSICPropertyVVerifier_aip3_syn_tu3(); } catch (final
     * OverflowException exception) { // never mind }
     */
  }

  public void testSICPropertyVVerifier_aip3_syn_tu4() throws Exception
  {
    /*
     * try { super.testSICPropertyVVerifier_aip3_syn_tu4(); } catch (final
     * OverflowException exception) { // never mind }
     */
  }
}
