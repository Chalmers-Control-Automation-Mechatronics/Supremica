//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class MaxCommonUncontrollableEventsHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;

  public MaxCommonUncontrollableEventsHeuristic
    (final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public MaxCommonUncontrollableEventsHeuristic
    (final KindTranslator translator,
     final ModularHeuristicFactory.Preference type)
  {
    super(translator);
    mType = type;
  }

  @Override
  public Collection<AutomatonProxy> heur(final ProductDESProxy composition,
                                         final Set<AutomatonProxy> nonComposedPlants,
                                         final Set<AutomatonProxy> nonComposedSpecPlants,
                                         final Set<AutomatonProxy> nonComposedSpecs,
                                         final SafetyCounterExampleProxy counterExample)
  {
    final TraceProxy trace = counterExample.getTrace();
    final KindTranslator translator = getKindTranslator();
    AutomatonProxy automaton = checkAutomata(false, nonComposedPlants,
                                             new MaxEventComparator(composition, translator),
                                             trace);
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automaton == null;
    if (automaton == null || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      automaton = checkAutomata(automaton, false, nonComposedSpecPlants,
                                new MaxEventComparator(composition, translator),
                                trace);
    }
    if (automaton == null ||
        mType == ModularHeuristicFactory.Preference.NOPREF ||
        runspecs) {
      automaton = checkAutomata(automaton, true, nonComposedSpecs,
                                new MaxEventComparator(composition, translator),
                                trace);
    }
    return automaton == null ? null : Collections.singleton(automaton);
  }

  private static class MaxEventComparator
    implements Comparator<AutomatonProxy>
  {
    private final Set<EventProxy> mEvents;
    private final KindTranslator mTranslator;

    public MaxEventComparator(final ProductDESProxy composition,
                              final KindTranslator translator)
    {
      mEvents = composition.getEvents();
      mTranslator = translator;
    }

    @Override
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      int count1 = 0;
      int count2 = 0;
      for (final EventProxy e : a1.getEvents()) {
        if (mTranslator.getEventKind(e).equals(EventKind.UNCONTROLLABLE)) {
          if (mEvents.contains(e)) {
            count1++;
          }
        }
      }
      for (final EventProxy e : a2.getEvents()) {
        if (mTranslator.getEventKind(e).equals(EventKind.UNCONTROLLABLE)) {
          if (mEvents.contains(e)) {
            count2++;
          }
        }
      }
      return count1 - count2;
    }
  }
}
