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

package net.sourceforge.waters.model.compiler.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
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
    mResultMap = new HashMap<Object,SourceInfo>();
    mParentSourceInfo = null;
  }

  public CompilationInfo(final boolean sourceInfoEnabled,
                         final boolean multiExceptionEnabled)
  {
    mExceptions = multiExceptionEnabled ? new MultiEvalException() : null;
    mResultMap = sourceInfoEnabled? new HashMap<Object,SourceInfo>() : null;
    mParentSourceInfo = null;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    dump(builder);
    return builder.toString();
  }


  //#########################################################################
  //# Configuration
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
  /**
   * Records source information without bindings for the given target.
   * @param  target  The output object to receive source information.
   * @param  source  The input object to be associated with the target.
   *                 If the compilation info contains source information for
   *                 the source, then the source's source object will
   *                 be associated with the target.
   * @return The created source information record, or <CODE>null</CODE>
   *         if source information is disabled.
   */
  public SourceInfo add(final Object target, final Proxy source)
  {
    return add(target, source, null);
  }

  /**
   * Records source information with bindings for the given target.
   * @param  target  The output object to receive source information.
   * @param  source  The input object to be associated with the target.
   *                 If the compilation info contains source information for
   *                 the source, then the source's source object will
   *                 be associated with the target.
   * @param  context The binding context with the values of any foreach-block
   *                 variables at the time this output is created.
   * @return The created source information record, or <CODE>null</CODE>
   *         if source information is disabled.
   */
  public SourceInfo add(final Object target,
                        final Proxy source,
                        final BindingContext context)
  {
    if (isSourceInfoEnabled()) {
      final SourceInfo info = createSourceInfo(source, context);
      add(target, info);
      return info;
    } else {
      return null;
    }
  }

  /**
   * Creates or retrieves a source information record without recording it.
   * If the given source has associated source information, the source's
   * source information is returned, otherwise a new source information
   * record is created. The parent and context information are combined from
   * the source's source information, the argument, and the currently set
   * parent ({@link #mParentSourceInfo}), but presently at least one of the
   * combined values must be <CODE>null</CODE>. The created source information
   * and can be filed later using {@link #add(Object, SourceInfo)}.
   * @param  source  The input object to be associated with the target.
   *                 If the compilation info contains source information for
   *                 the source, then the source's source object will
   *                 be used in the created source information.
   * @param  context The binding context with the values of any foreach-block
   *                 variables at the time this output is created, or
   *                 <CODE>null</CODE> to use the context of the source's
   *                 source.
   * @return A source information record, even if source information
   *         is disabled.
   */
  public SourceInfo createSourceInfo(Proxy source,
                                     BindingContext context)
  {
    final SourceInfo info = getSourceInfo(source);
    if (info == null) {
      return new SourceInfo(source, context, mParentSourceInfo);
    } else if ((mParentSourceInfo == null || mParentSourceInfo == info.getParent()) &&
               (context == null) || context == info.getBindingContext()) {
      return info;
    } else {
      assert info.getParent() == null || mParentSourceInfo == null :
        "Parent merging not implemented!";
      assert info.getBindingContext() == null || context == null :
        "Context merging not implemented!";
      source = info.getSourceObject();
      context = context == null ? info.getBindingContext() : context;
      final SourceInfo parent = mParentSourceInfo == null ? info.getParent() : mParentSourceInfo;
      return new SourceInfo(source, context, parent);
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

  /**
   * Returns the location of an error message to be associated with the
   * given output object. This method finds a root source object, i.e.,
   * and object within the originally compiled module that can be flagged
   * with an error in the editor.
   * @param  culprit  The object that caused the error.
   * @return The object in the original input module that can be flagged;
   *         or the argument if source information is disabled or could
   *         not be found for the argument.
   */
  public Proxy getErrorLocation(final Proxy culprit)
  {
    final SourceInfo info = getSourceInfo(culprit);
    return info == null ? culprit : info.getRoot().getSourceObject();
  }


  //#########################################################################
  //# Source Information Handling of Instantiations
  /**
   * Records the source information of the given instance object as the new
   * parent. The parent source information refers to an instance object
   * ({@link InstanceProxy}) that triggered compilation of a child module.
   * It will be added as the parent of every source information created
   * while compiling the child module.
   * @param  instance  Instance referring to a child being compiled.
   * @param  context   Binding context for variable in the instance.
   * @return The source information record used as the parent.
   * @see ModuleInstanceCompiler
   */
  public SourceInfo pushParentSourceInfo(final InstanceProxy instance,
                                         final BindingContext context)
  {
    mParentSourceInfo = new SourceInfo(instance, context, mParentSourceInfo);
    return mParentSourceInfo;
  }

  /**
   * Removes the current parent source information record.
   * This method assigns the parent of the current parent as the new parent.
   * @see #pushParentSourceInfo(InstanceProxy,BindingContext) pushParentSourceInfo()
   */
  public void popParentSourceInfo()
  {
    mParentSourceInfo = mParentSourceInfo.getParent();
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
   * information in {@link #mResultMap}. The exception's location is replaced
   * by the error location obtained from {@link #getErrorLocation(Proxy)
   * getErrorLocation()}.
   * @param exception The exception to be modified.
   */
  private void adjustLocation(final EvalException exception)
  {
    final Proxy location = exception.getLocation();
    final Proxy adjusted = getErrorLocation(location);
    if (adjusted != location) {
      exception.replaceLocation(adjusted);
    }
  }


  //#########################################################################
  //# Debugging
  public void dump(final StringBuilder builder)
  {
    if (mResultMap.isEmpty()) {
      builder.append("<empty compilation info>");
    } else {
      final List<String> lines = new ArrayList<>(mResultMap.size());
      for (final Entry<Object,SourceInfo> entry : mResultMap.entrySet()) {
        final StringBuilder line = new StringBuilder();
        final Object key = entry.getKey();
        ProxyTools.appendContainerName(key, line);
        line.append(" -> ");
        final SourceInfo info = entry.getValue();
        info.dump(line);
        lines.add(line.toString());
      }
      Collections.sort(lines);
      for (final String line : lines) {
        builder.append(line);
        builder.append("\n");
      }
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Multiple logged exceptions stored as a <CODE>MultiEvalException</CODE>.
   */
  private MultiEvalException mExceptions;

  /**
   * A map from target objects to their sources.
   */
  private Map<Object,SourceInfo> mResultMap;

  /**
   * The parent source information record, used by the {@link
   * ModuleInstanceCompiler} when compiling instances.
   * @see #pushParentSourceInfo(InstanceProxy,BindingContext) pushParentSourceInfo()
   */
  private SourceInfo mParentSourceInfo;

}
