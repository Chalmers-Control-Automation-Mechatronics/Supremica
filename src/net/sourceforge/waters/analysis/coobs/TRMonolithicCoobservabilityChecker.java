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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelAnalyzer;
import net.sourceforge.waters.analysis.monolithic.AbstractTRMonolithicModelVerifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>An implementation of the coobservability check algorithm.</P>
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
    mSiteMap = new LinkedHashMap<>();
    mCoobservabilityEventInfoMap = new HashMap<>(numEvents);

    for (int a = 0; a < mNumAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy aut = trs[a];
      final EventEncoding enc = aut.getEventEncoding();
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
                final SiteInfo site = getSite(value);
                info.addController(site);
                controlled = true;
              } else if (attrib.startsWith
                           (CoobservabilityAttributeFactory.OBSERVABITY_KEY)) {
                final String value = entry.getValue();
                final SiteInfo site = getSite(value);
                info.addObserver(site);
                observed = true;
              }
            }
            if (mDefaultSite != null) {
              if (!controlled &&
                  (enc.getProperEventStatus(e) &
                   EventStatus.STATUS_CONTROLLABLE) != 0) {
                final SiteInfo site = getSite(mDefaultSite);
                info.addController(site);
              }
              if (!observed && event.isObservable()) {
                final SiteInfo site = getSite(mDefaultSite);
                info.addObserver(site);
              }
            }
          }
        }
      }
    }

    mComponentInfoList = new ArrayList<>(mNumAutomata);
    for (final TRAutomatonProxy tr : trs) {
      final ComponentInfo info0 = new ComponentInfo(tr);
      mComponentInfoList.add(info0);
      if (!mSiteMap.isEmpty() && tr.getKind() == ComponentKind.SPEC) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final boolean det = rel.isDeterministic();
        for (final SiteInfo site : mSiteMap.values()) {
          if (det && !hasUnobservableTransition(tr, site)) {
            info0.addShadowingSite(site);
          } else {
            final ComponentInfo info1 = new ComponentInfo(tr, site);
            mComponentInfoList.add(info1);
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
    final int numComponents = mComponentInfoList.size();
    mTRAutomataExtended = new TRAutomatonProxy[numComponents];
    int a = 0;
    for (final ComponentInfo info : mComponentInfoList) {
      mTRAutomataExtended[a++] = info.getTRAutomaton();
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
    int siteEnd = eventInfo.getSiteGroupIndex(0);
    int d = eventInfo.findDisabling(decoded, 0, siteEnd);
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
          final SiteInfo site = mComponentInfoList.get(a).getSite();
          if (eventInfo.isControllable(site) && !autInfo.isEnabled(decoded)) {
            return true;
          }
        }
        // not coobservable
        // if (!handleUncontrollableState(e, a)) ...
        // TODO Counterexample ...
        setFailedResult(null);
        return false;
      }
    }
    int siteStart = 0;
    int groupIndex = 1;
    final int endGroupIndex = eventInfo.getNumberOfSiteIndices();
    if (eventInfo.hasObservingSpec()) {
      siteStart = siteEnd;
      siteEnd = eventInfo.getSiteGroupIndex(1);
      groupIndex++;
      d = eventInfo.findDisabling(decoded, siteStart, siteEnd);
    }
    if (d < 0) {
      eventInfo.createSuccessorStatesEncoded(encoded, 1, this);
    }
    while (groupIndex < endGroupIndex) {
      siteStart = siteEnd;
      siteEnd = eventInfo.getSiteGroupIndex(groupIndex);
      if (eventInfo.findDisabling(decoded, siteStart, siteEnd) < 0) {
        eventInfo.createSuccessorStatesEncoded(encoded, groupIndex, this);
      }
      groupIndex++;
    }
    return true;
  }

  private boolean isControlledByShadowingSite
    (final CoobservabilityEventInfo eventInfo,
     final AutomatonEventInfo autInfo)
  {
    final int a = autInfo.getAutomatonIndex();
    final ComponentInfo compInfo = mComponentInfoList.get(a);
    final List<SiteInfo> shadowingSites = compInfo.getShadowingSites();
    if (shadowingSites != null) {
      for (final SiteInfo site : shadowingSites) {
        if (eventInfo.isControllable(site)) {
          return true;
        }
      }
    }
    return false;
  }


  //#########################################################################
  //# Invocation
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
  //# Auxiliary Methods
  private SiteInfo getSite(final String name)
  {
    SiteInfo site = mSiteMap.get(name);
    if (site == null) {
      final int index = mSiteMap.size();
      site = new SiteInfo(name, index);
      mSiteMap.put(name, site);
    }
    return site;
  }

  private boolean hasUnobservableTransition(final TRAutomatonProxy tr,
                                            final SiteInfo site)
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


  //#########################################################################
  //# Inner Class SiteInfo
  private static class SiteInfo implements Comparable<SiteInfo>
  {
    //#######################################################################
    //# Constructor
    private SiteInfo(final String name, final int index)
    {
      mName = name;
    }

    //#######################################################################
    //# Simple Access
    private String getName()
    {
      return mName;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SiteInfo>
    @Override
    public int compareTo(final SiteInfo site)
    {
      return mIndex - site.mIndex;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mName;
    }

    //#######################################################################
    //# Instance Variables
    private final String mName;
    private int mIndex;
  }


  //#########################################################################
  //# Inner Class CoobservabilityEventInfo
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
    private boolean isControllable(final SiteInfo site)
    {
      return mControllers != null && mControllers.contains(site);
    }

    @SuppressWarnings("unused")
    private boolean isObservable()
    {
      return mObservers != null;
    }

    private boolean isObservable(final SiteInfo site)
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
    private void addController(final SiteInfo site)
    {
      if (mControllers == null) {
        mControllers = new THashSet<>();
      }
      mControllers.add(site);
    }

    private void addObserver(final SiteInfo site)
    {
      if (mObservers == null) {
        mObservers = new THashSet<>();
      }
      mObservers.add(site);
    }

    private void computeSiteIndices
      (final TRMonolithicCoobservabilityChecker verifier)
    {
      final TIntArrayList indices =
        new TIntArrayList(verifier.mSiteMap.size() + 1);
      int index = 0;
      int prevGroup = 0;
      SiteInfo prevSite = null;
      for (final AutomatonEventInfo info : getDisablingAutomata()) {
        final int a = info.getAutomatonIndex();
        final SiteInfo site = verifier.mComponentInfoList.get(a).getSite();
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
      collectSiteUpdates(false);
      collectSiteUpdates(true);
    }

    private void collectSiteUpdates(final boolean det)
    {
      int index = 0;
      // Index group 0 (reference) merged into group 1 (observers)
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

    // group == 0 : site == null : plant before spec
    // group == 1 : site observing event : order by site
    // group == 2 : site not observing event : order by site
    private int getSiteGroup(final SiteInfo site)
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

    //#######################################################################
    //# Instance Variables
    private final CoobservabilityAutomatonEventInfoCamparator mComparator;
    private Set<SiteInfo> mControllers;
    private Set<SiteInfo> mObservers;
    private boolean mHasObservingSpec;
    private int[] mSiteIndices;
    private AutomatonEventInfo[] mSiteUpdates;
  }


  //#########################################################################
  //# Inner Class ComponentInfo
  private static class ComponentInfo
  {
    //#######################################################################
    //# Constructors
    private ComponentInfo(final TRAutomatonProxy tr)
    {
      this(tr, null);
    }

    private ComponentInfo(final TRAutomatonProxy tr, final SiteInfo site)
    {
      mTRAutomaton = tr;
      mSite = site;
      mShadowingSites = null;
    }

    //#######################################################################
    //# Simple Access
    private TRAutomatonProxy getTRAutomaton()
    {
      return mTRAutomaton;
    }

    private SiteInfo getSite()
    {
      return mSite;
    }

    private List<SiteInfo> getShadowingSites()
    {
      return mShadowingSites;
    }

    private void addShadowingSite(final SiteInfo site)
    {
      if (mShadowingSites == null) {
        mShadowingSites = new LinkedList<>();
      }
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
    private final SiteInfo mSite;
    private List<SiteInfo> mShadowingSites;
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
      final SiteInfo site1 = mComponentInfoList.get(index1).getSite();
      final int group1 = mEventInfo.getSiteGroup(site1);
      final int index2 = info2.getAutomatonIndex();
      final SiteInfo site2 = mComponentInfoList.get(index2).getSite();
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
  private Map<String,SiteInfo> mSiteMap;
  private Map<EventProxy,CoobservabilityEventInfo> mCoobservabilityEventInfoMap;
  private List<ComponentInfo> mComponentInfoList;
  private TRAutomatonProxy[] mTRAutomataExtended;

}
