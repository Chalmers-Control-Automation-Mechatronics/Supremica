package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class ModuleTreeNode extends PromelaTreeNode
{
  public ModuleTreeNode(final Token token){
    super(token);
    mEx = token.getText();
  }
  public ModuleTreeNode(final int token){
    this((Token)new CommonToken(token,"Root"));
    mEx = "Root";
  }
  public String toString(){
      return "Module";
  }
  private String mEx;
  public String getValue()
  {
      return mEx;
  }
  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitModule(this);
  }
 }
