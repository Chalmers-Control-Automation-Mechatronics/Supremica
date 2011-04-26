package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class ConstantTreeNode extends CommonTree
{
	public ConstantTreeNode(final Token token){
		super(token);
		mValue = Integer.parseInt(token.getText());
	}
	public String toString(){
		return "Constant";
	}
	private final int mValue;
	public int getValue()
	{
		return mValue;
	}
}
