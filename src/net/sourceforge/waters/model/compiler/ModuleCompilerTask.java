//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ModuleCompiler
//###########################################################################
//# $Id: ModuleCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxy;


/**
 * A compiler task that handles modules ({@link ModuleProxy}).
 *
 * @author Robi Malik
 */

class ModuleCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  ModuleCompilerTask(final CompilerContext context,
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
    final ModuleProxy module = (ModuleProxy) proxy;
    compileModule(module);
  }

  void compileModule(final ModuleProxy module)
    throws EvalException
  {
    final ParameterContext actuals = new ParameterContext();
    compileModule(module, actuals, true);
  }

  void compileModule(final ModuleProxy module, final ParameterContext actuals)
    throws EvalException
  {
    compileModule(module, actuals, false);
  }

  void compileModule(final ModuleProxy module,
		     final ParameterContext actuals,
		     final boolean usedefaults)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final List constants = module.getConstantAliasList();
    if (!constants.isEmpty()) {
      final CompilerTask task =	new ConstantAliasCompilerTask(context);
      task.compileList(constants);
    }
    final List parameters = module.getParameterList();
    if (!parameters.isEmpty()) {
      final CompilerTask task =
	new ParameterCompilerTask(context, mEnvironment, actuals, usedefaults);
      task.compileList(parameters);
      actuals.checkForUnused(context);
    }
    final List events = module.getEventDeclList();
    if (!events.isEmpty()) {
      final CompilerTask task =
	new EventDeclCompilerTask(context, mEnvironment);
      task.compileList(events);
    }
    final List aliases = module.getEventAliasList();
    if (!aliases.isEmpty()) {
      final CompilerTask task =	new EventAliasCompilerTask(context);
      task.compileList(aliases);
    }
    final List components = module.getComponentList();
    if (!components.isEmpty()) {
      final CompilerTask task =
	new ComponentCompilerTask(context, mEnvironment);
      task.compileList(components);
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleCompiler mEnvironment;

}
