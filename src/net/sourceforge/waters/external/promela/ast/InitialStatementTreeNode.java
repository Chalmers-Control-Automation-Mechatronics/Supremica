package net.sourceforge.waters.external.promela.ast;



import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class InitialStatementTreeNode extends PromelaTreeNode
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
    public GraphProxy acceptVisitor(final PromelaVisitor visitor)
    {
      return (GraphProxy) visitor.visitInitialStatement(this);
    }
}

