//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractDeadlockChecker;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A monolithic deadlock checker implementation, written in C++. The
 * native deadlock checker implements the standard {@link DeadlockChecker}
 * interface and determines whether a given input model contains a deadlock.
 *
 * <P><STRONG>Supported Features.</STRONG></P>
 *
 * <P>This implementation supports both deterministic and nondeterministic
 * models.</P>
 *
 * <P>Counterexamples are computed for all models with deadlock.</P>
 *
 * <P><STRONG>Algorithm.</STRONG></P>
 *
 * <P>This algorithm performs a breadth-first search of the synchronous
 * product state space, terminating early as soon as a deadlock state is
 * discovered.</P>
 *
 * <P>State tuples are stored in bit-packed form in a hash table. The number
 * and size of the state tuples is the main limiting factor in terms of
 * memory requirements. The number of states can be limited by specifying
 * the node limit ({@link #setNodeLimit(int) setNodeLimit()}).</P>
 *
 * <P>The algorithm to determine which transitions are enabled is highly
 * optimised. A lot of effort is taken to suppress the exploration of
 * disabled transitions or transitions known to be selfloops early.</P>
 *
 * <P><STRONG>Configuration.</STRONG></P>
 *
 * <UL>
 * <LI>If the node limit is specified ({@link #setNodeLimit(int)
 *     setNodeLimit()}), it defines the maximum number of states that can
 *     be constructed. If the synchronous product state space turns out to
 *     be larger during the search, the verification attempt is aborted
 *     and an {@link net.sourceforge.waters.model.analysis.OverflowException
 *     OverflowException} is thrown.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class NativeDeadlockChecker
  extends NativeModelVerifier
  implements DeadlockChecker
{

  //#########################################################################
  //# Constructors
  public NativeDeadlockChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeDeadlockChecker(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  public NativeDeadlockChecker(final ProductDESProxy model,
                               final EventProxy marking,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.DeadlockChecker
  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Native Methods
  @Override
  native VerificationResult runNativeAlgorithm();


  //#########################################################################
  //# Hooks
  @Override
  public String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return AbstractDeadlockChecker.getTraceName(model);
  }

}
