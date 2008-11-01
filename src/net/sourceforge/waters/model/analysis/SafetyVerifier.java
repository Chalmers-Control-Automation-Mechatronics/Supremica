//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

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
  //# Simple Access Methods
  /**
   * Sets a kind translator to be used by this model verifier.
   */
  public void setKindTranslator(KindTranslator translator);

  /**
   * Gets the kind translator used by this model verifier.
   * The kind translator is used to remap component and event kinds
   * for the sake of a single algorithm, and thus implement different
   * checks using the same code.
   */
  public KindTranslator getKindTranslator();


  //#########################################################################
  //# More Specific Access to the Results
  public SafetyTraceProxy getCounterExample();

}
