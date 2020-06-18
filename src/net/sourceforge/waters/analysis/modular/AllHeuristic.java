//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class AllHeuristic
  extends AbstractModularHeuristic
{
  private final ModularHeuristicFactory.Preference mType;

  public AllHeuristic(final KindTranslator translator)
  {
    this(translator, ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
  }

  public AllHeuristic(final KindTranslator translator,
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
    final Collection<AutomatonProxy> automata = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy automaton : nonComposedPlants) {
      if (getNumberOfAcceptedEvents(automaton, trace) != trace.getEvents().size()) {
        automata.add(automaton);
      }
    }
    final boolean runspecs = mType == ModularHeuristicFactory.Preference.PREFER_REAL_PLANT && automata.isEmpty();
    if (automata.size() == 0 || mType != ModularHeuristicFactory.Preference.PREFER_REAL_PLANT) {
      for (final AutomatonProxy automaton : nonComposedSpecPlants) {
        if (getNumberOfAcceptedEvents(automaton, trace) != trace.getEvents().size()) {
          automata.add(automaton);
        }
      }
    }
    if (automata.size() == 0 ||
        mType == ModularHeuristicFactory.Preference.NOPREF ||
        runspecs) {
      final KindTranslator translator = getKindTranslator();
      for (final AutomatonProxy automaton : nonComposedSpecs) {
        final int i = getNumberOfAcceptedEvents(automaton, trace);
        if (i != trace.getEvents().size()
            && translator.getEventKind(trace.getEvents().get(i))
            == EventKind.CONTROLLABLE) {
          automata.add(automaton);
        }
      }
    }
    return automata.size() == 0 ? null : automata;
  }
}
