package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.external.promela.ast.BreakStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ChannelStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ChannelTreeNode;
import net.sourceforge.waters.external.promela.ast.ConditionTreeNode;
import net.sourceforge.waters.external.promela.ast.ConstantTreeNode;
import net.sourceforge.waters.external.promela.ast.DoConditionTreeNode;
import net.sourceforge.waters.external.promela.ast.GotoTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialTreeNode;
import net.sourceforge.waters.external.promela.ast.LabelTreeNode;
import net.sourceforge.waters.external.promela.ast.ModuleTreeNode;
import net.sourceforge.waters.external.promela.ast.MsgTreeNode;
import net.sourceforge.waters.external.promela.ast.NameTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeTreeNode;
import net.sourceforge.waters.external.promela.ast.PromelaTree;
import net.sourceforge.waters.external.promela.ast.ReceiveTreeNode;
import net.sourceforge.waters.external.promela.ast.RunTreeNode;
import net.sourceforge.waters.external.promela.ast.SemicolonTreeNode;
import net.sourceforge.waters.external.promela.ast.SendTreeNode;
import net.sourceforge.waters.external.promela.ast.SkipTreeNode;
import net.sourceforge.waters.external.promela.ast.TypeTreeNode;
import net.sourceforge.waters.external.promela.ast.VardefTreeNode;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.antlr.runtime.tree.Tree;


public class GraphCollectingVisitor implements PromelaVisitor
{
  private int counter;
  private final ModuleProxyFactory mFactory;
  private EventCollectingVisitor mVisitor=null;
  ArrayList<String> labels = new ArrayList<String>();
  final ArrayList<String> procNames = new ArrayList<String>();
  ArrayList<String> chanNames = new ArrayList<String>();
  Collection<String> duplicatedRun = new ArrayList<String>();
  boolean mUnWinding=false;
  boolean mIsInit = false;
  Hashtable<String,Integer> copyOfOccur = new Hashtable<String,Integer>();
  Collection<Proxy> mComponents = new ArrayList<Proxy>();

  Map<PromelaNode,PromelaEdge> mSourceOfBreakNode = new HashMap<PromelaNode,PromelaEdge>();

  Map<String,PromelaNode> mGotoNode = new HashMap<String,PromelaNode>();

  Map<String,PromelaNode> mLabelEnd = new HashMap<String,PromelaNode>();

  public GraphCollectingVisitor(final EventCollectingVisitor v){
    mVisitor = v;
    mFactory = v.getFactory();
   // copyOfOccur = new Hashtable<String,Integer>(mVisitor.getOccur());
  }
  public PromelaGraph collectGraphs(final PromelaTree node)
  {
    return (PromelaGraph) node.acceptVisitor(this);
  }

  public Collection<Proxy> getComponents(){
    return mComponents;
  }
  public Object visitModule(final ModuleTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  //Now it directly create PromelaGraph object, using events from Event collector; No need to visit further children
  public Object visitProcType(final ProctypeTreeNode t)
  {
    mUnWinding = false;
    final String procName = t.getText();
    final int occurance = mVisitor.getOccur().get(procName);
    //visit child 1
    final PromelaTree statement = (PromelaTree) t.getChild(1);
    PromelaGraph g = collectGraphs(statement);

    final IdentifierProxy ident;
    if(mVisitor.getAtomic()){
      ident = mFactory.createSimpleIdentifierProxy("initrun");
    }else{
      if(occurance==1){
        ident = mFactory.createSimpleIdentifierProxy("run_"+procName);
      }else{
        final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
        final IdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
        indexes.add(id);
        ident = mFactory.createIndexedIdentifierProxy("run_"+procName, indexes);
      }
    }

    final PromelaGraph newGraph = new PromelaGraph(ident);
    g = PromelaGraph.sequentialComposition(newGraph, g,mUnWinding,mFactory);
    final GraphProxy graph = g.createGraphProxy(mFactory, procName);
    SimpleComponentProxy component;
    if(occurance ==1){
      final IdentifierProxy name = mFactory.createSimpleIdentifierProxy("proctype_"+procName);
      component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);
      mComponents.add(component);
    }else{
      final Collection<SimpleIdentifierProxy> procs = new ArrayList<SimpleIdentifierProxy>();
      for(int i=0;i<occurance;i++){
        final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(procName+"_"+i);
        procs.add(id);
      }
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      //for(int y=1;y<labels.size();y++){
        final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
        indexes.add(id);

      final IndexedIdentifierProxy name = mFactory.createIndexedIdentifierProxy("proctype_"+procName,indexes);


      component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);

      //mComponents.add(component);
      final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(procs);
      final Collection<SimpleComponentProxy> c = new ArrayList<SimpleComponentProxy>();
      //mComponents.add(component);
      c.add(component);
      final ForeachComponentProxy f = mFactory.createForeachComponentProxy("procid", en,null,c);
      mComponents.add(f);
     // mComponents.add(f);
    }


    return null;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitVar(final VardefTreeNode t)
  {

    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    final String name = t.getChild(0).getText();
    chanNames.add(name);
    final ChanInfo ch = mVisitor.getChan().get(name);
    if(ch.getChanLength()==1){
      final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
      final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
      final Collection<Collection<SimpleExpressionProxy>> sendData = ch.getSendData();
      final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy id =
          mFactory.createSimpleIdentifierProxy(accepting);
      final List<SimpleIdentifierProxy> list = Collections.singletonList(id);
      final PlainEventListProxy eventList =
          mFactory.createPlainEventListProxy(list);
      final NodeProxy start = mFactory.createSimpleNodeProxy("empty", eventList, true, null, null, null);

      for(final Collection<SimpleExpressionProxy> s: sendData){
        String ename="s";
        for(final SimpleExpressionProxy ss: s){
           ename = ename+"_"+ss;
        }
        final NodeProxy node = mFactory.createSimpleNodeProxy(ename);
        mNodes.add(node);
      }
      final ArrayList<Collection<SimpleExpressionProxy>> storeSend = new ArrayList<Collection<SimpleExpressionProxy>>(sendData);
      final ArrayList<NodeProxy> storeNode = new ArrayList<NodeProxy>(mNodes);
      for(int i=0;i<storeSend.size();i++){
        Collection<IdentifierProxy> labelBlock = new ArrayList<IdentifierProxy>();
        Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
        for(final SimpleExpressionProxy s: storeSend.get(i)){
          final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
          indexes.add(c1);
        }
        IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("send_"+name, indexes);
        labelBlock.add(ident);
        LabelBlockProxy label =
          mFactory.createLabelBlockProxy(labelBlock, null);
        NodeProxy thisend = storeNode.get(i);
        NodeProxy thisstart = start;
        final EdgeProxy edge =mFactory.createEdgeProxy(thisstart, thisend, label, null, null, null, null);
        mEdges.add(edge);

        labelBlock = new ArrayList<IdentifierProxy>();
        indexes = new ArrayList<SimpleExpressionProxy>();
        for(final SimpleExpressionProxy s: storeSend.get(i)){
          final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
          indexes.add(c1);
        }
        ident = mFactory.createIndexedIdentifierProxy("recv_"+name, indexes);
        labelBlock.add(ident);
        label = mFactory.createLabelBlockProxy(labelBlock,null);
        thisend = start;
        thisstart = storeNode.get(i);
        final EdgeProxy edge2 = mFactory.createEdgeProxy(thisstart,thisend,label,null,null,null,null);
        mEdges.add(edge2);
      }
      mNodes.add(start);

      final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("channel_"+name);
      final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
      mComponents.add(component);
    }

    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t)
  {
    mIsInit = false;
    counter = 0;
    final List<PromelaNode> removeNode = new ArrayList<PromelaNode>();
    final List<PromelaEdge> removeEdge = new ArrayList<PromelaEdge>();
    final List<PromelaEdge> addEdge = new ArrayList<PromelaEdge>();
    PromelaGraph result = null;
    final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(0));
    result = PromelaGraph.sequentialComposition(result,step,mUnWinding,mFactory);
    for(final PromelaNode n: result.getNodes()){
      if(n.isGoto()){
        removeNode.add(n);
        final String name = n.getGotoLabel();
        final PromelaNode newNode = mGotoNode.get(name);
        for(final PromelaEdge e: result.getEdges()){
          if(e.getTarget()==n){
            final PromelaLabel label = e.getLabelBlock();
            final PromelaNode sourceNode = e.getSource();
            removeEdge.add(e);
            final PromelaEdge newEdge = new PromelaEdge(sourceNode,newNode,label);
            addEdge.add(newEdge);
          }
        }
      }
    }
    result.getEdges().removeAll(removeEdge);
    result.getEdges().addAll(addEdge);
    result.getNodes().removeAll(removeNode);
    return result;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t)
  {
    return null;
  }

  //return PromelaGraph of proctype statements
  public Object visitSend(final SendTreeNode t)
  {
    final String chanName = t.getChild(0).getText();
    final ChanInfo ch = mVisitor.getChan().get(chanName);
    final int length = ch.getChanLength();

    //Send statement

      labels = new ArrayList<String>();
      labels.add(chanName);

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }

      String ename = labels.get(0);
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
        //create indexedIdentifier
      IndexedIdentifierProxy indexEvent;
      if(length==0){
        ename = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
      }else{
        ename = "send_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
      }
        //System.out.println(indexEvent);
        return new PromelaGraph(indexEvent);

  }

  public Object visitReceive(final ReceiveTreeNode t)
  {
     //receive statement
    final String chanName = t.getChild(0).getText();
    final ChanInfo ch = mVisitor.getChan().get(chanName);
    final int length = ch.getChanLength();
    THashSet<IdentifierProxy> chanData ;
    labels = new ArrayList<String>();
    labels.add(chanName);


    for(int i = 0; i <t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    if(labels.size()-1 == ch.getDataLength()){
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
      IndexedIdentifierProxy indexEvent;
      final String name;
      if(length==0){

        name = "exch_"+chanName;

        indexEvent = mFactory.createIndexedIdentifierProxy(name,indexes);
        //System.out.println(indexEvent);
      }else{
        name = "send_"+chanName;
        indexEvent = mFactory.createIndexedIdentifierProxy(name,indexes);
      }
        return new PromelaGraph(indexEvent);
    }
    else{
    if(length==1){
      chanData =(THashSet<IdentifierProxy>) ch.getRecData();

    }else{

      chanData = (THashSet<IdentifierProxy>)ch.getChannelData();
    }
    return new PromelaGraph(chanData,mFactory);
    }

  }

  public Object visitConstant(final ConstantTreeNode t)
  {
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t)
  {
    mIsInit = true;
    copyOfOccur = new Hashtable<String,Integer>(mVisitor.getOccur());
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
    final PromelaGraph initGraph = collectGraphs((PromelaTree) t.getChild(0));
    final GraphProxy graph = initGraph.createGraphProxy(mFactory, t.getText());
    final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
    mComponents.add(component);
    return null;
  }

  public Collection<String> distinct(final Collection<String> t,final Collection<String> output){
    final ArrayList<String> temp = new ArrayList<String>(t);
    for(int i=0;i<t.size();i++){
      final String compare = temp.get(i);
      temp.set(i, null);
      if(temp.contains(compare)){
        output.add(compare);
      }
    }
    return output;
  }


  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    //assert t.getText().equals("atomic");
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("initrun");
    final PromelaGraph initGraph = new PromelaGraph(ident);
    return initGraph;
  }

  public Object visitRun(final RunTreeNode t)
  {
    final String name = t.getChild(0).getText();
    PromelaGraph graph=null;
    if(!mIsInit){

      //duplicatedRun.add(name);
      final int occurance = mVisitor.getOccur().get(name);

      if(occurance>1){
        final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
        final IdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
        indexes.add(id);
        final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("run_"+name,indexes);
        graph = new PromelaGraph(ident);
      }else{
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
        graph = new PromelaGraph(ident);
      }

    }else{
      final int occur1 = mVisitor.getOccur().get(name);
      final int occur2 = copyOfOccur.get(name);
      if(occur1-occur2 < occur1){
        final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
        final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+(occur1-occur2));
        indexes.add(id);
        final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("run_"+name,indexes);
        graph = new PromelaGraph(ident);
      }
      copyOfOccur.put(name,occur2-1);
    }
    return graph;
  }

  //return graph of init events
  public Object visitName(final NameTreeNode t)
  {
    if(t.getParent() instanceof RunTreeNode){
      procNames.add(t.getText());
    }
    return null;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    PromelaGraph result = null;

      for(int i=0;i<t.getChildCount();i++){

        final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
        mUnWinding = false;
        //if(!(t.getChild(i).getChild(0) instanceof SkipTreeNode)){
        result = PromelaGraph.sequentialComposition(result,step,mUnWinding,mFactory);
      //  }
      }

    return result;
  }

  public Object visitType(final TypeTreeNode t)
  {
    return null;
  }
  public Object visitCondition(final ConditionTreeNode t)
  {
    PromelaGraph result = null;

    for(int i=0;i<t.getChildCount();i++){
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      result = PromelaGraph.combineComposition(result,step,mUnWinding,mFactory);

    }

    return result;
  }

  public Object visitDoStatement(final DoConditionTreeNode t)
  {
    final boolean unwinding = mUnWinding;
   // System.out.println(unwinding);
    counter =counter+1;
    Tree tree = t;
    while(!(tree instanceof ProctypeTreeNode)){
      tree = tree.getParent();
    }
    System.out.println(unwinding+" "+tree.getText());
 //   final String name = tree.getText();
    final PromelaGraph result;
 //   final PromelaNode startNode = new PromelaNode();
 //   final PromelaNode secondStartNode = new PromelaNode();
    final PromelaNode endNode = new PromelaNode(PromelaNode.EndType.END);
    final List<PromelaGraph> branches = new ArrayList<PromelaGraph>();
    for(int i=0;i<t.getChildCount();i++){
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      branches.add(step);
      //result = PromelaGraph.doCombineComposition(result,step,startNode,endNode,secondStartNode,mFactory,name,mSourceOfBreakNode,unwinding);
    }
    result = PromelaGraph.doCombineComposition2(branches, unwinding,mFactory);
    mLabelEnd.put(""+counter,endNode);

    return result;
  }
  public Object visitBreak(final BreakStatementTreeNode t)
  {
    if (!mUnWinding) {
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.BREAK);
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges);
      return result;
    } else {
      //create step_* transition
      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode)) {
        tree = tree.getParent();
      }
      final String name = tree.getText();
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.BREAK);
      final PromelaNode startNode = new PromelaNode();
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("step_"+name);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      if (!mVisitor.getEvents().contains(event)) {
        mVisitor.getEvents().add(event);
      }
      final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
      label.add(ident2);
      final PromelaLabel l = new PromelaLabel(label);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaEdge edge = new PromelaEdge(startNode,node,l);
      cEdges.add(edge);
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,startNode);
      return result;
    }
  }

  public Object visitLabel(final LabelTreeNode t)
  {

    PromelaGraph result = null;

    final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(0));
    result = PromelaGraph.sequentialComposition(result,step,mUnWinding,mFactory);
    mGotoNode.put(t.getText(), result.getStart());

    return result;
  }

  public Object visitGoto(final GotoTreeNode t)
  {
    if (!mUnWinding) {
      final String labelName = t.getText();
      final PromelaNode node = new PromelaNode(labelName); //creating goto label
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes, cEdges);
      return result;
    } else {
      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode)) {
        tree = tree.getParent();
      }
      final String name = tree.getText();
      final String labelName = t.getText();
      final PromelaNode node = new PromelaNode(labelName); //creating goto label
      final PromelaNode startNode = new PromelaNode();
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      cNodes.add(startNode);
      final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("step_"+name);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      if (!mVisitor.getEvents().contains(event)) {
        mVisitor.getEvents().add(event);
      }
      final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
      label.add(ident2);
      final PromelaLabel l = new PromelaLabel(label);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaEdge edge = new PromelaEdge(startNode,node,l);
      cEdges.add(edge);
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,startNode);
      return result;
    }
  }

  public Object visitSkip(final SkipTreeNode t)
  {
/*    Tree tree = t;
    while(!(tree instanceof ProctypeTreeNode)){
      tree = tree.getParent();
    }

    final PromelaNode node = mLabelEnd.get(""+counter);
    final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
    cNodes.add(node);
    final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
    final PromelaGraph result = new PromelaGraph(cNodes,cEdges,node);
    return result;
*/
    //System.out.println(mUnWinding);
    if(!mUnWinding){
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.END);
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,node);
      return result;
    } else {
      //    System.out.println("true");
      Tree tree = t;
      while(!(tree instanceof ProctypeTreeNode)){
        tree = tree.getParent();
      }
      final String name = tree.getText();

      final PromelaNode node = new PromelaNode(PromelaNode.EndType.END);
      final PromelaNode startNode = new PromelaNode();
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      cNodes.add(startNode);
      final Collection<SimpleExpressionProxy> label = new ArrayList<SimpleExpressionProxy>();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("step_"+name);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      if(!mVisitor.getEvents().contains(event)){
        mVisitor.getEvents().add(event);
      }
      final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
      label.add(ident2);
      final PromelaLabel l = new PromelaLabel(label);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaEdge edge = new PromelaEdge(startNode,node,l);
      cEdges.add(edge);
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,startNode);
      return result;
    }

  }



}
