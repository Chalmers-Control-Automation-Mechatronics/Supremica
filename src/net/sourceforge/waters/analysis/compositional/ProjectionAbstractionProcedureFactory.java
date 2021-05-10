//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.ListedEnumFactory;


/**
 * A collection of abstraction methods to be used by the compositional
 * (projecting) safety verifier. The members of this enumeration are passed
 * to the {@link CompositionalSafetyVerifier} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureCreator(AbstractionProcedureCreator)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik
 */

public class ProjectionAbstractionProcedureFactory
  extends ListedEnumFactory<AbstractionProcedureCreator>
{

  //#########################################################################
  //# Singleton Pattern
  public static ProjectionAbstractionProcedureFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static ProjectionAbstractionProcedureFactory INSTANCE =
      new ProjectionAbstractionProcedureFactory();
  }


  //#########################################################################
  //# Constructors
  protected ProjectionAbstractionProcedureFactory()
  {
    register(PROJ);
  }


  //#########################################################################
  //# Enumeration
  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for standard nonblocking, but using weak observation
   * equivalence instead of observation equivalence, and using proper
   * certain conflicts simplification instead of limited certain
   * conflicts.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  public static final AbstractionProcedureCreator PROJ =
    new AbstractionProcedureCreator("PROJ")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalSafetyVerifier verifier =
        (CompositionalSafetyVerifier) analyzer;
      return ProjectionAbstractionProcedure.
        createProjectionAbstractionProcedure(verifier);
    }
  };

}
