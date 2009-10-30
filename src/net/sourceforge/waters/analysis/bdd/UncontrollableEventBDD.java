//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   UncontrollableEventBDD
//###########################################################################
//# $Id$
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
      final int bitno = autbdd.getBitIndex();
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
