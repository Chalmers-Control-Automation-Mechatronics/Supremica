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

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * <P>A transition relation simplifier to remove states from which neither an
 * alpha or omega state can be reached. This simplifier implements the
 * coreachability abstraction rule for compositional generalised nonblocking
 * verification.</P>
  *
 * <P><I>Reference:</I> Robi Malik, Ryan Leduc. Compositional Nonblocking
 * Verification using Generalised Nonblocking Abstractions. IEEE Transactions
 * on Automatic Control, <STRONG>58</STRONG>(8), 1-13, August 2013.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class GNBCoreachabilityTRSimplifier
  extends CoreachabilityTRSimplifier
{

  //#######################################################################
  //# Constructors
  public GNBCoreachabilityTRSimplifier()
  {
  }

  public GNBCoreachabilityTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.CoreachabilityTRSimplifier
  @Override
  protected boolean isTriviallyUnchanged()
  {
    if (super.isTriviallyUnchanged()) {
      return true;
    } else {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int alphaID = getPreconditionMarkingID();
      return !rel.isPropositionUsed(alphaID);
    }
  }

  @Override
  protected boolean isMarked(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    if (rel.isMarked(state, defaultID)) {
      return true;
    }
    final int alphaID = getPreconditionMarkingID();
    return rel.isMarked(state, alphaID);
  }

}
