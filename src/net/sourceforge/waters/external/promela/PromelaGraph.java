package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

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
  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges,final PromelaNode start, final PromelaNode end){
    mPromelaNodes = nodes;
    mPromelaEdges = edges;

    mPromelaSartNode = start;
    mPromelaEndNode = end;
  }

  public PromelaGraph(final IdentifierProxy ident)
  {
    mPromelaSartNode = new PromelaNode();
    mPromelaEndNode = new PromelaNode();

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    labelBlock.add(ident);
    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(mPromelaSartNode,mPromelaEndNode,label);
    mPromelaEdges.add(edge);
    mPromelaNodes.add(mPromelaSartNode);
    mPromelaNodes.add(mPromelaEndNode);

  }

  public PromelaGraph(final THashSet<IdentifierProxy> events, final ModuleProxyFactory factory){
    mPromelaSartNode = new PromelaNode();
    mPromelaEndNode = new PromelaNode();
    System.out.println(events.size());
    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(events);
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
    Collections.sort(tempLabel, comparator);
    final Collection<Proxy> labelBlock = new ArrayList<Proxy>(tempLabel);

    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(mPromelaSartNode, mPromelaEndNode, label);
    mPromelaEdges.add(edge);
    mPromelaNodes.add(mPromelaSartNode);
    mPromelaNodes.add(mPromelaEndNode);

  }
  /*
   * connect end node of first graph to start node of second graph
   */
  public static PromelaGraph sequentialComposition (final PromelaGraph first, final PromelaGraph second){
    if(first==null){
      return second;
    }else if(second ==null){
      return first;
    }else{

      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();

      final List<PromelaEdge> edgesOfSecond = second.getEdges();

      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final PromelaNode newNode = new PromelaNode();

      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>(edgesOfFirst.size() + edgesOfSecond.size());
      for (final PromelaEdge edge : edgesOfFirst) {
        if (edge.getTarget() == first.getEnd()) {
          final PromelaLabel label1 = edge.getLabelBlock();
          final PromelaNode sourceNode = edge.getSource();
          final PromelaEdge newEdge = new PromelaEdge(sourceNode, newNode, label1);
          edgesOfResult.add(newEdge);
        } else {
          edgesOfResult.add(edge);
        }
      }
      for (final PromelaEdge edge : edgesOfSecond) {
        if (edge.getSource() == second.getStart()) {
          final PromelaLabel label1 = edge.getLabelBlock();
          final PromelaNode targetNode = edge.getTarget();
          final PromelaEdge newEdge = new PromelaEdge(newNode, targetNode, label1);
          edgesOfResult.add(newEdge);
        } else {
          edgesOfResult.add(edge);
        }
      }

      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>(nodesOfFirst.size()+nodesOfSecond.size()-1);
      for (final PromelaNode node : nodesOfFirst) {
        if(node!=first.getEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newNode);
      for (final PromelaNode node : nodesOfSecond) {
        if(node!=second.getStart()){
          nodesOfResult.add(node);
        }
      }

      final PromelaGraph output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart(),second.getEnd());

      return output;

    }

  }
  /*
   * used for "if" statement, combine both start and end nodes of first and second graph together
   */
  public static PromelaGraph combineComposition(final PromelaGraph first,
                                                final PromelaGraph second)
  {
    System.out.println(first+" @@"+second);

    if(first==null){
      return second;
    }else if(second ==null){
      return first;
    }else{
      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final List<PromelaEdge> edgesOfSecond = second.getEdges();


      final PromelaNode newStartNode = new PromelaNode();
      final PromelaNode newEndNode = new PromelaNode();
      final List<PromelaEdge> edgesOfResult =
        new ArrayList<PromelaEdge>();  // edgesOfFirst.size() + edgesOfSecond.size()

      PromelaLabel label1;
      PromelaLabel label2;
      PromelaNode targetNode,sourceNode;
      PromelaEdge newEdge;
      PromelaEdge specialEdge1=null;
      PromelaEdge specialEdge2 = null;
      final Collection<Proxy> specialLabel = new ArrayList<Proxy>();
      boolean simpleFirst = false;
      boolean simpleSecond = false;


      for (final PromelaEdge edge : edgesOfFirst) {
        if (edge.getSource() == first.getStart()&& edge.getTarget()!=first.getEnd()) {
          label1 = edge.getLabelBlock();
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label1);
          edgesOfResult.add(newEdge);
          simpleFirst = false;
        } else if(edge.getTarget() == first.getEnd()&& edge.getSource()!=first.getStart()){
          label1 = edge.getLabelBlock();
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label1);
          edgesOfResult.add(newEdge);
          simpleFirst = false;
        }else if(edge.getTarget()==first.getEnd() && edge.getSource()==first.getStart()){
          label1 = edge.getLabelBlock();
          specialLabel.addAll(label1.getLabel());
          specialEdge1 = new PromelaEdge(newStartNode,newEndNode,label1);
          simpleFirst = true;
          edgesOfResult.add(specialEdge1);
        }
        else {
          edgesOfResult.add(edge);
          simpleFirst = false;
        }
      }

      for (final PromelaEdge edge : edgesOfSecond) {
        if (edge.getSource() == second.getStart()&& edge.getTarget()!=second.getEnd()) {
          label2 = edge.getLabelBlock();
          targetNode = edge.getTarget();
          newEdge = new PromelaEdge(newStartNode, targetNode, label2);
          edgesOfResult.add(newEdge);
          simpleSecond = false;
        }else if(edge.getTarget() == second.getEnd()&& edge.getSource()!=second.getStart()){
          label2 = edge.getLabelBlock();
          sourceNode = edge.getSource();
          newEdge = new PromelaEdge(sourceNode, newEndNode, label2);
          edgesOfResult.add(newEdge);
          simpleSecond = false;
        }else if(edge.getTarget()==second.getEnd() && edge.getSource()==second.getStart()){
          label2 = edge.getLabelBlock();
          specialLabel.addAll(label2.getLabel());
          specialEdge2 = new PromelaEdge(newStartNode,newEndNode,label2);
          edgesOfResult.add(specialEdge2);
          simpleSecond = true;
        }
        else {
          edgesOfResult.add(edge);
          simpleSecond = false;
        }
      }
      if(simpleFirst&& simpleSecond){
        edgesOfResult.remove(specialEdge1);
        edgesOfResult.remove(specialEdge2);
        final PromelaLabel label = new PromelaLabel(specialLabel);
        final PromelaEdge edge = new PromelaEdge(newStartNode, newEndNode,label);
        edgesOfResult.add(edge);
        simpleFirst = false;
        simpleSecond = false;
      }

      final List<PromelaNode> nodesOfResult =
        new ArrayList<PromelaNode>();  //nodesOfFirst.size()+nodesOfSecond.size()-2
      for (final PromelaNode node : nodesOfFirst) {
        if(node!=first.getStart()&& node!=first.getEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newStartNode);
      for (final PromelaNode node : nodesOfSecond) {
        if(node!=second.getStart()&& node!=second.getEnd()){
          nodesOfResult.add(node);
        }
      }
      nodesOfResult.add(newEndNode);
      final PromelaGraph output = new PromelaGraph(nodesOfResult,edgesOfResult,newStartNode,newEndNode);

      return output;
    }

  }

  public GraphProxy createGraphProxy(final ModuleProxyFactory mFactory, final String name)
  {
    int index = 0;
    SimpleNodeProxy proxy;
    for (final PromelaNode node : getNodes()) {
      final boolean initial = (node == this.getStart());
      final boolean marked = (node == this.getEnd());
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

      //assert this.getNodes().indexOf(e.getTarget())==-1;
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
    mPromelaSartNode =newStart ;
  }
  public void setEnd(final PromelaNode promelaNode){
    mPromelaEndNode = promelaNode ;
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
    return mPromelaSartNode;
  }
  public PromelaNode getEnd(){
    return mPromelaEndNode;
  }


  //#########################################################################
  //# Data Members
  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
  private PromelaNode mPromelaSartNode;
  private PromelaNode mPromelaEndNode;
  private List<PromelaNode> mPromelaNodes = new ArrayList<PromelaNode>();
  private List<PromelaEdge> mPromelaEdges = new ArrayList<PromelaEdge>();

}
