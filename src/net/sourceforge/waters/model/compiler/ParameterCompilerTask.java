//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ParameterCompilerTask
//###########################################################################
//# $Id: ParameterCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.RangeParameterProxy;


/**
 * A compiler task that handles parameters
 * ({@link net.sourceforge.waters.model.module.ParameterProxy}).
 *
 * @author Robi Malik
 */

class ParameterCompilerTask extends EventDeclCompilerTask
{

  //#########################################################################
  //# Constructors
  ParameterCompilerTask(final CompilerContext context,
			final ModuleCompiler environment,
			final ParameterContext actuals,
			final boolean usedefaults)
  {
    super(context, environment);
    mActuals = actuals;
    mUseDefaults = usedefaults;
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    if (proxy instanceof EventParameterProxy) {
      final EventParameterProxy param = (EventParameterProxy) proxy;
      compileEventParameter(param);
    } else if (proxy instanceof IntParameterProxy) {
      final IntParameterProxy param = (IntParameterProxy) proxy;
      compileIntParameter(param);
    } else if (proxy instanceof RangeParameterProxy) {
      final RangeParameterProxy param = (RangeParameterProxy) proxy;
      compileRangeParameter(param);
    } else {
      throw new ClassCastException
	("ParameterCompilerTask can't compile item of class " +
	 proxy.getClass().getName() + "!");
    }
  }

  void compileEventParameter(final EventParameterProxy param)
    throws EvalException
  {
    final EventDeclProxy decl = param.getEventDecl();
    final String name = decl.getName();
    final Value actual = mActuals.getValue(name);
    if (useDefaultValue(param, actual)) {
      compileEventDecl(param.getEventDecl());
    } else if (actual instanceof EventValue) {
      final CompilerContext context = getContext();
      final EventValue actualevent = (EventValue) actual;
      try {
	actualevent.checkParameterType(decl);
      } catch (final EventKindException exception) {
	final ExpressionProxy expr = mActuals.getExpression(name);
	exception.provideLocation(expr);
	throw exception;
      }
      context.set(name, actualevent);
    } else {
      final ExpressionProxy expr = mActuals.getExpression(name);
      throw new TypeMismatchException(expr, actual, "EVENT");
    }
  }

  void compileIntParameter(final IntParameterProxy param)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final String name = param.getName();
    final Value actual = mActuals.getValue(name);
    if (useDefaultValue(param, actual)) {
      final SimpleExpressionProxy expr = param.getDefault();
      final IntValue value = expr.evalToInt(context);
      context.set(name, value);
    } else if (actual instanceof IntValue) {
      context.set(name, actual);
    } else {
      final ExpressionProxy expr = mActuals.getExpression(name);
      throw new TypeMismatchException
	(expr, actual, SimpleExpressionProxy.TYPE_INT);
    }
  }

  void compileRangeParameter(final RangeParameterProxy param)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final String name = param.getName();
    final Value actual = mActuals.getValue(name);
    if (useDefaultValue(param, actual)) {
      final SimpleExpressionProxy expr = param.getDefault();
      final RangeValue value = expr.evalToRange(context);
      context.set(name, value);
    } else if (actual instanceof RangeValue) {
      context.set(name, actual);
    } else {
      final ExpressionProxy expr = mActuals.getExpression(name);
      throw new TypeMismatchException
	(expr, actual, SimpleExpressionProxy.TYPE_RANGE);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean useDefaultValue(final ParameterProxy param,
				  final Value actual)
    throws UndefinedIdentifierException
  {
    if (actual != null) {
      return false;
    } else if (!param.isRequired() || mUseDefaults) {
      return true;
    } else {
      throw new UndefinedIdentifierException
	(param.getName(), "required parameter", param);
    }
  }


  //#########################################################################
  //# Data Members
  private final ParameterContext mActuals;
  private final boolean mUseDefaults;

}
