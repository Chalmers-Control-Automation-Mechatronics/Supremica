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

import net.sourceforge.waters.model.base.Proxy;


/**
 * A result record containing a structured object.
 * A proxy result typically contains an automaton
 * ({@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy})
 * or a product DES ({@link net.sourceforge.waters.model.des.ProductDESProxy
 * ProductDESProxy}) representing the result of an analysis algorithm such
 * as projection, minimisation, or synthesis. In addition, it may contain
 * some statistics about the analysis run.
 *
 * @author Robi Malik
 */

public interface ProxyResult<P extends Proxy>
  extends AnalysisResult
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the object computed by the model analyser,
   * or <CODE>null</CODE> if the computation was unsuccessful.
   */
  public P getComputedProxy();

  /**
   * Sets the computed object (e.g.,
   * {@link net.sourceforge.waters.model.des.AutomatonProxy AutomatonProxy})
   * for this result. Setting the computed object also marks the analysis run
   * as completed and sets the Boolean result.
   * @param  proxy  The computed object, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  public void setComputedProxy(final P proxy);

  /**
   * Returns a short string describing the computed object,
   * e.g. &quot;counterexample&quot; or &quot;supervisor&quot;.
   */
  public String getResultDescription();

}
