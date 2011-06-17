package net.sourceforge.waters.external.promela;

import net.sourceforge.waters.external.promela.ast.BreakStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ChannelStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ChannelTreeNode;
import net.sourceforge.waters.external.promela.ast.ConditionTreeNode;
import net.sourceforge.waters.external.promela.ast.ConstantTreeNode;
import net.sourceforge.waters.external.promela.ast.DoConditionTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.InitialTreeNode;
import net.sourceforge.waters.external.promela.ast.ModuleTreeNode;
import net.sourceforge.waters.external.promela.ast.MsgTreeNode;
import net.sourceforge.waters.external.promela.ast.NameTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeStatementTreeNode;
import net.sourceforge.waters.external.promela.ast.ProctypeTreeNode;
import net.sourceforge.waters.external.promela.ast.ReceiveTreeNode;
import net.sourceforge.waters.external.promela.ast.RunTreeNode;
import net.sourceforge.waters.external.promela.ast.SemicolonTreeNode;
import net.sourceforge.waters.external.promela.ast.SendTreeNode;
import net.sourceforge.waters.external.promela.ast.TypeTreeNode;
import net.sourceforge.waters.external.promela.ast.VardefTreeNode;


public interface PromelaVisitor
{

  public Object visitModule(final ModuleTreeNode t);

  public Object visitProcType(final ProctypeTreeNode t);

  public Object visitMsg(final MsgTreeNode t);

  public Object visitVar(final VardefTreeNode t);

  public Object visitChannel(final ChannelTreeNode t);

  public Object visitProcTypeStatement(final ProctypeStatementTreeNode t);

  public Object visitChannelStatement(final ChannelStatementTreeNode t);

  public Object visitConstant(final ConstantTreeNode t);

  public Object visitInitial(final InitialTreeNode t);

  public Object visitInitialStatement(final InitialStatementTreeNode t);

  public Object visitRun(final RunTreeNode t);

  public Object visitName(final NameTreeNode t);

  public Object visitSemicolon(SemicolonTreeNode t);

  public Object visitType(TypeTreeNode t);

  public Object visitSend(SendTreeNode t);

  public Object visitReceive(ReceiveTreeNode t);

  public Object visitCondition(ConditionTreeNode t);

  public Object visitDoStatement(DoConditionTreeNode t);

  public Object visitBreak(BreakStatementTreeNode t);

}
