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
  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges,final PromelaNode start, final PromelaNode end){
    pNodes = nodes;
    pEdges = edges;

    pStart = start;
    pEnd = end;
  }
  public PromelaGraph(final IdentifierProxy ident,final ModuleProxyFactory factory){
    mFactory = factory;
    pStart = new PromelaNode();
    pEnd = new PromelaNode();

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    labelBlock.add(ident);
    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(pStart,pEnd,label);
    pEdges.add(edge);
    pNodes.add(pStart);
    pNodes.add(pEnd);

  }

  public PromelaGraph(final Collection<IdentifierProxy> events, final ModuleProxyFactory factory){
    pStart = new PromelaNode();
    pEnd = new PromelaNode();

    final List<SimpleExpressionProxy> tempLabel = new ArrayList<SimpleExpressionProxy>(events);
    Collections.sort(tempLabel,mComparator);
    final Collection<Proxy> labelBlock = new ArrayList<Proxy>(tempLabel);

    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(pStart, pEnd, label);
    pEdges.add(edge);
    pNodes.add(pStart);
    pNodes.add(pEnd);

  }

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

  public GraphProxy createGraphProxy(final String name)
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
      final Collection<Proxy> label = e.getLabelBlock().getLabel();
      final LabelBlockProxy labelBlock =
        mFactory.createLabelBlockProxy(label, null);
      final NodeProxy source = e.getSource().getNode();


      final NodeProxy target = e.getTarget().getNode();
     // System.out.println(source.toString()+"->"+target.toString());

      //assert this.getNodes().indexOf(e.getTarget())==-1;
      final EdgeProxy edge = mFactory.createEdgeProxy(source, target, labelBlock, null, null, null, null);
      mEdges.add(edge);
    }
    final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
    return graph;
  }

  public List<PromelaNode> getNodes(){
    return pNodes;
  }
  public List<PromelaEdge> getEdges(){
    return pEdges;
  }
  public void setStart(final PromelaNode newStart){
    pStart =newStart ;
  }
  public void setEnd(final PromelaNode promelaNode){
    pEnd = promelaNode ;
  }
  public void setNodes(final ArrayList<PromelaNode> nodes){
    pNodes.clear();
    for(final PromelaNode n: nodes){
      pNodes.add(n);
    }
  }
  public void setEdges(final ArrayList<PromelaEdge> edges){
    pEdges.clear();
    for(final PromelaEdge n: edges){
      pEdges.add(n);
    }
  }
  public PromelaNode getStart(){
    return pStart;
  }
  public PromelaNode getEnd(){
    return pEnd;
  }

  private static ModuleProxyFactory mFactory;
  private final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
  private final Comparator<SimpleExpressionProxy> mComparator=new ExpressionComparator(optable);

  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();

  private PromelaNode pStart;
  private PromelaNode pEnd;
  private List<PromelaNode> pNodes = new ArrayList<PromelaNode>();
  private List<PromelaEdge> pEdges = new ArrayList<PromelaEdge>();

  final ArrayList<NodeProxy> nodeStore = new ArrayList<NodeProxy>();
}
