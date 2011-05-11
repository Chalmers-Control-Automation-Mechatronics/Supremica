package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

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
  public PromelaGraph(final IdentifierProxy ident,final ModuleProxyFactory factory){
    mFactory = factory;
    mStart = mFactory.createSimpleNodeProxy("s"+r.nextInt());
    mEnd = mFactory.createSimpleNodeProxy("s"+r.nextInt());

    final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
    labelBlock.add(ident);
    final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);

    final EdgeProxy edge = mFactory.createEdgeProxy(mStart, mEnd, label, null, null, null, null);
    mEdges.add(edge);
    mNodes.add(mStart);
    mNodes.add(mEnd);
  }

  public PromelaGraph(final Collection<IdentifierProxy> events, final ModuleProxyFactory factory){
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
  }

  public static PromelaGraph sequentialComposition (final PromelaGraph first, final PromelaGraph second){
    if(first==null){
      return second;
    }else if(second ==null){
      return first;
    }else if(first ==null && second == null){
      return null;
    }else{
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
    }
  }
  public GraphProxy createGraphProxy(){
    final GraphProxy graph = mFactory.createGraphProxy(true, null, this.getNodes(), this.getEdges());
    return graph;
  }

  public Collection<NodeProxy> getNodes(){
    return mNodes;
  }
  public Collection<EdgeProxy> getEdges(){
    return mEdges;
  }
  public void setStart(final NodeProxy newStart){
    mStart = (SimpleNodeProxy) newStart ;
  }
  public void setEnd(final NodeProxy newEnd){
    mEnd = (SimpleNodeProxy) newEnd ;
  }
  public void setNodes(final Collection<NodeProxy> nodes){
    mNodes.clear();
    for(final NodeProxy n: nodes){
      mNodes.add(n);
    }
  }
  public void setEdges(final Collection<EdgeProxy> edges){
    mEdges.clear();
    for(final EdgeProxy n: edges){
      mEdges.add(n);
    }
  }
  public NodeProxy getStart(){
    return mStart;
  }
  public NodeProxy getEnd(){
    return mEnd;
  }
  private final static Random r = new Random();
  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
  private SimpleNodeProxy mStart;
  private SimpleNodeProxy mEnd;
  private static ModuleProxyFactory mFactory;
}
