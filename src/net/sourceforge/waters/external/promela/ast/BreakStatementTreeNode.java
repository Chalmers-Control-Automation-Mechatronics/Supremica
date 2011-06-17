package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;

public class BreakStatementTreeNode extends PromelaTree
{
  public BreakStatementTreeNode(final Token token){
    super(token);
    mCondition = token.getText();
}
public String toString(){
    return "Break";
}
private final String mCondition;
public String getValue()
{
    return mCondition;
}
public Object acceptVisitor(final PromelaVisitor visitor)
{
// TODO Auto-generated method stub
return visitor.visitBreak(this);
}
}
