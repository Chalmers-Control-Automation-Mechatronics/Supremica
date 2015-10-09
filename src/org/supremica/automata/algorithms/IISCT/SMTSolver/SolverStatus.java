//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: org.supremica.automata.algorithms.PDR.SMTSolver
//# CLASS:   SolverStatus
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.algorithms.IISCT.SMTSolver;

/**
 * Solver status values.
 * <p>
 * @author Mohammad Reza Shoaei
 */
public enum SolverStatus
{

  // Used to signify an unsatisfiable status.
  UNSATISFIABLE(-1),
  // Used to signify an unknown status.
  UNKNOWN(0),
  // Used to signify a satisfiable status.
  SATISFIABLE(1);

  private final int intValue;

  private SolverStatus(final int v)
  {
    this.intValue = v;
  }

  public static final SolverStatus fromInt(final int v)
  {
    for (final SolverStatus k : values()) {
      if (k.intValue == v) {
        return k;
      }
    }
    return values()[0];
  }

  public final int toInt()
  {
    return this.intValue;
  }

  public final boolean isSAT()
  {
    return this.intValue == 1;
  }

  public final boolean isUNSAT()
  {
    return this.intValue == -1;
  }

  public final boolean isUNKNOWN()
  {
    return this.intValue == 0;
  }

  @Override
  public final String toString()
  {
    switch (this.toInt()) {
      case -1:
        return "unsat";
      case 1:
        return "sat";
      default:
        return "unknown";
    }
  }

}
