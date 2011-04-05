package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class ConditionTreeNode extends CommonTree
{
	public ConditionTreeNode(final Token token){
		super(token);
		mCondition = token.getText();
	}
	public String toString(){
		return "Condition Node "+super.toString();
	}
	private final String mCondition;
	public String getValue()
	{
		return mCondition;
	}
}
