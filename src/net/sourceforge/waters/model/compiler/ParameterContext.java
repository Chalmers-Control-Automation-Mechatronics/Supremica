//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   ParameterContext
//###########################################################################
//# $Id: ParameterContext.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


class ParameterContext
{

  //#########################################################################
  //# Constructors
  ParameterContext()
  {
    mBindings = null;
    mContext = null;
  }


  ParameterContext(final Map bindings, final CompilerContext context)
  {
    mBindings = bindings;
    mContext = context;
  }


  //#########################################################################
  //# Accessing the Bindings
  ExpressionProxy getExpression(final String name)
  {
    if (mBindings == null) {
      return null;
    } else {
      final ParameterBindingProxy binding =
	(ParameterBindingProxy) mBindings.get(name);
      return binding == null ? null : binding.getExpression();
    }
  }

  Value getValue(final String name)
    throws EvalException
  {
    final ExpressionProxy expr = getExpression(name);
    if (expr == null) {
      return null;
    } else if (expr instanceof SimpleExpressionProxy) {
      final SimpleExpressionProxy simple = (SimpleExpressionProxy) expr;
      return simple.eval(mContext);
    } else if (expr instanceof EventListExpressionProxy) {
      final EventListExpressionProxy event = (EventListExpressionProxy) expr;
      final List exprlist = event.getEventList();
      final EventListValue eventlist = new EventListValue(exprlist.size());
      final CompilerTask task = new EventCompilerTask(mContext, eventlist);
      task.compileList(exprlist);
      return eventlist.getSimplified();
    } else {
      throw new ClassCastException
	("Unknown expression type " + expr.getClass().getName() +
	 "found in parameter binding for '" + name + "'!");
    }
  }

  void checkForUnused(final CompilerContext context)
    throws UndefinedIdentifierException
  {
    if (mBindings != null) {
      final Iterator iter = mBindings.keySet().iterator();
      while (iter.hasNext()) {
	final String name = (String) iter.next();
	if (context.get(name) == null) {
	  final ParameterBindingProxy binding =
	    (ParameterBindingProxy) mBindings.get(name);
	  throw new UndefinedIdentifierException(name, "parameter", binding);
	}
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Map mBindings;
  private final CompilerContext mContext;

}
