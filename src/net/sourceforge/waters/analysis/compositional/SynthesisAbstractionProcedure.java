//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SynthesisObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class SynthesisAbstractionProcedure
  extends AbstractAbstractionProcedure
{

  //#########################################################################
  //# Factory Methods
  /**
   * <P>Creates a synthesis abstraction procedure.</P>
   *
   * <P>The abstraction chain is specified by flags,
   * that indicate whether or not a particular method is used.
   * The order of abstraction is predefined: first halfway synthesis,
   * then bisimulation, then synthesis observation equivalence,
   * and finally weak synthesis observation equivalence, if these methods
   * are included.</P>
   *
   * @param  synthesizer
   *           The compositional synthesiser that will control the entire
   *           synthesis run.
   * @param  abstractionMethods
   *           An integer combination of flags specifying which abstraction
   *           methods are in the chain. For example use
   *           {@link #USE_HALFWAY}&nbsp;|&nbsp;{@link #USE_BISIMULATION} to
   *           specify an abstraction sequence that performs only halfway
   *           synthesis and bisimulation.
   *
   * @see #USE_HALFWAY
   * @see #USE_BISIMULATION
   * @see #USE_SOE
   * @see #USE_WSOE
   * @see #CHAIN_SOE
   * @see #CHAIN_WSOE
   * @see #CHAIN_ALL
   */
  public static SynthesisAbstractionProcedure
    createSynthesisAbstractionProcedure
      (final CompositionalSynthesizer synthesizer,
       final int abstractionMethods)
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    if ((abstractionMethods & USE_HALFWAY) != 0) {
      final HalfWaySynthesisTRSimplifier halfWay =
        new HalfWaySynthesisTRSimplifier();
      chain.add(halfWay);
    }
    if ((abstractionMethods & USE_BISIMULATION) != 0) {
      final TransitionRelationSimplifier bisimulator =
        new BisimulationTRSimplifier();
      chain.add(bisimulator);
    }
    final int limit = synthesizer.getInternalTransitionLimit();
    if ((abstractionMethods & USE_SOE) != 0) {
      final SynthesisObservationEquivalenceTRSimplifier synthesisAbstraction =
        new SynthesisObservationEquivalenceTRSimplifier();
      synthesisAbstraction.setTransitionLimit(limit);
      synthesisAbstraction.setUsesWeakSynthesisObservationEquivalence(false);
      chain.add(synthesisAbstraction);
    }
    if ((abstractionMethods & USE_WSOE) != 0) {
      final SynthesisObservationEquivalenceTRSimplifier synthesisAbstraction =
        new SynthesisObservationEquivalenceTRSimplifier();
      synthesisAbstraction.setTransitionLimit(limit);
      synthesisAbstraction.setUsesWeakSynthesisObservationEquivalence(true);
      chain.add(synthesisAbstraction);
    }
    return new SynthesisAbstractionProcedure(synthesizer, chain);
  }


  //#########################################################################
  //# Constructor
  private SynthesisAbstractionProcedure
    (final CompositionalSynthesizer synthesizer,
     final ChainTRSimplifier chain)
  {
    super(synthesizer);
    mChain = chain;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.compositional.
  //# AbstractionProcedure
  @Override
  public boolean run(final AutomatonProxy aut,
                     final Collection<EventProxy> local,
                     final List<AbstractionStep> steps, final Candidate cand)
    throws AnalysisException
  {
    try {
      final EventEncoding eventEnc = createEventEncoding(aut, local);
      final StateEncoding inputStateEnc = createStateEncoding(aut);
      final int config = mChain.getPreferredInputConfiguration();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc,
                                         inputStateEnc, config);
      mChain.setTransitionRelation(rel);
      if (mChain.run()) {
        final ListBufferTransitionRelation original =
          getTransitionRelationBeforeSOE(rel);
        final ListBufferTransitionRelation supervisor =
          getPseudoSupervisor();
        final CompositionalSynthesizer synthesizer = getAnalyzer();
        synthesizer.reportSupervisor("halfway synthesis", supervisor);
        final SynthesisAbstractionStep step;
        if (original == null) {
          final ProductDESProxyFactory factory = getFactory();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          synthesizer.reportAbstractionResult(convertedAut, null);
          step = new SynthesisAbstractionStep(synthesizer, convertedAut, aut,
                                              supervisor, eventEnc);
        } else {
          final List<int[]> partition = getResultPartition();
          step = synthesizer.createDeterministicAutomaton(aut, original, rel,
                                                          partition, eventEnc);
          step.setSupervisor(supervisor);
        }
        steps.add(step);
        return true;
      } else {
        return false;
      }
    } finally {
      mChain.reset();
    }
  }

  @Override
  public void storeStatistics()
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    result.setSimplifierStatistics(mChain);
  }

  @Override
  public void resetStatistics()
  {
    mChain.createStatistics();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mChain.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mChain.isAborting();
  }

  @Override
  public void resetAbort()
  {
    mChain.resetAbort();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.compositional.
  //# AbstractAbstractionProcedure
  @Override
  CompositionalSynthesizer getAnalyzer()
  {
    return (CompositionalSynthesizer) super.getAnalyzer();
  }

  @Override
  CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createEventEncoding
    (final AutomatonProxy aut,
     final Collection<EventProxy> local)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final Collection<EventProxy> filter;
    if (props == null) {
      filter = Collections.emptyList();
    } else {
      filter = props;
    }
    final EventEncoding encoding = new EventEncoding
      (aut, translator, filter, EventEncoding.FILTER_PROPOSITIONS);
    for (int e = EventEncoding.NONTAU;
         e < encoding.getNumberOfProperEvents();
         e++) {
      final EventProxy event = encoding.getProperEvent(e);
      if (local.contains(event)) {
        final byte status = encoding.getProperEventStatus(e);
        encoding.setProperEventStatus(e, status | EventEncoding.STATUS_LOCAL);
      }
    }
    encoding.sortProperEvents((byte) ~EventEncoding.STATUS_LOCAL,
                              EventEncoding.STATUS_CONTROLLABLE);
    final CompositionalSynthesizer synthesizer = getAnalyzer();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = encoding.getEventCode(defaultMarking);
    mChain.setDefaultMarkingID(defaultMarkingID);
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof HalfWaySynthesisTRSimplifier) {
        final HalfWaySynthesisTRSimplifier halfWay =
          (HalfWaySynthesisTRSimplifier) step;
        final TIntHashSet renamed =
          synthesizer.getRenamedControllables(encoding);
        halfWay.setRenamedEvents(renamed);
      }
    }
    return encoding;
  }

  private StateEncoding createStateEncoding(final AutomatonProxy aut)
  {
    final StateEncoding encoding = new StateEncoding(aut);
    encoding.setNumberOfExtraStates(1);
    return encoding;
  }

  private ListBufferTransitionRelation getTransitionRelationBeforeSOE
    (final ListBufferTransitionRelation rel)
  {
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof SynthesisObservationEquivalenceTRSimplifier) {
        final SynthesisObservationEquivalenceTRSimplifier soe =
          (SynthesisObservationEquivalenceTRSimplifier) step;
        final ListBufferTransitionRelation result =
          soe.getOriginalTransitionRelation();
        if (result != null) {
          rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
          if (rel.isDeterministic()) {
            return null;
          } else {
            return result;
          }
        }
      }
    }
    return null;
  }

  private List<int[]> getResultPartition()
  {
    return mChain.getResultPartition();
  }

  private ListBufferTransitionRelation getPseudoSupervisor()
  {
    for (int index = 0; index < mChain.size(); index++) {
      final TransitionRelationSimplifier step = mChain.getStep(index);
      if (step instanceof HalfWaySynthesisTRSimplifier) {
        final HalfWaySynthesisTRSimplifier halfWay =
          (HalfWaySynthesisTRSimplifier) step;
        return halfWay.getPseudoSupervisor();
      }
    }
    return null;
  }


  //#########################################################################
  //# Inner Class BisimulationTRSimplifier
  /**
   * A specialised observation equivalence simplifier for use only in
   * synthesis. This is used for bisimulation abstraction before
   * synthesis observation equivalence.
   */
  private static class BisimulationTRSimplifier
    extends ObservationEquivalenceTRSimplifier
  {
    //#######################################################################
    //# Constructor
    private BisimulationTRSimplifier()
    {
      setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
    /**
     * Destructively applies the computed partitioning to the simplifier's
     * transition relation. After applying the partition, this implementation
     * removes the result partition from the simplifier, pretending to the
     * chain that no partition was computed. In this way, the partition
     * computed from the chain will only include the synthesis observation
     * equivalence steps.
     */
    @Override
    protected void applyResultPartition() throws AnalysisException
    {
      super.applyResultPartition();
      setResultPartitionList(null);
    }
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mChain;


  //#########################################################################
  //# Class Constants
  /**
   * Flag to include halfway synthesis in abstraction chain.
   */
  static final int USE_HALFWAY = 0x01;
  /**
   * Flag to include halfway bisimulation in abstraction chain.
   */
  static final int USE_BISIMULATION = 0x02;
  /**
   * Flag to include synthesis observation equivalence in abstraction chain.
   */
  static final int USE_SOE = 0x04;
  /**
   * Flag to include weak synthesis observation equivalence in abstraction
   * chain.
   */
  static final int USE_WSOE = 0x08;

  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, and synthesis
   * observation equivalence.
   */
  static final int CHAIN_SOE = USE_HALFWAY | USE_BISIMULATION | USE_SOE;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, and weak synthesis
   * observation equivalence. This is the default.
   */
  static final int CHAIN_WSOE = USE_HALFWAY | USE_BISIMULATION | USE_WSOE;
  /**
   * Argument to
   * {@link #createSynthesisAbstractionProcedure(CompositionalSynthesizer,int)
   * createSynthesisAbstractionProcedure()} for specifying an abstraction
   * chain consisting of halfway synthesis, bisimulation, synthesis
   * observation equivalence, and weak synthesis observation equivalence.
   */
  static final int CHAIN_ALL =
    USE_HALFWAY | USE_BISIMULATION | USE_SOE | USE_WSOE;

}

