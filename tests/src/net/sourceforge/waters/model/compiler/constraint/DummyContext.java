//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DummyContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.constraint;

import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DummyContext implements VariableContext
{

  //#########################################################################
  //# Constructor
  DummyContext()
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mAtoms = new ProxyAccessorHashSet<>(eq);
    mRangeMap = new ProxyAccessorHashMap<>(eq);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
  @Override
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    return null;
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mAtoms.containsProxy(ident);
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
   return mRangeMap.getByProxy(varname);
  }

  @Override
  public int getNumberOfVariables()
  {
    return mRangeMap.size();
  }


  //#########################################################################
  //# Assignments
  void addAtom(final IdentifierProxy ident)
  {
    mAtoms.addProxy(ident);
  }

  void addVariable(final SimpleExpressionProxy varname,
                   final CompiledRange range)
  {
    mRangeMap.putByProxy(varname, range);
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorSet<IdentifierProxy> mAtoms;
  private final ProxyAccessorMap<SimpleExpressionProxy,CompiledRange>
    mRangeMap;

}
