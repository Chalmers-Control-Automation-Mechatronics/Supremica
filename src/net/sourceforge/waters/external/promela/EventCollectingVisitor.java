package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Hashtable;
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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

import org.antlr.runtime.tree.Tree;


public class EventCollectingVisitor implements PromelaVisitor
{
  int count = 0;
  boolean atomic = false;
  ArrayList<String> data =new ArrayList<String>();
  ArrayList<String> labels = new ArrayList<String>();
  private final Hashtable<String, ChanInfo> chan = new Hashtable<String,ChanInfo>();


  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

  ArrayList<Integer> lowerEnd = new ArrayList<Integer>();
  ArrayList<Integer> upperEnd = new ArrayList<Integer>();
  ArrayList<String> rangeData = new ArrayList<String>();
  final Hashtable<String,THashSet<IdentifierProxy>> procEvent = new Hashtable<String,THashSet<IdentifierProxy>>();
  final Hashtable<String,LabelTreeNode> gotoLabel = new Hashtable<String,LabelTreeNode>();
  //This is the output event table, for each proctype
  private final Collection<EventDeclProxy> mEventDecls = new ArrayList<EventDeclProxy>();

  private final Hashtable<String,Integer> occur = new Hashtable<String,Integer>();

  Collection<SimpleExpressionProxy> mRanges = new ArrayList<SimpleExpressionProxy>();

  //List<Message> mOutput = new ArrayList<Message>();
  List<String> channelMsg = new ArrayList<String>();
  Hashtable<String,String> mGlobalVar = new Hashtable<String,String>();
  Hashtable<String,List<Message>> mSendersMsg = new Hashtable<String,List<Message>>();
  //########################################################################
  //# Invocation
  public EventCollectingVisitor(final ModuleProxyFactory factory){
    mFactory = factory;

  }

  public void collectEvents(final PromelaTree node)
  {
    node.acceptVisitor(this);
  }
  public Collection<SimpleExpressionProxy> getRanges(){
    return mRanges;
  }
  public Hashtable<String,List<Message>> getSenderMsg(){
    return mSendersMsg;
  }
  public Collection<EventDeclProxy> getEvents(){
    return mEventDecls;
  }
  public Hashtable<String,THashSet<IdentifierProxy>> getChanEvent(){
    return procEvent;
  }
  public Hashtable<String,LabelTreeNode> getGotoLabel(){
    return gotoLabel;
  }
  public Hashtable<String,Integer> getOccur(){
    return occur;
  }

  public void makeMsg(){
    final ModuleProxyCloner cloner = mFactory.getCloner();
    //TODO
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator =
      new ExpressionComparator(optable);
    for(final Map.Entry<String,ChanInfo> chanIn: chan.entrySet())
    {
    //System.out.println(chanIn.getKey()+"  "+chanIn.getValue());
    //final List<Message> msgs = new ArrayList<Message>(chan.get(channelMsg.get(0)).getMessages());
    final List<Message> msgs = new ArrayList<Message>(chanIn.getValue().getMessages());
    //final List<Message> mOutput = chanIn.getValue().getOutput();
    final Hashtable<Integer,List<SimpleExpressionProxy>> table =new Hashtable<Integer,List<SimpleExpressionProxy>>();
    for(int i=0;i<chanIn.getValue().getDataLength();i++){
      //final List<SimpleExpressionProxy> channelList = new ArrayList<SimpleExpressionProxy>();
      table.put(i,new ArrayList<SimpleExpressionProxy>());
    }
   // table.put(0,channel_1);
   // table.put(1,channel_2);
    loop1:
    for(final Message m: msgs){
      for(final SimpleExpressionProxy s: m.getMsg()){
        if(s==null){
          continue loop1;
        }
      }
      for(int i=0;i<m.getMsg().size();i++){
        table.get(i).add(m.getMsg().get(i));
      }
    }
    Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>();
    for(int i=0;i<table.size();i++){
      Collections.sort(table.get(i),comparator);
      final BinaryOperator op = optable.getRangeOperator();
      final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, (SimpleExpressionProxy) cloner.getClone(table.get(i).get(0)), (SimpleExpressionProxy) cloner.getClone(table.get(i).get(table.get(i).size()-1)));
      ranges.add(range);
    }
    mRanges = new ArrayList<SimpleExpressionProxy>(ranges);

    final List<Message> recipients = new ArrayList<Message>();
    final List<Message> senders = new ArrayList<Message>();
 /*   for(final Message m: msgs){
      if(m.hasSenders()){
        senders.add(m);
        chanIn.getValue().addMsgList(m);
      }else if(m.hasRecipients()){
        recipients.add(m);
        chanIn.getValue().addMsgList(m);
      }else{
        chanIn.getValue().addMsgList(m);
      }
    }
    for(final Message m: msgs){
      if(!m.hasRecipients()){

      }
    }
*/

    for(final Message m: msgs){
      if(!m.hasSenders()){
        recipients.add(m);
        chanIn.getValue().addMsgList(m);
      }else if(!m.hasRecipients()){
        senders.add(m);
        chanIn.getValue().addMsgList(m);
      }else if(m.hasRecipients() && m.hasSenders()){
        senders.add(m);
        recipients.add(m);
        chanIn.getValue().addMsgList(m);
      }else{
        chanIn.getValue().addMsgList(m);
      }
    }

    //int a = 0;
    for (final Message m : chanIn.getValue().getOutput()) {
    //do{
    //  final Message m = recipients.get(a);
      final ArrayList<SimpleExpressionProxy> labels = new ArrayList<SimpleExpressionProxy>();
      if(recipients.contains(m)){
        for(final SimpleExpressionProxy s: m.getMsg()){
          if(s==null){
            break;
          }
          labels.add(s);
        }

      for(final Message m2: senders){
        boolean isSender = false;
        if(labels.size()==0){
          isSender=true;

        }else{
          for(int i=0;i<labels.size();i++){
            if(comparator.compare(labels.get(i),m2.getMsg().get(i))==0){
              isSender = true;
            }else{
              isSender = false;
              break;
            }
          }
        }
        if(isSender){

          for(final String s: m2.getSenders()){
            if(recipients.contains(m)){
              m.addSender(s);
            }
          }
          for(final String s: m.getRecipients()){
            m2.addRecipient(s);
          }


          boolean test = true;
          for(final Message t1: chanIn.getValue().getOutput()){
            if(t1.equals(m)){
              test = false;
              break;
            }else{
              test = true;
            }
          }
          if(test) chanIn.getValue().addMsgList(m);

        }

        boolean test = true;
        for(final Message t1: chanIn.getValue().getOutput()){
          if(t1.equals(m2)){
            test = false;
            break;
          }else{
            test = true;
          }
        }
        if(test) chanIn.getValue().addMsgList(m2);

      }
      boolean test = true;
      for(final Message t1: chanIn.getValue().getOutput()){
        if(t1.equals(m)){
          test = false;
          break;
        }else{
          test = true;
        }
      }
      if(test) chanIn.getValue().addMsgList(m);
    }

    }


    final ArrayList<Message> tempstore = new ArrayList<Message>();
    for(final Message msg: chanIn.getValue().getOutput()){
      if(msg.getMsg().contains(null)){
        tempstore.add(msg);
      }
    }
    //mOutput.removeAll(tempstore);
    for(final Message m1: tempstore){
      for(final Message m2: chanIn.getValue().getOutput()){
        if(m2.getSenders().size()==m1.getSenders().size()){
          boolean test = false;
          for(final String s: m2.getSenders()){
            if(m1.getSenders().contains(s)){
              test = true;
            }else{
              test = false;
              break;
            }
          }
          if(test){
            for(final String s: m1.getRecipients()){
              if(!m2.getRecipients().contains(s)){
                m2.addRecipient(s);
              }
            }
          }
        }
      }
    }
    final List<String> sendRange = new ArrayList<String>();
    final List<String> recRange = new ArrayList<String>();
    ranges = new ArrayList<SimpleExpressionProxy>();
    for(final Message m: chanIn.getValue().getOutput()){
      for(final String s: m.getSenders()){
        if(sendRange.size()==0){
            sendRange.add(s);
        }else{
          if(!sendRange.contains(s)){
            sendRange.add(s);
          }
        }
      }
      for(final String s: m.getRecipients()){
        if(recRange.size()==0){
            recRange.add(s);
        }else{
          if(!recRange.contains(s)){
            recRange.add(s);
          }
        }
      }
    }
    Collections.sort(sendRange);
    Collections.sort(recRange);
    final List<Message> cloneOutput = new ArrayList<Message>(chanIn.getValue().getOutput());
    cloneOutput.removeAll(tempstore);
    //final ChanInfo c = chan.elements().nextElement();
   // final int lengthOfChan = c.getChanLength();
   // final String chanName = chan.keys().nextElement();
    final ChanInfo c = chanIn.getValue();
    final int lengthOfChan = c.getChanLength();
    final String chanName = chanIn.getKey();
    boolean sending = false;

    final boolean receiving = false;
    for(final Message m: cloneOutput){
    //  if(!sending){
      if(!c.isSenderPresent()){
      if(m.getSenders().size()>1){
        c.setSenders(true);
        sending = true;
      }else if(m.getSenders().size()==1){
        final int occurrences = occur.get(m.getSenders().get(0));
     //   if(occurrences>1){
          c.setSenders(occurrences>1);
     //   }
      }
      }
 //     }
  //    if(!receiving){
      if(!c.isRecipientPresent()){
      if(m.getRecipients().size()>1){
        c.setRecipients(true);
        //receiving = true;
      }else if(m.getRecipients().size()==1){
        final int occurrences = occur.get(m.getRecipients().get(0));
        c.setRecipients(occurrences>1);
      //  if(occurrences>1){
      //    c.setRecipients(true);
         // receiving = true;
      //  }
      }

    }
     }
 //   }
    for(final Message m: cloneOutput){
 //     if(!sending){
        if(m.getSenders().size()>1){
         // c.setSenders(true);
          //c.addSenders(sendRange);
          final Collection<String> tempList = new ArrayList<String>();
          for (final String s : sendRange) {
            if(occur.get(s)==1){
            final String temp = s + "_0";
            for (int i = 0; i < occur.get(s); i++) {
              if(!c.getSenders().contains(temp)){
                tempList.add(temp);
              }
            }
            }else if(occur.get(s)>1){
              for(int i=0;i<occur.get(s);i++){
                final String temp = s+"_"+i;
                if(!c.getSenders().contains(temp)){
                  tempList.add(temp);
                }
              }
            }
          }
          c.addSenders(tempList);
          sending = true;
        } else if (m.getSenders().size() == 1) {
          final int occurrences = occur.get(m.getSenders().get(0));
      //    c.setSenders(occurrences > 1);
          if (c.isSenderPresent()) {
            final Collection<String> tempList = new ArrayList<String>();
           // for (int i = 0; i < occur.get(sendRange.get(0)); i++) {
            for(int i=0;i<occurrences;i++){
              //final String temp = sendRange.get(0) + "_" + i;
              final String temp = m.getSenders().get(0)+"_"+i;
              if(!c.getSenders().contains(temp)){
                tempList.add(temp);
              }
            }
            c.addSenders(tempList);
//            sending = true;
          }
        }

//      }
//      if (!receiving) {
        if (m.getRecipients().size() > 1) {
         // c.setRecipients(true);
          final Collection<String> tempList = new ArrayList<String>();
          for (final String s : recRange) {
            final String temp = s + "_0";
            for (int i = 0; i < occur.get(s); i++) {
              if(!c.getRecipients().contains(temp)){
                tempList.add(temp);
              }
            }
          }
          c.addRecipients(tempList);

        } else if (m.getRecipients().size() == 1) {
          final int occurance = occur.get(m.getRecipients().get(0));
     //     c.setRecipients(occurance > 1);
          if (c.isRecipientPresent()) {
            final Collection<String> tempList = new ArrayList<String>();
            for (int i = 0; i < occur.get(recRange.get(0)); i++) {
              final String temp = recRange.get(0) + "_" + i;
              if(!c.getRecipients().contains(temp)){
                tempList.add(temp);
              }
            }
            c.addRecipients(tempList);
   //         receiving = true;
          }
        }
 //     }

    }
  final Collection<SimpleExpressionProxy> specialSend = new ArrayList<SimpleExpressionProxy>();
    final Collection<SimpleExpressionProxy> specialRec = new ArrayList<SimpleExpressionProxy>();
    if(c.isSenderPresent()){
      if(c.getSenders().size()==1){
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(c.getSenders().get(0));
        ranges.add(ident);
        specialSend.add((SimpleExpressionProxy) cloner.getClone(ident));
      }else if(c.getSenders().size()>1){
        final Collection<SimpleIdentifierProxy> tempList = new ArrayList<SimpleIdentifierProxy>();
        for(final String s: c.getSenders()){
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s);
          tempList.add(ident);
        }

        final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(tempList);
        ranges.add(en);
        specialSend.add((SimpleExpressionProxy) cloner.getClone(en));
      }
    }

    if(c.isRecipientPresent()){
      if(c.getRecipients().size()==1){
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(c.getRecipients().get(0));
        ranges.add(ident);
        specialRec.add((SimpleExpressionProxy) cloner.getClone(ident));
      }else if(c.getRecipients().size()>1){
        final Collection<SimpleIdentifierProxy> tempList = new ArrayList<SimpleIdentifierProxy>();
        for(final String s: c.getRecipients()){
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s);
          tempList.add(ident);
        }
        final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(tempList);
        ranges.add(en);
        specialRec.add((SimpleExpressionProxy) cloner.getClone(en));
      }
    }

    ranges.addAll(mRanges);
    specialSend.addAll(mRanges);
    specialRec.addAll(cloner.getClonedList(mRanges));
    //now create event decls
    if(lengthOfChan==0){
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("exch_"+chanName);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
        mEventDecls.add(event);
    }else{
        final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("send_"+chanName);
        final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("recv_"+chanName);
        final EventDeclProxy event1 = mFactory.createEventDeclProxy(ident1, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, specialSend, null, null);
        final EventDeclProxy event2 = mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, specialRec, null, null);
        mEventDecls.add(event1);
        mEventDecls.add(event2);
    }
  }

    //create Run events
    if(!atomic){
      for(final Map.Entry<String,Integer> s: occur.entrySet()){
        if(s.getValue()==1){
          final IdentifierProxy id = mFactory.createSimpleIdentifierProxy("run_"+s.getKey());
          final EventDeclProxy event = mFactory.createEventDeclProxy(id, EventKind.CONTROLLABLE);
          mEventDecls.add(event);
        }else{
          final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
          final Collection<SimpleIdentifierProxy> enuSet = new ArrayList<SimpleIdentifierProxy>();
          for(int i=0;i<s.getValue();i++){
            final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(s.getKey()+"_"+i);
            //indexes.add(id);
            enuSet.add((SimpleIdentifierProxy) cloner.getClone(id));
          }
          final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(enuSet);
          indexes.add(en);
          final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+s.getKey());
          final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, indexes, null, null);

          mEventDecls.add(event);
        }

      }
    }


  }
 // public List<Message> getMsg(){
 //   return mOutput;
 // }
  //########################################################################
  //# Interface net.sourceforge.waters.external.promela.PromelaVisitor
  public Object visitModule(final ModuleTreeNode t)
  {
    //final String name = t.getText();
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
    final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(accepting);
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.PROPOSITION);
    mEventDecls.add(event);
    for(int i=0;i<t.getChildCount();i++) {
        ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
        final PromelaTree node = (PromelaTree)t.getChild(i);
        node.acceptVisitor(this);
    }
    return null;
  }

  public Object visitMsg(final MsgTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
  //  final PromelaTree tr1 = (PromelaTree) t.getChild(1);
 //   tr1.acceptVisitor(this);
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t){
    final int chanLength = Integer.parseInt(t.getChild(0).getText());
    final int datalength = t.getChildCount()-1;
    final String name = t.getParent().getChild(0).getText();
    final List<String> type = new ArrayList<String>();
    for(int i=1;i<t.getChildCount();i++){
      type.add(t.getChild(i).getText());
    }

    chan.put(name,new ChanInfo(name, chanLength, datalength,type));

    //final String chanName = t.getParent().getChild(0).getText();
    lowerEnd = new ArrayList<Integer>();
    upperEnd = new ArrayList<Integer>();
    @SuppressWarnings("unused")
    final ArrayList<String> rangeData = new ArrayList<String>();
    for(int i =1;i<t.getChildCount();i++){
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    final int size = t.getChildCount()-1;
    final Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>(size);
    for(int i=0;i<size;i++){
      final IntConstantProxy zero = mFactory.createIntConstantProxy(lowerEnd.get(i));
      final IntConstantProxy c255 = mFactory.createIntConstantProxy(upperEnd.get(i));
      final BinaryOperator op = optable.getRangeOperator();
      final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, zero, c255);
      ranges.add(range);
    }
  //  mRanges = new ArrayList<SimpleExpressionProxy>(ranges);

    return null;
  }

  public Object visitSend(final SendTreeNode t){

      data =new ArrayList<String>();
      labels = new ArrayList<String>();

      final String chanName = t.getChild(0).getText();
      labels.add(chanName);

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }

      final String ename = labels.get(0);
      final ChanInfo ch = chan.get(ename);
      final int length = ch.getChanLength();
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      final Collection<SimpleExpressionProxy> indexes2 = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
        final IntConstantProxy c2 = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes2.add(c2);
      }
      final List<SimpleExpressionProxy> msgList = new ArrayList<SimpleExpressionProxy>(indexes);
      final Message msg = new Message(msgList);

      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode)) {
        tree = tree.getParent();
      }
      final String n = tree.getText();
      if(mSendersMsg.containsKey(n)){
        final List<Message> l = mSendersMsg.get(n);
        l.add(msg);
        //System.out.println("123");
        mSendersMsg.put(n, l);
      }else{
        final List<Message> l = new ArrayList<Message>();
        l.add(msg);
        mSendersMsg.put(n, l);
      }
      msg.addSender(n);
      ch.addMessages(msg);
        //create indexedIdentifier, and store it for receive statement
      IndexedIdentifierProxy indexEvent = null;
      IndexedIdentifierProxy indexEvent2 = null;
      String name;
      if(length==0){
        name = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(name,indexes);
       // indexEvent2 = mFactory.createIndexedIdentifierProxy(name,indexes2);
      }else if(length==1){
        name = "send_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(name,indexes);
        name = "recv_"+ename;
        indexEvent2 = mFactory.createIndexedIdentifierProxy(name,indexes2);
      }

        THashSet<IdentifierProxy> temp = (THashSet<IdentifierProxy>) ch.getChannelData();
        if(temp==null){
          temp = new THashSet<IdentifierProxy>();
        }
        boolean same = false;
        if(temp.size()>1){
          for(final IdentifierProxy i : temp){
            if(i.compareTo(indexEvent)==0){
              same = true;
            }
          }
        }
        if(!same){
          temp.add(indexEvent);
          ch.addChannelData(indexEvent);
          ch.send(indexEvent.getIndexes());
          ch.addReceiveData(indexEvent2);
        }

        return indexEvent;
  }

  public Object visitReceive(final ReceiveTreeNode t)
  {
    data =new ArrayList<String>();
    labels = new ArrayList<String>();

    final String chanName = t.getChild(0).getText();
    labels.add(chanName);

    for(int i = 0; i <t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    final String ename = labels.get(0);
    final ChanInfo ch = chan.get(ename);

    final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);

    for(int y=1;y<labels.size();y++){
      if(labels.get(y)!=null){
      final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
      indexes.add(c);
      }else{
        indexes.add(null);
      }
    }

    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode)) {
      tree = tree.getParent();
    }
    final String n = tree.getText();

    final List<SimpleExpressionProxy> msgList = new ArrayList<SimpleExpressionProxy>(indexes);
    final Message msg = new Message(msgList);

    msg.addRecipient(n);
    ch.addMessages(msg);

    return null;
  }

  public Object visitConstant(final ConstantTreeNode t){
    data.add(t.getText());
    //add all event data
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t){
    count = 0;
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t){
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("initrun");
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      mEventDecls.add(event);
      atomic = true;
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
      return null;
  }

  public Object visitRun(final RunTreeNode t){
    final String proctypeName = t.getChild(0).getText();
    if(!occur.containsKey(proctypeName)){
      occur.put(proctypeName,1);
      //if(t.getParent().getParent() instanceof InitialTreeNode){
      //  final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+proctypeName);
      //  final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      //  mEventDecls.add(event);
      //  }
    }else{
      final int size = occur.get(proctypeName);
      occur.put(proctypeName, size+1);
    }

    return null;
  }

  public Hashtable<String, ChanInfo> getChan(){
    return chan;
  }

  public boolean getAtomic(){
    return atomic;
  }

  public Object visitVar(final VardefTreeNode t)
  {

    return null;
  }

  public Object visitName(final NameTreeNode t)
  {

    if(t.getParent() instanceof MsgTreeNode){
      labels.add(null);
    }
    if(t.getParent() instanceof ChannelTreeNode){
      channelMsg.add(t.getText());
    }
    if(t.getParent() instanceof TypeTreeNode){
      mGlobalVar.put(t.getText(),t.getParent().getText());
    }
    return null;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {

    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitType(final TypeTreeNode t)
  {
    //TODO
    if(t.getText().equals("byte")){
      lowerEnd.add(0);
      upperEnd.add(255);
    }
    if(t.getText().equals("mtype")){
     // rangeData.add
    }
    return null;
  }
  public ModuleProxyFactory getFactory()
  {

    return mFactory;
  }

  public Object visitCondition(final ConditionTreeNode t)
  {
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitDoStatement(final DoConditionTreeNode t)
  {
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitBreak(final BreakStatementTreeNode t)
  {

    return null;
  }

  public Object visitLabel(final LabelTreeNode t)
  {

    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitGoto(final GotoTreeNode t)
  {

    return null;
  }

  public Object visitSkip(final SkipTreeNode t)
  {

    return null;
  }

}
