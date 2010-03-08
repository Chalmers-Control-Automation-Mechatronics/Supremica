//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   AbstractSubject
//###########################################################################
//# $Id$
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
  public static Subject getProxyParent(Subject subject)
  {
    do {
      subject = subject.getParent();
      if (subject instanceof Proxy) {
        return subject;
      }
    } while (subject != null);
    return null;
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
   * class.  Possible ancestors include the object itself.
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
