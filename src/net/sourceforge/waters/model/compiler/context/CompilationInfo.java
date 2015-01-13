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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;
import net.sourceforge.waters.model.module.InstanceProxy;

/**
 * A utility class that helps to collect source information records and
 * compilation errors while compiling a module.
 *
 * @author Robi Malik, Tom Levy, Roger Su
 */

public class CompilationInfo
{
  //#########################################################################
  //# Constructor
  public CompilationInfo()
  {
    mExceptions = new MultiEvalException();
    mInstanceStack = new LinkedList<InstanceProxy>();
    mResultMap = new HashMap<Object,SourceInfo>();
  }

  public CompilationInfo(final boolean sourceInfoEnabled,
                         final boolean multiExceptionEnabled)
  {
    mExceptions = multiExceptionEnabled ? new MultiEvalException() : null;
    mInstanceStack = sourceInfoEnabled ? new LinkedList<InstanceProxy>() : null;
    mResultMap = sourceInfoEnabled? new HashMap<Object,SourceInfo>() : null;
  }


  //#########################################################################
  //# Configuation
  public boolean isSourceInfoEnabled()
  {
    return mResultMap != null;
  }

  public boolean isMultiExceptionsEnabled()
  {
    return mExceptions != null;
  }

  public void setSourceInfoEnabled(final boolean enabled)
  {
    if (enabled) {
      if (mResultMap == null)
        mResultMap = new HashMap<Object,SourceInfo>();
    }
    else
      mResultMap = null;
  }

  public void setMultiExceptionsEnabled(final boolean enabled)
  {
    if (enabled) {
      if (mExceptions == null)
        mExceptions = new MultiEvalException();
    }
    else
      mExceptions = null;
  }

  //#########################################################################
  //# Access
  public void add(final Object target, final Proxy source)
  {
    if (isSourceInfoEnabled())
    {
      if (!stackIsEmpty()) {
        add(target, getStackBase(), null);
      } else {
        add(target, source, null);
      }
    }
  }

  private void add(final Object target, final Proxy source,
                   final BindingContext context)
  {
    if (isSourceInfoEnabled())
    {
      SourceInfo info = getSourceInfo(source);
      if (info == null) {
        info = new SourceInfo(source, context);
      } else {
        if (context != null)
          info = new SourceInfo(info.getSourceObject(), context);
      }
      add(target, info);
    }
  }

  public void add(final Object target, final SourceInfo info)
  {
    if (isSourceInfoEnabled())
      mResultMap.put(target, info);
  }

  public Map<Object,SourceInfo> getResultMap()
  {
    return mResultMap;
  }

  public SourceInfo getSourceInfo(final Object target)
  {
    if (mResultMap != null && mResultMap.containsKey(target))
      return mResultMap.get(target);
    else
      return null;
  }


  //#########################################################################
  //# Source Information Handling of Instantiations
  /**
   * Adds the instance pointer to the stack.
   *
   * @param inst The instance pointer
   * @see #mInstanceStack
   */
  public void addCurrentInstance(final InstanceProxy inst)
  {
    if (mInstanceStack != null)
      mInstanceStack.add(inst);
  }

  /**
   * Removes the last item from the stack.
   *
   * @see #mInstanceStack
   */
  public void removeCurrentInstance()
  {
    if (mInstanceStack != null && mInstanceStack.size() > 0)
      mInstanceStack.remove(mInstanceStack.size()-1);
  }

  /**
   * Returns the base item of the stack, which is the base module.
   *
   * @return The base module
   */
  public InstanceProxy getStackBase()
  {
    return mInstanceStack.get(0);
  }

  /**
   * Tests whether the stack is empty.
   * <p>
   * In other words, this tests whether the module of interest is an
   * instantiation or not.
   *
   * @return <CODE>true</CODE> if the stack is empty, which means that the
   *                           module is not an instantiation;<br>
   *        <CODE>false</CODE> if the stack is not empty, which means that
   *                           the module is an instantiation.
   */
  public boolean stackIsEmpty()
  {
    return mInstanceStack.isEmpty();
  }


  //#########################################################################
  //# Exception Handling
  /**
   * Signals that a compilation error occurred.
   * <p>
   * The exception's location is adjusted if source information is enabled.
   * <p>
   * If multiple exceptions are enabled, and the exception is not an abort
   * exception, then the exception is logged and the method returns normally.
   * Otherwise, the exception is thrown.
   * <p>
   * Calling this method with the <CODE>MultiEvalException</CODE> of this
   * instance is permitted and has no effect.
   *
   * @param exception The compilation exception.
   * @throws EvalException if multiple exception are disabled.
   */
  public void raise(final EvalException exception) throws EvalException
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
   * Tests whether there is any exception accumulated in the
   * list {@link #mExceptions}.
   *
   * @return <CODE>true</CODE> if there is any accumulated exception, or
   *        <CODE>false</CODE> otherwise.
   */
  public boolean hasExceptions()
  {
    if (mExceptions == null)
      return false;
    else
      return mExceptions.hasException();
  }

  /**
   * Returns the <CODE>MultiEvalException</CODE> containing all the exceptions that
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
   * Sets the <CODE>MultiEvalException</CODE> containing all the exceptions that
   * occurred so far.
   *
   * @param exceptions The new <CODE>MultiEvalException</CODE> to use, or
   *                   <CODE>null</CODE> to disable multiple exceptions.
   */
  public void setExceptions(final MultiEvalException exceptions)
  {
    mExceptions = exceptions;
  }

  /**
   * Adjusts the location of a given {@link EvalException} according to the
   * information in {@link #mResultMap}.
   *
   * @param e The exception to be modified
   */
  private void adjustLocation(final EvalException ex)
  {
    final Proxy location = ex.getLocation();
    final SourceInfo info = getSourceInfo(location);
    if (info != null)
      ex.replaceLocation(info.getSourceObject());
  }


  //#########################################################################
  //# Data Members
  /**
   * Multiple logged exceptions stored as a <CODE>MultiEvalException</CODE>.
   */
  private MultiEvalException mExceptions;

  /**
   * Used by the {@link ModuleInstanceCompiler}, this list keeps track of
   * nested instances.
   * <p>
   * The first item of this list is the actual module, while the rest of the
   * existing items are the nested instances.
   */
  private final List<InstanceProxy> mInstanceStack;

  /**
   * A map from target objects to their sources.
   */
  private Map<Object,SourceInfo> mResultMap;
}