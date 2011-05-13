package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;

public class PromelaGraph
{
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
    }else if(first ==null && second == null){
      return null;
    }else{
      final ArrayList<PromelaEdge> edgesOfFirst = (ArrayList<PromelaEdge>) first.getEdges();
      final ArrayList<PromelaNode> nodeOfFirst = (ArrayList<PromelaNode>) first.getNodes();


      final ArrayList<PromelaEdge> edgesOfSecond = (ArrayList<PromelaEdge>) second.getEdges();
      final ArrayList<PromelaNode> nodeOfSecond = (ArrayList<PromelaNode>) second.getNodes();
      final PromelaNode newNode = new PromelaNode();

      //replace last node from first PromelaGraph with new node
      for(int i =0;i<edgesOfFirst.size();i++){
        if(edgesOfFirst.get(i).getTarget()==(first.getEnd())){
          final PromelaLabel label1 = edgesOfFirst.get(i).getLabelBlock();
          final PromelaNode sourceNode = edgesOfFirst.get(i).getSource();
          final PromelaEdge newEdge = new PromelaEdge(sourceNode,newNode,label1);
          edgesOfFirst.set(i, newEdge);
        }
      }
      for(int i =0;i<edgesOfSecond.size();i++){
        if(edgesOfSecond.get(i).getSource()==(second.getStart())){
          final PromelaLabel label1 = edgesOfSecond.get(i).getLabelBlock();
          final PromelaNode targetNode = edgesOfSecond.get(i).getSource();
          final PromelaEdge newEdge = new PromelaEdge(newNode,targetNode,label1);
          edgesOfSecond.set(i, newEdge);
        }
      }
      //add edges of second graph to first graph
      for(final PromelaEdge e: edgesOfSecond){
        edgesOfFirst.add(e);
      }

      //remove the original start node of second graph and end node of first graph
      //They are replaced by new node
      nodeOfSecond.remove(second.getStart());
      nodeOfFirst.remove(first.getEnd());

      //add this new node to node list of first graph
      nodeOfFirst.add(newNode);

      //then, add nodes of second graph to first graph
      for(final PromelaNode n: nodeOfSecond){
        nodeOfFirst.add(n);
      }

      //redefine first graph
      first.setEdges(edgesOfFirst);
      first.setNodes(nodeOfFirst);
      first.setEnd(second.getEnd());
    }
    return first;
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
    final ArrayList<NodeProxy> nodeStore = new ArrayList<NodeProxy>();

    for(int i =0;i<this.getNodes().size();i++){
      final NodeProxy node = mFactory.createSimpleNodeProxy(name+"_"+i);
      nodeStore.add(node);
      mNodes.add(node);
    }
    for(final PromelaEdge e : this.getEdges()){
      final Collection<Proxy> label = e.getLabelBlock().getLabel();
      final LabelBlockProxy labelBlock = mFactory.createLabelBlockProxy(label, null);
      final NodeProxy source = nodeStore.get(this.getNodes().indexOf(e.getSource()));
      final NodeProxy target = nodeStore.get(this.getNodes().indexOf(e.getTarget()));

      final EdgeProxy edge = mFactory.createEdgeProxy(source, target, labelBlock, null, null, null, null);
      mEdges.add(edge);
    }
    final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
    return graph;
  }

  public ArrayList<PromelaNode> getNodes(){
    return pNodes;
  }
  public ArrayList<PromelaEdge> getEdges(){
    return pEdges;
  }
  public void setStart(final PromelaNode newStart){
    pStart =newStart ;
  }
  public void setEnd(final PromelaNode promelaNode){
    pEnd = promelaNode ;
  }
  public void setNodes(final ArrayList<PromelaNode> nodeOfFirst){
    pNodes.clear();
    for(final PromelaNode n: nodeOfFirst){
      pNodes.add(n);
    }
  }
  public void setEdges(final ArrayList<PromelaEdge> edgesOfFirst){
    pEdges.clear();
    for(final PromelaEdge n: edgesOfFirst){
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
  private final ArrayList<PromelaNode> pNodes = new ArrayList<PromelaNode>();
  private final ArrayList<PromelaEdge> pEdges = new ArrayList<PromelaEdge>();
}
