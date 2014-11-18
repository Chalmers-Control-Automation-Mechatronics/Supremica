//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.compiler.groupnode
//# CLASS:   GroupNodeCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.groupnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * <P>
 * The second pass of the compiler.
 * </P>
 *
 * <P>
 * This pass of the compiler accepts a module ({@link ModuleProxy}) as the
 * input and produces another module as the output. It assumes that the input
 * module already has all the foreach blocks instantiated, and it removes the
 * group nodes by explicitly creating new edges between simple child nodes (
 * {@link SimpleNodeProxy}.
 * </P>
 *
 * <P>
 * It is ensured that the resultant module only contains:
 * </P>
 * <UI>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy} containing no {@link GroupnodeProxy};</LI>
 * <LI>{@link VariableComponentProxy}.</LI>
 * </UL>
 *
 * @author Roger Su
 */
public class GroupNodeCompiler extends DefaultModuleProxyVisitor implements
  Abortable
{
  //#########################################################################
  //# Constructor
  public GroupNodeCompiler(final ModuleProxyFactory factory,
                           final CompilationInfo compilationInfo,
                           final ModuleProxy module)
  {
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mEquality = new ModuleEqualityVisitor(false);
    mInputModule = module;
    mCloner = new SourceInfoCloner(factory, compilationInfo);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }

  private void checkAbort()
    throws VisitorException
  {
    if (mIsAborting) {
      final EvalAbortException exception = new EvalAbortException();
      throw new VisitorException(exception);
    }
  }

  //#########################################################################
  //# Invocation
  public ModuleProxy compile() throws EvalException
  {
    try {
      return visitModuleProxy(mInputModule);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw new WatersRuntimeException(cause);
      }
    }
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public ModuleProxy visitModuleProxy(final ModuleProxy inputModule)
    throws VisitorException
  {
    mComponents = new ArrayList<>();
    visitCollection(inputModule.getComponentList());

    return mFactory.createModuleProxy(inputModule.getName(),
                                      inputModule.getComment(),
                                      inputModule.getLocation(),
                                      inputModule.getConstantAliasList(),
                                      inputModule.getEventDeclList(),
                                      inputModule.getEventAliasList(),
                                      mComponents);
  }

  /**
   * Visits a {@link SimpleComponentProxy} of a {@link ModuleProxy}.
   * <p>
   * If the graph of a component contains group nodes, a new graph would be
   * created, and it will be added to the result list of components.
   * <p>
   * Otherwise, if the group contains no group nodes, it will not be changed,
   * and the original graph will be added to the list of components.
   *
   * @param oldComponent
   *          A component of the original module
   * @return Unimportant
   */
  @Override
  public Object visitSimpleComponentProxy
            (final SimpleComponentProxy oldComponent) throws VisitorException
  {
    checkAbort();

    final GraphProxy oldGraph = oldComponent.getGraph();
    final GraphProxy newGraph = visitGraphProxy(oldGraph);

    if (newGraph != oldGraph) {
      final SimpleComponentProxy newComponent =
        mFactory.createSimpleComponentProxy(oldComponent.getIdentifier(),
                                            oldComponent.getKind(), newGraph,
                                            oldComponent.getAttributes());
      mCompilationInfo.add(newComponent, oldComponent);
      mComponents.add(newComponent);
    } else {
      mComponents.add(oldComponent);
    }

    return null;
  }

  /**
   * Visits a {@link VariableComponentProxy} of a {@link ModuleProxy}.
   * <p>
   * Adds it to the list of components without performing any modification,
   * as variables are not relevant to this part of the compiler.
   */
  @Override
  public Object visitVariableComponentProxy
                (final VariableComponentProxy oldVar) throws VisitorException
  {
    checkAbort();
    mComponents.add(oldVar);
    return null;
  }

  /**
   * Visits the graph ({@link GraphProxy}) of a {@link SimpleComponentProxy},
   * and resolves all group nodes in a graph.
   *
   * @param oldGraph The original graph, which may contain group nodes
   * @return         The new graph without any group nodes
   */
  @Override
  public GraphProxy visitGraphProxy(final GraphProxy oldGraph)
    throws VisitorException
  {
    //Prepare a list that will contain the edges of the new graph.
    mNewEdges = new ArrayList<>();

    //Obtain the list of all edges in the graph.
    mOldEdges = new ArrayList<>();
    visitCollection(oldGraph.getEdges());

    //Obtain the list of all nodes (both simple and group) in the graph.
    mCompiledNodes = new ArrayList<>();
    visitCollection(oldGraph.getNodes());

    //For every compiled node 'x' of this graph.
    for (final CompiledNode x : mCompiledNodes)
    {
      //For every compiled node 'y' that is a simple child of 'x'.
      for (final CompiledNode y : mCompiledNodes) {
        if (x.getChildren().contains(y.getNodeProxy()))
        {
          //For every edge 'e' whose source is 'x'.
          for (final EdgeProxy e : mOldEdges) {
            if (e.getSource() == x.getNodeProxy())
            {
              final List<Proxy> oldIdentifiers =
                                  e.getLabelBlock().getEventIdentifierList();
              final List<Proxy> newIdentifiers = new ArrayList<>();

              //For every identifier 'i' of 'e'.
              for (final Proxy i : oldIdentifiers)
              {
                if (!strictlyContains(y.getCause(i), e.getSource()))
                {
                  final Proxy newI = mCloner.getClone(i);
                  mCompilationInfo.add(newI, i);
                  newIdentifiers.add(newI);
                  y.addCause(i, x.getNodeProxy());
                }
              } //Now the new list of identifiers is properly modified.

              //Create a new LabelBlock using the new identifier list.
              final LabelBlockProxy block =
                        mFactory.createLabelBlockProxy(newIdentifiers, null);

              //Create new edges to target simple nodes.
              for (final SimpleNodeProxy t : returnSimpleNodes(e.getTarget()))
              {
                checkAbort();
                final EdgeProxy newEdge =
                    mFactory.createEdgeProxy(y.getNodeProxy(), t, block,
                                             e.getGuardActionBlock(),
                                             e.getGeometry(),
                                             e.getEndPoint(),
                                             e.getStartPoint());
                mNewEdges.add(newEdge);
                mCompilationInfo.add(newEdge, e);
              }
            }
          }
        }
      }
    }

    //After simplifying the group nodes, create and return the new graph.
    final List<SimpleNodeProxy> resultNodes = new ArrayList<>();
    for (final CompiledNode cNode : mCompiledNodes)
      if (cNode.getNodeProxy() instanceof SimpleNodeProxy)
        resultNodes.add((SimpleNodeProxy) cNode.getNodeProxy());
    return mFactory.createGraphProxy(oldGraph.isDeterministic(),
                                     oldGraph.getBlockedEvents(),
                                     resultNodes, mNewEdges);
  }

  /**
   * Processes a raw node and adds it to the list of CompiledNodes.
   */
  @Override
  public Object visitNodeProxy(final NodeProxy node) throws VisitorException
  {
    checkAbort();
    mCompiledNodes.add(new CompiledNode(node));
    return null;
  }

  /**
   * Adds an edge from the raw collection to the list of edges.
   */
  @Override
  public Object visitEdgeProxy(final EdgeProxy edge) throws VisitorException
  {
    checkAbort();
    mOldEdges.add(edge);
    return null;
  }

  //#########################################################################
  //# Auxiliary Methods
  /**
   * If the given node is a {@link SimpleNodeProxy}, the returned list would
   * contain only the given node.
   * <p>
   * Otherwise, the returned list would contain all simple nodes that are
   * children of the given node.
   */
  private List<SimpleNodeProxy> returnSimpleNodes(final NodeProxy node)
  {
    final List<SimpleNodeProxy> result = new ArrayList<>();
    if (node instanceof SimpleNodeProxy) {
      result.add((SimpleNodeProxy) node);
      return result;
    } else {
      for (final NodeProxy child : node.getImmediateChildNodes())
        result.addAll(returnSimpleNodes(child));
    }
    return result;
  }

  /**
   * Tests whether any node {@link NodeProxy} in the collection is strictly
   * contained by a given {@link NodeProxy}.
   *
   * @param a
   *          A collection of {@link NodeProxy}, which are the cause of a
   *          particular identifier in a compiled node.
   * @param b
   *          The {@link NodeProxy} to be tested
   * @return <code>true</code> if there exists an element in a that is
   *         contained in b but not equal to b; <code>false</code> otherwise.
   */
  private boolean strictlyContains(final Collection<NodeProxy> a,
                                   final NodeProxy b)
  {
    if (a != null) {
      for (final NodeProxy n : a)
        if (b.getImmediateChildNodes().contains(n) && !mEquality.equals(n, b))
          return true;
    }
    return false;
  }

 //#########################################################################
  //# Inner Class: CompiledNode
  /**
   * A node in a form that is easier to be manipulated by the group node
   * compiler.
   */
  private class CompiledNode
  {
    //#######################################################################
    //# Constructor
    private CompiledNode(final NodeProxy node) throws VisitorException
    {
      mmNodeProxy = node;
      mmChildren = returnSimpleNodes(node);
    }

    //#######################################################################
    //# Access Methods
    NodeProxy getNodeProxy()
    {
      return mmNodeProxy;
    }

    List<SimpleNodeProxy> getChildren()
    {
      return mmChildren;
    }

    Collection<NodeProxy> getCause(final Proxy identifier)
    {
      return mmUsedEvents.getByProxy(identifier);
    }

    void addCause(final Proxy identifier, final NodeProxy cause)
    {
      Collection<NodeProxy> list = mmUsedEvents.getByProxy(identifier);
      if (list == null) {
        list = new ArrayList<>();
        mmUsedEvents.putByProxy(identifier, list);
      }
      list.add(cause);
    }

    //#######################################################################
    //# Data Members
    private final NodeProxy mmNodeProxy;
    private List<SimpleNodeProxy> mmChildren = new ArrayList<>();

    //Ordered pairs of the form: (identifier, causes)
    private final ProxyAccessorMap<Proxy, Collection<NodeProxy>> mmUsedEvents =
      new ProxyAccessorHashMap<>(mEquality);

  }

  //#########################################################################
  //# Data Members

  //These two variables are related to the entire instance of this class.
  private final ModuleProxy mInputModule;   //The original input module
  private List<ComponentProxy> mComponents; //The final list of components

  //These variables are related to only parts of the instance.
  private List<CompiledNode> mCompiledNodes; //All the nodes of a graph
  private List<EdgeProxy> mOldEdges; //All the edges of the original graph
  private List<EdgeProxy> mNewEdges; //All the edges of the new graph

  //These variables are utilities used by the compiler.
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final ModuleEqualityVisitor mEquality;
  private final ModuleProxyCloner mCloner;
  private boolean mIsAborting;
}
