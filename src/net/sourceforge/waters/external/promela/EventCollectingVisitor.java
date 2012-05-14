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

  ArrayList<String> lowerEnd = new ArrayList<String>();
  ArrayList<String> upperEnd = new ArrayList<String>();
  ArrayList<String> rangeData = new ArrayList<String>();

  private SymbolTable mSymbolTable;//This is an accessor to the symbol table that is used for storing variables in
  int currentTableCount = 0;

  final Hashtable<String,THashSet<IdentifierProxy>> procEvent = new Hashtable<String,THashSet<IdentifierProxy>>();
  final Hashtable<String,LabelTreeNode> gotoLabel = new Hashtable<String,LabelTreeNode>();
  //This is the output event table, for each proctype
  private final Collection<EventDeclProxy> mEventDecls = new ArrayList<EventDeclProxy>();

  private final Hashtable<String,Integer> occur = new Hashtable<String,Integer>();

  Collection<SimpleExpressionProxy> mRanges = new ArrayList<SimpleExpressionProxy>();

  //List<Message> mOutput = new ArrayList<Message>();
  List<String> channelMsg = new ArrayList<String>();
  //Hashtable<String,String> mGlobalVar = new Hashtable<String,String>();
  Hashtable<String,List<Message>> mSendersMsg = new Hashtable<String,List<Message>>();

  //########################################################################
  //# Invocation
  public EventCollectingVisitor(final ModuleProxyFactory factory, final SymbolTable table)
  {
    mFactory = factory;
    mSymbolTable = table;
  }

  public void collectEvents(final PromelaTree node)
  {
    node.acceptVisitor(this);
  }

  public Collection<SimpleExpressionProxy> getRanges()
  {
    return mRanges;
  }

  public Hashtable<String,List<Message>> getSenderMsg()
  {
    return mSendersMsg;
  }

  public Collection<EventDeclProxy> getEvents()
  {
    return mEventDecls;
  }

  public Hashtable<String,THashSet<IdentifierProxy>> getChanEvent()
  {
    return procEvent;
  }

  public Hashtable<String,LabelTreeNode> getGotoLabel()
  {
    return gotoLabel;
  }

  public Hashtable<String,Integer> getOccur()
  {
    return occur;
  }

  public List<String> getChanMsg()
  {
    return channelMsg;
  }

  public void makeMsg()
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    //TODO I have no idea why this todo is here -- Was here before me. Ethan Duff
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    for(final Map.Entry<String,ChanInfo> chanIn: chan.entrySet())
    {
      final List<Message> msgs = new ArrayList<Message>(chanIn.getValue().getMessages());

      final Hashtable<Integer,List<SimpleExpressionProxy>> table = new Hashtable<Integer,List<SimpleExpressionProxy>>();
      for(int i=0;i<chanIn.getValue().getDataLength();i++)
      {
        table.put(i,new ArrayList<SimpleExpressionProxy>());
      }

      Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>();
      final List<Message> recipients = new ArrayList<Message>();
      final List<Message> senders = new ArrayList<Message>();

      for(final Message m: msgs)
      {
        if(m.hasSenders())
        {
          senders.add(m);
        }
        if(m.hasRecipients())
        {
          recipients.add(m);
        }
        chanIn.getValue().addMsgList(m);
      }
      final ArrayList<Message> templist = new ArrayList<Message>();
      for (final Message m : chanIn.getValue().getOutput())
      {
        final ArrayList<SimpleExpressionProxy> labels = new ArrayList<SimpleExpressionProxy>();
        if(recipients.contains(m))
        {
          for(final SimpleExpressionProxy s: m.getMsg())
          {
            if(s==null)
            {
              break;
            }
            labels.add(s);
          }
          for(final Message m2: senders)
          {
            boolean isSender = false;
            if(labels.size()==0)
            {
              isSender=true;
            }
            else
            {
              for(int i=0;i<labels.size();i++)
              {
                if(comparator.compare(labels.get(i),m2.getMsg().get(i))==0)
                {
                  isSender = true;
                }
                else
                {
                  isSender = false;
                  break;
                }
              }
            }
            if(isSender)
            {
              for(final String s: m2.getSenders())
              {
                if(recipients.contains(m))
                {
                  m.addSender(s);
                }
              }
              for(final String s: m.getRecipients())
              {
                m2.addRecipient(s);
              }
            }

            if(!chanIn.getValue().getOutput().contains(m2))
            {
              chanIn.getValue().addMsgList(m2);
            }
          }

          if(!chanIn.getValue().getOutput().contains(m))
          {
            chanIn.getValue().addMsgList(m);
          }
        }
      }

      for(final Message msg: chanIn.getValue().getOutput())
      {
        if(msg.getMsg().size()==0)
        {
          templist.add(msg);
        }
      }
      chanIn.getValue().getOutput().removeAll(templist);

      final ArrayList<Message> tempstore = new ArrayList<Message>();
      for(final Message msg: chanIn.getValue().getOutput())
      {
        if(msg.getMsg().contains(null))
        {
          tempstore.add(msg);
        }
      }

      for(final Message m1: tempstore)
      {
        for(final Message m2: chanIn.getValue().getOutput())
        {
          if(m2.getSenders().size()==m1.getSenders().size())
          {
            boolean test = false;
            for(final String s: m2.getSenders())
            {
              if(m1.getSenders().contains(s))
              {
                test = true;
              }
              else
              {
                test = false;
                break;
              }
            }
            if(test)
            {
              for(final String s: m1.getRecipients())
              {
                if(!m2.getRecipients().contains(s))
                {
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
      for(final Message m: chanIn.getValue().getOutput())
      {
        for(final String s: m.getSenders())
        {
          if(sendRange.size()==0)
          {
            sendRange.add(s);
          }
          else
          {
            if(!sendRange.contains(s))
            {
              sendRange.add(s);
            }
          }
        }
        for(final String s: m.getRecipients())
        {
          if(recRange.size()==0)
          {
              recRange.add(s);
          }
          else
          {
            if(!recRange.contains(s))
            {
              recRange.add(s);
            }
          }
        }
      }
      Collections.sort(sendRange);
      Collections.sort(recRange);
      final List<Message> cloneOutput = new ArrayList<Message>(chanIn.getValue().getOutput());
      cloneOutput.removeAll(tempstore);

      final ChanInfo c = chanIn.getValue();
      final int lengthOfChan = c.getChanLength();
      final String chanName = chanIn.getKey();
      for(final Message m: cloneOutput)
      {
        if(!c.isSenderPresent())
        {
          if(m.getSenders().size()>1)
          {
            c.setSenders(true);
          }
          else if(m.getSenders().size()==1)
          {
            final int occurrences = occur.get(m.getSenders().get(0));
            c.setSenders(occurrences>1);
          }
        }

        if(!c.isRecipientPresent())
        {
          if(m.getRecipients().size()>1)
          {
            c.setRecipients(true);
          }
          else if(m.getRecipients().size()==1)
          {
            final int occurrences = occur.get(m.getRecipients().get(0));
            c.setRecipients(occurrences>1);
          }
        }
      }
      for(final Message m: cloneOutput)
      {
        if(m.getSenders().size()>1)
        {
          final Collection<String> tempList = new ArrayList<String>();
          for (final String s : sendRange)
          {
            for(int i=0;i<occur.get(s);i++)
            {
              final String temp = s+"_"+i;
              if(!c.getSenders().contains(temp))
              {
                tempList.add(temp);
              }
            }
          }
          c.addSenders(tempList);
        }
        else if (m.getSenders().size() == 1)
        {
          final int occurrences = occur.get(m.getSenders().get(0));
          if (c.isSenderPresent())
          {
            final Collection<String> tempList = new ArrayList<String>();
            for(int i=0;i<occurrences;i++)
            {
              final String temp = m.getSenders().get(0)+"_"+i;
              if(!c.getSenders().contains(temp))
              {
                tempList.add(temp);
              }
            }
            c.addSenders(tempList);
          }
        }

        if(m.getRecipients().size() > 1)
        {
          final Collection<String> tempList = new ArrayList<String>();
          for(final String s : recRange)
          {
            final String temp = s + "_0";
            for(int i = 0; i < occur.get(s); i++)
            {
              if(!c.getRecipients().contains(temp))
              {
                tempList.add(temp);
              }
            }
          }
          c.addRecipients(tempList);
        }
        else if(m.getRecipients().size() == 1)
        {
          if(c.isRecipientPresent())
          {
            final Collection<String> tempList = new ArrayList<String>();
            for(int i = 0; i < occur.get(recRange.get(0)); i++)
            {
              final String temp = recRange.get(0) + "_" + i;
              if(!c.getRecipients().contains(temp))
              {
                tempList.add(temp);
              }
            }
            c.addRecipients(tempList);
          }
        }
      }
      final Collection<SimpleExpressionProxy> specialSend = new ArrayList<SimpleExpressionProxy>();
      final Collection<SimpleExpressionProxy> specialRec = new ArrayList<SimpleExpressionProxy>();
      if(c.isSenderPresent())
      {
        if(c.getSenders().size()==1)
        {
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(c.getSenders().get(0));
          ranges.add(ident);
          specialSend.add((SimpleExpressionProxy) cloner.getClone(ident));
        }
        else if(c.getSenders().size()>1)
        {
          final Collection<SimpleIdentifierProxy> tempList = new ArrayList<SimpleIdentifierProxy>();
          for(final String s: c.getSenders())
          {
            final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s);
            tempList.add(ident);
          }
          final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(tempList);
          ranges.add(en);
          specialSend.add((SimpleExpressionProxy) cloner.getClone(en));
        }
      }

      if(c.isRecipientPresent())
      {
        if(c.getRecipients().size()==1)
        {
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(c.getRecipients().get(0));
          ranges.add(ident);
          specialRec.add((SimpleExpressionProxy) cloner.getClone(ident));
        }
        else if(c.getRecipients().size()>1)
        {
          final Collection<SimpleIdentifierProxy> tempList = new ArrayList<SimpleIdentifierProxy>();
          for(final String s: c.getRecipients())
          {
            final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s);
            tempList.add(ident);
          }
          final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(tempList);
          ranges.add(en);
          specialRec.add((SimpleExpressionProxy) cloner.getClone(en));
        }
      }

      loop1:
      for(final Message m: msgs)
      {
        for(final SimpleExpressionProxy s: m.getMsg())
        {
          if(s==null)
          {
            continue loop1;
          }
        }
        if(m.hasRecipients() && m.hasSenders())
        {
          for(int i=0;i<m.getMsg().size();i++)
          {
            boolean test = false;
            for(final SimpleExpressionProxy s: table.get(i))
            {
              if(comparator.compare(s, m.getMsg().get(i))==0)
              {
                test = true;
                break;
              }
            }
            if(!test)
            {
              table.get(i).add(m.getMsg().get(i));
            }
          }
        }
      }
      List<SimpleExpressionProxy> dataRange;
      if(chanIn.getValue().getChanLength()==0)
      {
        dataRange = new ArrayList<SimpleExpressionProxy>();

        for(int i=0;i<table.size();i++)
        {
          if(chanIn.getValue().getType().get(i).equals("mtype"))
          {
            final EnumSetExpressionProxy en = createChannelArgumentEnum(cloner, table, i,chanIn.getValue().getType());
            dataRange.add(en);
          }
          else
          {
            Collections.sort(table.get(i),comparator);
            final BinaryOperator op = optable.getRangeOperator();
            final BinaryExpressionProxy range
            = mFactory.createBinaryExpressionProxy(op, (SimpleExpressionProxy) cloner.getClone(table.get(i).get(0)),
                                                   (SimpleExpressionProxy) cloner.getClone(table.get(i).get(table.get(i).size()-1)));
            dataRange.add(range);
          }
        }
        ranges.addAll(dataRange);
      }
      else
      {
        dataRange = new ArrayList<SimpleExpressionProxy>();
        for(int i=0;i<table.size();i++)
        {
          if(chanIn.getValue().getType().get(i).equals("mtype"))
          {
            final EnumSetExpressionProxy en = createChannelArgumentEnum(cloner, table, i,chanIn.getValue().getType());
            dataRange.add(en);
          }
          else
          {
            Collections.sort(table.get(i),comparator);
            final BinaryOperator op = optable.getRangeOperator();
            final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, (SimpleExpressionProxy) cloner.getClone(table.get(i).get(0)), (SimpleExpressionProxy) cloner.getClone(table.get(i).get(table.get(i).size()-1)));
            dataRange.add(range);
          }
        }
        specialRec.addAll(cloner.getClonedList(dataRange));

        dataRange = new ArrayList<SimpleExpressionProxy>();
        for(int i=0;i<table.size();i++)
        {
          for(final Message m: chanIn.getValue().getOutput())
          {
            if(!m.hasRecipients())
            {
              table.get(i).add(m.getMsg().get(i));
            }
          }
          if(chanIn.getValue().getType().get(i).equals("mtype"))
          {
            final EnumSetExpressionProxy en =
              createChannelArgumentEnum(cloner, table, i,chanIn.getValue().getType());
            dataRange.add(en);
          }
          else
          {
            Collections.sort(table.get(i),comparator);
            final BinaryOperator op = optable.getRangeOperator();
            final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, (SimpleExpressionProxy) cloner.getClone(table.get(i).get(0)), (SimpleExpressionProxy) cloner.getClone(table.get(i).get(table.get(i).size()-1)));
            dataRange.add(range);
          }
        }
        specialSend.addAll(dataRange);
      }

      //now create event decls
      if(lengthOfChan==0)
      {
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("exch_"+chanName);
        final EventDeclProxy event =
          mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
        mEventDecls.add(event);
      }
      else if(lengthOfChan==1)
      {
        final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("send_"+chanName);
        final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("recv_"+chanName);
        final EventDeclProxy event1 =
          mFactory.createEventDeclProxy(ident1, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, specialSend, null, null);
        final EventDeclProxy event2 =
          mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, specialRec, null, null);
        mEventDecls.add(event1);
        mEventDecls.add(event2);
      }
      else
      {
        final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("send_"+chanName);
        final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("recv_"+chanName);
        final IdentifierProxy ident0 = mFactory.createSimpleIdentifierProxy("rppl_"+chanName);
        final IntConstantProxy c3 = mFactory.createIntConstantProxy(lengthOfChan-1);
        final IntConstantProxy c2 = mFactory.createIntConstantProxy(lengthOfChan-2);
        final IntConstantProxy c0 = mFactory.createIntConstantProxy(0);
        final BinaryOperator op = optable.getRangeOperator();
        final BinaryExpressionProxy b1 = mFactory.createBinaryExpressionProxy(op, c0, c2);
        final BinaryExpressionProxy b2 = mFactory.createBinaryExpressionProxy(op, (SimpleExpressionProxy)cloner.getClone(c0), c3);
        final Collection<SimpleExpressionProxy> copyOfSend = new ArrayList<SimpleExpressionProxy>();
        copyOfSend.addAll(cloner.getClonedList(specialSend));
        copyOfSend.add(b2);
        final Collection<SimpleExpressionProxy> copyOfRec = new ArrayList<SimpleExpressionProxy>();
        copyOfRec.addAll(cloner.getClonedList(specialRec));
        if(lengthOfChan>2)
        {
          copyOfRec.add(b1);
        }
        final EventDeclProxy event0 = mFactory.createEventDeclProxy(ident0,EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, copyOfRec, null, null);
        final EventDeclProxy event1 = mFactory.createEventDeclProxy(ident1, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, copyOfSend, null, null);
        final EventDeclProxy event2 = mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, specialRec, null, null);
        mEventDecls.add(event1);
        mEventDecls.add(event2);
        mEventDecls.add(event0);
      }
    }

    //create Run events
    if(!atomic)
    {
      for(final Map.Entry<String,Integer> s: occur.entrySet())
      {
        if(s.getValue()==1)
        {
          final IdentifierProxy id = mFactory.createSimpleIdentifierProxy("run_"+s.getKey());
          final EventDeclProxy event = mFactory.createEventDeclProxy(id, EventKind.CONTROLLABLE);
          mEventDecls.add(event);
        }
        else
        {
          final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
          final Collection<SimpleIdentifierProxy> enuSet = new ArrayList<SimpleIdentifierProxy>();
          for(int i=0;i<s.getValue();i++)
          {
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

  private EnumSetExpressionProxy createChannelArgumentEnum(final ModuleProxyCloner cloner,
                                                           final Hashtable<Integer,List<SimpleExpressionProxy>> table,
                                                           final int argIndex,
                                                           final List<String> typeList)
  {
    final List<SimpleExpressionProxy> rangeValues = table.get(argIndex);
    final int numValues = rangeValues.size();
    final List<SimpleIdentifierProxy> enumValues = new ArrayList<SimpleIdentifierProxy>(numValues);

    for (int i=0;i<rangeValues.size();i++)
    {
        final SimpleIdentifierProxy cloned = (SimpleIdentifierProxy) cloner.getClone(rangeValues.get(i));
        enumValues.add(cloned);
     }

    final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(enumValues);
    return en;
  }

  //########################################################################
  //# Interface net.sourceforge.waters.external.promela.PromelaVisitor
  public Object visitModule(final ModuleTreeNode t)
  {
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
    final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(accepting);
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.PROPOSITION);
    mEventDecls.add(event);
    for(int i=0; i<t.getChildCount(); i++)
    {
        ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t){
    //Have entered a new context, so add a new table for the context, and use that currently
    currentTableCount++;
    mSymbolTable = mSymbolTable.addNewSymbolTable("table" + currentTableCount);

    //Store a reference to the current symbol table inside the node t
    t.setSymbolTable(mSymbolTable);

    for(int i=0;i<t.getChildCount();i++)
    {
        final PromelaTree node = (PromelaTree)t.getChild(i);
        node.acceptVisitor(this);
    }

    //Are leaving the current context, so move back to the upper level of the table
    mSymbolTable = mSymbolTable.getParentTable();

    return null;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++)
    {
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++)
    {
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++)
    {
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t)
  {
    final int chanLength = Integer.parseInt(t.getChild(0).getText());
    final int datalength = t.getChildCount()-1;
    final String name = t.getParent().getChild(0).getText();
    final List<String> type = new ArrayList<String>();

    for(int i=1;i<t.getChildCount();i++)
    {
      type.add(t.getChild(i).getText());
    }
    chan.put(name,new ChanInfo(name, chanLength, datalength,type));

    return null;
  }

  public Object visitSend(final SendTreeNode t)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    data =new ArrayList<String>();
    labels = new ArrayList<String>();

    final String chanName = t.getChild(0).getText();
    labels.add(chanName);

    for(int i = 0; i <t.getChildCount();i++)
    {
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    final String ename = labels.get(0);
    final ChanInfo ch = chan.get(ename);
    final int length = ch.getChanLength();
    final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
    final Collection<SimpleExpressionProxy> indexes2 = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
    for(int y=0;y<ch.getType().size();y++)
    {
      if(ch.getType().get(y).equals("byte"))
      {
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y+1)));
        indexes.add(c);
        final IntConstantProxy c2 = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y+1)));
        indexes2.add(c2);
      }
      else if(ch.getType().get(y).equals("mtype"))
      {
        //TODO add mtype variable sending here
        if(mSymbolTable.get(labels.get(y+1)) instanceof NameTreeNode)
        {
          final IdentifierProxy c = mFactory.createSimpleIdentifierProxy(labels.get(y+1));
          indexes.add(c);
          indexes2.add((SimpleExpressionProxy) cloner.getClone(c));
        }
      }
    }
    final List<SimpleExpressionProxy> msgList = new ArrayList<SimpleExpressionProxy>(indexes);
    final Message msg = new Message(msgList);

    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final String n = tree.getText();
    if(mSendersMsg.containsKey(n))
    {
      final List<Message> l = mSendersMsg.get(n);
      l.add(msg);

      mSendersMsg.put(n, l);
    }
    else
    {
      final List<Message> l = new ArrayList<Message>();
      l.add(msg);
      mSendersMsg.put(n, l);
    }
    msg.addSender(n);
    ch.addMessages(msg);

    //create indexedIdentifier, and store it for receive statement
    IndexedIdentifierProxy indexEvent = null;
    if (length == 0)
    {
      final String name = "exch_" + ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(name, indexes);
    }
    else if (length == 1)
    {
      final String name = "send_" + ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(name, indexes);
    }

    THashSet<IdentifierProxy> temp = (THashSet<IdentifierProxy>) ch.getChannelData();
    if(temp==null)
    {
      temp = new THashSet<IdentifierProxy>();
    }
    return indexEvent;
  }

  public Object visitReceive(final ReceiveTreeNode t)
  {
    data =new ArrayList<String>();
    labels = new ArrayList<String>();

    final String chanName = t.getChild(0).getText();
    labels.add(chanName);

    for(int i = 0; i <t.getChildCount();i++)
    {
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    final String ename = labels.get(0);
    final ChanInfo ch = chan.get(ename);
    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final String n = tree.getText();

    final List<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);

    for(int y=0;y<ch.getType().size();y++)
    {
      if(ch.getType().get(y).equals("byte"))
      {
        if(mSymbolTable.containsKey(labels.get(y+1)))
        {
          indexes.add(null);
        }
        else
        {
          final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y+1)));
          indexes.add(c);
        }
      }
      else if(ch.getType().get(y).equals("mtype"))
      {
        if(mSymbolTable.containsKey(labels.get(y+1))
          && mSymbolTable.get(labels.get(y+1)) instanceof NameTreeNode)
        {
          final TypeTreeNode node = (TypeTreeNode)mSymbolTable.get(labels.get(y+1)).getParent();
          if(node.getText().equals("mtype"))
          {
            indexes.add(mFactory.createSimpleIdentifierProxy(labels.get(y+1)));
          }
          else
          {
            //I don't think it is possible to get here
            indexes.add(null);
          }
        }
        else if(mSymbolTable.containsKey(labels.get(y+1))
          && mSymbolTable.get(labels.get(y+1)) instanceof VardefTreeNode)
        {
          //TODO Add in for variable assignment to mtype?
          indexes.add(null);
        }
      }
    }
    final Message msg = new Message(indexes);
    msg.addRecipient(n);
    ch.addMessages(msg);

    return null;
  }

  public Object visitConstant(final ConstantTreeNode t)
  {
    data.add(t.getText());
    //add all event data
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t)
  {
    count = 0;
    for(int i=0;i<t.getChildCount();i++)
    {
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("initrun");
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
    mEventDecls.add(event);
    atomic = true;
    for(int i=0;i<t.getChildCount();i++)
    {
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitRun(final RunTreeNode t)
  {
    final String proctypeName = t.getChild(0).getText();
    if(!occur.containsKey(proctypeName))
    {
      occur.put(proctypeName,1);
    }
    else
    {
      final int size = occur.get(proctypeName);
      occur.put(proctypeName, size+1);
    }
    return null;
  }

  public Hashtable<String, ChanInfo> getChan()
  {
    return chan;
  }

  public boolean getAtomic()
  {
    return atomic;
  }

  public Object visitVar(final VardefTreeNode t)
  {
    for(int i = 0; i < t.getChildCount(); i++)
    {
      //These are all of the names for the variables that are being instantiated in this declaration
      //All of their types are specified by the typeTreeNode

      if(mSymbolTable.put(t.getChild(i).getText(), t) == false)
      {
        System.err.println("EventCollectingVisitor.visitVar()\n"
          + "Attempt to add variable to symbol table, but name was already taken");
      }
    }
    return null;
  }

  public Object visitName(final NameTreeNode t)
  {
    if(t.getParent() instanceof MsgTreeNode)
    {
        if(mSymbolTable.containsKey(t.getText()))
        {
          labels.add(t.getText());
        }
    }
    else if(t.getParent() instanceof ChannelTreeNode)
    {
      channelMsg.add(t.getText());
    }
    else if(t.getParent() instanceof TypeTreeNode)
    {
      Tree tree = t;
      while(!(tree instanceof ModuleTreeNode))
      {
        tree = tree.getParent();
      }
      ((ModuleTreeNode)tree).addMtype(t.getText());

      //This is a mtype definition, so add in the string as a reference to itself
      if(mSymbolTable.put(t.getText(), t) == false)
      {
        //Error, the name was already taken
        System.err.println("EventCollectingVisitor.visitName())\n"
        + "Attempted to add mtype definition to symbol table, but name was already taken");
      }
    }
    else if(t.getParent() instanceof SendTreeNode){
      if(!channelMsg.contains(t.getText())){
        if(mSymbolTable.containsKey(t.getText())){
          labels.add(t.getText());
        }
      }
    }
    else if(t.getParent() instanceof ReceiveTreeNode)
    {
      if(!channelMsg.contains(t.getText()))
      {
        if(mSymbolTable.containsKey(t.getText()))
        {
          labels.add(t.getText());
        }
      }
      else
      {
        return null;
      }
    }
    return null;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    if(t.getChildCount()>0)
    {
      for(int i=0;i<t.getChildCount();i++)
      {
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitType(final TypeTreeNode t)
  {
    //TODO: Add in integer and boolean
    if(t.getText().equals("byte"))
    {
      //flag = "byte";
      lowerEnd.add("0");
      upperEnd.add("255");
    }
    else if(t.getText().equals("mtype") && t.getParent() instanceof ModuleTreeNode)
    {
      if(t.getChildCount()>0)
      {
        for(int i=0;i<t.getChildCount();i++)
        {
          ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
        }
      }
    }
    return null;
  }

  public ModuleProxyFactory getFactory()
  {
    return mFactory;
  }

  public Object visitCondition(final ConditionTreeNode t)
  {
    if(t.getChildCount()>0)
    {
      for(int i=0;i<t.getChildCount();i++)
      {
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitDoStatement(final DoConditionTreeNode t)
  {
    if(t.getChildCount()>0)
    {
      for(int i=0;i<t.getChildCount();i++)
      {
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
    if(t.getChildCount()>0)
    {
      for(int i=0;i<t.getChildCount();i++)
      {
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