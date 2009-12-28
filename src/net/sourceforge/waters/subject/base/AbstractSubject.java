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
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * <P>The common base class for all Waters elements in the <I>subject</I>
 * implementation.</P>
 *
 * <P>This is the abstract base class of all immutable Waters elements in
 * the <I>subject</I> implementation. It provides the basic functionality
 * to access the parent. The listeners are implemented in two different
 * ways in the subclasses {@link ImmutableSubject} and {@link
 * MutableSubject}.
 *
 * @author Robi Malik
 */

public abstract class AbstractSubject
  implements ProxySubject, Cloneable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected AbstractSubject()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected AbstractSubject(final Proxy partner)
  {
  }


  //#########################################################################
  //# Cloning
  public AbstractSubject clone()
  {
    try {
      final AbstractSubject cloned = (AbstractSubject) super.clone();
      cloned.mParent = null;
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public Subject getParent()
  {
    return mParent;
  }

  public DocumentSubject getDocument()
  {
    if (mParent != null) {
      return mParent.getDocument();
    } else {
      return null;
    }
  }

  public void setParent(final Subject parent)
  {
    checkSetParent(parent);
    mParent = parent;
  }

  public void checkSetParent(final Subject parent)
  {
    if (parent != null && mParent != null) {
      final StringBuffer buffer = new StringBuffer();
      buffer.append("Trying to redefine parent of ");
      buffer.append(getShortClassName());
      if (this instanceof NamedProxy) {
        final NamedProxy named = (NamedProxy) this;
        buffer.append(" '");
        buffer.append(named.getName());
        buffer.append('\'');
      }
      buffer.append('!');
      throw new IllegalStateException(buffer.toString());
    }
  }

  public void fireModelChanged(final ModelChangeEvent event)
  {
    if (mParent != null) {
      mParent.fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Advanced Hierarchy Navigation
  /**
   * Gets the closest proper ancestor of this subject that implements the
   * {@link Proxy} interface.
   * @return The closest propert ancestor of type {@link Proxy}
   *         or <CODE>null</CODE>.
   */
  public AbstractSubject getProxyParent()
  {
    Subject subject = getParent();
    while (subject != null) {
      if (subject instanceof Proxy) {
        return (AbstractSubject) subject;
      }
      subject = subject.getParent();
    }
    return null;
  }

  /**
   * Finds the closest ancestor, which is an instance of the given class.
   * Possible ancestors include the object itself.
   * @return The closest ancestor of this subject, which can be assigned to
   *         a variable of the given class, or <CODE>null</CODE> if no such
   *         ancestor can be found.
   */
  public <S extends Subject> S getAncestor(final Class<? extends S> clazz)
  {
    Subject subject = this;
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
   * Finds the closest ancestor, which is an instance of one of two givenn
   * class.  Possible ancestors include the object itself.
   * @return The closest ancestor of this subject, which can be assigned to
   *         a variable one of the given classes, or <CODE>null</CODE> if
   *         no such ancestor can be found.
   */
  public Subject getAncestor(final Class<? extends Subject> clazz1,
                             final Class<? extends Subject> clazz2)
  {
    Subject subject = this;
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


  //#########################################################################
  //# Printing
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }

  public String getShortClassName()
  {
    return getShortClassName(this);
  }

  public static String getShortClassName(final Object object)
  {
    final Class<?> clazz = object.getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Data Members
  private Subject mParent;

}
