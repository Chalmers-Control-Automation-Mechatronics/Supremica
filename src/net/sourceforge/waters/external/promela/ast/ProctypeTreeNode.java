package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;


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
    public Object acceptVisitor(final PromelaVisitor visitor)
    {
        return  visitor.visitProcType(this);
    }

}
