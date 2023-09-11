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
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A general-purpose implementation of the {@link ModularHeuristic}
 * interface. This class merely contains several useful methods to help
 * implementing the various heuristics. The actual heuristic procedure
 * is implemented in each subclass using the tools provided here.
 *
 * @author Simon Ware
 */

class HeuristicEvaluator
  implements Comparator<AutomatonProxy>
{

  //#########################################################################
  //# Constructor
  HeuristicEvaluator(final KindTranslator translator,
                     final ModularHeuristicFactory.Preference pref,
                     final HeuristicValueProvider heuristics,
                     final HeuristicTraceChecker checker)
  {
    this(translator, pref, Collections.singletonList(heuristics), checker);
  }

  HeuristicEvaluator(final KindTranslator translator,
                     final ModularHeuristicFactory.Preference pref,
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

  ModularHeuristicFactory.Preference getPreference()
  {
    return mPreference;
  }

  String getName()
  {
    final HeuristicValueProvider provider = mHeuristics.get(0);
    return provider.getName();
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
      final int value1 = provider.getHeuristicValue(aut1);
      final int value2 = provider.getHeuristicValue(aut2);
      if (value1 != value2) {
        return value1 - value2;
      }
    }
    final String name1 = aut1.getName();
    final String name2 = aut2.getName();
    return name1.compareTo(name2);
  }


  //#########################################################################
  //# Invocation
  Collection<AutomatonProxy> collectNonAccepting
    (final ProductDESProxy des,
     final CounterExampleProxy counter,
     final Collection<AutomatonProxy> realPlants,
     final Collection<AutomatonProxy> specPlants,
     final Collection<AutomatonProxy> specs)
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

  Collection<AutomatonProxy> collectFirstNonAccepting
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> realPlants,
     final Collection<AutomatonProxy> specPlants,
     final Collection<AutomatonProxy> specs)
  {
    final AutomatonProxy aut =
      findFirstNonAccepting(counter, realPlants, specPlants, specs);
    if (aut == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(aut);
    }
  }

  private AutomatonProxy findFirstNonAccepting
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> realPlants,
     final Collection<AutomatonProxy> specPlants,
     final Collection<AutomatonProxy> specs)
  {
    AutomatonProxy aut =
      findFirstNonAccepting(counter, realPlants, ComponentKind.PLANT);
    if (aut == null) {
      aut = findFirstNonAccepting(counter, specPlants, ComponentKind.PLANT);
    }
    if (aut == null) {
      aut = findFirstNonAccepting(counter, specs, ComponentKind.PLANT);
    }
    return aut;
  }

  Collection<AutomatonProxy> collectBestNonAccepting
    (final ProductDESProxy des,
     final CounterExampleProxy counter,
     final Collection<AutomatonProxy> realPlants,
     final Collection<AutomatonProxy> specPlants,
     final Collection<AutomatonProxy> specs)
  {
    for (final HeuristicValueProvider provider : mHeuristics) {
      provider.setContext(des, counter, realPlants, specPlants, specs);
    }
    final AutomatonProxy best =
      findBestNonAccepting(counter, realPlants, specs, specs);
    if (best == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(best);
    }
  }

  AutomatonProxy findBestNonAccepting(final CounterExampleProxy counter,
                                     final Collection<AutomatonProxy> realPlants,
                                     final Collection<AutomatonProxy> specPlants,
                                     final Collection<AutomatonProxy> specs)
  {
    AutomatonProxy best = null;
    switch (mPreference) {
    case NOPREF:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      break;
    case PREFER_PLANT:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      if (best != null) {
        best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      }
      break;
    case PREFER_REAL_PLANT:
      best = findBestNonAccepting(counter, realPlants, ComponentKind.PLANT);
      if (best != null) {
        best = findBestNonAccepting(counter, specPlants, ComponentKind.PLANT, best);
      }
      if (best != null) {
        best = findBestNonAccepting(counter, specs, ComponentKind.PLANT, best);
      }
      break;
    }
    return best;
  }

  Collection<AutomatonProxy> collectAllNonAccepting
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> realPlants,
     final Collection<AutomatonProxy> specPlants,
     final Collection<AutomatonProxy> specs)
  {
    final Collection<AutomatonProxy> output = new LinkedList<>();
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
  private AutomatonProxy findFirstNonAccepting(final CounterExampleProxy counter,
                                               final Collection<AutomatonProxy> input,
                                               final ComponentKind kind)
  {
    for (final AutomatonProxy aut : input) {
      if (!accepts(aut, kind, counter)) {
        return aut;
      }
    }
    return null;
  }

  private AutomatonProxy findBestNonAccepting(final CounterExampleProxy counter,
                                              final Collection<AutomatonProxy> input,
                                              final ComponentKind kind)
  {
    return findBestNonAccepting(counter, input, kind, null);
  }

  private AutomatonProxy findBestNonAccepting(final CounterExampleProxy counter,
                                              final Collection<AutomatonProxy> input,
                                              final ComponentKind kind,
                                              AutomatonProxy best)
  {
    for (final AutomatonProxy aut : input) {
      if (!accepts(aut, kind, counter)) {
        if (best == null || compare(aut, best) < 0) {
          best = aut;
        }
      }
    }
    return best;
  }

  private void collectAllNonAccepting(final CounterExampleProxy counter,
                                      final Collection<AutomatonProxy> input,
                                      final Collection<AutomatonProxy> output,
                                      final ComponentKind kind)
  {
    for (final AutomatonProxy aut : input) {
      if (!accepts(aut, kind, counter)) {
        output.add(aut);
      }
    }
  }

  private boolean accepts(final AutomatonProxy aut,
                          final ComponentKind kind,
                          final CounterExampleProxy counter)
  {
    final TraceFinder finder = getTraceFinder(aut);
    finder.setComponentKind(kind);
    final TraceFinder.Result result = finder.examine(counter);
    return mTraceChecker.accepts(aut, kind, counter, result);
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
  private final KindTranslator mKindTranslator;
  private final ModularHeuristicFactory.Preference mPreference;
  private final List<HeuristicValueProvider> mHeuristics;
  private final HeuristicTraceChecker mTraceChecker;
  private final Map<AutomatonProxy,TraceFinder> mTraceFinders;


  //#########################################################################
  //# To be moved
  public interface HeuristicValueProvider
  {
    public String getName();
    public ModularHeuristicFactory.CollectionMode getCollectionMode();
    public void setContext(ProductDESProxy des,
                           CounterExampleProxy counter,
                           Collection<AutomatonProxy> realPlants,
                           Collection<AutomatonProxy> specPlants,
                           Collection<AutomatonProxy> specs);
    public int getHeuristicValue(AutomatonProxy aut);
  }

  public class DefaultHeuristicValueProvider implements HeuristicValueProvider
  {
    @Override
    public String getName()
    {
      final String fullname = getClass().getName();
      final int dotpos = fullname.lastIndexOf('.');
      final int start = dotpos + 1;
      if (fullname.endsWith(HEURISTIC_SUFFIX)) {
        final int end = fullname.length() - HEURISTIC_SUFFIX.length();
        return fullname.substring(start, end);
      } else {
        return fullname.substring(start);
      }
    }

    @Override
    public ModularHeuristicFactory.CollectionMode getCollectionMode()
    {
      return ModularHeuristicFactory.CollectionMode.BEST;
    }

    @Override
    public void setContext(final ProductDESProxy des,
                           final CounterExampleProxy counter,
                           final Collection<AutomatonProxy> realPlants,
                           final Collection<AutomatonProxy> specPlants,
                           final Collection<AutomatonProxy> specs)
    {
    }

    @Override
    public int getHeuristicValue(final AutomatonProxy aut)
    {
      return 0;
    }
  }

  static final String HEURISTIC_SUFFIX = "Heuristic";

  public interface HeuristicTraceChecker
  {
    public boolean accepts(AutomatonProxy aut,
                           ComponentKind kind,
                           CounterExampleProxy counter,
                           TraceFinder.Result result);
  }

  public class DefaultHeuristicTraceChecker implements HeuristicTraceChecker
  {
    @Override
    public boolean accepts(final AutomatonProxy aut,
                           final ComponentKind kind,
                           final CounterExampleProxy counter,
                           final TraceFinder.Result result)
    {
      return result.isAccepted();
    }
  }

}
