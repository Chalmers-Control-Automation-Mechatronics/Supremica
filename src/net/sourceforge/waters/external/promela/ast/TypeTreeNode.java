package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class TypeTreeNode extends PromelaTreeNode
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
  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return (GraphProxy) visitor.visitType(this);
  }
}
