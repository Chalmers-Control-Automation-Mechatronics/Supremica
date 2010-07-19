package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class ModularControlLoopChecker
  extends AbstractModelVerifier
  implements ControlLoopChecker
{

  public ModularControlLoopChecker(final ProductDESProxyFactory factory)
  {
    this(ControllabilityKindTranslator.getInstance(), factory);
  }

  public ModularControlLoopChecker(final KindTranslator translator,
                                      final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  public ModularControlLoopChecker(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    this(model, ControllabilityKindTranslator.getInstance(), factory);
  }

  public ModularControlLoopChecker(final ProductDESProxy model,
                                   final KindTranslator translator,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory, translator);
    createAnalysisResult();
  }


  public boolean run() throws AnalysisException
  {
    boolean output = false;
    try {
      setUp();
      if (getModel().getAutomata().size() == 0)
      {
        setSatisfiedResult();
        return true;
      }
      final MonolithicSCCControlLoopChecker checker = new MonolithicSCCControlLoopChecker(getModel(), mTranslator, getFactory());
      boolean removedLoopEvents = false;
      solved:
      while (true)
      {
        do
        {
          removedLoopEvents = false;
          for (final AutomataGroup group : mAutoSets)
          {
            group.run(checker);
            final Collection<EventProxy> nonLoop = group.getNonLoopEvents();
            if (mLoopEvents.removeAll(nonLoop))
            {
              removedLoopEvents = true;
            }
            if (mLoopEvents.size() == 0)
            {
              setSatisfiedResult();
              output = true;
              break solved;
            }
            mTranslator.removeLoopEvents(nonLoop);
            final LoopTraceProxy loop = testOne(group);
            if (loop != null)
            {
              setFailedResult(loop);
              output = false;
              break solved;
            }
          }
        }
        while (removedLoopEvents);
        final LoopTraceProxy loop = testAll();
        if (loop != null)
        {
          setFailedResult(loop);
          output = false;
          break solved;
        }
        switch (MERGE_VERSION)
        {
        case 0:
          mergeFirstTwo();
          break;
        case 1:
          mergeTwoSmallest();
          break;
        default:
          throw new UnsupportedOperationException("ERROR: Merge version is not supported");
        }
      }
    } finally {
      tearDown();
    }
    return output;
  }

  private LoopTraceProxy testAll()
  {
    LoopTraceProxy output = null;
    for (final AutomataGroup auto : mAutoSets)
    {
      output = testOne(auto);
      if (output != null)
        return output;
    }
    return null;
  }

  private LoopTraceProxy testOne(final AutomataGroup group)
  {
    if (group.getTrace() != null)
    {
      boolean acceptsAll = true;
      for (final AutomataGroup checkLoop : mAutoSets)
      {
        if (checkLoop != group)
        {
          if (!checkLoop.isControlLoop(group.getTrace(), group.getLoopIndex()))
          {
            acceptsAll = false;
            break;
          }
        }
      }
      if (acceptsAll)
      {
        return group.getLoopTraceProxy();
      }
    }
    return null;
  }

  private void mergeFirstTwo()
  {
    for (final AutomataGroup group : mAutoSets) {
      if (group.getTrace() != null) {
        for (final AutomataGroup checkLoop : mAutoSets) {
          if (checkLoop != group) {
            if (!checkLoop.isControlLoop(group.getTrace(), group.getLoopIndex()))
            {
              mAutoSets.remove(checkLoop);
              group.merge(checkLoop);
              return;
            }
          }
        }
      }
    }
  }

  private void mergeTwoSmallest()
  {
    System.out.println("DEBUG: Merging");
    AutomataGroup smallest = null;
    int smallestIndex = -1;
    double size = Double.MAX_VALUE;
    for (int looper = 0; looper < mAutoSets.size(); looper++)
    {
      if (mAutoSets.get(looper).getTrace() != null)
      {
        if (mAutoSets.get(looper).getStatistics().getTotalNumberOfStates() < size)
        {
          size = mAutoSets.get(looper).getStatistics().getTotalNumberOfStates();
          smallest = mAutoSets.get(looper);
          smallestIndex = looper;
        }
      }
    }
    for (int looper = 0; looper < mAutoSets.size(); looper++)
    {
      if (mAutoSets.get(looper).getTrace() != null)
      {
        if (looper != smallestIndex)
        {
          if (!mAutoSets.get(looper).isControlLoop(
              smallest.getTrace(), smallest.getLoopIndex())
              && !smallest.isControlLoop(
              mAutoSets.get(looper).getTrace(), mAutoSets.get(looper).getLoopIndex()))
          {
            smallest.merge(mAutoSets.get(looper));
            mAutoSets.remove(mAutoSets.get(looper));
            return;
          }
        }
      }
    }
    // If it gets this far, then the very strict check for Counter examples has failed: Use a less strict version
    for (int looper = 0; looper < mAutoSets.size(); looper++)
    {
      if (looper != smallestIndex)
      {
        if (!mAutoSets.get(looper).isControlLoop(
                                                 smallest.getTrace(), smallest.getLoopIndex()))
        {
          smallest.merge(mAutoSets.get(looper));
          mAutoSets.remove(mAutoSets.get(looper));
          return;
        }
      }
    }
    // If it gets THIS far, then something has gone wrong. Run testAll() to ensure a counter example hasn't gone missing
    final LoopTraceProxy confusion = testOne(mAutoSets.get(smallestIndex));
    if (confusion != null)
      throw new IllegalArgumentException("ERROR: testAll was not called in time");
    else
      throw new IllegalArgumentException("ERROR: testAll did not successfully detect forced control loop for group " + smallestIndex);
  }

  @SuppressWarnings("unused")
  private String printLoopEvents()
  {
    String output = "\nmLoopEvents says they are ";
    for (final EventProxy event : mLoopEvents)
    {
      output += event.getName() + " ";
    }
    /*
    output += "\nmTranslator says they aren't ";
    for (final EventProxy event : mTranslator.mFauxUncontrollable)
    {
      output += event.getName() + " ";
    }*/
    return output;
  }

  public LoopTraceProxy getCounterExample()
  {
      return (LoopTraceProxy) super.getCounterExample();
  }

  public Collection<EventProxy> getNonLoopEvents()
  {
    throw new UnsupportedOperationException("Modular Control Loop Checker does not calculate non-loop events");
  }

  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    mTranslator = new ManipulativeTranslator(translator);
    clearAnalysisResult();
  }

  public void setUp() throws AnalysisException
  {
    super.setUp();
    mAutoSets = new ArrayList<AutomataGroup>();
    mTranslator = new ManipulativeTranslator(getKindTranslator());
    mLoopEvents = new THashSet<EventProxy>();
    for (final EventProxy event : getModel().getEvents())
    {
      if (event.getKind() == EventKind.CONTROLLABLE)
        mLoopEvents.add(event);
    }
    for (final AutomatonProxy aut: getModel().getAutomata())
    {
      mAutoSets.add(new AutomataGroup(aut));
    }
  }

  public void tearDown()
  {
    super.tearDown();
    mLoopEvents = null;
    mTranslator = null;
    mAutoSets = null;
  }

  private class ManipulativeTranslator implements KindTranslator
  {

    public ManipulativeTranslator(final KindTranslator base)
    {
      mBase = base;
      mFauxUncontrollable = new THashSet<EventProxy>();
    }

    // Please can the person who is adding a suppressWarnings("unused") please stop doing so. This is
    // a public method, and thus the suppressWarnings method just creates a warning itself, and that
    // warning says that the suppressWarnings statement is not needed. Thanks, ach17
    // Then declare the method private. A private class does not need public
    // methods (unless they are in an interface). Or delete the method---
    // after all, it is not used. Robi
    @SuppressWarnings("unused")
    private void removeLoopEvents(final EventProxy event)
    {
      mFauxUncontrollable.add(event);
    }

    public void removeLoopEvents(final Collection<EventProxy> event)
    {
      mFauxUncontrollable.addAll(event);
    }

    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      return mBase.getComponentKind(aut);
    }

    public EventKind getEventKind(final EventProxy event)
    {
      if (mFauxUncontrollable.contains(event))
        return EventKind.UNCONTROLLABLE;
      else
        return mBase.getEventKind(event);
    }

    private final KindTranslator mBase;
    private final Set<EventProxy> mFauxUncontrollable;
  }


  //#########################################################################
  //# Setting the Result
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    double maxStates = -1;
    double maxTransitions = -1;
    int maxAutomata = -1;
    for (final AutomataGroup group : mAutoSets)
    {
      if (group.getStatistics() != null)
      {
        if (group.getStatistics().getTotalNumberOfAutomata() > maxAutomata)
          maxAutomata = group.getStatistics().getTotalNumberOfAutomata();
        if (group.getStatistics().getTotalNumberOfStates() > maxStates)
          maxStates = group.getStatistics().getTotalNumberOfStates();
        if (group.getStatistics().getTotalNumberOfTransitions() > maxTransitions)
          maxTransitions = group.getStatistics().getTotalNumberOfTransitions();
      }
    }
    result.setPeakNumberOfNodes(maxAutomata);
    result.setPeakNumberOfStates(maxStates);
    result.setPeakNumberOfTransitions(maxTransitions);
    //final int numstates = mGlobalStateSet.size();
    //result.setNumberOfAutomata(mNumAutomata);
    //result.setNumberOfStates(numstates);
    //result.setPeakNumberOfNodes(numstates);
  }


  //#########################################################################
  //# Data Members
  private ManipulativeTranslator mTranslator;
  private List<AutomataGroup> mAutoSets;
  private Set<EventProxy> mLoopEvents;

  //#########################################################################
  //# Constant Values

  /**
   * 0 = Merge First Two values
   * 1 = Merge Two Smallest Groups
   */
  private static final int MERGE_VERSION = 1;

}
