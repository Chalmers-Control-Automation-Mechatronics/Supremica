//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   ComponentCompilerTask
//###########################################################################
//# $Id: GraphCompilerTask.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.UnexpectedWatersException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class GraphCompilerTask
  extends CompilerTask
  implements EventConsumer, NodeConsumer
{

  //#########################################################################
  //# Constructors
  GraphCompilerTask(final CompilerContext context,
		    final AutomatonProxy aut)
  {
    super(context);
    mAutomaton = aut;
  }


  //#########################################################################
  //# Invocation
  void compile(final Proxy proxy)
    throws EvalException
  {
    final GraphProxy graph = (GraphProxy) proxy;
    compileGraph(graph);
  }

  void compileGraph(final GraphProxy graph)
    throws EvalException
  {
    final CompilerContext context = getContext();
    final Collection events = graph.getBlockedEvents();
    final Collection nodes = graph.getNodes();
    final Collection edges = graph.getEdges();
    mDeterministic = graph.isDeterministic();
    mPrecompiledNodes = new IdentityHashMap(nodes.size());
    if (!events.isEmpty()) {
      final EventValueConsumer consumer = new AutomatonEventConsumer(this);
      final CompilerTask task = new EventCompilerTask(context, consumer);
      task.compileList(events);
    }
    if (!nodes.isEmpty()) {
      final CompilerTask task = new NodeCompilerTask(context, this);
      task.compileList(nodes);
    }
    if (!edges.isEmpty()) {
      final Iterator edgeiter = edges.iterator();
      while (edgeiter.hasNext()) {
	final EdgeProxy edge = (EdgeProxy) edgeiter.next();
	final NodeProxy source = edge.getSource();
	final CompiledNode entry =
	  (CompiledNode) mPrecompiledNodes.get(source);
	entry.addEdge(edge);
      }
      final Iterator nodeiter = nodes.iterator();
      while (nodeiter.hasNext()) {
	final NodeProxy source = (NodeProxy) nodeiter.next();
	final CompiledNode sourceentry =
	  (CompiledNode) mPrecompiledNodes.get(source);
	final Iterator inneredgeiter = sourceentry.getEdgeIterator();
	while (inneredgeiter.hasNext()) {
	  final EdgeProxy edge = (EdgeProxy) inneredgeiter.next();
	  final NodeProxy target = edge.getTarget();
	  final Collection eventlist = edge.getLabelBlock();
	  final Iterator sourceiter = source.getSimpleChildNodeIterator();
	  while (sourceiter.hasNext()) {
	    final SimpleNodeProxy simplesource =
	      (SimpleNodeProxy) sourceiter.next();
	    final CompiledNode simplesourceentry =
	      (CompiledNode) mPrecompiledNodes.get(simplesource);
	    final Iterator targetiter = target.getSimpleChildNodeIterator();
	    while (targetiter.hasNext()) {
	      final SimpleNodeProxy simpletarget =
		(SimpleNodeProxy) targetiter.next();
	      final CompiledNode simpletargetentry =
		(CompiledNode) mPrecompiledNodes.get(simpletarget);
	      final EventConsumer eventconsumer =
		new TransitionEventConsumer(simplesourceentry,
					    simpletargetentry,
					    sourceentry);
	      final EventValueConsumer valueconsumer =
		new AutomatonEventConsumer(eventconsumer);
	      final EventCompilerTask task =
		new EventCompilerTask(context, valueconsumer);
	      task.compileList(eventlist);
	    }
	  }
	}
	sourceentry.clearChildNodes();
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventConsumer
  public void processEvent(final EventProxy event)
  {
    try {
      mAutomaton.addEvent(event);
    } catch (final DuplicateNameException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.NodeConsumer
  public void processNode(final CompiledNode entry)
  {
    final NodeProxy node = entry.getNode();
    final StateProxy state = entry.getState();
    mPrecompiledNodes.put(node, entry);
    if (state != null) {
      try {
	mAutomaton.addState(state);
      } catch (final DuplicateNameException exception) {
	// O.K.
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private boolean mDeterministic;
  private Map mPrecompiledNodes;


  //#########################################################################
  //# Local Class TransitionEventConsumer
  private class TransitionEventConsumer implements EventConsumer
  {
    //#######################################################################
    //# Constructor
    TransitionEventConsumer(final CompiledNode source,
			    final CompiledNode target,
			    final CompiledNode group)
    {
      mSource = source;
      mTarget = target;
      mGroup = group;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.EventConsumer
    public void processEvent(final EventProxy event)
      throws NondeterminismException
    {
      final Iterator iter = mSource.getCompiledTransitionIterator(event);
      CompiledTransition duplicate = null;
      boolean create = true;
      while (iter.hasNext()) {
	final CompiledTransition ctrans = (CompiledTransition) iter.next();
	if (ctrans.getTarget() == mTarget.getState()) {
	  duplicate = ctrans;
	  continue;
	}
	final NodeProxy cause = ctrans.getGroup();
	if (mGroup.hasChildNode(cause)) {
	  create = false;
	  break;
	} else if (mDeterministic) {
	  final StateProxy source = mSource.getState();
	  throw new NondeterminismException
	    ("Multiple transitions labelled '" + event.getName() +
	     "' originating from state '" + source.getName() + "'!");
	}
      }
      if (create) {
	createTransition(event, duplicate);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void createTransition(final EventProxy event,
				  final CompiledTransition duplicate)
    {
      final NodeProxy group = mGroup.getNode();
      if (duplicate == null) {
	final StateProxy source = mSource.getState();
	final StateProxy target = mTarget.getState();
	final TransitionProxy trans =
	  new TransitionProxy(source, target, event);
	try{
	  mAutomaton.addTransition(trans);
	} catch (final DuplicateNameException exception) {
	  throw new UnexpectedWatersException(exception);
	}
	mSource.addTransition(trans, group);
      } else {
	final TransitionProxy trans = duplicate.getTransition();
	mSource.addTransition(trans, group);
      }
    }

    //#######################################################################
    //# Data Members
    private final CompiledNode mSource;
    private final CompiledNode mTarget;
    private final CompiledNode mGroup;

  }

}
