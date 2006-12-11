package net.sourceforge.waters.analysis.modular;

import java.util.Iterator;
import java.util.PriorityQueue;
import net.sourceforge.waters.model.des.TraceProxy;
import java.util.Collections;
import java.util.Queue;
import net.sourceforge.waters.model.analysis.VerificationResult;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import java.io.File;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.plain.des.SafetyTraceElement;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElement;
import net.sourceforge.waters.model.des.EventProxy;
import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.xsd.base.ComponentKind;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;

public class ParallelModularControllabilityChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  
  public ParallelModularControllabilityChecker(ProductDESProxy model,
                                               ProductDESProxyFactory factory,
                                               ControllabilityChecker checker,
                                               ModularHeuristic heuristic)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = IdenticalKindTranslator.getInstance();
    mStates = 0;
    setStateLimit(2000000);
  }
  
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }
  
  public void setKindTranslator(KindTranslator trans)
  {
    mTranslator = trans;
  }
  
  public boolean run()
    throws AnalysisException
  {
    mStates = 0;
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
    final SortedSet<AutomatonProxy> specs = 
      new TreeSet<AutomatonProxy>(new Comparator<AutomatonProxy>() {
      public int compare(AutomatonProxy a1, AutomatonProxy a2)
      {
        if (a1.getStates().size() < a2.getStates().size()) {
          return -1;
        } else if (a1.getStates().size() > a2.getStates().size()) {
          return 1;
        }
        if (a1.getTransitions().size() < a2.getTransitions().size()) {
          return -1;
        } else if (a1.getTransitions().size() > a2.getTransitions().size()) {
          return 1;
        }
        if (a1.getEvents().size() < a2.getEvents().size()) {
          return -1;
        } else if (a1.getEvents().size() > a2.getEvents().size()) {
          return 1;
        }
        return a1.getName().compareTo(a2.getName());
      }
    });
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      break;
        default : break;
      }
    }
    if (specs.isEmpty()) {
      setSatisfiedResult();
      return true;
    }
    while (!specs.isEmpty()) {
      //System.out.println("start run");
      final Set<ParallelRun> lookedat = new HashSet<ParallelRun>();
      final Queue<ParallelRun> runs = new PriorityQueue<ParallelRun>(specs.size(),
                                                                     new Comparator<ParallelRun>()
      {
        public int compare(ParallelRun p1, ParallelRun p2)
        {
          if (p1.mStates < p2.mStates) {
            return -1;
          } else if (p1.mStates > p2.mStates) {
            return 1;
          }
          return 0;
        }
      });
      for (AutomatonProxy spec : specs) {
        runs.add(new ParallelRun(Collections.singleton(spec),
                                 new HashSet<AutomatonProxy>(),
                                 spec.getStates().size(),
                                 Collections.singleton(spec), null,
                                 spec.getEvents()));
      }
      while (!runs.isEmpty()) {
        ParallelRun run = runs.poll();
        mChecker.setModel(run.mModel);
        mChecker.setKindTranslator(new KindTranslator()
        {
          public EventKind getEventKind(EventProxy e)
          {
            return getKindTranslator().getEventKind(e);
          }
          
          public ComponentKind getComponentKind(AutomatonProxy a)
          {
            return specs.contains(a) ? ComponentKind.SPEC
                                     : ComponentKind.PLANT;
          }
        });
        mChecker.setStateLimit(getStateLimit() - mStates);
        if (!mChecker.run()) {
          Set<AutomatonProxy> cplants = 
            new HashSet<AutomatonProxy>(run.mPlants);
          Set<AutomatonProxy> cspecs = 
            new HashSet<AutomatonProxy>(run.mSpecs);
          Set<EventProxy> events = new HashSet<EventProxy>(run.mModel.getEvents());
          SortedSet<AutomatonProxy> uncomposedplants = 
            new TreeSet<AutomatonProxy>(new AutomatonComparator());
          SortedSet<AutomatonProxy> uncomposedspecplants = 
            new TreeSet<AutomatonProxy>(new AutomatonComparator());
          SortedSet<AutomatonProxy> uncomposedspecs = 
            new TreeSet<AutomatonProxy>(new AutomatonComparator());
          uncomposedplants.addAll(plants);
          uncomposedplants.removeAll(cplants);
          uncomposedspecplants.addAll(specplants);
          uncomposedspecplants.removeAll(cplants);
          uncomposedspecs.addAll(specs);
          uncomposedplants.removeAll(cspecs);
          Collection<AutomatonProxy> newComp =
            mHeuristic.heur(run.mModel,
                            uncomposedplants,
                            uncomposedspecplants,
                            uncomposedspecs,
                            mChecker.getCounterExample(),
                            getKindTranslator());
          if (newComp == null) {
            mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
            setFailedResult(mChecker.getCounterExample());
            return false;
          }
          int eventsBefore = events.size();
          double newStates = mChecker.getAnalysisResult().getTotalNumberOfStates();
          for (AutomatonProxy automaton : newComp) {
            if (specs.contains(automaton)) {
              cspecs.add(automaton);
            } else {
              cplants.add(automaton);
            }
            events.addAll(automaton.getEvents());
            newStates *= automaton.getStates().size();
            System.out.println(automaton.getName());
          }
          double numevents = events.size();
          double newEvents = events.size() - eventsBefore;
          newStates -= mChecker.getAnalysisResult().getTotalNumberOfStates();
          newStates *= (newEvents / numevents);
          newStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
          run.mCounter = mChecker.getCounterExample();
          run = new ParallelRun(cspecs, cplants, newStates,
                                newComp, run, events);
          if (lookedat.add(run)) {
            runs.offer(run);
          }
        } else {
          Collection<AutomatonProxy> changed = new ArrayList<AutomatonProxy>();
          StringBuffer thing = new StringBuffer();
          for (AutomatonProxy automaton : run.mModel.getAutomata()) {
            if (specs.contains(automaton)) {
              System.out.println(mChecker.getAnalysisResult().getTotalNumberOfStates() + " " + automaton.getName() + " size " + automaton.getStates().size());
              specs.remove(automaton);
              specplants.add(automaton);
              changed.add(automaton);
            }
          }
          /*for (ParallelRun i = run; i != null; i = i.mParent) {
            for (AutomatonProxy automaton : i.mAdded) {
              //if (specplants.contains(automaton) || specs.contains(automaton)) {
                thing.insert(0, automaton.getName());
                thing.insert(0, ',');
              //}
            }
          }
          System.out.println(thing);*/
          Iterator<ParallelRun> it = runs.iterator();
          Collection<ParallelRun> tobeadded = new ArrayList<ParallelRun>();
          while (it.hasNext()) {
            ParallelRun check = it.next();
            Collection<AutomatonProxy> changed2 = new ArrayList<AutomatonProxy>(changed);
            changed2.retainAll(check.mSpecs);
            if (!changed2.isEmpty()) {
              it.remove();
              ParallelRun neo = check(check, changed2);
              if (neo != null) {
                changed2.retainAll(neo.mSpecs);
                check = neo;
              }
              Set<AutomatonProxy> cspecs = new HashSet<AutomatonProxy>(check.mSpecs);
              Set<AutomatonProxy> cplants = new HashSet<AutomatonProxy>(check.mPlants);
              cspecs.removeAll(changed2);
              cplants.addAll(changed2);
              tobeadded.add(new ParallelRun(cspecs, cplants, check.mStates,
                                            check.mAdded, check.mParent,
                                            check.mModel.getEvents()));
            }
          }
          runs.addAll(tobeadded);
          lookedat.addAll(tobeadded);
        }
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        mChecker.setStateLimit(getStateLimit() - mStates);
      }
    }
    setSatisfiedResult();
    return true;
  }
  
  private ParallelRun check(ParallelRun run, Collection<AutomatonProxy> changed)
  {
    Collection<AutomatonProxy> changed2 = new ArrayList<AutomatonProxy>(changed);
    changed2.removeAll(run.mAdded);
    if (!changed2.isEmpty()) {
      if (run.mParent == null) {
        Set<AutomatonProxy> empty = Collections.emptySet();
        Set<EventProxy> emptyE = Collections.emptySet();
        return new ParallelRun(empty, empty, 0, empty, null, emptyE);
      }
      ParallelRun ret = check(run.mParent, changed2);
      if (ret != null) {
        return ret;
      }
    }
    if (run.mCounter == null) {
      return run;
    }
    for (AutomatonProxy automaton : changed) {
      if (!AbstractModularHeuristic.acc(automaton, run.mCounter)) {
        return run;
      }
    }
    return null;
  }
  
  protected void addStatistics(VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }
  
  private final class ParallelRun
  {
    public final ProductDESProxy mModel;
    public final Set<AutomatonProxy> mSpecs;
    public final Set<AutomatonProxy> mPlants;
    public final double mStates;
    public final Collection<AutomatonProxy> mAdded;
    public final ParallelRun mParent;
    public TraceProxy mCounter;
    
    public ParallelRun(Set<AutomatonProxy> specs, Set<AutomatonProxy> plants,
                       double states, Collection<AutomatonProxy> added,
                       ParallelRun parent, Set<EventProxy> events)
    {
      mSpecs = specs;
      mPlants = plants;
      mStates = states;
      mAdded = added;
      mParent = parent;
      Collection<AutomatonProxy> model = 
        new ArrayList<AutomatonProxy>(specs.size() + plants.size());
      model.addAll(specs);
      model.addAll(plants);
      mModel = getFactory().createProductDESProxy("comp", events, model);
    }
    
    public int hashCode()
    {
      return 17 + mSpecs.hashCode() * 31 + mPlants.hashCode() * 31;
    }
    
    public boolean equals(Object o)
    {
      if (o instanceof ParallelRun) {
        ParallelRun run = (ParallelRun) o;
        return mSpecs.equals(run.mSpecs) && mPlants.equals(run.mPlants);
      }
      return false;
    }
  }
  
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
}
