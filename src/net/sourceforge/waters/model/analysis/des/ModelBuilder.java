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
   * Sets the name to be given to the output object.
   * @param  name   Name for output, or <CODE>null</CODE> to indicate that a
   *                default name chosen based on the input is to be used.
   */
  public void setOutputName(String name);

  /**
   * Gets the configured name of the output object.
   * @see #setOutputName(String) setOutputName()
   */
  public String getOutputName();


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

  @Override
  public ProxyResult<P> getAnalysisResult();

}
