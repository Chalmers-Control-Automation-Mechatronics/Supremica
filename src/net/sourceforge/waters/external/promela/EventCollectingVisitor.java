package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Hashtable;

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
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.EventKind;
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
  final Hashtable<String,THashSet<IdentifierProxy>> procEvent = new Hashtable<String,THashSet<IdentifierProxy>>();
  final Hashtable<String,LabelTreeNode> gotoLabel = new Hashtable<String,LabelTreeNode>();
  //This is the output event table, for each proctype
  private final Collection<EventDeclProxy> mEventDecls = new ArrayList<EventDeclProxy>();

  private final Hashtable<String,Integer> occur = new Hashtable<String,Integer>();

  Collection<SimpleExpressionProxy> mRanges = new ArrayList<SimpleExpressionProxy>();
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
    mRanges = new ArrayList<SimpleExpressionProxy>(ranges);
    /*
    IdentifierProxy ident=null;
    IdentifierProxy ident2 = null;
    if(chanLength ==0){
     ident = mFactory.createSimpleIdentifierProxy("exch_"+chanName);
     //final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
     //mEventDecls.add(event);
    }else if(chanLength==1){
      ident = mFactory.createSimpleIdentifierProxy("send_"+chanName);
      ident2 = mFactory.createSimpleIdentifierProxy("recv_"+chanName);

      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);
      final Collection<SimpleExpressionProxy> ranges2 = new ArrayList<SimpleExpressionProxy>(size);
      for(int i=0;i<size;i++){
        final IntConstantProxy zero_2 = mFactory.createIntConstantProxy(lowerEnd.get(i));
        final IntConstantProxy c255_2 = mFactory.createIntConstantProxy(upperEnd.get(i));
        final BinaryOperator op = optable.getRangeOperator();
        final BinaryExpressionProxy range2 = mFactory.createBinaryExpressionProxy(op, zero_2, c255_2);
        ranges2.add(range2);
      }
      final EventDeclProxy event2 = mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges2, null, null);
      //mEventDecls.add(event);
      //mEventDecls.add(event2);
    }
*/


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
      msg.addSenders(n);
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

    msg.addRecipients(n);
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
      if(t.getParent().getParent() instanceof InitialTreeNode){
        final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("run_"+proctypeName);
        final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
        mEventDecls.add(event);
        }
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
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitName(final NameTreeNode t)
  {
    // TODO Auto-generated method stub
    if(t.getParent() instanceof MsgTreeNode){
      labels.add(null);
    }
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
  /*  if(t.getParent() instanceof DoConditionTreeNode){
      PromelaTree tree = t;
      while(!(tree instanceof ProctypeTreeNode)){
        tree = (PromelaTree) tree.getParent();
      }
      final String name = tree.getText();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("step_"+name.toUpperCase());
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      mEventDecls.add(event);

    }
    */
    return null;
  }

  public Object visitLabel(final LabelTreeNode t)
  {
   /* for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
    }
    if(t.getChildCount()>0){
      gotoLabel.put(t.getText(),t);
    }
    */
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        ( (PromelaTree) t.getChild(i)).acceptVisitor(this);
      }
    }
    return null;
  }

  public Object visitGoto(final GotoTreeNode t)
  {
    // TODO Auto-generated method stub

    return null;
  }

  public Object visitSkip(final SkipTreeNode t)
  {
    // TODO Auto-generated method stub
   /* if(!(t.getParent() instanceof SemicolonTreeNode)){
      PromelaTree tree = t;
      while(!((tree instanceof ProctypeTreeNode)||(tree instanceof InitialTreeNode))){
        tree = (PromelaTree) tree.getParent();
      }
      final String name = tree.getText();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("step_"+name);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      mEventDecls.add(event);
    }
    */
    return null;
  }

}
