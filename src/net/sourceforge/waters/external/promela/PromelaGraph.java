package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

  public PromelaGraph(final List<IdentifierProxy> events){

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
    final ArrayList<EdgeProxy> edgesOfFirst = (ArrayList<EdgeProxy>) first.getEdges();
    final NodeProxy lastNodeOfFirst = ((ArrayList<NodeProxy>) first.getNodes()).get(first.getNodes().size()-1);

    final ArrayList<EdgeProxy> edgesOfSecond = (ArrayList<EdgeProxy>) second.getEdges();
    final NodeProxy firstNodeOfSecond = ((ArrayList<NodeProxy>) second.getNodes()).get(second.getNodes().size()-1);

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
      if(edgesOfFirst.get(i).getSource().refequals(lastNodeOfFirst)){
        final LabelBlockProxy label = edgesOfSecond.get(i).getLabelBlock();
        final NodeProxy targetNode = edgesOfSecond.get(i).getSource();
        final EdgeProxy newEdge = mFactory.createEdgeProxy(newNode,targetNode,label,null,null,null,null);
        ((ArrayList<EdgeProxy>) second.getEdges()).set(i, newEdge);
      }
    }
    //continue, make new PromelaGraph
    return null;
  }
  public GraphProxy createGraphProxy(){
    return null;
  }

  public Collection<NodeProxy> getNodes(){
    return mNodes;
  }
  public Collection<EdgeProxy> getEdges(){
    return mEdges;
  }

  private final static Random r = new Random();
  private final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
  private final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
  private final SimpleNodeProxy mStart;
  private final SimpleNodeProxy mEnd;
  private final static ModuleProxyFactory mFactory = new ModuleElementFactory();
}
