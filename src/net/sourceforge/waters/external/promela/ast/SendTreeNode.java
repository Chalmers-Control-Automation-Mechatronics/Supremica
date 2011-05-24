package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class SendTreeNode extends PromelaTree
{
    public SendTreeNode(final Token token){
        super(token);
        mEx = token.getText();
    }
    public String toString(){
        return "send";
    }
    private final String mEx;
    public String getValue()
    {
        return mEx;
    }
    public Object acceptVisitor(final PromelaVisitor visitor)
    {
      return  visitor.visitSend(this);
    }
}
