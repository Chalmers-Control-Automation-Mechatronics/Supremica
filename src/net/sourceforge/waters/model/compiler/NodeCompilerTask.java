//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   NodeCompilerTask
//###########################################################################
//# $Id: NodeCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.EventKind;


class NodeCompilerTask extends CompilerTask
{

  //#########################################################################
  //# Constructors
  NodeCompilerTask(final CompilerContext context,
		   final NodeConsumer consumer)
  {
    super(context);
    mNodeConsumer = consumer;
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    if (proxy instanceof SimpleNodeProxy) {
      final SimpleNodeProxy node = (SimpleNodeProxy) proxy;
      compileSimpleNode(node);
    } else if (proxy instanceof GroupNodeProxy) {
      final GroupNodeProxy node = (GroupNodeProxy) proxy;
      compileGroupNode(node);
    }
  }

  void compileSimpleNode(final SimpleNodeProxy node)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final String name = node.getName();
    final List eventlist = node.getPropositions();
    final Set propositions = new TreeSet();
    final EventConsumer propconsumer = new EventConsumer() {
	public void processEvent(final EventProxy event)
	  throws EventKindException
	{
	  if (event.getKind() != EventKind.PROPOSITION) {
	    throw new EventKindException
	      ("Can't add event '" + event.getName() + "' to state '" +
	       name + "': not a proposition!");
	  }
	  propositions.add(event);
	}
      };
    final EventValueConsumer valueconsumer =
      new AutomatonEventConsumer(propconsumer);
    final EventCompilerTask task =
      new EventCompilerTask(context, valueconsumer);
    task.compileList(eventlist);
    final StateProxy state =
      new StateProxy(name, node.isInitial(), propositions);
    final CompiledNode entry = new CompiledNode(node, state);
    mNodeConsumer.processNode(entry);
  }

  void compileGroupNode(final GroupNodeProxy node)
  {
    final CompiledNode entry = new CompiledNode(node);
    mNodeConsumer.processNode(entry);
  }


  //#########################################################################
  //# Data Members
  private final NodeConsumer mNodeConsumer;

}
