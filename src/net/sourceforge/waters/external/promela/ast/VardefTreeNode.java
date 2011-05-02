package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class VardefTreeNode extends PromelaTreeNode
{
  public VardefTreeNode(final int token)
  {
    // TODO Need int constructor
    this((Token)new CommonToken(token,"Vardefinition"));
    mChanState = "Vardefinition";
  }

  public VardefTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }

  public String toString(){
    return "Vardefinition";
  }

  public String getValue()
  {
    return mChanState;
  }

  public void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitVar(this);
  }

  private String mChanState;

}
