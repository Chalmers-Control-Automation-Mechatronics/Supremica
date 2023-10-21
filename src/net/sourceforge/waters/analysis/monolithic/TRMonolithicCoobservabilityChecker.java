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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.coobs.CoobservabilityDiagnostics;
import net.sourceforge.waters.analysis.coobs.CoobservabilitySignature;
import net.sourceforge.waters.analysis.tr.EventEncoding;
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
import net.sourceforge.waters.model.base.EventKind;
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
  public void setSignature(final CoobservabilitySignature sig)
  {
    mSignature = sig;
  }

  @Override
  public CoobservabilityCounterExampleProxy getCounterExample()
  {
    return (CoobservabilityCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public MonolithicCoobservabilityVerificationResult createAnalysisResult()
  {
    return new MonolithicCoobservabilityVerificationResult(this);
  }

  @Override
  public MonolithicCoobservabilityVerificationResult getAnalysisResult()
  {
    return (MonolithicCoobservabilityVerificationResult) super.getAnalysisResult();
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
    } else {
      super.setOption(option);
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

    final MonolithicCoobservabilityVerificationResult result = getAnalysisResult();
    final ProductDESProxy des = getModel();
    final KindTranslator translator = getKindTranslator();
    final TRAutomatonProxy[] trs = getTRAutomata();
    if (mSignature == null) {
      mSignature = new CoobservabilitySignature(des, translator,
                                                mDefaultSiteName);
    }
    result.setNumberOfSites(mSignature.getSites().size());
    mSignature.filter(trs, translator);
    mSignature.merge();
    mSignature.subsume();
    result.updatePeakNumberOfSites(mSignature.getSites().size());

    mNumAutomata = trs.length;
    mReferenceSite = new SiteInfo(mNumAutomata);
    final Collection<CoobservabilitySignature.Site> sites = mSignature.getSites();
    final int numSites = sites.size();
    mSiteInfoMap = new LinkedHashMap<>(numSites);
    int index = 0;
    for (final CoobservabilitySignature.Site site : sites) {
      final SiteInfo info = new SiteInfo(site, index++, mNumAutomata);
      mSiteInfoMap.put(site, info);
    }

    final Collection<EventProxy> events = des.getEvents();
    final int numEvents = events.size();
    mCoobservabilityEventInfoMap = new HashMap<>(numEvents);
    for (final EventProxy event : des.getEvents()) {
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.CONTROLLABLE || kind == EventKind.UNCONTROLLABLE) {
        final CoobservabilityEventInfo info =
          new CoobservabilityEventInfo(event);
        mCoobservabilityEventInfoMap.put(event, info);
      }
    }

    mComponentInfoList = new ArrayList<>(mNumAutomata);
    for (int a = 0; a < mNumAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy tr = trs[a];
      final ComponentInfo compInfo0 = new ComponentInfo(tr, a, mReferenceSite);
      final int c0 = mComponentInfoList.size();
      mComponentInfoList.add(compInfo0);
      mReferenceSite.setComponentIndex(a, c0);
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      final boolean det = rel.isDeterministic();
      for (final SiteInfo siteInfo : mSiteInfoMap.values()) {
        final CoobservabilitySignature.Site site = siteInfo.getSite();
        if (det && !hasUnobservableTransition(tr, site)) {
          compInfo0.addShadowingSite(siteInfo);
          siteInfo.setComponentIndex(a, c0);
        } else {
          final ComponentInfo compInfo1 = new ComponentInfo(tr, a, siteInfo);
          final int c = mComponentInfoList.size();
          mComponentInfoList.add(compInfo1);
          siteInfo.setComponentIndex(a, c);
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
  protected int getStateTupleSize()
  {
    return mTRAutomataExtended.length;
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
  protected int getTraceDepth(final int target)
  {
    return getDepthMapSize() - 2;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSignature = null;
    mReferenceSite = null;
    mSiteInfoMap = null;
    mCoobservabilityEventInfoMap = null;
    mTRAutomataExtended = null;
    mComponentInfoList = null;
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
    final StateProxy specState = specTR.getTRState(decodedTarget[c]);

    // Final (failing) trace step
    CoobservabilityEventInfo eventInfo =
      (CoobservabilityEventInfo) getEventInfo(e);
    final EventProxy event = eventInfo.getEvent();
    assert !mSignature.isTotallyControllable(event);
    final List<CoobservabilitySignature.Site> controllers =
      mSignature.getControllingSites(event);
    final int numTraces = controllers.size() + 1;
    Iterator<CoobservabilitySignature.Site> iter = controllers.iterator();
    final List<List<TraceStepProxy>> steps = new ArrayList<>(numTraces);
    for (int g = 0; g < numTraces; g++) {
      final SiteInfo siteInfo;
      if (g == 0) {
        siteInfo = mReferenceSite;
      } else {
        final CoobservabilitySignature.Site site = iter.next();
        siteInfo = mSiteInfoMap.get(site);
      }
      final List<TraceStepProxy> list = new LinkedList<>();
      final TraceStepProxy step =
        eventInfo.buildFinalTraceStep(decodedTarget, siteInfo);
      list.add(step);
      steps.add(list);
   }

    // Intermediate trace steps
    int target = s;
    final CounterExampleCallback callback = prepareForCounterExample(target);
    final int numInit = getNumberOfInitialStates();
    // Until we reach the start state ...
    while (target >= numInit) {
      callback.findPredecessor(target);
      eventInfo = (CoobservabilityEventInfo) callback.getFoundEvent();
      final int source = callback.getFoundSource();
      assert source < target;
      stateSpace.getContents(source, encoded);
      encoding.decode(encoded, decodedSource);
      final SiteInfo stepSiteInfo = findSteppingSite(decodedSource, decodedTarget);
      if (stepSiteInfo == mReferenceSite || eventInfo.isObservable(stepSiteInfo)) {
        final List<TraceStepProxy> list0 = steps.get(0);
        final TraceStepProxy step0 = eventInfo.buildIntermediateTraceStep
          (decodedSource, decodedTarget, mReferenceSite);
        list0.add(0, step0);
        int g = 1;
        for (final CoobservabilitySignature.Site site : controllers) {
          if (eventInfo.isObservable(site)) {
            final SiteInfo siteInfo = mSiteInfoMap.get(site);
            final List<TraceStepProxy> list = steps.get(g);
            final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
              (decodedSource, decodedTarget, siteInfo);
            list.add(0, step);
          }
          g++;
        }
      } else {
        final CoobservabilitySignature.Site stepSite = stepSiteInfo.getSite();
        final int i = controllers.indexOf(stepSite);
        if (i >= 0) {
          final List<TraceStepProxy> list = steps.get(i + 1);
          final TraceStepProxy step = eventInfo.buildIntermediateTraceStep
            (decodedSource, decodedTarget, stepSiteInfo);
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
      final SiteInfo siteInfo;
      if (g == 0) {
        siteInfo = mReferenceSite;
      } else {
        final CoobservabilitySignature.Site site = iter.next();
        siteInfo = mSiteInfoMap.get(site);
      }
      final List<TraceStepProxy> list = steps.get(g);
      final TraceStepProxy step = buildInitialTraceStep(decodedTarget, siteInfo);
      list.add(0, step);
   }

    // Build traces and counterexample
    final ProductDESProxyFactory factory = getFactory();
    final List<TraceProxy> traces = new ArrayList<>(numTraces);
    iter = controllers.iterator();
    for (int g = 0; g < numTraces; g++) {
      final SiteInfo siteInfo;
      if (g == 0) {
        siteInfo = mReferenceSite;
      } else {
        final CoobservabilitySignature.Site site = iter.next();
        siteInfo = mSiteInfoMap.get(site);
      }
      final String name = siteInfo.getName();
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
                                              final SiteInfo siteInfo)
  {
    final TRAutomatonProxy[] trs = getTRAutomata();
    final int numAut = trs.length;
    final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
    for (int a = 0; a < numAut; a++) {
      final TRAutomatonProxy tr = trs[a];
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      if (rel.hasNondeterministicInitialStates()) {
        final AutomatonProxy aut = getInputAutomaton(a);
        final int c = siteInfo.getComponentIndex(a);
        final int s = decoded[c];
        final StateProxy state = tr.getOriginalState(s);
        stateMap.put(aut, state);
      }
    }
    final ProductDESProxyFactory factory = getFactory();
    return factory.createTraceStepProxy(null, stateMap);
  }

  private SiteInfo findSteppingSite(final int[] decodedSource,
                                    final int[] decodedTarget)
  {
    for (int a = 0; a < mNumAutomata; a++) {
      final int c = mReferenceSite.getComponentIndex(a);
      if (decodedSource[c] != decodedTarget[c]) {
        return mReferenceSite;
      }
    }
    for (final SiteInfo site : mSiteInfoMap.values()) {
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
  private boolean hasUnobservableTransition(final TRAutomatonProxy tr,
                                            final CoobservabilitySignature.Site site)
  {
    final EventEncoding enc = tr.getEventEncoding();
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      if (iter.getCurrentFromState() != iter.getCurrentToState()) {
        final int e = iter.getCurrentEvent();
        final EventProxy event = enc.getProperEvent(e);
        if (!site.isObservedEvent(event)) {
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
    private CoobservabilityEventInfo(final EventProxy event)
    {
      super(event, false);
    }

    //#######################################################################
    //# Simple Access
    private boolean isTotallyControllable()
    {
      final EventProxy event = getEvent();
      return mSignature.isTotallyControllable(event);
    }

    private boolean isControllable(final CoobservabilitySignature.Site site)
    {
      final EventProxy event = getEvent();
      return site.isControlledEvent(event);
    }

    private boolean isControllable(final SiteInfo siteInfo)
    {
      final CoobservabilitySignature.Site site = siteInfo.getSite();
      return isControllable(site);
    }

    private boolean isObservable(final CoobservabilitySignature.Site site)
    {
      final EventProxy event = getEvent();
      return site.isObservedEvent(event);
    }

    private boolean isObservable(final SiteInfo siteInfo)
    {
      final CoobservabilitySignature.Site site = siteInfo.getSite();
      return isObservable(site);
    }

    //#######################################################################
    //# Initialisation
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
      final Map<SiteInfo,List<AutomatonEventInfo>> nonObserverMap =
        new LinkedHashMap<>();
      int c = 0;
      for (final ComponentInfo compInfo : mComponentInfoList) {
        final int a = compInfo.getAutomatonIndex();
        final AutomatonEventInfo refInfo = disablingMap.get(a);
        if (refInfo != null) {
          final TRAutomatonProxy tr = trs[a];
          final SiteInfo site = compInfo.getSiteInfo();
          if (site.isReferenceSite()) {
            final boolean plant = refInfo.isPlant();
            final AutomatonEventInfo altInfo =
              new AutomatonEventInfo(refInfo, c, tr, plant, true);
            refList.add(altInfo);
          } else {
            final AutomatonEventInfo autInfo =
              new AutomatonEventInfo(refInfo, c, tr, true, true);
            if (isObservable(site)) {
              observerList.add(autInfo);
            } else {
              List<AutomatonEventInfo> list = nonObserverMap.get(site);
              if (list == null) {
                list = new LinkedList<>();
                nonObserverMap.put(site, list);
              }
              list.add(autInfo);
            }
          }
        }
        c++;
      }
      c = 0;
      for (final ComponentInfo compInfo : mComponentInfoList) {
        final int a = compInfo.getAutomatonIndex();
        final AutomatonEventInfo refInfo = disablingMap.get(a);
        if (refInfo != null && compInfo.getSiteInfo() == null) {
          final TRAutomatonProxy tr = trs[a];
          for (final SiteInfo shadow : compInfo.getShadowingSites()) {
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
          if (!isCoobservableState(decoded, d)) {
            return false;
          }
          disablingAutInfo = disablers0.get(d);
        } else {
          disablingAutInfo = findDisabling(decoded, 1);
        }
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

    private boolean isCoobservableState(final int[] decoded, final int d)
      throws AnalysisException
    {
      if (isTotallyControllable()) {
        return true;
      }
      final List<AutomatonEventInfo> disablers0 = mSiteDisablers.get(0);
      final AutomatonEventInfo disablingAutInfo = disablers0.get(d);
      if (disablingAutInfo.isPlant()) {
        return true;
      }
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
        final SiteInfo site = mComponentInfoList.get(a).getSiteInfo();
        if (isControllable(site) && !autInfo.isEnabled(decoded)) {
          return true;
        }
      }
      for (int g = 2; g < mSiteDisablers.size(); g++) {
        final List<AutomatonEventInfo> disablersG = mSiteDisablers.get(g);
        assert !disablersG.isEmpty();
        final AutomatonEventInfo autInfo = disablersG.get(0);
        final int a = autInfo.getAutomatonIndex();
        final SiteInfo site = mComponentInfoList.get(a).getSiteInfo();
        if (isControllable(site) && findDisabling(decoded, g) != null) {
          return true;
        }
      }
      // not coobservable
      final int e = getOutputCode();
      final int a = disablingAutInfo.getAutomatonIndex();
      return handleUncontrollableState(e, a);
    }

    private boolean isControlledByShadowingSite(final AutomatonEventInfo autInfo)
    {
      final int a = autInfo.getAutomatonIndex();
      final ComponentInfo compInfo = mComponentInfoList.get(a);
      for (final SiteInfo siteInfo : compInfo.getShadowingSites()) {
        final CoobservabilitySignature.Site site = siteInfo.getSite();
        if (isControllable(site)) {
          return true;
        }
      }
      return false;
    }

    private void createSuccessorStatesEncodedOrDecoded(final int[] encoded,
                                                       final int[] decoded,
                                                       final int groupIndex)
      throws AnalysisException
    {
      if (getStateCallback() != null) {
        createSuccessorStatesDecoded(decoded, groupIndex);
      } else {
        createSuccessorStatesEncoded(encoded, groupIndex);
      }
    }

    private TraceStepProxy buildIntermediateTraceStep
      (final int[] decodedSource,
       final int[] decodedTarget,
       final SiteInfo referenceSite)
    {
      final EventProxy event = getEvent();
      final TRAutomatonProxy[] trs = getTRAutomata();
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(mNumAutomata);
      for (final AutomatonEventInfo autInfo : mSiteDisablers.get(1)) {
        final int c0 = autInfo.getAutomatonIndex();
        final ComponentInfo compInfo = mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final TRAutomatonProxy tr = trs[a];
        final int c = referenceSite.getComponentIndex(a);
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
          final StateProxy state = tr.getOriginalState(t);
          stateMap.put(aut, state);
        }
      }
      final ProductDESProxyFactory factory = getFactory();
      return factory.createTraceStepProxy(event, stateMap);
    }

    private TraceStepProxy buildFinalTraceStep
      (final int[] decodedSource,
       final SiteInfo siteInfo)
    {
      final EventProxy event = getEvent();
      final TRAutomatonProxy[] trs = getTRAutomata();
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(mNumAutomata);
      for (final AutomatonEventInfo autInfo : mSiteDisablers.get(1)) {
        final int c0 = autInfo.getAutomatonIndex();
        final ComponentInfo compInfo = mComponentInfoList.get(c0);
        final int a = compInfo.getAutomatonIndex();
        final int c = siteInfo.getComponentIndex(a);
        final int s = decodedSource[c];
        final TRAutomatonProxy tr = trs[a];
        final EventEncoding enc = tr.getEventEncoding();
        final int e = enc.getEventCode(event);
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final TransitionIterator iter =
          rel.createSuccessorsReadOnlyIterator(s, e);
        if (iter.advance()) {
          final int t = iter.getCurrentTargetState();
          if (iter.advance()) {
            final AutomatonProxy aut = getInputAutomaton(a);
            final StateProxy state = tr.getOriginalState(t);
            stateMap.put(aut, state);

          }
        }
      }
      final ProductDESProxyFactory factory = getFactory();
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
      throws AnalysisException
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
  //# Inner Class SiteInfo
  private static class SiteInfo implements Comparable<SiteInfo>
  {
    //#########################################################################
    //# Constructor
    private SiteInfo(final int numAutomata)
    {
      mSite = null;
      mReference = true;
      mIndex = -1;
      mComponentIndices = new int[numAutomata];
    }

    private SiteInfo(final CoobservabilitySignature.Site site,
                     final int index,
                     final int numAutomata)
    {
      mSite = site;
      mReference = false;
      mIndex = index;
      mComponentIndices = new int[numAutomata];
    }

    //#########################################################################
    //# Simple Access
    private CoobservabilitySignature.Site getSite()
    {
      return mSite;
    }

    private String getName()
    {
      if (mSite == null) {
        return CoobservabilityDiagnostics.REFERENCE_SITE_NAME;
      } else {
        return mSite.getName();
      }
    }

    private boolean isReferenceSite()
    {
      return mReference;
    }

    private int getComponentIndex(final int autIndex)
    {
      return mComponentIndices[autIndex];
    }

    private void setComponentIndex(final int autIndex, final int compIndex)
    {
      mComponentIndices[autIndex] = compIndex;
    }

    //#########################################################################
    //# Interface java.util.Comparable<SiteInfo>
    @Override
    public int compareTo(final SiteInfo site)
    {
      return mIndex - site.mIndex;
    }

    //#########################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return getName();
    }

    //#########################################################################
    //# Data Members
    private final CoobservabilitySignature.Site mSite;
    private final boolean mReference;
    private final int mIndex;
    private final int[] mComponentIndices;
  }


  //#########################################################################
  //# Inner Class ComponentInfo
  private static class ComponentInfo
  {
    //#######################################################################
    //# Constructors
    private ComponentInfo(final TRAutomatonProxy tr,
                          final int autIndex,
                          final SiteInfo site)
    {
      mTRAutomaton = tr;
      mAutomatonIndex = autIndex;
      mSiteInfo = site;
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

    private SiteInfo getSiteInfo()
    {
      return mSiteInfo;
    }

    private List<SiteInfo> getShadowingSites()
    {
      return mShadowingSites;
    }

    private void addShadowingSite(final SiteInfo site)
    {
      mShadowingSites.add(site);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mTRAutomaton.getName() + " @ " + mSiteInfo.getName();
    }

    //#######################################################################
    //# Instance Variables
    private final TRAutomatonProxy mTRAutomaton;
    private final int mAutomatonIndex;
    private final SiteInfo mSiteInfo;
    private final List<SiteInfo> mShadowingSites;
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
  private CoobservabilitySignature mSignature;
  private SiteInfo mReferenceSite;
  private Map<CoobservabilitySignature.Site,SiteInfo> mSiteInfoMap;
  private Map<EventProxy,CoobservabilityEventInfo> mCoobservabilityEventInfoMap;
  private TRAutomatonProxy[] mTRAutomataExtended;
  private List<ComponentInfo> mComponentInfoList;
  private boolean mReverse;


  //#########################################################################
  //# Class Constants
  private static CoobservabilityAutomatonEventInfoCamparator
    mCoobservabilityAutomatonEventInfoComparator = null;

}
