//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ProjectionAbstractionProcedureFactory
//###########################################################################
//# $Id$
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