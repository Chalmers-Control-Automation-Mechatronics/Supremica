//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   AbstractCompiler
//###########################################################################
//# $Id: CompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ForeachProxy;


abstract class CompilerTask
{

  //#########################################################################
  //# Constructors
  CompilerTask(final CompilerContext context)
  {
    mContext = context;
  }


  //#########################################################################
  //# Invocation
  void compileList(final Collection list)
    throws EvalException
  {
    final Iterator iter = list.iterator();
    while (iter.hasNext()) {
      final Proxy proxy = (Proxy) iter.next();
      compile(proxy);
    }
  }

  void compileForeach(final ForeachProxy foreach)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final String name = foreach.getName();
    final SimpleExpressionProxy rangeexpr = foreach.getRange();
    final SimpleExpressionProxy guardexpr = foreach.getGuard();
    final Collection body = foreach.getBody();
    final RangeValue range = rangeexpr.evalToRange(context);
    final Iterator iter = range.iterator();
    while (iter.hasNext()) {
      final Value index = (Value) iter.next();
      context.set(name, index);
      if (guardexpr == null || guardexpr.evalToBoolean(context)) {
	compileList(body);
      }
      context.unset(name);
    }
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract void compile(final Proxy proxy)
    throws EvalException;


  //#########################################################################
  //# Common Functionality for all Tasks
  CompilerContext getContext()
  {
    return mContext;
  }


  //#########################################################################
  //# Data Members
  private final CompilerContext mContext;

}
