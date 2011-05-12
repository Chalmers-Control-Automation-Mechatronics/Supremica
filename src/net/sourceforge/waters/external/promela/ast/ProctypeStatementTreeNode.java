package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class ProctypeStatementTreeNode extends PromelaTree
{

  public ProctypeStatementTreeNode(final int token)
  {
    this((Token)new CommonToken(token,"ProcState"));
    mProcState = "ProcState";
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

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitProcTypeStatement(this);
  }

  private String mProcState;

}
