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
    try {
      setUp();
      if (getModel().getAutomata().size() == 0)
      {
        setSatisfiedResult();
        return true;
      }
      final MonolithicSCCControlLoopChecker checker = new MonolithicSCCControlLoopChecker(getModel(), mTranslator, getFactory());
      boolean removedLoopEvents = false;
      while (true)
      {
        do
        {
          removedLoopEvents = false;
          for (final AutomataGroup group : mAutoSets)
          {
            checkAbort();
            group.run(checker, mNodesRemaining);
            updateResult(group);
            final Collection<EventProxy> nonLoop = group.getNonLoopEvents();
            if (mLoopEvents.removeAll(nonLoop))
            {
              removedLoopEvents = true;
              for (final AutomataGroup invalidate : mAutoSets)
                invalidate.isChanged(nonLoop);
            }
            if (mLoopEvents.size() == 0) {
              setSatisfiedResult();
              return true;
            }
            mTranslator.removeLoopEvents(nonLoop);
            final LoopTraceProxy loop = testOne(group);
            if (loop != null) {
              setFailedResult(loop);
              return false;
            }
          }
        }
        while (removedLoopEvents);
        final LoopTraceProxy loop = testAll();
        if (loop != null) {
          setFailedResult(loop);
          return false;
        }
        boolean failed = true;
        AutomataGroup primary = null;
        int bestScore = Integer.MIN_VALUE;
        for (final AutomataGroup group : mAutoSets)
        {
          if (group.getScore() > bestScore)
          {
            bestScore = group.getScore();
            primary = group;
          }
        }
        bestScore = Integer.MIN_VALUE;
        AutomataGroup bestGroup = null;
        if (primary.getTrace() != null) {
          for (final AutomataGroup checkLoop : mAutoSets) {
            if (checkLoop != primary) {
              final int score = checkLoop.isControlLoop(primary, mTranslator);
              if (score > bestScore)
              {
                bestGroup = checkLoop;
                bestScore = score;
              }
            }
          }
          if (bestGroup != null)
          {
            checkAbort();
            //System.out.println("DEBUG: Merged with score " + bestGroup.isControlLoop(primary, mTranslator) + " at time " + System.currentTimeMillis());
            mAutoSets.remove(bestGroup);
            primary.merge(bestGroup);
            failed = false;
            mTotalCompositions++;
            updateResult(primary);
           }
        }
        else
          throw new IllegalArgumentException("Primary has no trace. This shouldn't happen, as the implementation demands that such a group has a score of Integer.MIN_VALUE");
        if (failed)
          throw new AnalysisException("ERROR: Two automata could not be selected for merging. Scoring system may not work. Num Events: " + mLoopEvents.size());
      }
    }
    finally {
      tearDown();
    }
  }

  private void updateResult(final AutomataGroup newGroup)
  {
    if (newGroup.rerun())
    {
      if (mPeakAutomata < newGroup.getStatistics().getTotalNumberOfAutomata() || mPeakAutomata == -1)
        mPeakAutomata = newGroup.getStatistics().getTotalNumberOfAutomata();
      if (mPeakStates < newGroup.getStatistics().getTotalNumberOfStates() || mPeakStates == -1)
        mPeakStates = newGroup.getStatistics().getTotalNumberOfStates();
      if (mPeakTransitions < newGroup.getStatistics().getTotalNumberOfTransitions() || mPeakTransitions == -1)
        mPeakTransitions = newGroup.getStatistics().getTotalNumberOfTransitions();
      mTotalStates += newGroup.getStatistics().getTotalNumberOfStates();
      mTotalTransitions += newGroup.getStatistics().getTotalNumberOfTransitions();
    }
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
          if (checkLoop.isControlLoop(group, mTranslator) != Integer.MIN_VALUE)
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

  public void setMergeVersion(final AutomataGroup.MergeVersion m)
  {
    AutomataGroup.setMergeVersion(m);
  }
  public void setSelectVersion(final AutomataGroup.SelectVersion s)
  {
    AutomataGroup.setSelectVersion(s);
  }
  public void setControlLoopDetection(final MonolithicSCCControlLoopChecker.CLDetector c)
  {
    MonolithicSCCControlLoopChecker.setLoopDetector(c);
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
    mPeakAutomata = 0;
    mPeakStates = 0;
    mPeakTransitions = 0;
    mTotalAutomata = getModel().getAutomata().size();
    mTotalStates = 0;
    mTotalTransitions = 0;
    mTotalCompositions = 0;
  }

  public void tearDown()
  {
    super.tearDown();
    mLoopEvents = null;
    mTranslator = null;
    mAutoSets = null;
    mPeakAutomata = -1;
    mPeakStates = -1;
    mPeakTransitions = -1;
    mTotalAutomata = -1;
    mTotalStates = -1;
    mTotalTransitions = -1;
    mTotalCompositions = -1;
  }

  private class ManipulativeTranslator implements KindTranslator
  {

    public ManipulativeTranslator(final KindTranslator base)
    {
      mBase = base;
      mFauxUncontrollable = new THashSet<EventProxy>();
    }

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
  protected LoopResult createAnalysisResult()
  {
    return new LoopResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final LoopResult result = (LoopResult) getAnalysisResult();
    result.setPeakNumberOfAutomata(mPeakAutomata);
    result.setPeakNumberOfStates(mPeakStates);
    result.setPeakNumberOfTransitions(mPeakTransitions);
    result.setTotalNumberOfAutomata(mTotalAutomata);
    result.setTotalNumberOfStates(mTotalStates);
    result.setTotalNumberOfTransitions(mTotalTransitions);
    result.setNumberOfCompositions(mTotalCompositions);
    setAnalysisResult(result);
  }

  //#########################################################################
  //# Output Data
  private int mPeakAutomata;
  private double mPeakStates;
  private double mPeakTransitions;
  private int mTotalAutomata;
  private double mTotalStates;
  private double mTotalTransitions;
  private int mTotalCompositions;

  //#########################################################################
  //# Data Members
  private ManipulativeTranslator mTranslator;
  private List<AutomataGroup> mAutoSets;
  private Set<EventProxy> mLoopEvents;
  private final int mNodesRemaining = 3000000;
}
