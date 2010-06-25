package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
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
    mModel = model;
    mFactory = factory;
    setKindTranslator(translator);
  }

  MonolithicSCCControlLoopChecker subChecker;

  public boolean run() throws AnalysisException
  {
    for (final AutomatonProxy aut: mModel.getAutomata())
    {
      mAutoSets.add(new AutomataGroup(aut));
    }
    final MonolithicSCCControlLoopChecker checker = new MonolithicSCCControlLoopChecker(mModel, mTranslator, mFactory);
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
            // Set mResult
            mRun = true;
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
            // Set mResult
            mRun = true;
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
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<EventProxy> getNonLoopEvents()
  {
    throw new UnsupportedOperationException("Modular Control Loop Checker does not calculate non-loop events");
  }

  public VerificationResult getAnalysisResult()
  {
    return mResult;
  }

  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }

  public boolean isSatisfied()
  {
    if (!mRun)
      throw new IllegalStateException("Program hasn't run");
    else
      return mResult.getCounterExample() == null;
  }

  public void setKindTranslator(final KindTranslator translator)
  {
    mTranslator = new ManipulativeTranslator(translator);
  }

  public void clearAnalysisResult()
  {
    mResult = new VerificationResult();
    mRun = false;
  }

  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  public ProductDESProxy getModel()
  {
    return mModel;
  }

  public int getNodeLimit()
  {
    return mNodeLimit;
  }

  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  public boolean isAborting()
  {
    return mAbort;
  }

  public void requestAbort()
  {
    mAbort = true;
  }

  public void setModel(final ProductDESProxy model)
  {
    mModel = model;
  }

  public void setModel(final AutomatonProxy aut)
  {
    final Set<AutomatonProxy> autList = new THashSet<AutomatonProxy>();
    autList.add(aut);
    mModel = mFactory.createProductDESProxy(aut.getName(), aut.getEvents(), autList);
  }

  public void setNodeLimit(final int limit)
  {
    mNodeLimit = limit;
  }

  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
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

    KindTranslator mBase;
    Set<EventProxy> mFauxUncontrollable;
  }

  int mTransitionLimit;
  int mNodeLimit;
  ProductDESProxy mModel;
  boolean mAbort;
  boolean mRun;
  ProductDESProxyFactory mFactory;
  ManipulativeTranslator mTranslator;
  VerificationResult mResult;
  Set<AutomataGroup> mAutoSets;
  Set<EventProxy> mLoopEvents;
}
