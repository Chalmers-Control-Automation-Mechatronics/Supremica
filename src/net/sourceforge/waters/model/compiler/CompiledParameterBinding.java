//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledParameterBinding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


class CompiledParameterBinding
{

  //#########################################################################
  //# Constructors
  CompiledParameterBinding(final ParameterBindingProxy binding,
                           final Value value)
  {
    mBinding = binding;
    mValue = value;
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

  Value getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Data Members
  private final ParameterBindingProxy mBinding;
  private final Value mValue;

}
