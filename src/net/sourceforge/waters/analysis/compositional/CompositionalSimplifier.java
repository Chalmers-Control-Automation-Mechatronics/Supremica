//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


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
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxyFactory factory,
     final AbstractionProcedureFactory abstractionFactory)
  {
    this(null, factory, abstractionFactory);
  }

  /**
   * Creates a new compositional simplifier to minimise the given model .
   * @param model
   *          The model to be minimised.
   * @param factory
   *          Factory used for result construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final AbstractionProcedureFactory abstractionFactory)
  {
    this(model, factory,
         IdenticalKindTranslator.getInstance(), abstractionFactory);
  }

  /**
   * Creates a new compositional simplifier to minimise the given model .
   * @param model
   *          The model to be minimised.
   * @param factory
   *          Factory used for result construction.
   * @param translator
   *          Kind translator to determine event and automaton types.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSimplifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureFactory abstractionFactory)
  {
    super(model, factory, translator, abstractionFactory,
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
  protected CompositionalSimplificationResult createAnalysisResult()
  {
    return new CompositionalSimplificationResult();
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
    final AbstractionProcedureFactory abstraction =
      getAbstractionProcedureFactory();
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

