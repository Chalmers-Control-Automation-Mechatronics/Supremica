//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   ConstantAliasCompilerTask
//###########################################################################
//# $Id: ConstantAliasCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ConstantAliasProxy;



class ConstantAliasCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  ConstantAliasCompilerTask(final CompilerContext context)
  {
    super(context);
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    final ConstantAliasProxy alias = (ConstantAliasProxy) proxy;
    compileConstantAlias(alias);
  }

  void compileConstantAlias(final ConstantAliasProxy alias)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final SimpleIdentifierProxy ident =
      (SimpleIdentifierProxy) alias.getIdentifier();
    final String name = ident.getName();
    final SimpleExpressionProxy expr =
      (SimpleExpressionProxy) alias.getExpression();
    final Value value = expr.eval(context);
    context.set(name, value);
  }

}
