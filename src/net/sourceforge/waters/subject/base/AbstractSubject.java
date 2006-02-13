//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ImmutableSubject
//###########################################################################
//# $Id: AbstractSubject.java,v 1.4 2006-02-13 21:30:52 robi Exp $
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
  //# Equals and Hashcode
  /**
   * Checks whether two elements are equal.
   * This method implements content-based equality, i.e., two elements
   * will be equal if their contents are the same. This method can
   * be slow for large structures and therefore should be used with
   * care.
   * @see #equalsWithGeometry(Object) equalsWithGeometry()
   */
  public boolean equals(final Object partner)
  {
    return partner != null && getClass() == partner.getClass();
  }

  /**
   * Checks whether two elements are equal and have the same geometry
   * information. This method implements content-based equality, i.e., two
   * elements will be equal if their contents are the same. While the
   * standard {@link #equals(Object) equals()} method only considers structural
   * contents, this method also takes the layout information of graphical
   * objects such as nodes and edges into account. This method is very slow
   * for large structures and so far is only used for testing purposes.
   */
  public boolean equalsWithGeometry(final Object partner)
  {
    return equals(partner);
  }

  /**
   * Returns a hash code value for this element.
   * This is an implementation of the hashCode() function as documented in
   * the Java API. Care has been taken to satisfy the general hashCode()
   * contract, so the hash code does only depend on the immutable members
   * of an element. As a consequence, the method is not always as effective
   * as might be desired.
   */
  public int hashCode()
  {
    return getClass().hashCode();
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
  //# Printing
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }

  public String getShortClassName()
  {
    final Class clazz = getClass();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Data Members
  private Subject mParent;

}
