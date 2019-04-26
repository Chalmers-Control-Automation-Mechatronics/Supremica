//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * <P>The second pass of the compiler.</P>
 *
 * <P>This pass of the compiler accepts a module ({@link ModuleProxy}) as the
 * input and produces another module as the output. It assumes that the input
 * module has neither foreach blocks nor instantiations, and it removes the
 * group nodes by explicitly creating new edges between their simple child
 * nodes ({@link SimpleNodeProxy}). This compiler also explicitly expands
 * guards and pass them to the next pass of the {@link ModuleCompiler}.</P>
 *
 * <P>It is ensured that the resultant module only contains objects of the
 * following types:</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e. the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy} containing no {@link GroupNodeProxy};</LI>
 * <LI>{@link VariableComponentProxy}.</LI>
 * </UL>
 *
 * @author Roger Su
 */
public class GroupNodeCompiler extends DefaultModuleProxyVisitor
                               implements Abortable
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
    // Prepare a list that will contain the edges of the new graph.
    mNewEdges = new ArrayList<>();

    // Obtain the list of all edges in the graph.
    mOldEdges = new ArrayList<>();
    visitCollection(oldGraph.getEdges());

    // Obtain the list of all nodes (both simple and group) in the graph.
    mCompiledNodes = new ArrayList<>();
    visitCollection(oldGraph.getNodes());

    // For every compiled node 'x' of this graph.
    for (final CompiledNode x : mCompiledNodes)
    {
      // For every compiled node 'y' that is a simple child of 'x'.
      for (final CompiledNode y : x.getChildren())
      {
        // For every edge 'e' whose source is 'x'.
        for (final EdgeProxy e : x.getEdges())
        {
          SimpleExpressionProxy currentGuard = null;
          if (e.getGuardActionBlock() != null &&
                          !e.getGuardActionBlock().getGuards().isEmpty())
            currentGuard = e.getGuardActionBlock().getGuards().get(0);

          // Obtain the identifiers of 'e'.
          final List<Proxy> oldIdentifiers =
                                   e.getLabelBlock().getEventIdentifierList();

          // For every identifier 'i' of 'e'.
          for (final Proxy i : oldIdentifiers)
          {
            SimpleExpressionProxy newGuard = null;
            final List<Proxy> newIdentifiers = new ArrayList<>();

            final boolean contains =
                              strictlyContains(y.getCauses(i), e.getSource());
            final boolean hasGuards = hasGuards(y, i);

            if (!contains || hasGuards)
            {
              if (contains && hasGuards)
              {
                newGuard = createNewGuard(currentGuard, y.getGuards(i));
                if (newGuard == null)
                  continue;
              }

              final Proxy newI = mCloner.getClone(i);
              mCompilationInfo.add(newI, i);
              newIdentifiers.add(newI);
              y.addCause(i, x.getNodeProxy(), currentGuard);

              // Create a new LabelBlock using the new identifier list.
              final LabelBlockProxy lbBlock =
                         mFactory.createLabelBlockProxy(newIdentifiers, null);

              /* If a new guard has been created, make a new GuardActionBlock
               * with the new guard and the other original fields.
               */
              final GuardActionBlockProxy gaBlock;
              if (newGuard != null)
                gaBlock = createNewGABlock(newGuard, e);
              else
                gaBlock = e.getGuardActionBlock();

              // Create new edges to target simple nodes.
              for (final SimpleNodeProxy t : returnSimpleNodes(e.getTarget()))
              {
                checkAbort();
                final EdgeProxy newEdge =
                    mFactory.createEdgeProxy(y.getNodeProxy(), t, lbBlock,
                                             gaBlock, e.getGeometry(),
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

    // After simplifying the group nodes, create and return the new graph.
    final List<SimpleNodeProxy> resultNodes = new ArrayList<>();
    for (final CompiledNode cNode : mCompiledNodes)
      if (cNode.getNodeProxy() instanceof SimpleNodeProxy)
        resultNodes.add((SimpleNodeProxy) cNode.getNodeProxy());
    return mFactory.createGraphProxy(oldGraph.isDeterministic(),
                                     oldGraph.getBlockedEvents(),
                                     resultNodes, mNewEdges);
  }

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
   *          The {@link NodeProxy} to be tested.
   *
   * @return <code>true</code> if there exists an element in a that is
   *                           contained in b but not equal to b;
   *        <code>false</code> otherwise.
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

  /**
   * Tests whether the given node is already using an event with guards.
   *
   * @param node
   *             The node of interest.
   * @param identifier
   *             This identifies the events of interest.
   *
   * @return <code>true</code> if the given node is already using guards;
   *        <code>false</code> otherwise.
   */
  private boolean hasGuards(final CompiledNode node, final Proxy identifier)
  {
    for (final SimpleExpressionProxy guard : node.getGuards(identifier))
      if (guard != null)
        return true;
    return false;
  }

  /**
   * Makes a new guard using a current guard and and list of existing guards.
   *
   * @param a
   *          This may be null.
   *
   * @param existing
   *          This list is assumed to be not empty.
   *
   * @return If b1, b2, ..., bN are elements of the list of existing guards,
   *       then the resultant guard will be: (a && !b1 && !b2 && ... && !bN).
   */
  private SimpleExpressionProxy createNewGuard(final SimpleExpressionProxy a,
                                   final List<SimpleExpressionProxy> existing)
  {
    final CompilerOperatorTable table = CompilerOperatorTable.getInstance();
    final UnaryOperator not = table.getNotOperator();
    final BinaryOperator and = table.getAndOperator();

    /* First attempt to test whether there exists an element in 'existing'
     * that is exactly identical to 'a'.
     */
    for (final SimpleExpressionProxy b : existing)
    {
      if (mEquality.equals(a, b))
        return null;
    }
    // Compute the conjunction of all the negated elements of 'existing'.
    SimpleExpressionProxy result =
                   mFactory.createUnaryExpressionProxy(not, existing.get(0));
    for (int i=1; i < existing.size(); i++)
    {
      result = mFactory.createBinaryExpressionProxy(and, result,
                   mFactory.createUnaryExpressionProxy(not, existing.get(i)));
    }

    // If 'a' is not null, add it to the conjunction.
    if (a != null)
      result = mFactory.createBinaryExpressionProxy(and, a, result);

    return result;
  }

  /**
   * Creates a new {@link GuardActionBlockProxy} using the given guard and
   * the remaining fields of the given edge.
   */
  private GuardActionBlockProxy createNewGABlock
                    (final SimpleExpressionProxy guard, final EdgeProxy edge)
  {
    // Prepare the list that has the guard as its sole element.
    final List<SimpleExpressionProxy> gList = new ArrayList<>(1);
    gList.add(guard);

    // Prepare the list of actions, initialized as an empty list.
    List<BinaryExpressionProxy> aList = new ArrayList<>();

    // Prepare the geometry information, initialized as 'null'.
    LabelGeometryProxy geo = null;

    /* If the edge already has a GuardActionBlock, then use its actions and
     * geometry information.
     */
    if (edge.getGuardActionBlock() != null) {
      aList = edge.getGuardActionBlock().getActions();
      geo = edge.getGuardActionBlock().getGeometry();
    }

    return mFactory.createGuardActionBlockProxy(gList, aList, geo);
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

      final List<SimpleNodeProxy> simpleChildren = returnSimpleNodes(node);
      if (node instanceof SimpleNodeProxy)
        mmChildren.add(this);
      for (final CompiledNode n : mCompiledNodes)
        if (simpleChildren.contains(n.getNodeProxy()))
          mmChildren.add(n);

      for (final EdgeProxy e : mOldEdges)
        if (e.getSource() == node)
          mmEdges.add(e);
    }

    //#######################################################################
    //# Access Methods
    /**
     * @return The {@link NodeProxy} associated with this CompiledNode.
     */
    NodeProxy getNodeProxy()
    {
      return mmNodeProxy;
    }

    /**
     * @return A list of children of this CompiledNode.
     */
    List<CompiledNode> getChildren()
    {
      return mmChildren;
    }

    /**
     * @return A list of edges that have this CompiledNode as their sources.
     */
    List<EdgeProxy> getEdges()
    {
      return mmEdges;
    }

    /**
     * @return A collection of causes ({@link NodeProxy}) of the given
     *         identifier.
     */
    Collection<NodeProxy> getCauses(final Proxy identifier)
    {
      final List<NodeProxy> result = new ArrayList<>();
      if (mmUsedEvents.getByProxy(identifier) != null)
        for (final CauseInfo c : mmUsedEvents.getByProxy(identifier))
          result.add(c.getCause());
      return result;
    }

    /**
     * @return A list of guards ({@link SimpleExpressionProxy}) that
     *         are associated with the given identifier.
     */
    List<SimpleExpressionProxy> getGuards(final Proxy identifier)
    {
      final List<SimpleExpressionProxy> result = new ArrayList<>();
      if (mmUsedEvents.getByProxy(identifier) != null)
        for (final CauseInfo c : mmUsedEvents.getByProxy(identifier))
          result.add(c.getGuard());
      return result;
    }

    /**
     * Adds a new CauseInfo to the list of used events of this CompiledNode.
     *
     * @param identifier
     * @param cause
     * @param guard
     */
    void addCause(final Proxy identifier, final NodeProxy cause,
                  final SimpleExpressionProxy guard)
    {
      Collection<CauseInfo> list = mmUsedEvents.getByProxy(identifier);
      if (list == null) {
        list = new ArrayList<>();
        mmUsedEvents.putByProxy(identifier, list);
      }
      list.add(new CauseInfo(cause, guard));
    }

    //#######################################################################
    //# Data Members
    private final NodeProxy mmNodeProxy;
    private final List<CompiledNode> mmChildren = new ArrayList<>();
    private final List<EdgeProxy> mmEdges = new ArrayList<>();

    //Ordered pairs of the form: (Identifier, CauseInfo)
    private final ProxyAccessorMap<Proxy, Collection<CauseInfo>>
                         mmUsedEvents = new ProxyAccessorHashMap<>(mEquality);
  }


  //#########################################################################
  //# Inner Class: CauseInfo
  /**
   * A CauseInfo contains the cause of a particular edge, as well as the
   * guard associated with it.
   */
  private class CauseInfo
  {
    //#######################################################################
    //# Constructor
    private CauseInfo(final NodeProxy cause,
                      final SimpleExpressionProxy guard)
    {
      mmCause = cause;
      mmGuard = guard;
    }

    //#######################################################################
    //# Access Methods
    NodeProxy getCause()
    {
      return mmCause;
    }

    SimpleExpressionProxy getGuard()
    {
      return mmGuard;
    }

    //#######################################################################
    //# Data Members
    private final NodeProxy mmCause;
    private final SimpleExpressionProxy mmGuard;

  }


  //#########################################################################
  //# Data Members

  // These two variables are related to the entire instance of this class.
  private final ModuleProxy mInputModule;   // The original input module
  private List<ComponentProxy> mComponents; // The final list of components

  // These variables are related to only parts of the instance.
  private List<CompiledNode> mCompiledNodes; // All the nodes of a graph
  private List<EdgeProxy> mOldEdges; // All the edges of the original graph
  private List<EdgeProxy> mNewEdges; // All the edges of the new graph

  // These variables are utilities used by the compiler.
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final ModuleEqualityVisitor mEquality;
  private final ModuleProxyCloner mCloner;
  private boolean mIsAborting;
}
