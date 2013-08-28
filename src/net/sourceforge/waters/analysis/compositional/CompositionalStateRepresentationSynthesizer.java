//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.map.hash.TLongIntHashMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.LongSynchronisationEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;

import org.apache.log4j.Logger;


/**
 * An implementation of the compositional synthesis algorithm.
 *
 * <I>References:</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian. On the Use of
 * Observation Equivalence in Synthesis Abstraction. Proc. 3rd IFAC Workshop
 * on Dependable Control of Discrete Systems, DCDS&nbsp;2011,
 * Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. Nondeterminism Avoidance in
 * Compositional Synthesis of Discrete Event Systems, Proc. 7th International
 * Conference on Automation Science and Engineering, CASE&nbsp;2011, Trieste,
 * Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class CompositionalStateRepresentationSynthesizer extends
  AbstractCompositionalSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxyFactory factory)
  {
    this(factory, SynthesisAbstractionProcedureFactory.WSOE);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxyFactory factory,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(factory, IdenticalKindTranslator.getInstance(), abstractionFactory);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(null, factory, translator, abstractionFactory);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate selection
   *          methods.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory,
                                  final PreselectingMethodFactory preselectingMethodFactory,
                                  final SelectingMethodFactory selectingMethodFactory)
  {
    this(null, factory, translator, abstractionFactory,
         preselectingMethodFactory, selectingMethodFactory);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(model, factory, translator, abstractionFactory,
         new PreselectingMethodFactory(), new SelectingMethodFactory());
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate selection
   *          methods.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory,
                                  final PreselectingMethodFactory preselectingMethodFactory,
                                  final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory, selectingMethodFactory);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.requestAbort();
    }
  }

  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final CompositionalSynthesisResult result = getAnalysisResult();
      if (!result.isFinished()) {
        runCompositionalMinimisation();
      }
      if (!result.isFinished()) {
        result.setSatisfied(true);
        if (getConstructsResult()) {
          final ProductDESProxyFactory factory = getFactory();
          result.close(factory, getOutputName());
        }
      }
      final Logger logger = getLogger();
      logger.debug("CompositionalSynthesizer done.");
      return result.isSatisfied();
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
  @Override
  protected void setUp() throws AnalysisException
  {
    mHalfwaySimplifier = new HalfWaySynthesisTRSimplifier();
    mHalfwaySimplifier.setOutputMode
      (HalfWaySynthesisTRSimplifier.OutputMode.PSEUDO_SUPERVISOR);
    super.setUp();
    final List<AutomatonProxy> automata = getCurrentAutomata();
    mStateRepresentationMap = new HashMap<>(automata.size());
    for (final AutomatonProxy automaton: automata) {
      final StateEncoding encoding = new StateEncoding(automaton);
      final SynthesisStateSpace.SynthesisStateMap map =
        SynthesisStateSpace.createStateEncodingMap(automaton, encoding);
      final StateRepresentationInfo info = new StateRepresentationInfo(map, encoding);
      mStateRepresentationMap.put(automaton, info);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mHalfwaySimplifier = null;
  }

  //#########################################################################
  //# Hooks

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
    if (step instanceof MergeStep) {
      final MergeStep merge = (MergeStep) step;
      final AutomatonProxy original = step.getOriginalAutomaton();
      final AutomatonProxy result = step.getResultAutomaton();
      final StateRepresentationInfo parentInfo =
        mStateRepresentationMap.get(original);
      final SynthesisStateSpace.SynthesisStateMap parent = parentInfo.getMap();
      final List<int[]> partitionList = merge.getPartition();
      final TRPartition partition =
        new TRPartition(partitionList, original.getStates().size());
      final SynthesisStateSpace.SynthesisStateMap map =
        SynthesisStateSpace.createPartitionMap(partition, parent);
      final StateRepresentationInfo info =
        new StateRepresentationInfo(map, merge.getResultStateEncoding());
      mStateRepresentationMap.put(result, info);
      mStateRepresentationMap.remove(original);
    } else if (step instanceof HidingStep) {
      final HidingStep hide = (HidingStep) step;
      final List<AutomatonProxy> originals = step.getOriginalAutomata();
      final AutomatonProxy result = step.getResultAutomaton();
      if(originals.size()>=2) {
        final List<SynthesisStateSpace.SynthesisStateMap> parents =
          new ArrayList<SynthesisStateSpace.SynthesisStateMap>(originals.size());
        final int[] stateSize = new int[originals.size()];
        final StateEncoding[] originalStateEncodings = new StateEncoding[originals.size()];
        int a = 0;
        for (final AutomatonProxy automaton : originals) {
          final StateRepresentationInfo info = mStateRepresentationMap.remove(automaton);
          final SynthesisStateSpace.SynthesisStateMap parent = info.getMap();
          parents.add(parent);
          originalStateEncodings[a] = info.getEncoding();
          stateSize[a] = originalStateEncodings[a].getNumberOfStates();
          a++;
        }
        final LongSynchronisationEncoding synchEncoding =
          new LongSynchronisationEncoding(stateSize);
        final Set<StateProxy> synchStates = result.getStates();
        final TLongIntHashMap synchMap =
          new TLongIntHashMap(synchStates.size(), 0.5f, -1, -1);
        final StateEncoding synchStateEncoding = new StateEncoding(result);
        final int[] stateCodes = new int[originals.size()];
        for (final StateProxy synchState : synchStates) {
          a = 0;
          for (final AutomatonProxy automaton : originals) {
            final StateProxy originalState =
              hide.getOriginalState(synchState, automaton);
            final int stateCode = originalStateEncodings[a].getStateCode(originalState);
            stateCodes[a] = stateCode;
            a++;
          }
          final int synchStateCode = synchStateEncoding.getStateCode(synchState);
          final long synchKey = synchEncoding.encode(stateCodes);
          synchMap.put(synchKey, synchStateCode);
        }
        final SynthesisStateSpace.SynthesisStateMap map =
          SynthesisStateSpace.createSynchronisationMap(synchMap, synchEncoding, parents);
        final StateRepresentationInfo info =
          new StateRepresentationInfo(map, synchStateEncoding);
        mStateRepresentationMap.put(result, info);
      } else {
        final AutomatonProxy original = originals.get(0);
        final StateRepresentationInfo info =
          mStateRepresentationMap.get(original);
        mStateRepresentationMap.put(result, info);
        mStateRepresentationMap.remove(original);
      }
    }
  }

  @Override
  protected boolean doMonolithicAnalysis(final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    AutomatonProxy automaton = null;
    switch (automata.size()) {
    case 0:
      return true;
    case 1:
      automaton = automata.get(0);
      break;
    default:
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        double estimate = 1.0;
        for (final AutomatonProxy aut : automata) {
          estimate *= aut.getStates().size();
        }
        logger.debug("Monolithically composing " + automata.size() +
                     " automata, estimated " + estimate + " states.");
      }
      final MonolithicSynchronousProductBuilder syncBuilder =
        getSynchronousProductBuilder();
      final ProductDESProxy des = createProductDESProxy(automata);
      syncBuilder.setModel(des);
      final int slimit = getMonolithicStateLimit();
      syncBuilder.setNodeLimit(slimit);
      final int tlimit = getMonolithicTransitionLimit();
      syncBuilder.setTransitionLimit(tlimit);
      syncBuilder.setConstructsResult(true);
      syncBuilder.setPropositions(getPropositions());
      syncBuilder.run();
      automaton = syncBuilder.getComputedAutomaton();
      break;
    }
    final EventEncoding encoding = createSynthesisEventEncoding(automaton);
    final ListBufferTransitionRelation supervisor =
      synthesise(automaton, encoding);
    if (supervisor != null) {
      reportSupervisor("monolithic", supervisor);
      final CompositionalSynthesisResult result = getAnalysisResult();
      result.addSynchSize(automaton.getStates().size());
      if (supervisor.getNumberOfReachableStates() == 0) {
        result.setSatisfied(false);
        return false;
      } else {

        return true;
      }
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Renaming


  private EventEncoding createSynthesisEventEncoding(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final Collection<EventProxy> filter;
    if (props == null) {
      filter = Collections.emptyList();
    } else {
      filter = props;
    }
    final EventEncoding encoding =
      new EventEncoding(aut, translator, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    for (int e = EventEncoding.NONTAU; e < encoding.getNumberOfProperEvents(); e++) {
      final byte status = encoding.getProperEventStatus(e);
      encoding.setProperEventStatus(e, status | EventEncoding.STATUS_LOCAL);
    }
    encoding.sortProperEvents((byte) ~EventEncoding.STATUS_LOCAL,
                              EventEncoding.STATUS_CONTROLLABLE);
    return encoding;
  }

  private ListBufferTransitionRelation synthesise(final AutomatonProxy aut,
                                                  final EventEncoding eventEnc)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut,
                                       eventEnc,
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final int numStates = rel.getNumberOfStates();
    mHalfwaySimplifier.setTransitionRelation(rel);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = eventEnc.getEventCode(defaultMarking);
    if (defaultID < 0) {
      return null;
    }
    mHalfwaySimplifier.setDefaultMarkingID(defaultID);
    mHalfwaySimplifier.run();
    final ListBufferTransitionRelation supervisor =
      mHalfwaySimplifier.getTransitionRelation();
    final List<int[]> partition = mHalfwaySimplifier.getResultPartition();
    if (partition == null) {
      return null;
    } else if (partition.isEmpty()) {
      return supervisor;
    }
    final BitSet safeStates =
      SynthesisAbstractionProcedure.getSafeStates(partition, numStates);
    final TransitionIterator iter =
      supervisor.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int t = iter.getCurrentTargetState();
      if (!safeStates.get(t)) {
        final int e = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(e);
        if (EventEncoding.isControllableEvent(status)) {
          return supervisor;
        }
      }
    }
    return null;
  }

  //#########################################################################
  //# Debugging
  @Override
  void reportAbstractionResult(final AutomatonProxy aut,
                               final AutomatonProxy dist)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      /*
       * final boolean nonblocking = AnalysisTools.isNonBlocking(aut); final
       * String msg1 = "Simplified automaton is " + (nonblocking ?
       * "nonblocking." : "BLOCKING."); logger.debug(msg1);
       */
      if (dist != null) {
        final String msg2 =
          "Creating distinguisher '" + dist.getName() + "' with "
            + dist.getStates().size() + " states.";
        logger.debug(msg2);
      }
    }
  }

  @Override
  void reportSupervisor(final String kind,
                        final ListBufferTransitionRelation sup)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled() && sup != null) {
      final String msg =
        "Got " + kind + " supervisor '" + sup.getName() + "' with "
          + sup.getNumberOfReachableStates() + " states.";
      logger.debug(msg);
    }
  }

  @SuppressWarnings("unused")
  private int getNumberOfDumpStates(final ListBufferTransitionRelation rel)
  {
    int numBadState = 0;
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      iter.resetState(s);
      if (!iter.advance() & rel.isReachable(s) & rel.getAllMarkings(s) == 0) {
        numBadState++;
      }
    }
    return numBadState;
  }

  //#########################################################################
  //# Inner Class

  private static class StateRepresentationInfo
  {

    StateRepresentationInfo(final SynthesisStateSpace.SynthesisStateMap map,
                            final StateEncoding encoding)
    {
      mMap = map;
      mEncoding = encoding;
    }

    private SynthesisStateSpace.SynthesisStateMap getMap()
    {
      return mMap;
    }

    private StateEncoding getEncoding()
    {
      return mEncoding;
    }

    //#######################################################################
    //# Data Members
    private final SynthesisStateSpace.SynthesisStateMap mMap;
    private final StateEncoding mEncoding;
  }

  //#########################################################################
  //# Data Members
  private HalfWaySynthesisTRSimplifier mHalfwaySimplifier;
  private Map<AutomatonProxy,StateRepresentationInfo> mStateRepresentationMap;
}
