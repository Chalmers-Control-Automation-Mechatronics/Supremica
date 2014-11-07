//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.tr.DuplicateFreeQueue;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractModelAnalyzer
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory,
                                        final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
  }

  @Override
  public boolean isSatisfied()
  {
    final AnalysisResult result = getAnalysisResult();
    return result.isSatisfied();
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredDefaultMarking = marking;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mConfiguredPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mConfiguredPreconditionMarking;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy model = getModel();
    if (mConfiguredDefaultMarking == null) {
      mUsedDefaultMarking = AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mConfiguredDefaultMarking;
    }
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final Collection<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      if (isProperAutomaton(aut)) {
        final EventEncoding eventEnc = createInitialEventEncoding(aut);
        final TRAutomatonProxy tr =
          new TRAutomatonProxy(aut, eventEnc, INITIAL_CONFIG);
        trs.add(tr);
      }
    }
    final int numEvents = model.getEvents().size();
    mCurrentSubsystem = new SubsystemInfo(trs, numEvents);
    mSubsystemQueue = new PriorityQueue<>();
    mNeedsSimplification = new DuplicateFreeQueue<>(trs);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final AnalysisResult result = getAnalysisResult();
      do {
        analyseCurrentSubsystemCompositionally();
        if (result.isFinished()) {
          // TODO Trace expansion ???
          return result.isSatisfied();
        }
        mCurrentSubsystem = mSubsystemQueue.poll();
      } while (mCurrentSubsystem != null);
      result.setSatisfied(true);
      return true;
    } catch (final OutOfMemoryError error) {
      throw new OverflowException(error);
    } catch (final StackOverflowError error) {
      throw new OverflowException(error);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding enc = new EventEncoding();
    enc.addEvent(mUsedDefaultMarking, translator, EventStatus.STATUS_UNUSED);
    if (mConfiguredPreconditionMarking != null) {
      enc.addEvent(mConfiguredPreconditionMarking, translator,
                   EventStatus.STATUS_UNUSED);
    }
    for (final EventProxy event : aut.getEvents()) {
      enc.addEvent(event, translator, EventStatus.STATUS_NONE);
    }
    return enc;
  }

  private void analyseCurrentSubsystemCompositionally()
  {
    while (mCurrentSubsystem.getNumberOfAutomata() >= 2) {
      if (earlyTerminationCheckCurrentSubsystem()) {
        return;
      }
      final boolean simplified = simplifyAutomata();
      if (simplified && earlyTerminationCheckCurrentSubsystem()) {
        return;
      } else if (disjointSubsystemsCheck()) {
        return;
      } else if (mCurrentSubsystem.getNumberOfAutomata() == 2) {
        break;
      }
      // TODO candidate selection & simplification
    }
    analyseCurrentSubsystemMonolithically();
  }

  private boolean earlyTerminationCheckCurrentSubsystem()
  {
    boolean allMarked = true;
    outer:
    for (final TRAutomatonProxy aut : mCurrentSubsystem.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(DEFAULT_MARKING)) {
        allMarked = false;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s) && rel.isMarked(s, DEFAULT_MARKING)) {
            continue outer;
          }
        }
        final AnalysisResult result = getAnalysisResult();
        result.setSatisfied(false);
        return true;
      }
    }
    // TODO Generalised nonblocking stuff ...
    return allMarked;
  }

  private boolean disjointSubsystemsCheck()
  {
    final List<SubsystemInfo> splits =
      mCurrentSubsystem.findEventDisjointSubsystems();
    if (splits == null) {
      return false;
    } else {
      mCurrentSubsystem = null;
      mSubsystemQueue.addAll(splits);
      return true;
    }
  }

  private boolean analyseCurrentSubsystemMonolithically()
  {
    // TODO Auto-generated method stub
    return false;
  }


  private boolean simplifyAutomata()
  {
    while (!mNeedsSimplification.isEmpty()) {
      @SuppressWarnings("unused")
      final TRAutomatonProxy aut = mNeedsSimplification.poll();
      // TODO
      // simplify aut
      // replace aut in mCurrentSubsystem
      // update event information
    }
    return false;
  }



  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;

  // Data Structures
  private EventProxy mUsedDefaultMarking;
  private Queue<SubsystemInfo> mSubsystemQueue;
  private SubsystemInfo mCurrentSubsystem;
  private Queue<TRAutomatonProxy> mNeedsSimplification;


  //#########################################################################
  //# Class Constants
  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

  private static final int INITIAL_CONFIG =
    ListBufferTransitionRelation.CONFIG_SUCCESSORS;

}
