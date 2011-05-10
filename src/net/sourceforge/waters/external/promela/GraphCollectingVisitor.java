package net.sourceforge.waters.external.promela;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import net.sourceforge.waters.external.promela.ast.ChannelStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ChannelTreeNode;
import net.sourceforge.waters.external.promela.ast.ConstantTreeNode;
import net.sourceforge.waters.external.promela.ast.ExchangeTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialTreeNode;
import net.sourceforge.waters.external.promela.ast.ModuleTreeNode;
import net.sourceforge.waters.external.promela.ast.MsgTreeNode;
import net.sourceforge.waters.external.promela.ast.NameTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeTreeNode;
import net.sourceforge.waters.external.promela.ast.PromelaTreeNode;
import net.sourceforge.waters.external.promela.ast.RunTreeNode;
import net.sourceforge.waters.external.promela.ast.SemicolonTreeNode;
import net.sourceforge.waters.external.promela.ast.TypeTreeNode;
import net.sourceforge.waters.external.promela.ast.VardefTreeNode;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

public class GraphCollectingVisitor implements PromelaVisitor
{
  private final ModuleProxyFactory mFactory = new ModuleElementFactory();
  private final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
  @SuppressWarnings("unused")
  private EventCollectingVisitor mVisitor=null;

  ArrayList<String> data =new ArrayList<String>();
  ArrayList<String> labels = new ArrayList<String>();

  ArrayList<List<String>> componentLabels = new ArrayList<List<String>>();

  final Hashtable<String,Collection<Proxy>> procEvent = new Hashtable<String,Collection<Proxy>>();

  private final Hashtable<String,Integer> graphIndex = new Hashtable<String,Integer>();
  ArrayList<Integer> lowerEnd = new ArrayList<Integer>();
  ArrayList<Integer> upperEnd = new ArrayList<Integer>();

  public GraphCollectingVisitor(final EventCollectingVisitor v){
    mVisitor = v;
  }
  public GraphProxy collectGraphs(final PromelaTreeNode node)
  {
    return (GraphProxy) node.acceptVisitor(this);
  }

  public Object visitModule(final ModuleTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ((PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t)
  {

    final Collection<NodeProxy> nodes = new ArrayList<NodeProxy>();
    final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>();

    final String procName = t.getText();

    procEvent.put(procName,new ArrayList<Proxy>());
    graphIndex.put(procName,1);

    //final Point2D point = new Point(10,-10);
    //final PointGeometryProxy geo = mFactory.createPointGeometryProxy(point);
    final NodeProxy node = mFactory.createSimpleNodeProxy(procName+1);
    nodes.add(node);

    final NodeProxy nodeSource = node;
    final EdgeProxy edge = mFactory.createEdgeProxy(nodeSource, null, null, null, null, null, null);
    edges.add(edge);

    final GraphProxy graph = mFactory.createGraphProxy(true, null, nodes, edges);

    for(int i=0;i<t.getChildCount();i++){
      ((PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }

    return graph;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitVar(final VardefTreeNode t)
  {
 // TODO Auto-generated method stub
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ((PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t)
  {
    final String chanName = t.getParent().getChild(0).getText();
    lowerEnd = new ArrayList<Integer>();
    upperEnd = new ArrayList<Integer>();
    for(int i =1;i<t.getChildCount();i++){
      ((PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy(chanName);
    final int size = t.getChildCount()-1;
    final Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>(size);
    for(int i=0;i<size;i++){
      final IntConstantProxy zero = mFactory.createIntConstantProxy(lowerEnd.get(i));
      final IntConstantProxy c255 = mFactory.createIntConstantProxy(upperEnd.get(i));
      final BinaryOperator op = optable.getRangeOperator();
      final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, zero, c255);
      ranges.add(range);
    }
    @SuppressWarnings("unused")
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);

    return null;
  }
  //return graph of proctype statements
  public Object visitExchange(final ExchangeTreeNode t)
  {
    GraphProxy graph = null;
    final Collection<NodeProxy> nodes = new ArrayList<NodeProxy>();
    final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>();

    final String proctypeName =t.getParent().getParent().getParent().getText();

    //Send statement
    if(t.getText().equals("!")|| t.getText().equals("!!")){

      labels = new ArrayList<String>();

      final String chanName = t.getChild(0).getText();
      labels.add(chanName);

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
      }

      final String ename = labels.get(0);
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
      //create indexedIdentifier, and store it for receive statement
      final IndexedIdentifierProxy indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
      final ArrayList<Proxy> temp = (ArrayList<Proxy>) procEvent.get(proctypeName);
      temp.add(indexEvent);
      procEvent.put(proctypeName,temp);

      //create label block for edges
      final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
      labelBlock.add(indexEvent);
      final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);

      //create nodes
      int graphindex = graphIndex.get(proctypeName);
      final String name1 = proctypeName+(graphindex);
      final String name2 = proctypeName+(graphindex++);
      graphIndex.put(proctypeName,graphindex);
      final NodeProxy node1 = mFactory.createSimpleNodeProxy(name1);
      final NodeProxy node2 = mFactory.createSimpleNodeProxy(name2);
      nodes.add(node1);
      nodes.add(node2);

      //create edges
      final NodeProxy nodeSource = node1;
      final NodeProxy nodeTarget = node2;
      final EdgeProxy edge = mFactory.createEdgeProxy(nodeSource, nodeTarget, label, null, null, null, null);
      edges.add(edge);

      //create graph
      graph = mFactory.createGraphProxy(true, null, nodes, edges);

      }

      //receive statement
      if(t.getText().equals("?")|| t.getText().equals("??")){
        //create nodes
        int graphindex = graphIndex.get(proctypeName);
        final String name1 = proctypeName+(graphindex);
        final String name2 = proctypeName+(graphindex++);
        graphIndex.put(proctypeName,graphindex);
        final NodeProxy node1 = mFactory.createSimpleNodeProxy(name1);
        final NodeProxy node2 = mFactory.createSimpleNodeProxy(name2);
        nodes.add(node1);
        nodes.add(node2);

        //create edges

        final Collection<Proxy> labelBlock = procEvent.get(proctypeName);
        final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);
        final NodeProxy nodeSource = node1;
        final NodeProxy nodeTarget = node2;
        final EdgeProxy edge = mFactory.createEdgeProxy(nodeSource, nodeTarget, label, null, null, null, null);
        edges.add(edge);

        //create graph
        graph = mFactory.createGraphProxy(true, null, nodes, edges);
      }

    return graph;
  }

  public Object visitConstant(final ConstantTreeNode t)
  {

    //add all event data
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t)
  {
    @SuppressWarnings("unused")
    final GraphProxy graph = null;
    final Collection<NodeProxy> nodes = new ArrayList<NodeProxy>();
    @SuppressWarnings("unused")
    final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>();

    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
    @SuppressWarnings("unused")
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);

    //create node
    final Point2D point = new Point(10,-10);
    final PointGeometryProxy geo = mFactory.createPointGeometryProxy(point);
    final NodeProxy node = mFactory.createSimpleNodeProxy("init"+0, null, true, null, geo, null);
    nodes.add(node);



    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    if(t.getText().equals("atomic")){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitRun(final RunTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  //return graph of init events
  public Object visitName(final NameTreeNode t)
  {
    // TODO Auto-generated method stub, need more if statement
    GraphProxy graph = null;
    final Collection<NodeProxy> nodes = new ArrayList<NodeProxy>();
    final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>();

    if(t.getParent() instanceof RunTreeNode){
      //create nodes
      final String proctypeName = t.getText();
      final Point2D point = new Point(10,-10);
      final PointGeometryProxy geo = mFactory.createPointGeometryProxy(point);
      final NodeProxy node1 = mFactory.createSimpleNodeProxy(proctypeName+0, null, true, null, geo, null);
      final NodeProxy node2 = mFactory.createSimpleNodeProxy(proctypeName+1);
      nodes.add(node1);
      nodes.add(node2);

      //create edges
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
      final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
      labelBlock.add(ident);
      final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);
      final NodeProxy nodeSource = node1;
      final NodeProxy nodeTarget = node2;
      final EdgeProxy edge = mFactory.createEdgeProxy(nodeSource, nodeTarget, label, null, null, null, null);
      edges.add(edge);

      //create graph
      graph = mFactory.createGraphProxy(true, null, nodes, edges);
      return graph;
    }

    return graph;

  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitType(final TypeTreeNode t)
  {
    if(t.getText().equals("byte")){
      lowerEnd.add(0);
      upperEnd.add(255);
    }
    return null;
  }

}
