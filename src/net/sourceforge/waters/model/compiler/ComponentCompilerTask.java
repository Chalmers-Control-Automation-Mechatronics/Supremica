//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ComponentCompilerTask
//###########################################################################
//# $Id: ComponentCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.File;
import java.util.Map;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.expr.DuplicateIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>A compiler task that handles module components.</P>
 *
 * <P>This compiler task process a module's list of components
 * and translates all the following structures.</P>
 * <UL>
 * <LI>{@link net.sourceforge.waters.model.module.SimpleComponentProxy}</LI>
 * <LI>{@link net.sourceforge.waters.model.module.InstanceProxy}</LI>
 * <LI>{@link net.sourceforge.waters.model.module.ForeachComponentProxy}</LI>
 * </UL>
 *
 * @author Robi Malik
 */

class ComponentCompilerTask
  extends CompilerTask
{

  //#########################################################################
  //# Constructors
  ComponentCompilerTask(final CompilerContext context,
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
    if (proxy instanceof SimpleComponentProxy) {
      final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
      compileComponent(comp);
    } else if (proxy instanceof InstanceProxy) {
      final InstanceProxy inst = (InstanceProxy) proxy;
      compileInstance(inst);
    } else if (proxy instanceof ForeachComponentProxy) {
      final ForeachComponentProxy foreach = (ForeachComponentProxy) proxy;
      compileForeach(foreach);
    } else {
      throw new ClassCastException
	("ComponentCompilerTask can't compile item of class " +
	 proxy.getClass().getName() + "!");
    }
  }

  void compileComponent(final SimpleComponentProxy comp)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final SimpleExpressionProxy ident = comp.getIdentifier();
    final String name = ident.evalToName(context);
    final String fullname = context.getPrefixedName(name);
    final ComponentKind kind = comp.getKind();
    final GraphProxy graph = comp.getGraph();
    final AutomatonProxy aut = new AutomatonProxy(fullname, kind);
    final GraphCompilerTask task = new GraphCompilerTask(context, aut);
    task.compileGraph(graph);
    try {
      mEnvironment.processAutomaton(aut);
    } catch (final DuplicateNameException exception) {
      throw new DuplicateIdentifierException(name, comp);
    }
  }

  void compileInstance(final InstanceProxy inst)
    throws EvalException
  {
    try {
      final CompilerContext context = getContext();
      final SimpleExpressionProxy ident = inst.getIdentifier();
      final String instname = ident.evalToName(context);
      final Map bindings = inst.getBindingMap();
      final ParameterContext actuals =
	new ParameterContext(bindings, context);
      final DocumentManager manager = mEnvironment.getDocumentManager();
      final File path = context.getPath();
      final String filename = inst.getModuleName();
      final ModuleProxy module =
	(ModuleProxy) manager.load(path, filename, ModuleProxy.class);
      final String fullname = context.getPrefixedName(instname);
      final CompilerContext newcontext =
	new CompilerContext(module, fullname);
      final ModuleCompilerTask task =
      new ModuleCompilerTask(newcontext, mEnvironment);
      task.compileModule(module, actuals);
    } catch (final JAXBException exception) {
      throw new InstantiationException(exception, inst);
    } catch (final ModelException exception) {
      throw new InstantiationException(exception, inst);
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleCompiler mEnvironment;

}
