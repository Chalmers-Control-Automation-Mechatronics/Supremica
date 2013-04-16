//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>Interface of model analysers that compute automata or other objects
 * as a result.</P>
 *
 * <P>A model builder takes a finite-state machine model as input,
 * performs some kind of analysis, and computes a new object, typically an
 * automaton ({@link net.sourceforge.waters.model.des.AutomatonProxy
 * AutomatonProxy}) or a product DES ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy})
 * as a result. The input model may contain several automata, in which case
 * the system to be analysed is their synchronous product.</P>
 *
 * <P>To use a model builder, the user first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then the algorithm is started
 * using the {@link #run() run()} method. Afterwards results can be queried
 * using the {@link #getComputedProxy()} method.</P>
 *
 * <P>This interface is extended for different types of algorithms.</P>
 *
 * @author Robi Malik
 */

public interface ModelBuilder<P extends Proxy> extends ModelAnalyzer
{

  //#########################################################################
  //# Configuration
  /**
   * Sets the name to be given to the output automaton.
   * @param  name   Name for output automaton, or <CODE>null</CODE> to
   *                indicate that the name of the input automaton is to
   *                be used.
   */
  public void setOutputName(String name);

  /**
   * Gets the configured name of the output automaton.
   * @see #setOutputName(String) setOutputName()
   */
  public String getOutputName();

  /**
   * Sets whether this builder actually constructs a result object.
   * This option is set to <CODE>true</CODE> by default, but it can turned off
   * to run an automaton builder that only produces statistics.
   */
  public void setConstructsResult(boolean construct);

  /**
   * Gets whether an result is actually constructed by this builder.
   * @see #setConstructsResult(boolean) setConstructsResult()
   */
  public boolean getConstructsResult();


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the item computed by this algorithm.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyzer#run() run()} has been called, or model checking
   *         has found that no proper result can be computed for the
   *         input model.
   */
  public P getComputedProxy();

  public ProxyResult<P> getAnalysisResult();

}
