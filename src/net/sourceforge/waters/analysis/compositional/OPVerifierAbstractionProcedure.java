//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * An experimental abstraction procedure to run OP-Verifier experiments
 * during a compositional conflict check.
 *
 * @author Robi Malik
 */

class OPVerifierAbstractionProcedure
  extends TRConflictEquivalenceAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static OPVerifierAbstractionProcedure createOPVerifierProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence(equivalence);
    bisimulator.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    bisimulator.setMarkingMode
      (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
    final int limit = analyzer.getInternalTransitionLimit();
    bisimulator.setTransitionLimit(limit);
    chain.add(bisimulator);
    return new OPVerifierAbstractionProcedure(analyzer, chain);
  }


  //#########################################################################
  //# Constructor
  private OPVerifierAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier simplifier)
  {
    super(analyzer, simplifier, false);
    mExperiment = OPVerifierExperiment.getInstance();
  }


  //#########################################################################
  //# Overrides for AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps, final Candidate cand)
    throws AnalysisException
  {
    if (!local.isEmpty()) {
      assert local.size() <= 1 : "At most one tau event supported!";
      final EventProxy tau = local.iterator().next();
      final EventProxy omega = getUsedDefaultMarking();
      mExperiment.runExperiment(aut, tau, omega);
    }
    return super.run(aut, local, steps, cand);
  }


  //#########################################################################
  //# Data Members
  private final OPVerifierExperiment mExperiment;

}
