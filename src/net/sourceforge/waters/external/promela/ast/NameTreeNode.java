package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.*;

public class NameTreeNode extends PromelaTreeNode
{
  public NameTreeNode(final Token token){
    super(token);
    mInitState = token.getText();
  }
  public String toString(){
      return "name";
  }
  private final String mInitState;
  public String getValue()
  {
      return mInitState;
  }
  public void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitName(this);
  }
}
