//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompilationInfo
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;

/**
 * A utility class that helps to collect source information records and
 * compilation errors while compiling a module.
 *
 * @author Robi Malik, Tom Levy
 */

public class CompilationInfo
{

  //#########################################################################
  //# Constructor
  /**
   * Creates compilation information.
   *
   * @param sourceInfoEnabled       If source information should be enabled.
   * @param multiExceptionsEnabled  If multiple exceptions should be enabled.
   */
  public CompilationInfo(final boolean sourceInfoEnabled,
                         final boolean multiExceptionsEnabled)
  {
    mResultMap = sourceInfoEnabled ? new HashMap<Object,SourceInfo>() : null;
    mExceptions = multiExceptionsEnabled ? new MultiEvalException() : null;
  }


  //#########################################################################
  //# Access
  public boolean isSourceInfoEnabled()
  {
    return mResultMap != null;
  }

  public boolean isMultiExceptionsEnabled()
  {
    return mExceptions != null;
  }

  public void add(final Object target, final Proxy source)
  {
    add(target, source, null);
  }

  public void add(final Object target,
                  final Proxy source,
                  final BindingContext context)
  {
    if (isSourceInfoEnabled()) {
      SourceInfo info = getSourceInfo(source);
      if (info == null) {
          info = new SourceInfo(source, context);
          add(target, info);
      } else {
        if (context != null) {
          info = new SourceInfo(info.getSourceObject(), context);
        }
        add(target, info);
      }
    }
  }

  public void add(final Object target, final SourceInfo info)
  {
    if (isSourceInfoEnabled()) {
      mResultMap.put(target, info);
    }
  }

  public Map<Object,SourceInfo> getResultMap()
  {
    return mResultMap;
  }

  public SourceInfo getSourceInfo(final Object target)
  {
    if (mResultMap != null && mResultMap.containsKey(target)) {
      return mResultMap.get(target);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Exception Handling
  /**
   * Signals that a compilation error occurred.
   *
   * The exception's location is adjusted if source information is enabled.
   *
   * If multiple exceptions are enabled (and the exception is not an abort
   * exception) the exception is logged and the method returns normally.
   * Otherwise the exception is thrown.
   *
   * Calling this method with the <CODE>MultiEvalException</CODE> of this
   * instance is permitted and has no effect.
   *
   * @param exception The compilation exception.
   * @throws EvalException if multiple exception are disabled.
   */
  public void raise(final EvalException exception)
    throws EvalException
  {
    if (isSourceInfoEnabled()) {
      adjustLocation(exception);
    }
    final boolean abort = exception instanceof EvalAbortException;
    if (isMultiExceptionsEnabled() && !abort) {
      mExceptions.add(exception);
    } else {
      throw exception;
    }
  }

  /**
   * Same as {@link #raise(EvalException)}, but if the exception is thrown it
   * is wrapped in a VisitorException.
   *
   * @param exception The compilation exception.
   * @throws VisitorException if multiple exception are disabled.
   * @see #raise(EvalException)
   */
  public void raiseInVisitor(final EvalException exception)
    throws VisitorException
  {
    try {
      raise(exception);
    } catch (final EvalException e) {
      throw new VisitorException(e);
    }
  }

  /**
   * Tests whether there is any exception accumulated in
   * the list {@link #mExceptions}.
   *
   * @return <code>true</code> if there is any accumulated exception, or
   *        <code>false</code> otherwise.
   */
  public boolean hasExceptions()
  {
    if (mExceptions == null)
      return false;
    else
      return mExceptions.hasException();
  }

  /**
   * Returns the {@link MultiEvalException} containing all the exceptions that
   * occurred so far.
   *
   * @return The <CODE>MultiEvalException</CODE>, or <CODE>null</CODE> if
   *         multiple exceptions are disabled.
   */
  public MultiEvalException getExceptions()
  {
    return mExceptions;
  }

  /**
   * Sets the {@link MultiEvalException} containing all the exceptions that
   * occurred so far.
   *
   * @param exceptions The new <CODE>MultiEvalException</CODE> to use, or
   *                   <CODE>null</CODE> to disable multiple exceptions.
   */
  public void setExceptions(final MultiEvalException exceptions)
  {
    mExceptions = exceptions;
  }

  private void adjustLocation(final EvalException e)
  {
    final Proxy location = e.getLocation();
    final SourceInfo info = getSourceInfo(location);
    if (info != null) {
      e.replaceLocation(info.getSourceObject());
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<Object,SourceInfo> mResultMap;
  private MultiEvalException mExceptions;

}