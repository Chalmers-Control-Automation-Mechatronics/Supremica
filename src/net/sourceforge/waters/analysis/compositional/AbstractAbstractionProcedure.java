//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractAbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;

/**
 * A default implementation of the {@link AbstractionProcedure} interface.
 * This class makes it easy to access the model analyser
 * ({@link AbstractCompositionalModelAnalyzer}), but provides no additional
 * functionality.
 *
 * @author Robi Malik
 */
abstract class AbstractAbstractionProcedure implements AbstractionProcedure
{

  //#########################################################################
  //# Constructors
  AbstractAbstractionProcedure(final AbstractCompositionalModelAnalyzer analyzer)
  {
    mAnalyzer = analyzer;
  }


  //#########################################################################
  //# Simple Access
  AbstractCompositionalModelAnalyzer getAnalyzer()
  {
    return mAnalyzer;
  }

  CompositionalAnalysisResult getAnalysisResult()
  {
    return mAnalyzer.getAnalysisResult();
  }

  ProductDESProxyFactory getFactory()
  {
    return mAnalyzer.getFactory();
  }

  KindTranslator getKindTranslator()
  {
    return mAnalyzer.getKindTranslator();
  }

  Collection<EventProxy> getPropositions()
  {
    return mAnalyzer.getPropositions();
  }

  EventProxy getUsedDefaultMarking()
  {
    return mAnalyzer.getUsedDefaultMarking();
  }

  EventProxy getUsedPreconditionMarking()
  {
    return mAnalyzer.getUsedPreconditionMarking();
  }


  //#########################################################################
  //# Auxiliary Methods
  protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final EventProxy tau,
                                              final Candidate candidate)
  {
    final Collection<EventProxy> events = aut.getEvents();
    return createEventEncoding(events, tau, candidate);
  }

  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final EventProxy tau,
                                              final Candidate candidate)
  {
    final KindTranslator translator = getKindTranslator();
    Collection<EventProxy> filter = getPropositions();
    if (filter == null) {
      filter = Collections.emptyList();
    }
    final EventEncoding enc =
      new EventEncoding(events, translator, tau, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    if (mAnalyzer.isUsingSpecialEvents()) {
      final int numEvents = enc.getNumberOfProperEvents();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final EventProxy event = enc.getProperEvent(e);
        final AbstractCompositionalModelAnalyzer.EventInfo info =
          mAnalyzer.getEventInfo(event);
        if (info.isOnlyNonSelfLoopCandidate(candidate)) {
          final byte status = enc.getProperEventStatus(e);
          enc.setProperEventStatus
            (e, status | EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP);
        }
      }
    }
    return enc;
  }


  //#########################################################################
  //# Logging
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final AbstractCompositionalModelAnalyzer mAnalyzer;

}
