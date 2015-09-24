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

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObserverProjectionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An abstraction procedure that minimises automata according using
 * <I>observer projection</I>. The present implementation determines a
 * coarsest causal reporter map satisfying the observer property.
 * Nondeterminism in the projected automata is not resolved, nondeterministic
 * abstractions are used instead.
 *
 * @author Robi Malik
 */

class ObserverProjectionAbstractionProcedure
  extends TRConflictEquivalenceAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  public static AbstractionProcedure createObserverProjectionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
    final ObserverProjectionTRSimplifier op =
      new ObserverProjectionTRSimplifier();
    final int limit = analyzer.getInternalTransitionLimit();
    op.setTransitionLimit(limit);
    op.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    chain.add(op);
    return new ObserverProjectionAbstractionProcedure(analyzer, chain, op);
  }


  //#########################################################################
  //# Constructor
  private ObserverProjectionAbstractionProcedure
    (final AbstractCompositionalModelAnalyzer analyzer,
     final ChainTRSimplifier chain,
     final ObserverProjectionTRSimplifier op)
  {
    super(analyzer, chain, false);
    mOPSimplifier = op;
  }


  //#########################################################################
  //# Overrides for class TRSimplifierAbstractionProcedure
  @Override
  protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final EventEncoding eventEnc =
      super.createEventEncoding(aut, local, candidate);
    final ProductDESProxyFactory factory = getFactory();
    final String name = "vtau:" + aut.getName();
    final EventProxy vtau =
      factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
    final KindTranslator id = IdenticalKindTranslator.getInstance();
    final int codeOfVTau = eventEnc.addEvent(vtau, id, 0);
    mOPSimplifier.setVisibleTau(codeOfVTau);
    return eventEnc;
  }


  //###########################################################################
  //# Data Members
  private final ObserverProjectionTRSimplifier mOPSimplifier;

}
