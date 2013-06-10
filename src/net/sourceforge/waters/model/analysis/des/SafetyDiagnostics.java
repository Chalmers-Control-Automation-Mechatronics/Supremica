//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A helper interface to facilitate the generation of diagnostic messages for
 * different safety verification tasks.</P>
 *
 * <P>While all controllability and language inclusion checkers are implemented
 * as subtypes of {@link SafetyVerifier}, it is desirable to use standardised
 * messages for all controllability or language inclusion counterexamples. For
 * example, all controllability counterexamples should have a comment such as
 * &quot;The model 'small_factory' is not controllable: specification buffer
 * disables the uncontrollable event finish1 in state FULL, but it is possible
 * according to the plant model.;&quot; The necessary bridge is established by
 * choosing different implementations of this interface for safety verifiers
 * depending on their task.</P>
 *
 * @see SafetyVerifier#getDiagnostics()
 * @see ControllabilityDiagnostics
 * @see LanguageInclusionDiagnostics
 *
 * @author Robi Malik
 */

public interface SafetyDiagnostics
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the name to be given to a counterexample for the given model.
   */
  public String getTraceName(ProductDESProxy des);

  /**
   * Generates a comment to be used for a safety counterexample.
   * @param  des    The model being verified.
   * @param  event  The event that causes the safety property under
   *                investigation to fail.
   * @param  aut    The automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @param  state  The state in the automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @return An English string that describes why the safety property is
   *         violated, which can be used as a trace comment.
   */
  public String getTraceComment(final ProductDESProxy des,
                                final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state);

}
