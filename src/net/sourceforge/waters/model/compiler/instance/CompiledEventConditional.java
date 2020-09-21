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

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A compiler-internal object representing a conditional block
 * ({@link ConditionalProxy}) within a list of compiled events.</P>
 *
 * @author Robi Malik
 */

class CompiledEventConditional implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledEventConditional(final SimpleExpressionProxy condition,
                           final CompiledEventList body)
  {
    mCondition = condition;
    mBody = body;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.CompiledEvent
  public int getAllowedKindMask()
  {
    return mBody.getAllowedKindMask();
  }

  @Override
  public int getKindMask()
  {
    return mBody.getKindMask();
  }

  @Override
  public boolean isObservable()
  {
    return mBody.isObservable();
  }

  @Override
  public boolean hasConditional()
  {
    return true;
  }

  @Override
  public List<CompiledRange> getIndexRanges()
  {
    return Collections.emptyList();
  }

  @Override
  public CompiledEvent find(final SimpleExpressionProxy index)
    throws EvalException
  {
    throw new IndexOutOfRangeException(this);
  }

  @Override
  public SourceInfo getSourceInfo()
  {
    return null;
  }

  @Override
  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return mBody.getChildrenIterator();
  }


  //#########################################################################
  //# Simple Access
  public SimpleExpressionProxy getCondition()
  {
    return mCondition;
  }

  public CompiledEventList getBody()
  {
    return mBody;
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mCondition;
  private final CompiledEventList mBody;

}
