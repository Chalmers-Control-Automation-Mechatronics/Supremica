package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.external.promela.SymbolTable;


import org.antlr.runtime.*;

public class ProctypeTreeNode extends PromelaTree
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

    private SymbolTable mSymbolTable;//A pointer to the symbol table

    /**
     * A method to get the symbol table used for this current ProcTypeTreeNode.
     * It contains the variables etc. that are local to this scope, and is link up to higher scopes for global and broader range local variables, channels, etc.
     * @author Ethan Duff
     * @return The Symbol table for this ProcTypeTreeNode
     */
    public SymbolTable getSymbolTable()
    {
      return mSymbolTable;
    }

    /**
     * A method to set the symbol table for this ProcTypeTreeNode
     * @author Ethan Duff
     * @param table The table that has been created to for storing information relative to the scope of this ProcTypeTreeNode
     */
    public void setSymbolTable(final SymbolTable table)
    {
      mSymbolTable = table;
    }
}
