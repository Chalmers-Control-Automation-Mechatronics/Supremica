package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ProctypeTreeNode extends PromelaTreeNode
{
	public ProctypeTreeNode(final Token token){
		super(token);
		mProc = token.getText();
	}
	public String toString(){
		return "Proctype";
	}
	private final String mProc;
	public String getValue()
	{
		return mProc;
	}
    public GraphProxy acceptVisitor(final PromelaVisitor visitor)
    {
        return (GraphProxy) visitor.visitProcType(this);
    }

}
