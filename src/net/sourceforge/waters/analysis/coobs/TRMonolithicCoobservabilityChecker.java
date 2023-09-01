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

import gnu.trove.list.array.TIntArrayList;
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
  public void setDefaultSite(final String name)
  {
    mDefaultSite = "".equals(name) ? null : name;
  }

  @Override
  public String getDefaultSite()
  {
    return mDefaultSite == null ? "" : mDefaultSite;
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
      setDefaultSite(value);
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
      (CoobservabilityDiagnostics.REFERENCE_SITE_NAME, -1, mNumAutomata);
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
            if (mDefaultSite != null) {
              if (!controlled &&
                  (enc.getProperEventStatus(e) &
                   EventStatus.STATUS_CONTROLLABLE) != 0) {
                final SupervisorSite site = getSite(mDefaultSite);
                info.addController(site);
              }
              if (!observed && event.isObservable()) {
                final SupervisorSite site = getSite(mDefaultSite);
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
      final ComponentInfo info0 = new ComponentInfo(tr, a);
      final int c0 = mComponentInfoList.size();
      mComponentInfoList.add(info0);
      mReferenceSite.setComponentIndex(a, c0);
      if (!mSiteMap.isEmpty()) {
        switch (tr.getKind()) {
        case PLANT:
          for (final SupervisorSite site : mSiteMap.values()) {
            site.setComponentIndex(a, c0);
          }
          break;
        case SPEC:
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
          break;
        default:
          break;
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
    if (mTRAutomataExtended == null) {
      final int numComponents = mComponentInfoList.size();
      mTRAutomataExtended = new TRAutomatonProxy[numComponents];
    }
    int a = 0;
    for (final ComponentInfo info : mComponentInfoList) {
      final int a0 = info.getAutomatonIndex();
      mTRAutomataExtended[a++] = automata[a0];
    }
    return super.setUpTransitions(mTRAutomataExtended, eventInfoMap, reverse);
  }

  @Override
  protected void setUpStateTupleEncoding()
    throws AnalysisAbortException, OverflowException
  {
    setUpStateTupleEncoding(mTRAutomataExtended);
  }

  @Override
  protected void postProcessEventInfo(final List<EventInfo> eventInfoList)
    throws AnalysisException
  {
    super.postProcessEventInfo(eventInfoList);
    for (final CoobservabilityEventInfo info :
         mCoobservabilityEventInfoMap.values()) {
      checkAbort();
      info.computeSiteIndices(this);
    }
  }

  @Override
  protected int storeInitialStates()
    throws AnalysisException
  {
    return storeInitialStates(mTRAutomataExtended);
  }

  @Override
  protected boolean expandState(final int[] encoded,
                                final int[] decoded,
                                final EventInfo event)
    throws AnalysisException
  {
    final CoobservabilityEventInfo eventInfo =
      (CoobservabilityEventInfo) event;
    int d, siteEnd;
    if (mReverse) {
      siteEnd = eventInfo.getSiteGroupIndex(1);
      d = eventInfo.findDisabling(decoded, 0, siteEnd);
    } else {
      siteEnd = eventInfo.getSiteGroupIndex(0);
      d = eventInfo.findDisabling(decoded, 0, siteEnd);
      if (d >= 0) {
        final List<AutomatonEventInfo> disabling =
          eventInfo.getDisablingAutomata();
        final AutomatonEventInfo disablingAutInfo = disabling.get(d);
        if (disablingAutInfo.isPlant()) {
          return true;
        } else {
          // enabled by reference plant, disabled by reference spec
          // check controllability condition
          if (isControlledByShadowingSite(eventInfo, disablingAutInfo)) {
            return true;
          }
          for (int i = d + 1; i < siteEnd; i++) {
            final AutomatonEventInfo autInfo = disabling.get(i);
            if (!autInfo.isEnabled(decoded) &&
              isControlledByShadowingSite(eventInfo, autInfo)) {
              return true;
            }
          }
          final int end = disabling.size();
          for (int i = siteEnd; i < end; i++) {
            final AutomatonEventInfo autInfo = disabling.get(i);
            final int a = autInfo.getAutomatonIndex();
            final SupervisorSite site = mComponentInfoList.get(a).getSite();
            if (eventInfo.isControllable(site) && !autInfo.isEnabled(decoded)) {
              return true;
            }
          }
          // not coobservable
          final int e = event.getOutputCode();
          final int a = disablingAutInfo.getAutomatonIndex();
          if (!handleUncontrollableState(e, a)) {
            return false;
          }
        }
      }
      if (eventInfo.hasObservingSpec()) {
        final int siteStart = siteEnd;
        siteEnd = eventInfo.getSiteGroupIndex(1);
        d = eventInfo.findDisabling(decoded, siteStart, siteEnd);
      }
    }
    if (d < 0) {
      createSuccessorStatesEncodedOrDecoded(encoded, decoded, eventInfo, 1);
    }
    final int endGroupIndex = eventInfo.getNumberOfSiteIndices();
    for (int groupIndex = 2; groupIndex < endGroupIndex; groupIndex++) {
      final int siteStart = siteEnd;
      siteEnd = eventInfo.getSiteGroupIndex(groupIndex);
      if (eventInfo.findDisabling(decoded, siteStart, siteEnd) < 0) {
        createSuccessorStatesEncodedOrDecoded(encoded, decoded,
                                              eventInfo, groupIndex);
      }
    }
    return true;
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
        eventInfo.buildFinalTraceStep(this, decodedTarget, site);
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
      if (stepSite == mReferenceSite) {
        final List<TraceStepProxy> list0 = steps.get(0);
        final TraceStepProxy step0 = eventInfo.buildIntermediateTraceStep
          (this, decodedSource, decodedTarget, mReferenceSite);
        list0.add(0, step0);
        int g = 1;
        for (final SupervisorSite site : controllers) {
          if (eventInfo.isObservable(site)) {
            final List<TraceStepProxy> list = steps.get(g);
            final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
              (this, decodedSource, decodedTarget, site);
            list.add(0, step);
          }
          g++;
        }
      } else {
        final int g = controllers.indexOf(stepSite) + 1;
        final List<TraceStepProxy> list = steps.get(g);
        final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
          (this, decodedSource, decodedTarget, stepSite);
        list.add(0, step);
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
      site = new SupervisorSite(name, index, mNumAutomata);
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
    for (int e = EventEncoding.TAU; e < enc.getNumberOfProperEvents(); e++) {
      if ((enc.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
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

  private boolean isControlledByShadowingSite
    (final CoobservabilityEventInfo eventInfo,
     final AutomatonEventInfo autInfo)
  {
    final int a = autInfo.getAutomatonIndex();
    final ComponentInfo compInfo = mComponentInfoList.get(a);
    for (final SupervisorSite site : compInfo.getShadowingSites()) {
      if (eventInfo.isControllable(site)) {
        return true;
      }
    }
    return false;
  }

  private void createSuccessorStatesEncodedOrDecoded
    (final int[] encoded,
     final int[] decoded,
     final CoobservabilityEventInfo eventInfo,
     final int groupIndex)
    throws OverflowException
  {
    if (mReverse) {
      eventInfo.createSuccessorStatesDecoded(decoded, groupIndex, this);
    } else {
      eventInfo.createSuccessorStatesEncoded(encoded, groupIndex, this);
    }
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
   * <P>A list of {@link
   * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
   * AutomatonEventInfo} records representing these pairs is retrieved by
   * calling {@link #getDisablingAutomata()}). Within the list, the pairs are
   * organised into groups as follows:</P>
   *
   * <UL>
   * <LI>Group 0 contains plants and specifications that use the event,
   * with no associated supervisor site. These represent the so-called
   * <I>reference state</I>, the state the plant is in (while supervisors may
   * believe that it is in another state).</LI>
   * <LI>Group 1 contains specifications that use the event, one for each
   * site that can observe the event. These state components must be updated
   * together with those in group 0 if a transition is executed.</LI>
   * <LI>Groups 2 and following contain specifications that use the event,
   * with a separate group for each site that cannot observe the event.
   * The state components in each of these groups are updated together
   * but independently from all other groups.</LI>
   * </UL>
   *
   * <P>The state components are ordered by group number for processing,
   * the order within groups is optimised for speed of processing. Within
   * group 0, plants are processed before specifications. The field
   * {@link #mSiteIndices} determines which automaton/site pairs belong
   * to which groups. Additionally, the field {@link #mSiteUpdates}
   * contains lists of {@link
   * net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer.AutomatonEventInfo
   * AutomatonEventInfo} records representing the automaton/site pairs whose
   * state component need updating when a transition occurs. They are indexed
   * by the group numbers above, with groups 0 and&nbsp;1 combined under
   * index&nbsp;1, and index&nbsp;0 always holds a <CODE>null</CODE> entry.</P>
   */
  private static class CoobservabilityEventInfo
    extends AbstractTRMonolithicModelAnalyzer.EventInfo
  {
    //#######################################################################
    //# Constructor
    private CoobservabilityEventInfo
      (final TRMonolithicCoobservabilityChecker verifier,
       final EventProxy event)
    {
      super(event, false);
      mComparator =
        verifier.new CoobservabilityAutomatonEventInfoCamparator(this);
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

    private boolean hasObservingSpec()
    {
      return mHasObservingSpec;
    }

    private int getSiteGroupIndex(final int groupIndex)
    {
      return mSiteIndices[groupIndex];
    }

    private int getNumberOfSiteIndices()
    {
      return mSiteIndices.length;
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

    private void computeSiteIndices
      (final TRMonolithicCoobservabilityChecker verifier)
    {
      if (mSiteIndices == null) {
        final TIntArrayList indices =
          new TIntArrayList(verifier.mSiteMap.size() + 1);
        int index = 0;
        int prevGroup = 0;
        SupervisorSite prevSite = null;
        for (final AutomatonEventInfo info : getDisablingAutomata()) {
          final int a = info.getAutomatonIndex();
          final SupervisorSite site =
            verifier.mComponentInfoList.get(a).getSite();
          final int group = getSiteGroup(site);
          mHasObservingSpec |= group == 1;
          if (group != prevGroup || group == 2 && site != prevSite) {
            if (group == 2 && prevGroup == 0) {
              indices.add(index);
            }
            indices.add(index);
            prevGroup = group;
            prevSite = site;
          }
          index++;
        }
        if (prevGroup == 0) {
          indices.add(index);
        }
        indices.add(index);
        mSiteIndices = indices.toArray();
        mSiteUpdates = new AutomatonEventInfo[indices.size()];
      } else {
        Arrays.fill(mSiteUpdates, null);
      }
      collectSiteUpdates(false);
      collectSiteUpdates(true);
    }

    /**
     * Adds update instructions to lists within the array {@link #mSiteIndices}.
     * This method processes the list of disabling automata
     * ({@link #getDisablingAutomata()} and prepends entries representing
     * deterministic or nondeterministic automata (depending on the parameter)
     * to the list representing their site group.
     * @param  det  Whether or not deterministic automata are to be included.
     * @see CoobservabilityEventInfo
     */
    private void collectSiteUpdates(final boolean det)
    {
      int index = 0;
      // Index group 0 (reference) merged into group 1 (observers if existent)
      // mSiteUpdates[0] always remains null
      int s = 1;
      // TODO This assumes always enabling components are listed as 'disabling'
      for (final AutomatonEventInfo info : getDisablingAutomata()) {
        while (mSiteIndices[s] <= index) {
          s++;
        }
        if (!info.isSelfloopOnly() && info.isDetermistic() == det) {
          info.setNextUpdate(mSiteUpdates[s]);
          mSiteUpdates[s] = info;
        }
        index++;
      }
    }

    private TraceStepProxy buildIntermediateTraceStep
      (final TRMonolithicCoobservabilityChecker analyzer,
       final int[] decodedSource,
       final int[] decodedTarget,
       final SupervisorSite site)
    {
      final EventProxy event = getEvent();
      final TRAutomatonProxy[] trs = analyzer.getTRAutomata();
      final int numAut = trs.length;
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
      final List<AutomatonEventInfo> disabling = getDisablingAutomata();
      for (int i = 0; i < mSiteIndices[1]; i++) {
        final AutomatonEventInfo transInfo = disabling.get(i);
        final int c0 = transInfo.getAutomatonIndex();
        final ComponentInfo compInfo = analyzer.mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final int c = site.getComponentIndex(a);
        final TRAutomatonProxy tr = trs[a];
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final int s = decodedSource[c];
        final EventEncoding enc = tr.getEventEncoding();
        final int e = enc.getEventCode(event);
        final TransitionIterator iter =
          rel.createSuccessorsReadOnlyIterator(s, e);
        if (!iter.advance()) {
          assert false : "Counterexample transition not defined!";
        } else if (iter.advance()) { // nondeterministic, so state info needed
          final AutomatonProxy aut = analyzer.getInputAutomaton(a);
          final int t = decodedTarget[c];
          final StateProxy state = tr.getState(t);
          stateMap.put(aut, state);
        }
      }
      final ProductDESProxyFactory factory = analyzer.getFactory();
      return factory.createTraceStepProxy(event, stateMap);
    }

    private TraceStepProxy buildFinalTraceStep
      (final TRMonolithicCoobservabilityChecker analyzer,
       final int[] decoded,
       final SupervisorSite site)
    {
      final int numAut = analyzer.getTRAutomata().length;
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
      final List<AutomatonEventInfo> disabling = getDisablingAutomata();
      for (int i = 0; i < mSiteIndices[1]; i++) {
        final AutomatonEventInfo transInfo = disabling.get(i);
        final int c0 = transInfo.getAutomatonIndex();
        final ComponentInfo compInfo = analyzer.mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final int c = site.getComponentIndex(a);
        transInfo.putTargetInStateMap(stateMap, decoded[c], analyzer);
      }
      final ProductDESProxyFactory factory = analyzer.getFactory();
      final EventProxy event = getEvent();
      return factory.createTraceStepProxy(event, stateMap);
    }

    /**
     * Gets the site group representing the given site when processing this
     * event.
     * @see CoobservabilityEventInfo
     */
    private int getSiteGroup(final SupervisorSite site)
    {
      if (site == null) {
        return 0;
      } else if (isObservable(site)) {
        return 1;
      } else {
        return 2;
      }
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
      return mComparator;
    }

    //#######################################################################
    //# State Expansion
    private void createSuccessorStatesEncoded
      (final int[] encoded,
       final int groupIndex,
       final TRMonolithicCoobservabilityChecker verifier)
      throws OverflowException
    {
      final AutomatonEventInfo update = mSiteUpdates[groupIndex];
      if (update != null) {
        final int code = getOutputCode();
        final int[] encodedTarget = verifier.getEncodedTargetBuffer(encoded);
        update.createSuccessorStatesEncoded(code, encodedTarget, true, verifier);
      }
    }

    private void createSuccessorStatesDecoded
      (final int[] decoded,
       final int groupIndex,
       final TRMonolithicCoobservabilityChecker verifier)
      throws OverflowException
    {
      final AutomatonEventInfo update = mSiteUpdates[groupIndex];
      if (update != null) {
        final int code = getOutputCode();
        final int[] decodedTarget = verifier.getDecodedTargetBuffer(decoded);
        update.createSuccessorStatesDecoded(code, decodedTarget, true, verifier);
      }
    }

    //#######################################################################
    //# Instance Variables
    private final CoobservabilityAutomatonEventInfoCamparator mComparator;
    private Set<SupervisorSite> mControllers;
    private Set<SupervisorSite> mObservers;
    private boolean mHasObservingSpec;
    /**
     * Array of indices of automaton/event entries for different site
     * groups to explore. For each site group, the array contains the
     * smallest index into the list of disabling components ({@link
     * #getDisablingAutomata()}) representing an entry no longer contained
     * in the associated group.
     * @see CoobservabilityEventInfo
     */
    private int[] mSiteIndices;
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
    private ComponentInfo(final TRAutomatonProxy tr, final int autIndex)
    {
      mTRAutomaton = tr;
      mAutomatonIndex = autIndex;
      mSite = null;
      mShadowingSites = new LinkedList<>();
    }

    private ComponentInfo(final TRAutomatonProxy tr,
                          final int autIndex,
                          final SupervisorSite site)
    {
      mTRAutomaton = tr;
      mAutomatonIndex = autIndex;
      mSite = site;
      mShadowingSites = Collections.singletonList(null);
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    private TRAutomatonProxy getTRAutomaton()
    {
      return mTRAutomaton;
    }

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
      if (mSite == null) {
        return mTRAutomaton.getName() + " (reference)";
      } else {
        return mTRAutomaton.getName() + " @ " + mSite.getName();
      }
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
  private class CoobservabilityAutomatonEventInfoCamparator
    extends DefaultAutomatonEventInfoCamparator
  {
    //#######################################################################
    //# Constructor
    private CoobservabilityAutomatonEventInfoCamparator
      (final CoobservabilityEventInfo info)
    {
      mEventInfo = info;
    }

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonEventInfo>
    @Override
    public int compare(final AutomatonEventInfo info1,
                       final AutomatonEventInfo info2)
    {
      final int index1 = info1.getAutomatonIndex();
      final SupervisorSite site1 = mComponentInfoList.get(index1).getSite();
      final int group1 = mEventInfo.getSiteGroup(site1);
      final int index2 = info2.getAutomatonIndex();
      final SupervisorSite site2 = mComponentInfoList.get(index2).getSite();
      final int group2 = mEventInfo.getSiteGroup(site2);
      if (group1 != group2) {
        return group1 - group2;
      } else if (site1 != null) {
        final int result = site1.compareTo(site2);
        if (result != 0) {
          return result;
        }
      }
      return super.compare(info1, info2);
    }

    //#######################################################################
    //# Instance Variables
    private final CoobservabilityEventInfo mEventInfo;
  }


  //#########################################################################
  //# Instance Variables
  private String mDefaultSite = CoobservabilityAttributeFactory.DEFAULT_SITE_NAME;

  private int mNumAutomata;
  private SupervisorSite mReferenceSite;
  private Map<String,SupervisorSite> mSiteMap;
  private Map<EventProxy,CoobservabilityEventInfo> mCoobservabilityEventInfoMap;
  private List<ComponentInfo> mComponentInfoList;
  private TRAutomatonProxy[] mTRAutomataExtended;
  private boolean mReverse;

}
