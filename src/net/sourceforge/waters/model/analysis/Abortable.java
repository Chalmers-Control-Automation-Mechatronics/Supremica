//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   Abortable
//###########################################################################
//# $Id$
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
 * <P>Every {@link ModelAnalyzer} should support abort requests, but
 * this interface is kept separate to facilitate its implementation by
 * subroutines that are not model analysers.</P>
 *
 * @see AbortException
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
   * {@link AbortException} from its {@link ModelAnalyzer#run() run()} method.
   */
  public void requestAbort();

  /**
   * Returns whether the model analyser has been requested to abort.
   */
  public boolean isAborting();

}
