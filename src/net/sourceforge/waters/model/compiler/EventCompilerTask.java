//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventCompilerTask
//###########################################################################
//# $Id: EventCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;


import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.Context;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ForeachEventProxy;


class EventCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  EventCompilerTask(final CompilerContext context,
		    final EventValueConsumer consumer)
  {
    super(context);
    mConsumer = consumer;
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    if (proxy instanceof SimpleExpressionProxy) {
      final SimpleExpressionProxy expr = (SimpleExpressionProxy) proxy;
      compileEvent(expr);
    } else if (proxy instanceof ForeachEventProxy) {
      final ForeachEventProxy foreach = (ForeachEventProxy) proxy;
      compileForeach(foreach);
    } else {
      throw new ClassCastException
	("EventCompilerTask can't compile item of class " +
	 proxy.getClass().getName() + "!");
    }
  }

  void compileEvent(final SimpleExpressionProxy expr)
    throws EvalException
  {
    final EventValue event = evalToEvent(expr);
    try {
      mConsumer.processValue(event);
    } catch (final EventKindException exception) {
      exception.provideLocation(expr);
      throw exception;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventValue evalToEvent(final SimpleExpressionProxy expr)
    throws EvalException
  {
    final Context context = getContext();
    final Value value = expr.eval(context);
    if (value instanceof EventValue) {
      return (EventValue) value;
    } else {
      throw new TypeMismatchException(expr, value, "EVENT");
    }
  }


  //#########################################################################
  //# Data Members
  private final EventValueConsumer mConsumer;

}
