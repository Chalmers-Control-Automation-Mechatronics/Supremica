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

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.CoobservabilityVerificationResult;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>A structure to keep track of events and their controlling and observing
 * supervisor sites during a coobservability check.</P>
 *
 * <P>The coobservability signature contains records of all events in a model,
 * and knows which supervisor can control or observe the event. It also holds
 * a list of supervisor sites with the information of which events they control
 * and observe.</P>
 *
 * <P>Constructors can obtain this information from the event attributes of
 * a {@link ProductDESProxy}. Further methods are available to simplify the
 * signature by removing redundant supervisor sites. Events can also be
 * identified as <I>totally controllable</I>, meaning that there is no need
 * to check the coobservability condition for them.</P>
 *
 * @author Robi Malik
 * @see CoobservabilityChecker
 */

public class CoobservabilitySignature
{
  //###########################################################################
  //# Constructors
  public CoobservabilitySignature(final ProductDESProxy des,
                                  final KindTranslator translator)
  {
    this(des, translator, null);
  }

  public CoobservabilitySignature(final ProductDESProxy des,
                                  final KindTranslator translator,
                                  final String defaultSiteName)
  {
    final Collection<EventProxy> events = des.getEvents();
    final int numEvents = events.size();
    mSites = new LinkedList<>();
    mEventMap = new HashMap<>(numEvents);

    final Map<String,Site> siteMap = new HashMap<>();
    for (final EventProxy event : events) {
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.CONTROLLABLE || kind == EventKind.UNCONTROLLABLE) {
        final EventInfo info = new EventInfo(event);
        mEventMap.put(event, info);
        final Map<String,String> attribs = event.getAttributes();
        boolean controlled = false;
        boolean observed = false;
        for (final Map.Entry<String,String> entry : attribs.entrySet()) {
          final String attrib = entry.getKey();
          if (attrib.startsWith
            (CoobservabilityAttributeFactory.CONTROLLABITY_KEY)) {
            final String value = entry.getValue();
            final Site site = getSite(siteMap, value);
            site.addControlledEvent(event);
            info.addControllingSite(site);
            controlled = true;
          } else if (attrib.startsWith
            (CoobservabilityAttributeFactory.OBSERVABITY_KEY)) {
            final String value = entry.getValue();
            final Site site = getSite(siteMap, value);
            site.addObservedEvent(event);
            info.addObservingSite(site);
            observed = true;
          }
        }
        if (defaultSiteName != null) {
          if (!controlled && (kind == EventKind.CONTROLLABLE)) {
            final Site site = getSite(siteMap, defaultSiteName);
            site.addControlledEvent(event);
            info.addControllingSite(site);
          }
          if (!observed && event.isObservable()) {
            final Site site = getSite(siteMap, defaultSiteName);
            site.addObservedEvent(event);
            info.addObservingSite(site);
          }
        }
      }
    }
    final Site site = siteMap.get(defaultSiteName);
    if (site != null && site.getControlledEvents().isEmpty()) {
      removeEmptySiteFromEvents(site);
      mSites.remove(site);
    }
  }

  public CoobservabilitySignature(final CoobservabilitySignature sig)
  {
    this(sig, sig.mSites);
  }

  public CoobservabilitySignature(final CoobservabilitySignature sig,
                                  final SiteSet set)
  {
    this(sig, set.getSites());
    for (final Map.Entry<EventProxy,EventInfo> entry : mEventMap.entrySet()) {
      final EventProxy event = entry.getKey();
      if (!set.isCoveredEvent(event)) {
        final EventInfo info = entry.getValue();
        info.setTotallyControllable();
      }
    }
  }

  private CoobservabilitySignature(final CoobservabilitySignature sig,
                                   final Collection<Site> sites)
  {
    final int numSites = sites.size();
    final Map<Site,Site> cloneMap = new HashMap<>(numSites);
    mSites = new LinkedList<>();
    for (final Site site : sites) {
      final Site cloned = site.clone();
      cloneMap.put(site, cloned);
      mSites.add(cloned);
    }
    final int numEvents = sig.mEventMap.size();
    mEventMap = new HashMap<>(numEvents);
    for (final Map.Entry<EventProxy,EventInfo> entry : sig.mEventMap.entrySet()) {
      final EventProxy event = entry.getKey();
      final EventInfo info = entry.getValue();
      final EventInfo cloned = new EventInfo(info, cloneMap);
      mEventMap.put(event, cloned);
    }
  }

  public CoobservabilitySignature(final CoobservabilitySignature sig,
                                  final ProductDESProxy des)
  {
    final Collection<EventProxy> events = des.getEvents();
    final Collection<Site> sites = sig.getSites();
    final int numSites = sites.size();
    final Map<Site,Site> cloneMap = new HashMap<>(numSites);
    mSites = new LinkedList<>();
    for (final Site site : sites) {
      final Site cloned = site.restrict(events);
      cloneMap.put(site, cloned);
      mSites.add(cloned);
    }
    final int numEvents = Math.max(sig.mEventMap.size(), events.size());
    mEventMap = new HashMap<>(numEvents);
    for (final EventProxy event : events) {
      final EventInfo info = sig.mEventMap.get(event);
      if (info != null) {
        final EventInfo cloned = new EventInfo(info, cloneMap);
        mEventMap.put(event, cloned);
      }
    }
  }

  private Site getSite(final Map<String,Site> siteMap, final String name)
  {
    Site site = siteMap.get(name);
    if (site == null) {
      site = new SingletonSite(name);
      siteMap.put(name, site);
      mSites.add(site);
    }
    return site;
  }


  //###########################################################################
  //# Simple Access
  /**
   * Returns a list of all supervisor sites in the signature, in the order
   * in which they were discovered.
   */
  public List<Site> getSites()
  {
    return mSites;
  }

  /**
   * Returns whether the given event was determined to be <I>totally
   * controllable</I>. It is not necessary to check the coobservability
   * condition for totally controllable events, but their observability
   * may still be relevant.
   */
  public boolean isTotallyControllable(final EventProxy event)
  {
    final EventInfo info = mEventMap.get(event);
    if (info == null) {
      assert false : "Unkown event loopup of " + event.getName() + "!";
      return false;
    } else {
      return info.isTotallyControllable();
    }
  }

  /**
   * Returns a list of all known sites controlling the given event.
   * @return List of sites or <CODE>null</CODE> if the event cannot be found.
   *         The returned list may be empty for totally controllable events
   *         as their controlling sites may have been removed from the
   *         signature.
   * @see #isTotallyControllable(EventProxy)
   */
  public List<Site> getControllingSites(final EventProxy event)
  {
    final EventInfo info = mEventMap.get(event);
    if (info == null) {
      return null;
    } else {
      return info.getControllingSites();
    }
  }

  /**
   * Returns a list of all known sites observing the given event.
   * @return List of sites or <CODE>null</CODE> if the event cannot be found.
   */
  public List<Site> getObservingSites(final EventProxy event)
  {
    final EventInfo info = mEventMap.get(event);
    if (info == null) {
      return null;
    } else {
      return info.getObservingSites();
    }
  }


  //###########################################################################
  //# Stats Gathering
  public void addStatistics(final CoobservabilityVerificationResult result)
  {
    result.setTotalNumberOfEvents(mEventMap.size());
    result.setNumberOfSites(mSites.size());
  }

  //###########################################################################
  //# Simplification
  /**
   * <P>Simplifies the signature by removing events that do not need to be
   * checked for coobservability.</P>
   * <UL>
   * <LI>Events that are not used in any automaton are removed from the event
   *     map and from all sites.</LI>
   * <LI>Control of events always disabled in at least one plant is removed
   *     from from all sites.</LI>
   * <LI>Control of events always enabled in all specifications is removed
   *     from all sites.</LI>
   * <LI>If all automata are deterministic, then sites are identified as
   *     <I>all-observing</I> if they observe all events that are not
   *     selfloop-only in all automata. Control of events controlled by an
   *     all-observing site (<I>totally controllable events</I>) is removed
   *     from all sites</LI>
   * <LI>Sites that do not control any events are removed.</LI>
   * <UL>
   */
  public void filter(final TRAutomatonProxy[] trs,
                     final KindTranslator translator)
  {
    final Iterator<Map.Entry<EventProxy,EventInfo>> mapIter =
      mEventMap.entrySet().iterator();
    while (mapIter.hasNext()) {
      final Map.Entry<EventProxy,EventInfo> entry = mapIter.next();
      final EventProxy event = entry.getKey();
      boolean used = false;
      for (final TRAutomatonProxy tr : trs) {
        final EventEncoding enc = tr.getEventEncoding();
        final int e = enc.getEventCode(event);
        if (e >= 0 &&
            (enc.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
          used = true;
          break;
        }
      }
      final EventInfo info = entry.getValue();
      if (used) {
        info.resetFilters();
      } else {
        info.removeFromSites();
        mapIter.remove();
      }
    }

    int numNeedsChecking = mEventMap.size();
    boolean removedFromSites = false;
    boolean det = true;
    for (final TRAutomatonProxy tr : trs) {
      final EventEncoding enc = tr.getEventEncoding();
      final int numEvents = enc.getNumberOfProperEvents();
      final int[] count = new int[numEvents];
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      det &= rel.isDeterministic();
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int numStates = rel.getNumberOfStates();
      int numReachable = 0;
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s)) {
          numReachable++;
          int prev = -1;
          iter.resetState(s);
          while (iter.advance()) {
            final int e = iter.getCurrentEvent();
            if (e != prev) {
              count[e]++;
              prev = e;
            }
          }
        }
      }
      final ComponentKind kind = translator.getComponentKind(tr);
      for (int e = 0; e < numEvents; e++) {
        final boolean removable =
          kind == ComponentKind.PLANT ? count[e] == 0 : count[e] < numReachable;
        if (removable) {
          final EventProxy event = enc.getProperEvent(e);
          final EventInfo info = event == null ? null : mEventMap.get(event);
          if (info != null) {
            if (kind == ComponentKind.PLANT) {
              info.setTotallyControllable();
              numNeedsChecking--;
              removedFromSites = true;
            } else if (info.setNeedsChecking()) {
              numNeedsChecking--;
            }
            if (numNeedsChecking == 0) {
              break;
            }
          }
        }
      }
    }
    if (numNeedsChecking > 0) {
      final Iterator<EventInfo> iter = mEventMap.values().iterator();
      while (iter.hasNext()) {
        final EventInfo info = iter.next();
        if (!info.needsChecking()) {
          info.setTotallyControllable();
          removedFromSites = true;
        }
      }
    }
    if (removedFromSites) {
      removeEmptySites();
    }

    if (det) {
      int numNeedsObserving = mEventMap.size();
      for (final TRAutomatonProxy tr : trs) {
        final EventEncoding enc = tr.getEventEncoding();
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        if (numNeedsObserving > 0) {
          final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
          while (iter.advance()) {
            if (iter.getCurrentFromState() != iter.getCurrentToState()) {
              final int e = iter.getCurrentEvent();
              final EventProxy event = enc.getProperEvent(e);
              final EventInfo info = event == null ? null : mEventMap.get(event);
              if (info != null && info.setNeedsObserving()) {
                numNeedsObserving--;
                if (numNeedsObserving == 0) {
                  break;
                }
              }
            }
          }
        }
      }
      if (numNeedsObserving > 0) {
        final Iterator<Site> iter = mSites.iterator();
        while (iter.hasNext()) {
          final Site site = iter.next();
          if (isAllObserving(site)) {
            setTotalControl(site);
            iter.remove();
          }
        }
        removeEmptySites();
      }
    }
  }

  /**
   * Merges sites that observe the same events.
   * Any sites that observe exactly the same events are replaced by a single
   * site that observes those events and controls all events controlled by
   * at least one of the merged sites.
   */
  public void merge()
  {
    final int numSites = mSites.size();
    final Site[] sites = new Site[numSites];
    mSites.toArray(sites);
    mSites = new LinkedList<>();
    Map<Site,Site> replacements = null;
    for (int i = 0; i < numSites; i++) {
      Site site1 = sites[i];
      if (site1 != null) {
        final Set<EventProxy> events = site1.getObservedEvents();
        for (int j = i + 1; j < numSites; j++) {
          final Site site2 = sites[j];
          if (site2 != null && site2.getObservedEvents().equals(events)) {
            final Site site = site1.merge(site2);
            sites[j] = null;
            if (replacements == null) {
              replacements = new HashMap<>(numSites);
            }
            replacements.put(site1, site);
            replacements.put(site2, site);
            site1 = site;
          }
        }
        mSites.add(site1);
      }
    }
    if (replacements != null) {
      for (final EventInfo info : mEventMap.values()) {
        info.replaceSites(replacements);
      }
    }
  }

  /**
   * Removes subsumed sites.
   * If the set events controlled and observed are both contained in the
   * sets of events controlled and observed by another site, then the site
   * with the smaller event sets is <I>subsumed</I> by the site with the larger
   * event sets. This method removes such subsumed sites by merging them into
   * a site that subsumes them.
   */
  public void subsume()
  {
    final int numSites = mSites.size();
    final Site[] sites = new Site[numSites];
    mSites.toArray(sites);
    Map<Site,Site> replacements = null;
    for (int i = 0; i < numSites; i++) {
      Site site1 = sites[i];
      if (site1 != null) {
        for (int j = 0; j < numSites; j++) {
          final Site site2 = sites[j];
          if (i != j && site2 != null && site1.subsumes(site2)) {
            final Site site = site1.merge(site2);
            sites[j] = null;
            if (replacements == null) {
              replacements = new HashMap<>(numSites);
            }
            replacements.put(site1, site);
            replacements.put(site2, site);
            site1 = site;
          }
        }
        sites[i] = site1;
      }
    }
    if (replacements != null) {
      mSites = new LinkedList<>();
      for (final Site site : sites) {
        if (site != null) {
          mSites.add(site);
        }
      }
      for (final EventInfo info : mEventMap.values()) {
        info.replaceSites(replacements);
      }
    }
  }

  public SiteSet findMinimalSiteSet()
  {
    final Map<Set<Site>,SiteSet> candidates = new HashMap<>();
    int bestNumSites = Integer.MAX_VALUE;
    for (final EventInfo info : mEventMap.values()) {
      if (!info.isTotallyControllable()) {
        final Collection<Site> sites = info.getControllingSites();
        final int numSites = sites.size();
        if (numSites < bestNumSites) {
          candidates.clear();
          bestNumSites = numSites;
        }
        if (numSites == bestNumSites) {
          final Set<Site> set = new TreeSet<>(sites);
          final SiteSet candidate = candidates.get(set);
          if (candidate == null) {
            final SiteSet newCandidate = new SiteSet(set, info);
            candidates.put(set, newCandidate);
          } else {
            candidate.addEvent(info);
          }
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    } else {
      return Collections.min(candidates.values());
    }
  }

  public void removeCoveredEvents(final SiteSet set)
  {
    for (final EventProxy event : set.getCoveredEvents()) {
      final EventInfo info = mEventMap.get(event);
      info.setTotallyControllable();
    }
    removeEmptySites();
  }


  //###########################################################################
  //# Auxiliary Methods
  private boolean isAllObserving(final Site site)
  {
    for (final Map.Entry<EventProxy,EventInfo> entry : mEventMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.needsObserving()) {
        final EventProxy event = entry.getKey();
        if (!site.isObservedEvent(event)) {
          return false;
        }
      }
    }
    return true;
  }

  private void setTotalControl(final Site site)
  {
    final List<EventProxy> events = new ArrayList<>(site.getControlledEvents());
    for (final EventProxy event : events) {
      final EventInfo info = mEventMap.get(event);
      info.setTotallyControllable();
    }
  }

  private void removeEmptySites()
  {
    final Iterator<Site> iter = mSites.iterator();
    while (iter.hasNext()) {
      final Site site = iter.next();
      if (site.getControlledEvents().isEmpty()) {
        removeEmptySiteFromEvents(site);
        iter.remove();
      }
    }
  }

  private void removeEmptySiteFromEvents(final Site site)
  {
    for (final EventProxy event : site.getObservedEvents()) {
      final EventInfo info = mEventMap.get(event);
      info.removeObservingSite(site);
    }
  }

  private static List<Site> cloneSiteList(final List<Site> list,
                                          final Map<Site,Site> cloneMap)
  {
    if (list == null) {
      return null;
    } else {
      final List<Site> result = new LinkedList<>();
      for (final Site site : list) {
        final Site cloned = cloneMap.get(site);
        if (cloned != null) {
          result.add(cloned);
        }
      }
      return result;
    }
  }

  private static void replaceSites(final List<Site> list,
                                   final Map<Site,Site> replacements)
  {
    if (list != null) {
      final ListIterator<Site> iter = list.listIterator();
      while (iter.hasNext()) {
        final Site site = iter.next();
        final Site replacement = replacements.get(site);
        if (replacement != null) {
          iter.set(replacement);
        }
      }
    }
  }


  //###########################################################################
  //# Inner Class Site
  public static abstract class Site
    implements Cloneable, Comparable<Site>
  {
    //#########################################################################
    //# Constructor
    private Site()
    {
      mControlledEvents = new THashSet<>();
      mObservedEvents = new THashSet<>();
    }

    private Site(final Site site)
    {
      mControlledEvents = new THashSet<>(site.mControlledEvents);
      mObservedEvents = new THashSet<>(site.mObservedEvents);
    }

    private Site(final Site site, final Collection<EventProxy> events)
    {
      final int numEvents = events.size();
      final int numControlled = Math.max(numEvents, site.mControlledEvents.size());
      mControlledEvents = new THashSet<>(numControlled);
      for (final EventProxy event : events) {
        if (site.mControlledEvents.contains(event)) {
          mControlledEvents.add(event);
        }
      }
      final int numObserved = Math.max(numEvents, site.mObservedEvents.size());
      mObservedEvents = new THashSet<>(numObserved);
      for (final EventProxy event : events) {
        if (site.mObservedEvents.contains(event)) {
          mObservedEvents.add(event);
        }
      }
    }

    //#########################################################################
    //# Simple Access
    public String getName()
    {
      final StringBuilder builder = new StringBuilder();
      appendName(builder, false);
      return builder.toString();
    }

    public Set<EventProxy> getControlledEvents()
    {
      return mControlledEvents;
    }

    public Set<EventProxy> getObservedEvents()
    {
      return mObservedEvents;
    }

    public boolean isControlledEvent(final EventProxy event)
    {
      return mControlledEvents.contains(event);
    }

    public boolean isObservedEvent(final EventProxy event)
    {
      return mObservedEvents.contains(event);
    }

    boolean addControlledEvent(final EventProxy event)
    {
      return mControlledEvents.add(event);
    }

    boolean addObservedEvent(final EventProxy event)
    {
      return mObservedEvents.add(event);
    }

    private boolean removeControlledEvent(final EventProxy event)
    {
      return mControlledEvents.remove(event);
    }

    private boolean removeObservedEvent(final EventProxy event)
    {
      return mObservedEvents.remove(event);
    }

    private boolean subsumes(final Site site)
    {
      return
        mControlledEvents.containsAll(site.mControlledEvents) &&
        mObservedEvents.containsAll(site.mObservedEvents);
    }

    //#########################################################################
    //# Abstract Methods
    @Override
    protected abstract Site clone();

    protected abstract Site restrict(Collection<EventProxy> events);

    abstract void appendName(StringBuilder builder, boolean braced);

    abstract Collection<SingletonSite> getSingletonMembers();

    abstract MergedSite merge(Site site);

    //#########################################################################
    //# Interface java.lang.Comparable<Site>
    @Override
    public int compareTo(final Site site)
    {
      final String name1 = getName();
      final String name2 = site.getName();
      return name1.compareTo(name2);
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
    private final Set<EventProxy> mControlledEvents;
    private final Set<EventProxy> mObservedEvents;
  }


  //###########################################################################
  //# Inner Class SingletonSite
  private static class SingletonSite extends Site
  {
    //#########################################################################
    //# Constructor
    private SingletonSite(final String name)
    {
      mName = name;
    }

    private SingletonSite(final SingletonSite site)
    {
      super(site);
      mName = site.mName;
    }

    private SingletonSite(final SingletonSite site,
                          final Collection<EventProxy> events)
    {
      super(site, events);
      mName = site.mName;
    }

    //#########################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.coobs.CoobservabilitySignature.Site
    @Override
    protected SingletonSite clone()
    {
      return new SingletonSite(this);
    }

    @Override
    protected SingletonSite restrict(final Collection<EventProxy> events)
    {
      return new SingletonSite(this, events);
    }

    @Override
    public String getName()
    {
      return mName;
    }

    @Override
    void appendName(final StringBuilder builder, final boolean braced)
    {
      builder.append(mName);
    }

    @Override
    Collection<SingletonSite> getSingletonMembers()
    {
      return Collections.singletonList(this);
    }

    @Override
    MergedSite merge(final Site site)
    {
      return new MergedSite(this, site);
    }

    //#########################################################################
    //# Data Members
    private final String mName;

  }


  //###########################################################################
  //# Inner Class MergedSite
  private static class MergedSite extends Site
  {
    //#########################################################################
    //# Constructor
    private MergedSite(final Site... sites)
    {
      mMembers = new LinkedList<>();
      for (final Site site : sites) {
        include(site);
      }
    }

    private MergedSite(final Site site, final Collection<EventProxy> events)
    {
      super(site, events);
      mMembers = new LinkedList<>();
    }

    //#########################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.coobs.CoobservabilitySignature.Site
    @Override
    public MergedSite clone()
    {
      final MergedSite site = new MergedSite();
      for (final SingletonSite member : mMembers) {
        final SingletonSite cloned = member.clone();
        site.include(cloned);
      }
      return site;
    }

    @Override
    public MergedSite restrict(final Collection<EventProxy> events)
    {
      final MergedSite site = new MergedSite(this, events);
      for (final SingletonSite member : mMembers) {
        final SingletonSite cloned = member.restrict(events);
        site.include(cloned);
      }
      return site;
    }

    @Override
    void appendName(final StringBuilder builder, final boolean braced)
    {
      if (braced) {
        builder.append(']');
      }
      boolean first = true;
      for (final SingletonSite member : mMembers) {
        if (first) {
          first = false;
        } else {
          builder.append(',');
        }
        member.appendName(builder, braced);
      }
      if (braced) {
        builder.append(']');
      }
    }

    @Override
    public Collection<SingletonSite> getSingletonMembers()
    {
      return mMembers;
    }

    @Override
    public MergedSite merge(final Site site)
    {
      include(site);
      return this;
    }

    //#########################################################################
    //# Auxiliary Methods
    private void include(final Site site)
    {
      for (final SingletonSite member : site.getSingletonMembers()) {
        mMembers.add(member);
      }
      for (final EventProxy event : site.getControlledEvents()) {
        addControlledEvent(event);
      }
      for (final EventProxy event : site.getObservedEvents()) {
        addObservedEvent(event);
      }
    }

    //#########################################################################
    //# Data Members
    private final Collection<SingletonSite> mMembers;
  }


  //###########################################################################
  //# Inner Class SiteSet
  public static class SiteSet implements Comparable<SiteSet>
  {
    //#########################################################################
    //# Constructor
    private SiteSet(final Set<Site> sites, final EventInfo info)
    {
      mSites = sites;
      mEvents = new THashSet<>();
      addEvent(info);
   }

    //#########################################################################
    //# Simple Access
    public Set<Site> getSites()
    {
      return mSites;
    }

    public Set<EventProxy> getCoveredEvents()
    {
      return mEvents;
    }

    public boolean isCoveredEvent(final EventProxy event)
    {
      return mEvents.contains(event);
    }

    private void addEvent(final EventInfo info)
    {
      mEvents.add(info.getEvent());
    }

    //#########################################################################
    //# Interface java.lang.Comparable<SiteSet>
    @Override
    public int compareTo(final SiteSet set)
    {
      int result = mSites.size() - set.mSites.size();
      if (result != 0) {
        return result;
      }
      result = mEvents.size() - set.mEvents.size();
      if (result != 0) {
        return result;
      }
      final Iterator<Site> iter1 = mSites.iterator();
      final Iterator<Site> iter2 = set.mSites.iterator();
      while (iter1.hasNext()) {
        final Site site1 = iter1.next();
        final Site site2 = iter2.next();
        result = site1.compareTo(site2);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }

    //#########################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append('{');
      boolean first = true;
      for (final Site site : mSites) {
        if (first) {
          first = false;
        } else {
          builder.append(',');
        }
        site.appendName(builder, true);
      }
      builder.append('}');
      return builder.toString();
    }

    //#########################################################################
    //# Data Members
    private final Set<Site> mSites;
    private final Set<EventProxy> mEvents;
  }


  //###########################################################################
  //# Inner Class EventInfo
  private class EventInfo
  {
    //#########################################################################
    //# Constructor
    private EventInfo(final EventProxy event)
    {
      mEvent = event;
      mControllingSites = mObservingSites = null;
      mTotallyControllable = false;
      mNeedsChecking = mNeedsObserving = true;
    }

    private EventInfo(final EventInfo info, final Map<Site,Site> cloneMap)
    {
      mEvent = info.mEvent;
      mControllingSites = cloneSiteList(info.mControllingSites, cloneMap);
      mObservingSites = cloneSiteList(info.mObservingSites, cloneMap);
      mTotallyControllable = info.mTotallyControllable;
      mNeedsChecking = info.mNeedsChecking;
      mNeedsObserving = info.mNeedsObserving;
    }

    //#########################################################################
    //# Simple Access
    private EventProxy getEvent()
    {
      return mEvent;
    }

    private List<Site> getControllingSites()
    {
      if (mControllingSites == null) {
        return Collections.emptyList();
      } else {
        return mControllingSites;
      }
    }

    private void addControllingSite(final Site site)
    {
      if (mControllingSites == null) {
        mControllingSites = new LinkedList<>();
      }
      mControllingSites.add(site);
    }

    private List<Site> getObservingSites()
    {
      if (mObservingSites == null) {
        return Collections.emptyList();
      } else {
        return mObservingSites;
      }
    }

    private void addObservingSite(final Site site)
    {
      if (mObservingSites == null) {
        mObservingSites = new LinkedList<>();
      }
      mObservingSites.add(site);
    }

    private void removeObservingSite(final Site site)
    {
      if (mObservingSites != null) {
        mObservingSites.remove(site);
        if (mObservingSites.isEmpty()) {
          mObservingSites = null;
        }
      }
    }

    private boolean isTotallyControllable()
    {
      return mTotallyControllable;
    }

    //#########################################################################
    //# Filtering
    private void resetFilters()
    {
      mNeedsChecking = mNeedsObserving = false;
    }

    private boolean needsChecking()
    {
      return mNeedsChecking;
    }

    private boolean needsObserving()
    {
      return mNeedsObserving;
    }

    private boolean setNeedsChecking()
    {
      if (mNeedsChecking) {
        return false;
      } else {
        mNeedsChecking = true;
        return true;
      }
    }

    private boolean setNeedsObserving()
    {
      if (mNeedsObserving) {
        return false;
      } else {
        mNeedsObserving = true;
        return true;
      }
    }

    private void setTotallyControllable()
    {
      if (mControllingSites != null) {
        for (final Site site : mControllingSites) {
          site.removeControlledEvent(mEvent);
        }
        mControllingSites = null;
      }
      mTotallyControllable = true;
    }

    private void removeFromSites()
    {
      if (mControllingSites != null) {
        for (final Site site : mControllingSites) {
          site.removeControlledEvent(mEvent);
        }
      }
      if (mObservingSites != null) {
        for (final Site site : mObservingSites) {
          site.removeObservedEvent(mEvent);
        }
      }
    }

    private void replaceSites(final Map<Site,Site> replacements)
    {
      CoobservabilitySignature.replaceSites(mControllingSites, replacements);
      CoobservabilitySignature.replaceSites(mObservingSites, replacements);
    }

    //#########################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mEvent.getName();
    }

    //#########################################################################
    //# Data Members
    private final EventProxy mEvent;
    private List<Site> mControllingSites;
    private List<Site> mObservingSites;
    private boolean mTotallyControllable;
    private boolean mNeedsChecking;
    private boolean mNeedsObserving;
  }


  //###########################################################################
  //# Data Members
  private List<Site> mSites;
  private final Map<EventProxy,EventInfo> mEventMap;

}
