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

package net.sourceforge.waters.analysis.abstraction;

/**
 * Callback interface for transition relation simplifiers.
 * This interface contains two methods to execute custom code
 * when a transition relation simplifier starts or finishes
 * execution.
 *
 * @see TransitionRelationSimplifier
 * @author Robi Malik
 */

public interface TRSimplificationListener
{

  /**
   * Callback executed before a transition relation simplifier starts
   * execution.
   * @param   simplifier  The simplifier that just starts execution.
   * @return  Whether simplification is allowed to start.
   *          The simplifier will only execute if this method returns
   *          <CODE>true</CODE>. If <CODE>false</CODE> is returned,
   *          simplification will be skipped, and the result reported
   *          as unchanged. The callback {@link
   *          #onSimplificationFinish(TransitionRelationSimplifier, boolean)
   *          onSimplificationFinish()} will not be called in this case.
   */
  public boolean onSimplificationStart(TransitionRelationSimplifier simplifier);

  /**
   * Callback executed after a transition relation simplifier has finished
   * execution. This method is only called on successful completion, not
   * in case of an exception, and not when simplification was cancelled
   * by the {@link #onSimplificationStart(TransitionRelationSimplifier)
   * onSimplificationStart()} handler.
   * @param   simplifier  The simplifier that has just finished.
   *                      Additional information can be retrieved from
   *                      this object.
   * @param   result      The result returned by the simplifier's {@link
   *                      TransitionRelationSimplifier#run() run()} method.
   *                      A value of <CODE>true</CODE> indicates that the
   *                      input transition relation has been modified.
   */
  public void onSimplificationFinish(TransitionRelationSimplifier simplifier,
                                     boolean result);

}
