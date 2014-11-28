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
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;

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
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final Collection<EventProxy> events = aut.getEvents();
    return createEventEncoding(events, local, candidate);
  }

  protected EventEncoding createEventEncoding(final Collection<EventProxy> events,
                                              final Collection<EventProxy> local,
                                              final Candidate candidate)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    Collection<EventProxy> filter = getPropositions();
    if (filter == null) {
      filter = Collections.emptyList();
    }
    final EventEncoding enc = new EventEncoding();
    for (final EventProxy event : events) {
      if (local.contains(event)) {
        enc.addSilentEvent(event);
      } else if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (filter.contains(event)) {
          enc.addEvent(event, translator, 0);
        }
      } else {
        byte status = 0;
        if (mAnalyzer.isUsingSpecialEvents()) {
          final AbstractCompositionalModelAnalyzer.EventInfo info =
            mAnalyzer.getEventInfo(event);
          if (info.isOnlyNonSelfLoopCandidate(candidate)) {
            status = EventStatus.STATUS_SELFLOOP_ONLY;
          }
        }
        enc.addEvent(event, translator, status);
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
