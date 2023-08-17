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

package net.sourceforge.waters.subject.base;

import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Proxy} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface ProxySubject
  extends Proxy, Subject
{

  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this object.
   * Subjects are cloned by deep copying. All contained objects are
   * cloned as well, ensuring that all references within the clone point
   * to other objects of the clone. However, the cloned subject has no
   * parent, and observers registered on a subject are not transferred
   * to the clone.
   */
  public ProxySubject clone();

  /**
   * Creates assignment instructions to replace the contents of this list
   * by the contents of another list. This method attempts to produce a
   * minimal sequence of deletions and insertions to convert this set
   * to the given new set.
   * @param  newState An object containing the data after the intended
   *                  assignment.
   * @param  boundary Set of unchanged {@link Subject}s. Any children of
   *                  the receiving subject contained in this set will be
   *                  assumed and changed and not recursed into. The
   *                  boundary can be <CODE>null</CODE> to force full
   *                  recursion.
   * @return Undo information containing a minimal assignment sequence.
   */
  public UndoInfo createUndoInfo(ProxySubject newState,
                                 Set<? extends Subject> boundary);

}
