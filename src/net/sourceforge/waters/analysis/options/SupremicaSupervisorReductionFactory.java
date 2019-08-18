//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */

package net.sourceforge.waters.analysis.options;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;


/**
 * <P>Supervisor reduction factory for Supremica.</P>
 *
 * <P>This is a dummy implementation of the {@link SupervisorReductionFactory}
 * interface, making it possible to configure Supremica's supervisor through
 * the {@link net.sourceforge.waters.analysis.options.Parameter Parameter}
 * interfaces of Waters.</P>
 *
 * @author Robi Malik
 */

public enum SupremicaSupervisorReductionFactory
  implements SupervisorReductionFactory
{
  //#########################################################################
  //# Enumeration
  OFF("Off"),
  DET_MINSTATE("Deterministic min-state");


  //#########################################################################
  //# Constructor
  private SupremicaSupervisorReductionFactory(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory
  @Override
  public TransitionRelationSimplifier createInitialMinimizer
    (final boolean includeCoreachability)
  {
    return new SupervisorReductionChain(includeCoreachability);
  }

  @Override
  public SupervisorReductionSimplifier createSupervisorReducer()
  {
    return null;
  }

  @Override
  public boolean isSupervisedReductionEnabled()
  {
    return this != OFF;
  }

  @Override
  public boolean isSupervisedEventRequired()
  {
    return false;
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
