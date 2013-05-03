//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMVariableContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

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
 * @author saharm
 */
//#########################################################################
//# Inner Class EFSMVariableContext
/**
 * A variable context for EFSM compilation. Contains ranges of all
 * variables, and identifies enumeration atoms.
 */
class EFSMVariableContext implements VariableContext
{
  //#######################################################################
  //# Constructor
  public EFSMVariableContext(final ModuleProxy module,
                             final CompilerOperatorTable op)
  {

    mModuleContext = new ModuleBindingContext(module);
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mGlobalVariableMap =
      new ProxyAccessorHashMap<IdentifierProxy,EFSMVariable>(eq);
    mNextOperator = op.getNextOperator();
  }

  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    if (varname instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varname;
      final EFSMVariable variable = mGlobalVariableMap.getByProxy(ident);
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
  public SimpleExpressionProxy getBoundExpression(final SimpleExpressionProxy ident)
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
  public EFSMVariable getVariable(final SimpleExpressionProxy varName)
  {
    if (varName instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) varName;
      return mGlobalVariableMap.getByProxy(ident);
    } else {
      return null;
    }
  }

  public void addVariable(final EFSMVariable var)
  {
    final IdentifierProxy ident = (IdentifierProxy) var.getVariableName();
    mGlobalVariableMap.putByProxy(ident, var);
  }

  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException
  {
    mModuleContext.insertEnumAtom(ident);
  }

  //#######################################################################
  //# Data Members
  private final ProxyAccessorMap<IdentifierProxy,EFSMVariable> mGlobalVariableMap;
  private final ModuleBindingContext mModuleContext;
  private final UnaryOperator mNextOperator;
}