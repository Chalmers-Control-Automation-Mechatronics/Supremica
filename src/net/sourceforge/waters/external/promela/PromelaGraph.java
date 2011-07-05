package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class PromelaGraph
{
  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges,final PromelaNode start){
    mPromelaNodes = nodes;
    mPromelaEdges = edges;

    mPromelaStartNode = start;
    //mPromelaEndNode = end;
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

  public PromelaGraph(final List<IdentifierProxy> events, final ModuleProxyFactory factory){
    mPromelaStartNode = new PromelaNode();
    final PromelaNode promelaEndNode = new PromelaNode(PromelaNode.EndType.END);
    System.out.println(events.size());
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(events);
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
    if(tempLabel.size()>1){
      Collections.sort(tempLabel, comparator);
    }
    final Collection<SimpleExpressionProxy> labelBlock = new ArrayList<SimpleExpressionProxy>(tempLabel);

    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(mPromelaStartNode, promelaEndNode, label);
    mPromelaEdges.add(edge);
    mPromelaNodes.add(mPromelaStartNode);
    mPromelaNodes.add(promelaEndNode);

  }
  /*
   * connect end node of first graph to start node of second graph
   */
  public static PromelaGraph sequentialComposition (final PromelaGraph first, final PromelaGraph second,final boolean unwind,final ModuleProxyFactory factory){
    if(first==null){
      //unwind = false;
      return second;
    }else if(second ==null){
     // unwind = false;
      return first;
    }else{
      PromelaGraph output=null;
      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();

      final List<PromelaEdge> edgesOfSecond = second.getEdges();

      final List<PromelaNode> nodesOfSecond = second.getNodes();
      // Use start node of second graph for this.


      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>(edgesOfFirst.size() + edgesOfSecond.size());

      //consider break statement
      if(nodesOfSecond.size()==1){

          if(nodesOfSecond.get(0).isBreak()){


            for (final PromelaEdge edge : edgesOfFirst) {
              if (edge.getTarget().isEnd()) {

             //   first.getEnd().setBreak(true);

                edge.getTarget().setBreak(true);
                edgesOfResult.add(edge);
              } else {
                edgesOfResult.add(edge);
              }
            }
            final List<PromelaNode> nodesOfResult =
              new ArrayList<PromelaNode>(nodesOfFirst.size());
            for (final PromelaNode node : nodesOfFirst) {

                nodesOfResult.add(node);

            }
            output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());

          }
          else if(nodesOfSecond.get(0).isGoto()){
            for (final PromelaEdge edge : edgesOfFirst) {
              if (edge.getTarget().isEnd()) {
                final PromelaNode sourceNode = edge.getSource();
                final PromelaLabel label = edge.getLabelBlock();
                final PromelaEdge newEdge = new PromelaEdge(sourceNode,nodesOfSecond.get(0),label);

                edgesOfResult.add(newEdge);
              } else if(edge.getSource().isEnd()){
                final PromelaNode targetNode = edge.getTarget();
                final PromelaLabel label =  new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
                final PromelaEdge newEdge = new PromelaEdge(nodesOfSecond.get(0),targetNode,label);

                edgesOfResult.add(newEdge);
              }
                else {

                edgesOfResult.add(edge);
              }
            }
            //possible bug if first graph only has 1 node
            final List<PromelaNode> nodesOfResult =
              new ArrayList<PromelaNode>(nodesOfFirst.size());
            for (final PromelaNode node : nodesOfFirst) {
              if(!node.isEnd()){
                nodesOfResult.add(node);
              }
            }
            nodesOfResult.add(nodesOfSecond.get(0));
            output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
          }else{
            for (final PromelaEdge edge : edgesOfFirst) {
              if (edge.getTarget().isEnd()) {
                final PromelaNode sourceNode = edge.getSource();
                final PromelaLabel label = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
                final PromelaEdge newEdge = new PromelaEdge(sourceNode,nodesOfSecond.get(0),label);

                edgesOfResult.add(newEdge);
              } else if(edge.getSource().isEnd()){
                final PromelaNode targetNode = edge.getTarget();
                final PromelaLabel label = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
                final PromelaEdge newEdge = new PromelaEdge(nodesOfSecond.get(0),targetNode,label);

                edgesOfResult.add(newEdge);
              }
                else {

                edgesOfResult.add(edge);
              }
            }
            //possible bug if first graph only has 1 node
            final List<PromelaNode> nodesOfResult =
              new ArrayList<PromelaNode>(nodesOfFirst.size());
            for (final PromelaNode node : nodesOfFirst) {
              if(!node.isEnd()){
                nodesOfResult.add(node);
              }
            }
            nodesOfResult.add(nodesOfSecond.get(0));
            output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
          }
      }else{


      for (final PromelaEdge edge : edgesOfFirst) {
        if (edge.getTarget().isEnd()) {
          final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          final PromelaNode sourceNode = edge.getSource();
          final PromelaEdge newEdge = new PromelaEdge(sourceNode, second.getStart(), label1);
          edgesOfResult.add(newEdge);
        } else {
          edgesOfResult.add(edge);
        }
      }
      for (final PromelaEdge edge : edgesOfSecond) {
        if (edge.getSource() == second.getStart()) {
          final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          final PromelaNode targetNode = edge.getTarget();
          final PromelaEdge newEdge;
          if(targetNode==edge.getSource()){
            newEdge = new PromelaEdge(second.getStart(),second.getStart(),label1);
          }else{
            newEdge = new PromelaEdge(second.getStart(), targetNode, label1);
          }
          edgesOfResult.add(newEdge);
        }else if(edge.getTarget()==second.getStart()){
          final PromelaLabel label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          final PromelaNode targetNode = edge.getSource();
          final PromelaEdge newEdge;
          if(targetNode==edge.getTarget()){
            newEdge = new PromelaEdge(second.getStart(),second.getStart(),label1);
          }else{
            newEdge = new PromelaEdge(targetNode,second.getStart(),label1);
          }
          edgesOfResult.add(newEdge);

        }else {
          edgesOfResult.add(edge);
        }
      }

      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>(nodesOfFirst.size()+nodesOfSecond.size()-1);
      for (final PromelaNode node : nodesOfFirst) {
        if(!node.isEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(second.getStart());
      for (final PromelaNode node : nodesOfSecond) {
        if(node!=second.getStart()){
          nodesOfResult.add(node);
        }

      }
      if(edgesOfFirst.size()!=0){
        output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());
      }else{
        output = new PromelaGraph(nodesOfResult,edgesOfResult,second.getStart());
      }

      }

      return output;
    }


  }
  /*
   * used for "if" statement, combine both start and end nodes of first and second graph together
   */
  public static PromelaGraph combineComposition(final PromelaGraph first,
                                                final PromelaGraph second,
                                                final boolean unwind,
                                                final ModuleProxyFactory factory)
  {


    if(first==null){
      return second;
    }else if(second ==null){
      return first;
    }else{
      final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
      final Comparator<SimpleExpressionProxy> comparator =
        new ExpressionComparator(optable);

      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      PromelaGraph output = null;

      final PromelaNode newStartNode = new PromelaNode();
      final PromelaNode newEndNode = new PromelaNode(PromelaNode.EndType.END);
      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();  // edgesOfFirst.size() + edgesOfSecond.size()

      PromelaLabel label1;
      PromelaLabel label2;
      PromelaNode targetNode,sourceNode;
      PromelaEdge newEdge;
      PromelaEdge specialEdge1=null;
      PromelaEdge specialEdge2 = null;
      final Collection<SimpleExpressionProxy> specialLabel = new ArrayList<SimpleExpressionProxy>();

      for (final PromelaEdge edge : edgesOfFirst) {
        if (edge.getSource() == first.getStart()&& !edge.getTarget().isEnd()) {
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label1);
          edgesOfResult.add(newEdge);
        } else if(edge.getTarget().isEnd()&& edge.getSource()!=first.getStart()){
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label1);
          edgesOfResult.add(newEdge);
        }else if(edge.getTarget().isEnd() && edge.getSource()==first.getStart()){
          label1 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: label1.getLabel()){
            for(final SimpleExpressionProxy e2: specialLabel){
              if(comparator.compare(e, e2)==0){
                continue loop1;
              }
            }

            specialLabel.add(e);
          }
          //specialLabel.addAll(label1.getLabel());
          specialEdge1 = new PromelaEdge(newStartNode,newEndNode,label1);
          if(!edgesOfResult.contains(specialEdge1)){
            edgesOfResult.add(specialEdge1);
          }else{
            System.out.println("make sure");
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge1));
            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel){
              for(final SimpleExpressionProxy e2: l){
                if(comparator.compare(e, e2)==0){
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label);
            edgesOfResult.remove(specialEdge1);
            edgesOfResult.add(e);
          }
      //    edgesOfResult.add(specialEdge1);
        }
        else {
          edgesOfResult.add(edge);
        }
      }

      for (final PromelaEdge edge : edgesOfSecond) {
        if (edge.getSource() == second.getStart()&& !edge.getTarget().isEnd()) {
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label2);
          edgesOfResult.add(newEdge);
        }else if(edge.getTarget().isEnd()&& edge.getSource()!=second.getStart()){
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
          edgesOfResult.add(newEdge);
        }else if(edge.getTarget().isEnd() && edge.getSource()==second.getStart()){
          System.out.println(edge.getLabelBlock().getLabel().toString());
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
            for(final SimpleExpressionProxy e: label2.getLabel()){
              for(final SimpleExpressionProxy e2: specialLabel){
                if(comparator.compare(e, e2)==0){
                  continue loop1;
                }
              }
              specialLabel.add(e);
            }

          //specialLabel.addAll(label2.getLabel());
         // PromelaLabel label = new PromelaLabel(label2);
          specialEdge2 = new PromelaEdge(newStartNode,newEndNode,label2);
          if(!edgesOfResult.contains(specialEdge2)){
            edgesOfResult.add(specialEdge2);
          }else{
            System.out.println("make sure");
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge2));

            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel){
              for(final SimpleExpressionProxy e2: l){
                if(comparator.compare(e, e2)==0){
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label);
            edgesOfResult.remove(specialEdge2);
            edgesOfResult.add(e);
          }
        }else if(edge.getTarget().isBreak()&& edge.getSource()==second.getStart()){
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          loop1:
            for(final SimpleExpressionProxy e: label2.getLabel()){
              for(final SimpleExpressionProxy e2: specialLabel){
                if(comparator.compare(e, e2)==0){
                  continue loop1;
                }
              }
              specialLabel.add(e);
            }

          //specialLabel.addAll(label2.getLabel());
          newEndNode.setBreak(true);
          //PromelaLabel label = new PromelaLabel(specialLabel);
          specialEdge2 = new PromelaEdge(newStartNode,newEndNode,label2);
     //     edgesOfResult.add(specialEdge2);
          if(!edgesOfResult.contains(specialEdge2)){
            edgesOfResult.add(specialEdge2);
          }else{
            System.out.println("make sure");
            final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(specialEdge2));

            final List<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
            l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
            for(final SimpleExpressionProxy e: specialLabel){
              for(final SimpleExpressionProxy e2: l){
                if(comparator.compare(e, e2)==0){
                  continue loop1;
                }
              }
              l.add(e);
            }
            Collections.sort(l, comparator);
            final PromelaLabel label = new PromelaLabel(l);
            final PromelaEdge e = new PromelaEdge(newStartNode,newEndNode,label);
            edgesOfResult.remove(specialEdge2);
            edgesOfResult.add(e);
            }
        } else if(edge.getTarget().isBreak()&& edge.getSource()!=second.getStart()){
          label2 = new PromelaLabel(edge.getLabelBlock().getCloneLabel(factory));
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
          edgesOfResult.add(newEdge);
        }
        else {
          edgesOfResult.add(edge);
        }
      }
      /*
      if(simpleFirst&& simpleSecond){
      //  edgesOfResult.remove(specialEdge1);
      //  edgesOfResult.remove(specialEdge2);
        PromelaLabel label = new PromelaLabel(specialLabel);
        PromelaEdge edge = new PromelaEdge(newStartNode, newEndNode,label);
        if(!edgesOfResult.contains(edge)){
          edgesOfResult.add(edge);
        }else{
          System.out.println("make sure");
          final PromelaEdge tempEdge = edgesOfResult.get(edgesOfResult.indexOf(edge));
          final Collection<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();
          l.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: specialLabel){
            for(final SimpleExpressionProxy e2: l){
              if(e.getPlainText()==e2.getPlainText()){
                continue loop1;
              }
            }
            l.add(e);
          }
          label = new PromelaLabel(l);
          edge = new PromelaEdge(newStartNode,newEndNode,label);
          edgesOfResult.add(edge);
        }
        simpleFirst = false;
        simpleSecond = false;
      }
*/
      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();  //nodesOfFirst.size()+nodesOfSecond.size()-2
      for (final PromelaNode node : nodesOfFirst) {
        if(node!=first.getStart()&& !node.isEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newStartNode);
      for (final PromelaNode node : nodesOfSecond) {
        if(node!=second.getStart()&& !node.isEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,newStartNode);

      return output;
    }

  }

  public static PromelaGraph doCombineComposition2(final List<PromelaGraph> branches, final boolean unwinding,final ModuleProxyFactory factory){
    //comparator
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
    //create nodes
    final List<PromelaNode> nodes = new ArrayList<PromelaNode>();
    final PromelaNode newStartNode = new PromelaNode();
    PromelaNode secondStart;
    nodes.add(newStartNode);
    for(final PromelaGraph branch: branches){
      for(final PromelaNode node: branch.getNodes()){
        if(node!=branch.getStart() && !node.isBreak()&& !node.isEnd()){
            nodes.add(node);
        }
      }
    }
    if(unwinding){
      secondStart = new PromelaNode();
      nodes.add(secondStart);
    }else{
      secondStart = newStartNode;
    }
    final PromelaNode newEndNode = new PromelaNode(PromelaNode.EndType.END);
    nodes.add(newEndNode);

    //create edges
    final List<PromelaEdge> edges = new ArrayList<PromelaEdge>();
    for(final PromelaGraph branch: branches){
      for(final PromelaEdge edge: branch.getEdges()){
        PromelaNode source,target;
        if(edge.getSource()==branch.getStart()){
          source = newStartNode;
        }else{
          source = edge.getSource();
        }

        if(edge.getTarget().isEnd()){
          target = secondStart;
        }else if(edge.getTarget().isBreak()){
          target = newEndNode;
        }else{
          target = edge.getTarget();
        }

        final Collection<SimpleExpressionProxy> normalLabel = new ArrayList<SimpleExpressionProxy>();
        normalLabel.addAll(edge.getLabelBlock().getCloneLabel(factory));
        PromelaLabel label = new PromelaLabel(normalLabel);
        final PromelaEdge newEdge = new PromelaEdge(source,target,label);

        if(!edges.contains(newEdge)){
          edges.add(newEdge);
        }else{
          final PromelaEdge tempEdge = edges.get(edges.indexOf(newEdge));
          edges.remove(newEdge);
          final List<SimpleExpressionProxy> l2 = new ArrayList<SimpleExpressionProxy>();
          l2.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
          loop1:
          for(final SimpleExpressionProxy e: edge.getLabelBlock().getCloneLabel(factory)){
            for(final SimpleExpressionProxy check: tempEdge.getLabelBlock().getCloneLabel(factory)){
              if(comparator.compare(e, check)==0){
                continue loop1;
              }
            }
            l2.add(e);
          }
         // l2.addAll(edge.getLabelBlock().getCloneLabel(factory));
          Collections.sort(l2, comparator);
          label = new PromelaLabel(l2);
          final PromelaEdge duplicateEdge = new PromelaEdge(source,target,label);
          edges.add(duplicateEdge);
        }

        if(unwinding && source==newStartNode){
          final Collection<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>();

          l.addAll(edge.getLabelBlock().getCloneLabel(factory));
          final PromelaLabel label2 = new PromelaLabel(l);
          final PromelaEdge newEdge2 = new PromelaEdge(secondStart,target,label2);
          if(!edges.contains(newEdge2)){
            edges.add(newEdge2);
          }else{
            final PromelaEdge tempEdge = edges.get(edges.indexOf(newEdge2));
            edges.remove(newEdge2);
            final List<SimpleExpressionProxy> l2 = new ArrayList<SimpleExpressionProxy>();
            l2.addAll(tempEdge.getLabelBlock().getCloneLabel(factory));
            loop1:
              for(final SimpleExpressionProxy e: edge.getLabelBlock().getCloneLabel(factory)){
                for(final SimpleExpressionProxy check: tempEdge.getLabelBlock().getCloneLabel(factory)){
                  if(comparator.compare(e, check)==0){
                    continue loop1;
                  }
                }
                l2.add(e);
              }
            //l2.addAll(edge.getLabelBlock().getCloneLabel(factory));
            Collections.sort(l2, comparator);
            label = new PromelaLabel(l2);
            final PromelaEdge duplicateEdge = new PromelaEdge(secondStart,target,label);
            edges.add(duplicateEdge);
          }

        }
      }
    }

    //create graph
    final PromelaGraph result = new PromelaGraph(nodes,edges,newStartNode);
    return result;
  }

  /*
  public static PromelaGraph doCombineComposition(final PromelaGraph first,
                                                final PromelaGraph second,
                                                final PromelaNode start,
                                                final PromelaNode endNode,
                                                final PromelaNode secondStart,
                                                final ModuleProxyFactory factory,
                                                final String name,
                                                final Map<PromelaNode,PromelaEdge> toBreakNode,
                                                final boolean unwind)
  {
    if(!unwind){
    final PromelaNode newStartNode = start;
    final PromelaNode newEndNode = endNode;
    final Map<PromelaNode,PromelaEdge> breakNodeSource = toBreakNode;
    if(first==null){
    //  return second;
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      final List<PromelaEdge> indexEdge = new ArrayList<PromelaEdge>();
      final List<PromelaNode> indexNode = new ArrayList<PromelaNode>();
      PromelaGraph output = null;

      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();
      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();
      PromelaLabel label2;

      PromelaNode sourceNode;
      PromelaEdge newEdge;

      if(!nodesOfSecond.contains(newEndNode)){
        nodesOfResult.add(newEndNode);
        //System.out.println("only once");
      }


      if(nodesOfSecond.size()==1){
        final PromelaNode node = nodesOfSecond.get(0);
        if(node.isBreak()){
          indexNode.add(node);
         // newStartNode = new PromelaNode();
          second.setStart(newStartNode);
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);

          label.add(ident);
          label2 = new PromelaLabel(label);
          newEdge = new PromelaEdge(newStartNode,newEndNode,label2);
          edgesOfResult.add(newEdge);
          nodesOfResult.add(newStartNode);
          breakNodeSource.put(newStartNode, newEdge);
        }else if(node.isEnd()){

        }
      }else{
      for(final PromelaNode n: nodesOfSecond){
        if (n.isBreak()){
          indexNode.add(n);

          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
              breakNodeSource.put(sourceNode, newEdge);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }else if(n.isEnd()&&n!=newEndNode){
          indexNode.add(n);
          final PromelaNode startNode = second.getStart();
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, startNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
      }
      for(final PromelaEdge e: edgesOfSecond){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfSecond){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }
      //second.setEnd(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,second.getStart());
      return output;
    }
    else if(second==null){
      return first;
    }
    else{
      //if neither first or second are null
      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      List<PromelaEdge> indexEdge = new ArrayList<PromelaEdge>();
      List<PromelaNode> indexNode = new ArrayList<PromelaNode>();
      PromelaGraph output = null;


      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();
      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();
      PromelaLabel label2;
      PromelaNode targetNode;
      PromelaNode sourceNode;
      PromelaEdge newEdge;

      if(!nodesOfFirst.contains(newEndNode)){
        nodesOfResult.add(newEndNode);
        //System.out.println("only once");
      }


      if(nodesOfFirst.size()==1){
        final PromelaNode node = nodesOfFirst.get(0);
        if(node.isBreak()){
          indexNode.add(node);

          first.setStart(newStartNode);
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);

          label.add(ident);
          label2 = new PromelaLabel(label);
          newEdge = new PromelaEdge(newStartNode,newEndNode,label2);
          edgesOfResult.add(newEdge);
          nodesOfResult.add(newStartNode);

          // one source node from break node
          breakNodeSource.put(newStartNode,newEdge);
        }else if(node.isEnd()){
          //TO DO
        }
      }else{
      for(final PromelaNode n: nodesOfFirst){
        if (n.isBreak()){
          indexNode.add(n);

          for(final PromelaEdge e: edgesOfFirst){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
              breakNodeSource.put(sourceNode,newEdge);
            }
          }
        }else if(n.isEnd()&&n!=newEndNode){
          indexNode.add(n);
          final PromelaNode startNode = first.getStart();
          for(final PromelaEdge e: edgesOfFirst){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, startNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
      }
      for(final PromelaEdge e: edgesOfFirst){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfFirst){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }

      indexEdge = new ArrayList<PromelaEdge>();
      indexNode = new ArrayList<PromelaNode>();

      if(nodesOfSecond.size()==1){
        final PromelaNode n = nodesOfSecond.get(0);
        indexNode.add(n);
        if(n.isBreak()){
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);

          label.add(ident);
          label2 = new PromelaLabel(label);
          if(!breakNodeSource.containsKey(first.getStart())){
          newEdge = new PromelaEdge(first.getStart(),newEndNode,label2);
          }else{
           for(final SimpleExpressionProxy l: breakNodeSource.get(first.getStart()).getLabelBlock().getLabel()){
             label.add(l);
           }
           edgesOfResult.remove(breakNodeSource.get(first.getStart()));
           newEdge =breakNodeSource.get(first.getStart());
           for(final SimpleExpressionProxy l: label){
             newEdge.getLabelBlock().getLabel().add(l);
           }
          }
          edgesOfResult.add(newEdge);

        }else if(n.isEnd()){
          //TO DO
        }
      }
      else{

      for(final PromelaNode n: nodesOfSecond){
        if(n==second.getStart()){
          indexNode.add(n);
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getSource()==n && !e.getTarget().isEnd()&&!e.getTarget().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              newEdge = new PromelaEdge(first.getStart(), targetNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
            //take me forever to debug this line @_@
            else if(e.getSource()==n && e.getTarget().isEnd()){// && !second.getEnd().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              indexNode.add(targetNode);
              final List<PromelaEdge> tempEdges = new ArrayList<PromelaEdge>(edgesOfResult);
              for(final PromelaEdge edge: tempEdges){
                if(edge.getTarget()==edge.getSource()){
                  label2.getLabel().addAll(edge.getLabelBlock().getLabel());
                  final List<SimpleExpressionProxy> tempList = new ArrayList<SimpleExpressionProxy>(label2.getLabel());

                  final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
                  final Comparator<SimpleExpressionProxy> comparator =
                    new ExpressionComparator(optable);
                  Collections.sort(tempList,comparator);
                  final Collection<SimpleExpressionProxy> orderedEvent = new ArrayList<SimpleExpressionProxy>(tempList);
                  label2 = new PromelaLabel(orderedEvent);
                  edgesOfResult.remove(edge);
                }
              }
              final PromelaNode startNode = first.getStart();
              final PromelaNode endnode = first.getStart();
              newEdge = new PromelaEdge(startNode, endnode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
        else if (n.isBreak()){

          indexNode.add(n);
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              if(e.getSource()!=second.getStart()){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              //first.getEnd()
              if(!breakNodeSource.containsKey(sourceNode)){
                newEdge = new PromelaEdge(sourceNode,newEndNode,label2);
                }else{
                 edgesOfResult.remove(breakNodeSource.get(sourceNode));
                 newEdge =breakNodeSource.get(sourceNode);
                 for(final SimpleExpressionProxy l: label2.getLabel()){
                   newEdge.getLabelBlock().getLabel().add(l);
                 }
                }
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
            else if(e.getSource()==second.getStart()){
              label2= e.getLabelBlock();

              if(!breakNodeSource.containsKey(first.getStart())){
                newEdge = new PromelaEdge(first.getStart(),newEndNode,label2);
                }else{
                  edgesOfResult.remove(breakNodeSource.get(first.getStart()));
                 newEdge =breakNodeSource.get(first.getStart());
                 for(final SimpleExpressionProxy l: label2.getLabel()){
                   newEdge.getLabelBlock().getLabel().add(l);
                 }
                }
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
            }
          }
        }else if(n.isEnd()&&n!=newEndNode){
          indexNode.add(n);
          final PromelaNode startNode = first.getStart();
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n && e.getSource()!=second.getStart()){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, startNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
      }
      for(final PromelaEdge e: edgesOfSecond){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfSecond){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }
      nodesOfResult.remove(newEndNode);
      nodesOfResult.add(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());

      return output;
    }

  }
      //Separate unwind

    else{
    //TODO
    final PromelaNode newStartNode = start;
    final PromelaNode secondStartNode = new PromelaNode();
    final PromelaNode newEndNode = endNode;
    final HashMap<PromelaNode,PromelaEdge> breakNodeSource = (HashMap<PromelaNode,PromelaEdge>) toBreakNode;

     // I'm ignoring this part for now

    if(first==null){

      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      final List<PromelaEdge> indexEdge = new ArrayList<PromelaEdge>();
      final List<PromelaNode> indexNode = new ArrayList<PromelaNode>();
      PromelaGraph output = null;

      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();
      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();
      PromelaLabel label2;

      PromelaNode sourceNode;
      PromelaEdge newEdge;

      if(!nodesOfSecond.contains(newEndNode)){
        nodesOfResult.add(newEndNode);
        //System.out.println("only once");
      }


      if(nodesOfSecond.size()==1){
        final PromelaNode node = nodesOfSecond.get(0);
        if(node.isBreak()){
          indexNode.add(node);
         // newStartNode = new PromelaNode();
          second.setStart(newStartNode);
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);

          label.add(ident);
          label2 = new PromelaLabel(label);
          newEdge = new PromelaEdge(newStartNode,newEndNode,label2);
          edgesOfResult.add(newEdge);
          nodesOfResult.add(newStartNode);
          breakNodeSource.put(newStartNode, newEdge);
        }else if(node.isEnd()){

        }
      }else{
      for(final PromelaNode n: nodesOfSecond){
        if (n.isBreak()){
          indexNode.add(n);

          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
              breakNodeSource.put(sourceNode, newEdge);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }else if(n.isEnd()&&n!=newEndNode){
          indexNode.add(n);
          final PromelaNode startNode = second.getStart();
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, startNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
      }
      for(final PromelaEdge e: edgesOfSecond){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfSecond){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }
      //second.setEnd(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,second.getStart());
      return output;
    }
    else if(second==null){
      return first;
    }
    else{

       // if neither first or second are null

      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
      List<PromelaEdge> indexEdge = new ArrayList<PromelaEdge>();
      List<PromelaNode> indexNode = new ArrayList<PromelaNode>();
      PromelaGraph output = null;


      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();
      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();
      PromelaLabel label2;
      PromelaNode targetNode;
      PromelaNode sourceNode;
      PromelaEdge newEdge;
      PromelaLabel secondLabel;
      PromelaEdge secondEdge;
      if(!nodesOfFirst.contains(newEndNode)){
        nodesOfResult.add(newEndNode);
        //System.out.println("only once");
      }
      if(!nodesOfFirst.contains(secondStartNode)){
        nodesOfResult.add(secondStartNode);
        //System.out.println("only once");
      }
      if(!nodesOfFirst.contains(secondStartNode)){
        nodesOfResult.add(newStartNode);
        //System.out.println("only once");
      }
      if(nodesOfFirst.size()==1){
        final PromelaNode node = nodesOfFirst.get(0);
        if(node.isBreak()){
          indexNode.add(node);

          first.setStart(newStartNode);
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);

          label.add(ident);
          label2 = new PromelaLabel(label);
          newEdge = new PromelaEdge(newStartNode,newEndNode,label2);
          edgesOfResult.add(newEdge);
          nodesOfResult.add(newStartNode);
          final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
          secondlabel.add(ident);
          secondLabel = new PromelaLabel(secondlabel);
          secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
          edgesOfResult.add(secondEdge);

          // one source node from break node
          breakNodeSource.put(newStartNode,newEdge);
        }else if(node.isEnd()){
          //TO DO

        }
      }else{
      for(final PromelaNode n: nodesOfFirst){
        if(n==first.getStart()){
          indexNode.add(n);
          for(final PromelaEdge e: edgesOfFirst){
            if(e.getSource()==n && !e.getTarget().isEnd()&&!e.getTarget().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              newEdge = new PromelaEdge(newStartNode, targetNode, label2);
              final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
              secondlabel.addAll(label2.getLabel());
              secondLabel = new PromelaLabel(secondlabel);
              secondEdge = new PromelaEdge(secondStartNode,targetNode,secondLabel);
              edgesOfResult.add(secondEdge);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
            //take me forever to debug this line @_@
            else if(e.getSource()==n && e.getTarget().isEnd()){// && !second.getEnd().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              indexNode.add(targetNode);
              final List<PromelaEdge> tempEdges = new ArrayList<PromelaEdge>(edgesOfResult);
              for(final PromelaEdge edge: tempEdges){
                if(edge.getTarget()==edge.getSource()){
                  label2.getLabel().addAll(edge.getLabelBlock().getLabel());
                  final List<SimpleExpressionProxy> tempList = new ArrayList<SimpleExpressionProxy>(label2.getLabel());

                  final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
                  final Comparator<SimpleExpressionProxy> comparator =
                    new ExpressionComparator(optable);
                  Collections.sort(tempList,comparator);
                  final Collection<SimpleExpressionProxy> orderedEvent = new ArrayList<SimpleExpressionProxy>(tempList);
                  label2 = new PromelaLabel(orderedEvent);
                  edgesOfResult.remove(edge);
                }
              }

              newEdge = new PromelaEdge(newStartNode, secondStartNode, label2);

              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }else if (n.isBreak()){
          indexNode.add(n);

          for(final PromelaEdge e: edgesOfFirst){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();

              newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
              if(sourceNode==first.getStart()){
                newEdge = new PromelaEdge(newStartNode,newEndNode,label2);
                final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
                secondlabel.addAll(label2.getLabel());
                secondLabel = new PromelaLabel(secondlabel);
                secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);  // L - E
                edgesOfResult.add(secondEdge);

              }
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
              breakNodeSource.put(sourceNode,newEdge);

            }
          }
        }else if(n.isEnd()&&n!=newEndNode){
          indexNode.add(n);
          //final PromelaNode startNode = first.getStart();
          for(final PromelaEdge e: edgesOfFirst){
            if(e.getTarget()==n){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              if(sourceNode!=first.getStart()){
              newEdge = new PromelaEdge(sourceNode, secondStartNode, label2); // S -> L
              edgesOfResult.add(newEdge);

              indexEdge.add(e);
              }
             }
          }
        }
      }
      for(final PromelaEdge e: edgesOfFirst){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfFirst){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }

      indexEdge = new ArrayList<PromelaEdge>();
      indexNode = new ArrayList<PromelaNode>();

      if(nodesOfSecond.size()==1){
        final PromelaNode n = nodesOfSecond.get(0);
        indexNode.add(n);
        if(n.isBreak()){
          final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy ident = factory.createSimpleIdentifierProxy("step_"+name);
          Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
          secondlabel.add(ident);
          label.add(ident);
          label2 = new PromelaLabel(label);
          if(!breakNodeSource.containsKey(first.getStart())){
            newEdge = new PromelaEdge(first.getStart(),newEndNode,label2);
            secondLabel = new PromelaLabel(secondlabel);
            secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
            edgesOfResult.add(newEdge);
          }else{
           for(final SimpleExpressionProxy l: breakNodeSource.get(first.getStart()).getLabelBlock().getLabel()){
             label.add(l);
           }
           edgesOfResult.remove(breakNodeSource.get(first.getStart()));
           newEdge =breakNodeSource.get(first.getStart());
           for(final SimpleExpressionProxy l: label){
             newEdge.getLabelBlock().getLabel().add(l);
           }
           secondlabel = new ArrayList<SimpleExpressionProxy>();
           secondlabel.addAll(newEdge.getLabelBlock().getLabel());
           secondLabel = new PromelaLabel(secondlabel);
           secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
           edgesOfResult.add(secondEdge);
          }
          edgesOfResult.add(newEdge);

        }else if(n.isEnd()){
          //TO DO
        }
      }
      else{

      for(final PromelaNode n: nodesOfSecond){
        if(n==second.getStart()){
          indexNode.add(n);
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getSource()==n && !e.getTarget().isEnd()&&!e.getTarget().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              newEdge = new PromelaEdge(first.getStart(), targetNode, label2);
              final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
              secondlabel.addAll(label2.getLabel());
              secondLabel = new PromelaLabel(secondlabel);
              secondEdge = new PromelaEdge(secondStartNode,targetNode,secondLabel);
              edgesOfResult.add(secondEdge);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
            //take me forever to debug this line @_@
            else if(e.getSource()==n && e.getTarget().isEnd()){// && !second.getEnd().isBreak()){
              label2 = e.getLabelBlock();
              targetNode = e.getTarget();
              indexNode.add(targetNode);
              final List<PromelaEdge> tempEdges = new ArrayList<PromelaEdge>(edgesOfResult);
              for(final PromelaEdge edge: tempEdges){
                if(edge.getTarget()==edge.getSource()){
                  label2.getLabel().addAll(edge.getLabelBlock().getLabel());
                  final List<SimpleExpressionProxy> tempList = new ArrayList<SimpleExpressionProxy>(label2.getLabel());

                  final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
                  final Comparator<SimpleExpressionProxy> comparator =
                    new ExpressionComparator(optable);
                  Collections.sort(tempList,comparator);
                  final Collection<SimpleExpressionProxy> orderedEvent = new ArrayList<SimpleExpressionProxy>(tempList);
                  label2 = new PromelaLabel(orderedEvent);
                  edgesOfResult.remove(edge);
                }
              }
              final PromelaNode startNode = first.getStart();
              final PromelaNode endnode = first.getStart();
              newEdge = new PromelaEdge(startNode, secondStartNode, label2);
              final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
              secondlabel.addAll(label2.getLabel());
              secondLabel = new PromelaLabel(secondlabel);
              secondEdge = new PromelaEdge(secondStartNode,endnode,secondLabel);
              edgesOfResult.add(secondEdge);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
        else if (n.isBreak()){

          indexNode.add(n);
          for(final PromelaEdge e: edgesOfSecond){
            if(e.getTarget()==n){
              if(e.getSource()!=second.getStart()){
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              //first.getEnd()
              if(!breakNodeSource.containsKey(sourceNode)){
                newEdge = new PromelaEdge(sourceNode,newEndNode,label2);
                final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
                secondlabel.addAll(label2.getLabel());
                secondLabel = new PromelaLabel(secondlabel);
                secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
                edgesOfResult.add(secondEdge);
                }else{
                 edgesOfResult.remove(breakNodeSource.get(sourceNode));
                 newEdge =breakNodeSource.get(sourceNode);
                 for(final SimpleExpressionProxy l: label2.getLabel()){
                   newEdge.getLabelBlock().getLabel().add(l);
                 }
                }
              edgesOfResult.add(newEdge);
              final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
              secondlabel.addAll(newEdge.getLabelBlock().getLabel());
              secondLabel = new PromelaLabel(secondlabel);
              secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
              edgesOfResult.add(secondEdge);
              indexEdge.add(e);
            }
            else if(e.getSource()==second.getStart()){
              label2= e.getLabelBlock();

              if(!breakNodeSource.containsKey(first.getStart())){
                newEdge = new PromelaEdge(first.getStart(),newEndNode,label2);
                final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
                secondlabel.addAll(label2.getLabel());
                secondLabel = new PromelaLabel(secondlabel);
                secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
                edgesOfResult.add(secondEdge);
                }else{
                  edgesOfResult.remove(breakNodeSource.get(first.getStart()));
                 newEdge =breakNodeSource.get(first.getStart());
                 for(final SimpleExpressionProxy l: label2.getLabel()){
                   newEdge.getLabelBlock().getLabel().add(l);
                 }
                }
              edgesOfResult.add(newEdge);
              final Collection<SimpleExpressionProxy> secondlabel = new ArrayList<SimpleExpressionProxy>();
              secondlabel.addAll(newEdge.getLabelBlock().getLabel());
              secondLabel = new PromelaLabel(secondlabel);
              secondEdge = new PromelaEdge(secondStartNode,newEndNode,secondLabel);
              edgesOfResult.add(secondEdge);
              indexEdge.add(e);
            }
            }
          }
        } else if (n.isEnd() && n != newEndNode) {
          indexNode.add(n);
          for (final PromelaEdge e: edgesOfSecond) {
            if (e.getTarget() == n && e.getSource() != second.getStart()) {
              label2 = e.getLabelBlock();
              sourceNode = e.getSource();
              newEdge = new PromelaEdge(sourceNode, secondStartNode, label2);
              edgesOfResult.add(newEdge);
              indexEdge.add(e);
            }
          }
        }
      }
      for(final PromelaEdge e: edgesOfSecond){
        if(!indexEdge.contains(e)){
          edgesOfResult.add(e);
        }
      }
      for(final PromelaNode n: nodesOfSecond){
        if(!indexNode.contains(n)){
          nodesOfResult.add(n);
        }
      }
      }
      nodesOfResult.remove(newEndNode);
      nodesOfResult.add(newEndNode);
      output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart());

      return output;
    }

  }
  }
  */
  public GraphProxy createGraphProxy(final ModuleProxyFactory mFactory, final String name)
  {
    int index = 0;
    SimpleNodeProxy proxy;
    for (final PromelaNode node : getNodes()) {
      final boolean initial = (node == this.getStart());
     // System.out.println(name+" "+getNodes().contains(this.getStart()));
      final boolean marked = (node.isEnd());
      proxy = node.createNode(name, index++, initial, marked, mFactory);
      mNodes.add(proxy);
    }
    for(final PromelaEdge e : this.getEdges()){
      final Collection<Proxy> label = new ArrayList<Proxy>();
      label.addAll(e.getLabelBlock().getLabel());
      final LabelBlockProxy labelBlock =
        mFactory.createLabelBlockProxy(label, null);

      final NodeProxy source = e.getSource().getNode();
      final NodeProxy target = e.getTarget().getNode();

      final EdgeProxy edge = mFactory.createEdgeProxy(source, target, labelBlock, null, null, null, null);
      mEdges.add(edge);
    }
    final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
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
