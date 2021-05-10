//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.compiler;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * @author Robi Malik
 */

public class MultiExceptionModuleProxyVisitor
  extends DefaultModuleProxyVisitor
  implements Abortable
{
  //#########################################################################
  //# Constructor
  public MultiExceptionModuleProxyVisitor(final CompilationInfo info)
  {
    mCompilationInfo = info;
  }


  //#########################################################################
  //# Simple Access
  protected CompilationInfo getCompilationInfo()
  {
    return mCompilationInfo;
  }

  protected SourceInfo linkCompilationInfo(final Object target,
                                           final Proxy source)
  {
    return mCompilationInfo.add(target, source);
  }

  protected SourceInfo linkCompilationInfo(final Object target,
                                           final Proxy source,
                                           final BindingContext context)
  {
    return mCompilationInfo.add(target, source, context);
  }

  protected void linkCompilationInfo(final Object target,
                                     final SourceInfo source)
  {
    mCompilationInfo.add(target, source);
  }


  //#########################################################################
  //# Raising Exceptions
  protected void raise(final EvalException exception)
    throws EvalException
  {
    mCompilationInfo.raise(exception);
  }

  protected void recordCaughtException(final EvalException exception)
    throws EvalException
  {
    if (exception instanceof EvalAbortException) {
      throw exception;
    } else {
      mCompilationInfo.raise(exception);
    }
  }

  protected void recordCaughtException(final VisitorException exception,
                                       final SimpleExpressionProxy location)
    throws EvalException
  {
    final Throwable cause = exception.getCause();
    if (cause instanceof EvalException) {
      final EvalException evalCause = (EvalException) cause;
      evalCause.provideLocation(location);
      recordCaughtException(evalCause);
    } else {
      throw exception.getRuntimeException();
    }
  }

  protected void throwRecordedExceptions()
    throws EvalException
  {
    if (mCompilationInfo.hasExceptions()) {
      throw mCompilationInfo.getExceptions();
    }
  }


  protected void raiseInVisitor(final EvalException exception)
    throws VisitorException
  {
    mCompilationInfo.raiseInVisitor(exception);
  }

  protected void recordCaughtExceptionInVisitor
    (final VisitorException exception)
    throws VisitorException
  {
    final Throwable cause = exception.getCause();
    if ((cause instanceof EvalException) &&
        !(cause instanceof EvalAbortException)) {
      mCompilationInfo.raiseInVisitor((EvalException) cause);
    } else {
      throw exception;
    }
  }

  protected void throwRecordedExceptionsInVisitor()
    throws VisitorException
  {
    if (mCompilationInfo.hasExceptions()) {
      throw wrap(mCompilationInfo.getExceptions());
    }
  }

  protected void throwAsEvalException(final VisitorException exception)
    throws EvalException
  {
    final Throwable cause = exception.getCause();
    if (cause instanceof EvalException) {
      throw (EvalException) cause;
    } else {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.base.DefaultProxyVisitor
  @Override
  public Object visitCollection(final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    boolean hasExceptions = false;
    for (final Proxy proxy : collection) {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        recordCaughtExceptionInVisitor(exception);
        hasExceptions = true;
      }
    }
    if (hasExceptions) {
      throwRecordedExceptionsInVisitor();
    }
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }

  protected void checkAbort()
    throws EvalAbortException
  {
    if (mIsAborting) {
      throw new EvalAbortException();
    }
  }

  protected void checkAbortInVisitor()
    throws VisitorException
  {
    if (mIsAborting) {
      final EvalAbortException exception = new EvalAbortException();
      throw new VisitorException(exception);
    }
  }


  //#########################################################################
  //# Data Members
  private final CompilationInfo mCompilationInfo;
  private boolean mIsAborting;

}
