//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;


/**
 * <P>Factory class to create transition relation simplifiers or heuristics
 * objects for a compositional model analyser.</P>
 *
 * <P>The tool creator has a type argument that represents the type of tool
 * it creates, e.g., {@link TransitionRelationSimplifier}.
 * Its {@link #create(AbstractTRCompositionalModelAnalyzer) create()} method is
 * invoked during initialisation of the model analyser to create the tool in
 * the correct context. In addition, the tool creator has a name, so it can
 * be added to a {@link ListedEnumFactory} to implement command line
 * options.</P>
 *
 * @author Robi Malik
 *
 * @see AbstractTRCompositionalModelAnalyzer
 */

public abstract class TRToolCreator<T>
{

  //#########################################################################
  //# Constructors
  protected TRToolCreator(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Override for java.lang.Object
  @Override
  public String toString()
  {
    return getName();
  }


  //#########################################################################
  //# Factory Methods
  /**
   * Returns the name of the tool created by this tool creator.
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Creates a tool to be used by the given model analyser.
   */
  public abstract T create(AbstractTRCompositionalModelAnalyzer analyzer)
    throws AnalysisConfigurationException;


  //#########################################################################
  //# Data Members
  private final String mName;

}
