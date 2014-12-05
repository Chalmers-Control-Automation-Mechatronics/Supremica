//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   PreselectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AbstractAbortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public abstract class PreselectionHeuristic
  extends AbstractAbortable
{

  //#########################################################################
  //# Invocation
  abstract Collection<TRCandidate> collectCandidates
    (TRSubsystemInfo subsys)
    throws AnalysisException;


  //#########################################################################
  //# Auxiliary Methods
  void recordCandidate
    (final List<TRAutomatonProxy> automata,
     final TRSubsystemInfo subsys,
     final Map<List<TRAutomatonProxy>,TRCandidate> candidates)
    throws AnalysisException
  {
    checkAbort();
    if (automata.size() < subsys.getNumberOfAutomata()) {
      Collections.sort(automata);
      if (!candidates.containsKey(automata)) {
        final TRCandidate candidate = new TRCandidate(automata, subsys);
        final TRCandidate overflow = mOverflowCandidates.get(automata);
        if (overflow == null) {
          candidates.put(automata, candidate);
        } else if (!candidate.hasSameEventStatus(overflow)) {
          mOverflowCandidates.remove(automata);
          candidates.put(automata, candidate);
        }
      }
    }
  }

  void addOverflowCandidate(final TRCandidate candidate)
  {
    final List<TRAutomatonProxy> automata = candidate.getAutomata();
    mOverflowCandidates.put(automata, candidate);
  }


  //#########################################################################
  //# Debugging
  public String getName()
  {
    final String KEY = "PreselectionHeuristic";
    final String clazzName = getClass().getName();
    final int pos = clazzName.lastIndexOf(KEY);
    if (pos >= 0 && pos + KEY.length() < clazzName.length()) {
      return clazzName.substring(pos + KEY.length());
    } else {
      return ProxyTools.getShortClassName(this);
    }
  }

  @Override
  public String toString()
  {
    return getName();
  }

  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private final Map<List<TRAutomatonProxy>,TRCandidate> mOverflowCandidates =
    new HashMap<>();

}
