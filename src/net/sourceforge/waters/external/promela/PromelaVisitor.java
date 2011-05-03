package net.sourceforge.waters.external.promela;

import net.sourceforge.waters.external.promela.ast.ModuleTreeNode;

import org.antlr.runtime.tree.CommonTree;


public interface PromelaVisitor
{

  public Object visitModule(final ModuleTreeNode t);

  public void visitProcType(final CommonTree t);

  public void visitMsg(final CommonTree t);

  public void visitVar(final CommonTree t);

  public void visitChannel(final CommonTree t);

  public void visitProcTypeStatement(final CommonTree t);

  public void visitChannelStatement(final CommonTree t);

  public void visitExchange(final CommonTree t);

  public void visitConstant(final CommonTree t);

  public void visitInitial(final CommonTree t);

  public void visitInitialStatement(final CommonTree t);

  public void visitRun(final CommonTree t);

  public void visitName(final CommonTree t);

}
