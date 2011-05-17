package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class PromelaGraph
{
  public PromelaGraph(final List<PromelaNode> nodes, final List<PromelaEdge> edges,final PromelaNode start, final PromelaNode end){
    pNodes = nodes;
    pEdges = edges;
    /*.clear();
    pEdges.clear();
    for(int i=0;i<nodes.size();i++){
      pNodes.add(nodes.get(i));
    }
    for(int i=0;i<edges.size();i++){
      pEdges.add(edges.get(i));
    }
    */
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

    /*
    mStart = mFactory.createSimpleNodeProxy("s"+r.nextInt());
    mEnd = mFactory.createSimpleNodeProxy("s"+r.nextInt());

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    labelBlock.add(ident);
    final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);

    final EdgeProxy edge = mFactory.createEdgeProxy(mStart, mEnd, label, null, null, null, null);
    mEdges.add(edge);
    mNodes.add(mStart);
    mNodes.add(mEnd);
  */
  }

  public PromelaGraph(final Collection<IdentifierProxy> events, final ModuleProxyFactory factory){
    pStart = new PromelaNode();
    pEnd = new PromelaNode();

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    for(final Proxy value: events){
      labelBlock.add(value);
    }
    final PromelaLabel label = new PromelaLabel(labelBlock);

    final PromelaEdge edge = new PromelaEdge(pStart, pEnd, label);
    pEdges.add(edge);
    pNodes.add(pStart);
    pNodes.add(pEnd);

    /*
    mFactory = factory;
    mStart = mFactory.createSimpleNodeProxy("l"+r.nextInt());
    mEnd = mFactory.createSimpleNodeProxy("l"+r.nextInt());

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    for(final Proxy value: events){
      labelBlock.add(value);
    }
    final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);

    final EdgeProxy edge = mFactory.createEdgeProxy(mStart, mEnd, label, null, null, null, null);
    mEdges.add(edge);
    mNodes.add(mStart);
    mNodes.add(mEnd);
    */
  }

  public static PromelaGraph sequentialComposition (final PromelaGraph first, final PromelaGraph second){
    if(first==null){
      return second;
    }else if(second ==null){
      return first;
    }else{
      //final List<PromelaNode> newNodes = new ArrayList<PromelaNode>(first.getNodes());
   //   final ArrayList<PromelaEdge> edgesOfFirst = new ArrayList<PromelaEdge>(first.getEdges());
      final List<PromelaNode> nodesOfFirst = first.getNodes();
      final List<PromelaEdge> edgesOfFirst = first.getEdges();

 //     final ArrayList<PromelaEdge> edgesOfSecond =  new ArrayList<PromelaEdge>(second.getEdges());
      final List<PromelaEdge> edgesOfSecond = second.getEdges();
    //  final ArrayList<PromelaNode> nodeOfSecond = new ArrayList<PromelaNode>(second.getNodes());
      final List<PromelaNode> nodesOfSecond = second.getNodes();
      final PromelaNode newNode = new PromelaNode();
/*
      //replace last node from first PromelaGraph with new node
      for(int i =0;i<edgesOfFirst.size();i++){
        if(edgesOfFirst.get(i).getTarget()==(first.getEnd())){
          //System.out.println(edgesOfFirst.get(i).getLabelBlock().getLabel()+" "+i);
          final PromelaLabel label1 = edgesOfFirst.get(i).getLabelBlock();
          final PromelaNode sourceNode = edgesOfFirst.get(i).getSource();
          final PromelaEdge newEdge = new PromelaEdge(sourceNode,newNode,label1);
          edgesOfFirst.set(i, newEdge);
        }
      }
      for(int i =0;i<edgesOfSecond.size();i++){
        if(edgesOfSecond.get(i).getSource()==(second.getStart())){
         // System.out.println(edgesOfSecond.get(i).getLabelBlock().getLabel()+" "+i);
          final PromelaLabel label1 = edgesOfSecond.get(i).getLabelBlock();
          final PromelaNode targetNode = edgesOfSecond.get(i).getTarget();
          final PromelaEdge newEdge = new PromelaEdge(newNode,targetNode,label1);
          edgesOfSecond.set(i, newEdge);
        }
      }
      //add edges of second graph to first graph
      edgesOfFirst.addAll(edgesOfSecond);
*/
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
      //remove the original start node of second graph and end node of first graph
      //They are replaced by new node
    //  nodeOfSecond.remove(second.getStart());
    //  nodeOfFirst.remove(first.getEnd());

      //add this new node to node list of first graph
   //   nodeOfFirst.add(newNode);


      //then, add nodes of second graph to first graph
 //     for(int i =0;i< nodeOfSecond.size();i++){
 //       nodeOfFirst.add(nodeOfSecond.get(i));
 //     }
      final PromelaGraph output = new PromelaGraph(nodesOfResult,edgesOfResult,first.getStart(),second.getEnd());
     // System.out.println(output.getNodes().size()+" "+output.getEdges().size());
      return output;

      //redefine first graph
     // System.out.println(first.getNodes().size());
     // first.setEdges(edgesOfFirst);
     // first.setNodes(nodeOfFirst);
     // first.setEnd(second.getEnd());
     // System.out.println(first.getNodes().size());
    }

      /*
    final ArrayList<EdgeProxy> edgesOfFirst = (ArrayList<EdgeProxy>) first.getEdges();
    final ArrayList<NodeProxy> nodeOfFirst = (ArrayList<NodeProxy>) first.getNodes();


    final ArrayList<EdgeProxy> edgesOfSecond = (ArrayList<EdgeProxy>) second.getEdges();
    final ArrayList<NodeProxy> nodeOfSecond = (ArrayList<NodeProxy>) second.getNodes();



    final NodeProxy newNode = mFactory.createSimpleNodeProxy("new"+r.nextInt());
    //((ArrayList<NodeProxy>) first.getNodes()).set(first.getNodes().size()-1, newNode);
    //((ArrayList<NodeProxy>) second.getNodes()).set(0,newNode);

    //replace last node from first PromelaGraph with new node
    for(int i =0;i<edgesOfFirst.size();i++){
      if(edgesOfFirst.get(i).getTarget().refequals(first.getEnd())){
        final LabelBlockProxy label1 = edgesOfFirst.get(i).getLabelBlock();
        final NodeProxy sourceNode = edgesOfFirst.get(i).getSource();
        final EdgeProxy newEdge = mFactory.createEdgeProxy(sourceNode,newNode,label1,null,null,null,null);
        edgesOfFirst.set(i, newEdge);
      }
    }

    //replace first node from second PromelaGraph with new node
    for(int i =0;i<edgesOfSecond.size();i++){
      if(edgesOfSecond.get(i).getSource().refequals(second.getStart())){
        final LabelBlockProxy label2 = edgesOfSecond.get(i).getLabelBlock();
        final NodeProxy targetNode = edgesOfSecond.get(i).getTarget();
        final EdgeProxy newEdge = mFactory.createEdgeProxy(newNode,targetNode,label2,null,null,null,null);
        edgesOfSecond.set(i, newEdge);
      }

    }

    //add edges of second graph to first graph
    for(final EdgeProxy e: edgesOfSecond){
      edgesOfFirst.add(e);
    }

    //remove the original start node of second graph and end node of first graph
    //They are replaced by new node
    nodeOfSecond.remove(second.getStart());
    nodeOfFirst.remove(first.getEnd());

    //add this new node to node list of first graph
    nodeOfFirst.add(newNode);

    //then, add nodes of second graph to first graph
    for(final NodeProxy n: nodeOfSecond){
      nodeOfFirst.add(n);
    }

    //redefine first graph
    first.setEdges(edgesOfFirst);
    first.setNodes(nodeOfFirst);
    first.setEnd(second.getEnd());

    return first;
    */

  }

  public GraphProxy createGraphProxy(final String name){
    //final GraphProxy graph = mFactory.createGraphProxy(true, null, this.getNodes(), this.getEdges());
    //boolean initial,marked;

    int index = 0;
    for (final PromelaNode node : getNodes()) {
      final boolean initial = (node == this.getStart());
      final boolean marked = (node == this.getEnd());
      final SimpleNodeProxy proxy = node.createNode(name, index++, initial, marked, mFactory);
      mNodes.add(proxy);
    }
    /*for(int i =0;i<this.getNodes().size();i++){
      //final NodeProxy node = mFactory.createSimpleNodeProxy(name+"_"+i);
      if(this.getNodes().get(i)==this.getStart()) initial = true;
      else initial = false;
      if(this.getNodes().get(i)==this.getEnd()) marked = true;
      else marked = false;
      final SimpleNodeProxy node = this.getNodes().get(i).createNode(name,i,initial,marked,mFactory);
      // nodeStore.add(node);
      mNodes.add(node);
    }
    System.out.println(this.getNodes().size()+" "+nodeStore.size()+"\n");
*/
    for(final PromelaEdge e : this.getEdges()){
      final Collection<Proxy> label = e.getLabelBlock().getLabel();
      final LabelBlockProxy labelBlock = mFactory.createLabelBlockProxy(label, null);
      final NodeProxy source = e.getSource().getNode();
    //  System.out.println(this.getNodes().indexOf(e.getSource())+"=>"+this.getNodes().indexOf(e.getTarget()));

      final NodeProxy target = e.getTarget().getNode();
      System.out.println(source.toString()+"->"+target.toString());

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
  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();

  private PromelaNode pStart;
  private PromelaNode pEnd;
  private List<PromelaNode> pNodes = new ArrayList<PromelaNode>();
  private List<PromelaEdge> pEdges = new ArrayList<PromelaEdge>();

  final ArrayList<NodeProxy> nodeStore = new ArrayList<NodeProxy>();
}
