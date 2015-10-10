//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: org.supremica.automata.algorithms.PDR.SMTSolver
//# CLASS:   SolverException
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

import net.sourceforge.waters.model.analysis.AnalysisException;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SolverException extends AnalysisException
{

  private static final long serialVersionUID = -6707355760997049105L;

  public SolverException()
  {
  }

  public SolverException(final Throwable t)
  {
    super(t);
  }

  public SolverException(final String solver, final Exception e)
  {
    super(solver + " solver exception > " + e.getMessage());
  }

  public SolverException(final String solver, final String message)
  {
    super(solver + " solver exception > " + message);
  }

  public SolverException(final String message)
  {
    super(message);
  }

  public static void wrap(final Throwable t) throws SolverException
  {
    throw new SolverException(t);
  }

  public static void wrap(final String solver, final String message) throws SolverException
  {
    throw new SolverException(solver, message);
  }

  public static void wrap(final String solver, final Exception e) throws SolverException
  {
    throw new SolverException(solver, e.getMessage());
  }

  public static void wrap(final String message) throws SolverException
  {
    throw new SolverException(message);
  }

}
