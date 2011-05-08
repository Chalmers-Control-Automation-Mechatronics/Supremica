package net.sourceforge.waters.external.promela;

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
import net.sourceforge.waters.external.promela.ast.VardefTreeNode;

public class GraphCollectingVisitor implements PromelaVisitor
{

  public void collectGraphs(final PromelaTreeNode node)
  {
    node.acceptVisitor(this);
  }

  public Object visitModule(final ModuleTreeNode t)
  {
    // TODO Auto-generated method stub

    return null;
  }

  public Object visitProcType(final ProctypeTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitMsg(final MsgTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitVar(final VardefTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitChannel(final ChannelTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t)
  {
    return null;
    // TODO Auto-generated method stub

  }

  public Object visitChannelStatement(final ChannelStatementTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitExchange(final ExchangeTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitConstant(final ConstantTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitInitial(final InitialTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitInitialStatement(final InitialStatementTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitRun(final RunTreeNode t)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Object visitName(final NameTreeNode t)
  {
    return null;
    // TODO Auto-generated method stub

  }

  public Object visitSemicolon(final SemicolonTreeNode semicolonTreeNode)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
