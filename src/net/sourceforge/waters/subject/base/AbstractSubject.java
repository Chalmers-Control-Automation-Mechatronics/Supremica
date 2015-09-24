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

package net.sourceforge.waters.subject.base;

import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
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
      final StringBuilder buffer = new StringBuilder();
      buffer.append("Trying to redefine parent of ");
      buffer.append(ProxyTools.getShortClassName(this));
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

  public UndoInfo createUndoInfo(final ProxySubject newState,
                                 final Set<? extends Subject> boundary)
  {
    if (boundary != null && boundary.contains(this)) {
      return null;
    }
    final RecursiveUndoInfo info = new RecursiveUndoInfo(this);
    collectUndoInfo(newState, info, boundary);
    if (info.isEmpty()) {
      return null;
    } else {
      return info;
    }
  }

  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    return null;
  }


  //#########################################################################
  //# Assignment
  protected void collectUndoInfo(final ProxySubject newState,
                                 final RecursiveUndoInfo info,
                                 final Set<? extends Subject> boundary)
  {
  }


  //#########################################################################
  //# Printing
  @Override
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }


  //#########################################################################
  //# Data Members
  private Subject mParent;

}
