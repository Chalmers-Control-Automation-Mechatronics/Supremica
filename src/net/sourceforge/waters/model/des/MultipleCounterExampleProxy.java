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

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;

/**
 * <P>A counterexample consisting of two traces.</P>
 *
 * <P>This class is used by model verifiers for several properties of discrete
 * event system, particularly does concerned with observability of events,
 * that are refuted by a combination of several traces.</P>
 *
 * <UL>
 * <LI>A <I>coobservability</I> counterexample consists of a first trace
 * representing the system behaviour followed by further traces representing
 * supervisor sites. The system behaviour trace takes the plant and specification
 * to a state where some event is enabled by the plant but disabled by the
 * specification, with that last event included in the trace. Each of the
 * supervisor site traces is labelled by the name of a supervisor (through
 * {@link TraceProxy#getName()}) that can disable the last event of the system
 * trace and contains a sequence of events that is indistinguishable from the
 * system behaviour trace based on the event observability of its supervisor
 * site. It takes the specification to a state where the last event of the
 * system behaviour trace (also included in the trace) is enabled.</LI>
 * </UL>
 *
 * @author Robi Malik
 * @see CoobservabilityChecker
 */

public interface MultipleCounterExampleProxy
extends CounterExampleProxy
{
}
