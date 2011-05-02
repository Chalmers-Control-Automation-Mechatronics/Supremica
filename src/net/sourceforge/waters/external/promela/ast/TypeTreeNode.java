package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;

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
  public void acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub

  }
}
