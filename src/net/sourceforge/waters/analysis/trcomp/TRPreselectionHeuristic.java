//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRPreselectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AbstractAbortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * <P>The interface for all preselection heuristics used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <I>preselection heuristic</I> implements the first step of candidate
 * selection, where several groups of automata to be considered for
 * composition are selected from a subsystem.</P>

 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public abstract class TRPreselectionHeuristic
  extends AbstractAbortable
{

  //#########################################################################
  //# Invocation
  /**
   * Sets the context in which the heuristic runs.
   * This method is called when a heuristic is registered with a model
   * analyser to pass that model analyser as a context into the heuristic.
   */
  public void setContext(final AbstractTRCompositionalAnalyzer analyzer)
  {
    mAutomataLimit = analyzer.getMonolithicAutomataLimit();
  }

  /**
   * Invokes this heuristic to produce a collection of candidates for
   * a given subsystem. This method is implemented differently by
   * subclasses.
   * @param  subsys    Subsystem containing the current set of automata.
   * @return Collection of candidates, each representing a group of automata
   *         from the subsystem to be considered for composition.
   */
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
    if (subsys.getNumberOfAutomata() - automata.size() + 1 >= mAutomataLimit) {
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

  boolean isOverflowCandidate(final TRCandidate candidate)
  {
    final List<TRAutomatonProxy> automata = candidate.getAutomata();
    final TRCandidate overflow = mOverflowCandidates.get(automata);
    if (overflow == null) {
      return false;
    } else if (overflow == candidate) {
      return true;
    } else {
      return !overflow.isComposedSuccessfully() ||
             overflow.hasSameEventStatus(candidate);
    }
  }

  void removeOverflowCandidatesContaining(final TRAutomatonProxy aut)
  {
    if (!mOverflowCandidates.isEmpty()) {
      final Set<TRAutomatonProxy> set = Collections.singleton(aut);
      removeOverflowCandidatesContaining(set);
    }
  }

  void removeOverflowCandidatesContaining(final TRCandidate candidate)
  {
    if (!mOverflowCandidates.isEmpty()) {
      final Set<TRAutomatonProxy> set = new THashSet<>(candidate.getAutomata());
      removeOverflowCandidatesContaining(set);
    }
  }

  void removeOverflowCandidatesContaining(final Set<TRAutomatonProxy> set)
  {
    final Iterator<List<TRAutomatonProxy>> iter =
      mOverflowCandidates.keySet().iterator();
    while (iter.hasNext()) {
      final List<TRAutomatonProxy> key = iter.next();
      for (final TRAutomatonProxy aut : key) {
        if (set.contains(aut)) {
          iter.remove();
          break;
        }
      }
    }
  }


  //#########################################################################
  //# Debugging
  public String getName()
  {
    final String KEY = "TRPreselectionHeuristic";
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


  //#########################################################################
  //# Data Members
  private final Map<List<TRAutomatonProxy>,TRCandidate> mOverflowCandidates =
    new HashMap<>();
  private int mAutomataLimit = 2;

}
