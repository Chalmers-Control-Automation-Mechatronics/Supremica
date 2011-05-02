package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.*;

public class ModuleTreeNode extends PromelaTreeNode
{
  public ModuleTreeNode(final Token token){
    super(token);
    mEx = token.getText();
}
public String toString(){
    return "Module";
}
private final String mEx;
public String getValue()
{
    return mEx;
}
void acceptVisitor(final PromelaVisitor visitor)
{
  visitor.visitModule(this);
}
}
