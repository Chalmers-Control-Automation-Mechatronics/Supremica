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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.model.base.ComponentKind;


/**
 * <P>A model verifier that checks whether a system of composed automata
 * is <I>coobservable</I>.</P>
 *
 * <P>A coobservability checker determines the controllability and
 * observability status of events using the attributes prefixes
 * {@link CoobservabilityAttributeFactory#CONTROLLABITY_KEY} and
 * {@link CoobservabilityAttributeFactory#OBSERVABITY_KEY} defined in class
 * {@link CoobservabilityAttributeFactory}. Any attribute whose name starts
 * with one of these prefixes carries the name of a site or controller that
 * can disable or observe all instances of the event.</P>
 *
 * <P>Thus the coobservability checker ignores the event kind information
 * and uses the attributes to determine controllability and observability.
 * It does however use {@link ComponentKind} information of automata to
 * distinguish plants and specifications.</P>
 *
 * <P>A model is coobservable with respect to a group of sites if it
 * satisfies the following controllability: If in the synchronous composition
 * of all plants and specification a state can be reached by some
 * trace&nbsp;<I>s</I> and in that state an event&nbsp;<I>e</I> is enabled
 * by all plant components  and disabled by some specification component,
 * then there is a site&nbsp;<I>C</I> that can disable the event&nbsp;<I>e</I>
 * and that has sufficient observation capability to make this disablement,
 * i.e., any trace accepted by the system that is indistinguishable
 * from&nbsp;<I>s</I> by&nbsp;<I>C</I> leads to a state where <I>e</I> is
 * disabled by at least one plant or specification component.</P>
 *
 * @see CoobservabilityAttributeFactory
 *
 * @author Robi Malik
 */

public interface CoobservabilityChecker extends ModelVerifier
{

  //#########################################################################
  //# Configuration


  //#########################################################################
  //# More Specific Access to the Results

}
