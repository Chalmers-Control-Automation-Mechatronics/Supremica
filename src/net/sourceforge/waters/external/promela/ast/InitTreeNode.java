package net.sourceforge.waters.external.promela.ast;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

  public class InitTreeNode extends CommonTree
  {
    public InitTreeNode(final Token token){
      super();
      mInit = token.getText();
  }
  public String toString(){
      return "INIT";
  }
  private final String mInit;
  public String getValue()
  {
      return mInit;
  }
}
