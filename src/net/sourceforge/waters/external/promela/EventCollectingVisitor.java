package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
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
import net.sourceforge.waters.external.promela.ast.PromelaTreeNode;
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
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


public class EventCollectingVisitor implements PromelaVisitor
{
  int count = 0;
  boolean atomic = false;
  ArrayList<String> data =new ArrayList<String>();
  ArrayList<String> labels = new ArrayList<String>();
  private final Hashtable<String, ChanInfo> chan = new Hashtable<String,ChanInfo>();
  private final Hashtable<String, ArrayList<List<String>>> component = new Hashtable<String,ArrayList<List<String>>>();
  ArrayList<List<String>> componentLabels = new ArrayList<List<String>>();
  private final Hashtable<String, ArrayList<List<String>>> eventData = new Hashtable<String,ArrayList<List<String>>>();

  private final ModuleProxyFactory mFactory = new ModuleElementFactory();
  private final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

  ArrayList<Integer> lowerEnd = new ArrayList<Integer>();
  ArrayList<Integer> upperEnd = new ArrayList<Integer>();
  ArrayList<Collection<IdentifierProxy>> eventStore = new ArrayList<Collection<IdentifierProxy>>();
  final Hashtable<String,Collection<IdentifierProxy>> procEvent = new Hashtable<String,Collection<IdentifierProxy>>();
  //This is the output event table, for each proctype
  private Hashtable<String,List<Collection<IdentifierProxy>>> outputEvent = new Hashtable<String,List<Collection<IdentifierProxy>>>();
  private final Collection<EventDeclProxy> mEventDecls = new ArrayList<EventDeclProxy>();
  //########################################################################
  //# Invocation
  public void collectEvents(final PromelaTreeNode node)
  {
    node.acceptVisitor(this);
  }
  public Hashtable<String,List<Collection<IdentifierProxy>>> getOutputEvent(){
    return outputEvent;
  }
  public Collection<EventDeclProxy> getEvents(){
    return mEventDecls;
  }
  //########################################################################
  //# Interface net.sourceforge.waters.external.promela.PromelaVisitor
  public Object visitModule(final ModuleTreeNode t)
  {
    //final String name = t.getText();
    for(int i=0;i<t.getChildCount();i++) {
        ((PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t){
    final String proctypeName = t.getText();
    eventStore = new ArrayList<Collection<IdentifierProxy>>();
    outputEvent = new Hashtable<String,List<Collection<IdentifierProxy>>>();
    outputEvent.put(proctypeName, eventStore);

    procEvent.put(proctypeName,new ArrayList<IdentifierProxy>());

    componentLabels = new ArrayList<List<String>>();
    if(!component.containsKey(proctypeName)){
      component.put(proctypeName,componentLabels);
    }
    for(int i=0;i<t.getChildCount();i++){
        final PromelaTreeNode node = (PromelaTreeNode)t.getChild(i);
        node.acceptVisitor(this);
    }
    return null;
  }

  public Object visitMsg(final MsgTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t){
    final PromelaTreeNode tr1 = (PromelaTreeNode) t.getChild(1);
    final String name = t.getChild(0).getText();

    chan.put(name,new ChanInfo());
    tr1.acceptVisitor(this);
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t){
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
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
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
    mEventDecls.add(event);
    return ident;
  }

  public Object visitExchange(final ExchangeTreeNode t){
    final String proctypeName =t.getParent().getParent().getParent().getText();

    //send statement
    if(t.getText().equals("!")|| t.getText().equals("!!")){
      data =new ArrayList<String>();
      labels = new ArrayList<String>();

      final String chanName = t.getChild(0).getText();
      labels.add(chanName);

      //chan.get(t.getParent().getChild(0).getText()).incSendnumber();

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
      }

      //store channel name and relevant data into hashtable
      chan.get(chanName).storeMsg(data);

      //add this event info to event list
      componentLabels.add(labels);
      final String ename = labels.get(0);
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
        //create indexedIdentifier, and store it for receive statement
        final IndexedIdentifierProxy indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
        ArrayList<IdentifierProxy> temp =  (ArrayList<IdentifierProxy>) procEvent.get(proctypeName);
        if(temp==null){
          temp = new ArrayList<IdentifierProxy>();
        }
        temp.add(indexEvent);
        procEvent.put(proctypeName,temp);

        final Collection<IdentifierProxy> temp2 = new ArrayList<IdentifierProxy>();
        temp2.add(indexEvent);
        eventStore.add(temp2);
        component.put(proctypeName, componentLabels);
        outputEvent.put(proctypeName, eventStore);
        return indexEvent;
      }
      //receive statement
      if(t.getText().equals("?")|| t.getText().equals("??")){

        //if it is receiving messages, set it to default string receive
        final ArrayList<String> recEverything = new ArrayList<String>();
        recEverything.add("receive");
        componentLabels.add(recEverything);

        component.put(proctypeName, componentLabels);
        final ArrayList<IdentifierProxy> temp = (ArrayList<IdentifierProxy>) procEvent.get(proctypeName);
        eventStore.add(temp);
        outputEvent.put(proctypeName, eventStore);
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
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }
  public Object visitInitialStatement(final InitialStatementTreeNode t){

      final ArrayList<List<String>> tempLabel = new ArrayList<List<String>>();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      mEventDecls.add(event);

      component.put("Init",tempLabel);
      final ArrayList<String> temp = new ArrayList<String>();

      temp.add("init");

      atomic = true;
      //insert this particular event into first place of event label list, for each component
      for (final Map.Entry<String,ArrayList<List<String>>> entry : component.entrySet()) {
        entry.getValue().add(0,temp);

      }

    return null;
  }
  public Object visitRun(final RunTreeNode t){
    final String proctypeName = t.getChild(0).getText();
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("Run"+proctypeName);
    final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
    mEventDecls.add(event);
    if(!eventData.containsKey(proctypeName)){
      eventData.put(proctypeName,component.get(proctypeName));
    }else{
      count++;
      final String newKey = proctypeName+"_"+count;
      eventData.put(newKey,component.get(proctypeName));
    }
    return null;
  }

  public Hashtable<String, ChanInfo> getChan(){
    return chan;

  }

  public Hashtable<String, ArrayList<List<String>>> getComponent(){
    return component;
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
