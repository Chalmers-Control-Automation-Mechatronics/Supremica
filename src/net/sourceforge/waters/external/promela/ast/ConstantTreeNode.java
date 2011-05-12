package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class ConstantTreeNode extends PromelaTree
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
  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return  visitor.visitConstant(this);
  }
}
