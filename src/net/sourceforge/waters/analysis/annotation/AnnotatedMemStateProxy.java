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

package net.sourceforge.waters.analysis.annotation;

import net.sourceforge.waters.model.des.EventProxy;
import java.util.Collection;
import java.util.Collections;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import gnu.trove.set.hash.THashSet;
import java.util.Set;


public class AnnotatedMemStateProxy implements StateProxy
{
  private final int mName;
  private final boolean mIsInitial;
  private final Collection<EventProxy> mProps;

  public AnnotatedMemStateProxy(final int name,
                                final Collection<EventProxy> props,
                                final boolean isInitial)
  {
    mName = name;
    mProps = props;
    mIsInitial = isInitial;
  }

  public AnnotatedMemStateProxy(final int name, final EventProxy marked,
                                final boolean isInitial)
  {
    this(name, marked == null ? new THashSet<EventProxy>() : Collections
        .singleton(marked), isInitial);
  }

  public AnnotatedMemStateProxy(final int name, final EventProxy marked)
  {
    this(name, Collections.singleton(marked), false);
  }

  public AnnotatedMemStateProxy(final int name)
  {
    this(name, getRightType(), false);
  }

  private static Set<EventProxy> getRightType()
  {
    final Set<EventProxy> empty = Collections.emptySet();
    return empty;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mProps;
  }

  public boolean isInitial()
  {
    return mIsInitial;
  }

  public int getNum()
  {
    return mName;
  }

  public AnnotatedMemStateProxy clone()
  {
    return new AnnotatedMemStateProxy(mName, mProps, mIsInitial);
  }

  public String getName()
  {
    return Integer.toString(mName);
  }

  public boolean refequals(final Object o)
  {
    if (o instanceof NamedProxy) {
      return refequals((NamedProxy) o);
    }
    return false;
  }

  public boolean refequals(final NamedProxy o)
  {
    if (o instanceof AnnotatedMemStateProxy) {
      final AnnotatedMemStateProxy s = (AnnotatedMemStateProxy) o;
      return s.mName == mName;
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return mName;
  }

  public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }

  public Class<StateProxy> getProxyInterface()
  {
    return StateProxy.class;
  }

  public int compareTo(final NamedProxy n)
  {
    return n.getName().compareTo(getName());
  }

  public String toString()
  {
    return "S:" + mName;
  }
}
