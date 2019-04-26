//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * @author Robi Malik
 */

public class EventDecisionTreeBuilder
  implements EventProbabilityProvider
{

  //#########################################################################
  //# Command Line Tool
  public static void main(final String[] args)
  {
    try {
      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ExpressionParser parser =
        new ExpressionParser(moduleFactory, optable);
      List<ParameterBindingProxy> bindings = null;

      boolean noargs = false;
      final List<String> fileNames = new LinkedList<String>();
      for (final String arg : args) {
        if (noargs) {
          fileNames.add(arg);
        } else if (arg.startsWith("-D")) {
          final int eqpos = arg.indexOf('=', 2);
          if (eqpos > 2) {
            final String name = arg.substring(2, eqpos);
            final String text = arg.substring(eqpos + 1);
            final SimpleExpressionProxy expr = parser.parse(text);
            final ParameterBindingProxy binding =
              moduleFactory.createParameterBindingProxy(name, expr);
            if (bindings == null) {
              bindings = new LinkedList<ParameterBindingProxy>();
            }
            bindings.add(binding);
          } else {
            fileNames.add(arg);
          }
        } else if (arg.equals("--")) {
          noargs = true;
          fileNames.add(arg);
        } else {
          fileNames.add(arg);
        }
      }

      final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
      final JAXBModuleMarshaller moduleMarshaller =
        new JAXBModuleMarshaller(moduleFactory, optable, false);
      final JAXBProductDESMarshaller desMarshaller =
        new JAXBProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final Collection<String> empty = Collections.emptyList();
      final KindTranslator translator =
        ConflictKindTranslator.getInstanceControllable();
      final PrintWriter writer = new PrintWriter(System.out);

      for (final String name : fileNames) {
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        final ProductDESProxy des;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
        } else {
          final ModuleProxy module = (ModuleProxy) doc;
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          compiler.setEnabledPropositionNames(empty);
          des = compiler.compile(bindings);
        }
        final long start = System.currentTimeMillis();
        final EventDecisionTreeBuilder builder =
          new EventDecisionTreeBuilder(des, translator);
        final EventDecisionTree tree = builder.run();
        final long stop = System.currentTimeMillis();
        final double time = 0.001 * (stop - start);
        tree.dump(writer);
        System.out.println();
        System.out.format("Time to build: %.3fs\n", time);
        final double avgSteps = tree.getAverageNumberOfSteps(builder);
        System.out.format("Average number of steps: %.3f\n", avgSteps);
      }

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(" caught in main()!");
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Constructor
  public EventDecisionTreeBuilder(final ProductDESProxy des,
                                  final KindTranslator translator)
    throws OverflowException
  {
    mDES = des;
    final Collection<EventProxy> events = des.getEvents();
    final Collection<EventProxy> empty = Collections.emptySet();
    mEventEncoding = new EventEncoding
      (events, translator, empty, EventEncoding.FILTER_PROPOSITIONS);
    final Collection<AutomatonProxy> allAutomata = des.getAutomata();
    int numAutomata = 0;
    for (final AutomatonProxy aut : allAutomata) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        numAutomata++;
        break;
      default:
        break;
      }
    }
    mAutomataMap = new TObjectIntHashMap<>(numAutomata);
    mAutomataInfo = new AutomatonInfo[numAutomata];
    final List<AutomatonProxy> usedAutomata = new ArrayList<>(numAutomata);
    int a = 0;
    for (final AutomatonProxy aut : allAutomata) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        final AutomatonInfo info = new AutomatonInfo(aut, a);
        mAutomataMap.put(aut, a);
        mAutomataInfo[a] = info;
        usedAutomata.add(aut);
        a++;
        break;
      default:
        break;
      }
    }
    mDecisionTree = new EventDecisionTree(usedAutomata, mEventEncoding);
    for (final AutomatonInfo info : mAutomataInfo) {
      info.removeBlockedEvents();
    }
    final int numEvents = mEventEncoding.getNumberOfProperEvents();
    int maxFanOut = 0;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = mEventEncoding.getProperEventStatus(e);
      if ((status & EventStatus.STATUS_BLOCKED) == 0) {
        maxFanOut++;
      }
    }
    mMaxFanOut = maxFanOut;
  }


  //#########################################################################
  //# Invocation
  public EventDecisionTree run()
  {
    mTaskMap = new HashMap<>();
    mTaskStack = new ArrayList<>();
    mNextTaskID = 0;
    mLineNumberMap = new TIntIntHashMap();
    final Task endTask = new Task();
    mTaskMap.put(endTask, endTask);
    Task task = new Task(endTask);
    addTask(task);
    while (!mTaskStack.isEmpty()) {
      final int last = mTaskStack.size() - 1;
      task = mTaskStack.remove(last);
      task.generate();
    }
    final int endTaskID = endTask.getTaskID();
    final int endLineNo = mDecisionTree.getNextLineNumber();
    mLineNumberMap.put(endTaskID, endLineNo);
    mDecisionTree.renumber(mLineNumberMap);
    return mDecisionTree;
  }


  //#########################################################################
  //# Auxiliary Methods
  private Task addTask(final Task task)
  {
    final Task existing = mTaskMap.get(task);
    if (existing == null) {
      mTaskMap.put(task, task);
      mTaskStack.add(task);
      return task;
    } else {
      return existing;
    }
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.monolithic.EventProbabilityProvider
  @Override
  public double getProbability(final int aut, final int event)
  {
    return mAutomataInfo[aut].getProbability(event);
  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  private class AutomatonInfo
  {
    //#######################################################################
    //# Constructor
    private AutomatonInfo(final AutomatonProxy aut, final int a)
    {
      mAutomatonCode = a;
      mStateEncoding = new StateEncoding(aut);
      final Collection<EventProxy> events = aut.getEvents();
      mTransitionInfoMap = new TIntObjectHashMap<>(events.size());
      for (final EventProxy event : events) {
        final int e = mEventEncoding.getEventCode(event);
        if (e >= 0 && (mEventEncoding.getProperEventStatus(e) &
                       EventStatus.STATUS_BLOCKED) == 0) {
          final TransitionInfo info = new TransitionInfo(a, e);
          mTransitionInfoMap.put(e, info);
        }
      }
      for (final TransitionProxy trans : aut.getTransitions()) {
        final EventProxy event = trans.getEvent();
        final int e = mEventEncoding.getEventCode(event);
        final TransitionInfo info = mTransitionInfoMap.get(e);
        if (info != null) {
          info.addTransition(trans, mStateEncoding);
        }
      }
      final TIntObjectIterator<TransitionInfo> iter =
        mTransitionInfoMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final TransitionInfo info = iter.value();
        if (info.isAlwaysDisabled()) {
          iter.remove();
          final int e = info.getEventCode();
          final byte status = mEventEncoding.getProperEventStatus(e);
          mEventEncoding.setProperEventStatus
            (e, status | EventStatus.STATUS_BLOCKED);
        }
      }
    }

    //#######################################################################
    //# Initialisation
    public void removeBlockedEvents()
    {
     final TIntObjectIterator<TransitionInfo> iter =
       mTransitionInfoMap.iterator();
     while (iter.hasNext()) {
       iter.advance();
       final TransitionInfo info = iter.value();
       final int e = info.getEventCode();
       final byte status = mEventEncoding.getProperEventStatus(e);
       if ((status & EventStatus.STATUS_BLOCKED) != 0) {
         iter.remove();
       }
     }
    }

    //#######################################################################
    //# Simple Access
    private int getAutomatonCode()
    {
      return mAutomatonCode;
    }

    private int getNumberOfStates()
    {
      return mStateEncoding.getNumberOfStates();
    }

    private Collection<TransitionInfo> getTransitions()
    {
      return mTransitionInfoMap.valueCollection();
    }

    private boolean usesEvent(final TIntArrayList events)
    {
      for (int i = 0; i < events.size(); i++) {
        final int e = events.get(i);
        if (mTransitionInfoMap.get(e) != null) {
          return true;
        }
      }
      return false;
    }

    private boolean isEnabled(final int state, final int event)
    {
      final TransitionInfo info = mTransitionInfoMap.get(event);
      return info.isEnabled(state);
    }

    public double getProbability(final int event)
    {
      final TransitionInfo info = mTransitionInfoMap.get(event);
      return info.getProbability();
    }

    private int getTopEventCount()
    {
      return mTopEventCount;
    }

    //#######################################################################
    //# Algorithm
    private void resetTopProbability()
    {
      mTopProbability = 0.0;
      mTopEventCount = 0;
    }

    private void registerTopProbability(final TransitionInfo info)
    {
      assert mAutomatonCode == info.getAutomatonCode();
      if (info.getProbability() > mTopProbability) {
        mTopProbability = info.getProbability();
        mTopEventCount = 1;
      } else if (info.getProbability() == mTopProbability) {
        mTopEventCount++;
      }
    }

    private void generateCaseStatement(final TIntArrayList events,
                                       final TIntArrayList automata,
                                       final Task nextTask,
                                       final int prevFanout)
    {
      mDecisionTree.appendCase(mAutomatonCode);
      final int numAutomata = automata.size();
      final TIntArrayList reducedAutomata = new TIntArrayList(numAutomata - 1);
      for (int i = 0; i < numAutomata; i++) {
        final int a = automata.get(i);
        if (a != mAutomatonCode) {
          reducedAutomata.add(a);
        }
      }
      final int numEvents = events.size();
      final int numStates = getNumberOfStates();
      final int fanout = prevFanout * numStates;
      for (int s = 0; s < numStates; s++) {
        final TIntArrayList reducedEvents = new TIntArrayList(numEvents);
        for (int i = 0; i < numEvents; i++) {
          final int e = events.get(i);
          if (isEnabled(s, e)) {
            reducedEvents.add(e);
          }
        }
        if (reducedEvents.isEmpty()) {
          final int nextTaskID = nextTask.getTaskID();
          mDecisionTree.appendRaw(nextTaskID);
        } else {
          Task task = new Task(reducedAutomata, reducedEvents, nextTask, fanout);
          task = addTask(task);
          final int taskID = task.getTaskID();
          mDecisionTree.appendRaw(taskID);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonCode;
    private final StateEncoding mStateEncoding;
    private final TIntObjectHashMap<TransitionInfo> mTransitionInfoMap;

    private double mTopProbability;
    private int mTopEventCount;
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final int event)
    {
      mEventCode = event;
      mTransitionInfoList = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private int getEventCode()
    {
      return mEventCode;
    }

    private boolean isAlwaysEnabled()
    {
      return mTransitionInfoList.isEmpty();
    }

    //#######################################################################
    //# Algorithm
    private void addTransitionInfo(final TransitionInfo info)
    {
      if (!info.isAlwaysEnabled()) {
        mTransitionInfoList.add(info);
      }
    }

    private boolean registerTopProbabilities(final int fanout)
    {
      assert !mTransitionInfoList.isEmpty();
      Collections.sort(mTransitionInfoList);
      double topProbability = 0.0;
      for (final TransitionInfo transInfo : mTransitionInfoList) {
        final int a = transInfo.getAutomatonCode();
        final AutomatonInfo autInfo = mAutomataInfo[a];
        if (fanout * autInfo.getNumberOfStates() <= mMaxFanOut) {
          topProbability = transInfo.getProbability();
          break;
        }
      }
      if (topProbability == 0.0) {
        return false;
      }
      for (final TransitionInfo transInfo : mTransitionInfoList) {
        if (transInfo.getProbability() == topProbability) {
          final int a = transInfo.getAutomatonCode();
          final AutomatonInfo autInfo = mAutomataInfo[a];
          if (fanout * autInfo.getNumberOfStates() <= mMaxFanOut) {
            autInfo.registerTopProbability(transInfo);
          }
        } else if (transInfo.getProbability() < topProbability) {
          break;
        }
      }
      return true;
    }

    private boolean isTopAutomaton(final int a)
    {
      final TransitionInfo first = mTransitionInfoList.get(0);
      final double probability = first.getProbability();
      for (final TransitionInfo info : mTransitionInfoList) {
        if (info.getAutomatonCode() == a) {
          return true;
        } else if (info.getProbability() < probability) {
          break;
        }
      }
      return false;
    }

    private void generate(final int nextTaskID)
    {
      for (final TransitionInfo info : mTransitionInfoList) {
        info.generate(nextTaskID);
      }
      mDecisionTree.appendExecute(mEventCode);
    }

    //#######################################################################
    //# Data Members
    private final int mEventCode;
    private final List<TransitionInfo> mTransitionInfoList;
  }


  //#########################################################################
  //# Inner Class TransitionInfo
  private class TransitionInfo implements Comparable<TransitionInfo>
  {
    //#######################################################################
    //# Constructor
    private TransitionInfo(final int aut, final int event)
    {
      mAutomatonCode = aut;
      mEventCode = event;
      mEnabled = new TIntHashSet();
    }

    //#######################################################################
    //# Initialisation
    private void addTransition(final TransitionProxy trans,
                               final StateEncoding encoding)
    {
      final StateProxy source = trans.getSource();
      final int s = encoding.getStateCode(source);
      mEnabled.add(s);
    }

    //#######################################################################
    //# Simple Access
    private int getAutomatonCode()
    {
      return mAutomatonCode;
    }

    private int getEventCode()
    {
      return mEventCode;
    }

    private boolean isAlwaysEnabled()
    {
      final AutomatonInfo info = mAutomataInfo[mAutomatonCode];
      return mEnabled.size() == info.getNumberOfStates();
    }

    private boolean isAlwaysDisabled()
    {
      return mEnabled.isEmpty();
    }

    private double getProbability()
    {
      final AutomatonInfo info = mAutomataInfo[mAutomatonCode];
      return (double) mEnabled.size() / info.getNumberOfStates();
    }

    public boolean isEnabled(final int state)
    {
      return mEnabled.contains(state);
    }

    //#######################################################################
    //# Interface java.util.Comparable<TransitionInfo>
    @Override
    public int compareTo(final TransitionInfo info)
    {
      final double prob1 = getProbability();
      final double prob2 = info.getProbability();
      if (prob1 < prob2) {
        return -1;
      } else if (prob1 > prob2) {
        return 1;
      }
      return mAutomatonCode - info.mAutomatonCode;
    }

    //#######################################################################
    //# Algorithm
    private void generate(final int nextTaskID)
    {
      mDecisionTree.appendIfDisabled(mAutomatonCode, mEventCode, nextTaskID);
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonCode;
    private final int mEventCode;
    private final TIntHashSet mEnabled;
  }


  //#########################################################################
  //# Inner Class Task
  private class Task
  {
    //#######################################################################
    //# Data Members
    private Task()
    {
      mAutomata = new TIntArrayList();
      mEvents = new TIntArrayList();
      mNextTask = null;
      mTaskID = mNextTaskID++;
      mPreviousFanOut = 1;
    }

    private Task(final Task next)
    {
      mAutomata = new TIntArrayList(mAutomataInfo.length);
      for (final AutomatonInfo info : mAutomataInfo) {
        mAutomata.add(info.getAutomatonCode());
      }
      final int numEvents = mEventEncoding.getNumberOfProperEvents();
      mEvents = new TIntArrayList(numEvents);
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = mEventEncoding.getProperEventStatus(e);
        if ((status & EventStatus.STATUS_BLOCKED) == 0) {
          mEvents.add(e);
        }
      }
      mNextTask = next;
      mTaskID = mNextTaskID++;
      mPreviousFanOut = 1;
    }

    private Task(final TIntArrayList automata,
                 final TIntArrayList events,
                 final Task next,
                 final int fanout)
    {
      final int numAutomata = automata.size();
      mAutomata = new TIntArrayList(numAutomata);
      for (int i = 0; i < numAutomata; i++) {
        final int a = automata.get(i);
        final AutomatonInfo info = mAutomataInfo[a];
        if (info.usesEvent(events)) {
          mAutomata.add(a);
        }
      }
      mEvents = events;
      mNextTask = next;
      mTaskID = mNextTaskID++;
      mPreviousFanOut = fanout;
    }

    //#######################################################################
    //# Simple Access
    private int getTaskID()
    {
      return mTaskID;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final Task task = (Task) other;
        return
          mAutomata.equals(task.mAutomata) &&
          mEvents.equals(task.mEvents) &&
          mNextTask == task.mNextTask;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      int result = mAutomata.hashCode() + 5 * mEvents.hashCode();
      if (mNextTask != null) {
        result += 25 * mNextTask.mTaskID;
      }
      return result;
    }

    //#######################################################################
    //# Algorithm
    private void generate()
    {
      final int lineNumber = mDecisionTree.getNextLineNumber();
      mLineNumberMap.put(mTaskID, lineNumber);
      final TIntObjectHashMap<EventInfo> eventMap =
        new TIntObjectHashMap<>(mEvents.size());
      for (int i = 0; i < mEvents.size(); i++) {
        final int e = mEvents.get(i);
        final EventInfo info = new EventInfo(e);
        eventMap.put(e, info);
      }
      for (int i = 0; i < mAutomata.size(); i++) {
        final int a = mAutomata.get(i);
        final AutomatonInfo autInfo = mAutomataInfo[a];
        autInfo.resetTopProbability();
        for (final TransitionInfo transInfo : autInfo.getTransitions()) {
          final int e = transInfo.getEventCode();
          final EventInfo eventInfo = eventMap.get(e);
          if (eventInfo != null) {
            eventInfo.addTransitionInfo(transInfo);
          }
        }
      }
      for (int i = 0; i < mEvents.size(); i++) {
        final int e = mEvents.get(i);
        final EventInfo info = eventMap.get(e);
        if (info.isAlwaysEnabled()) {
          // Execute an always enabled event, then try reduced task.
          mDecisionTree.appendExecute(e);
          Task task = makeTaskWithoutEvent(e);
          task = addTask(task);
          gotoTaskUnlessNext(task);
          return;
        } else {
          info.registerTopProbabilities(mPreviousFanOut);
        }
      }
      int topValue = 0;
      AutomatonInfo topAut = null;
      for (int i = 0; i < mAutomata.size(); i++) {
        final int a = mAutomata.get(i);
        final AutomatonInfo info = mAutomataInfo[a];
        if (topValue < info.getTopEventCount()) {
          topValue = info.getTopEventCount();
          topAut = info;
        }
      }
      final TIntArrayList topEvents = new TIntArrayList(topValue);
      if (topAut != null) {
        final int a = topAut.getAutomatonCode();
        for (final EventInfo info : eventMap.valueCollection()) {
          if (info.isTopAutomaton(a)) {
            final int e = info.getEventCode();
            topEvents.add(e);
          }
        }
      }
      if (topEvents.size() > 1) {
        // Split on topAut using topEvents, then continue with remaining events
        Task task2 = makeTaskWithoutEvents(topEvents);
        task2 = addTask(task2);
        topAut.generateCaseStatement(topEvents, mAutomata, task2, mPreviousFanOut);
      } else if (!mEvents.isEmpty()) {
        // Test an event individually, then goto reduced task.
        final int e = mEvents.get(0);
        final EventInfo info = eventMap.get(e);
        Task task = makeTaskWithoutEvent(e);
        task = addTask(task);
        final int nextTaskID = task.getTaskID();
        info.generate(nextTaskID);
        gotoTaskUnlessNext(task);
      } else {
        gotoTaskUnlessNext(mNextTask);
      }
    }

    private Task makeTaskWithoutEvent(final int removed)
    {
      final int numEvents = mEvents.size();
      final TIntArrayList reducedEvents = new TIntArrayList(numEvents - 1);
      for (int i = 0; i < numEvents; i++) {
        final int e = mEvents.get(i);
        if (e != removed) {
          reducedEvents.add(e);
        }
      }
      if (reducedEvents.isEmpty()) {
        return mNextTask;
      } else {
        return new Task(mAutomata, reducedEvents, mNextTask, mPreviousFanOut);
      }
    }

    private Task makeTaskWithoutEvents(final TIntArrayList removed)
    {
      final TIntHashSet removedSet = new TIntHashSet(removed);
      final int numEvents = mEvents.size();
      final TIntArrayList reducedEvents =
        new TIntArrayList(numEvents - removed.size());
      for (int i = 0; i < numEvents; i++) {
        final int e = mEvents.get(i);
        if (!removedSet.contains(e)) {
          reducedEvents.add(e);
        }
      }
      if (reducedEvents.isEmpty()) {
        return mNextTask;
      } else {
        return new Task(mAutomata, reducedEvents, mNextTask, mPreviousFanOut);
      }
    }

    private void gotoTaskUnlessNext(final Task task)
    {
      final int index = mTaskStack.size() - 1;
      if (index >= 0) {
        final Task next = mTaskStack.get(index);
        if (task != next) {
          final int id = task.getTaskID();
          mDecisionTree.appendGoto(id);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final TIntArrayList mAutomata;
    private final TIntArrayList mEvents;
    private final Task mNextTask;
    private final int mTaskID;
    private final int mPreviousFanOut;
  }


  //#########################################################################
  //# Data Members
  @SuppressWarnings("unused")
  private final ProductDESProxy mDES;
  private final EventEncoding mEventEncoding;
  private final TObjectIntHashMap<AutomatonProxy> mAutomataMap;
  private final EventDecisionTree mDecisionTree;
  private final AutomatonInfo[] mAutomataInfo;
  private final int mMaxFanOut;

  private Map<Task,Task> mTaskMap;
  private List<Task> mTaskStack;
  private int mNextTaskID;
  private TIntIntHashMap mLineNumberMap;

}
