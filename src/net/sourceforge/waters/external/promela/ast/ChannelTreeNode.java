package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ChannelTreeNode extends PromelaTreeNode
{
    public ChannelTreeNode(final Token token){
        super(token);
        mType = token.getText();
    }
    public String toString(){
        return "chan";
    }
    private final String mType;
    public String getValue()
    {
        return mType;
    }
    public GraphProxy acceptVisitor(final PromelaVisitor visitor)
    {
      return (GraphProxy) visitor.visitChannel(this);

    }
}

