package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

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
    public void acceptVisitor(final PromelaVisitor visitor)
    {
      visitor.visitChannel(this);

    }
}

