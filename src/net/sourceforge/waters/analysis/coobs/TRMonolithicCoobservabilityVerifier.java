//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>An implementation of the coobservability check algorithm.</P>
 *
 * @author Robi Malik
 */
public class TRMonolithicCoobservabilityVerifier
  extends AbstractTRMonolithicModelVerifier
  implements CoobservabilityChecker
{

  //#########################################################################
  //# Constructors
  public TRMonolithicCoobservabilityVerifier()
  {
    this(ControllabilityKindTranslator.getInstance());
  }

  public TRMonolithicCoobservabilityVerifier(final KindTranslator translator)
  {
    super(translator);
  }

  public TRMonolithicCoobservabilityVerifier(final ProductDESProxy model,
                                             final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      return setSatisfiedResult();
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
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
              new CoobservabilityEventInfo(event);
            mCoobservabilityEventInfoMap.put(event, info);
            final Map<String,String> attribs = event.getAttributes();
            for (final Map.Entry<String,String> entry : attribs.entrySet()) {
              final String attrib = entry.getKey();
              if (attrib.startsWith
                    (CoobservabilityAttributeFactory.CONTROLLABITY_KEY)) {
                final String value = entry.getValue();
                final SiteInfo site = getSite(value);
                info.addController(site);
              } else if (attrib.startsWith
                           (CoobservabilityAttributeFactory.OBSERVABITY_KEY)) {
                final String value = entry.getValue();
                final SiteInfo site = getSite(value);
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
          if (!det || hasUnobservableTransition(tr, site)) {
            final ComponentInfo info1 = new ComponentInfo(tr, site);
            mComponentInfoList.add(info1);
          }
        }
      }
    }

    return mCoobservabilityEventInfoMap;
  }

  @Override
  protected void setUpStateTupleEncoding()
    throws AnalysisAbortException, OverflowException
  {
    final int numComponents = mComponentInfoList.size();
    mTRAutomataExtended = new TRAutomatonProxy[numComponents];
    int a = 0;
    for (final ComponentInfo info : mComponentInfoList) {
      mTRAutomataExtended[a++] = info.getTRAutomaton();
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
  protected boolean expandState(final int[] encoded,
                                final int[] decoded,
                                final EventInfo event)
    throws AnalysisException
  {
    final AutomatonEventInfo disablingAut = event.findDisabling(decoded);
    final int a;
    if (disablingAut == null) {
      a = Integer.MAX_VALUE;
    } else if (disablingAut.isPlant()) {
      return true;
    } else {
      a = disablingAut.getAutomatonIndex();
    }
    if (a < mNumAutomata) {
      // enabled by plant, disabled by spec
      // check controllability condition
    } else {
      // enabled by plant and spec
      // expand transition for plant. spec, and observing sites; may yet fail
      final CoobservabilityEventInfo info = (CoobservabilityEventInfo) event;
      expandState(encoded, decoded, info, null);
    }
    // separately expand transitions for plant and each not observing site
    final CoobservabilityEventInfo info = (CoobservabilityEventInfo) event;
    for (final SiteInfo site : mSiteMap.values()) {
      if (!info.isObservable(site)) {
        expandState(encoded, decoded, info, site);
      }
    }
    return true;
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

  private void expandState(final int[] encoded,
                           final int[] decoded,
                           final CoobservabilityEventInfo event,
                           final SiteInfo site)
  {
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
    @SuppressWarnings("unused")
    private String getName()
    {
      return mName;
    }

    @SuppressWarnings("unused")
    private int getIndex()
    {
      return mIndex;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SiteInfo>
    @Override
    public int compareTo(final SiteInfo site)
    {
      return mIndex - site.mIndex;
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
    private CoobservabilityEventInfo(final EventProxy event)
    {
      super(event, false);
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
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

    //#######################################################################
    //# Initialisation
    private void addController(final SiteInfo site)
    {
      if (mControllers == null) {
        mControllers = new THashSet<>();
        setControllable();
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

    //#######################################################################
    //# Instance Variables
    private Set<SiteInfo> mControllers;
    private Set<SiteInfo> mObservers;
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
    }

    //#######################################################################
    //# Simple Access
    private TRAutomatonProxy getTRAutomaton()
    {
      return mTRAutomaton;
    }

    @SuppressWarnings("unused")
    private SiteInfo getSite()
    {
      return mSite;
    }

    //#######################################################################
    //# Instance Variables
    private final TRAutomatonProxy mTRAutomaton;
    private final SiteInfo mSite;
  }


  //#########################################################################
  //# Instance Variables
  private int mNumAutomata;
  private Map<String,SiteInfo> mSiteMap;
  private Map<EventProxy,CoobservabilityEventInfo> mCoobservabilityEventInfoMap;
  private List<ComponentInfo> mComponentInfoList;
  private TRAutomatonProxy[] mTRAutomataExtended;


  //#########################################################################
  //# Class Constants

}
