//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.AliasBindingContext;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfo;


/**
 * The compiler-internal algorithm to obtain a list of event output objects
 * ({@link SingleEventOutput}) from an any given compiled event ({@link
 * CompiledEvent}).
 *
 * @author Robi Malik
 */

class EventOutputIterable implements Iterable<SingleEventOutput>
{

  //#########################################################################
  //# Constructor
  EventOutputIterable(final CompiledEvent event)
  {
    this(event, null);
  }

  EventOutputIterable(final CompiledEvent event,
                      final CompilationInfo info)
  {
    mEvent = event;
    mCompilationInfo = info;
  }


  //#########################################################################
  //# Interface java.lang.Iterable
  @Override
  public Iterator<SingleEventOutput> iterator()
  {
    return createEventOutputIterator(mEvent);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Iterator<SingleEventOutput> createEventOutputIterator
    (final CompiledEvent event)
  {
    return createEventOutputIterator(event, null);
  }

  private Iterator<SingleEventOutput> createEventOutputIterator
    (final CompiledEvent event, final SourceInfo outerInfo)
  {
    final SourceInfo localInfo = event.getSourceInfo();
    final SourceInfo info;
    if (mCompilationInfo == null) {
      info = null;
    } else if (localInfo == null) {
      info = outerInfo;
    } else if (outerInfo == null) {
      info = localInfo;
    } else {
      final Proxy source = localInfo.getSourceObject();
      final BindingContext context = localInfo.getBindingContext();
      final BindingContext alias =
        new AliasBindingContext(outerInfo, context);
      info = mCompilationInfo.createSourceInfo(source, alias);
    }
    if (event instanceof CompiledSingleEvent) {
      final CompiledSingleEvent single = (CompiledSingleEvent) event;
      return new SingleEventOutputIterator(single, info);
    } else {
      return new NestedEventOutputIterator(event, info);
    }
  }


  //#########################################################################
  //# Local Class SingleEventOutputIterator
  private static class SingleEventOutputIterator
    implements Iterator<SingleEventOutput>
  {

    //#######################################################################
    //# Constructor
    SingleEventOutputIterator(final CompiledSingleEvent event,
                              final SourceInfo info)
    {
      mOutput = new SingleEventOutput(event, info);
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      return mOutput != null;
    }

    @Override
    public SingleEventOutput next()
    {
      if (mOutput != null) {
        final SingleEventOutput output = mOutput;
        mOutput = null;
        return output;
      } else {
        throw new NoSuchElementException
          ("No more events in single event output iteration!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("Can't remove from single event output iteration!");
    }

    //#######################################################################
    //# Data Members
    private SingleEventOutput mOutput;
  }


  //#########################################################################
  //# Local Class NestedEventOutputIterator
  private class NestedEventOutputIterator
    implements Iterator<SingleEventOutput>
  {

    //#######################################################################
    //# Constructor
    NestedEventOutputIterator(final CompiledEvent event,
                              final SourceInfo info)
    {
      mSourceInfo = info;
      mOuterIterator = event.getChildrenIterator();
      mInnerIterator = null;
      advance();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    @Override
    public boolean hasNext()
    {
      return mOuterIterator != null;
    }

    @Override
    public SingleEventOutput next()
    {
      if (mOuterIterator != null) {
        final SingleEventOutput output = mInnerIterator.next();
        advance();
        return output;
      } else {
        throw new NoSuchElementException
          ("No more events in nested event output iteration!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("Can't remove from nested event output iteration!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mInnerIterator == null || !mInnerIterator.hasNext()) {
        if (mOuterIterator.hasNext()) {
          final CompiledEvent event = mOuterIterator.next();
          mInnerIterator = createEventOutputIterator(event, mSourceInfo);
        } else {
          mOuterIterator = null;
          mInnerIterator = null;
          return;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final SourceInfo mSourceInfo;
    private Iterator<? extends CompiledEvent> mOuterIterator;
    private Iterator<SingleEventOutput> mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final CompiledEvent mEvent;
  private final CompilationInfo mCompilationInfo;

}
