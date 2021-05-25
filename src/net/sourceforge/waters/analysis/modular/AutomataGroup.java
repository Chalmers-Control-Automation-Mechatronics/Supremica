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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

public class AutomataGroup
{
  public AutomataGroup(final Set<AutomatonProxy> initial)
  {
    mAllAutomata = initial;
    mSensitiveEvents = new THashSet<EventProxy>();
    for (final AutomatonProxy auto : initial)
    {
      mSensitiveEvents.addAll(auto.getEvents());
    }
    mCounterExample = null;
  }
  public AutomataGroup(final AutomatonProxy initial)
  {
    mAllAutomata = new THashSet<AutomatonProxy>();
    mAllAutomata.add(initial);
    mSensitiveEvents = new THashSet<EventProxy>(initial.getEvents());
    mCounterExample = null;
  }

  public void merge(final AutomataGroup newGroup)
  {
    mAllAutomata.addAll(newGroup.mAllAutomata);
    mSensitiveEvents.addAll(newGroup.mSensitiveEvents);
    mCounterExample = null;
    mValidRun = false;
    mValidStats = false;
  }

  public void addAutomata(final AutomatonProxy auto)
  {
    mAllAutomata.add(auto);
    mSensitiveEvents.addAll(auto.getEvents());
    mCounterExample = null;
    mValidRun = false;
    mValidStats = false;
  }

  public void setCounterExample(final LoopCounterExampleProxy lProxy)
  {
    mCounterExample = lProxy;
    mValidRun = false;
    mValidStats = false;
  }

  public static void setMergeVersion(final MergeVersion newVersion)
  {
    MERGE_VERSION = newVersion;
  }

  public static MergeVersion getMergeVersion()
  {
    return MERGE_VERSION;
  }

  public static void setSelectVersion(final SelectVersion selectVersion)
  {
    SELECT_VERSION = selectVersion;
  }

  public int getLoopIndex()
  {
    if (mCounterExample != null) {
      final TraceProxy trace = mCounterExample.getTrace();
      return trace.getLoopIndex();
    } else {
      return -1;
    }
  }

  public List<EventProxy> getTrace()
  {
    if (mCounterExample != null) {
      final TraceProxy trace = mCounterExample.getTrace();
      return trace.getEvents();
    } else {
      return null;
    }
  }

  public LoopCounterExampleProxy getCounterExample()
  {
    return mCounterExample;
  }

  public Collection<EventProxy> getNonLoopEvents()
  {
    return mNonLoopEvents;
  }

  public Collection<EventProxy> getEvents()
  {
    final ArrayList<EventProxy> output = new ArrayList<EventProxy>();
    for (final AutomatonProxy auto : mAllAutomata)
      output.addAll(auto.getEvents());
    return output;
  }

  /**
   * Returns the score to be the primary automata group to be merged, depending on the merging method
   * Note that a group with no counter-example automatically gets a score of Integer.MIN_VALUE, regardless of the
   * merging method.
   * @return The score of this group. The highest score is the primary merging candidate
   */
  public int getScore()
  {
    if (getCounterExample() == null)
      return Integer.MIN_VALUE;
    switch (SELECT_VERSION)
    {
    case Naive:
      return 1;
    }
    throw new UnsupportedOperationException("Merging method not supported");
  }

  /**
   * Returns a score (Highest being the best, Lowest being the worst) determining how well the group is a candidate for merging
   * It returns Integer.MIN_VALUE if the control loop is accepting
   * @param otherGroup The primary merging candidate
   * @param trans The translator used in the SCC running phase
   * @return A score determining the candicy of how well it will merge with the other automata, highest being the best, and
   * Integer.MIN_VALUE meaning it accepts the control loop
   */
  public int isControlLoop(final AutomataGroup otherGroup, final KindTranslator trans)
  {
    final List<EventProxy> testTrace = otherGroup.getTrace();
    final int loopIndex = otherGroup.getLoopIndex();
    int constantOutput = Integer.MIN_VALUE;
    int output = Integer.MIN_VALUE;
    switch (MERGE_VERSION)
    {
    case One:
      constantOutput = 1;
      break;
    case MinAutomata:
      constantOutput = 0 - mAllAutomata.size(); // The less automata, the better
      break;
    case EarlyNotAccept:
      output = Integer.MAX_VALUE;
      break;
    case LateNotAccept:
      output = Integer.MIN_VALUE;
      break;
    case MaxCommonEvents:
      constantOutput = 0;
      final Collection<EventProxy> thisEvents = getEvents();
      for (final EventProxy otherEvent : otherGroup.getEvents())
      {
        if (thisEvents.contains(otherEvent))
          constantOutput++;
      }
      break;
    case MaxCommonUncontr:
      constantOutput = 0;
      final Collection<EventProxy> thisUncontEvents = getEvents();
      for (final EventProxy otherEvent : otherGroup.getEvents())
      {
        if (thisUncontEvents.contains(otherEvent) && trans.getEventKind(otherEvent) == EventKind.UNCONTROLLABLE)
          constantOutput++;
      }
      break;
    case MinEvents:
      constantOutput = 0 - getEvents().size();
      break;
    case MinNewEvents:
      int shared = 0;
      final Collection<EventProxy> otherDifferentEvents = otherGroup.getEvents();
      for (final EventProxy thisDifferentEvent : getEvents())
      {
        if (otherDifferentEvents.contains(thisDifferentEvent))
          shared++;
      }
      constantOutput = shared - getEvents().size();
      break;
    case RelMaxCommonEvents:
      int relativeShared = 0;
      final Collection<EventProxy> otherRelativeEvents = otherGroup.getEvents();
      for (final EventProxy thisDifferentEvent : getEvents())
      {
        if (otherRelativeEvents.contains(thisDifferentEvent))
          relativeShared++;
      }
      constantOutput = relativeShared / getEvents().size();
      break;
    case MinStates:
      if (mStats == null) // In this case, we are testing if it is a control loop or not, so we don't need to know the number of states
        constantOutput = 1;
      else
        constantOutput = (int)(0 - mStats.getTotalNumberOfStates());
    }
    for (final AutomatonProxy auto : mAllAutomata)
    {
      final int thisAutoScore = isControlLoop(auto, testTrace, loopIndex);
      if (thisAutoScore != Integer.MIN_VALUE)
      {
        if (constantOutput != Integer.MIN_VALUE)
        {
          return constantOutput;
        }
        else
        {
          if (MERGE_VERSION == MergeVersion.EarlyNotAccept)
          {
            if (thisAutoScore < output)
              output = thisAutoScore;
          }
          else if (MERGE_VERSION == MergeVersion.LateNotAccept)
          {
            if (thisAutoScore > output)
              output = thisAutoScore;
          }
        }
      }
    }
    if (MERGE_VERSION == MergeVersion.EarlyNotAccept && output == Integer.MAX_VALUE)
      output = Integer.MIN_VALUE; // In this case, all automata accept the counter-example, so say so
    return output;
  }

  /**
   * Returns the number of events the control loop accepted. If the entire counter example was accepted, it returns Integer.MIN_VALUE
   * @param auto The Automaton in this group which is being checked for the candicy for merging
   * @param testTrace The trace from the other automata group which is being merged
   * @param loopIndex The index in the trace where the loop starts
   * @return A score determining the candicy of how well it will merge with the other automata, highest being the best, and
   * Integer.MIN_VALUE meaning it accepts the control loop
   */
  private int isControlLoop(final AutomatonProxy auto, final List<EventProxy> testTrace, final int loopIndex)
  {
      final Collection<EventProxy> events = auto.getEvents();
      final Collection<StateProxy> states = auto.getStates();
      final Collection<TransitionProxy> transitions = auto.getTransitions();

      StateProxy currState = null;
      for(final StateProxy sProxy: states){
        if(sProxy.isInitial()){
          currState = sProxy;
          break;
        }
      }

      if(currState == null){
        throw new IllegalArgumentException("ERROR: No initial state!");
      }

      int index = 0;
      StateProxy loopStart = null;
      for (final EventProxy eProxy: testTrace){
        if (index++ == loopIndex) {
          loopStart = currState;
        }
        if (events.contains(eProxy)) {
          boolean found = false;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == currState && trans.getEvent() == eProxy) {
              currState = trans.getTarget();
              found = true;
              break;
            }
          }
          if (!found) {
            return index;
          }
        }
      }
      if (loopStart == currState)
        return Integer.MIN_VALUE;
      else
        return index;
  }

  /**
   * Runs the Monolithic SCC Control Loop Checker to determine information on
   * the subset of Automata in this group.
   * @param monolithicVerifier The Control Loop Checker to use. It should contain the
   *                {@link KindTranslator} and a factory before being called.
   * @param nodesRemaining
   *                The amount of space left in the model checker.
   */
  void run(final ControlLoopChecker monolithicVerifier,
           final int nodesRemaining)
    throws AnalysisException
  {
    if (mValidRun)
      return;
    int spaceLeft;
    if (monolithicVerifier.getAnalysisResult() != null)
      spaceLeft = (int) (nodesRemaining + monolithicVerifier.getAnalysisResult().getTotalNumberOfStates());
    else
      spaceLeft = nodesRemaining;
    final ProductDESProxy passer;
    passer = monolithicVerifier.getFactory().createProductDESProxy(getName() , mSensitiveEvents, mAllAutomata);
    monolithicVerifier.setModel(passer);
    monolithicVerifier.setNodeLimit(spaceLeft);
    if (monolithicVerifier.run())
    {
      mCounterExample = null;
    }
    else
    {
      mCounterExample = monolithicVerifier.getCounterExample();
    }
    spaceLeft = (int) (spaceLeft - monolithicVerifier.getAnalysisResult().getTotalNumberOfStates());
    mNonLoopEvents = monolithicVerifier.getNonLoopEvents();
    mStats = monolithicVerifier.getAnalysisResult();
    //System.out.println(getStatisticsText());
    //System.out.println(getTextNonLoop());
    mValidRun = true;
    mValidStats = false;
  }
  /**
   * Checks to see if the stats have changed. Regardless, it then invalidates the stats
   * Calling rerun();rerun(); is guaranteed to return FALSE on the second call.
   * @return TRUE if the stats have changed (and thus they should be updated). FALSE otherwise
   */
  public boolean rerun()
  {
    final boolean output = !mValidStats;
    mValidStats = true;
    return output;
  }

  /**
   * Invalidates the run if an automata within this group is sensitive to one of these events
   * Pass an empty array, or null to return if the run is valid or not
   * @param changedEvents The list of events which have recently become faux-uncontrollable
   * @return TRUE if the run has been invalidated, FALSE otherwise
   */
  public boolean isChanged(final Collection<EventProxy> changedEvents)
  {
    if (changedEvents == null)
      return mValidRun;
    if (!mValidRun)
      return false;
    for (final AutomatonProxy auto : mAllAutomata)
    {
      for (final EventProxy event : changedEvents)
      {
        if (auto.getEvents().contains(event))
        {
          mValidRun = false;
          return false;
        }
      }
    }
    return true;
  }

  @SuppressWarnings("unused")
  private String getTextNonLoop()
  {
    String output = "Non Loop Events from " + getName() + " are ";
    for (final EventProxy event : mNonLoopEvents)
    {
      output += event.getName() + " ";
    }
    return output;
  }
  private String getName()
  {
    String output = "";
    for (final AutomatonProxy auto : mAllAutomata)
    {
      output += auto.getName() + "|";
    }
    output = output.substring(0, output.length() - 1);
    return output;
  }
  public VerificationResult getStatistics()
  {
    return mStats;
  }
  public String getStatisticsText()
  {
    return "Stats: Automata:" + mStats.getTotalNumberOfAutomata() + ". States: " + mStats.getTotalNumberOfStates() + ". Transitions: " + mStats.getTotalNumberOfTransitions() + ". Runtime: " + mStats.getRunTime();
  }

  Collection<EventProxy> mNonLoopEvents;
  Set<AutomatonProxy> mAllAutomata;
  LoopCounterExampleProxy mCounterExample;
  Set<EventProxy> mSensitiveEvents;
  VerificationResult mStats;
  /** This is used to ensure that the MonolithicSCCControl Loop Checker is not run a second time, when no data has changed
   * It is TRUE when nothing has changed, and FALSE otherwise */
  boolean mValidRun;
  /** This is used to ensure that the statistics are not updated a second time, when no data has changed
   * It is TRUE when nothing has changed, and FALSE otherwise*/
  boolean mValidStats;


  //#########################################################################
  //# Constant Values
  /**
   * 0 = One
   * 1 = MinAutomata
   * 2 = EarlyNotAccept
   * 3 = LateNotAccept
   * 4 = MaxCommonEvents
   * 5 = MaxCommonUncontr
   * 6 = MinEvents
   * 7 = MinNewEvents
   * 8 = RelMaxCommonEvents
   * 9 = MinStates
   */
  private static MergeVersion MERGE_VERSION = MergeVersion.MaxCommonEvents;

  private static SelectVersion SELECT_VERSION = SelectVersion.Naive;


  //##################################################################
  //# Enumerations
  public enum MergeVersion {
    One,
    MinAutomata,
    EarlyNotAccept,
    LateNotAccept,
    MaxCommonEvents,
    MaxCommonUncontr,
    MinEvents,
    MinNewEvents,
    RelMaxCommonEvents,
    MinStates
  }

  public enum SelectVersion {
    Naive
  }
}
