package net.sourceforge.waters.external.promela.ast;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class InitialTreeNode extends CommonTree
{
    public InitialTreeNode(final Token token){
        super(token);
        mProc = token.getText();
    }
    public String toString(){
        return "init";
    }
    private final String mProc;
    public String getValue()
    {
        return mProc;
    }
}
