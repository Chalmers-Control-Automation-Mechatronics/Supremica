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


  Map<PromelaNode,PromelaEdge> mSourceOfBreakNode = new HashMap<PromelaNode,PromelaEdge>();

  Map<String,PromelaNode> mGotoNode = new HashMap<String,PromelaNode>();

  Map<String,PromelaNode> mLabelEnd = new HashMap<String,PromelaNode>();

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

    final PromelaGraph newGraph = new PromelaGraph(ident,false);
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
      //mFactory.c

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
      for(final Message msg : ch.getOutput()){
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
      System.out.println(ch);
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

      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
      final List<SimpleExpressionProxy> index = new ArrayList<SimpleExpressionProxy>(indexes);
      final Message msg = new Message(index);
      final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();
      final ChanInfo c = ch;
      final Collection<SimpleIdentifierProxy> senders = new ArrayList<SimpleIdentifierProxy>();
      final Collection<SimpleIdentifierProxy> recvs = new ArrayList<SimpleIdentifierProxy>();
      for(final Message m: ch.getOutput()){

        if(m.equals(msg)){
          if(c.isSenderPresent()){

            if(mVisitor.getOccur().get(name)>1){
                for(int i=0;i<mVisitor.getOccur().get(name);i++){
                  final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy("procid");
                  senders.add(ident);
                }
            }else if(mVisitor.getOccur().get(name)==1){
                final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name+"_"+0);
                senders.add(ident);
            }
          }

          if(c.isRecipientPresent()){
            Collections.sort(m.getRecipients());
            for(final String n: m.getRecipients()){

              if(mVisitor.getOccur().get(n)>1){
                for(int i=0;i<mVisitor.getOccur().get(n);i++){
                  final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(n+"_"+i);
                  recvs.add(ident);
                }
              }else if(mVisitor.getOccur().get(n)==1){
                final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(n+"_"+0);
                recvs.add(ident);
              }
            }
          }

          if(senders.size()>=1 && recvs.size()>=1){
          for(final SimpleIdentifierProxy s1: senders){
            for(final SimpleIdentifierProxy s2: recvs){
              final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
              in.add(s1);
              if(length==0){
                in.add(s2);
              }
              in.addAll(cloner.getClonedList(indexes));
              IndexedIdentifierProxy indexEvent;
              String ename = labels.get(0);
              if(length ==0){
                ename = "exch_"+ename;
                indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
              }else{
                ename = "send_"+ename;
                indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
        }else if(senders.size()==0 && recvs.size()==0){
          final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
          in.addAll(cloner.getClonedList(indexes));
          String ename = labels.get(0);
          IndexedIdentifierProxy indexEvent;
          if(length ==0){
            ename = "exch_"+ename;
            indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
          }else{
            ename = "send_"+ename;
            indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
        }else if(senders.size()==0 && recvs.size()>0){
          for(final SimpleIdentifierProxy s2: recvs){
            final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
            if(length==0){
              in.add(s2);
            }
            in.addAll(cloner.getClonedList(indexes));
            IndexedIdentifierProxy indexEvent;
            String ename = labels.get(0);
            if(length ==0){
              ename = "exch_"+ename;
              indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
            }else{
              ename = "send_"+ename;
              indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
        }else if(senders.size()>=1 && recvs.size()==0){
          for(final SimpleIdentifierProxy s1: senders){
            final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
            in.add(s1);
            in.addAll(cloner.getClonedList(indexes));
            IndexedIdentifierProxy indexEvent;
            String ename = labels.get(0);
            if(length ==0){
              ename = "exch_"+ename;
              indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
            }else{
              ename = "send_"+ename;
              indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
      System.out.println("");
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
    final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
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

    final List<SimpleExpressionProxy> index = new ArrayList<SimpleExpressionProxy>(indexes);
    final Message msg = new Message(index);
    final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();


      for(final Message m: ch.getOutput()){
        if(m.equals(msg)){
            loop2:
            for(final Message m2: ch.getMessages()){
              if(m2.getMsg().contains(null)){
                continue loop2;
              }
              if(!m2.getMsg().contains(null) && !m.getMsg().contains(null)){
                for(int i=0;i< m.getMsg().size();i++){
                  if(comparator.compare(m.getMsg().get(i),m2.getMsg().get(i))!=0){
                    continue loop2;
                  }
                }
              }
              final Collection<SimpleIdentifierProxy> senders = new ArrayList<SimpleIdentifierProxy>();
              final Collection<SimpleIdentifierProxy> recvs = new ArrayList<SimpleIdentifierProxy>();
              if(ch.isSenderPresent()){
                for(final String s: m2.getSenders()){
                  if(mVisitor.getOccur().get(s)==1){
                    final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s+"_"+0);
                    senders.add(ident);
                  }else if (mVisitor.getOccur().get(s)>1){
                    //TODO
                    for(int i=0;i<mVisitor.getOccur().get(s);i++){
                      final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s+"_"+i);
                      senders.add(ident);
                    }
                  }
                }
              }

              if(ch.isRecipientPresent()){
                Collections.sort(m.getRecipients());

                  if(mVisitor.getOccur().get(name)>1){
                      final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy("procid");
                      recvs.add(ident);
                  }else if(mVisitor.getOccur().get(name)==1){
                    final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name+"_"+0);
                    recvs.add(ident);
                  }
              }
              Collection<SimpleExpressionProxy> data = new ArrayList<SimpleExpressionProxy>();
              if(msg.getMsg().contains(null)){
                if(indexes.size()==1){
                  data = cloner.getClonedList(m2.getMsg());
                }else if(indexes.size()>1){
                  //TODO
                  boolean test = false;
                  final ArrayList<SimpleExpressionProxy> l = new ArrayList<SimpleExpressionProxy>(indexes);
                  for(int i=0;i<indexes.size();i++){
                    if(l.get(i)==null){
                      test = true;
                    }else if(comparator.compare(m2.getMsg().get(i),l.get(i))==0){
                      test = true;
                    }else{
                      test = false;
                      break;
                    }
                  }
                  if(test){
                    data = cloner.getClonedList(m2.getMsg());
                  }
                }
              }else{
                data=cloner.getClonedList(indexes);
              }
              if(data.size()==0){
                continue loop2;
              }
              if(senders.size()>=1 && recvs.size()>=1){
                for(final SimpleIdentifierProxy s1: senders){
                  for(final SimpleIdentifierProxy s2: recvs){
                    final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
                    if(length==0){
                    in.add(s1);
                    }
                    in.add(s2);

                    if(!data.isEmpty()){
                      in.addAll(cloner.getClonedList(data));
                    }
                    IndexedIdentifierProxy indexEvent;
                    String ename = labels.get(0);
                    if(length ==0){
                      ename = "exch_"+ename;
                      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
                    }else{
                      ename = "recv_"+ename;
                      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
              }else if(senders.size()==0 && recvs.size()==0){
                final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
                in.addAll(cloner.getClonedList(data));
                String ename = labels.get(0);
                IndexedIdentifierProxy indexEvent;
                if(length ==0){
                  ename = "exch_"+ename;
                  indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
                }else{
                  ename = "recv_"+ename;
                  indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
              }else if(senders.size()==0 && recvs.size()>0){
                for(final SimpleIdentifierProxy s2: recvs){
                  final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
                  in.add(s2);
                  in.addAll(cloner.getClonedList(data));
                  IndexedIdentifierProxy indexEvent;
                  String ename = labels.get(0);
                  if(length ==0){
                    ename = "exch_"+ename;
                    indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
                  }else{
                    ename = "recv_"+ename;
                    indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
              }else if(senders.size()>=1 && recvs.size()==0){
                for(final SimpleIdentifierProxy s1: senders){
                  final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
                  if(length==0){
                    in.add(s1);
                  }
                  in.addAll(cloner.getClonedList(data));
                  IndexedIdentifierProxy indexEvent;
                  String ename = labels.get(0);
                  if(length ==0){
                    ename = "exch_"+ename;
                    indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
                  }else{
                    ename = "recv_"+ename;
                    indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
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
    final boolean isEnd = checkEnd(t.getParent());
    //  final boolean isEnd = false;
      return new PromelaGraph(events,isEnd,mFactory);
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
    final PromelaGraph initGraph = new PromelaGraph(ident,false);
    return initGraph;
  }

  public Object visitRun(final RunTreeNode t)
  {
    final String name = t.getChild(0).getText();
    PromelaGraph graph=null;
    final boolean isEnd = checkEnd(t.getParent());
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
        graph = new PromelaGraph(ident,isEnd);
      }
      copyOfOccur.put(name,occur2-1);
      }else{
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
        graph = new PromelaGraph(ident,isEnd);
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
    final boolean isEnd = checkEnd(t.getParent());
    for(int i=0;i<t.getChildCount();i++){
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      result = PromelaGraph.combineComposition(result,step,mUnWinding,isEnd,mFactory);

    }

    return result;
  }

  public Object visitDoStatement(final DoConditionTreeNode t)
  {
    final boolean unwinding = mUnWinding;
    final boolean isEnd = checkEnd(t.getParent());
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
   // final boolean isEnd = false;
    result = PromelaGraph.doCombineComposition2(branches, unwinding,isEnd,mFactory);
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

  }
  public boolean checkEnd(final Tree t){
    if(t instanceof LabelTreeNode){
      final String temp = t.getText();
      if(temp.length()>=3){
        final String end = temp.substring(0, 3);
        if(end.toLowerCase().equals("end")){
          return true;
        }
      }
    }
    return false;
  }
}
