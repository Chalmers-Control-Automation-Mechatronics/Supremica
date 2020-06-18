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

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.Proxy;


/**
 * A utility class to navigate subject hierarchies.
 * This class contains a few static methods that enable navigation to the
 * parents and ancestors of subjects in ways not provided by the simple
 * {@link Subject} interface.
 *
 * @author Robi Malik
 */

public class SubjectTools
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of class.
   */
  private SubjectTools()
  {
  }


  //#########################################################################
  //# Advanced Hierarchy Navigation
  /**
   * Gets the closest proper ancestor of the given subject that implements the
   * {@link Proxy} interface.
   * @return The closest proper ancestor of type {@link Proxy}
   *         or <CODE>null</CODE>.
   */
  public static ProxySubject getProxyParent(Subject subject)
  {
    do {
      subject = subject.getParent();
      if (subject instanceof Proxy) {
        return (ProxySubject) subject;
      }
    } while (subject != null);
    return null;
  }

  /**
   * Checks whether a subject is an ancestor of another.
   * @return <CODE>true</CODE> if the given ancestor is equal to
   *         the given descendant or one of the descendant's parents.
   */
  public static boolean isAncestor(final Subject ancestor,
                                   final Subject descendant)
  {
    if (ancestor == descendant) {
      return true;
    } else {
      final Subject parent = descendant.getParent();
      return parent == null ? false : isAncestor(ancestor, parent);
    }
  }

  /**
   * Finds the closest ancestor of the given subject,
   * which is an instance of the given class.
   * Possible ancestors include the object itself.
   * @return The closest ancestor of the given subject, which can be assigned
   *         to a variable of the given class, or <CODE>null</CODE> if no such
   *         ancestor can be found.
   */
  public static <S extends Subject> S
    getAncestor(Subject subject, final Class<? extends S> clazz)
  {
    do {
      final Class<? extends Subject> current = subject.getClass();
      if (clazz.isAssignableFrom(current)) {
        return clazz.cast(subject);
      }
      subject = subject.getParent();
    } while (subject != null);
    return null;
  }

  /**
   * Finds the closest ancestor, which is an instance of one of two given
   * classes. Possible ancestors include the object itself.
   * @return The closest ancestor of this subject, which can be assigned to
   *         a variable one of the given classes, or <CODE>null</CODE> if
   *         no such ancestor can be found.
   */
  public static Subject getAncestor(Subject subject,
                                    final Class<? extends Subject> clazz1,
                                    final Class<? extends Subject> clazz2)
  {
    do {
      final Class<? extends Subject> current = subject.getClass();
      if (clazz1.isAssignableFrom(current) ||
          clazz2.isAssignableFrom(current)) {
        return subject;
      }
      subject = subject.getParent();
    } while (subject != null);
    return null;
  }

}
