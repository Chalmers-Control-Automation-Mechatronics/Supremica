//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A compiler-internal object representing a list of compiled
 * events.</P>
 *
 * <P>Event lists are collections of events corresponding to the edges of
 * graphs, where each event in the list produces its own transition.  They
 * also occur as values of event aliases or the actual parameters for
 * module instantiations</P>
 *
 * <P>Technically, an event list value is an implementation of the {@link
 * net.sourceforge.waters.model.compiler.instance.CompiledEvent} interface
 * with additional capability to access controllability information, and
 * the list of contained events.</P>
 *
 * @author Robi Malik
 */

class CompiledEventList implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledEventList()
  {
    this(EventKindMask.TYPEMASK_ANY);
  }

  CompiledEventList(final int allowedKindMask)
  {
    mList = new LinkedList<CompiledEvent>();
    mAllowedKindMask = allowedKindMask;
    mKindMask = 0;
    mIsObservable = true;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.CompiledEvent
  public int getKindMask()
  {
    return mKindMask;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public List<CompiledRange> getIndexRanges()
  {
    final CompiledEvent event = getSingleEvent();
    if (event == null) {
      return Collections.emptyList();
    } else {
      return event.getIndexRanges();
    }
  }

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws EvalException
  {
    final CompiledEvent event = getSingleEvent();
    if (event == null) {
      throw new IndexOutOfRangeException(this);
    } else {
      return event.find(index);
    }
  }

  public SourceInfo getSourceInfo()
  {
    return null;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return mList.iterator();
  }


  //#########################################################################
  //# Specific Access
  void addEvent(final CompiledEvent value)
    throws EventKindException
  {
    final int mask = value.getKindMask();
    final int badmask = mask & ~mAllowedKindMask;
    if (badmask != 0) {
      throw new EventKindException(value, badmask);
    }
    mKindMask |= mask;
    mIsObservable &= value.isObservable();
    mList.add(value);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledEvent getSingleEvent()
  {
    final Iterator<CompiledEvent> iter = mList.iterator();
    if (iter.hasNext()) {
      final CompiledEvent event = iter.next();
      return iter.hasNext() ? null : event;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final List<CompiledEvent> mList;
  private final int mAllowedKindMask;
  private int mKindMask;
  private boolean mIsObservable;

}
