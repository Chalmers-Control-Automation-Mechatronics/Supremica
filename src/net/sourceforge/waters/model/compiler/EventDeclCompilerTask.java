//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EventDeclCompilerTask
//###########################################################################
//# $Id: EventDeclCompilerTask.java,v 1.2 2005-02-21 19:19:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * A compiler task that handles event declarations.
 * ({@link net.sourceforge.waters.model.module.EventDeclProxy}).
 *
 * @author Robi Malik
 */

class EventDeclCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  EventDeclCompilerTask(final CompilerContext context,
			final ModuleCompiler environment)
  {
    super(context);
    mEnvironment = environment;
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    final EventDeclProxy decl = (EventDeclProxy) proxy;
    compileEventDecl(decl);
  }

  void compileEventDecl(final EventDeclProxy decl)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final List expressions = decl.getRanges();
    final List ranges = new ArrayList(expressions.size());
    final Iterator iter = expressions.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy expr = (SimpleExpressionProxy) iter.next();
      final RangeValue range = expr.evalToRange(context);
      ranges.add(range);
    }
    final String name = decl.getName();
    final String fullname = context.getPrefixedName(name);
    final CompiledEventDecl entry =
      new CompiledEventDecl(fullname, decl, ranges, mEnvironment);
    final EventValue value = entry.getValue();
    context.set(name, value);
  }


  //#########################################################################
  //# Data Members
  private final ModuleCompiler mEnvironment;

}
