package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class SemicolonTreeNode extends PromelaTreeNode
{
  public SemicolonTreeNode(final int token)
  {
    this((Token)new CommonToken(token,"STATEMENT"));
    mSemi = "STATEMENT";
  }
  public SemicolonTreeNode(final Token token)
  {
    super(token);
    mSemi = token.getText();
  }
  private String mSemi;
  public String getValue()
  {
    return mSemi;
  }
  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    return (GraphProxy) visitor.visitSemicolon(this);

  }

}
