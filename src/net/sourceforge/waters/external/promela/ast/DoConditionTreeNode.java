package net.sourceforge.waters.external.promela.ast;
import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;
public class DoConditionTreeNode extends PromelaTree
{
  public DoConditionTreeNode(final Token token){
    super(token);
    mInitState = token.getText();
}
public String toString(){
    return "do";
}
private final String mInitState;
public String getValue()
{
    return mInitState;
}
public Object acceptVisitor(final PromelaVisitor visitor)
{
  return  visitor.visitDoStatement(this);
}
}




