//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * A result returned by the compositional synthesis algorithms
 * {@link CompositionalSynthesizer}. In addition to the common result data, it
 * includes a collection of automata representing the synthesised modular
 * supervisor.
 *
 * @author Robi Malik
 */

public class CompositionalSynthesisResult
  extends CompositionalAnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new synthesis result representing an incomplete run.
   */
  public CompositionalSynthesisResult()
  {
    mSupervisors = new ArrayList<AutomatonProxy>();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.DefaultAnalysisResult
  @Override
  public void setSatisfied(final boolean sat)
  {
    super.setSatisfied(sat);
    if (!sat) {
      mSupervisors.clear();
    }
  }

  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalSynthesisResult result =
      (CompositionalSynthesisResult) other;
    final Collection<AutomatonProxy> sups = result.getSupervisors();
    mSupervisors.addAll(sups);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.VerificationResult
  /**
   * Gets the list of synthesised supervisors.
   */
  public Collection<AutomatonProxy> getSupervisors()
  {
    return mSupervisors;
  }

  /**
   * Adds the given automaton to the list of synthesised supervisors.
   */
  public void addSupervisor(final AutomatonProxy sup)
  {
    mSupervisors.add(sup);
  }


  //#########################################################################
  //# Data Members
  private final Collection<AutomatonProxy> mSupervisors;

}
