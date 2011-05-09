package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ConstantTreeNode extends PromelaTreeNode
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
  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return (GraphProxy) visitor.visitConstant(this);
  }
}
