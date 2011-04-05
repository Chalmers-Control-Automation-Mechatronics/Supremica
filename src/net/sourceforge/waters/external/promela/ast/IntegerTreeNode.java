package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class IntegerTreeNode extends CommonTree
{
	public IntegerTreeNode(final Token token){
		super(token);
		mValue = Integer.parseInt(token.getText());
	}
	public String toString(){
		return "Integer Node :"+super.toString();
	}
	private final int mValue;
	public int getValue()
	{
		return mValue;
	}
}
