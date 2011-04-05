package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class ExchangeTreeNode extends CommonTree
{
	public ExchangeTreeNode(final Token token){
		super(token);
		mEx = token.getText();
	}
	public String toString(){
		return "Exchange Node :"+super.toString();
	}
	private final String mEx;
	public String getValue()
	{
		return mEx;
	}
}
