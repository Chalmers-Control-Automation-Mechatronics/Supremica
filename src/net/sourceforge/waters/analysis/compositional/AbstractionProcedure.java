//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractionProcedure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

public interface AbstractionProcedure extends Abortable
{

  /**
   * Runs this abstraction procedure to simplify an automaton.
   * This method hides any local events and then attempts to simplify
   * the given automaton. If abstraction is possible, it creates the
   * appropriate abstraction steps and adds them to the given list.
   * @param  aut    The automaton to be simplified.
   * @param  local  Collection of local events to be hidden.
   * @param  steps  List to receive new abstraction steps.
   * @return <CODE>true</CODE> if the automaton was simplified and
   *         an abstraction step added to the queue, <CODE>false</CODE>
   *         otherwise.
   */
  public boolean run(AutomatonProxy aut,
                     Collection<EventProxy> local,
                     List<AbstractionStep> steps)
    throws AnalysisException;

  public void storeStatistics();

  public void resetStatistics();

}
