package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class ProctypeTreeNode extends CommonTree
{
	public ProctypeTreeNode(final Token token){
		super(token);
		mProc = token.getText();
	}
	public String toString(){
		return "Proctype Node :"+super.toString();
	}
	private final String mProc;
	public String getValue()
	{
		return mProc;
	}
}
