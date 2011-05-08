package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;

public class SemicolonTreeNode extends PromelaTreeNode
{

  public SemicolonTreeNode(final Token token)
  {
    super(token);
   mSemi = token.getText();
  }
  private final String mSemi;
  public String getValue()
  {
    return mSemi;
  }
  public void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitSemicolon(this);

  }

}
