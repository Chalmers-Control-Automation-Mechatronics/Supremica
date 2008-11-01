//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledParameterBinding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class CompiledParameterBinding
{

  //#########################################################################
  //# Constructors
  CompiledParameterBinding(final ParameterBindingProxy binding,
                           final Object value)
  {
    mBinding = binding;
    if (value instanceof SimpleExpressionProxy) {
      mSimpleValue = (SimpleExpressionProxy) value;
      mEventValue = null;
    } else if (value instanceof CompiledEvent) {
      mSimpleValue = null;
      mEventValue = (CompiledEvent) value;
    } else {
      throw new ClassCastException("Unknown type in parameter binding: " +
                                   value.getClass().getName() + "!");
    }
  }

  CompiledParameterBinding(final ParameterBindingProxy binding,
                           final SimpleExpressionProxy value)
  {
    mBinding = binding;
    mSimpleValue = value;
    mEventValue = null;
  }

  CompiledParameterBinding(final ParameterBindingProxy binding,
                           final CompiledEvent value)
  {
    mBinding = binding;
    mSimpleValue = null;
    mEventValue = value;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mBinding.getName();
  }

  ParameterBindingProxy getBinding()
  {
    return mBinding;
  }

  SimpleExpressionProxy getSimpleValue()
    throws TypeMismatchException
  {
    if (mSimpleValue != null) {
      return mSimpleValue;
    } else {
      throw new TypeMismatchException(mBinding, "simple expression");
    }
  }

  CompiledEvent getEventValue()
    throws TypeMismatchException
  {
    if (mEventValue != null) {
      return mEventValue;
    } else {
      throw new TypeMismatchException(mBinding, "event");
    }
  }


  //#########################################################################
  //# Data Members
  private final ParameterBindingProxy mBinding;
  private final SimpleExpressionProxy mSimpleValue;
  private final CompiledEvent mEventValue;

}
