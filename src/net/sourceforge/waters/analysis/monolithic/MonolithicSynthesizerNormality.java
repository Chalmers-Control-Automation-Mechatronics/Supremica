//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class MonolithicSynthesizerNormality
  extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{
  //#########################################################################
  //# Constructors
  public MonolithicSynthesizerNormality(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynthesizerNormality(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynthesizerNormality(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean getSupervisorReductionEnabled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean getSupervisorLocalizationEnabled()
  {
    // TODO Auto-generated method stub
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  public void setUp()
  {
    try {
      //Get the automata and default marking proposition
      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();
      final EventProxy marking = getMarkingProposition();
      final Collection<EventProxy> filter = Collections.singleton(marking);
      mMarking = marking;
      mAutomata = model.getAutomata().toArray(new AutomatonProxy[0]);
      mEventEncoding = new EventEncoding(model.getEvents(), translator, filter, EventEncoding.FILTER_PROPOSITIONS);

      //Get the number of plant automata, this will be the size of our tuple
      for (final AutomatonProxy aut : mAutomata) {
        if (translator.getComponentKind(aut) == ComponentKind.PLANT)
          mNumPlantAutomata++;
      }

      //Setup state markings map and transition map
      mStateMarkings = new boolean[mAutomata.length][];
      mTransitionMap = new int[mAutomata.length][][];
      mStateToIndexMap = new HashMap<StateProxy, Integer>();

      //Loop through each automaton
      int automataCounter = 0;
      for(final AutomatonProxy aut : mAutomata) {
        //Get events/states/transitions for each automata and initialise maps
        final Set<EventProxy> events = aut.getEvents();
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        final Set<StateProxy> states = aut.getStates();
        mStateMarkings[automataCounter] = new boolean[states.size()];
        mTransitionMap[automataCounter] = new int[states.size()][events.size()];

        //If a marked state isn't specified, all states are marked
        if(!events.contains(marking)) {
          Arrays.fill(mStateMarkings[automataCounter], true);
        }
        //Process all states to build marking map
        int stateCounter = 0;
        for(final StateProxy state : states){
          mStateToIndexMap.put(state, stateCounter);
          final Collection<EventProxy> propositions = state.getPropositions();
          if(propositions.contains(marking)){
            mStateMarkings[automataCounter][stateCounter] = true;
          }
          stateCounter++;
        }

        //Need to fill transitionmap array with a value to indicate when there's no transition. e.g -1

        //Process all transitions to build a transition map
        for(final TransitionProxy trans : transitions){
          final int event = mEventEncoding.getEventCode(trans.getEvent());
          final int sourceState = mStateToIndexMap.get(trans.getSource());
          final int targetState = mStateToIndexMap.get(trans.getTarget());
          mTransitionMap[automataCounter][sourceState][event] = targetState;
        }
        automataCounter++;
      }

    } catch (final EventNotFoundException exception) {
      exception.printStackTrace();
    } catch (final OverflowException exception) {
      //to indicate that the number of propositions exceeds the supported maximum
      exception.printStackTrace();
    }
  }

  @Override
  public boolean run() throws AnalysisException
  {
    setUp();

    //Find reachable states


    //Need setup and teardown methods to setup and reset object for reuse.
    //1 Find reachable states
    //2 remove blocking
    //3 do my stuff here, so check/prune for controllability and normality
    //4 create output automaton



    return false;
  }

  @Override
  public void tearDown()
  {
    mAutomata = null;
    mMarking = null;
    mStateMarkings = null;
    mEventEncoding = null;
    mStateToIndexMap = null;
    mNumPlantAutomata = 0;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  //#########################################################################
  //# Auxiliary Methods
  private EventProxy getMarkingProposition() throws EventNotFoundException
  {
    if(mMarking == null){
      final ProductDESProxy model = getModel();
      mMarking = AbstractConflictChecker.getMarkingProposition(model);
    }
    return mMarking;
  }

  //#########################################################################
  //# Data Members
  private AutomatonProxy[] mAutomata;
  private EventProxy mMarking;
  private EventEncoding mEventEncoding;
  private HashMap<StateProxy, Integer> mStateToIndexMap;
  private boolean[][] mStateMarkings;
  private int[][][] mTransitionMap;
  @SuppressWarnings("unused")
  private int mNumPlantAutomata; //Integer? So can be null?

}
