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
    boolean solved = false;
    try {
      setUp();
      if (getModel().getAutomata().size() == 0)
      {
        solved = true;
        output = true;
        setSatisfiedResult();
      }
      final MonolithicSCCControlLoopChecker checker = new MonolithicSCCControlLoopChecker(getModel(), mTranslator, getFactory());
      boolean removedLoopEvents = false;
      while (!solved)
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
            System.out.println("DEBUG: Loop events are " + printLoopEvents());
            if (mLoopEvents.size() == 0)
            {
              System.out.println("Solved: True");
              setSatisfiedResult();
              output = true;
              solved = true;
            }
            if (!solved)
            {
              mTranslator.removeLoopEvents(nonLoop);
              if (group.getTrace() != null)
              {
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
                  System.out.println("Solved: False");
                  final LoopTraceProxy loop =
                    getFactory().createLoopTraceProxy(getModel().getName() + "-loop",
                                                      getModel(),
                                                      group.getTrace(),
                                                      group.getLoopIndex());
                  setFailedResult(loop);
                  output = false;
                  solved = true;
                }
              }
            }
          }
        }
        while (removedLoopEvents);
        if (!solved)
        {
          outer:
          for (final AutomataGroup group : mAutoSets)
          {
            if (group.getTrace() != null)
            {
              for (final AutomataGroup checkLoop : mAutoSets)
              {
                if (checkLoop != group)
                {
                  if (!checkLoop.isControlLoop(group.getTrace(), group.getLoopIndex()));
                  {
                    mAutoSets.remove(checkLoop);
                    group.merge(checkLoop);
                    break outer;
                  }
                }
              }
            }
          }
        }
      }
      System.out.println("DEBUG: Processing complete");
    } finally {
      tearDown();
    }
    return output;
  }

  private String printLoopEvents()
  {
    String output = "\nmLoopEvents says they are ";
    for (final EventProxy event : mLoopEvents)
    {
      output += event.getName() + " ";
    }
    output += "\nmTranslator says they aren't ";
    for (final EventProxy event : mTranslator.mFauxUncontrollable)
    {
      output += event.getName() + " ";
    }
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
    mAutoSets = new THashSet<AutomataGroup>();
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
  /*
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    //final int numstates = mGlobalStateSet.size();
    //result.setNumberOfAutomata(mNumAutomata);
    //result.setNumberOfStates(numstates);
    //result.setPeakNumberOfNodes(numstates);
  }
  */


  //#########################################################################
  //# Data Members
  private ManipulativeTranslator mTranslator;
  private Set<AutomataGroup> mAutoSets;
  private Set<EventProxy> mLoopEvents;

}
