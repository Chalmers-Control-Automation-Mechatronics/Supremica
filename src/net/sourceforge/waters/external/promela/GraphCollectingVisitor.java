package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

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
  int runedOnce = 0;
  Hashtable<String,Integer> copyOfOccur = new Hashtable<String,Integer>();
  Collection<Proxy> mComponents = new ArrayList<Proxy>();
 // Collection<Proxy> mCompleteComponents = new ArrayList<Proxy>();

  Map<PromelaNode,PromelaEdge> mSourceOfBreakNode = new HashMap<PromelaNode,PromelaEdge>();

  Map<String,PromelaNode> mGotoNode = new HashMap<String,PromelaNode>();

  Map<String,PromelaNode> mLabelEnd = new HashMap<String,PromelaNode>();

  List<Message> mOutput = new ArrayList<Message>();

 // final Collection<NodeProxy> mChannelNode = new ArrayList<NodeProxy>();
 // final Collection<EdgeProxy> mChannelEdge = new ArrayList<EdgeProxy>();
  private Collection<EventDeclProxy> mEvents = new ArrayList<EventDeclProxy>();

  public GraphCollectingVisitor(final EventCollectingVisitor v){
    mVisitor = v;
    mFactory = v.getFactory();



  }
  public PromelaGraph collectGraphs(final PromelaTree node)
  {
    return (PromelaGraph) node.acceptVisitor(this);
  }
  public Collection<EventDeclProxy> getEvent(){
    return mEvents;
  }

  public Collection<Proxy> getComponents(){
    return mComponents;
  }
  public Object visitModule(final ModuleTreeNode t)
  {
    mEvents = new ArrayList<EventDeclProxy>(mVisitor.getEvents());
    mOutput = new ArrayList<Message>(mVisitor.getMsg());
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
        //final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
       // mEvents.add(event);
        //ident = mFactory.createSimpleIdentifierProxy("run_"+procName);
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

      final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
      indexes.add(id);

      final IndexedIdentifierProxy name = mFactory.createIndexedIdentifierProxy("proctype_"+procName,indexes);


      component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);


      final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(procs);
      final Collection<SimpleComponentProxy> c = new ArrayList<SimpleComponentProxy>();

      c.add(component);
      final ForeachComponentProxy f = mFactory.createForeachComponentProxy("procid", en,null,c);
      mComponents.add(f);

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
    @SuppressWarnings("unused")
    final ModuleProxyCloner cloner = mFactory.getCloner();

    if(ch.getChanLength()==1){

      final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
      final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
      final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy id =
          mFactory.createSimpleIdentifierProxy(accepting);
      final List<SimpleIdentifierProxy> list = Collections.singletonList(id);
      final PlainEventListProxy eventList =
          mFactory.createPlainEventListProxy(list);
      final NodeProxy start = mFactory.createSimpleNodeProxy("empty", eventList, true, null, null, null);
      mNodes.add(start);
      loop1:
      for(final Message msg : mVisitor.getMsg()){
        if(msg.getMsg().contains(null)){
          continue loop1;
        }
        String ename = "s";
        for(final SimpleExpressionProxy s: msg.getMsg()){
          ename += "_"+s;
        }
        final NodeProxy node = mFactory.createSimpleNodeProxy(ename);
        mNodes.add(node);


        final Collection<IdentifierProxy> labelBlock = new ArrayList<IdentifierProxy>();
        for(int i=0;i<msg.getSenders().size();i++){
          final String procname = msg.getSenders().get(i);
          if(mVisitor.getOccur().get(procname)==1){
            final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
            for(final SimpleExpressionProxy s:  msg.getMsg()){
              final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
              indexes.add(c1);
            }
            final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("send_"+name, indexes);
            labelBlock.add(ident);
          }
          else{
            for(int a=0;a<mVisitor.getOccur().get(procname);a++){
              final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
              final IdentifierProxy identName = mFactory.createSimpleIdentifierProxy(procname+"_"+a);
              indexes.add(identName);
              for(final SimpleExpressionProxy s:  msg.getMsg()){
                final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                indexes.add(c1);
              }
              final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("send_"+name, indexes);
              labelBlock.add(ident);
            }
          }
        }
        final LabelBlockProxy label =
              mFactory.createLabelBlockProxy(labelBlock, null);
        final EdgeProxy sendEdge =
              mFactory.createEdgeProxy(start, node, label, null, null, null, null);
        mEdges.add(sendEdge);

        final Collection<IdentifierProxy> labelBlock2 = new ArrayList<IdentifierProxy>();

        for(int i=0;i<msg.getRecipients().size();i++){
          final String procname = msg.getRecipients().get(i);
          if(mVisitor.getOccur().get(procname)==1){

            final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
            for(final SimpleExpressionProxy s:  msg.getMsg()){
              final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
              indexes.add(c1);
            }
            final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("recv_"+name, indexes);
            labelBlock2.add(ident);


          }else{
            for(int a=0;a<mVisitor.getOccur().get(procname);a++){

              final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
              final IdentifierProxy identName = mFactory.createSimpleIdentifierProxy(procname+"_"+a);
              indexes.add(identName);
              for(final SimpleExpressionProxy s:  msg.getMsg()){
                final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                indexes.add(c1);
              }
              final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("recv_"+name, indexes);
              labelBlock2.add(ident);
            }
          }
        }
        final LabelBlockProxy label2 =
            mFactory.createLabelBlockProxy(labelBlock2, null);
        final EdgeProxy recvEdge =
            mFactory.createEdgeProxy(node, start, label2, null, null, null, null);
        mEdges.add(recvEdge);
      }

      final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("channel_"+name);
      final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
      mComponents.add(component);
    }
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
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
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
      final String chanName = t.getChild(0).getText();
      final ChanInfo ch = mVisitor.getChan().get(chanName);
      final int length = ch.getChanLength();
      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode)) {
        tree = tree.getParent();
      }
      final String name = tree.getText();

      labels = new ArrayList<String>();
      labels.add(chanName);

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }


      Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
      final List<SimpleExpressionProxy> index = new ArrayList<SimpleExpressionProxy>(indexes);
      final Message msg = new Message(index);
      final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();
      for(final Message m: mOutput){

        if(m.equals(msg)){
          if(m.hasRecipients()){
            for(final String rec: m.getRecipients()){

              for(int i=0;i<mVisitor.getOccur().get(rec);i++){
              indexes = new ArrayList<SimpleExpressionProxy>();
              final Collection<SimpleExpressionProxy> sendRange = new ArrayList<SimpleExpressionProxy>();
              if(m.hasSenders()){
                  if(mVisitor.getOccur().get(name)>1){
                    final IdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
                    indexes.add(id);
                    sendRange.add((SimpleExpressionProxy) cloner.getClone(id));
                  }else if(m.getSenders().size()>1){
                    final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+0);
                    indexes.add(id);
                    sendRange.add((SimpleExpressionProxy) cloner.getClone(id));
                  }
              }
              if(m.getRecipients().size()>1 || mVisitor.getOccur().get(rec)>1){
                final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(rec+"_"+i);
                indexes.add(id);
              }
             String cName="s";
             for(int y=1;y<labels.size();y++){
               final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
               cName+= "_"+labels.get(y);
               indexes.add(c);
               sendRange.add((SimpleExpressionProxy) cloner.getClone(c));
             }
             final Collection<IdentifierProxy> labelBlock = new ArrayList<IdentifierProxy>();
        //     final NodeProxy node = mFactory.createSimpleNodeProxy(cName);
        //     if(!mChannelNode.contains(node)){
        //       mChannelNode.add(node);
        //     }
             final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("send_"+name, cloner.getClonedList(sendRange));
             labelBlock.add(ident);
        //     final LabelBlockProxy label =
        //       mFactory.createLabelBlockProxy(labelBlock, null);
       //      final ArrayList<NodeProxy> templist = new ArrayList<NodeProxy>(mChannelNode);

       //      final EdgeProxy edge =mFactory.createEdgeProxy(templist.get(0), node, label, null, null, null, null);
       //      mChannelEdge.add(edge);
             //create indexedIdentifier
             IndexedIdentifierProxy indexEvent;
             String ename = labels.get(0);
             if(length==0){
               ename = "exch_"+ename;
               indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
             }else{
               ename = "send_"+ename;
               indexEvent = mFactory.createIndexedIdentifierProxy(ename,sendRange);
             }
             boolean test = false;
             for(final IdentifierProxy id: events){
               if(comparator.compare(id, indexEvent)==0){
                 test = true;
                 break;
               }
             }
             if(!test){
               events.add(indexEvent);
             }
            }
          }
        }
      }
      }
      return new PromelaGraph(events,mFactory);

  }

  public Object visitReceive(final ReceiveTreeNode t)
  {
    runedOnce++;
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);

     //receive statement
    final String chanName = t.getChild(0).getText();
    final ChanInfo ch = mVisitor.getChan().get(chanName);
    final int length = ch.getChanLength();

    labels = new ArrayList<String>();
    labels.add(chanName);
    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode)) {
      tree = tree.getParent();
    }
    final String name = tree.getText();

    for(int i = 0; i <t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
    final Collection<SimpleExpressionProxy> templabel = new ArrayList<SimpleExpressionProxy>();
    for(int y=1;y<labels.size();y++){
      if(labels.get(y)!=null){
      final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
      indexes.add(c);
      templabel.add(c);
      }else{
        indexes.add(null);
      }
    }
    /*
     * Used to create Event declaration
     */
    final Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>();
    final Collection<SimpleIdentifierProxy> sender = new ArrayList<SimpleIdentifierProxy>();
    final Collection<SimpleIdentifierProxy> reciever = new ArrayList<SimpleIdentifierProxy>();

    final List<SimpleExpressionProxy> index = new ArrayList<SimpleExpressionProxy>(indexes);
    final Message msg = new Message(index);
    final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();

    for(final Message m: mOutput){

      if(m.equals(msg)){
        if(m.hasSenders()){
          for(final String send: m.getSenders()){

            for(int i=0;i<mVisitor.getOccur().get(send);i++){
              if((msg.getMsg().size()==templabel.size())){
                final Collection<SimpleExpressionProxy> indexes1 = new ArrayList<SimpleExpressionProxy>();
                final Collection<SimpleExpressionProxy> recRange = new ArrayList<SimpleExpressionProxy>();
                //sender
                if(m.getSenders().size()>1 || mVisitor.getOccur().get(send)>1){
                  final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(send+"_"+i);
                  boolean testSend=false;
                  for(final SimpleExpressionProxy s: sender){
                    if(comparator.compare(s, id)==0){
                      testSend=false;
                      break;
                    }else{
                      testSend = true;
                    }
                  }
                  if(sender.size()==0){
                    testSend=true;
                  }
                  if(testSend){
                    sender.add((SimpleIdentifierProxy)cloner.getClone(id));
                  }
                  indexes1.add(id);
                }
                //reciever
                if(m.hasRecipients()){
                    if(mVisitor.getOccur().get(name)>1){
                      final IdentifierProxy r = mFactory.createSimpleIdentifierProxy("procid");
                      indexes1.add(r);
                      for(int a=0;a<mVisitor.getOccur().get(name);a++){
                      final SimpleIdentifierProxy r1 = mFactory.createSimpleIdentifierProxy(name+"_"+a);
                      boolean testSend=false;
                      for(final SimpleExpressionProxy s: reciever){
                        if(comparator.compare(s, r1)==0){
                          testSend=false;
                          break;
                        }else{
                          testSend = true;
                        }
                      }
                      if(reciever.size()==0){
                        testSend = true;
                      }
                      if(testSend){
                        reciever.add(r1);
                        recRange.add((SimpleExpressionProxy) cloner.getClone(r1));
                      }
                      }
                    }else if(m.getRecipients().size()>1){
                      final IdentifierProxy r = mFactory.createSimpleIdentifierProxy(name+"_"+0);
                      indexes1.add(r);

                      final SimpleIdentifierProxy r1 = (SimpleIdentifierProxy) cloner.getClone(r);
                      boolean testSend=false;
                      for(final SimpleExpressionProxy s: reciever){
                        if(comparator.compare(s, r1)==0){
                          testSend=false;
                          break;
                        }else{
                          testSend = true;
                        }
                      }
                      if(reciever.size()==0){
                        testSend = true;
                      }
                      if(testSend){
                        reciever.add(r1);
                        recRange.add((SimpleExpressionProxy) cloner.getClone(r1));
                      }
                    }


                }
                //constant msgs
                indexes1.addAll( cloner.getClonedList(msg.getMsg()));
                recRange.addAll(cloner.getClonedList(msg.getMsg()));
                //create indexedIdentifier
                IndexedIdentifierProxy indexEvent;
                String ename = labels.get(0);
                if(length==0){
                  ename = "exch_"+ename;
                  indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes1);
                }else{
                  ename = "recv_"+ename;
                  indexEvent = mFactory.createIndexedIdentifierProxy(ename,recRange);
                }
                boolean test = false;
                for(final IdentifierProxy id: events){
                  if(comparator.compare(id, indexEvent)==0){
                    test = true;
                    break;
                  }
                }
                if(!test){
                  events.add(indexEvent);
                }
              } else{
              for(final Message m2: mOutput){
               // final Collection<SimpleExpressionProxy> sendRange = new ArrayList<SimpleExpressionProxy>();
                final Collection<SimpleExpressionProxy> recRanges = new ArrayList<SimpleExpressionProxy>();
                if(!m2.equals(msg)){
                  if(m2.getRecipients().contains(name)){
                   indexes = new ArrayList<SimpleExpressionProxy>();

                   //sender
                   if(mVisitor.getOccur().get(send)>1){
                     final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(send+"_"+i);
                     boolean testSend=false;
                     for(final SimpleExpressionProxy s: sender){
                       if(comparator.compare(s, id)==0){
                         testSend=false;
                         break;
                       }else{
                         testSend = true;
                       }
                     }
                     if(sender.size()==0){
                       testSend = true;
                     }
                     if(testSend){
                       sender.add((SimpleIdentifierProxy) cloner.getClone(id));
                     }
                     indexes.add(id);
                 //    sendRange.add(id);
                   }

                   //reciever
                   if(m.hasRecipients()){
                       if(mVisitor.getOccur().get(name)>1){
                         final IdentifierProxy r = mFactory.createSimpleIdentifierProxy("procid");
                         indexes.add(r);
                         recRanges.add((SimpleExpressionProxy) cloner.getClone(r));
                         for(int a=0;a<mVisitor.getOccur().get(name);a++){
                           final SimpleIdentifierProxy r1 = mFactory.createSimpleIdentifierProxy(name+"_"+a);
                           boolean testSend=false;
                           for(final SimpleExpressionProxy s: reciever){
                             if(comparator.compare(s, r1)==0){
                               testSend=false;
                               break;
                             }else{
                               testSend = true;
                             }
                           }
                           if(reciever.size()==0){
                             testSend = true;
                           }
                           if(testSend){
                             reciever.add(r1);
                             //recRanges.add((SimpleExpressionProxy) cloner.getClone(r1));
                           }
                       }
                       }else if(m.getRecipients().size()>1){
                         final IdentifierProxy r = mFactory.createSimpleIdentifierProxy(name+"_"+0);
                         indexes.add(r);
                         for(final String n: m.getRecipients()){
                           final SimpleIdentifierProxy r1 = mFactory.createSimpleIdentifierProxy(n+"_"+0);

                           boolean testSend=false;
                           for(final SimpleExpressionProxy s: reciever){
                             if(comparator.compare(s, r1)==0){
                               testSend=false;
                               break;
                             }else{
                               testSend = true;
                             }
                           }
                           if(reciever.size()==0){
                             testSend = true;
                           }
                           if(testSend){
                             reciever.add(r1);
                             recRanges.add((SimpleExpressionProxy) cloner.getClone(r1));
                           }
                       }
                       }
                   }
                   //constants
                   indexes.addAll( cloner.getClonedList(m2.getMsg()));
                   //sendRange.addAll(cloner.getClonedList(m2.getMsg()));
                   recRanges.addAll(cloner.getClonedList(m2.getMsg()));
                   //create indexedIdentifier
                   IndexedIdentifierProxy indexEvent;
                   String ename = labels.get(0);
                   if(length==0){
                     ename = "exch_"+ename;
                     indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
                   }else{
                     ename = "recv_"+ename;
                     indexEvent = mFactory.createIndexedIdentifierProxy(ename,recRanges);
                   }
                   boolean test = false;
                   for(final IdentifierProxy id: events){
                     if(comparator.compare(id, indexEvent)==0){
                       test = true;
                       break;
                     }
                   }
                   if(!test){
                     events.add(indexEvent);
                   }
                }
                }
            }
            }
          }
        }
      }
    }
    }
    final Collection<SimpleExpressionProxy> sendRange = new ArrayList<SimpleExpressionProxy>();
    final Collection<SimpleExpressionProxy> recRange = new ArrayList<SimpleExpressionProxy>();
    final int lengthOfChan = mVisitor.getChan().get(chanName).getChanLength();
    if(runedOnce==1){
      if(sender.size()>0){
        final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(sender);
        ranges.add(en);
        if(lengthOfChan>0){
          sendRange.add((SimpleExpressionProxy) cloner.getClone(en));
        }
      }
      if(reciever.size()>0){
        final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(reciever);
        ranges.add(en);
        recRange.add((SimpleExpressionProxy) cloner.getClone(en));
      }

      ranges.addAll(cloner.getClonedList(mVisitor.getRanges()));
      sendRange.addAll(cloner.getClonedList(mVisitor.getRanges()));
      recRange.addAll(cloner.getClonedList(mVisitor.getRanges()));

      if(lengthOfChan==0){
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("exch_"+chanName);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
      mEvents.add(event);
      }else{
        final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("send_"+chanName);
        final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("recv_"+chanName);
        final EventDeclProxy event1 = mFactory.createEventDeclProxy(ident1, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, sendRange, null, null);
        final EventDeclProxy event2 = mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, recRange, null, null);
        mEvents.add(event1);
        mEvents.add(event2);
      }
    }
    return new PromelaGraph(events,mFactory);
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

    }else{

      final int occur1 = mVisitor.getOccur().get(name);
      final int occur2 = copyOfOccur.get(name);
      if(occur1>1){
      if(occur1-occur2 < occur1){
        final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
        final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+(occur1-occur2));
        indexes.add(id);
        //final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("run_"+name,indexes);
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
        graph = new PromelaGraph(ident);
      }
      copyOfOccur.put(name,occur2-1);
      }else{
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
        graph = new PromelaGraph(ident);
      }
    }
    return graph;
  }

  //return graph of init events
  public Object visitName(final NameTreeNode t)
  {
    if(t.getParent() instanceof RunTreeNode){
      procNames.add(t.getText());
    }
    if(t.getParent() instanceof MsgTreeNode){
      labels.add(null);
    }
    return null;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    PromelaGraph result = null;
      for(int i=0;i<t.getChildCount();i++){
        final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
        mUnWinding = false;
        result = PromelaGraph.sequentialComposition(result,step,mUnWinding,mFactory);
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
    counter =counter+1;
    Tree tree = t;
    while(!(tree instanceof ProctypeTreeNode)){
      tree = tree.getParent();
    }
    final PromelaGraph result;

    final PromelaNode endNode = new PromelaNode(PromelaNode.EndType.END);
    final List<PromelaGraph> branches = new ArrayList<PromelaGraph>();
    for(int i=0;i<t.getChildCount();i++){
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      branches.add(step);
    }
    result = PromelaGraph.doCombineComposition2(branches, unwinding,mFactory);
    mLabelEnd.put(""+counter,endNode);

    return result;
  }
  public Object visitBreak(final BreakStatementTreeNode t)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
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
      final IdentifierProxy ident;
      final IdentifierProxy ident2;
      final Collection<SimpleIdentifierProxy> procs = new ArrayList<SimpleIdentifierProxy>();
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
      if(mVisitor.getOccur().get(name)==1){
        ident = mFactory.createSimpleIdentifierProxy("step_"+name);
        ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
      }else{


        final Collection<SimpleIdentifierProxy> list2 = new ArrayList<SimpleIdentifierProxy>();
        for(int i=0;i<mVisitor.getOccur().get(name);i++){
          final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+i);
          //indexes.add(id);
          procs.add((SimpleIdentifierProxy) cloner.getClone(id));
        }
        final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(procs);
        indexes.add(en);
        ident = mFactory.createSimpleIdentifierProxy("step_"+name);

        //create step[procid] for edge label
        final SimpleIdentifierProxy id2 = mFactory.createSimpleIdentifierProxy("procid");
        list2.add(id2);
        ident2 = mFactory.createIndexedIdentifierProxy("step_"+name,list2);
      }
      EventDeclProxy event;
      if(procs.size()==0){
        event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      }else{
        event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, indexes,null, null);
      }
      if (!mEvents.contains(event)) {
       mEvents.add(event);
      }
     // ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
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
      if (!mEvents.contains(event)) {
        mEvents.add(event);
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
    if(!mUnWinding){
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.END);
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,node);
      return result;
    } else {
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
      if(!mEvents.contains(event)){
        mEvents.add(event);
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

  @SuppressWarnings("unused")
  public void createChannelGraph()
  {
    final String name = chanNames.get(0);
    final ModuleProxyCloner cloner = mFactory.getCloner();
 //   final GraphProxy graph = mFactory.createGraphProxy(true, null, mChannelNode, mChannelEdge);
 //  final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("channel_"+name);
 //   final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
 //   mCompleteComponents.add(component);
 //   mCompleteComponents.addAll(mComponents);
  }
}
