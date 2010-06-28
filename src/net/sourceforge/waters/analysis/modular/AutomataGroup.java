package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
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
    mCounterExampleTrace = null;
    mLoopIndex = -1;
  }
  public AutomataGroup(final AutomatonProxy initial)
  {
    mAllAutomata = new THashSet<AutomatonProxy>();
    mAllAutomata.add(initial);
    mSensitiveEvents = new THashSet<EventProxy>(initial.getEvents());
    mCounterExampleTrace = null;
    mLoopIndex = -1;
  }

  public void merge(final AutomataGroup newGroup)
  {
    mAllAutomata.addAll(newGroup.mAllAutomata);
    mSensitiveEvents.addAll(newGroup.mSensitiveEvents);
    mCounterExampleTrace = null;
    mLoopIndex = -1;
  }

  public void addAutomata(final AutomatonProxy auto)
  {
    mAllAutomata.add(auto);
    mSensitiveEvents.addAll(auto.getEvents());
    mCounterExampleTrace = null;
    mLoopIndex = -1;
  }

  public void setCounterExample(final List<EventProxy> newTrace, final int loopIndex)
  {
    mCounterExampleTrace = newTrace;
    mLoopIndex = loopIndex;
  }

  public int getLoopIndex()
  {
    return mLoopIndex;
  }

  public List<EventProxy> getTrace()
  {
    return mCounterExampleTrace;
  }

  public boolean isControlLoop(final List<EventProxy> testTrace, final int loopIndex)
  {
    for (final AutomatonProxy auto : mAllAutomata)
    {
      if (!isControlLoop(auto, testTrace, loopIndex))
        return false;
    }
    return true;
  }

  private boolean isControlLoop(final AutomatonProxy auto, final List<EventProxy> testTrace, final int loopIndex)
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
        return false;
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
            return false;
          }
        }
      }
      return (loopStart == currState);
  }

  /**
   * Runs the Monolithic SCC Control Loop Checker to determine information on the subset of Automata in this group
   * @param checker The Control Loop Checker to use. It should contain the Kind Translator, and a factory before being called
   * @return The set of non-loop events. The counter example and the loop index can be recieved by calling getTrace() and getLoopIndex()
   * @throws AnalysisException
   */
  public Collection<EventProxy> setControlLoops(final MonolithicSCCControlLoopChecker checker) throws AnalysisException
  {
    final ProductDESProxy passer = checker.getFactory().createProductDESProxy("AutomataGroup", mSensitiveEvents, mAllAutomata);
    checker.setModel(passer);
    if (checker.run())
    {
      mCounterExampleTrace = null;
      mLoopIndex = -1;
    }
    else
    {
      mCounterExampleTrace = checker.getCounterExample().getEvents();
      mLoopIndex = checker.getCounterExample().getLoopIndex();
    }
    return checker.getNonLoopEvents();
  }

  Set<AutomatonProxy> mAllAutomata;
  List<EventProxy> mCounterExampleTrace;
  Set<EventProxy> mSensitiveEvents;
  int mLoopIndex;
}
