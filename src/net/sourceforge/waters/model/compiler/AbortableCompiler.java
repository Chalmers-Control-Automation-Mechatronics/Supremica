//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   AbortableCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.base.VisitorException;


/**
 * An implementation of the {@link Abortable} interface used by compilers.
 * Throws {@link EvalAbortException} when abort is requested.
 *
 * @author Robi Malik
 */

public class AbortableCompiler implements Abortable
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Aborting
  /**
   * Checks whether this compiler has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  public void checkAbort()
    throws EvalAbortException
  {
    if (mIsAborting) {
      throw new EvalAbortException();
    }
  }

  /**
   * Checks whether this compiler has been requested to abort,
   * and if so, performs the abort by throwing a {@link VisitorException}
   * wrapped around an {@link AnalysisAbortException}. This method is used
   * instead of {@link #checkAbort()} when inside a {@link
   * net.sourceforge.waters.model.base.ProxyVisitor ProxyVisitor}.
   */
  public void checkAbortInVisitor()
    throws VisitorException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw new VisitorException(exception);
    }
  }


  //#########################################################################
  //# Data Members
  private volatile boolean mIsAborting;

}
