//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFAVariableContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

/**
 * A variable context for EFA compilation. Contains ranges of all variables, and
 * identifies enumeration atoms.
 * <p/>
 * @author Robi Malik
 */
public abstract class AbstractEFAVariableContext<L, 
                                                 V extends AbstractEFAVariable<L>>
 implements VariableContext
{
  
  //#######################################################################
  //# Constructor
  public AbstractEFAVariableContext(final ModuleProxy module,
                                    final CompilerOperatorTable op)
  {

    mModuleContext = new ModuleBindingContext(module);
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mGlobalVariableMap =
     new ProxyAccessorHashMap<>(eq);
    mNextOperator = op.getNextOperator();
  }

  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    if (varname instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varname;
      final AbstractEFAVariable<L> variable =
       mGlobalVariableMap.getByProxy(ident);
      if (variable != null) {
        return variable.getRange();
      }
    } else if (varname instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
      if (unary.getOperator() == mNextOperator) {
        return getVariableRange(unary.getSubTerm());
      }
    }
    return null;
  }

  @Override
  public SimpleExpressionProxy getBoundExpression(
   final SimpleExpressionProxy ident)
  {
    return mModuleContext.getBoundExpression(ident);
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mModuleContext.isEnumAtom(ident);
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return mModuleContext;
  }

  @Override
  public int getNumberOfVariables()
  {
    return mGlobalVariableMap.size();
  }

  //#######################################################################
  //# Simple Access
  public V getVariable(final SimpleExpressionProxy varName)
  {
    if (varName instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varName;
      return mGlobalVariableMap.getByProxy(ident);
    } else {
      return null;
    }
  }

  public void addVariable(final V var)
  {
    final IdentifierProxy ident = var.getVariableName();
    mGlobalVariableMap.putByProxy(ident, var);
  }

  public void insertEnumAtom(final SimpleIdentifierProxy ident)
   throws DuplicateIdentifierException
  {
    mModuleContext.insertEnumAtom(ident);
  }
  private final ModuleBindingContext mModuleContext;
  private final UnaryOperator mNextOperator;
  
  //#######################################################################
  //# Data Members
  protected final ProxyAccessorMap<IdentifierProxy, V> mGlobalVariableMap;
  
}