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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.analysis.diagnosis.DiagnosabilityAttributeFactory;

/**
 * <P>A model verifier that checks whether a system of composed automata
 * is <I>diagnosable</I>.</P>
 *
 * <P>A diagnosability checker searches the input model for unobservable
 * events marked with the FAULT attribute. Events with this attribute are
 * considered as faults, and the attribute value defines the fault class.
 * Different fault events with the same attribute value belong to the same
 * fault class.</P>
 *
 * <P>A model is diagnosable with respect to a fault class, if for each trace
 * that includes an event with this fault class, it is guaranteed that the
 * fault is detected eventually. That is, there exists a maximum number of
 * steps, such that all continuations of this length with equal projection
 * to observable events include a fault of the same class.</P>
 *
 * <P><I>Reference:</I> M. Sampath, R. Sengupta, S. Lafortune,
 * K. Sinnamohideen, D. Teneketzis. Diagnosability of discrete-event systems.
 * IEEE Transactions on Automatic Control, <STRONG>40</STRONG>(9),
 * 1555&ndash;1575, 1995.</P>
 *
 * @see DiagnosabilityAttributeFactory
 *
 * @author Robi Malik
 */

public interface DiagnosabilityChecker extends ModelVerifier
{

}
