package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.Collection;
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
    setKindTranslator(translator);
    mAutoSets = new THashSet<AutomataGroup>();
    createAnalysisResult();
  }


  public boolean run() throws AnalysisException
  {
    if (getModel().getAutomata().size() == 0)
      return setSatisfiedResult();
    for (final AutomatonProxy aut: getModel().getAutomata())
    {
      mAutoSets.add(new AutomataGroup(aut));
    }
    final MonolithicSCCControlLoopChecker checker = new MonolithicSCCControlLoopChecker(getModel(), mTranslator, getFactory());
    boolean removedLoopEvents = false;
    do
    {
      do
      {
        removedLoopEvents = false;
        for (final AutomataGroup group : mAutoSets)
        {
          final Collection<EventProxy> nonLoop = group.setControlLoops(checker);
          if (mLoopEvents.removeAll(nonLoop))
            removedLoopEvents = true;
          if (mLoopEvents.size() == 0)
          {
            setSatisfiedResult();
            return true;
          }
          mTranslator.removeLoopEvents(nonLoop);
          boolean acceptsAll = true;
          for (final AutomataGroup checkLoop : mAutoSets)
          {
            if (checkLoop != group)
            {
              if (!checkLoop.isControlLoop(group.getTrace(), group.getLoopIndex()));
              {
                acceptsAll = false;
                break;
              }
            }
          }
          if (acceptsAll)
          {
            mLoop = getFactory().createLoopTraceProxy(getModel().getName() + "-loop",
                                                      getModel(),
                                                      group.getTrace(),
                                                      group.getLoopIndex());
            setFailedResult(mLoop);
            return false;
          }
        }
      }
      while (removedLoopEvents);
      boolean merged = false;
      for (final AutomataGroup group : mAutoSets)
      {
        for (final AutomataGroup checkLoop : mAutoSets)
        {
          if (checkLoop != group)
          {
            if (!checkLoop.isControlLoop(group.getTrace(), group.getLoopIndex()));
            {
              merged = true;
              mAutoSets.remove(checkLoop);
              group.merge(checkLoop);
              break;
            }
          }
        }
        if (merged)
          break;
      }
    }
    while (true);
  }

  public LoopTraceProxy getCounterExample()
  {
      return mLoop;
  }

  public Collection<EventProxy> getNonLoopEvents()
  {
    throw new UnsupportedOperationException("Modular Control Loop Checker does not calculate non-loop events");
  }

  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }

  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(new ManipulativeTranslator(translator));
    clearAnalysisResult();
  }

  private class ManipulativeTranslator implements KindTranslator
  {

    public ManipulativeTranslator(final KindTranslator base)
    {
      mBase = base;
      mFauxUncontrollable = new THashSet<EventProxy>();
    }

    public void removeLoopEvents(final EventProxy event)
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
    //final int numstates = mGlobalStateSet.size();
    //result.setNumberOfAutomata(mNumAutomata);
    //result.setNumberOfStates(numstates);
    //result.setPeakNumberOfNodes(numstates);
    result.setCounterExample(getCounterExample());
  }

  LoopTraceProxy mLoop;
  ManipulativeTranslator mTranslator;
  Set<AutomataGroup> mAutoSets;
  Set<EventProxy> mLoopEvents;
}
