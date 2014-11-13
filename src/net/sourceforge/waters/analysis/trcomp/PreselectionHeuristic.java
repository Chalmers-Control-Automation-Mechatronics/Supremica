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
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.log4j.Logger;


/**
 * @author Robi Malik
 */

public abstract class PreselectionHeuristic
{

  //#########################################################################
  //# Invocation
  public abstract Collection<TRCandidate> collectCandidates
    (TRSubsystemInfo subsys)
    throws AnalysisException;


  //#########################################################################
  //# Auxiliary Methods
  protected void recordCandidate
    (final List<TRAutomatonProxy> automata,
     final TRSubsystemInfo subsys,
     final Map<List<TRAutomatonProxy>,TRCandidate> candidates)
    throws OverflowException
  {
    Collections.sort(automata);
    if (!candidates.containsKey(automata)) {
      final TRCandidate candidate = new TRCandidate(automata, subsys);
      candidates.put(automata, candidate);
    }
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

}
