package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class SemicolonTreeNode extends PromelaTree
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
  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitSemicolon(this);

  }

}
