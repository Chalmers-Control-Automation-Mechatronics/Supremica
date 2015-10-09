//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: 
//# PACKAGE: org.supremica.automata.algorithms.PDR.SMTSolver
//# CLASS:   AbstractSolver
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

/**
 *
 * @author Mohammad Reza Shoaei
 */
abstract class AbstractSolver
{

  public AbstractSolver()
  {
  }

 
  /**
   * Resets the solver. This removes all assertions from the solver.
   */
  public abstract void reset() throws SolverException;

  /**
   * Creates a backtracking point. See also {@link #pop()}
   * <p>
   * @throws SolverException
   */
  public abstract void push() throws SolverException;

  /**
   * Backtracks i backtracking points. An exception is thrown if i is not smaller than the scope.
   * See also {@link #push()}.
   * <p>
   * @param i Backtrack point
   * <p>
   * @throws SolverException
   */
  public abstract void pop(int i) throws SolverException;

  /**
   * Backtracks one backtracking point.
   *
   */
  public abstract void pop() throws SolverException;

  /**
   * The current number of backtracking points (scopes). See also {@link #pop()}.
   * <p>
   * @return The current number of scope
   * <p>
   * @throws SolverException
   */
  public abstract int getNumScops() throws SolverException;

  /**
   * Checks whether the assertions in the solver are consistent or not.
   * <p>
   * @return Solver status.
   * <p>
   * @throws SolverException
   */
  public abstract SolverStatus check() throws SolverException;

}
