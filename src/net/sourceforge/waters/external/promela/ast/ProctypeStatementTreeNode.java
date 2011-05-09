package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ProctypeStatementTreeNode extends PromelaTreeNode
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

  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    return (GraphProxy) visitor.visitProcTypeStatement(this);
  }

  private String mProcState;

}
