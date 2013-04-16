//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * <P>A model verifier that checks safety properties.  This
 * interface is a generalisation of the controllability and language
 * inclusion tests. Event and component kind translation can be used to
 * implement these very similar checks using the same code.</P>
 *
 * @see KindTranslator
 * @author Robi Malik
 */

public interface SafetyVerifier extends ModelVerifier
{

  //#########################################################################
  //# Diagnostics
  /**
   * Gets the diagnostics generator for the property being checked.
   * This is used to generate standardised comments for safety
   * counterexamples for all verifiers implementing a particular type of
   * check, e.g. controllability.
   * @see SafetyDiagnostics
   */
  public SafetyDiagnostics getDiagnostics();


  //#########################################################################
  //# More Specific Access to the Results
  public SafetyTraceProxy getCounterExample();

}
