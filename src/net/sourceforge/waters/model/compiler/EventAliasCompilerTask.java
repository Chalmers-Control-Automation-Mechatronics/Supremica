//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventAliasCompilerTask
//###########################################################################
//# $Id: EventAliasCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.DuplicateIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.IndexedIdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;



class EventAliasCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  EventAliasCompilerTask(final CompilerContext context)
  {
    super(context);
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    if (proxy instanceof EventAliasProxy) {
      final EventAliasProxy alias = (EventAliasProxy) proxy;
      compileEventAlias(alias);
    } else if (proxy instanceof ForeachEventAliasProxy) {
      final ForeachEventAliasProxy foreach = (ForeachEventAliasProxy) proxy;
      compileForeach(foreach);
    } else {
      throw new ClassCastException
	("EventAliasCompilerTask can't compile item of class " +
	 proxy.getClass().getName() + "!");
    }
  }

  void compileEventAlias(final EventAliasProxy alias)
    throws EvalException
  {
    final ExpressionProxy expr = alias.getExpression();
    final Value value = evalExpression(expr);
    final IdentifierProxy ident = alias.getIdentifier();
    storeAlias(ident, value);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Value evalExpression(final ExpressionProxy expr)
    throws EvalException
  {
    final CompilerContext context = getContext();
    if (expr instanceof SimpleExpressionProxy) {
      final SimpleExpressionProxy simple = (SimpleExpressionProxy) expr;
      return simple.eval(context);
    } else if (expr instanceof EventListExpressionProxy) {
      final EventListExpressionProxy event = (EventListExpressionProxy) expr;
      final List exprlist = event.getEventList();
      final EventListValue eventlist = new EventListValue(exprlist.size());
      final CompilerTask task = new EventCompilerTask(context, eventlist);
      task.compileList(exprlist);
      return eventlist.getSimplified();
    } else {
      throw new ClassCastException
	("EventAliasCompilerTask can't evaluate expression of class " +
	 expr.getClass().getName() + "!");
    }
  }

  private void storeAlias(final IdentifierProxy ident, final Value value)
    throws EvalException
  {
    final CompilerContext context = getContext();
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = ident.getName();
      try {
	context.set(name, value);
      } catch (final DuplicateIdentifierException exception) {
	exception.provideLocation(ident);
	throw exception;
      }
    } else if (ident instanceof IndexedIdentifierProxy) {
      final IndexedIdentifierProxy indexed = (IndexedIdentifierProxy) ident;
      final String name = indexed.getName();
      final Value foundvalue = context.get(name);
      ArrayAliasValue alias = null;
      if (foundvalue == null) {
	alias = new ArrayAliasValue(name);
	context.set(name, alias);
      } else if (value instanceof ArrayAliasValue) {
	alias = (ArrayAliasValue) foundvalue;
      } else {
	throw new DuplicateIdentifierException(name, ident);
      }	
      final List indexes = indexed.getIndexes();
      final Iterator iter = indexes.iterator();
      while (iter.hasNext()) {
	final SimpleExpressionProxy expr =
	  (SimpleExpressionProxy) iter.next();
	final Value index = expr.evalToIndex(context);
	final Value nextvalue = alias.get(index);
	if (nextvalue == null) {
	  if (iter.hasNext()) {
	    final ArrayAliasValue nextalias =
	      new ArrayAliasValue(alias, index);
	    alias.set(index, nextalias);
	    alias = nextalias;
	  } else {
	    alias.set(index, value);
	  }
	} else if (nextvalue instanceof ArrayAliasValue && iter.hasNext()) {
	  alias = (ArrayAliasValue) nextvalue;
	} else {
	  throw new DuplicateIdentifierException(value.toString(), ident);
	}
      }	
    } else {
      throw new ClassCastException
	("Bad identifier type " + ident.getClass().getName() + "!");
    }
  }


  private interface AliasSetter
  {
    public void set(Object name, Value value);
  }

}
