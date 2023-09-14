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

package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>A bridge to encapsulate various aspects of a component selection
 * heuristic used by the {@link AbstractModularVerifier} and its
 * subclasses.</P>
 *
 * <P>The heuristic evaluator combines the heuristic preference ({@link
 * net.sourceforge.waters.analysis.modular.HeuristicFactory.Preference})
 * and heuristic method ({@link
 * net.sourceforge.waters.analysis.modular.HeuristicFactory.Method}) that
 * defines the component selection strategy with a {@link
 * HeuristicTraceChecker} that determines whether a component is deemed to
 * accept a counterexample based on the property being checked.</P>
 *
 * @see HeuristicFactory
 *
 * @author Robi Malik, Simon Ware
 */

class HeuristicEvaluator
  implements Comparator<AutomatonProxy>
{

  //#########################################################################
  //# Constructor
  HeuristicEvaluator(final KindTranslator translator,
                     final HeuristicFactory.Preference pref,
                     final HeuristicValueProvider heuristics,
                     final HeuristicTraceChecker checker)
  {
    this(translator, pref, Collections.singletonList(heuristics), checker);
  }

  HeuristicEvaluator(final KindTranslator translator,
                     final HeuristicFactory.Preference pref,
                     final List<HeuristicValueProvider> heuristics,
                     final HeuristicTraceChecker checker)
  {
    mKindTranslator = translator;
    mPreference = pref;
    mHeuristics = heuristics;
    mTraceChecker = checker;
    mTraceFinders = new HashMap<AutomatonProxy,TraceFinder>();
  }


  //#########################################################################
  //# Simple Access
  KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  HeuristicFactory.Preference getPreference()
  {
    return mPreference;
  }

  String getName()
  {
    final HeuristicValueProvider provider = mHeuristics.get(0);
    return provider.getName();
  }

  TraceFinder getTraceFinder(final AutomatonProxy aut,
                             final ComponentKind kind)
  {
    final TraceFinder finder = getTraceFinder(aut);
    finder.setComponentKind(kind);
    return finder;
  }

  TraceFinder getTraceFinder(final AutomatonProxy aut)
  {
    TraceFinder finder = mTraceFinders.get(aut);
    if (finder == null) {
      finder = new TraceFinder(aut, mKindTranslator);
      mTraceFinders.put(aut, finder);
    }
    return finder;
  }


  //#########################################################################
  //# Interface java.util.Comparator<AutomatonProxy>
  @Override
  public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
  {
    for (final HeuristicValueProvider provider : mHeuristics) {
      final float value1 = provider.getHeuristicValue(this, aut1);
      final float value2 = provider.getHeuristicValue(this, aut2);
      if (value1 < value2) {
        return -1;
      } else if (value1 > value2) {
        return 1;
      }
    }
    final String name1 = aut1.getName();
    final String name2 = aut2.getName();
    return name1.compareTo(name2);
  }


  //#########################################################################
  //# Invocation
  <A extends AutomatonProxy>
  Collection<A> collectNonAccepting(final ProductDESProxy des,
                                    final CounterExampleProxy counter,
                                    final Collection<A> realPlants,
                                    final Collection<A> specPlants,
                                    final Collection<A> specs)
  {
    final HeuristicValueProvider provider = mHeuristics.get(0);
    switch (provider.getCollectionMode()) {
    case FIRST:
      return collectFirstNonAccepting(counter, realPlants, specPlants, specs);
    case BEST:
      return collectBestNonAccepting(des, counter, realPlants, specPlants, specs);
    case ALL:
      return collectAllNonAccepting(counter, realPlants, specPlants, specs);
    default:
      assert false;
      return Collections.emptyList();
    }
  }

  <A extends AutomatonProxy>
  Collection<A> collectFirstNonAccepting(final CounterExampleProxy counter,
                                         final Collection<A> realPlants,
                                         final Collection<A> specPlants,
                                         final Collection<A> specs)
  {
    final A aut = findFirstNonAccepting(counter, realPlants, specPlants, specs);
    if (aut == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(aut);
    }
  }

  <A extends AutomatonProxy>
  A findFirstNonAccepting(final CounterExampleProxy counter,
                          final Collection<A> realPlants,
                          final Collection<A> specPlants,
                          final Collection<A> specs)
  {
    A aut = findFirstNonAccepting(counter, realPlants, ComponentKind.PLANT);
    if (aut == null) {
      aut = findFirstNonAccepting(counter, specPlants, ComponentKind.PLANT);
    }
    if (aut == null) {
      aut = findFirstNonAccepting(counter, specs, ComponentKind.PLANT);
    }
    return aut;
  }

  <A extends AutomatonProxy>
  Collection<A> collectBestNonAccepting(final ProductDESProxy des,
                                        final CounterExampleProxy counter,
                                        final Collection<A> realPlants,
                                        final Collection<A> specPlants,
                                        final Collection<A> specs)
  {
    for (final HeuristicValueProvider provider : mHeuristics) {
      provider.setContext(des, mKindTranslator, counter,
                          realPlants, specPlants, specs);
    }
    final A best = findBestNonAccepting(counter, realPlants, specPlants, specs);
    if (best == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(best);
    }
  }

  <A extends AutomatonProxy>
  A findBestNonAccepting(final CounterExampleProxy counter,
                         final Collection<A> realPlants,
                         final Collection<A> specPlants,
                         final Collection<A> specs)
  {
    A best = null;
    switch (mPreference) {
    case NOPREF:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      break;
    case PREFER_PLANT:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      if (best == null) {
        best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      }
      break;
    case PREFER_REAL_PLANT:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      if (best == null) {
        best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      }
      if (best == null) {
        best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      }
      break;
    }
    return best;
  }

  <A extends AutomatonProxy>
  Collection<A> collectAllNonAccepting(final CounterExampleProxy counter,
                                       final Collection<A> realPlants,
                                       final Collection<A> specPlants,
                                       final Collection<A> specs)
  {
    final Collection<A> output = new LinkedList<>();
    switch (mPreference) {
    case NOPREF:
      collectAllNonAccepting(counter, realPlants, output, ComponentKind.PLANT);
      collectAllNonAccepting(counter, specPlants, output, ComponentKind.PLANT);
      collectAllNonAccepting(counter, specs, output, ComponentKind.SPEC);
      break;
    case PREFER_PLANT:
      collectAllNonAccepting(counter, realPlants, output, ComponentKind.PLANT);
      collectAllNonAccepting(counter, specPlants, output, ComponentKind.PLANT);
      if (!output.isEmpty()) {
        collectAllNonAccepting(counter, specs, output, ComponentKind.SPEC);
      }
      break;
    case PREFER_REAL_PLANT:
      collectAllNonAccepting(counter, realPlants, output, ComponentKind.PLANT);
      if (!output.isEmpty()) {
        collectAllNonAccepting(counter, specPlants, output, ComponentKind.PLANT);
      }
      if (!output.isEmpty()) {
        collectAllNonAccepting(counter, specs, output, ComponentKind.SPEC);
      }
      break;
    }
    return output;
  }


  //#########################################################################
  //# Auxiliary Methods
  private <A extends AutomatonProxy>
  A findFirstNonAccepting(final CounterExampleProxy counter,
                          final Collection<A> input,
                          final ComponentKind kind)
  {
    for (final A aut : input) {
      if (!accepts(aut, kind, counter)) {
        return aut;
      }
    }
    return null;
  }

  private <A extends AutomatonProxy>
  A findBestNonAccepting(final CounterExampleProxy counter,
                         final Collection<A> input,
                         final ComponentKind kind)
  {
    return findBestNonAccepting(counter, input, kind, null);
  }

  private <A extends AutomatonProxy>
  A findBestNonAccepting(final CounterExampleProxy counter,
                         final Collection<A> input,
                         final ComponentKind kind,
                         A best)
  {
    for (final A aut : input) {
      if (!accepts(aut, kind, counter)) {
        if (best == null || compare(aut, best) < 0) {
          best = aut;
        }
      }
    }
    return best;
  }

  private <A extends AutomatonProxy>
  void collectAllNonAccepting(final CounterExampleProxy counter,
                              final Collection<A> input,
                              final Collection<A> output,
                              final ComponentKind kind)
  {
    for (final A aut : input) {
      if (!accepts(aut, kind, counter)) {
        output.add(aut);
      }
    }
  }

  private boolean accepts(final AutomatonProxy aut,
                          final ComponentKind kind,
                          final CounterExampleProxy counter)
  {
    final TraceFinder finder = getTraceFinder(aut, kind);
    finder.setComponentKind(kind);
    final TraceFinder.Result result = finder.examine(counter);
    return mTraceChecker.accepts(aut, kind, counter, result);
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    return ProxyTools.getShortClassName(this) + " " + getName();
  }


  //#########################################################################
  //# Data Members
  private final KindTranslator mKindTranslator;
  private final HeuristicFactory.Preference mPreference;
  private final List<HeuristicValueProvider> mHeuristics;
  private final HeuristicTraceChecker mTraceChecker;
  private final Map<AutomatonProxy,TraceFinder> mTraceFinders;

}
