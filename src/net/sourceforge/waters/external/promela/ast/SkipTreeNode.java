package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;

public class SkipTreeNode extends PromelaTree
{
  public SkipTreeNode(final Token token){
    super(token);
    mType = token.getText();
  }
  public String toString(){
    return "skip";
  }
  private final String mType;
  public String getValue()
  {
      return mType;
  }
  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitSkip(this);

  }
}
