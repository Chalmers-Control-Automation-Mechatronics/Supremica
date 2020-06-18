//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A wrapper superclass to implement abstraction rules based on a
 * {@link TransitionRelationSimplifier}.
 *
 * @author Robi Malik
 */

abstract class TRSimplifierAbstractionRule extends AbstractionRule
{

  //#########################################################################
  //# Constructor
  TRSimplifierAbstractionRule(final ProductDESProxyFactory factory,
                              final KindTranslator translator,
                              final TransitionRelationSimplifier simplifier)
  {
    this(factory, translator, null, simplifier);
  }

  TRSimplifierAbstractionRule(final ProductDESProxyFactory factory,
                              final KindTranslator translator,
                              final Collection<EventProxy> propositions,
                              final TransitionRelationSimplifier simplifier)
  {
    super(factory, translator, propositions);
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Simple Access
  TransitionRelationSimplifier getSimplifier()
  {
    return mSimplifier;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mSimplifier.requestAbort();
  }

  @Override
  public boolean isAborting()
  {
    return mSimplifier.isAborting();
  }

  @Override
  public void resetAbort()
  {
    mSimplifier.resetAbort();
  }


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;

}
