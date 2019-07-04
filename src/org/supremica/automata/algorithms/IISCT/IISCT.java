//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: org.supremica.automata.algorithms.IISCT
//# CLASS:   IISCT
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;

import org.supremica.automata.algorithms.IISCT.SMTSolver.SolverException;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class IISCT
{

  public IISCT(final SimpleEFASystem system) throws SolverException
  {
    mSystem = system;
//    mEventEncoding = system.getEventEncoding();
    mSynchronizer = new EFASynchronizer(EFASynchronizer.MODE_IISCT);
  }

  public void run() throws AnalysisException
  {
    final ArrayList<SimpleEFAComponent> plants = new ArrayList<>();
    final ArrayList<SimpleEFAComponent> specs = new ArrayList<>();
    for (final SimpleEFAComponent efa : mSystem.getComponents()) {
      if (efa.getKind() == ComponentKind.PLANT) {
        plants.add(efa);
      } else if (efa.getKind() == ComponentKind.SPEC) {
        specs.add(efa);
      }
    }
    if (plants.isEmpty()){
    	return;
    }
    uSynthesizer(plants, specs);
  }

  private boolean uSynthesizer(final List<SimpleEFAComponent> plants, final List<SimpleEFAComponent> specs)
   throws AnalysisException
  {
    mSynchronizer.init(plants);
    specs.forEach(spec -> mSynchronizer.addComponent(spec));
    mSynchronizer.lowSynch();
    final IISynthesizer synthesizer = new IISynthesizer(mSynchronizer.getLowVarContext(),
                                                  mSynchronizer.getLowTransitionRelation(),
                                                  mSynchronizer.getLowLabelEncoding(),
                                                  mSynchronizer.getLowStateEncoding(),
                                                  mSynchronizer.getLowLbToSpecMap());
    final boolean result = synthesizer.synthesis();
    System.out.println("Done: " + result);
    System.err.println("Done: " + result);
    synthesizer.dispose();
    return result;
  }

//  private TIntArrayList getUncontrollableEvents(final int[] events)
//  {
//    final TIntArrayList uevents = new TIntArrayList(events.length);
//    for (final int e : events) {
//      if (!mEventEncoding.isControllable(e)) {
//        uevents.add(e);
//      }
//    }
//    return uevents;
//  }


  private final SimpleEFASystem mSystem;
//  private final SimpleEFAEventEncoding mEventEncoding;
  private final EFASynchronizer mSynchronizer;

}
