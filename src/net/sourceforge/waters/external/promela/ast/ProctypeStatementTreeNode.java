package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;

public class ProctypeStatementTreeNode extends PromelaTreeNode
{
  public ProctypeStatementTreeNode(final Token token){
    super(token);
    mProcState = token.getText();
}
  public String toString(){
      return "PROC_STATEMENT";
  }
  private final String mProcState;
  public String getValue()
  {
      return mProcState;
  }
  void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitProcTypeStatement(this);

  }
}
