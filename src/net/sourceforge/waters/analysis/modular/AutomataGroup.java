package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
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
    mLoopTraceProxy = null;
  }
  public AutomataGroup(final AutomatonProxy initial)
  {
    mAllAutomata = new THashSet<AutomatonProxy>();
    mAllAutomata.add(initial);
    mSensitiveEvents = new THashSet<EventProxy>(initial.getEvents());
    mLoopTraceProxy = null;
  }

  public void merge(final AutomataGroup newGroup)
  {
    mAllAutomata.addAll(newGroup.mAllAutomata);
    mSensitiveEvents.addAll(newGroup.mSensitiveEvents);
    mLoopTraceProxy = null;
  }

  public void addAutomata(final AutomatonProxy auto)
  {
    mAllAutomata.add(auto);
    mSensitiveEvents.addAll(auto.getEvents());
    mLoopTraceProxy = null;
  }

  public void setCounterExample(final LoopTraceProxy lProxy)
  {
    mLoopTraceProxy = lProxy;
  }

  public int getLoopIndex()
  {
    if (mLoopTraceProxy != null)
      return mLoopTraceProxy.getLoopIndex();
    else
      return -1;
  }

  public List<EventProxy> getTrace()
  {
    if (mLoopTraceProxy != null)
      return mLoopTraceProxy.getEvents();
    else
      return null;
  }

  public LoopTraceProxy getLoopTraceProxy()
  {
    return mLoopTraceProxy;
  }

  public Collection<EventProxy> getNonLoopEvents()
  {
    return mNonLoopEvents;
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
   * @throws AnalysisException
   */
  public void run(final MonolithicSCCControlLoopChecker checker) throws AnalysisException
  {
    final ProductDESProxy passer;
    passer = checker.getFactory().createProductDESProxy(getName() , mSensitiveEvents, mAllAutomata);
    checker.setModel(passer);
    if (checker.run())
    {
      mLoopTraceProxy = null;
    }
    else
    {
      mLoopTraceProxy = checker.getCounterExample();
    }
    mNonLoopEvents = checker.getNonLoopEvents();
    mStats = checker.getAnalysisResult();
    //System.out.println(getStatisticsText());
    //System.out.println(getTextNonLoop());
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
  LoopTraceProxy mLoopTraceProxy;
  Set<EventProxy> mSensitiveEvents;
  VerificationResult mStats;
}
