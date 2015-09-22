//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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








