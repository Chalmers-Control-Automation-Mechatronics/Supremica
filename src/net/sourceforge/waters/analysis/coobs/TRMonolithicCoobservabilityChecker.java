//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.coobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer;
import net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelVerifier;
import net.sourceforge.waters.analysis.monolithic.StateTupleEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CoobservabilityCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>An implementation of the coobservability check algorithm.</P>
 *
 * <P>This class implements a verifier-based algorithm based on the paper
 * referenced below, whose complexity is polynomial in the number of states
 * and exponential in the number of supervisor sites. Optimisations are
 * implemented to compress state tuples and reduce their number of state
 * components, avoiding duplications for deterministic automata.</P>
 *
 * <P><I>References:</I><BR>
 * Karen Rudie, Jan C. Willems. The Computational Complexity of Decentralized
 * Discrete-Event Control Problems. IEEE Transactions on Automatic Control,
 * <STRONG>27</STRONG>(11), 1692&ndash;1708, 1992.</P>
 *
 * @author Robi Malik
 */
public class TRMonolithicCoobservabilityChecker
  extends AbstractTRMonolithicModelVerifier
  implements CoobservabilityChecker
{

  //#########################################################################
  //# Constructors
  public TRMonolithicCoobservabilityChecker()
  {
    this(ControllabilityKindTranslator.getInstance());
  }

  public TRMonolithicCoobservabilityChecker(final KindTranslator translator)
  {
    super(translator);
  }

  public TRMonolithicCoobservabilityChecker(final ProductDESProxy model,
                                            final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.coobs.CoobservabilityChecker
  @Override
  public void setDefaultSiteName(final String name)
  {
    mDefaultSiteName = "".equals(name) ? null : name;
  }

  @Override
  public String getDefaultSiteName()
  {
    return mDefaultSiteName == null ? "" : mDefaultSiteName;
  }

  @Override
  public CoobservabilityCounterExampleProxy getCounterExample()
  {
    return (CoobservabilityCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.Configurable
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_CoobservabilityChecker_DefaultSite);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_CoobservabilityChecker_DefaultSite)) {
      final String value = (String) option.getValue();
      setDefaultSiteName(value);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer
  @Override
  public boolean isSensitiveToControllability()
  {
    return true;
  }

  @Override
  protected Map<EventProxy,CoobservabilityEventInfo> setUpEventEncoding()
    throws OverflowException, AnalysisAbortException
  {
    assert getEventEncoding() == null;

    final ProductDESProxy des = getModel();
    final Collection<EventProxy> events = des.getEvents();
    final int numEvents = events.size();
    final TRAutomatonProxy[] trs = getTRAutomata();
    mNumAutomata = trs.length;
    mReferenceSite = new SupervisorSite
      (CoobservabilityDiagnostics.REFERENCE_SITE_NAME, true, -1, mNumAutomata);
    mSiteMap = new LinkedHashMap<>();
    mCoobservabilityEventInfoMap = new HashMap<>(numEvents);

    for (int a = 0; a < mNumAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy tr = trs[a];
      final EventEncoding enc = tr.getEventEncoding();
      for (int e = EventEncoding.TAU; e < enc.getNumberOfProperEvents(); e++) {
        if ((enc.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
          // For any used event: create event info if not yet present
          final EventProxy event = enc.getProperEvent(e);
          if (!mCoobservabilityEventInfoMap.containsKey(event)) {
            final CoobservabilityEventInfo info =
              new CoobservabilityEventInfo(this, event);
            mCoobservabilityEventInfoMap.put(event, info);
            final Map<String,String> attribs = event.getAttributes();
            boolean controlled = false;
            boolean observed = false;
            for (final Map.Entry<String,String> entry : attribs.entrySet()) {
              final String attrib = entry.getKey();
              if (attrib.startsWith
                    (CoobservabilityAttributeFactory.CONTROLLABITY_KEY)) {
                final String value = entry.getValue();
                final SupervisorSite site = getSite(value);
                info.addController(site);
                controlled = true;
              } else if (attrib.startsWith
                           (CoobservabilityAttributeFactory.OBSERVABITY_KEY)) {
                final String value = entry.getValue();
                final SupervisorSite site = getSite(value);
                info.addObserver(site);
                observed = true;
              }
            }
            if (mDefaultSiteName != null) {
              if (!controlled &&
                  (enc.getProperEventStatus(e) &
                   EventStatus.STATUS_CONTROLLABLE) != 0) {
                final SupervisorSite site = getSite(mDefaultSiteName);
                info.addController(site);
              }
              if (!observed && event.isObservable()) {
                final SupervisorSite site = getSite(mDefaultSiteName);
                info.addObserver(site);
              }
            }
          }
        }
      }
    }

    mComponentInfoList = new ArrayList<>(mNumAutomata);
    for (int a = 0; a < mNumAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy tr = trs[a];
      final ComponentInfo info0 = new ComponentInfo(tr, a, mReferenceSite);
      final int c0 = mComponentInfoList.size();
      mComponentInfoList.add(info0);
      mReferenceSite.setComponentIndex(a, c0);
      if (!mSiteMap.isEmpty()) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final boolean det = rel.isDeterministic();
        for (final SupervisorSite site : mSiteMap.values()) {
          if (det && !hasUnobservableTransition(tr, site)) {
            info0.addShadowingSite(site);
            site.setComponentIndex(a, c0);
          } else {
            final ComponentInfo info1 = new ComponentInfo(tr, a, site);
            final int c = mComponentInfoList.size();
            mComponentInfoList.add(info1);
            site.setComponentIndex(a, c);
          }
        }
      }
    }

    return mCoobservabilityEventInfoMap;
  }

  @Override
  protected List<EventInfo> setUpTransitions
    (final TRAutomatonProxy[] automata,
     final Map<EventProxy,? extends EventInfo> eventInfoMap,
     final boolean reverse)
    throws AnalysisException
  {
    mReverse = reverse;
    return super.setUpTransitions(automata, eventInfoMap, reverse);
  }

  @Override
  protected void setUpStateTupleEncoding()
    throws AnalysisAbortException, OverflowException
  {
    final int numComponents = mComponentInfoList.size();
    mTRAutomataExtended = new TRAutomatonProxy[numComponents];
    final TRAutomatonProxy[] trs = getTRAutomata();
    int c = 0;
    for (final ComponentInfo info : mComponentInfoList) {
      final int a = info.getAutomatonIndex();
      mTRAutomataExtended[c++] = trs[a];
    }
    setUpStateTupleEncoding(mTRAutomataExtended);
  }

  @Override
  protected int storeInitialStates()
    throws AnalysisException
  {
    return storeInitialStates(mTRAutomataExtended);
  }

  @Override
  protected boolean handleUncontrollableState(final int e, final int spec)
    throws AnalysisException
  {
    if (isCounterExampleEnabled()) {
      final int s = getCurrentSource();
      final CoobservabilityCounterExampleProxy counterExample =
        buildCounterExample(s, e, spec);
      return setFailedResult(counterExample);
    } else {
      return setFailedResult(null);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSiteMap = null;
    mCoobservabilityEventInfoMap = null;
    mComponentInfoList = null;
    mTRAutomataExtended = null;
  }


  //#########################################################################
  //# Counterexample
  private CoobservabilityCounterExampleProxy buildCounterExample(final int s,
                                                                 final int e,
                                                                 final int c)
    throws AnalysisException
  {
    // Set up traces: reference plus one trace for each controlling site
    final IntArrayBuffer stateSpace = getStateSpace();
    final StateTupleEncoding encoding = getStateTupleEncoding();
    final int numAut = mTRAutomataExtended.length;
    final int[] encoded = new int[encoding.getNumberOfWords()];
    int[] decodedSource = new int[numAut];
    int[] decodedTarget = new int[numAut];
    stateSpace.getContents(s, encoded);
    encoding.decode(encoded, decodedTarget);

    // Get details about end state for diagnostics
    final ComponentInfo specInfo = mComponentInfoList.get(c);
    final int a = specInfo.getAutomatonIndex();
    final AutomatonProxy[] inputAutomata = getInputAutomata();
    final AutomatonProxy specAut = inputAutomata[a];
    final TRAutomatonProxy[] trs = getTRAutomata();
    final TRAutomatonProxy specTR = trs[a];
    final StateProxy specState = specTR.getState(decodedTarget[c]);

    // Final (failing) trace step
    CoobservabilityEventInfo eventInfo =
      (CoobservabilityEventInfo) getEventInfo(e);
    final EventProxy event = eventInfo.getEvent();
    final List<SupervisorSite> controllers = getControllers(eventInfo);
    final int numTraces = controllers.size() + 1;
    Iterator<SupervisorSite> iter = controllers.iterator();
    final List<List<TraceStepProxy>> steps = new ArrayList<>(numTraces);
    for (int g = 0; g < numTraces; g++) {
      final List<TraceStepProxy> list = new LinkedList<>();
      final SupervisorSite site = g == 0 ? mReferenceSite : iter.next();
      final TraceStepProxy step =
        eventInfo.buildFinalTraceStep(decodedTarget, site);
      list.add(step);
      steps.add(list);
   }

    // Intermediate trace steps
    final CounterExampleCallback callback = prepareForCounterExample();
    final int numInit = getNumberOfInitialStates();
    int target = s;
    // Until we reach the start state ...
    while (target >= numInit) {
      expandState(encoded, decodedTarget, callback);
      eventInfo = (CoobservabilityEventInfo) callback.getSmallestStateEvent();
      final int source = callback.getSmallestStateIndex();
      assert source < target;
      stateSpace.getContents(source, encoded);
      encoding.decode(encoded, decodedSource);
      final SupervisorSite stepSite = findSteppingSite(decodedSource, decodedTarget);
      if (stepSite == mReferenceSite || eventInfo.isObservable(stepSite)) {
        final List<TraceStepProxy> list0 = steps.get(0);
        final TraceStepProxy step0 = eventInfo.buildIntermediateTraceStep
          (decodedSource, decodedTarget, mReferenceSite);
        list0.add(0, step0);
        int g = 1;
        for (final SupervisorSite site : controllers) {
          if (eventInfo.isObservable(site)) {
            final List<TraceStepProxy> list = steps.get(g);
            final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
              (decodedSource, decodedTarget, site);
            list.add(0, step);
          }
          g++;
        }
      } else {
        final int i = controllers.indexOf(stepSite);
        if (i >= 0) {
          final List<TraceStepProxy> list = steps.get(i + 1);
          final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
            (decodedSource, decodedTarget, stepSite);
          list.add(0, step);
        }
      }
      target = source;
      final int[] tmp = decodedTarget;
      decodedTarget = decodedSource;
      decodedSource = tmp;
    }

    // Initial state trace step
    iter = controllers.iterator();
    for (int g = 0; g < numTraces; g++) {
      final List<TraceStepProxy> list = steps.get(g);
      final SupervisorSite site = g == 0 ? mReferenceSite : iter.next();
      final TraceStepProxy step = buildInitialTraceStep(decodedTarget, site);
      list.add(0, step);
   }

    // Build traces and counterexample
    final ProductDESProxyFactory factory = getFactory();
    final List<TraceProxy> traces = new ArrayList<>(numTraces);
    iter = controllers.iterator();
    for (int g = 0; g < numTraces; g++) {
      final SupervisorSite site = g == 0 ? mReferenceSite : iter.next();
      final String name = site.getName();
      final List<TraceStepProxy> list = steps.get(g);
      final TraceProxy trace = factory.createTraceProxy(name, list, -1);
      traces.add(trace);
    }
    final ProductDESProxy des = getModel();
    final CoobservabilityDiagnostics diag =
      new CoobservabilityDiagnostics(controllers);
    final String name = diag.getTraceName(des);
    final String comment = diag.getTraceComment(des, event, specAut, specState);
    final Collection<AutomatonProxy> automata = Arrays.asList(inputAutomata);
    return factory.createCoobservabilityCounterExampleProxy
      (name, comment, null, des, automata, traces);
  }

  public TraceStepProxy buildInitialTraceStep(final int[] decoded,
                                              final SupervisorSite site)
  {
    final TRAutomatonProxy[] trs = getTRAutomata();
    final int numAut = trs.length;
    final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
    for (int a = 0; a < numAut; a++) {
      final TRAutomatonProxy tr = trs[a];
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      if (rel.hasNondeterministicInitialStates()) {
        final AutomatonProxy aut = getInputAutomaton(a);
        final int c = site.getComponentIndex(a);
        final int s = decoded[c];
        final StateProxy state = tr.getState(s);
        stateMap.put(aut, state);
      }
    }
    final ProductDESProxyFactory factory = getFactory();
    return factory.createTraceStepProxy(null, stateMap);
  }

  private SupervisorSite findSteppingSite(final int[] decodedSource,
                                          final int[] decodedTarget)
  {
    for (int a = 0; a < mNumAutomata; a++) {
      final int c = mReferenceSite.getComponentIndex(a);
      if (decodedSource[c] != decodedTarget[c]) {
        return mReferenceSite;
      }
    }
    for (final SupervisorSite site : mSiteMap.values()) {
      for (int a = 0; a < mNumAutomata; a++) {
        final int c = site.getComponentIndex(a);
        if (decodedSource[c] != decodedTarget[c]) {
          return site;
        }
      }
    }
    assert false : "No state change found on trace step!";
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private SupervisorSite getSite(final String name)
  {
    SupervisorSite site = mSiteMap.get(name);
    if (site == null) {
      final int index = mSiteMap.size();
      site = new SupervisorSite(name, false, index, mNumAutomata);
      mSiteMap.put(name, site);
    }
    return site;
  }

  private List<SupervisorSite> getControllers(final CoobservabilityEventInfo eventInfo)
  {
    final List<SupervisorSite> list = new LinkedList<>();
    for (final SupervisorSite site : mSiteMap.values()) {
      if (eventInfo.isControllable(site)) {
        list.add(site);
      }
    }
    return list;
  }

  private boolean hasUnobservableTransition(final TRAutomatonProxy tr,
                                            final SupervisorSite site)
  {
    final EventEncoding enc = tr.getEventEncoding();
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      if (iter.getCurrentFromState() != iter.getCurrentToState()) {
        final int e = iter.getCurrentEvent();
        final EventProxy event = enc.getProperEvent(e);
        final CoobservabilityEventInfo info =
          mCoobservabilityEventInfoMap.get(event);
        if (!info.isObservable(site)) {
          return true;
        }
      }
    }
    return false;
  }


  //#########################################################################
  //# Inner Class CoobservabilityEventInfo
  /**
   * <P>Extended event information record to support coobservability.</P
   *
   * <P>For coobservability, state tuple may contain several entries for the
   * same automaton, and events may be processed several times depending
   * on observation capabilities of supervisor sites. The extended event
   * information record holds automaton/site pairs that need to be checked
   * or updated when processing a particular event.<P>
   *
   * <P>To support state expansion for coobservability, the {@link
   * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
   * AutomatonEventInfo} records are replicated and into several lists for the
   * following groups:</P>
   *
   * <UL>
   * <LI>Group 0 contains plants and specifications that use the event,
   * with no associated supervisor site. These represent the so-called
   * <I>reference state</I>, the state the plant is in (while supervisors may
   * believe that it is in another state).</LI>
   * <LI>Group 1 contains specifications that use the event, one for each
   * site that can observe the event. These state components must be updated
   * together with those in group 0 if a transition is executed.</LI>
   * <LI>Groups 2 and following contain plants and specifications that use the
   * event, with a separate group for each site that cannot observe the event.
   * The state components in each of these groups are updated together
   * but independently from all other groups.</LI>
   * </UL>
   *
   * <P>The list {@link #mSiteDisablers} contains lists corresponding to
   * each group. When expanding a state, group&nbsp;0 is used to determine
   * whether the controllability condition needs to be checked for a state.
   * This is the case if the plants in group&nbsp;0 enable an event while
   * the specifications in group&nbsp;0 disable it. In this case, at least
   * one of the components in another group must also disable the event or the
   * model is not coobservable.</P>
   *
   * <P>If the controllability condition passes, then transitions can be
   * constructed using the records in groups 0 and&nbsp;1 together, and
   * further transitions can be constructed for each of the other groups.
   * The construction of transition is done using linked lists of {@link
   * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
   * AutomatonEventInfo} records in the array {@link #mSiteUpdates}, which
   * are also indexed by group number. Here, the entry at index&nbsp;0 is
   * unused (always <CODE>null</CODE>) with its records merged into
   * group&nbsp;1, as these two groups are processed together.</P>
   *
   * <P>For reverse transition exploration, the groups 0 and&nbsp;1 are
   * also merged into group&nbsp;1 in {@link #mSiteDisablers}, as the
   * controllability condition is not checked during reverse exploration.</P>
   */
  private class CoobservabilityEventInfo
    extends AbstractTRMonolithicModelAnalyzer.EventInfo
  {
    //#######################################################################
    //# Constructor
    private CoobservabilityEventInfo
      (final TRMonolithicCoobservabilityChecker verifier,
       final EventProxy event)
    {
      super(event, false);
    }

    //#######################################################################
    //# Simple Access
    private boolean isControllable(final SupervisorSite site)
    {
      return mControllers != null && mControllers.contains(site);
    }

    @SuppressWarnings("unused")
    private boolean isObservable()
    {
      return mObservers != null;
    }

    private boolean isObservable(final SupervisorSite site)
    {
      return mObservers != null && mObservers.contains(site);
    }

    //#######################################################################
    //# Initialisation
    private void addController(final SupervisorSite site)
    {
      if (mControllers == null) {
        mControllers = new THashSet<>();
      }
      mControllers.add(site);
    }

    private void addObserver(final SupervisorSite site)
    {
      if (mObservers == null) {
        mObservers = new THashSet<>();
      }
      mObservers.add(site);
    }

    @Override
    protected void sort()
    {
      computeSiteDisablers();
      final Comparator<AutomatonEventInfo> comparator =
        getAutomatonEventInfoComparator();
      for (final List<AutomatonEventInfo> list : mSiteDisablers) {
        Collections.sort(list, comparator);
      }
      if (mSiteUpdates == null) {
        final int numGroups = mSiteDisablers.size();
        mSiteUpdates = new AutomatonEventInfo[numGroups];
      } else {
        Arrays.fill(mSiteUpdates, null);
      }
      collectSiteUpdates(false);
      collectSiteUpdates(true);
    }

    private void computeSiteDisablers()
    {
      final List<AutomatonEventInfo> disablingList = getDisablingAutomata();
      final int numDisabling = disablingList.size();
      final TIntObjectMap<AutomatonEventInfo> disablingMap =
        new TIntObjectHashMap<>(numDisabling);
      for (final AutomatonEventInfo info : disablingList) {
        final int a = info.getAutomatonIndex();
        disablingMap.put(a, info);
      }

      final TRAutomatonProxy[] trs = getTRAutomata();
      final List<AutomatonEventInfo> refList = new ArrayList<>(numDisabling);
      final List<AutomatonEventInfo> observerList = new LinkedList<>();
      final Map<SupervisorSite,List<AutomatonEventInfo>> nonObserverMap =
        new LinkedHashMap<>();
      int c = 0;
      for (final ComponentInfo compInfo : mComponentInfoList) {
        final int a = compInfo.getAutomatonIndex();
        final AutomatonEventInfo refInfo = disablingMap.get(a);
        if (refInfo != null) {
          final TRAutomatonProxy tr = trs[a];
          final SupervisorSite site = compInfo.getSite();
          if (site.isReferenceSite()) {
            final boolean plant = refInfo.isPlant();
            final AutomatonEventInfo altInfo =
              new AutomatonEventInfo(refInfo, c, tr, plant, true);
            refList.add(altInfo);
          } else {
            final AutomatonEventInfo siteInfo =
              new AutomatonEventInfo(refInfo, c, tr, true, true);
            if (isObservable(site)) {
              observerList.add(siteInfo);
            } else {
              List<AutomatonEventInfo> list = nonObserverMap.get(site);
              if (list == null) {
                list = new LinkedList<>();
                nonObserverMap.put(site, list);
              }
              list.add(siteInfo);
            }
          }
        }
        c++;
      }
      c = 0;
      for (final ComponentInfo compInfo : mComponentInfoList) {
        final int a = compInfo.getAutomatonIndex();
        final AutomatonEventInfo refInfo = disablingMap.get(a);
        if (refInfo != null && compInfo.getSite() == null) {
          final TRAutomatonProxy tr = trs[a];
          for (final SupervisorSite shadow : compInfo.getShadowingSites()) {
            if (!isObservable(shadow)) {
              final List<AutomatonEventInfo> list = nonObserverMap.get(shadow);
              if (list != null) {
                final AutomatonEventInfo shadowInfo =
                  new AutomatonEventInfo(refInfo, c, tr, true, true);
                list.add(shadowInfo);
              }
            }
          }
        }
        c++;
      }

      if (mSiteDisablers == null) {
        final int numGroups = 2 + nonObserverMap.size();
        mSiteDisablers = new ArrayList<>(numGroups);
        mSiteDisablers.add(refList);
        mSiteDisablers.add(observerList);
        mSiteDisablers.addAll(nonObserverMap.values());
      } else {
        // reverse
        mSiteDisablers.clear();
        mSiteDisablers.add(Collections.emptyList());
        observerList.addAll(refList);
        mSiteDisablers.add(observerList);
        mSiteDisablers.addAll(nonObserverMap.values());
      }
    }

    private void collectSiteUpdates(final boolean det)
    {
      // Index group 0 (reference) merged into group 1 (observers if existent)
      // mSiteUpdates[0] always remains null
      for (int disIndex = 0; disIndex < mSiteDisablers.size(); disIndex++) {
        final int updateIndex;
        if (disIndex > 0) {
          updateIndex = disIndex;
        } else if (mReverse) {
          continue;
        } else {
          updateIndex = 1;
        }
        final List<AutomatonEventInfo> list = mSiteDisablers.get(disIndex);
        for (final AutomatonEventInfo info : list) {
          if (!info.isSelfloopOnly() && info.isDetermistic() == det) {
            info.setNextUpdate(mSiteUpdates[updateIndex]);
            mSiteUpdates[updateIndex] = info;
          }
        }
      }
    }

    @Override
    protected boolean expandState(final int[] encoded, final int[] decoded)
      throws AnalysisException
    {
      AutomatonEventInfo disablingAutInfo;
      if (mReverse) {
        disablingAutInfo = findDisabling(decoded, 1);
      } else {
        final List<AutomatonEventInfo> disablers0 = mSiteDisablers.get(0);
        final int d = findDisablingIndex(decoded, disablers0);
        if (d >= 0) {
          disablingAutInfo = disablers0.get(d);
          if (disablingAutInfo.isPlant()) {
            return true;
          } else {
            // enabled by reference plant, disabled by reference spec
            // check controllability condition
            if (isControlledByShadowingSite(disablingAutInfo)) {
              return true;
            }
            for (int i = d + 1; i < disablers0.size(); i++) {
              final AutomatonEventInfo autInfo = disablers0.get(i);
              if (!autInfo.isEnabled(decoded) &&
                  isControlledByShadowingSite(autInfo)) {
                return true;
              }
            }
            final List<AutomatonEventInfo> disablers1 = mSiteDisablers.get(1);
            for (final AutomatonEventInfo autInfo : disablers1) {
              final int a = autInfo.getAutomatonIndex();
              final SupervisorSite site = mComponentInfoList.get(a).getSite();
              if (isControllable(site) && !autInfo.isEnabled(decoded)) {
                return true;
              }
            }
            for (int g = 2; g < mSiteDisablers.size(); g++) {
              final List<AutomatonEventInfo> disablersG = mSiteDisablers.get(g);
              assert !disablersG.isEmpty();
              final AutomatonEventInfo autInfo = disablersG.get(0);
              final int a = autInfo.getAutomatonIndex();
              final SupervisorSite site = mComponentInfoList.get(a).getSite();
              if (isControllable(site) && findDisabling(decoded, g) != null) {
                return true;
              }
            }
            // not coobservable
            final int e = getOutputCode();
            final int a = disablingAutInfo.getAutomatonIndex();
            if (!handleUncontrollableState(e, a)) {
              return false;
            }
          }
        }
        disablingAutInfo = findDisabling(decoded, 1);
      }
      if (disablingAutInfo == null) {
        createSuccessorStatesEncodedOrDecoded(encoded, decoded, 1);
      }
      for (int g = 2; g < mSiteDisablers.size(); g++) {
        if (findDisabling(decoded, g) == null) {
          createSuccessorStatesEncodedOrDecoded(encoded, decoded, g);
        }
      }
      return true;
    }

    private AutomatonEventInfo findDisabling(final int[] decoded,
                                             final int groupIndex)
    {
      final List<AutomatonEventInfo> list = mSiteDisablers.get(groupIndex);
      return findDisabling(decoded, list);
    }

    private boolean isControlledByShadowingSite(final AutomatonEventInfo autInfo)
    {
      final int a = autInfo.getAutomatonIndex();
      final ComponentInfo compInfo = mComponentInfoList.get(a);
      for (final SupervisorSite site : compInfo.getShadowingSites()) {
        if (isControllable(site)) {
          return true;
        }
      }
      return false;
    }

    private void createSuccessorStatesEncodedOrDecoded(final int[] encoded,
                                                       final int[] decoded,
                                                       final int groupIndex)
      throws OverflowException
    {
      if (mReverse) {
        createSuccessorStatesDecoded(decoded, groupIndex);
      } else {
        createSuccessorStatesEncoded(encoded, groupIndex);
      }
    }

    private TraceStepProxy buildIntermediateTraceStep
      (final int[] decodedSource,
       final int[] decodedTarget,
       final SupervisorSite site)
    {
      final EventProxy event = getEvent();
      final TRAutomatonProxy[] trs = getTRAutomata();
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(mNumAutomata);
      for (final AutomatonEventInfo autInfo : mSiteDisablers.get(0)) {
        final int c0 = autInfo.getAutomatonIndex();
        final ComponentInfo compInfo = mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final TRAutomatonProxy tr = trs[a];
        final int c = site.getComponentIndex(a);
        final int s = decodedSource[c];
        final EventEncoding enc = tr.getEventEncoding();
        final int e = enc.getEventCode(event);
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final TransitionIterator iter =
          rel.createSuccessorsReadOnlyIterator(s, e);
        if (!iter.advance()) {
          assert false : "Counterexample transition not defined!";
        } else if (iter.advance()) { // nondeterministic, so state info needed
          final AutomatonProxy aut = getInputAutomaton(a);
          final int t = decodedTarget[c];
          final StateProxy state = tr.getState(t);
          stateMap.put(aut, state);
        }
      }
      final ProductDESProxyFactory factory = getFactory();
      return factory.createTraceStepProxy(event, stateMap);
    }

    private TraceStepProxy buildFinalTraceStep
      (final int[] decoded,
       final SupervisorSite site)
    {
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(mNumAutomata);
      for (final AutomatonEventInfo autInfo : mSiteDisablers.get(0)) {
        final int c0 = autInfo.getAutomatonIndex();
        final ComponentInfo compInfo = mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final int c = site.getComponentIndex(a);
        autInfo.putTargetInStateMap(stateMap, decoded[c],
                                    TRMonolithicCoobservabilityChecker.this);
      }
      final ProductDESProxyFactory factory = getFactory();
      final EventProxy event = getEvent();
      return factory.createTraceStepProxy(event, stateMap);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.monolithic.
    //# AbstractTRMonolithicModelAnalyzer.EventInfo
    @Override
    public void resetForReverse()
    {
      super.resetForReverse();
    }

    @Override
    protected Comparator<AutomatonEventInfo> getAutomatonEventInfoComparator()
    {
      if (mCoobservabilityAutomatonEventInfoComparator == null) {
        mCoobservabilityAutomatonEventInfoComparator =
          new CoobservabilityAutomatonEventInfoCamparator();
      }
      return mCoobservabilityAutomatonEventInfoComparator;
    }

    //#######################################################################
    //# State Expansion
    private void createSuccessorStatesEncoded
      (final int[] encoded,
       final int groupIndex)
      throws OverflowException
    {
      final AutomatonEventInfo update = mSiteUpdates[groupIndex];
      if (update != null) {
        final int code = getOutputCode();
        final int[] encodedTarget = getEncodedTargetBuffer(encoded);
        update.createSuccessorStatesEncoded(code, encodedTarget, true,
                                            TRMonolithicCoobservabilityChecker.this);
      }
    }

    private void createSuccessorStatesDecoded
      (final int[] decoded,
       final int groupIndex)
      throws OverflowException
    {
      final AutomatonEventInfo update = mSiteUpdates[groupIndex];
      if (update != null) {
        final int code = getOutputCode();
        final int[] decodedTarget = getDecodedTargetBuffer(decoded);
        update.createSuccessorStatesDecoded(code, decodedTarget, true,
                                            TRMonolithicCoobservabilityChecker.this);
      }
    }

    //#######################################################################
    //# Instance Variables
    /**
     * Set of supervisor sites that can disable this event.
     */
    private Set<SupervisorSite> mControllers;
    /**
     * Set of supervisor sites that can observe this event.
     */
    private Set<SupervisorSite> mObservers;
    /**
     * List of lists of {@link
     * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
     * AutomatonEventInfo} records representing the automaton/site pairs whose
     * state components need to be checked to determine whether a transition
     * occurs. Each list represents a group or site with tuple components
     * that need to be considered together by the algorithm.</P>
     * @see CoobservabilityEventInfo
     */
    private List<List<AutomatonEventInfo>> mSiteDisablers;
    /**
     * Array of {@link
     * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
     * AutomatonEventInfo} records representing the automaton/site pairs whose
     * state components need updating when a transition occurs. Each entry is
     * the start of a linked list, <CODE>null</CODE> for empty list, where the
     * next list element is stored in the {@link
     * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo AutomatonEventInfo}
     * object. The array entries are indexed by group number, with groups 0
     * and&nbsp;1 combined under index&nbsp;1; index&nbsp;0 always holds a
     * <CODE>null</CODE> entry.</P>
     * @see CoobservabilityEventInfo
     */
    private AutomatonEventInfo[] mSiteUpdates;
  }


  //#########################################################################
  //# Inner Class ComponentInfo
  private static class ComponentInfo
  {
    //#######################################################################
    //# Constructors
    private ComponentInfo(final TRAutomatonProxy tr,
                          final int autIndex,
                          final SupervisorSite site)
    {
      mTRAutomaton = tr;
      mAutomatonIndex = autIndex;
      mSite = site;
      if (site.isReferenceSite()) {
        mShadowingSites = new LinkedList<>();
      } else {
        mShadowingSites = Collections.singletonList(site);
      }
    }

    //#######################################################################
    //# Simple Access
    private int getAutomatonIndex()
    {
      return mAutomatonIndex;
    }

    private SupervisorSite getSite()
    {
      return mSite;
    }

    private List<SupervisorSite> getShadowingSites()
    {
      return mShadowingSites;
    }

    private void addShadowingSite(final SupervisorSite site)
    {
      mShadowingSites.add(site);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mTRAutomaton.getName() + " @ " + mSite.getName();
    }

    //#######################################################################
    //# Instance Variables
    private final TRAutomatonProxy mTRAutomaton;
    private final int mAutomatonIndex;
    private final SupervisorSite mSite;
    private final List<SupervisorSite> mShadowingSites;
  }


  //#########################################################################
  //# Inner Class CoobservabilityAutomatonEventInfoCamparator
  private static class CoobservabilityAutomatonEventInfoCamparator
    implements Comparator<AutomatonEventInfo>
  {
    //#######################################################################
    //# Interface java.util.Comparator<AutomatonEventInfo>
    @Override
    public int compare(final AutomatonEventInfo info1,
                       final AutomatonEventInfo info2)
    {
      // ignoring event controllability (unlike default)
      final float prob1 = info1.getProbability();
      final float prob2 = info2.getProbability();
      final boolean prioritised1 = info1.isPlant() && prob1 < 1.0f;
      final boolean prioritised2 = info2.isPlant() && prob2 < 1.0f;
      if (prioritised1 && !prioritised2) {
        return -1;
      } else if (prioritised2 && !prioritised1) {
        return 1;
      } else if (prob1 < prob2) {
        return -1;
      } else if (prob1 > prob2) {
        return 1;
      } else {
        return info1.getAutomatonIndex() - info2.getAutomatonIndex();
      }
    }
  }


  //#########################################################################
  //# Instance Variables
  private String mDefaultSiteName =
    CoobservabilityAttributeFactory.DEFAULT_SITE_NAME;

  private int mNumAutomata;
  private TRAutomatonProxy[] mTRAutomataExtended;
  private SupervisorSite mReferenceSite;
  private Map<String,SupervisorSite> mSiteMap;
  private Map<EventProxy,CoobservabilityEventInfo> mCoobservabilityEventInfoMap;
  private List<ComponentInfo> mComponentInfoList;
  private boolean mReverse;


  //#########################################################################
  //# Class Constants
  private static CoobservabilityAutomatonEventInfoCamparator
    mCoobservabilityAutomatonEventInfoComparator = null;

}
