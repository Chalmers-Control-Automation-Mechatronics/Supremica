package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
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

  //########################################################################
  //# Invocation
  public void collectEvents(final PromelaTreeNode node)
  {
    node.acceptVisitor(this);
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
    //data.clear();
    //labels.clear();

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
    //System.out.println(name);
    chan.put(name,new ChanInfo(name, length, datalength,type));
    return null;
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

      }
      //receive statement
      if(t.getText().equals("?")|| t.getText().equals("??")){
       // chan.get(proctypeName).incRecnumber();
        //if it is receiving messages, set it to default string receive
        final ArrayList<String> recEverything = new ArrayList<String>();
        //!!!!!!!!! how to solve where the send request come from@

        recEverything.add("receive");
        componentLabels.add(recEverything);
      }
      component.put(proctypeName, componentLabels);
     // System.out.println(proctypeName + "->"+componentLabels);
      return null;
  }

  public Object visitConstant(final ConstantTreeNode t){
    data.add(t.getText());
    //add all event data
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t){
   // final ArrayList<String> temp = new ArrayList<String>();
    final ArrayList<List<String>> tempLabel = new ArrayList<List<String>>();
    //temp.add("init");
    //tempLabel.add(temp);
    component.put("Init",tempLabel);
    count = 0;
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }
  public Object visitInitialStatement(final InitialStatementTreeNode t){
    if(t.getText().equals("atomic")){
      final ArrayList<String> temp = new ArrayList<String>();

      temp.add("init");

      atomic = true;
      //insert this particular event into first place of event label list, for each component
      for (final Map.Entry<String,ArrayList<List<String>>> entry : component.entrySet()) {
        entry.getValue().add(0,temp);

      }
    }
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }
  public Object visitRun(final RunTreeNode t){
    final String proctypeName = t.getChild(0).getText();

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
  /*  for (final Map.Entry<String,ArrayList<List<String>>> entry : component.entrySet()) {
      System.out.println(entry.getKey()+"->"+entry.getValue());
    }
    */
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
 // TODO Auto-generated method stub
    return null;


  }
}
