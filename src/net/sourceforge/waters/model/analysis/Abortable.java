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

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * <P>The interface for analysis operations that can be aborted.</P>
 *
 * <P>As Java does not support threads to be terminated externally,
 * every task has to support graceful aborts. Users of an operation
 * can request the operation to be aborted. When such a request is
 * made, it is up to the operation to stop at the next convenient time.</P>
 *
 * <P>A {@link ModelAnalyzer} or other class that contains an abortable
 * algorithm should implement this interface. Its user can request the
 * algorithm to abort by calling {@link #requestAbort()}, which is typically
 * done from a separate thread. The running algorithm then should stop by
 * throwing an {@link AnalysisAbortException} at the next convenient time.
 * This usually means that the algorithm must check periodically whether an
 * abort request has been received.</P>
 *
 * <P>The easiest way to implement this behaviour is by extending the helper
 * class {@link AbstractAbortable} and making the algorithm call {@link
 * AbstractAbortable#checkAbort() checkAbort()} periodically.</P>
 *
 * @see AbstractAbortable
 * @see AnalysisAbortException
 * @author Robi Malik
 */

public interface Abortable
{

  //#########################################################################
  //# Aborting
  /**
   * Requests that a running model analyser aborts.
   * This does not necessarily cause an abort, it merely sets a flag to
   * request abort at a later time. It may take some time for the model
   * analyser to check this flag and react, or the request may not be
   * obeyed at all. If a model analyser aborts, it will throw an
   * {@link AnalysisAbortException} from its {@link ModelAnalyzer#run() run()}
   * method.
   */
  public void requestAbort();

  /**
   * Returns whether the model analyser has been requested to abort.
   */
  public boolean isAborting();

  /**
   * Resets a previous abort request. This method removes any pending
   * abort request, so {@link #isAborting()} will return <CODE>false</CODE>
   * again. This method should not be called while the controlled object
   * is still executing, as it is undefined whether or not an abort will
   * be cancelled safely in such a case.
   */
  public void resetAbort();

}
