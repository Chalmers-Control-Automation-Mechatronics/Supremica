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

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.apache.log4j.Logger;

import gnu.trove.set.hash.TIntHashSet;


/**
 * A Java implementation of the monolithic synchronous product algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Robi Malik
 */

public abstract class TRAbstractSynchronousProductBuilder
  extends TRAbstractModelAnalyzer
  implements SynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public TRAbstractSynchronousProductBuilder()
  {
  }

  public TRAbstractSynchronousProductBuilder(final ProductDESProxy model)
  {
    super(model);
  }

  public TRAbstractSynchronousProductBuilder
    (final ProductDESProxy model,
     final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether redundant selfloops are to be removed.
   * If enabled, events that appear as selfloops on all states except dump
   * states and nowhere else are removed from the output, and markings
   * that appear on all states are also removed.
   */
  public void setRemovingSelfloops(final boolean removing)
  {
    mRemovingSelfloops = removing;
  }

  /**
   * Returns whether selfloops are removed.
   * @see #setRemovingSelfloops(boolean) setRemovingSelfloops()
   */
  public boolean getRemovingSelfloops()
  {
    return mRemovingSelfloops;
  }

  /**
   * Sets whether the synchronous product builder is used for state
   * counting.
   * <p>
   * If enabled, each state tuple of the synchronous product would contain
   * a state count, and the state count of the tuple&nbsp;
   * x = (q<sub>1</sub>,<sub>q2</sub>,...,q<sub>n</sub>)
   * would be #(x) = #(q<sub>1</sub>)*#(q<sub>2</sub>)*...*#(q<sub>n</sub>).
   */
  public void setCountingStates(final boolean counting)
  {
    mCountingStates = counting;
  }

  /**
   * Returns whether the synchronous product builder is used for state
   * counting.
   * @see #setCountingStates(boolean) setCountingStates()
   */
  public boolean getCountingStates()
  {
    return mCountingStates;
  }

  @Override
  public int getDefaultConfig()
  {
    int config = super.getDefaultConfig();
    if (mCountingStates) {
      config |= ListBufferTransitionRelation.CONFIG_COUNT_LONG;
    }
    return config;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  @Override
  public void setOutputKind(final ComponentKind kind)
  {
    mOutputKind = kind;
  }

  @Override
  public ComponentKind getOutputKind()
  {
    return mOutputKind;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  @Override
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement)
    throws OverflowException
  {
    EventEncoding enc = getEventEncoding();
    if (enc == null) {
      enc = new EventEncoding();
      setConfiguredEventEncoding(enc);
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy alias : hidden) {
      enc.addEventAlias(alias, replacement, translator, EventStatus.STATUS_NONE);
    }
  }

  @Override
  public void clearMask()
  {
    final EventEncoding enc = getEventEncoding();
    if (enc == null) {
      // nothing
    } else if (enc.getNumberOfPropositions() == 0) {
      setConfiguredEventEncoding(null);
    } else {
      enc.removeAllProperEvents();
    }
  }

  @Override
  public TRSynchronousProductResult getAnalysisResult()
  {
    return (TRSynchronousProductResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  @Override
  public TRAutomatonProxy getComputedProxy()
  {
    final TRSynchronousProductResult result = getAnalysisResult();
    if (result != null) {
      return result.getComputedAutomaton();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @Override
  public TRAutomatonProxy getComputedAutomaton()
  {
    return getComputedProxy();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();

    // Set up transition buffer
    final EventEncoding enc = getOutputEventEncoding();
    final int numOutputEvents = enc.getNumberOfProperEvents();
    final int transitionLimit = getTransitionLimit();
    mPreTransitionBuffer =
      new PreTransitionBuffer(numOutputEvents, transitionLimit);

    // Auxiliary variables
    mPreviousSource = -1;
    mPreviousEvent = -1;
  }

  @Override
  public TRSynchronousProductResult createAnalysisResult()
  {
    return new TRSynchronousProductResult(this);
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      exploreStateSpace();
      createAutomaton();
      return true;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final AutomatonResult result = getAnalysisResult();
    if (mPreTransitionBuffer != null) {
      result.setNumberOfTransitions(mPreTransitionBuffer.size());
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPreTransitionBuffer = null;
    mCurrentTargets = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void createTransition(final int event, final int target)
    throws OverflowException
  {
    if (target < 0) {
      return;
    }
    final int source = getCurrentSource();
    if (target == source) {
      final EventEncoding enc = getOutputEventEncoding();
      final byte status = enc.getProperEventStatus(event);
      if (EventStatus.isSelfloopOnlyEvent(status)) {
        return;
      }
    }
    if (source != mPreviousSource) {
      mPreviousSource = source;
      mPreviousEvent = -1;
      mCurrentTargets = new TIntHashSet();
    }
    if (event != mPreviousEvent) {
      mPreviousEvent = event;
      mCurrentTargets.clear();
      mCurrentTargets.add(target);
      mPreTransitionBuffer.addTransition(source, event, target);
    } else if (mCurrentTargets.add(target)) {
      mPreTransitionBuffer.addTransition(source, event, target);
    }
  }

  private void createAutomaton()
    throws OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final EventEncoding outputEnc = getOutputEventEncoding();
      final IntArrayBuffer stateSpace = getStateSpace();
      final int deadlockState = getDeadlockState();
      final ListBufferTransitionRelation rel;
      final int config = mCountingStates ?
        ListBufferTransitionRelation.CONFIG_S_C :
        ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      // Create a transition relation of the appropriate type.
      if (deadlockState >= 0) {
        // Deadlock & State Count
        rel = new ListBufferTransitionRelation(computeOutputName(),
                                               computeOutputKind(),
                                               outputEnc,
                                               stateSpace.size(),
                                               deadlockState,
                                               config);
      } else {
        // Not Deadlock & State Count
        rel = new ListBufferTransitionRelation(computeOutputName(),
                                               computeOutputKind(),
                                               outputEnc,
                                               stateSpace.size(),
                                               config);
      }
      // Prepare the initial states.
      final int numInit = getNumberOfInitialStates();
      for (int s = 0; s < numInit; s++) {
        rel.setInitial(s, true);
      }
      // Handle the propositions.
      final int numProps = outputEnc.getNumberOfPropositions();
      boolean hasProps = false;
      for (int p = 0; p < numProps; p++) {
        if (outputEnc.isPropositionUsed(p)) {
          final EventProxy prop = outputEnc.getProposition(p);
          addMarkings(rel, prop);
          hasProps = true;
        }
      }
      mPreTransitionBuffer.addOutgoingTransitions(rel);
      // Handle the self loops.
      if (getRemovingSelfloops()) {
        rel.removeTauSelfLoops();
        if (getPruningDeadlocks() && hasProps) {
          removeSelfloopsConsideringDeadlocks(rel);
        } else {
          rel.removeProperSelfLoopEvents();
        }
      }
      // Handle the state count.
      final StateTupleEncoding tupleEnc = getStateTupleEncoding();
      if (mCountingStates) {
        final TRAutomatonProxy[] components = getInputAutomata();
        final int numComponents = components.length;
        final int[] encoded = new int[stateSpace.getArraySize()];
        final int[] decoded = new int[numComponents];
        // For each state tuple in the synchronous product,
        for (int tupleI = 0; tupleI < stateSpace.size(); tupleI++) {
          // Load an encoded tuple.
          stateSpace.getContents(tupleI, encoded);
          // Decode the state tuple.
          tupleEnc.decode(encoded, decoded);
          // For each component automaton, multiply its state count with
          // the total state count.
          long totalCount = 1L;
          for (int autI = 0; autI < numComponents; autI++) {
            totalCount *= components[autI].getTransitionRelation().
              getStateBuffer().getStateCount(decoded[autI]);
          }
          // Now, store this state count to the location of its
          // corresponding tuple.
          rel.getStateBuffer().setStateCount(tupleI, totalCount);
        }
      }
      // Build the final product.
      final TRAutomatonProxy aut = new TRAutomatonProxy(outputEnc, rel);
      final TRSynchronousProductResult result = getAnalysisResult();
      result.setComputedAutomaton(aut);
      final Collection<AutomatonProxy> automata = getModel().getAutomata();
      final TRSynchronousProductStateMap stateMap =
        new TRSynchronousProductStateMap(automata, tupleEnc, stateSpace);
      result.setStateMap(stateMap);
    }
  }

  /**
   * Computes a name for the output automaton.
   * @return The name set by the user, if present, or a default name computed
   *         from the names of the automata in the input model.
   * @see #setOutputName(String) setOutputName()
   */
  protected String computeOutputName()
  {
    if (mOutputName != null) {
      return mOutputName;
    } else {
      final ProductDESProxy des = getModel();
      return AbstractModelBuilder.computeOutputName(des);
    }
  }

  /**
   * Computes a component kind for the output automaton (or automata).
   * @return The component kind set by the user, if present, or a default kind
   *         determined from the automata in the input model.
   * @see #setOutputKind(ComponentKind) setOutputKind()
   */
  private ComponentKind computeOutputKind()
  {
    if (mOutputKind != null) {
      return mOutputKind;
    } else {
      ComponentKind result = null;
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> automata = model.getAutomata();
      for (final AutomatonProxy aut : automata) {
        final ComponentKind kind = aut.getKind();
        if (kind == result) {
          continue;
        } else if (result == null) {
          result = kind;
        } else {
          return ComponentKind.PLANT;
        }
      }
      return result;
    }
  }

  private void addMarkings(final ListBufferTransitionRelation outputRel,
                           final EventProxy prop)
  {
    final EventEncoding enc = getOutputEventEncoding();
    final int globalP = enc.getEventCode(prop);
    if (globalP < 0) {
      return;
    }
    final MarkingInfo marking = getMarkingInfo(prop);
    if (marking.isTrivial() && getRemovingSelfloops()) {
      outputRel.setPropositionUsed(globalP, false);
      return;
    }
    boolean allMarked = getDeadlockState() < 0;
    final int numStates = outputRel.getNumberOfStates();
    final int dumpIndex = outputRel.getDumpStateIndex();
    for (int globalS = 0; globalS < numStates; globalS++) {
      if (globalS != dumpIndex) {
        if (marking.isMarkedState(globalS)) {
          outputRel.setMarked(globalS, globalP, true);
        } else {
          allMarked = false;
        }
      }
    }
    if (allMarked && getRemovingSelfloops()) {
      outputRel.setPropositionUsed(globalP, false);
    }
  }

  private void removeSelfloopsConsideringDeadlocks
    (final ListBufferTransitionRelation rel)
  {
    final int numStates = rel.getNumberOfStates();
    final long props = rel.getUsedPropositions();
    final int numEvents = rel.getNumberOfProperEvents();
    final int[] progress = new int[numEvents];
    int expected = 0;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    iter.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    for (int s = 0; s < numStates; s++) {
      iter.resetState(s);
      if (iter.advance()) {
        do {
          final int e = iter.getCurrentEvent();
          final int t = iter.getCurrentTargetState();
          if (s != t) {
            progress[e] = -1;
          } else if (progress[e] == expected) {
            progress[e] = s + 1;
          }
        } while (iter.advance());
        expected = s + 1;
      } else if ((rel.getAllMarkings(s) & props) != 0) {
        return;
      }
    }
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      if (progress[e] == expected) {
        rel.removeEvent(e);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private String mOutputName;
  private ComponentKind mOutputKind = ComponentKind.PLANT;
  private boolean mRemovingSelfloops = true;
  private boolean mCountingStates;

  private PreTransitionBuffer mPreTransitionBuffer;
  private int mPreviousSource;
  private int mPreviousEvent;
  private TIntHashSet mCurrentTargets;

}
