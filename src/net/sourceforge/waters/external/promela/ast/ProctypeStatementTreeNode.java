package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.*;

public class ProctypeStatementTreeNode extends PromelaTreeNode
{

  public ProctypeStatementTreeNode(final int token)
  {
    super(null);
    mProcState = null;
    // TODO Need int constructor
  }

  public ProctypeStatementTreeNode(final Token token){
    super(token);
    mProcState = token.getText();
  }

  public String toString(){
    return "PROC_STATEMENT";
  }

  public String getValue()
  {
    return mProcState;
  }

  void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitProcTypeStatement(this);
  }

  private final String mProcState;

}
