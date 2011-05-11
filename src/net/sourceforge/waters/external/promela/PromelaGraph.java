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
import net.sourceforge.waters.plain.module.ModuleElementFactory;

public class PromelaGraph
{
  public PromelaGraph(final IdentifierProxy ident){

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

  public PromelaGraph(final Collection<IdentifierProxy> events){

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
    final NodeProxy lastNodeOfFirst = ((ArrayList<NodeProxy>) first.getNodes()).get(first.getNodes().size()-1);

    final ArrayList<EdgeProxy> edgesOfSecond = (ArrayList<EdgeProxy>) second.getEdges();
    final NodeProxy firstNodeOfSecond = ((ArrayList<NodeProxy>) second.getNodes()).get(0);


    final NodeProxy newNode = mFactory.createSimpleNodeProxy("new"+r.nextInt());
    ((ArrayList<NodeProxy>) first.getNodes()).set(first.getNodes().size()-1, newNode);
    ((ArrayList<NodeProxy>) second.getNodes()).set(0,newNode);

    //replace last node from first PromelaGraph with new node
    for(int i =0;i<edgesOfFirst.size();i++){
      if(edgesOfFirst.get(i).getTarget().refequals(lastNodeOfFirst)){
        final LabelBlockProxy label = edgesOfFirst.get(i).getLabelBlock();
        final NodeProxy sourceNode = edgesOfFirst.get(i).getSource();
        final EdgeProxy newEdge = mFactory.createEdgeProxy(sourceNode,newNode,label,null,null,null,null);
        ((ArrayList<EdgeProxy>) first.getEdges()).set(i, newEdge);
      }
    }
    //replace first node from second PromelaGraph with new node
    for(int i =0;i<edgesOfSecond.size();i++){
      if(edgesOfSecond.get(i).getSource().refequals(firstNodeOfSecond)){
        final LabelBlockProxy label = edgesOfSecond.get(i).getLabelBlock();
        final NodeProxy targetNode = edgesOfSecond.get(i).getSource();
        final EdgeProxy newEdge = mFactory.createEdgeProxy(newNode,targetNode,label,null,null,null,null);
        ((ArrayList<EdgeProxy>) second.getEdges()).set(i, newEdge);
      }

    }
    //continue, make new PromelaGraph
    for(int i=0;i<second.getEdges().size();i++){
    //add edges of second graph to first graph
      first.getEdges().add(((ArrayList<EdgeProxy>) second.getEdges()).get(i));
      if(i>=1){
        //add all nodes but first node of second graph to first graph
        first.getNodes().add(((ArrayList<NodeProxy>) second.getNodes()).get(i));
      }
    }
    //reset first graph end node
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
  private final static ModuleProxyFactory mFactory = new ModuleElementFactory();
}
