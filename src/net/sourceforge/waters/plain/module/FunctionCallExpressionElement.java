//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# THIS FILE HAS BEEN AUTOMATICALLY GENERATED BY A SCRIPT.
//# DO NOT EDIT.
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

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link FunctionCallExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class FunctionCallExpressionElement
  extends SimpleExpressionElement
  implements FunctionCallExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new function call expression.
   * @param plainText The original text of the new function call expression, or <CODE>null</CODE>.
   * @param functionName The function name of the new function call expression.
   * @param arguments The arguments of the new function call expression, or <CODE>null</CODE> if empty.
   */
  public FunctionCallExpressionElement(final String plainText,
                                       final String functionName,
                                       final Collection<? extends SimpleExpressionProxy> arguments)
  {
    super(plainText);
    mFunctionName = functionName;
    if (arguments == null) {
      mArguments = Collections.emptyList();
    } else {
      final List<SimpleExpressionProxy> argumentsModifiable =
        new ArrayList<SimpleExpressionProxy>(arguments);
      mArguments =
        Collections.unmodifiableList(argumentsModifiable);
    }
  }

  /**
   * Creates a new function call expression using default values.
   * This constructor creates a function call expression with
   * the original text set to <CODE>null</CODE>.
   * @param functionName The function name of the new function call expression.
   * @param arguments The arguments of the new function call expression, or <CODE>null</CODE> if empty.
   */
  public FunctionCallExpressionElement(final String functionName,
                                       final Collection<? extends SimpleExpressionProxy> arguments)
  {
    this(null,
         functionName,
         arguments);
  }


  //#########################################################################
  //# Cloning
  @Override
  public FunctionCallExpressionElement clone()
  {
    return (FunctionCallExpressionElement) super.clone();
  }


  //#########################################################################
  //# Comparing
  public Class<FunctionCallExpressionProxy> getProxyInterface()
  {
    return FunctionCallExpressionProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitFunctionCallExpressionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.FunctionCallExpressionProxy
  public String getFunctionName()
  {
    return mFunctionName;
  }

  public List<SimpleExpressionProxy> getArguments()
  {
    return mArguments;
  }


  //#########################################################################
  //# Data Members
  private final String mFunctionName;
  private final List<SimpleExpressionProxy> mArguments;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4647733522263092637L;

}
