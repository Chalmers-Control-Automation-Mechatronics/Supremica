//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMPartialUnfolderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * A test for the {@link EFSMPartialUnfolder}.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMPartialUnfolderTest
  extends AbstractEFSMTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public EFSMPartialUnfolderTest()
  {
  }

  public EFSMPartialUnfolderTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mPartialUnfolder = new EFSMPartialUnfolder(factory, optable);
    mPartialUnfolder.setSourceInfoEnabled(true);
    mVariablePartitionComputer =
      new EFSMVariablePartitionComputer(factory, optable);
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    mPartialUnfolder = null;
    mVariablePartitionComputer = null;
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/efsm/
   * unfolding01.wmod.
   * </P>
   *
   * <P>
   * All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton before unfolding. The automaton "after" defines the
   * expected result of partial unfolding. In addition, an automaton called
   * "selfloops" may be present, which contains additional updates to be
   * passed as selfloops to the partial unfolder.
   * </P>
   *
   * <P>
   * After running the test, a module containing the result of partial
   * unfolding is saved in {supremica}/logs/results/analysis/efsm/{classname}
   * as a .wmod file for viewing in the IDE.
   * </P>
   */
  public void testUnfolding_1() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding01");
    runPartialUnfolder(module);
  }

  public void testUnfolding_2() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding02");
    runPartialUnfolder(module);
  }

  public void testUnfolding_3() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding03");
    runPartialUnfolder(module);
  }

  /*
   * This case is handled by the compiler. No partial unfolding needed.
  public void testUnfolding_4() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding04");
    runPartialUnfolder(module);
  }
   */

  public void testUnfolding_5() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding05");
    runPartialUnfolder(module);
  }

  public void testUnfolding_6() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding06");
    runPartialUnfolder(module);
  }

  public void testUnfolding_7() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding07");
    runPartialUnfolder(module);
  }

  public void testUnfolding_8() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding08");
    runPartialUnfolder(module);
  }

  public void testUnfolding_9() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding09");
    runPartialUnfolder(module);
  }

  public void testUnfolding_10() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding10");
    runPartialUnfolder(module);
  }

  public void testUnfolding_11() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding11");
    runPartialUnfolder(module);
  }

  public void testUnfolding_12() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding12");
    runPartialUnfolder(module);
  }

  public void testPartitionUnfolding_11() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding11");
    runPartialUnfolder(module, true);
  }

  public void testPartitionUnfolding_12() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding12");
    runPartialUnfolder(module, true);
  }

  public void testReentrant() throws Exception
  {
    testUnfolding_8();
    testUnfolding_9();
    testUnfolding_10();
    testPartitionUnfolding_11();
    testUnfolding_9();
    testPartitionUnfolding_12();
    testUnfolding_12();
    testUnfolding_9();
    testUnfolding_10();
    testPartitionUnfolding_11();
    testUnfolding_9();
    testUnfolding_8();
    testUnfolding_7();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runPartialUnfolder(final ModuleProxy module)
    throws Exception
  {
    runPartialUnfolder(module, false);
  }

  private void runPartialUnfolder(final ModuleProxy module,
                                  final boolean partitioned)
    throws Exception
  {
    runPartialUnfolder(module, null, partitioned);
  }

  private void runPartialUnfolder(final ModuleProxy module,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean partitioned)
    throws Exception
  {
    final EFSMSystem system = createEFSMSystem(module, bindings);
    final EFSMVariable unfoldedVariable = system.getVariables().get(0);
    final TRPartition partition;
    if (partitioned) {
      partition = computePartition(unfoldedVariable, system);
    } else {
      partition = null;
    }
    final EFSMTransitionRelation resultTransitionRelation =
      mPartialUnfolder.unfold(unfoldedVariable, system, partition);
    saveResult(resultTransitionRelation, system, module);
    SimpleComponentProxy after = findComponent(module, AFTER);
    after = renameForPartition(after, unfoldedVariable, partition);
    compareWithAfter(resultTransitionRelation, system, module, after);
  }

  private TRPartition computePartition(final EFSMVariable var,
                                       final EFSMSystem system)
    throws EvalException, AnalysisException
  {
    return mVariablePartitionComputer.computePartition(var, system);
  }

  private SimpleComponentProxy renameForPartition
    (final SimpleComponentProxy oldComp,
     final EFSMVariable var,
     final TRPartition partition)
  {
    if (partition == null) {
      return oldComp;
    }

    final CompiledRange range = var.getRange();
    final int[] codeMap = new int[range.size()];
    int classno = 0;
    for (final int[] clazz : partition.getClasses()) {
      for (final int s : clazz) {
        codeMap[s] = classno;
      }
      classno++;
    }

    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxyCloner cloner = factory.getCloner();
    final GraphProxy oldGraph = oldComp.getGraph();
    final Collection<NodeProxy> oldNodes = oldGraph.getNodes();
    final Collection<NodeProxy> newNodes =
      new ArrayList<NodeProxy>(oldNodes.size());
    final Map<NodeProxy,NodeProxy> nodeMap =
      new HashMap<NodeProxy,NodeProxy>(oldNodes.size());
    for (final NodeProxy node : oldNodes) {
      assertTrue("Only simple nodes supported!",
                 node instanceof SimpleNodeProxy);
      final SimpleNodeProxy oldNode = (SimpleNodeProxy) node;
      final String oldName = oldNode.getName();
      final int colon = oldName.indexOf(':');
      final String tail = oldName.substring(colon + 1);
      final int oldCode = Integer.parseInt(tail);
      final int newCode = codeMap[oldCode];
      final String prefix = oldName.substring(0, colon);
      final String newName = prefix + ":" + newCode;
      final boolean init = oldNode.isInitial();
      final PlainEventListProxy oldProps = oldNode.getPropositions();
      final PlainEventListProxy newProps =
        (PlainEventListProxy) cloner.getClone(oldProps);
      final SimpleNodeProxy newNode =
        factory.createSimpleNodeProxy(newName, newProps, null, init,
                                      null, null, null);
      newNodes.add(newNode);
      nodeMap.put(oldNode, newNode);
    }
    final LabelBlockProxy oldBlocked = oldGraph.getBlockedEvents();
    final LabelBlockProxy newBlocked =
      (LabelBlockProxy) cloner.getClone(oldBlocked);
    final Collection<EdgeProxy> oldEdges = oldGraph.getEdges();
    final Collection<EdgeProxy> newEdges =
      new ArrayList<EdgeProxy>(oldEdges.size());
    for (final EdgeProxy oldEdge : oldEdges) {
      final NodeProxy oldSource = oldEdge.getSource();
      final NodeProxy newSource = nodeMap.get(oldSource);
      final NodeProxy oldTarget = oldEdge.getTarget();
      final NodeProxy newTarget = nodeMap.get(oldTarget);
      final LabelBlockProxy oldBlock = oldEdge.getLabelBlock();
      final LabelBlockProxy newBlock =
        (LabelBlockProxy) cloner.getClone(oldBlock);
      final GuardActionBlockProxy oldGA = oldEdge.getGuardActionBlock();
      final GuardActionBlockProxy newGA =
        (GuardActionBlockProxy) cloner.getClone(oldGA);
      final EdgeProxy newEdge =
        factory.createEdgeProxy(newSource, newTarget, newBlock, newGA,
                                null, null, null);
      newEdges.add(newEdge);
    }
    final boolean det = oldGraph.isDeterministic();
    final GraphProxy newGraph =
      factory.createGraphProxy(det, newBlocked, newNodes, newEdges);
    final IdentifierProxy oldIdent = oldComp.getIdentifier();
    final IdentifierProxy newIdent =
      (IdentifierProxy) cloner.getClone(oldIdent);
    final ComponentKind kind = oldComp.getKind();
    return factory.createSimpleComponentProxy(newIdent, kind, newGraph);
  }


  //#########################################################################
  //# Data Members
  private EFSMPartialUnfolder mPartialUnfolder;
  private EFSMVariablePartitionComputer mVariablePartitionComputer;

}
