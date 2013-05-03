package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class PromelaGraph
{
  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges,final PromelaNode start)
  {
    mPromelaNodes = nodes;
    mPromelaEdges = edges;

    mPromelaStartNode = start;
    //mPromelaEndNode = end;
  }

  public PromelaGraph()
  {
    mPromelaNodes = new ArrayList<PromelaNode>();
    mPromelaStartNode = new PromelaNode();
    mPromelaNodes.add(mPromelaStartNode);
    final PromelaNode promelaEndNode = new PromelaNode(PromelaNode.EndType.END);
    mPromelaNodes.add(promelaEndNode);
  }

  public void addEdge(final PromelaNode source, final PromelaNode target,
                      final List<IdentifierProxy> events, final ModuleProxyFactory mFactory)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(cloner.getClonedList(events));
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    Collections.sort(tempLabel, comparator);
    final PromelaLabel label = new PromelaLabel(tempLabel);
    final PromelaEdge edge = new PromelaEdge(source, target, label);
    mPromelaEdges.add(edge);
  }

  public void addEdge(final PromelaNode source, final PromelaNode target,
                      final List<IdentifierProxy> events, final ModuleProxyFactory mFactory, final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(cloner.getClonedList(events));
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    Collections.sort(tempLabel, comparator);
    final PromelaLabel label = new PromelaLabel(tempLabel);
    final PromelaEdge edge = new PromelaEdge(source, target, label, guards, actions);
    mPromelaEdges.add(edge);
  }

  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges){
    mPromelaNodes = nodes;
    mPromelaEdges = edges;
  }

  public PromelaGraph(final IdentifierProxy ident)
  {
    mPromelaStartNode = new PromelaNode();
    final PromelaNode promelaEndNode = new PromelaNode(PromelaNode.EndType.END);

    final Collection<SimpleExpressionProxy> labelBlock = new ArrayList<SimpleExpressionProxy>();
    labelBlock.add(ident);
    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(mPromelaStartNode,promelaEndNode,label);
    mPromelaEdges.add(edge);

    mPromelaNodes.add(mPromelaStartNode);
    mPromelaNodes.add(promelaEndNode);

  }

  public PromelaGraph(final List<IdentifierProxy> events, final ModuleProxyFactory mFactory){
    mPromelaStartNode = new PromelaNode();
    final PromelaNode promelaEndNode = new PromelaNode(PromelaNode.EndType.END);
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(cloner.getClonedList(events));
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    if(tempLabel.size()>1){
      Collections.sort(tempLabel, comparator);
    }
    //final Collection<SimpleExpressionProxy> labelBlock = new ArrayList<SimpleExpressionProxy>(tempLabel);

    final PromelaLabel label = new PromelaLabel(tempLabel);
   // if(tempLabel.size()>0){
      final PromelaEdge edge = new PromelaEdge(mPromelaStartNode, promelaEndNode, label);
      mPromelaEdges.add(edge);
   // }
    mPromelaNodes.add(mPromelaStartNode);
    mPromelaNodes.add(promelaEndNode);

  }

  public PromelaGraph(final List<IdentifierProxy> events, final boolean isEnd, final ModuleProxyFactory mFactory){
    mPromelaStartNode = new PromelaNode();
    mPromelaStartNode.setAccepting(isEnd);
    final PromelaNode promelaEndNode = new PromelaNode(PromelaNode.EndType.END);
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(cloner.getClonedList(events));
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
    if(tempLabel.size()>1){
      Collections.sort(tempLabel, comparator);
    }
    //final Collection<SimpleExpressionProxy> labelBlock = new ArrayList<SimpleExpressionProxy>(tempLabel);

    final PromelaLabel label = new PromelaLabel(tempLabel);
    //if(tempLabel.size()>0){
      final PromelaEdge edge = new PromelaEdge(mPromelaStartNode, promelaEndNode, label);
      mPromelaEdges.add(edge);
    //}
    mPromelaNodes.add(mPromelaStartNode);
    mPromelaNodes.add(promelaEndNode);

  }

  /*
   * connect end node of first graph to start node of second graph
   */
  public static PromelaGraph sequentialComposition (final PromelaGraph first, final PromelaGraph second,final boolean unwind,final ModuleProxyFactory factory)
  {
    if(first==null)
    {
      //unwind = false;
      return second;
    }
    else if(second ==null)
    {
     // unwind = false;
      return first;
    }
    else
    {
      PromelaGraph output=null;
      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();

      final List<PromelaEdge> edgesOfSecond = second.getEdges();

      final List<PromelaNode> nodesOfSecond = second.getNodes();
      // Use start node of second graph for this.


      final List<PromelaEdge> edgesOfResult = new ArrayList<PromelaEdge>(edgesOfFirst.size() + edgesOfSecond.size());

      //consider break statement
      if(nodesOfSecond.size()==1)
      {
        if(nodesOfSecond.get(0).isBreak())
        {
          for(final PromelaEdge edge : edgesOfFirst)
          {
            if(edge.getTarget().isEnd())
            {
              //first.getEnd().setBreak(true);
              edge.getTarget().setBreak(true);
              edgesOfResult.add(edge);
            }
            else
            {
              edgesOfResult.add(edge);
            }
          }
          final List<PromelaNode> nodesOfResult = new ArrayList<PromelaNode>(nodesOfFirst.size());
          for(final PromelaNode node : nodesOfFirst)
          {
              nodesOfResult.add(node);
          }
          output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
        }
        else if(nodesOfSecond.get(0).isGoto())
        {
          for(final PromelaEdge edge : edgesOfFirst)
          {
            if(edge.getTarget().isEnd())
            {
              final PromelaNode sourceNode = edge.getSource();
              final PromelaLabel label = edge.getLabelBlock();
              final PromelaEdge newEdge = new PromelaEdge(sourceNode,nodesOfSecond.get(0),label, edge.getGuards(), edge.getActions());

              edgesOfResult.add(newEdge);
            }
            else if(edge.getSource().isEnd())
            {
              final PromelaNode targetNode = edge.getTarget();
              final PromelaLabel label =  new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
              final PromelaEdge newEdge = new PromelaEdge(nodesOfSecond.get(0),targetNode,label,edge.getGuards(), edge.getActions());

              edgesOfResult.add(newEdge);
            }
            else
            {
              edgesOfResult.add(edge);
            }
          }
          //possible bug if first graph only has 1 node
          final List<PromelaNode> nodesOfResult = new ArrayList<PromelaNode>(nodesOfFirst.size());
          for(final PromelaNode node : nodesOfFirst)
          {
            if(!node.isEnd())
            {
              nodesOfResult.add(node);
            }
          }
          nodesOfResult.add(nodesOfSecond.get(0));
          output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
        }
        else
        {
          for(final PromelaEdge edge : edgesOfFirst)
          {
            if(edge.getTarget().isEnd())
            {
              final PromelaNode sourceNode = edge.getSource();
              final PromelaLabel label = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
              final PromelaEdge newEdge = new PromelaEdge(sourceNode,nodesOfSecond.get(0),label, edge.getGuards(), edge.getActions());

              edgesOfResult.add(newEdge);
            }
            else if(edge.getSource().isEnd())
            {
              final PromelaNode targetNode = edge.getTarget();
              final PromelaLabel label = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
              final PromelaEdge newEdge = new PromelaEdge(nodesOfSecond.get(0),targetNode,label, edge.getGuards(), edge.getActions());

              edgesOfResult.add(newEdge);
            }
            else
            {
              edgesOfResult.add(edge);
            }
          }
          //possible bug if first graph only has 1 node
          final List<PromelaNode> nodesOfResult = new ArrayList<PromelaNode>(nodesOfFirst.size());
          for (final PromelaNode node : nodesOfFirst)
          {
            if(!node.isEnd())
            {
              nodesOfResult.add(node);
            }
          }
          nodesOfResult.add(nodesOfSecond.get(0));
          output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
        }
      }
      else
      {
        for (final PromelaEdge edge : edgesOfFirst)
        {
          if(edge.getTarget().isEnd())
          {
            final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
            final PromelaNode sourceNode = edge.getSource();
            final PromelaEdge newEdge = new PromelaEdge(sourceNode, second.getStart(), label1, edge.getGuards(), edge.getActions());
            edgesOfResult.add(newEdge);
          }
          else
          {
            edgesOfResult.add(edge);
          }
        }
        for(final PromelaEdge edge : edgesOfSecond)
        {
          if(edge.getSource() == second.getStart())
          {
            final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
            final PromelaNode targetNode = edge.getTarget();
            final PromelaEdge newEdge;
            if(targetNode==edge.getSource())
            {
              newEdge = new PromelaEdge(second.getStart(),second.getStart(),label1, edge.getGuards(), edge.getActions());
            }
            else
            {
              newEdge = new PromelaEdge(second.getStart(), targetNode, label1, edge.getGuards(), edge.getActions());
            }
            edgesOfResult.add(newEdge);
          }
          else if(edge.getTarget()==second.getStart())
          {
            final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
            final PromelaNode targetNode = edge.getSource();
            final PromelaEdge newEdge;
            if(targetNode==edge.getTarget())
            {
              newEdge = new PromelaEdge(second.getStart(),second.getStart(),label1, edge.getGuards(), edge.getActions());
            }
            else
            {
              newEdge = new PromelaEdge(targetNode,second.getStart(),label1, edge.getGuards(), edge.getActions());
            }
            edgesOfResult.add(newEdge);
          }
          else
          {
            edgesOfResult.add(edge);
          }
        }

        final List<PromelaNode> nodesOfResult = new ArrayList<PromelaNode>(nodesOfFirst.size()+nodesOfSecond.size()-1);
        for(final PromelaNode node : nodesOfFirst)
        {
          if(!node.isEnd())
          {
            nodesOfResult.add(node);
          }
        }
        nodesOfResult.add(second.getStart());
        for(final PromelaNode node : nodesOfSecond)
        {
          if(node!=second.getStart())
          {
            nodesOfResult.add(node);
          }
        }
        if(edgesOfFirst.size()!=0)
        {
          output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
        }
        else
        {
          output = new PromelaGraph(nodesOfResult,edgesOfResult,second.getStart());
        }
      }
      return output;
    }
  }

  //TODO Fix this so that the edges have the guards and actions
  /*
   * used for "if" statement, combine both start and end nodes of first and second graph together
   */
  public static PromelaGraph combineComposition(final PromelaGraph first,
                                                final PromelaGraph second,
                                                final boolean unwind,
                                                final ModuleProxyFactory factory)
  {
    if(first==null)
    {
      return second;
    }
    else if(second ==null)
    {
      return first;
    }
    else
    {
      final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
      final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);

      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      PromelaGraph output = null;

      final PromelaNode newStartNode = new PromelaNode();
      final PromelaNode newEndNode = new PromelaNode(PromelaNode.EndType.END);
      final List<PromelaEdge> edgesOfResult = new ArrayList<PromelaEdge>();  // edgesOfFirst.size() + edgesOfSecond.size()

      PromelaLabel label1;
      PromelaLabel label2;
      PromelaNode targetNode,sourceNode;
      PromelaEdge newEdge;
      PromelaEdge specialEdge1=null;
      PromelaEdge specialEdge2 = null;
      final Collection<SimpleExpressionProxy> specialLabel = new ArrayList<SimpleExpressionProxy>();

      for(final PromelaEdge edge : edgesOfFirst)
      {
        if(edge.getSource() == first.getStart()&& !edge.getTarget().isEnd())
        {
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label1, edge.getGuards(), edge.getActions());
          edgesOfResult.add(newEdge);
        }
        else if(edge.getTarget().isEnd()&& edge.getSource()!=first.getStart())
        {
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label1, edge.getGuards(), edge.getActions());
          edgesOfResult.add(newEdge);
        }
        else if(edge.getTarget().isEnd() && edge.getSource()==first.getStart())
        {
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: label1.getLabel())
          {
            for(final SimpleExpressionProxy e2: specialLabel)
            {
              if(comparator.compare(e, e2)==0)
              {
                continue loop1;
              }
            }

            specialLabel.add(e);
          }
          //specialLabel.addAll(label1.getLabel());
          specialEdge1 = new PromelaEdge(newStartNode,newEndNode,label1, edge.getGuards(), edge.getActions());
          if(!edgesOfResult.contains(specialEdge1))
          {
            edgesOfResult.add(specialEdge1);
          }
          else
          {
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge1));
            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel)
            {
              for(final SimpleExpressionProxy e2: l)
              {
                if(comparator.compare(e, e2)==0)
                {
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label, edge.getGuards(), edge.getActions());
            edgesOfResult.remove(specialEdge1);
            edgesOfResult.add(e);
          }
          //edgesOfResult.add(specialEdge1);
        }
        else
        {
          edgesOfResult.add(edge);
        }
      }

      for(final PromelaEdge edge : edgesOfSecond)
      {
        if(edge.getSource() == second.getStart()&& !edge.getTarget().isEnd())
        {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label2, edge.getGuards(), edge.getActions());
          edgesOfResult.add(newEdge);
        }
        else if(edge.getTarget().isEnd()&& edge.getSource()!=second.getStart())
        {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label2, edge.getGuards(), edge.getActions());
          edgesOfResult.add(newEdge);
        }
        else if(edge.getTarget().isEnd() && edge.getSource()==second.getStart())
        {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
            for(final SimpleExpressionProxy e: label2.getLabel())
            {
              for(final SimpleExpressionProxy e2: specialLabel)
              {
                if(comparator.compare(e, e2)==0)
                {
                  continue loop1;
                }
              }
              specialLabel.add(e);
            }

          //specialLabel.addAll(label2.getLabel());
          //PromelaLabel label = new PromelaLabel(label2);
          specialEdge2 = new PromelaEdge(newStartNode,newEndNode,label2, edge.getGuards(), edge.getActions());
          if(!edgesOfResult.contains(specialEdge2))
          {
            edgesOfResult.add(specialEdge2);
          }
          else
          {
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge2));

            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel)
            {
              for(final SimpleExpressionProxy e2: l)
              {
                if(comparator.compare(e, e2)==0)
                {
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label, edge.getGuards(), edge.getActions());
            edgesOfResult.remove(specialEdge2);
            edgesOfResult.add(e);
          }
        }
        else if(edge.getTarget().isBreak()&& edge.getSource()==second.getStart())
        {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: label2.getLabel())
          {
            for(final SimpleExpressionProxy e2: specialLabel)
            {
              if(comparator.compare(e, e2)==0)
              {
                continue loop1;
              }
            }
            specialLabel.add(e);
          }

          //specialLabel.addAll(label2.getLabel());
          newEndNode.setBreak(true);
          //PromelaLabel label = new PromelaLabel(specialLabel);
          specialEdge2 = new PromelaEdge(newStartNode,newEndNode,label2, edge.getGuards(), edge.getActions());
          //edgesOfResult.add(specialEdge2);
          if(!edgesOfResult.contains(specialEdge2))
          {
            edgesOfResult.add(specialEdge2);
          }
          else
          {
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge2));

            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel)
            {
              for(final SimpleExpressionProxy e2: l)
              {
                if(comparator.compare(e, e2)==0)
                {
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label, edge.getGuards(), edge.getActions());
            edgesOfResult.remove(specialEdge2);
            edgesOfResult.add(e);
          }
        }
        else if(edge.getTarget().isBreak()&& edge.getSource()!=second.getStart())
        {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label2, edge.getGuards(), edge.getActions());
          edgesOfResult.add(newEdge);
        }
        else
        {
          edgesOfResult.add(edge);
        }
      }

      final List<PromelaNode> nodesOfResult = new ArrayList<PromelaNode>();  //nodesOfFirst.size()+nodesOfSecond.size()-2
      for (final PromelaNode node : nodesOfFirst)
      {
        if(node!=first.getStart()&& !node.isEnd())
        {
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newStartNode);
      for (final PromelaNode node : nodesOfSecond)
      {
        if(node!=second.getStart()&& !node.isEnd())
        {
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,newStartNode);

      return output;
    }
  }

  public static PromelaGraph doCombineComposition2(final List<PromelaGraph> branches,
                                                   final boolean unwinding,
                                                   final ModuleProxyFactory factory)
  {
    //comparator
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    //create nodes
    final List<PromelaNode> nodes = new ArrayList<PromelaNode>();
    final PromelaNode newStartNode = new PromelaNode();
    PromelaNode secondStart;
    nodes.add(newStartNode);
    for(final PromelaGraph branch: branches)
    {
      for(final PromelaNode node: branch.getNodes())
      {
        if(node!=branch.getStart() && !node.isBreak()&& !node.isEnd())
        {
            nodes.add(node);
        }
      }
    }
    if(unwinding)
    {
      //TODO
      secondStart = new PromelaNode();
      nodes.add(secondStart);
    }
    else
    {
      secondStart = newStartNode;
    }

    final PromelaNode newEndNode = new PromelaNode(PromelaNode.EndType.END);
    nodes.add(newEndNode);

    //create edges
    final List<PromelaEdge> edges = new ArrayList<PromelaEdge>();
    for(final PromelaGraph branch: branches)
    {
      for(final PromelaEdge edge: branch.getEdges())
      {
        PromelaNode source,target;
        if(edge.getSource()==branch.getStart())
        {
          source = newStartNode;
        }
        else
        {
          source = edge.getSource();
        }

        if(edge.getTarget().isEnd())
        {
          target = secondStart;
        }
        else if(edge.getTarget().isBreak())
        {
          target = newEndNode;
        }
        else
        {
          target = edge.getTarget();
        }

        final Collection<SimpleExpressionProxy> normalLabel = new ArrayList<SimpleExpressionProxy>();
        normalLabel.addAll(edge.getLabelBlock().getCloneLabel(factory));
        PromelaLabel label = new PromelaLabel(normalLabel);
        final PromelaEdge newEdge = new PromelaEdge(source,target,label, edge.getGuards(), edge.getActions());

        if(!edges.contains(newEdge))
        {
          edges.add(newEdge);
        }
        else
        {
          final PromelaEdge tempEdge = edges.get(edges.indexOf(newEdge));
          edges.remove(newEdge);
          final List<SimpleExpressionProxy> l2 = new ArrayList<SimpleExpressionProxy>();
          l2.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: edge.getLabelBlock().getCloneLabel(factory))
          {
            for(final SimpleExpressionProxy check: tempEdge.getLabelBlock().getCloneLabel(factory))
            {
              if(comparator.compare(e, check)==0)
              {
                continue loop1;
              }
            }
            l2.add(e);
          }
          // l2.addAll(edge.getLabelBlock().getCloneLabel(factory));
          Collections.sort(l2, comparator);
          label = new PromelaLabel(l2);
          final PromelaEdge duplicateEdge = new PromelaEdge(source,target,label, newEdge.getGuards(), newEdge.getActions());
          edges.add(duplicateEdge);
        }

        if(unwinding && source==newStartNode)
        {
          final Collection<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();

          l.addAll(edge.getLabelBlock().getCloneLabel(factory));
          final PromelaLabel label2 = new PromelaLabel(l);
          final PromelaEdge newEdge2 = new PromelaEdge(secondStart,target,label2,edge.getGuards(), edge.getActions());

          if(!edges.contains(newEdge2))
          {
            edges.add(newEdge2);
          }
          else
          {
            final PromelaEdge tempEdge = edges.get(edges.indexOf(newEdge2));
            edges.remove(newEdge2);
            final List<SimpleExpressionProxy> l2 = new ArrayList<SimpleExpressionProxy>();
            l2.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
              for(final SimpleExpressionProxy e: edge.getLabelBlock().getCloneLabel(factory))
              {
                for(final SimpleExpressionProxy check: tempEdge.getLabelBlock().getCloneLabel(factory))
                {
                  if(comparator.compare(e, check)==0)
                  {
                    continue loop1;
                  }
                }
                l2.add(e);
              }
            //l2.addAll(edge.getLabelBlock().getCloneLabel(factory));
            Collections.sort(l2, comparator);
            label = new PromelaLabel(l2);
            final PromelaEdge duplicateEdge = new PromelaEdge(secondStart,target,label,newEdge.getGuards(), newEdge.getActions());
            edges.add(duplicateEdge);
          }
        }
      }
    }

    //create graph
    final PromelaGraph result = new PromelaGraph(nodes,edges,newStartNode);
    return result;
  }

  public GraphProxy createGraphProxy(final ModuleProxyFactory mFactory, final String name)
  {
    int index = 0;
    SimpleNodeProxy proxy;
    for (final PromelaNode node : getNodes())
    {
      final boolean initial = (node == this.getStart());

      final boolean marked = (node.isEnd() || node.isAccepting());
      proxy = node.createNode(name, index++, initial, marked, mFactory);
      mNodes.add(proxy);
    }

    final ModuleProxyCloner cloner = mFactory.getCloner();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);

    for(final PromelaEdge e : this.getEdges())
    {
      final Collection<Proxy> label = new ArrayList<Proxy>();
      label.addAll(e.getLabelBlock().getLabel());
      final NodeProxy source = e.getSource().getNode();
      final NodeProxy target = e.getTarget().getNode();
      if(!label.isEmpty())
      {
        final LabelBlockProxy labelBlock =
          mFactory.createLabelBlockProxy(label, null);

        final List<SimpleExpressionProxy> guards = e.getGuards();
        final List<BinaryExpressionProxy> actions = e.getActions();
        GuardActionBlockProxy guardAction = null;
        if(guards != null || actions != null)
          guardAction = mFactory.createGuardActionBlockProxy(guards, actions, null);

        final EdgeProxy edge = mFactory.createEdgeProxy(source, target, labelBlock, guardAction, null, null, null);
        mEdges.add(edge);
      }
    }
    boolean isDetermine = true;
    loop1:
    for(final PromelaNode node: getNodes())
    {
      final List<SimpleExpressionProxy> labels = new ArrayList<SimpleExpressionProxy>();
      for(final PromelaEdge e: getEdges())
      {
        if(e.getSource()==node)
        {
          labels.addAll(cloner.getClonedList(e.getLabelBlock().getLabel()));
        }
      }
      for(int i=0;i<labels.size();i++)
      {
        for(int j=i+1;j<labels.size();j++)
        {
          if(comparator.compare(labels.get(i), labels.get(j))==0)
          {
            isDetermine = false;
            break loop1;
          }
        }
      }
    }

    final GraphProxy graph = mFactory.createGraphProxy(isDetermine, null, mNodes, mEdges);
    return graph;
  }

  public List<PromelaNode> getNodes(){
    return mPromelaNodes;
  }

  public List<PromelaEdge> getEdges(){
    return mPromelaEdges;
  }

  public void setStart(final PromelaNode newStart){
    mPromelaStartNode =newStart ;
  }

  public void setNodes(final ArrayList<PromelaNode> nodes){
    mPromelaNodes.clear();
    for(final PromelaNode n: nodes){
      mPromelaNodes.add(n);
    }
  }

  public void setEdges(final ArrayList<PromelaEdge> edges){
    mPromelaEdges.clear();
    for(final PromelaEdge n: edges){
      mPromelaEdges.add(n);
    }
  }

  public PromelaNode getStart(){
    return mPromelaStartNode;
  }

  //#########################################################################
  //# Data Members
  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
  private PromelaNode mPromelaStartNode;

  private List<PromelaNode> mPromelaNodes = new ArrayList<PromelaNode>();
  private List<PromelaEdge> mPromelaEdges = new ArrayList<PromelaEdge>();

}
