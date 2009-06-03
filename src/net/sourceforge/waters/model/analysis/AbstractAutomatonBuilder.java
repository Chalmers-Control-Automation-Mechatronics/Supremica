//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * An abstract base class to facilitate the implementation of automaton
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyser}, this class provides access to a automaton
 * result member, and uses this to implement access to the computed
 * automaton.
 *
 * @author Robi Malik
 */

public abstract class AbstractAutomatonBuilder
  extends AbstractModelAnalyser
  implements AutomatonBuilder
{

  //#########################################################################
  //# Constructors
  public AbstractAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractAutomatonBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mResult = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  public AutomatonProxy getComputedAutomaton()
  {
    if (mResult != null) {
      return mResult.getAutomaton();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public AutomatonResult getAnalysisResult()
  {
    return mResult;
  }

  public void clearAnalysisResult()
  {
    mResult = null;
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores an automaton result indicating successful computation.
   * @param  aut     The automaton to be stored in the result.
   * @return <CODE>true</CODE>
   */
  protected boolean setAutomatonResult(final AutomatonProxy aut)
  {
    mResult = new AutomatonResult(aut);
    addStatistics(mResult);
    return true;
  }

  /**
   * Stores a verification result indicating unsuccessful computation.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final AutomatonProxy counterexample)
  {
    mResult = new AutomatonResult();
    addStatistics(mResult);
    return false;
  }

  /**
   * Stores any available statistics on this automaton builder's last run
   * in the given automaton result. This default implementation does
   * nothing, it needs to be overridden by subclasses.
   */
  protected void addStatistics(AutomatonResult result)
  {
  }


  //#########################################################################
  //# Data Members
  private AutomatonResult mResult;

}
