package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ExchangeTreeNode extends PromelaTreeNode
{
	public ExchangeTreeNode(final Token token){
		super(token);
		mEx = token.getText();
	}
	public String toString(){
		return "Exchange";
	}
	private final String mEx;
	public String getValue()
	{
		return mEx;
	}
    public GraphProxy acceptVisitor(final PromelaVisitor visitor)
    {
      return (GraphProxy) visitor.visitExchange(this);
    }
}
