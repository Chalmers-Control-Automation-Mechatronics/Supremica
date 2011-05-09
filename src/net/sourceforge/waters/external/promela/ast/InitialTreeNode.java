package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class InitialTreeNode extends PromelaTreeNode
{
    public InitialTreeNode(final Token token){
        super(token);
        mInit = token.getText();
    }
    public String toString(){
        return "init";
    }
    private final String mInit;
    public String getValue()
    {
        return mInit;
    }
    public GraphProxy acceptVisitor(final PromelaVisitor visitor)
    {
      return (GraphProxy) visitor.visitInitial(this);

    }
}
