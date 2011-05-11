package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
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
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class GraphCollectingVisitor implements PromelaVisitor
{
  private final ModuleProxyFactory mFactory = new ModuleElementFactory();

  private EventCollectingVisitor mVisitor=null;

  ArrayList<String> data =new ArrayList<String>();
  ArrayList<String> labels = new ArrayList<String>();

  ArrayList<List<String>> componentLabels = new ArrayList<List<String>>();

  final Hashtable<String,Collection<IdentifierProxy>> procEvent = new Hashtable<String,Collection<IdentifierProxy>>();


  ArrayList<Integer> lowerEnd = new ArrayList<Integer>();
  ArrayList<Integer> upperEnd = new ArrayList<Integer>();
  Hashtable<String,PromelaGraph> storePromela = new Hashtable<String,PromelaGraph>();
  final ArrayList<String> procNames = new ArrayList<String>();
  ArrayList<String> chanNames = new ArrayList<String>();
  Collection<SimpleComponentProxy> mComponents = new ArrayList<SimpleComponentProxy>();

  public GraphCollectingVisitor(final EventCollectingVisitor v){
    mVisitor = v;
  }
  public PromelaGraph collectGraphs(final PromelaTreeNode node)
  {
    return (PromelaGraph) node.acceptVisitor(this);
  }

  public Collection<SimpleComponentProxy> getComponents(){
    return mComponents;
  }
  public Object visitModule(final ModuleTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  //Now it directly create PromelaGraph object, using events from Event collector; No need to visit further children
  public Object visitProcType(final ProctypeTreeNode t)
  {
    final String procName = t.getText();
    //visit child 1
    final PromelaTreeNode statement = (PromelaTreeNode) t.getChild(1);
    PromelaGraph g = collectGraphs(statement);
    final IdentifierProxy ident;
    if(mVisitor.getAtomic()){
      ident = mFactory.createSimpleIdentifierProxy("init");
    }else{
      ident = mFactory.createSimpleIdentifierProxy("Run"+procName);
    }
    final PromelaGraph newGraph = new PromelaGraph(ident);
    g = PromelaGraph.sequentialComposition(newGraph, g);
    final GraphProxy graph = g.createGraphProxy();
    final IdentifierProxy name = mFactory.createSimpleIdentifierProxy(procName);
    final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(name, ComponentKind.PLANT, graph);
    mComponents.add(component);

    //procEvent.put(procName,new ArrayList<IdentifierProxy>());
    //graphIndex.put(procName,1);

    return null;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    for(int i=0;i<t.getChildCount();i++){
      ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
    }
    return null;
  }

  public Object visitVar(final VardefTreeNode t)
  {
 // TODO Auto-generated method stub
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    final String name = t.getChild(0).getText();
    procEvent.put(name,new ArrayList<IdentifierProxy>());
    chanNames.add(name);
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t)
  {
    PromelaGraph result = null;
    final PromelaGraph step = collectGraphs((PromelaTreeNode) t.getChild(0));
    result = PromelaGraph.sequentialComposition(result,step);

    return result;
  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t)
  {
    return null;
  }

  //return PromelaGraph of proctype statements
  public Object visitExchange(final ExchangeTreeNode t)
  {
    final String chanName = t.getChild(0).getText();

    //Send statement
    if(t.getText().equals("!")|| t.getText().equals("!!")){
      labels = new ArrayList<String>();
      labels.add(chanName);

      for(int i = 0; i <t.getChildCount();i++){
        ( (PromelaTreeNode) t.getChild(i)).acceptVisitor(this);
      }

      final String ename = labels.get(0);
      final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(labels.size()-1);
      for(int y=1;y<labels.size();y++){
        final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(labels.get(y)));
        indexes.add(c);
      }
      //create indexedIdentifier, and store it for receive statement
      final IndexedIdentifierProxy indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
      Collection<IdentifierProxy> temp = procEvent.get(chanName);
      if(temp==null){
        temp = new ArrayList<IdentifierProxy>();

        procEvent.put(chanName,temp);
      }
        temp.add(indexEvent);

        return new PromelaGraph(indexEvent);
      }

      //receive statement
      if(t.getText().equals("?")|| t.getText().equals("??")){
        return new PromelaGraph(procEvent.get(chanName));
      }

    return null;
  }

  public Object visitConstant(final ConstantTreeNode t)
  {
    //add all event data
    labels.add(t.getText());
    return null;
  }

  public Object visitInitial(final InitialTreeNode t)
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
    final PromelaGraph initGraph = collectGraphs((PromelaTreeNode) t.getChild(0));
    final GraphProxy graph = initGraph.createGraphProxy();
    final SimpleComponentProxy component = mFactory.createSimpleComponentProxy(ident, ComponentKind.PLANT, graph);
    mComponents.add(component);

    return null;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    assert t.getText().equals("atomic");
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
    final PromelaGraph initGraph = new PromelaGraph(ident);
    return initGraph;
  }

  public Object visitRun(final RunTreeNode t)
  {
      final String name = t.getChild(0).getText();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("Run"+name);
      final PromelaGraph graph = new PromelaGraph(ident);
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
    if(t.getChildCount()>0){
      for(int i=0;i<t.getChildCount();i++){
        final PromelaGraph step = collectGraphs((PromelaTreeNode) t.getChild(i));
        result = PromelaGraph.sequentialComposition(result,step);
      }
    }
    return result;
  }

  public Object visitType(final TypeTreeNode t)
  {
    return null;
  }

}
