//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   OverflowKind
//###########################################################################
//# $Id$
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
  MEMORY;


  //#########################################################################
  //# Display
  String getMessage(final int limit)
  {
    final StringBuffer buffer = new StringBuffer();
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
