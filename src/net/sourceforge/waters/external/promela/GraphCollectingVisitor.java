package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
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
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
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
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;
import org.antlr.runtime.tree.CommonTree;
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

  //This is a reference to the symbol table that is used for storing variables in
  private SymbolTable mSymbolTable;

  //This is where the arguments to a send or receive statement are stored
  private List<ChannelData> mMessageArguments = new LinkedList<GraphCollectingVisitor.ChannelData>();

  Map<PromelaNode,PromelaEdge> mSourceOfBreakNode = new HashMap<PromelaNode,PromelaEdge>();

  private final Map<String,PromelaNode> mGotoNode = new HashMap<String,PromelaNode>();

  Map<String,PromelaNode> mLabelEnd = new HashMap<String,PromelaNode>();

  private Collection<EventDeclProxy> mEvents = new ArrayList<EventDeclProxy>();

  public GraphCollectingVisitor(final EventCollectingVisitor v, final SymbolTable table)
  {
    mVisitor = v;
    mFactory = v.getFactory();
    mSymbolTable = table;
  }

  public PromelaGraph collectGraphs(final PromelaTree node)
  {
    return (PromelaGraph) node.acceptVisitor(this);
  }

  public Collection<EventDeclProxy> getEvent()
  {
    return mEvents;
  }

  public Collection<Proxy> getComponents()
  {
    return mComponents;
  }

  public Object visitModule(final ModuleTreeNode t)
  {
    mEvents = new ArrayList<EventDeclProxy>(mVisitor.getEvents());

    //Create the variables
    for(final String keyName : mSymbolTable.getLocalKeys())
    {
      if(mSymbolTable.get(keyName) instanceof VardefTreeNode
        && ((VardefTreeNode) mSymbolTable.get(keyName)).isVisible())
      {
        final VardefTreeNode node = (VardefTreeNode)mSymbolTable.get(keyName);

        final VariableComponentProxy var = createVariable(keyName, node, t);
        if(var != null)
          mComponents.add(var);
      }
    }

    //Now, visit the child nodes
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t)
  {
    //Retrieve the symbol table for the current position
    mSymbolTable = t.getSymbolTable();

    mUnWinding = false;
    final String procName = t.getText();
    final int occurance = mVisitor.getOccur().get(procName);

    //visit child 1
    final PromelaTree statement = (PromelaTree) t.getChild(1);
    PromelaGraph g = collectGraphs(statement);

    final IdentifierProxy ident;
    if(mVisitor.getAtomic())
    {
      ident = mFactory.createSimpleIdentifierProxy("initrun");
    }
    else
    {
      if(occurance==1)
      {
        ident = mFactory.createSimpleIdentifierProxy("run_"+procName);
      }
      else
      {
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

    if(occurance == 1)
    {
      //Create the variables
      for(final String keyName : mSymbolTable.getLocalKeys())
      {
        if(mSymbolTable.get(keyName) instanceof VardefTreeNode
          && ((VardefTreeNode) mSymbolTable.get(keyName)).isVisible())
        {
          final VardefTreeNode node = (VardefTreeNode)mSymbolTable.get(keyName);

          final VariableComponentProxy var = createVariable(keyName, node, t);
          if(var != null)
            mComponents.add(var);
        }
      }

      final IdentifierProxy name = mFactory.createSimpleIdentifierProxy("proctype_"+procName);
      component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);
      mComponents.add(component);
    }
    else
    {
      final Collection<SimpleIdentifierProxy> procs = new ArrayList<SimpleIdentifierProxy>();
      for(int i=0; i<occurance; i++)
      {
        final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(procName+"_"+i);
        procs.add(id);
      }
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);

      final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
      indexes.add(id);

      final IndexedIdentifierProxy name = mFactory.createIndexedIdentifierProxy("proctype_"+procName,indexes);

      component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);

      final EnumSetExpressionProxy en = mFactory.createEnumSetExpressionProxy(procs);
      final Collection<ComponentProxy> c = new ArrayList<ComponentProxy>();

      //Create the variables
      for(final String keyName : mSymbolTable.getLocalKeys())
      {
        if(mSymbolTable.get(keyName) instanceof VardefTreeNode
          && ((VardefTreeNode) mSymbolTable.get(keyName)).isVisible())
        {
          final VardefTreeNode node = (VardefTreeNode)mSymbolTable.get(keyName);

          final VariableComponentProxy var = createIndexedVariable(keyName, node, t);
          if(var != null)
            c.add(var);
        }
      }

      c.add(component);
      final ForeachProxy f = mFactory.createForeachProxy("procid", en,null,c);
      mComponents.add(f);
    }

    //Are leaving the current context, so move up to the previous level on the symbol table
    mSymbolTable = mSymbolTable.getParentTable();

    return null;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    for(int i=0; i<t.getChildCount(); i++)
    {
      ((PromelaTree)t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitVar(final VardefTreeNode t)
  {
    //Nothing to do here, as it was done by the event collecting visitor class
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    final String name = t.getChild(0).getText();
    chanNames.add(name);
    final ChanInfo ch = mVisitor.getChan().get(name);
    final ModuleProxyCloner cloner = mFactory.getCloner();

    if(ch.getChanLength()==1)
    {
      visit_ExchangeChannel(name, ch);
    }
    else if(ch.getChanLength()>1)
    {
      visit_BufferedChannel(name, ch, cloner);
    }

    for(int i=0;i<t.getChildCount();i++)
    {
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  private void visit_ExchangeChannel(final String name, final ChanInfo ch)
  {
    final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
    final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
    final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
    final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(accepting);
    final List<SimpleIdentifierProxy> list = Collections.singletonList(id);
    final PlainEventListProxy eventList = mFactory.createPlainEventListProxy(list);
    final NodeProxy start =
      mFactory.createSimpleNodeProxy("empty", eventList, null,
                                     true, null, null, null);

    mNodes.add(start);
    loop1:
      for(final Message msg : ch.getOutput())
      {
        if(msg.getMsg().contains(null) || msg.getSenders().size()==0)
          continue loop1;

        String ename = "s";
        for(final SimpleExpressionProxy s: msg.getMsg())
        {
          ename += "_"+s;
        }
        final NodeProxy node = mFactory.createSimpleNodeProxy(ename);
        mNodes.add(node);

        final Collection<IdentifierProxy> labelBlock = new ArrayList<IdentifierProxy>();
        for(int i=0;i<msg.getSenders().size();i++)
        {
          final String procname = msg.getSenders().get(i);

          for(int a=0;a<mVisitor.getOccur().get(procname);a++)
          {
            final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();

            //Not sure why case '== 1' needs to be treated separately
            if(mVisitor.getOccur().get(procname) == 1 && ch.isSenderPresent()
              || mVisitor.getOccur().get(procname) != 1)
            {
              final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(procname+"_"+a);
              indexes.add(ident);
            }

            for(int b=0;b<ch.getType().size();b++)
            {
              final SimpleExpressionProxy s = msg.getMsg().get(b);

              if(ch.getType().get(b).equals("mtype"))
              {
                final IdentifierProxy c1 = mFactory.createSimpleIdentifierProxy(s.toString());
                indexes.add(c1);
              }
              else
              {
                final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                indexes.add(c1);
              }
            }
            final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("send_"+name, indexes);
            labelBlock.add(ident);
          }
        }

        final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);
        if(!labelBlock.isEmpty())
        {
          final EdgeProxy sendEdge = mFactory.createEdgeProxy(start, node, label, null, null, null, null);
          mEdges.add(sendEdge);
        }
        final Collection<IdentifierProxy> labelBlock2 = new ArrayList<IdentifierProxy>();

        for(int i=0;i<msg.getRecipients().size();i++)
        {
          if(!msg.hasSenders()) break;
          final String procname = msg.getRecipients().get(i);

          for(int a=0;a<mVisitor.getOccur().get(procname);a++)
          {
            final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();

            if(mVisitor.getOccur().get(procname)==1 && ch.isRecipientPresent() || mVisitor.getOccur().get(procname)!=1)
            {
              final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(procname+"_"+a);
              indexes.add(ident);
            }
            for(int b=0;b<ch.getType().size();b++)
            {
              final SimpleExpressionProxy s = msg.getMsg().get(b);

              if(ch.getType().get(b).equals("mtype"))
              {
                final IdentifierProxy c1 = mFactory.createSimpleIdentifierProxy(s.toString());
                indexes.add(c1);
              }
              else
              {
                final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                indexes.add(c1);
              }

            }
            final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("recv_"+name, indexes);
            labelBlock2.add(ident);
          }
        }
        final LabelBlockProxy label2 = mFactory.createLabelBlockProxy(labelBlock2, null);
        if(!labelBlock2.isEmpty())
        {
          final EdgeProxy recvEdge = mFactory.createEdgeProxy(node, start, label2, null, null, null, null);
          mEdges.add(recvEdge);
        }
      }

    final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("channel_"+name);
    final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
    mComponents.add(component);
  }

  private void visit_BufferedChannel(final String name, final ChanInfo ch, final ModuleProxyCloner cloner)
  {
    for(int l=0;l<=ch.getChanLength()-1;l++)
    {
      final Collection<NodeProxy> mNodes = new ArrayList<NodeProxy>();
      final Collection<EdgeProxy> mEdges = new ArrayList<EdgeProxy>();
      final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(accepting);
      final List<SimpleIdentifierProxy> list = Collections.singletonList(id);
      final PlainEventListProxy eventList = mFactory.createPlainEventListProxy(list);
      final NodeProxy start =
        mFactory.createSimpleNodeProxy("empty", eventList, null,
                                       true, null, null, null);
      mNodes.add(start);

      loop1:
        for(final Message msg : ch.getOutput())
        {
          if(msg.getMsg().contains(null) || msg.getSenders().size()==0)
          {
            continue loop1;
          }
          String ename = "s";
          for(final SimpleExpressionProxy s: msg.getMsg())
          {
            ename += "_"+s;
          }
          final NodeProxy node = mFactory.createSimpleNodeProxy(ename);
          mNodes.add(node);

          final Collection<IdentifierProxy> labelBlock = new ArrayList<IdentifierProxy>();
          final Collection<IdentifierProxy> labelBlock2 = new ArrayList<IdentifierProxy>();
          final Collection<IdentifierProxy> labelBlock3 = new ArrayList<IdentifierProxy>();

          for(int i=0;i<msg.getSenders().size();i++)
          {
            final String procname = msg.getSenders().get(i);

            final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
            if(ch.isSenderPresent())
            {
              final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(procname+"_"+0);
              indexes.add(ident);
            }
            for(int a=0;a<ch.getType().size();a++)
            {
              final SimpleExpressionProxy s = msg.getMsg().get(a);
              if(ch.getType().get(a).equals("mtype"))
              {
                final IdentifierProxy c1 = mFactory.createSimpleIdentifierProxy(s.toString());
                indexes.add(c1);
              }
              else
              {
                final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                indexes.add(c1);
              }
            }

            IndexedIdentifierProxy ident = null;
            final Collection<SimpleExpressionProxy> copyofindex = cloner.getClonedList(indexes);
            final IntConstantProxy c0 = mFactory.createIntConstantProxy(l);
            copyofindex.add(c0);
            ident = mFactory.createIndexedIdentifierProxy("send_"+name, copyofindex);

            if(l>0)
            {
              for(int x=0;x<l;x++)
              {
                final Collection<SimpleExpressionProxy> copy= cloner.getClonedList(indexes);
                final IntConstantProxy c2 = mFactory.createIntConstantProxy(x);
                copy.add(c2);
                final IndexedIdentifierProxy ident3 = mFactory.createIndexedIdentifierProxy("send_"+name, copy);
                labelBlock3.add(ident3);
              }
            }
            labelBlock.add(ident);
            if(l!=ch.getChanLength()-1)
            {
              if(ch.getChanLength()>2)
              {
                final IntConstantProxy c2 = (IntConstantProxy) cloner.getClone(c0);
                indexes.add(c2);
                final IndexedIdentifierProxy ident2 = mFactory.createIndexedIdentifierProxy("rppl_"+name, cloner.getClonedList(indexes));
                labelBlock.add(ident2);
              }
              else
              {
                final IndexedIdentifierProxy ident2 = mFactory.createIndexedIdentifierProxy("rppl_"+name,cloner.getClonedList(indexes));
                labelBlock.add(ident2);
              }
            }
          }

          final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null);
          final LabelBlockProxy label3 = mFactory.createLabelBlockProxy(labelBlock3,null);
          if(!labelBlock.isEmpty())
          {
            final EdgeProxy sendEdge = mFactory.createEdgeProxy(start, node, label, null, null, null, null);
            mEdges.add(sendEdge);
          }
          if(!labelBlock3.isEmpty())
          {
            EdgeProxy e = null;
            for(final EdgeProxy ed: mEdges)
            {
              if(ed.getSource()==start && ed.getTarget()==start)
              {
                e = ed;
              }
            }
            if(e!=null)
            {
              final Collection<Proxy> copy= cloner.getClonedList(e.getLabelBlock().getEventList());
              copy.addAll(labelBlock3);
              final LabelBlockProxy label4 = mFactory.createLabelBlockProxy(cloner.getClonedList(copy),null);
              mEdges.remove(e);
              final EdgeProxy sendingEdge = mFactory.createEdgeProxy(start, start, label4, null, null, null, null);
              mEdges.add(sendingEdge);
            }
            else
            {
              final EdgeProxy sendingEdge = mFactory.createEdgeProxy(start, start, label3, null, null, null, null);
              mEdges.add(sendingEdge);
            }
          }

          /*
           * Receiving
           */

          for(int i=0;i<msg.getRecipients().size();i++)
          {
            if(!msg.hasSenders())
              break;

            final String procname = msg.getRecipients().get(i);
            if(mVisitor.getOccur().get(procname)==1)
            {
              final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
              if(ch.isRecipientPresent())
              {
                final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(procname+"_"+0);
                indexes.add(ident);
              }
              for(int a=0;a<ch.getType().size();a++)
              {
                final SimpleExpressionProxy s = msg.getMsg().get(a);
                if(ch.getType().get(a).equals("byte"))
                {
                  final IntConstantProxy c1 = mFactory.createIntConstantProxy(Integer.parseInt(s.toString()));
                  indexes.add(c1);
                }
                else if(ch.getType().get(a).equals("mtype"))
                {
                  final IdentifierProxy c1 = mFactory.createSimpleIdentifierProxy(s.toString());
                  indexes.add(c1);
                }
              }
              IndexedIdentifierProxy ident = null;
              if(l==0)
              {
                ident = mFactory.createIndexedIdentifierProxy("recv_"+name, indexes);
              }
              else
              {
                if(ch.getChanLength()>2)
                {
                  final IntConstantProxy c2 = mFactory.createIntConstantProxy(l-1);
                  indexes.add(c2);
                  ident = mFactory.createIndexedIdentifierProxy("rppl_"+name, indexes);
                }
                else
                {
                  ident = mFactory.createIndexedIdentifierProxy("rppl_"+name, indexes);
                }
              }
              labelBlock2.add(ident);
            }
          }
          final LabelBlockProxy label2 = mFactory.createLabelBlockProxy(labelBlock2, null);
          if(!labelBlock2.isEmpty())
          {
            final EdgeProxy recvEdge = mFactory.createEdgeProxy(node, start, label2, null, null, null, null);
            mEdges.add(recvEdge);
          }
        }
      final GraphProxy graph = mFactory.createGraphProxy(true, null, mNodes, mEdges);
      final IntConstantProxy ch_index = mFactory.createIntConstantProxy(l);
      final Collection<SimpleExpressionProxy> ChIndex = new ArrayList<SimpleExpressionProxy>();
      ChIndex.add(ch_index);
      final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("channel_"+name, ChIndex);
      final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
      mComponents.add(component);
    }
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
    for(final PromelaNode n: result.getNodes())
    {
      if(n.isGoto())
      {
        removeNode.add(n);
        final String name = n.getGotoLabel();
        final PromelaNode newNode = mGotoNode.get(name);
        for(final PromelaEdge e: result.getEdges())
        {
          if(e.getTarget()==n)
          {
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

  public Object visitSend(final SendTreeNode t)
  {
    final String chanName = t.getChild(0).getText();
    final ChanInfo ch = mVisitor.getChan().get(chanName);
    final PromelaChannel channel = mVisitor.getChannels().get(chanName);

    mMessageArguments = new ArrayList<ChannelData>();

    labels = new ArrayList<String>();
    labels.add(chanName);
    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final String name = tree.getText();

    for(int i = 0; i <t.getChildCount();i++)
    {
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    final ArrayList<Message> receivableMessages = new ArrayList<Message>();
    for(final Message m : ch.getOutput())
    {
      if(m.hasRecipients() && !(m.getMsg().contains(null)))
      {
       receivableMessages.add(m);
      }
    }

    final PromelaGraph graph = new PromelaGraph();
    createSendMessages(graph, new ArrayList<SimpleExpressionProxy>(), new ArrayList<SimpleExpressionProxy>(), mMessageArguments, 0, channel, name, receivableMessages);

    return graph;
  }

  /**
   * A recursive method to create the messages for the given send statement.
   * @param graph The graph to put the send edges on.
   * @param currentMessage The current message being created.
   * For the initial call to this method, pass in a new array list.
   * @param guards The guard block restricting this send statement.
   * For the initial call to this method, pass in a new array list.
   * @param iterList The list of message arguments to iterate over.
   * @param iterIndex The current index that is being processed in the iterList.
   * Use zero for the initial call to this method.
   * @param receivableMessages The messages that can be received on the channel.
   */
  private void createSendMessages(final PromelaGraph graph, final ArrayList<SimpleExpressionProxy> currentMessage, final ArrayList<SimpleExpressionProxy> guards, final List<ChannelData> iterList, final int iterIndex, final PromelaChannel channel, final String name, final ArrayList<Message> receivableMessages)
  {
    if(iterIndex == iterList.size())
    {
      final ModuleProxyCloner cloner = mFactory.getCloner();
      //Have finished preparing one message, create it
      createSendGraphEdge(graph, new Message(cloner.getClonedList(currentMessage)), guards, channel, name, receivableMessages);

      //Now, return back to creating more messages, or back to visitSend()
      return;
    }
    else
    {
      final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

      //Still working on a message, process the next item in the receive statement
      final ChannelData data = iterList.get(iterIndex);
      switch(data.getDataType())
      {
      case CONSTANT:
      {
        //This is a constant
        //Add its value into the message
        final SimpleExpressionProxy value = data.getPossibleValues();
        currentMessage.add(value);

        //Make a recursive call to this method, for the next item
        createSendMessages(graph, currentMessage, guards, iterList, iterIndex+1, channel, name, receivableMessages);

        //Now, this item is finished with, remove it from the message
        currentMessage.remove(currentMessage.size() - 1);
        break;
      }
      case SHOWN_VARIABLE:
      {
        //This is a shown variable
        final SimpleExpressionProxy values = data.getPossibleValues();

        if(values instanceof BinaryExpressionProxy)
        {
          //This is an integer type variable
          //Iterate over the possible values of the variable
          final BinaryExpressionProxy range = (BinaryExpressionProxy) values;
          for(int i = ((IntConstantProxy)range.getLeft()).getValue(); i < ((IntConstantProxy)range.getRight()).getValue(); i++)
          {
            data.setValue(i);
            //Add the current value to the message
            currentMessage.add(data.getValue());

            if(channel.getLength() > 0 || channel.isReceived(iterIndex, data.getValue()))
            {

              final SimpleExpressionProxy varName = data.getIdentifier().clone();
              final SimpleExpressionProxy varValue = data.getValue().clone();
              //Add this value into the guards
              guards.add(mFactory.createBinaryExpressionProxy(optable.getEqualsOperator(), varName, varValue));

              //Make a recursive call for this value of the expression
              createSendMessages(graph, currentMessage, guards, iterList, iterIndex+1, channel, name, receivableMessages);

              //Now, remove the value from the guards
              guards.remove(guards.size() - 1);
            }

            currentMessage.remove(currentMessage.size()-1);
          }
        }
        else
        {
          //This is a mtype variable
          //Iterate over the possible values
          final EnumSetExpressionProxy range = (EnumSetExpressionProxy) values;
          for(final SimpleIdentifierProxy item : range.getItems())
          {
            data.setValue(item.getName());
            //Add the current value to the message
            currentMessage.add(data.getValue());

            //Check if this value is sendable on the channel
            //If it is not sendable, then don't do the recursive call
            if(channel.getLength() > 0 || channel.isReceived(iterIndex, data.getValue()))
            {
              //Can send this value on the channel
              final SimpleExpressionProxy varName = data.getIdentifier().clone();
              final SimpleExpressionProxy varValue = data.getValue().clone();
              //Add this value into the guards
              guards.add(mFactory.createBinaryExpressionProxy(optable.getEqualsOperator(), varName, varValue));

              //Make a recursive call for this value of the expression
              createSendMessages(graph, currentMessage, guards, iterList, iterIndex+1, channel, name, receivableMessages);

              //Now, remove the value from the guards
              guards.remove(guards.size() - 1);
            }

            //Remove this value from the message
            currentMessage.remove(currentMessage.size()-1);
          }
        }
        break;
      }
      default:
      {
        //This is an error, as cannot send hidden variables
        System.err.println("ERROR in method GraphCollectingVisitor.createSendMessages()");
        System.err.println("Attempt to send hidden variable on a channel");
        //TODO Probably use an exception instead at some point
      }
      }
    }
  }

  /**
   * A method to create the graph edge for sending a given message.
   * @param graph The graph to add the send statement on to.
   * @param message The message to add onto the graph
   * @param guards The guard block restricting this send edge
   * @param ch The channel that is sending the message
   * @param name The name of the channel
   * @param receivableMessages The messages that are capable of being received on the channel
   */
  private void createSendGraphEdge(final PromelaGraph graph, final Message message, final ArrayList<SimpleExpressionProxy> guards, final PromelaChannel channel, final String name, final ArrayList<Message> receivableMessages)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    final int channelLength = channel.getLength();

    if(channel.isReceivable(message, mFactory))
    {
      //This message can be received, so add an edge for it
      final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();

      final Collection<SimpleExpressionProxy> data = cloner.getClonedList(message.getMsg());
      final Collection<SimpleIdentifierProxy> senders = new ArrayList<SimpleIdentifierProxy>();
      final Collection<SimpleIdentifierProxy> recvs = new ArrayList<SimpleIdentifierProxy>();

      final Message match = channel.getMatch(message);
      message.combine(match);

      //TODO Add in senders and receivers that send variables as well to be in the message

      getSendersAndReceiversOfSendMessage(name, channel, senders, recvs, message);

      if(senders.size()>=1 && recvs.size()>=1)
        send_MultipleSendersAndReceivers(cloner, comparator, channelLength, data, events, senders, recvs);
      else if(senders.size()==0 && recvs.size()==0)
        send_NoSenders_NoReceivers(cloner, comparator, channelLength, data, events);
      else if(senders.size()==0 && recvs.size()>0)
        send_MultipleReceiversNoSenders(cloner, comparator, channelLength, data, events, recvs);
      else if(senders.size()>=1 && recvs.size()==0)
        send_MultipleSendersNoReceivers(cloner, comparator, channelLength, data, events, senders);

      //Create an edge on the graph labelled with the events
      final PromelaNode startNode = graph.getNodes().get(0);
      final PromelaNode finishNode = graph.getNodes().get(1);

      final List<SimpleExpressionProxy> edgeGuards;
      if(!guards.isEmpty())
        edgeGuards = cloner.getClonedList(guards);
      else
        edgeGuards = null;
      final List<BinaryExpressionProxy> edgeActions = null;

      graph.addEdge(startNode, finishNode, events, mFactory, edgeGuards, edgeActions);
    }
    else
    {
      //This message cannot be received, so nothing to do
      return;
    }
  }

  private void getSendersAndReceiversOfSendMessage(final String name, final PromelaChannel c, final Collection<SimpleIdentifierProxy> senders, final Collection<SimpleIdentifierProxy> recvs, final Message m)
  {
    if(c.hasMultipleSenders())
    {
      if(mVisitor.getOccur().get(name)>1)
      {
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy("procid");
        senders.add(ident);
      }
      else if(mVisitor.getOccur().get(name)==1)
      {
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(name+"_"+0);
        senders.add(ident);
      }
    }

    if(c.hasMultipleReceivers())
    {
      Collections.sort(m.getRecipients());
      for(final String n: m.getRecipients())
      {
        for(int i=0;i<mVisitor.getOccur().get(n);i++)
        {
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(n+"_"+i);
          recvs.add(ident);
        }
      }
    }
  }

  private void send_MultipleSendersAndReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final Collection<SimpleExpressionProxy> data, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> senders, final Collection<SimpleIdentifierProxy> recvs)
  {
    for(final SimpleIdentifierProxy s1: senders)
    {
      for(final SimpleIdentifierProxy s2: recvs)
      {
        final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
        in.add(s1);
        if(length==0)
        {
          in.add(s2);
        }
        in.addAll(cloner.getClonedList(data));
        IndexedIdentifierProxy indexEvent;
        String ename = labels.get(0);
        if(length ==0)
        {
          ename = "exch_"+ename;
          indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
        }
        else
        {
          ename = "send_"+ename;
          indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
        }

        if(!events.contains(indexEvent))
        {
          events.add(indexEvent);
        }
      }
    }
  }

  private void send_NoSenders_NoReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final Collection<SimpleExpressionProxy> data, final List<IdentifierProxy> events)
  {
    final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
    in.addAll(cloner.getClonedList(data));
    String ename = labels.get(0);
    IndexedIdentifierProxy indexEvent;
    if(length ==0)
    {
      ename = "exch_"+ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
    }
    else
    {
      ename = "send_"+ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
    }

    if(!events.contains(indexEvent))
    {
      events.add(indexEvent);
    }
  }

  private void send_MultipleReceiversNoSenders(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final Collection<SimpleExpressionProxy> data, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> recvs)
  {
    for(final SimpleIdentifierProxy s2: recvs)
    {
      final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
      if(length==0)
      {
        in.add(s2);
      }
      in.addAll(cloner.getClonedList(data));
      IndexedIdentifierProxy indexEvent;
      String ename = labels.get(0);
      if(length ==0)
      {
        ename = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }
      else
      {
        ename = "send_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }

      if(!events.contains(indexEvent))
      {
        events.add(indexEvent);
      }
    }
  }

  private void send_MultipleSendersNoReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final Collection<SimpleExpressionProxy> data, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> senders)
  {
    for(final SimpleIdentifierProxy s1: senders)
    {
      final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
      in.add(s1);
      in.addAll(cloner.getClonedList(data));
      IndexedIdentifierProxy indexEvent;
      String ename = labels.get(0);
      if(length ==0)
      {
        ename = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }
      else
      {
        ename = "send_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }

      if(!events.contains(indexEvent))
      {
        events.add(indexEvent);
      }
    }
  }

  public Object visitReceive(final ReceiveTreeNode t)
  {
    runedOnce++;

    //receive statement
    final String chanName = t.getChild(0).getText();

    final PromelaChannel channel = mVisitor.getChannels().get(chanName);

    mMessageArguments = new ArrayList<ChannelData>();

    labels = new ArrayList<String>();
    labels.add(chanName);
    Tree tree = t;
    while (!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final String name = tree.getText();

    for(int i = 0; i <t.getChildCount();i++)
    {
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }

    //Create the initial message list
    final ArrayList<Message> msgList = new ArrayList<Message>();
    createReceiveMessageList(msgList, new ArrayList<SimpleExpressionProxy>(), mMessageArguments, 0, channel);

    //Now, create the graph, and create the messages corresponding to that graph
    final PromelaGraph graph = new PromelaGraph();
    createReceiveMessages(graph, msgList, new ArrayList<BinaryExpressionProxy>(), mMessageArguments, 0, channel, name);

    return graph;
  }

  /**
   * A method to create the initial message list for a receive statement.
   * @param msgList The list to store the message into.
   * @param currentMessage The current message.
   * For the first call to this method, pass in a new ArrayList.
   * @param iterList The list of channel data items to create the message list from.
   * @param iterIndex The current index into the 'iterList'.
   * Use zero when first calling this method.
   * @param sendableMessages The messages that are sendable on the channel
   */
  private void createReceiveMessageList(final Collection<Message> msgList, final ArrayList<SimpleExpressionProxy> currentMessage, final List<ChannelData> iterList, final int iterIndex, final PromelaChannel channel)
  {
    if(iterIndex == iterList.size())
    {
      final ModuleProxyCloner cloner = mFactory.getCloner();
      //We have completed a message, add it into the msgList
      msgList.add(new Message(cloner.getClonedList(currentMessage)));
    }
    else
    {
      final ChannelData data = iterList.get(iterIndex);
      switch (data.getDataType())
      {
        case CONSTANT:
        {
          //This value is a constant
          final SimpleExpressionProxy value = data.getPossibleValues();
          currentMessage.add(value);

          createReceiveMessageList(msgList, currentMessage, iterList, iterIndex+1, channel);
          currentMessage.remove(currentMessage.size() - 1);
          break;
        }
        case HIDDEN_VARIABLE:
        {
          //This value is a hidden variable
          final SimpleExpressionProxy values = data.getPossibleValues();
          if(values instanceof BinaryExpressionProxy)
          {
            final BinaryExpressionProxy range = (BinaryExpressionProxy) values;

            for(int i = ((IntConstantProxy)range.getLeft()).getValue(); i < ((IntConstantProxy)range.getRight()).getValue(); i++)
            {
              data.setValue(i);
              currentMessage.add(data.getValue());

              //Check if this value can be sent on the channel
              //Make the recursive call if the value is sendable
              if(channel.isSent(iterIndex, data.getValue()))
              {
                createReceiveMessageList(msgList, currentMessage, iterList, iterIndex+1, channel);
              }
              currentMessage.remove(currentMessage.size() - 1);
            }
          }
          else
          {
            final EnumSetExpressionProxy range = (EnumSetExpressionProxy) values;

            for(final SimpleIdentifierProxy val : range.getItems())
            {
              data.setValue(val.getName());
              currentMessage.add(data.getValue());

              //Check if this value can be sent on the channel
              //Make the recursive call if the value is sendable
              if(channel.isSent(iterIndex, data.getValue()))
              {
                createReceiveMessageList(msgList, currentMessage, iterList, iterIndex+1, channel);
              }
              currentMessage.remove(currentMessage.size() - 1);
            }
          }

          break;
        }
        case SHOWN_VARIABLE:
        {
          //This value is a shown variable
          currentMessage.add(null);

          createReceiveMessageList(msgList, currentMessage, iterList, iterIndex+1, channel);
          break;
        }
      }
    }
  }

  /**
   * The second pass in creating the messages for a receive statement.
   * This expands on the createMessageList method by adding the shown variables
   * and creating the message edges, before adding them onto the nodes.
   * @param graph The graph to add the edges onto
   * @param msgList The initial message list assigned to by the createMessageList method
   * @param actions The action block for the variable assignments.  Use an empty array list for the initial call to this method.
   * @param iter An iterator over all of the channel arguments
   * @param ch The channel information of the channel the messages are being sent on
   * @param name The name of the process receiving the messages
   */
  private void createReceiveMessages(final PromelaGraph graph, final ArrayList<Message> msgList, final ArrayList<BinaryExpressionProxy> actions, final List<ChannelData> iterList, final int iterIndex, final PromelaChannel channel, final String processName)
  {
    if(iterIndex == iterList.size())
    {
      //Have got the messages for one graph edge, create that edge
      createReceiveGraphEdge(graph, msgList, actions, channel, processName);
    }
    else
    {
      final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

      final ChannelData data = iterList.get(iterIndex);
      if(data.getDataType() == ChannelDataType.SHOWN_VARIABLE)
      {
        //This value is a shown variable
        /* Iterate over all of the possible values for this variable,
         * and make one recursive call for each value.
         * One edge will be added per value of the shown variable
         */

        //Get the index of the shown variable inside the messages
        //This is the first null value
        int shownIndex = -1;
        final Message firstMessage = msgList.get(0);
        for(int i = 0; i < firstMessage.getMsg().size(); i++)
        {
          final SimpleExpressionProxy val = firstMessage.getMsg().get(i);
          if(val == null)
          {
            shownIndex = i;
            break;
          }
        }

        final SimpleExpressionProxy values = data.getPossibleValues();
        if(values instanceof BinaryExpressionProxy)
        {
          final BinaryExpressionProxy range = (BinaryExpressionProxy) values;
          for(int i = ((IntConstantProxy)range.getLeft()).getValue(); i < ((IntConstantProxy)range.getRight()).getValue(); i++)
          {
            data.setValue(i);
            for(final Message m : msgList)
            {
              m.getMsg().set(shownIndex, data.getValue());
            }

            //Check if this value is sendable on the channel
            //If it is not sendable, then don't do the recursive call
            if(channel.isSent(iterIndex, data.getValue()))
            {
              final SimpleExpressionProxy varName = data.getIdentifier().clone();
              final SimpleExpressionProxy varValue = data.getValue().clone();
              //Add this value into the action collection
              actions.add(mFactory.createBinaryExpressionProxy(optable.getAssignmentOperator(), varName, varValue));

              //Make a recursive call for this value of the expression
              createReceiveMessages(graph, msgList, actions, iterList, iterIndex+1, channel, processName);

              //Now, remove the value for the action collection
              actions.remove(actions.size() - 1);
            }
          }
          //Set the value back to null
          //This is necessary if we return into a loop, as this code will run again
          for(final Message m : msgList)
          {
            m.getMsg().set(shownIndex, null);
          }
        }
        else
        {
          final EnumSetExpressionProxy range = (EnumSetExpressionProxy) values;
          for(final SimpleIdentifierProxy item : range.getItems())
          {
            data.setValue(item.getName());
            for(final Message m : msgList)
            {
              m.getMsg().set(shownIndex, data.getValue());
            }

            //Check if this value is sendable on the channel
            //If it is not sendable, then don't do the recursive call
            if(channel.isSent(iterIndex, data.getValue()))
            {
              final SimpleExpressionProxy varName = data.getIdentifier().clone();
              final SimpleExpressionProxy varValue = data.getValue().clone();
              //Add this value into the action collection
              actions.add(mFactory.createBinaryExpressionProxy(optable.getAssignmentOperator(), varName, varValue));

              //Make a recursive call for this value of the expression
              createReceiveMessages(graph, msgList, actions, iterList, iterIndex+1, channel, processName);

              //Now, remove the value for the action collection
              actions.remove(actions.size() - 1);
            }
            //Set the value back to null
            //This is necessary if we return into a loop, as this code will run again
            for(final Message m : msgList)
            {
              m.getMsg().set(shownIndex, null);
            }
          }
        }
      }
      else
      {
        //Not a shown variable, nothing to process, so make recursive call
        createReceiveMessages(graph, msgList, actions, iterList, iterIndex+1, channel, processName);
      }
    }
  }

  /**
   * A method to create the graph edge for the given messages being received on the given channel
   * @param graph The graph to create the edge on
   * @param msgList The list of messages that can be received
   * @param ch The channel the messages are being sent and received on
   * @param name The name of the process receiving the messages
   */
  private void createReceiveGraphEdge(final PromelaGraph graph, final ArrayList<Message> msgList, final ArrayList<BinaryExpressionProxy> actions, final PromelaChannel channel, final String processName)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final Comparator<SimpleExpressionProxy> comparator = new ExpressionComparator(optable);
    final int channelLength = channel.getLength();

    //Get all of the messages that can be sent on the channel, and can be received
    //i.e. discard messages that can't be received on this end from the messages that can be sent on the other end
    final ArrayList<Message> receivingMessages = new ArrayList<Message>();

    for(final Message m : msgList)
    {
      if(channel.isReceivable(m, mFactory))
        receivingMessages.add(channel.getMatch(m));
    }

    //Create the events list that matches the messages
    final List<IdentifierProxy> events = new ArrayList<IdentifierProxy>();

    for(final Message m : receivingMessages)
    {
      final Collection<SimpleExpressionProxy> data = cloner.getClonedList(m.getMsg());
      final Collection<SimpleIdentifierProxy> senders = new ArrayList<SimpleIdentifierProxy>();
      final Collection<SimpleIdentifierProxy> recvs = new ArrayList<SimpleIdentifierProxy>();
      getSendersAndReceiversOfReceiveMessage(channel, m, senders, recvs, processName);

      if(senders.size()>=1 && recvs.size()>=1)
        receive_MultipleSendersAndReceivers(cloner, comparator, channelLength, events, senders, recvs, data);
      else if(senders.size()==0 && recvs.size()==0)
        receive_NoSendersAndReceivers(cloner, comparator, channelLength, events, data);
      else if(senders.size()==0 && recvs.size()>0)
        receive_NoSendersMultipleReceivers(cloner, comparator, channelLength, events, recvs, data);
      else if(senders.size()>0 && recvs.size()==0)
        receive_MultipleSendersNoReceivers(cloner, comparator, channelLength, events, senders, data);
    }

    //Create an edge on the graph labelled with the events
    final PromelaNode startNode = graph.getNodes().get(0);
    final PromelaNode finishNode = graph.getNodes().get(1);

    final List<SimpleExpressionProxy> edgeGuards = null;
    //Use the actions block, or null if there is no actions
    final List<BinaryExpressionProxy> edgeActions;
    if(!(actions.isEmpty()))
      edgeActions = cloner.getClonedList(actions);
    else
      edgeActions = null;

    graph.addEdge(startNode, finishNode, events, mFactory, edgeGuards, edgeActions);
  }

  /**
   * A method to get the senders and receivers of a message, for messages received
   * @param ch The channel information of the channel the messages are being send and received on.
   * @param name The name of the process receiving the messages.
   * @param m The message to create the senders and receivers for.
   * @param senders The collection to store the senders in.
   * @param recvs The collection to store the receivers in.
   */
  private void getSendersAndReceiversOfReceiveMessage(final PromelaChannel channel, final Message m, final Collection<SimpleIdentifierProxy> senders, final Collection<SimpleIdentifierProxy> recvs, final String processName)
  {
    if(channel.hasMultipleSenders())
    {
      for(final String s: m.getSenders())
      {
        for(int i=0; i<mVisitor.getOccur().get(s); i++)
        {
          final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(s+"_"+i);
          senders.add(ident);
        }
      }
    }
    if(channel.hasMultipleReceivers())
    {
      Collections.sort(m.getRecipients());

      if(mVisitor.getOccur().get(processName)>1)
      {
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy("procid");
        recvs.add(ident);
      }
      else if(mVisitor.getOccur().get(processName)==1)
      {
        final SimpleIdentifierProxy ident = mFactory.createSimpleIdentifierProxy(processName+"_"+0);
        recvs.add(ident);
      }
    }
  }

  private void receive_MultipleSendersNoReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> senders, final Collection<SimpleExpressionProxy> data)
  {
    for(final SimpleIdentifierProxy s1: senders)
    {
      final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
      if(length==0)
      {
        in.add(s1);
      }
      in.addAll(cloner.getClonedList(data));
      IndexedIdentifierProxy indexEvent;
      String ename = labels.get(0);
      if(length ==0)
      {
        ename = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }
      else
      {
        ename = "recv_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }
      boolean test = false;
      for(final IdentifierProxy id: events)
      {
        if(comparator.compare(id, indexEvent)==0)
        {
          test = true;
          break;
        }
      }
      if(!test)
      {
        events.add(indexEvent);
      }
    }
  }

  private void receive_NoSendersMultipleReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> recvs, final Collection<SimpleExpressionProxy> data)
  {
    for(final SimpleIdentifierProxy s2: recvs)
    {
      final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
      in.add(s2);
      in.addAll(cloner.getClonedList(data));
      IndexedIdentifierProxy indexEvent;
      String ename = labels.get(0);
      if(length ==0)
      {
        ename = "exch_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }
      else
      {
        ename = "recv_"+ename;
        indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
      }

      boolean test = false;
      for(final IdentifierProxy id: events)
      {
        if(comparator.compare(id, indexEvent)==0)
        {
          test = true;
          break;
        }
      }
      if(!test)
      {
        events.add(indexEvent);
      }
    }
  }

  private void receive_NoSendersAndReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final List<IdentifierProxy> events, final Collection<SimpleExpressionProxy> data)
  {
    final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
    in.addAll(cloner.getClonedList(data));
    String ename = labels.get(0);
    IndexedIdentifierProxy indexEvent;
    if(length ==0)
    {
      ename = "exch_"+ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
    }
    else
    {
      ename = "recv_"+ename;
      indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
    }
    boolean test = false;
    for(final IdentifierProxy id: events)
    {
      if(comparator.compare(id, indexEvent)==0)
      {
        test = true;
        break;
      }
    }
    if(!test)
    {
      events.add(indexEvent);
    }
  }

  private void receive_MultipleSendersAndReceivers(final ModuleProxyCloner cloner, final Comparator<SimpleExpressionProxy> comparator, final int length, final List<IdentifierProxy> events, final Collection<SimpleIdentifierProxy> senders, final Collection<SimpleIdentifierProxy> recvs, final Collection<SimpleExpressionProxy> data)
  {
    for(final SimpleIdentifierProxy s1: senders)
    {
      for(final SimpleIdentifierProxy s2: recvs)
      {
        final Collection<SimpleExpressionProxy> in = new ArrayList<SimpleExpressionProxy>();
        if(length==0)
        {
          in.add(s1);
        }
        in.add(s2);

        if(!data.isEmpty())
        {
          in.addAll(cloner.getClonedList(data));
        }
        IndexedIdentifierProxy indexEvent;
        String ename = labels.get(0);
        if(length ==0)
        {
          ename = "exch_"+ename;
          indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
        }
        else
        {
          ename = "recv_"+ename;
          indexEvent = mFactory.createIndexedIdentifierProxy(ename,cloner.getClonedList(in));
        }

        boolean test = false;
        for(final IdentifierProxy id: events)
        {
          if(comparator.compare(id, indexEvent)==0)
          {
            test = true;
            break;
          }
        }
        if(!test)
        {
          events.add(indexEvent);
        }
      }
    }
  }

  public Object visitConstant(final ConstantTreeNode t)
  {
    final int value = t.getValue();
    final ChannelData constant = new ChannelData(ChannelDataType.CONSTANT, mFactory.createSimpleIdentifierProxy("" + value), mFactory.createIntConstantProxy(value));

    mMessageArguments.add(constant);

    /*if(t.getParent() instanceof MsgTreeNode)
    {
      final int value = t.getValue();
      final ChannelData constant = new ChannelData(ChannelDataType.CONSTANT, mFactory.createSimpleIdentifierProxy("" + value), mFactory.createIntConstantProxy(value));

      mMessageArguments.add(constant);
    }
    else if(t.getParent() instanceof SendTreeNode)
    {

    }*/

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

  public Collection<String> distinct(final Collection<String> t,final Collection<String> output)
  {
    final ArrayList<String> temp = new ArrayList<String>(t);
    for(int i=0;i<t.size();i++)
    {
      final String compare = temp.get(i);
      temp.set(i, null);
      if(temp.contains(compare))
      {
        output.add(compare);
      }
    }
    return output;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("initrun");
    final PromelaGraph initGraph = new PromelaGraph(ident);
    return initGraph;
  }

  public Object visitRun(final RunTreeNode t)
  {
    final String name = t.getChild(0).getText();
    PromelaGraph graph=null;
    if(!mIsInit)
    {

    }
    else
    {
      final int occur1 = mVisitor.getOccur().get(name);
      final int occur2 = copyOfOccur.get(name);
      if(occur1>1)
      {
        if(occur1-occur2 < occur1)
        {
          final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
          final IdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+(occur1-occur2));
          indexes.add(id);
          //final IndexedIdentifierProxy ident = mFactory.createIndexedIdentifierProxy("run_"+name,indexes);
          final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
          graph = new PromelaGraph(ident);
        }
        copyOfOccur.put(name,occur2-1);
      }
      else
      {
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+name);
        graph = new PromelaGraph(ident);
      }
    }
    return graph;
  }

  //return graph of init events
  public Object visitName(final NameTreeNode t)
  {
    if(t.getParent() instanceof RunTreeNode)
    {
      procNames.add(t.getText());
    }
    else if(t.getParent() instanceof MsgTreeNode)
    {
      mMessageArguments.add(createChannelData(t));

      if(mSymbolTable.containsKey(t.getText()))
      {
        labels.add(t.getText());
      }
    }
    else if(t.getParent() instanceof SendTreeNode)
    {
      if(mSymbolTable.containsKey(t.getText()))
      {
        //This is a variable, so add it into the message arguments
        mMessageArguments.add(createChannelData(t));
      }

      if(!mVisitor.getChanMsg().contains(t.getText()))
      {
        if(mSymbolTable.containsKey(t.getText()))
        {
          labels.add(t.getText());
        }
      }
    }
    else if(t.getParent() instanceof ReceiveTreeNode)
    {
      //This is the name of the channel
    }

    return null;
  }

  /**
   * A method to create the channel data from a given name
   * @param t The name tree node containing the name of the item
   * @return A channel data structure containing a representation of the given item.
   */
  private ChannelData createChannelData(final NameTreeNode t)
  {
    final Tree symbol = mSymbolTable.get(t.getText());
    ChannelDataType type;
    String variableScope = "";

    //Find out the scope of the variable
    Tree tree = t;
    while(!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final ProctypeTreeNode proctypeTree = (ProctypeTreeNode) tree;
    if(proctypeTree.getSymbolTable().getLocalKeys().contains(t.getText()))
    {
      //This is a local variable to this proctype
      variableScope = proctypeTree.getText() + "_";
    }

    final String variableName = "var_" + variableScope + t.getText();
    final IdentifierProxy identifier;

    if(mVisitor.getOccur().get(proctypeTree.getText()) > 1)//if is [procid] indexed variable
    {
      final SimpleIdentifierProxy procid = mFactory.createSimpleIdentifierProxy("procid");
      final List<SimpleIdentifierProxy> list = Collections.singletonList(procid);
      identifier = mFactory.createIndexedIdentifierProxy(variableName, list);
    }
    else
    {
      identifier = mFactory.createSimpleIdentifierProxy(variableName);
    }

    final SimpleExpressionProxy range;

    if(symbol instanceof VardefTreeNode)
    {
      final boolean visible = ((VardefTreeNode) symbol).isVisible();
      if(visible)
        type = ChannelDataType.SHOWN_VARIABLE;
      else
        type = ChannelDataType.HIDDEN_VARIABLE;

      range = ((VardefTreeNode) symbol).getVariableType().getRangeExpression(mFactory);
    }
    else
    {
      type = ChannelDataType.CONSTANT;

      range = mFactory.createSimpleIdentifierProxy(t.getText());
    }

    final ChannelData channelData = new ChannelData(type, identifier, range);
    return channelData;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    PromelaGraph result = null;
    for(int i=0;i<t.getChildCount();i++)
    {
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
    for (int i = 0; i < t.getChildCount(); i++) {
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      result =
        PromelaGraph.combineComposition(result, step, mUnWinding, mFactory);
    }
    return result;
  }

  public Object visitDoStatement(final DoConditionTreeNode t)
  {
    final boolean unwinding = mUnWinding;
    counter =counter+1;
    Tree tree = t;
    while(!(tree instanceof ProctypeTreeNode))
    {
      tree = tree.getParent();
    }
    final PromelaGraph result;

    final PromelaNode endNode = new PromelaNode(PromelaNode.EndType.END);
    final List<PromelaGraph> branches = new ArrayList<PromelaGraph>();
    for(int i=0; i<t.getChildCount(); i++)
    {
      mUnWinding = true;
      final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(i));
      branches.add(step);
    }
    result =
      PromelaGraph.doCombineComposition2(branches, unwinding, mFactory);
    mLabelEnd.put(""+counter,endNode);

    return result;
  }

  public Object visitBreak(final BreakStatementTreeNode t)
  {
    final ModuleProxyCloner cloner = mFactory.getCloner();
    if(!mUnWinding)
    {
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.BREAK);
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges);
      return result;
    }
    else
    {
      //create step_* transition
      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode))
      {
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
      if(mVisitor.getOccur().get(name)==1)
      {
        ident = mFactory.createSimpleIdentifierProxy("step_"+name);
        ident2 = mFactory.createSimpleIdentifierProxy("step_"+name);
      }
      else
      {
        final Collection<SimpleIdentifierProxy> list2 = new ArrayList<SimpleIdentifierProxy>();
        for(int i=0;i<mVisitor.getOccur().get(name);i++)
        {
          final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy(name+"_"+i);

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
      if(procs.size()==0)
      {
        event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      }
      else
      {
        event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, indexes,null, null);
      }

      if(!mEvents.contains(event))
      {
        mEvents.add(event);
      }

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
    final PromelaGraph step = collectGraphs((PromelaTree) t.getChild(0));
    final PromelaGraph result = PromelaGraph.sequentialComposition(null, step, mUnWinding, mFactory);
    final String name = t.getText();
    final PromelaNode start = result.getStart();
    mGotoNode.put(name, start);
    if(name.startsWith("end"))
    {
      start.setAccepting(true);
    }
    return result;
  }

  public Object visitGoto(final GotoTreeNode t)
  {
    if(!mUnWinding)
    {
      final String labelName = t.getText();
      final PromelaNode node = new PromelaNode(labelName); //creating goto label
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes, cEdges);
      return result;
    }
    else
    {
      Tree tree = t;
      while (!(tree instanceof ProctypeTreeNode))
      {
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
      if (!mEvents.contains(event))
      {
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
    if(!mUnWinding)
    {
      final PromelaNode node = new PromelaNode(PromelaNode.EndType.END);
      final List<PromelaNode> cNodes = new ArrayList<PromelaNode>();
      cNodes.add(node);
      final List<PromelaEdge> cEdges = new ArrayList<PromelaEdge>();
      final PromelaGraph result = new PromelaGraph(cNodes,cEdges,node);
      return result;
    }
    else
    {
      Tree tree = t;
      while(!(tree instanceof ProctypeTreeNode))
      {
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
      if(!mEvents.contains(event))
      {
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

  public boolean checkEnd(final Tree t)
  {
    if(t instanceof LabelTreeNode)
    {
      final String temp = t.getText();
      if(temp.length() >= 3)
      {
        final String end = temp.substring(0, 3);
        if(end.toLowerCase().equals("end"))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * A method to create a variable
   * @param variableName The name of the variable
   * @param variableDefinition The VardefTreeNode containing the definition for the variable
   * @param processType The ProctypeTreeNode that contains the process this variable is being created for.
   * @return The variable that has been created
   */
  private VariableComponentProxy createVariable(final String variableName, final VardefTreeNode variableDefinition, final CommonTree processType)
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

    //TODO make sure that the name doesn't clash with other names
    SimpleIdentifierProxy name = null;
    if(processType instanceof ModuleTreeNode)
    {
      //This is a global variable
      name = mFactory.createSimpleIdentifierProxy("var_" + variableName);
    }
    else
    {
      //This is a local variable
      name = mFactory.createSimpleIdentifierProxy("var_" + processType.getText()  + "_" + variableName);
    }

    SimpleExpressionProxy type;
    SimpleExpressionProxy init;
    SimpleExpressionProxy initialValue = variableDefinition.getVariableType().getInitialValue(mFactory);

    if(variableDefinition.getVariableType().equals("mtype"))
    {
      //Get the module tree node that contains the mtype names
      Tree t = processType;
      while(!(t instanceof ModuleTreeNode))
      {
        t = t.getParent();
      }

      /*      String initialVal = ((PromelaMType)variableDefinition.getVariableType()).getMTypes().get(0);
      if(false)//TODO get the initial value
      {
        initialVal = "";
      }

      //Specify the initial value
      initialValue = mFactory.createSimpleIdentifierProxy(initialVal);
       */
    }
    else
    {
      //Get the initial value
      int initialVal = 0;//The default initial value
      for(int i=0; i<variableDefinition.getChildCount(); i++)
      {
        final NameTreeNode n = (NameTreeNode)variableDefinition.getChild(i);
        if(n.getText().equals(variableName))
        {
          for(int j=0;j<n.getChildCount(); j++)
          {
            if(n.getChild(j) instanceof ConstantTreeNode)
            {
              final ConstantTreeNode c = (ConstantTreeNode) n.getChild(j);
              initialVal = new Integer(c.getText());

              break;
            }
          }
        }
      }
      //Specify the initial value
      if(initialVal != 0)
        initialValue = mFactory.createIntConstantProxy(initialVal);
    }

    //Create the variable range expression
    type = variableDefinition.getVariableType().getRangeExpression(mFactory);

    //Create the initial value expression
    final BinaryOperator equals = optable.getEqualsOperator();
    final SimpleExpressionProxy name_2 = mFactory.createSimpleIdentifierProxy(name.getName());

    init = mFactory.createBinaryExpressionProxy(equals, name_2, initialValue);

    //Create the variable
    final VariableComponentProxy var = mFactory.createVariableComponentProxy(name, type, true, init);
    return var;
  }

  /**
   * A method to create an indexed variable.
   * The variables will be indexed by "procid"
   * @param variableName The name of the variable
   * @param variableDefinition The VardefTreeNode containing the definition for the variable
   * @param processType The ProctypeTreeNode that contains the process this variable is being created for.
   * @return The variable that has been created
   */
  private VariableComponentProxy createIndexedVariable(final String variableName, final VardefTreeNode variableDefinition, final CommonTree processType)
  {
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

    //TODO make sure that the name doesn't clash with other names

    final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>();
    final SimpleIdentifierProxy id = mFactory.createSimpleIdentifierProxy("procid");
    indexes.add(id);

    IndexedIdentifierProxy name = null;
    if(processType == null)
    {
      //This is a global variable
      name = mFactory.createIndexedIdentifierProxy("var_" + variableName, indexes);
    }
    else
    {
      //This is a local variable
      name = mFactory.createIndexedIdentifierProxy("var_" + processType.getText()  + "_" + variableName, indexes);
    }

    final SimpleExpressionProxy type = variableDefinition.getVariableType().getRangeExpression(mFactory);

    SimpleExpressionProxy init;
    SimpleExpressionProxy initialValue = variableDefinition.getVariableType().getInitialValue(mFactory);
    if(variableDefinition.getVariableType().equals("mtype"))
    {
      //Get the module tree node that contains the mtype names
      Tree t = processType;
      while(!(t instanceof ModuleTreeNode))
      {
        t = t.getParent();
      }

      //Get all of the possible values for the mtype
      final ArrayList<SimpleIdentifierProxy> values = new ArrayList<SimpleIdentifierProxy>();

      for(final String mtype : ((ModuleTreeNode)t).getMtypes())
      {
        values.add(mFactory.createSimpleIdentifierProxy(mtype));
      }

      /*type = mFactory.createEnumSetExpressionProxy(values);

      String initialVal = values.get(0).getName();//The default initial value
      if(false)//TODO get the initial value
      {
        initialVal = "";
      }

      //Specify the initial value
      initialValue = mFactory.createSimpleIdentifierProxy(initialVal);
       */
    }
    else
    {
      //Get the initial value
      int initialVal = 0;
      for(int i=0; i<variableDefinition.getChildCount(); i++)
      {
        final NameTreeNode n = (NameTreeNode)variableDefinition.getChild(i);
        if(n.getText().equals(variableName))
        {
          for(int j=0;j<n.getChildCount(); j++)
          {
            if(n.getChild(j) instanceof ConstantTreeNode)
            {
              final ConstantTreeNode c = (ConstantTreeNode) n.getChild(j);
              initialVal = new Integer(c.getText());

              break;
            }
          }
        }
      }

      if(initialVal != 0)
        initialValue = mFactory.createIntConstantProxy(initialVal);

    }

    //Create the initial value expression
    final BinaryOperator equals = optable.getEqualsOperator();
    final Collection<SimpleExpressionProxy> indexes_2 = new ArrayList<SimpleExpressionProxy>();
    final SimpleIdentifierProxy id_2 = mFactory.createSimpleIdentifierProxy("procid");
    indexes_2.add(id_2);
    final IndexedIdentifierProxy name_2 = mFactory.createIndexedIdentifierProxy(name.getName(), indexes_2);
    init = mFactory.createBinaryExpressionProxy(equals, name_2, initialValue);

    //Create the variable
    final VariableComponentProxy var = mFactory.createVariableComponentProxy(name, type, true, init);
    return var;
  }

  private enum ChannelDataType { CONSTANT, SHOWN_VARIABLE, HIDDEN_VARIABLE };

  /**
   * This class is used to store the type for the channel items
   * @author Ethan Duff
   */
  private class ChannelData
  {
    private final ChannelDataType mDataType;
    private final IdentifierProxy mIdentifier;//The identifier for this item
    private SimpleExpressionProxy mValue;//The current value for the member
    private final SimpleExpressionProxy mPossibleValues;//The possible values that this item can take

    /**
     * The constructor for this class
     * @param dataType The type for the channel member
     * @param identifier The identifier for this channel data item.
     * In particular, for variables this value should be the name of the variable
     * @param possibleValues The possible values that this item can take
     * @param factory The factory used to generate identifiers and expressions with
     */
    private ChannelData(final ChannelDataType dataType, final IdentifierProxy identifier, final SimpleExpressionProxy possibleValues)
    {
      mDataType = dataType;
      mPossibleValues = possibleValues;
      mIdentifier = identifier;
      mValue = null;
    }

    /**
     * A method to set the channel data value to be equal to the provided value
     * @param newValue The value that the channel data should take at this time
     */
    private void setValue(final String newValue)
    {
      mValue = mFactory.createSimpleIdentifierProxy(newValue);
    }

    private void setValue(final int newValue)
    {
      mValue = mFactory.createIntConstantProxy(newValue);
    }

    private SimpleExpressionProxy getValue()
    {
      return mValue;
    }

    private ChannelDataType getDataType()
    {
      return mDataType;
    }

    private SimpleExpressionProxy getPossibleValues()
    {
      return mPossibleValues;
    }

    private IdentifierProxy getIdentifier()
    {
      return mIdentifier;
    }
  }
}
