//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.analysis;


/**
 * Enumeration of possible overflow causes.
 * This class is used to describe the cause of an {@link OverflowException}
 * more specifically.
 *
 * @see OverflowException
 * @author Robi Malik
 */

public enum OverflowKind {

  //#########################################################################
  //# Enumeration Values
  /**
   * Overflow by constructing an automaton with too many states.
   */
  STATE,
  /**
   * Overflow by constructing an automaton with too many transitions.
   */
  TRANSITION,
  /**
   * Overflow by constructing too many nodes.
   * This may be used by BDDs or other symbolic algorithms
   */
  NODE,
  /**
   * Overflow after catching {@link OutOfMemoryError}.
   */
  MEMORY,
  /**
   * Overflow after catching {@link StackOverflowError}.
   */
  STACK;


  //#########################################################################
  //# Display
  String getMessage(final int limit)
  {
    final StringBuilder buffer = new StringBuilder();
    final String name = toString();
    final int namelen = name.length();
    buffer.append(name.charAt(0));
    for (int i = 1; i < namelen; i++) {
      final char ch = name.charAt(i);
      buffer.append(Character.toLowerCase(ch));
    }
    buffer.append(" limit ");
    if (limit >= 0) {
      buffer.append("of ");
      buffer.append(limit);
      buffer.append(' ');
      for (int i = 0; i < namelen; i++) {
        final char ch = name.charAt(i);
        buffer.append(Character.toLowerCase(ch));
      }
      buffer.append("s ");
    }
    buffer.append("exceeded!");
    return buffer.toString();
  }

}
