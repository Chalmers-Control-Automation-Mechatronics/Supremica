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

package net.sourceforge.waters.analysis.bdd;

import java.util.BitSet;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

class UncontrollableEventBDD extends EventBDD
{

  //#########################################################################
  //# Constructor
  UncontrollableEventBDD(final EventProxy event,
                         final int numautomata,
                         final BDDFactory factory)
  {
    super(event, numautomata, factory);
    mTestedAutomata = new BitSet(numautomata);
    mTestedSpecs = new BitSet(numautomata);
    mEnabledBDD = null;
    mPlantEnabledBDD = factory.one();
    mSpecEnabledBDD = factory.one();
    mControllabilityConditionBDD = null;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class EventBDD
  EventKind getEventKind()
  {
    return EventKind.UNCONTROLLABLE;
  }

  void startAutomaton(final AutomatonBDD autbdd, final BDDFactory factory)
  {
    super.startAutomaton(autbdd, factory);
    if (mPlantEnabledBDD != null) {
      final ComponentKind kind = autbdd.getKind();
      switch (kind) {
      case PLANT:
        mEnabledBDD = factory.zero();
        break;
      case SPEC:
        if (mSpecEnabledBDD != null) {
          mEnabledBDD = factory.zero();
        }
        break;
      default:
        break;
      }
    }
  }

  void includeTransition(final TransitionProxy trans, final BDDFactory factory)
  {
    super.includeTransition(trans, factory);
    if (mEnabledBDD != null) {
      final AutomatonBDD autbdd = getCurrentAutomaton();
      final StateProxy source = trans.getSource();
      final BDD statebdd = autbdd.getStateBDD(source, factory);
      mEnabledBDD.orWith(statebdd);
    }
  }

  void finishAutomaton(final BDDFactory factory)
  {
    if (mEnabledBDD != null) {
      final AutomatonBDD autbdd = getCurrentAutomaton();
      final ComponentKind kind = autbdd.getKind();
      final int bitno = autbdd.getAutomatonIndex();
      switch (kind) {
      case PLANT:
        if (mEnabledBDD.isZero()) {
          mPlantEnabledBDD.free();
          mPlantEnabledBDD = null;
          if (mSpecEnabledBDD != null) {
            mSpecEnabledBDD.free();
            mSpecEnabledBDD = null;
            mTestedSpecs.clear();
          }
          mTestedAutomata.clear();
        } else if (!mEnabledBDD.isOne()) {
          mPlantEnabledBDD.andWith(mEnabledBDD);
          mTestedAutomata.set(bitno);
        }
        break;
      case SPEC:
        if (mEnabledBDD.isZero()) {
          mSpecEnabledBDD.free();
          mSpecEnabledBDD = null;
          mTestedSpecs.clear();
        } else if (!mEnabledBDD.isOne()) {
          mSpecEnabledBDD.andWith(mEnabledBDD);
          mTestedSpecs.set(bitno);
        }
        break;
      default:
        break;
      }
      mEnabledBDD = null;
    }
    super.finishAutomaton(factory);
  }

  BDD getControllabilityConditionBDD()
  {
    if (mControllabilityConditionBDD == null) {
      if (mPlantEnabledBDD == null) {
        return null;
      } else if (mSpecEnabledBDD == null) {
        mControllabilityConditionBDD = mPlantEnabledBDD.not();
        mPlantEnabledBDD.free();
        mPlantEnabledBDD = null;
      } else {
        mControllabilityConditionBDD =
          mPlantEnabledBDD.impWith(mSpecEnabledBDD);
        mPlantEnabledBDD = null;
        mSpecEnabledBDD = null;
        mTestedAutomata.or(mTestedSpecs);
      }
    }
    if (mControllabilityConditionBDD.isOne()) {
      mControllabilityConditionBDD = null;
    }
    return mControllabilityConditionBDD;
  }

  BitSet getControllabilityTestedAutomata()
  {
    return mTestedAutomata;
  }


  //#########################################################################
  //# Data Members
  private final BitSet mTestedAutomata;
  private final BitSet mTestedSpecs;

  private BDD mControllabilityConditionBDD;
  private BDD mPlantEnabledBDD;
  private BDD mSpecEnabledBDD;
  private BDD mEnabledBDD;

}
