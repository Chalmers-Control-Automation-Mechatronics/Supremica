package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class TypeTreeNode extends CommonTree
{
	public TypeTreeNode(final Token token){
		super(token);
		mType = token.getText();
	}
	public String toString(){
		return "Type";
	}
	private final String mType;
	public String getValue()
	{
		return mType;
	}
}
