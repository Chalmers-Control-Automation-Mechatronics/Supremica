package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

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
import net.sourceforge.waters.external.promela.ast.PromelaTree;
import net.sourceforge.waters.external.promela.ast.RunTreeNode;
import net.sourceforge.waters.external.promela.ast.SemicolonTreeNode;
import net.sourceforge.waters.external.promela.ast.TypeTreeNode;
import net.sourceforge.waters.external.promela.ast.VardefTreeNode;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


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
  final Hashtable<String,Collection<IdentifierProxy>> procEvent = new Hashtable<String,Collection<IdentifierProxy>>();
  //This is the output event table, for each proctype
  private final Collection<EventDeclProxy> mEventDecls = new ArrayList<EventDeclProxy>();

  //########################################################################
  //# Invocation
  public EventCollectingVisitor(final ModuleProxyFactory factory){
    mFactory = factory;
  }

  public void collectEvents(final PromelaTree node)
  {
    node.acceptVisitor(this);
  }

  public Collection<EventDeclProxy> getEvents(){
    return mEventDecls;
  }
  public Hashtable<String,Collection<IdentifierProxy>> getChanEvent(){
    return procEvent;
  }
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
    final PromelaTree tr1 = (PromelaTree) t.getChild(1);
    final String name = t.getChild(0).getText();
    procEvent.put(name,new ArrayList<IdentifierProxy>());
    //chan.put(name,new ChanInfo());
    tr1.acceptVisitor(this);
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t){
    final int length = Integer.parseInt(t.getChild(0).getText());
    final int datalength = t.getChildCount()-2;
    final String name = t.getParent().getChild(0).getText();
    final ArrayList<String> type = new ArrayList<String>();
    for(int i=1;i<t.getChildCount();i++){
      type.add(t.getChild(i).getText());
    }

    chan.put(name,new ChanInfo(name, length, datalength,type));

    final String chanName = t.getParent().getChild(0).getText();
    lowerEnd = new ArrayList<Integer>();
    upperEnd = new ArrayList<Integer>();
    for(int i =1;i<t.getChildCount();i++){
      ((PromelaTree) t.getChild(i)).acceptVisitor(this);
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
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
    mEventDecls.add(event);
    return ident;
  }

  public Object visitExchange(final ExchangeTreeNode t){

    //send statement
    if(t.getText().equals("!")|| t.getText().equals("!!")){
      data =new ArrayList<String>();
      labels = new ArrayList<String>();

      final String chanName = t.getChild(0).getText();
      labels.add(chanName);

      //chan.get(t.getParent().getChild(0).getText()).incSendnumber();

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }

      //store channel name and relevant data into hashtable
      chan.get(chanName).storeMsg(data);

      final String ename = labels.get(0);
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
        //create indexedIdentifier, and store it for receive statement
        final IndexedIdentifierProxy indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
        //ChanInfo info = chan.get(chanName);
        //info.addEvent(indexEvent);
        Collection<IdentifierProxy> temp = procEvent.get(chanName);
        if(temp==null){
          temp = new ArrayList<IdentifierProxy>();
        }
        temp.add(indexEvent);
        procEvent.put(chanName,temp);

        final Collection<IdentifierProxy> temp2 = new ArrayList<IdentifierProxy>();
        temp2.add(indexEvent);

        return indexEvent;
      }
      //receive statement
      if(t.getText().equals("?")|| t.getText().equals("??")){

        //do nothing

        return null;
      }

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


      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      mEventDecls.add(event);



      atomic = true;

    return null;
  }
  public Object visitRun(final RunTreeNode t){
    final String proctypeName = t.getChild(0).getText();
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+proctypeName.toUpperCase());
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
    mEventDecls.add(event);

    return null;
  }

  public Hashtable<String, ChanInfo> getChan(){
    return chan;

  }


  public void output(){
    for(final Map.Entry<String,ChanInfo> entry: chan.entrySet()){
      System.out.println(entry.getKey()+" ->>"+ entry.getValue().getValue());
    }
  }
  public boolean getAtomic(){
    return atomic;
  }

  public Object visitVar(final VardefTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitName(final NameTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitSemicolon(final SemicolonTreeNode t)
  {
    // TODO Auto-generated method stub
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
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
  public ModuleProxyFactory getFactory()
  {
    // TODO Auto-generated method stub
    return mFactory;
  }
}
