
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms.HDS;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.EFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAEventDecl;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAState;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.SupremicaException;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class EFASynchronizer
 implements Abortable
{

  //TODO HDS: Accept ComponentProxy 
  public EFASynchronizer(final List<SimpleEFAComponent> components,
                         final SynchronizationOptions options)
   throws AnalysisException
  {
    options.setEFAMode(true);
    mComponents = components;
    mVariables = new THashSet<>();
    final Automata automata = removeGuardsActionsFromEFAs(components);
    mFactory = components.iterator().next().getFactory();
    mAutomata = automata;
    syncOptions = options;
    mHelper = new EFAHelper(mFactory);
    synchHelper = new AutomataSynchronizerHelper(automata, options,
                                                 arc2edgeTable,
                                                 autName2indexTable, false);
    initialize();
    System.err.println("Synchronizer initialized ...");
  }

  public void execute()
  {
    State currInitialState;
    final int[] initialState = AutomataIndexFormHelper.createState(mAutomata
     .size());

    // Build the initial state - and the comment
    final Iterator<Automaton> autIt = mAutomata.iterator();
    final StringBuffer comment = new StringBuffer();

    // Set an apropriate comment on the automaton
    while (autIt.hasNext()) {
      final Automaton currAutomaton = autIt.next();

      currInitialState = currAutomaton.getInitialState();
      initialState[indexMap.getAutomatonIndex(currAutomaton)] = indexMap
       .getStateIndex(currAutomaton, currInitialState);

      comment.append(currAutomaton.getName());
      comment.append(syncOptions.getAutomatonNameSeparator());
    }
    comment.delete(comment.length() - syncOptions.getAutomatonNameSeparator()
     .length(), comment.length());
    try {
      synchHelper.addState(initialState);
    } catch (final SupremicaException e1) {
      throw new RuntimeException(e1);
    }
    synchHelper.addComment(comment.toString());

    // Start all the synchronization executers and wait for completetion
    for (final AutomataSynchronizerExecuter synchExecuter
         : synchronizationExecuters) {
      synchExecuter.start();
    }

    // Wait for completion
    try {
      for (final AutomataSynchronizerExecuter synchExecuter
           : synchronizationExecuters) {
        synchExecuter.join();
      }
    } catch (final InterruptedException e) {
      // Current thread has been interrupted, perhaps
      // due to an exception in one of the executers.
      // Stop all tasks and throw the original exception
      for (final AutomataSynchronizerExecuter synchExecuter
           : synchronizationExecuters) {
        synchExecuter.requestStop();
      }
      for (final AutomataSynchronizerExecuter synchExecuter
           : synchronizationExecuters) {
        final Throwable cause = synchExecuter.getCauseOfInterrupt();
        if (cause != null) {
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          } else {
            throw new RuntimeException(cause);
          }
        }
      }
    }
  }

  public void displayInfo()
  {
    synchHelper.printStatistics();
  }

  // -- MF -- Added to allow users easy access to the number of synch'ed states
  public long getNumberOfStates()
  {
    return synchHelper.getNumberOfAddedStates();
  }

  public AutomataSynchronizerHelper getHelper()
  {
    return synchHelper;
  }

  /**
   * Help the garbage collector by clearing variables.
   */
  public void clear()
  {
    mAutomata = null;
    synchHelper = null;
    syncOptions = null;
    synchronizationExecuters.clear();
    synchronizationExecuters = null;
  }

  @Override
  public void requestAbort()
  {
    abortRequested = true;

    for (int i = 0; i < synchronizationExecuters.size(); i++) {
      synchronizationExecuters.get(i).requestStop();
    }
  }

  @Override
  public boolean isAborting()
  {
    return abortRequested;
  }

  @Override
  public void resetAbort()
  {
    abortRequested = false;
  }

  public void setStateNameAsValue(boolean enable)
  {
    mStateNameAsValue = enable;
  }

  public SimpleEFAComponent getSynchronizedEFA(String name) throws EvalException
  {
    System.err.println("Synchronizer start executing ...");
    execute();
    final Automaton automaton = getAutomaton();
    System.err.println("Synchronizer finish executing ...");
    name = name.isEmpty() ? automaton.getName() : name;
    synchHelper.createExtendedAutomaton(name);

    final List<ComponentProxy> list =
     new ArrayList<>(mVariables.size() + 1);
    final SimpleComponentProxy synchEFA = synchHelper.getSynchronizedComponent();
    System.err.println("Synchronized component created ...");
    list.add(synchEFA);
    for (final SimpleEFAVariable variable : mVariables) {
      list.add(variable.getVariableComponent(mFactory));
    }
    final Collection<SimpleEFAEventDecl> events = new THashSet<>();
    for (final SimpleEFAComponent component : mComponents) {
      events.addAll(component.getAlphabet());
    }
    final Collection<EventDeclProxy> edecls =
     mHelper.getEventDeclProxy(events);
    final ModuleProxy module = mFactory.createModuleProxy(
     synchEFA.getName(), null, null, null, edecls, null, list);
    final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
    final SimpleEFASystem system = compiler.compile();
    final SimpleEFAComponent efa = system.getComponents().iterator().next();
    efa.setBlockedEvents(null);
    efa.setDeterministic(automaton.isDeterministic());
    if (mStateNameAsValue) {
      readStateNames(efa.getStateEncoding());
    }
    return efa;
  }

  /**
   * Initializing the AutomataSynchronizerExecuter:s based on the
   * AutomataSynchronizerHelper.
   */
  private void initialize()
  {
    // Allocate and initialize the synchronizationExecuters
    final int nbrOfExecuters = syncOptions.getNbrOfExecuters();
    synchronizationExecuters = new ArrayList<>(
     nbrOfExecuters);
    for (int i = 0; i < nbrOfExecuters; i++) {
      final AutomataSynchronizerExecuter currSynchronizationExecuter =
       new AutomataSynchronizerExecuter(synchHelper);
      synchronizationExecuters.add(currSynchronizationExecuter);
    }
    indexMap = synchHelper.getIndexMap();
    for (final SimpleEFAComponent component : mComponents) {
      mVariables.addAll(component.getVariables());
    }
  }

  private Automaton getAutomaton()
  {
    final AutomataSynchronizerExecuter currExec = synchronizationExecuters
     .get(0);

    if (currExec.buildAutomaton()) {
      return synchHelper.getAutomaton();
    } else {
      return null;
    }
  }

  /**
   * Method for synchronizing Automata with default options.
   * <p/>
   * @param automata the Automata to be synchronized.
   * <p/>
   * @return Automaton representing the synchronous composition.
   */
  @SuppressWarnings("unused")
  private static Automaton synchronizeAutomata(final Automata automata)
   throws Exception
  {
    return synchronizeAutomata(automata, false);
  }

  /**
   * Method for synchronizing Automata with default options.
   * <p/>
   * @param automata the Automata to be synchronized.
   * <p/>
   * @return Automaton representing the synchronous composition.
   */
  private static Automaton synchronizeAutomata(final Automata automata,
                                               final boolean sups_as_plants)
   throws Exception
  {
    final SynchronizationOptions options = SynchronizationOptions
     .getDefaultSynchronizationOptions();
    return synchronizeAutomata(automata, options, sups_as_plants);
  }

  /**
   * Method for synchronizing Automata with supplied options.
   * <p/>
   * @param automata the Automata to be synchronized.
   * @param options  the SynchronizationOptions that should be used.
   * <p/>
   * @return Automaton representing the synchronous composition.
   */
  private static Automaton synchronizeAutomata(
   final Automata automata, final SynchronizationOptions options,
   final boolean sups_as_plants)
   throws Exception
  {
    options.setEFAMode(false);
    final AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(
     automata, options, sups_as_plants);
    return synchronizeAutomata(helper);
  }

  /**
   * Method for synchronizing Automata based on an already existing
   * AutomataSynchronizerHelper. The helper includes the options and the
   * automata to be composed!
   * <p/>
   * @param helper the AutomataSynchronizerHelper to be used.
   * <p/>
   * @return Automaton representing the synchronous composition.
   */
  private static Automaton synchronizeAutomata(
   final AutomataSynchronizerHelper helper)
  {
    final AutomataSynchronizer synchronizer = new AutomataSynchronizer(helper);
    synchronizer.execute();
    final Automaton result = synchronizer.getAutomaton();
    synchronizer.clear();

    return result;
  }

  @SuppressWarnings("unchecked")
  private Automata removeGuardsActionsFromEFAs(
   final List<SimpleEFAComponent> components) throws AnalysisException
  {
    final Automata automata = new Automata();
    final HashSet<SimpleEFAComponent> efas = new HashSet<>();

    for (final SimpleEFAComponent component : components) {
      if (!component.getTransitionRelation().isEmpty()) {
        efas.add(component);
      }
    }
    arc2edgeTable = new HashMap[efas.size()];

    for (final SimpleEFAComponent efa : efas) {
      autName2indexTable.put(efa.getName(), automatonIndex);
      arc2edgeTable[automatonIndex] = new HashMap<>();
      automata.addAutomaton(removeGuardsActionsFromEFA(efa));
      automatonIndex++;
    }

    return automata;
  }

  private Automaton removeGuardsActionsFromEFA(
   final SimpleEFAComponent component)
  {
    final Automaton automaton = new Automaton(component.getName());

    if (component.getKind() == ComponentKind.PLANT) {
      automaton.setType(AutomatonType.PLANT);
    }
    if (component.getKind() == ComponentKind.SPEC) {
      automaton.setType(AutomatonType.SPECIFICATION);
    }
    if (component.getKind() == ComponentKind.SUPERVISOR) {
      automaton.setType(AutomatonType.SUPERVISOR);
    }
    if (component.getKind() == ComponentKind.PROPERTY) {
      automaton.setType(AutomatonType.PROPERTY);
    }

    State fromState, toState;
    LabeledEvent event;
    boolean initialFlag = true;
    if (!component.getEdges().isEmpty()) {
      for (final EdgeProxy item : component.getEdges()) {
        final EdgeSubject edge = (EdgeSubject) item;
        fromState = automaton.getStateWithName(edge.getSource().getName());
        if (fromState == null) {
          fromState = new State(edge.getSource().getName());
          if (initialFlag && edge.getSource().toString().contains("initial")) {
            fromState.setInitial(true);
            initialFlag = false;
          }
          if (edge.getSource().toString().contains("accepting")) {
            fromState.setAccepting(true);
          }
          if (edge.getSource().toString().contains("forbidden")) {
            fromState.setForbidden(true);
          }
          automaton.addState(fromState);
          if (fromState.isInitial()) {
            automaton.setInitialState(fromState);
          }
        }
        toState = automaton.getStateWithName(edge.getTarget().getName());
        if (toState == null) {
          toState = new State(edge.getTarget().getName());
          if (initialFlag && edge.getTarget().toString().contains("initial")) {
            toState.setInitial(true);
            initialFlag = false;
          }
          if (edge.getTarget().toString().contains("accepting")) {
            toState.setAccepting(true);
          }
          if (edge.getTarget().toString().contains("forbidden")) {
            toState.setForbidden(true);
          }
          automaton.addState(toState);
          if (toState.isInitial()) {
            automaton.setInitialState(toState);
          }
        }
        final ListSubject<AbstractSubject> eventList =
         edge.getLabelBlock().getEventIdentifierListModifiable();
        for (final AbstractSubject e : eventList) {
          final SimpleIdentifierSubject eventSubject =
           (SimpleIdentifierSubject) e;
          event = automaton.getAlphabet().getEvent(eventSubject.getName());
          if (event == null) {
            event = new LabeledEvent(eventSubject.getName());
            automaton.getAlphabet().add(event);
          }
          if (edge.getGuardActionBlock() == null) {
            final GuardActionBlockSubject gab = new GuardActionBlockSubject();
            edge.setGuardActionBlock(gab);
          }
          final Arc currArc = new Arc(fromState, toState, event);
          arc2edgeTable[automatonIndex].put(currArc, edge);
          automaton.addArc(currArc);
        }
      }
    } else {
      for (final SimpleEFAState location : component.getStateSet()) {
        if (location.isInitial()) {
          final State state = new State(location.getName());
          if (location.isMarked()) {
            state.setAccepting(true);
          }
          if (location.isForbidden()) {
            state.setForbidden(true);
          }
          automaton.addState(state);
          automaton.setInitialState(state);
          break;
        }
      }
    }

    return automaton;
  }

  private void readStateNames(final SimpleEFAStateEncoding oStateEncoding)
  {
    for (final SimpleEFAState state : oStateEncoding.getSimpleStates()) {
      final String str = state.getName();
      final List<SimpleExpressionProxy> exps =
       mHelper.parseString(str,
                           EFAPartialEvaluator.DEFAULT_VALUE_OPENING,
                           EFAPartialEvaluator.DEFAULT_VALUE_CLOSING);
      for (final SimpleExpressionProxy exp : exps) {
        state.mergeToAttribute(EFAPartialEvaluator.DEFAULT_STATEVALUE_STRING,
                               exp.toString(),
                               EFAPartialEvaluator.DEFAULT_VALUE_SEPARATOR);
      }
    }
  }

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.createLogger(
   AutomataSynchronizer.class);
  private Automata mAutomata;
  private AutomataSynchronizerHelper synchHelper;
  private SynchronizationOptions syncOptions;
  private ArrayList<AutomataSynchronizerExecuter> synchronizationExecuters;
  private AutomataIndexMap indexMap;
  // For stopping execution
  private boolean abortRequested = false;
  private HashMap<Arc, EdgeSubject> arc2edgeTable[];
  private final HashMap<String, Integer> autName2indexTable =
   new HashMap<>();
  private int automatonIndex = 0;
  private final ModuleProxyFactory mFactory;
  private final List<SimpleEFAComponent> mComponents;
  private final THashSet<SimpleEFAVariable> mVariables;
  private final EFAHelper mHelper;
  private boolean mStateNameAsValue = false;
}
