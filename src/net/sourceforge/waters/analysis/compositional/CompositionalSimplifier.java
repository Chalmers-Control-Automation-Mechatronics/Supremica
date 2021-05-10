//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;

import gnu.trove.set.hash.THashSet;


/**
 * A general compositional simplification algorithm that can be configured to
 * use different abstraction sequences for its simplification steps.
 *
 * @author Robi Malik
 */

public class CompositionalSimplifier
  extends AbstractCompositionalModelAnalyzer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new compositional simplifier without a model.
   * @param factory
   *          Factory used for result construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(null, factory, abstractionCreator);
  }

  /**
   * Creates a new compositional simplifier to minimise the given model .
   * @param model
   *          The model to be minimised.
   * @param factory
   *          Factory used for result construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(model, factory,
         IdenticalKindTranslator.getInstance(), abstractionCreator);
  }

  /**
   * Creates a new compositional simplifier to minimise the given model .
   * @param model
   *          The model to be minimised.
   * @param factory
   *          Factory used for result construction.
   * @param translator
   *          Kind translator to determine event and automaton types.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator)
  {
    super(model, factory, translator, abstractionCreator,
          new PreselectingMethodFactory());
    // TODO This is specific to nonblocking and should be in a subclass.
    setPruningDeadlocks(true);
  }


  //#########################################################################
  //# Configuration
  /**
   * Specifies a set of events that cannot be hidden and thus must
   * remain present in the final simplification result.
   */
  public void setPreservedEvents(final Collection<EventProxy> events)
  {
    mPreservedEvents = new THashSet<EventProxy>(events);
  }

  /**
   * Gets the set of events that cannot be hidden.
   * @see #setPreservedEvents(Collection) setPreservedEvents()
   */
  public Set<EventProxy> getPreservedEvents()
  {
    return mPreservedEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public ConflictAbstractionProcedureFactory getAbstractionProcedureFactory()
  {
    return ConflictAbstractionProcedureFactory.getInstance();
  }

  @Override
  public CompositionalSimplificationResult createAnalysisResult()
  {
    return new CompositionalSimplificationResult(this);
  }

  @Override
  public CompositionalSimplificationResult getAnalysisResult()
  {
    return (CompositionalSimplificationResult) super.getAnalysisResult();
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      runCompositionalMinimisation();
      final CompositionalSimplificationResult result = getAnalysisResult();
      result.setSatisfied(true);
      final ProductDESProxyFactory factory = getFactory();
      final String name = getModel().getName();
      result.close(factory, name);
      return true;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user has not
   * configured them.
   */
  @Override
  protected void setUp()
    throws AnalysisException
  {
    final EventProxy defaultMarking = createDefaultMarking();
    final AbstractionProcedureCreator abstraction =
      getAbstractionProcedureCreator();
    final EventProxy preconditionMarking;
    if (abstraction.expectsAllMarkings()) {
      preconditionMarking = createPreconditionMarking();
    } else {
      preconditionMarking = getConfiguredPreconditionMarking();
    }
    setPropositionsForMarkings(defaultMarking, preconditionMarking);
    super.setUp();
  }


  //#########################################################################
  //# Hooks
  @Override
  protected EventInfo createEventInfo(final EventProxy event)
  {
    return new SimplificationEventInfo(event);
  }

  @Override
  protected boolean doMonolithicAnalysis(final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    final CompositionalSimplificationResult result = getAnalysisResult();
    for (final AutomatonProxy aut : automata) {
      if (isTrivial(aut)) {
        continue;
      } else if (isTriviallyBlocking(aut)) {
        // TODO This is specific to nonblocking and should be in a subclass.
        result.clearAutomata();
        result.addAutomaton(aut);
        result.setSatisfied(true);
        return true;
      } else {
        result.addAutomaton(aut);
      }
    }
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isTrivial(final AutomatonProxy aut)
  {
    return aut.getEvents().size() == 0;
  }

  private boolean isTriviallyBlocking(final AutomatonProxy aut)
  {
    final Collection<StateProxy> states = aut.getStates();
    if (states.size() != 1) {
      return false;
    }
    final EventProxy omega = getUsedDefaultMarking();
    final Collection<EventProxy> events = aut.getEvents();
    if (!events.contains(omega)) {
      return false;
    }
    final StateProxy state = states.iterator().next();
    return !state.getPropositions().contains(omega);
  }


  //#########################################################################
  //# Inner Class SimplificationEventInfo
  private final class SimplificationEventInfo
    extends EventInfo
  {
    //#######################################################################
    //# Constructor
    private SimplificationEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.abstraction.
    //# AbstractCompositionalModelVerifier.EventInfo
    @Override
    protected boolean canBeTau()
    {
      final EventProxy event = getEvent();
      return !mPreservedEvents.contains(event);
    }

    @Override
    protected boolean canBeLocal()
    {
      return canBeTau();
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Set of events that cannot be hidden even if local.
   */
  private Set<EventProxy> mPreservedEvents = Collections.emptySet();

}
