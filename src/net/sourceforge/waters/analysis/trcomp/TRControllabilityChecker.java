//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>The compositional controllability check algorithm.</P>

 * <P>This is the front-end for the compositional controllability check
 * algorithm. The checker supports arbitrary input models with any
 * number of specifications and uncontrollable events.</P>
 *
 * <P>The internal representation of automata is based on list buffer
 * transition relations through the {@link TRAutomatonProxy} class. Input
 * models that are not in this form are converted. Verification is performed
 * one uncontrollable event at a time, creating one-property language
 * inclusion models and passing them to a {@link
 * TRCompositionalOnePropertyChecker}.</P>
 *
 * <P><I>References:</I><BR>
 * Simon Ware, Robi Malik. The use of language projection for compositional
 * verification of discrete event systems. Proc. 9th International Workshop
 * on Discrete Event Systems (WODES'08), 322-327, G&ouml;teborg, Sweden,
 * 2008.</P>
 *
 * @author Robi Malik
 */

public class TRControllabilityChecker
  extends AbstractTRDelegatingSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public TRControllabilityChecker()
  {
    this(null);
  }

  public TRControllabilityChecker(final ProductDESProxy model)
  {
    this(model,
         ControllabilityKindTranslator.getInstance(),
         ControllabilityDiagnostics.getInstance());
  }

  public TRControllabilityChecker
    (final ProductDESProxy model,
     final KindTranslator translator,
     final SafetyDiagnostics diag)
  {
    super(model, translator, diag);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final Logger logger = LogManager.getLogger();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();

    int numPlants = 0;
    int numSpecs = 0;
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        numPlants++;
        break;
      case SPEC:
        numSpecs++;
        break;
      default:
        break;
      }
    }
    if (numSpecs == 0) {
      logger.debug("Did not find any specifications, returning TRUE.");
      setSatisfiedResult();
      return;
    }
    int numUncontrollable = 0;
    for (final EventProxy event : des.getEvents()) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        numUncontrollable++;
      }
    }
    if (numUncontrollable == 0) {
      logger.debug("Did not find any uncontrollable events, returning TRUE.");
      setSatisfiedResult();
      return;
    }

    mOnePropertyKindTranslator = new OnePropertyKindTranslator();
    final TRCompositionalOnePropertyChecker delegate = getDelegate();
    delegate.setKindTranslator(mOnePropertyKindTranslator);
    delegate.setPreservingEncodings(true);
    final int config = getPreferredInputConfiguration();
    final SpecialEventsFinder finder = new SpecialEventsFinder();
    finder.setBlockedEventsDetected(true);
    finder.setAlwaysEnabledEventsDetected(true);
    finder.setSelfloopOnlyEventsDetected(true);

    mAutomatonInfo = new HashMap<>(numPlants + numSpecs);
    final Set<EventProxy> blockedInPlant = new THashSet<>();
    final Map<EventProxy,EventInfo> eventInfoMap =
      new HashMap<>(numUncontrollable);
    for (final AutomatonProxy aut : des.getAutomata()) {
      final ComponentKind kind = translator.getComponentKind(aut);
      if (kind != ComponentKind.PLANT && kind != ComponentKind.SPEC) {
        continue;
      }
      final TRAutomatonProxy tr;
      final EventEncoding enc;
      if (aut instanceof TRAutomatonProxy) {
        tr = (TRAutomatonProxy) aut;
        enc = tr.getEventEncoding();
      } else {
        enc = delegate.createInitialEventEncoding(aut);
        tr = new TRAutomatonProxy(aut, enc, config);
      }
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      finder.setTransitionRelation(rel);
      finder.run();
      final byte[] statusArray = finder.getComputedEventStatus();
      final AutomatonInfo autInfo = new AutomatonInfo(aut, tr, statusArray);
      mAutomatonInfo.put(aut, autInfo);
      for (int e = EventEncoding.NONTAU; e < statusArray.length; e++) {
        final EventProxy event = enc.getProperEvent(e);
        if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
          final byte status = statusArray[e];
          final boolean blocked = EventStatus.isBlockedEvent(status);
          switch (kind) {
          case PLANT:
            if (blocked && blockedInPlant.add(event)) {
              logger.debug("Skipping uncontrollable event {}, because it is blocked in plant {}.",
                           event.getName(), aut.getName());
            }
            break;
          case SPEC:
            if (isRelevantEvent(status)) {
              EventInfo eventInfo = eventInfoMap.get(event);
              if (eventInfo == null) {
                eventInfo = new EventInfo(event);
                eventInfoMap.put(event, eventInfo);
              }
              eventInfo.addSpec(autInfo, blocked);
            }
          default:
            break;
          }
        }
      }
    }
    for (final EventProxy blocked : blockedInPlant) {
      eventInfoMap.remove(blocked);
    }
    if (eventInfoMap.isEmpty()) {
      logger.debug("All uncontrollable events are always enabled in all the specifications, returning TRUE.");
      setSatisfiedResult();
      return;
    }
    mEventInfo = new ArrayList<>(eventInfoMap.values());
    Collections.sort(mEventInfo);
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();

      final VerificationResult result = getAnalysisResult();
      if (result.isFinished()) {
        return result.isSatisfied();
      }
      final TRCompositionalOnePropertyChecker delegate = getDelegate();
      final Logger logger = LogManager.getLogger();
      for (final EventInfo info : mEventInfo) {
        final EventProxy event = info.getEvent();
        logger.debug("Checking controllability with respect to {} ...",
                     event.getName());
        final ProductDESProxy des;
        if (info.isBlocked()) {
          des = createBlockedLanguageInclusionModel(info);
        } else {
          des = createRegularLanguageInclusionModel(info);
        }
        delegate.setModel(des);
        delegate.run();
        final VerificationResult subResult = delegate.getAnalysisResult();
        result.merge(subResult);
         if (!subResult.isSatisfied()) {
          final TRTraceProxy trace = delegate.getCounterExample();
          convertCounterExample(trace, event);
          return setFailedResult(trace);
        }
      }
      return setSatisfiedResult();
    } catch (final AnalysisException exception) {
      setExceptionResult(exception);
      throw exception;
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mAutomatonInfo = null;
    mEventInfo = null;
    mOnePropertyKindTranslator = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy createRegularLanguageInclusionModel(final EventInfo info)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();

    final Collection<EventProxy> inputEvents = des.getEvents();
    final int numEvents = inputEvents.size();
    final Collection<AutomatonInfo> disabling = info.getDisablingSpecs();
    final int numFailures = disabling.size();
    final List<EventProxy> failures = new ArrayList<>(numFailures);
    final List<EventProxy> convertedEvents =
      new ArrayList<>(numEvents + numFailures);
    convertedEvents.addAll(inputEvents);
    for (final AutomatonInfo specInfo : disabling) {
      final EventProxy failure = specInfo.getFailureEvent();
      failures.add(failure);
      convertedEvents.add(failure);
    }

    final Collection<AutomatonProxy> inputAutomata = des.getAutomata();
    final int numAutomata = inputAutomata.size();
    final List<TRAutomatonProxy> convertedAutomata =
      new ArrayList<>(numAutomata + 1);
    final EventProxy event = info.getEvent();
    for (final AutomatonProxy aut : des.getAutomata()) {
      final AutomatonInfo autInfo = mAutomatonInfo.get(aut);
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        final TRAutomatonProxy plant =
          autInfo.createExtendedPlant(event, failures);
        convertedAutomata.add(plant);
        break;
      case SPEC:
        final TRAutomatonProxy spec = autInfo.createExtendedSpec(event);
        convertedAutomata.add(spec);
        break;
      default:
        break;
      }
    }
    final TRAutomatonProxy property = creatingBlockingSpec(failures);
    convertedAutomata.add(property);

    return createProductDES(convertedEvents, convertedAutomata, event);
  }

  private ProductDESProxy createBlockedLanguageInclusionModel(final EventInfo info)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> inputAutomata = des.getAutomata();
    final int numAutomata = inputAutomata.size();
    final List<TRAutomatonProxy> convertedAutomata =
      new ArrayList<>(numAutomata + 1);
    final EventProxy event = info.getEvent();
    for (final AutomatonProxy aut : des.getAutomata()) {
      final AutomatonInfo autInfo = mAutomatonInfo.get(aut);
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        final TRAutomatonProxy plant = autInfo.getPrimaryTRAutomaton();
        convertedAutomata.add(plant);
        break;
      case SPEC:
        final TRAutomatonProxy spec = autInfo.createExtendedSpec(event);
        convertedAutomata.add(spec);
        break;
      default:
        break;
      }
    }
    final Collection<EventProxy> failures = Collections.singletonList(event);
    final TRAutomatonProxy property = creatingBlockingSpec(failures);
    convertedAutomata.add(property);
    final Collection<EventProxy> events = des.getEvents();
    return createProductDES(events, convertedAutomata, event);
  }

  private ProductDESProxy createProductDES(final Collection<EventProxy> events,
                                           final List<TRAutomatonProxy> automata,
                                           final EventProxy event)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = des.getName();
    final String comment =
      "Generated by + " + ProxyTools.getShortClassName(this) + " from " +
      name + " to check controllability with respect to event " +
      event.getName() + ".";
    return factory.createProductDESProxy(name, comment, null,
                                         events, automata);
  }

  private TRAutomatonProxy creatingBlockingSpec(final Collection<EventProxy> events)
    throws AnalysisException
  {
    final int config = getPreferredInputConfiguration();
    final EventEncoding enc = new EventEncoding(events, mOnePropertyKindTranslator);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (":never", ComponentKind.PROPERTY, enc, 1, config);
    rel.setInitial(0, true);
    final TRAutomatonProxy aut = new TRAutomatonProxy(enc, rel);
    mOnePropertyKindTranslator.setProperty(aut);
    return aut;
  }

  private void convertCounterExample(final TRTraceProxy trace,
                                     final EventProxy event)
  {
    final ProductDESProxy des = getModel();
    final int end = trace.getNumberOfSteps() - 1;
    final int numAutomata = mAutomatonInfo.size();
    final Map<AutomatonProxy,AutomatonInfo> autInfoMap =
      new HashMap<>(numAutomata);
    for (final AutomatonInfo info : mAutomatonInfo.values()) {
      final TRAutomatonProxy aut = info.getExtendedTRAutomaton();
      autInfoMap.put(aut, info);
    }
    final Collection<AutomatonProxy> automata =
      new ArrayList<>(trace.getAutomata());
    AutomatonProxy failedAut = null;
    for (final AutomatonProxy aut : automata) {
      final AutomatonInfo info = autInfoMap.get(aut);
      if (info != null) {
        final TRAbstractionStepInput step = info.createInputStep();
        trace.replaceInputAutomaton(aut, step);
        trace.setInputAutomaton(step);
        if (info.getComponentKind() == ComponentKind.SPEC) {
          final int s = trace.getState(step, end - 1);
          final int t = trace.getState(step, end);
          if (!info.hasTransition(s, event, t)) {
            trace.setState(step, end, -1);
            if (failedAut == null) {
              failedAut = aut;
              final SafetyDiagnostics diag = getDiagnostics();
              if (diag != null) {
                final StateProxy failedState = step.getState(s);
                final String comment =
                  diag.getTraceComment(des, event, failedAut, failedState);
                trace.setComment(comment);
              }
            }
          }
        }
      } else {
        trace.removeInputAutomaton(aut);
      }
    }
    trace.setProductDES(des);
    trace.replaceEvent(end - 1, event);
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static boolean isRelevantEvent(final byte status)
  {
    final byte FLAGS =
      EventStatus.STATUS_UNUSED | EventStatus.STATUS_ALWAYS_ENABLED;
    return (status & FLAGS) == 0;
  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  private class AutomatonInfo
  {
    //#######################################################################
    //# Constructor
    private AutomatonInfo(final AutomatonProxy aut,
                          final TRAutomatonProxy tr,
                          final byte[] status)
    {
      mInputAutomaton = aut;
      mPrimaryTRAutomaton = tr;
      mEventStatus = status;
    }

    //#######################################################################
    //# Simple Access
    private ComponentKind getComponentKind()
    {
      final KindTranslator translator = getKindTranslator();
      return translator.getComponentKind(mInputAutomaton);
    }

    private TRAutomatonProxy getPrimaryTRAutomaton()
    {
      return mPrimaryTRAutomaton;
    }

    private TRAutomatonProxy getExtendedTRAutomaton()
    {
      return mExtendedTRAutomaton;
    }

    private EventProxy getFailureEvent()
    {
      if (mFailureEvent == null) {
        final ProductDESProxyFactory factory = getFactory();
        final String name = mInputAutomaton.getName() + ":failed";
        mFailureEvent = factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
      }
      return mFailureEvent;
    }

    //#######################################################################
    //# Automaton Construction
    private TRAutomatonProxy createExtendedPlant(final EventProxy event,
                                                 final List<EventProxy> failures)
      throws AnalysisException
    {
      final EventEncoding primaryEnc = mPrimaryTRAutomaton.getEventEncoding();
      final int e = primaryEnc.getEventCode(event);
      if (e < 0 || !isRelevantEvent(mEventStatus[e])) {
        mExtendedTRAutomaton = mPrimaryTRAutomaton;
      } else {
        final EventEncoding extendedEnc = new EventEncoding(primaryEnc);
        int f0 = -1;
        int f1 = -1;
        for (final EventProxy failure : failures) {
          f1 = extendedEnc.addProperEvent(failure, EventStatus.STATUS_NONE);
          if (f0 < 0) {
            f0 = f1;
          }
        }
        final int config = getPreferredInputConfiguration();
        final ListBufferTransitionRelation primaryRel =
          mPrimaryTRAutomaton.getTransitionRelation();
        final ListBufferTransitionRelation extendedRel =
          new ListBufferTransitionRelation(primaryRel, extendedEnc, config);
        extendedRel.setKind(ComponentKind.PLANT);
        final int numStates = extendedRel.getNumberOfStates();
        final int numFailures = f1 - f0 + 1;
        final int limit = getInternalTransitionLimit();
        int numTrans = extendedRel.getNumberOfTransitions();
        if ((config & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
          final TransitionIterator iter =
            extendedRel.createSuccessorsReadOnlyIterator();
          for (int s = 0; s < numStates; s++) {
            if (extendedRel.isReachable(s)) {
              iter.reset(s, e);
              if (iter.advance()) {
                numTrans += numFailures;
                if (numTrans > limit) {
                  throw new OverflowException(OverflowKind.TRANSITION, limit);
                }
                final int t = iter.getCurrentTargetState();
                for (int f = f0; f <= f1; f++) {
                  extendedRel.addTransition(s, f, t);
                }
              }
            }
          }
        } else {
          final TIntIntHashMap transitions =
            new TIntIntHashMap(numStates, 0.5f, -1, -1);
          final TransitionIterator iter =
            extendedRel.createAllTransitionsReadOnlyIterator(e);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            final int t = iter.getCurrentTargetState();
            transitions.putIfAbsent(s, t);
          }
          for (int s = 0; s < numStates; s++) {
            final int t = transitions.get(s);
            if (t >= 0) {
              numTrans += numFailures;
              if (numTrans > limit) {
                throw new OverflowException(OverflowKind.TRANSITION, limit);
              }
              for (int f = f0; f <= f1; f++) {
                extendedRel.addTransition(s, f, t);
              }
            }
          }
        }
        mExtendedTRAutomaton = new TRAutomatonProxy(extendedEnc, extendedRel);
      }
      return mExtendedTRAutomaton;
    }

    private TRAutomatonProxy createExtendedSpec(final EventProxy event)
      throws AnalysisException
    {
      final EventEncoding primaryEnc = mPrimaryTRAutomaton.getEventEncoding();
      final EventProxy failure = getFailureEvent();
      final int e = primaryEnc.getEventCode(event);
      final byte status = e >= 0 ? mEventStatus[e] : EventStatus.STATUS_UNUSED;
      if (!isRelevantEvent(status)) {
        mExtendedTRAutomaton = mPrimaryTRAutomaton;
      } else if (EventStatus.isBlockedEvent(status) ||
                 event == failure && EventStatus.isSelfloopOnlyEvent(status)) {
        final ListBufferTransitionRelation primaryRel =
          mPrimaryTRAutomaton.getTransitionRelation();
        final EventEncoding reducedEnc = new EventEncoding(primaryEnc);
        reducedEnc.setProperEventStatus(e, EventStatus.STATUS_UNUSED);
        final int config = getPreferredInputConfiguration();
        final ListBufferTransitionRelation reducedRel =
          new ListBufferTransitionRelation(primaryRel, reducedEnc, config);
        reducedRel.setKind(ComponentKind.PLANT);
        if (!EventStatus.isBlockedEvent(status)) {
          reducedRel.removeEvent(e);
        }
        mExtendedTRAutomaton = new TRAutomatonProxy(reducedEnc, reducedRel);
      } else {
        final EventEncoding extendedEnc = new EventEncoding(primaryEnc);
        final int f = extendedEnc.addProperEvent(failure, EventStatus.STATUS_NONE);
        final int config = getPreferredInputConfiguration();
        final ListBufferTransitionRelation primaryRel =
          mPrimaryTRAutomaton.getTransitionRelation();
        final ListBufferTransitionRelation extendedRel =
          new ListBufferTransitionRelation(primaryRel, extendedEnc, config);
        extendedRel.setKind(ComponentKind.PLANT);
        final int numStates = extendedRel.getNumberOfStates();
        final int limit = getInternalTransitionLimit();
        int numTrans = extendedRel.getNumberOfTransitions();
        if ((config & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
          final TransitionIterator iter =
            extendedRel.createSuccessorsReadOnlyIterator();
          for (int s = 0; s < numStates; s++) {
            if (extendedRel.isReachable(s)) {
              iter.reset(s, e);
              if (!iter.advance()) {
                if (++numTrans > limit) {
                  throw new OverflowException(OverflowKind.TRANSITION, limit);
                }
                extendedRel.addTransition(s, f, s);
              }
            }
          }
        } else {
          final BitSet enabling = new BitSet(numStates);
          final TransitionIterator iter =
            extendedRel.createAllTransitionsReadOnlyIterator(e);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            enabling.set(s);
          }
          for (int s = 0; s < numStates; s++) {
            if (!enabling.get(s)) {
              if (++numTrans > limit) {
                throw new OverflowException(OverflowKind.TRANSITION, limit);
              }
              extendedRel.addTransition(s, f, s);
            }
          }
        }
        mExtendedTRAutomaton = new TRAutomatonProxy(extendedEnc, extendedRel);
      }
      return mExtendedTRAutomaton;
    }

    //#######################################################################
    //# Trace Conversion
    private TRAbstractionStepInput createInputStep()
    {
      final EventEncoding enc = mPrimaryTRAutomaton.getEventEncoding();
      return new TRAbstractionStepInput(mInputAutomaton, enc);
    }

    private boolean hasTransition(final int s,
                                  final EventProxy event,
                                  final int t)
    {
      if (t < 0) {
        return false;
      }
      final EventEncoding enc = mPrimaryTRAutomaton.getEventEncoding();
      final int e = enc.getEventCode(event);
      if (e < 0) {
        return s == t;
      }
      final ListBufferTransitionRelation rel =
        mPrimaryTRAutomaton.getTransitionRelation();
      final byte status = rel.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        return s == t;
      }
      final int config = rel.getConfiguration();
      if ((config & ListBufferTransitionRelation.CONFIG_SUCCESSORS) != 0) {
        final TransitionIterator iter =
          rel.createSuccessorsReadOnlyIterator(s, e);
        while (iter.advance()) {
          if (iter.getCurrentTargetState() == t) {
            return true;
          }
        }
      } else {
        final TransitionIterator iter =
          rel.createPredecessorsReadOnlyIterator(t, e);
        while (iter.advance()) {
          if (iter.getCurrentSourceState() == s) {
            return true;
          }
        }
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mInputAutomaton;
    private final TRAutomatonProxy mPrimaryTRAutomaton;
    private final byte[] mEventStatus;
    private EventProxy mFailureEvent;
    private TRAutomatonProxy mExtendedTRAutomaton;
  }


  //#########################################################################
  //# Inner Class EventInfo
  private static class EventInfo implements Comparable<EventInfo>
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final EventProxy event)
    {
      mEvent = event;
      mBlocked = false;
      mDisablingSpecs = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private EventProxy getEvent()
    {
      return mEvent;
    }

    private boolean isBlocked()
    {
      return mBlocked;
    }

    private Collection<AutomatonInfo> getDisablingSpecs()
    {
      return mDisablingSpecs;
    }

    private void addSpec(final AutomatonInfo aut, final boolean blocked)
    {
      mBlocked |= blocked;
      mDisablingSpecs.add(aut);
    }

    //#######################################################################
    //# Interface java.lang.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
    {
      if (mBlocked != info.mBlocked) {
        return mBlocked ? -1 : 1;
      }
      final int delta = info.mDisablingSpecs.size() - mDisablingSpecs.size();
      if (delta != 0) {
        return delta;
      }
      return mEvent.compareTo(info.mEvent);
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private boolean mBlocked;
    private final List<AutomatonInfo> mDisablingSpecs;
  }


  //#########################################################################
  //# Inner Class OnePropertyKindTranslator
  private static class OnePropertyKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Simple Access
    private void setProperty(final AutomatonProxy property)
    {
      mProperty = property;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mProperty) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      return EventKind.UNCONTROLLABLE;
    }

    //#######################################################################
    //# Data Members
    private AutomatonProxy mProperty;
  }


  //#########################################################################
  //# Data Members
  private Map<AutomatonProxy,AutomatonInfo> mAutomatonInfo;
  private List<EventInfo> mEventInfo;
  private OnePropertyKindTranslator mOnePropertyKindTranslator;

}
