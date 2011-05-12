package net.sourceforge.waters.external.promela.ast;



import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class InitialStatementTreeNode extends PromelaTree
{
    public InitialStatementTreeNode(final Token token){
        super(token);
        mInitState = token.getText();
    }
    public String toString(){
        return "INIT_STATEMENT";
    }
    private final String mInitState;
    public String getValue()
    {
        return mInitState;
    }
    public Object acceptVisitor(final PromelaVisitor visitor)
    {
      return  visitor.visitInitialStatement(this);
    }
}

