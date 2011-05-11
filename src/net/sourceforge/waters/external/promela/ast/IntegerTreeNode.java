package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class IntegerTreeNode extends PromelaTreeNode
{
	public IntegerTreeNode(final Token token){
		super(token);
		mValue = Integer.parseInt(token.getText());
	}
	public String toString(){
		return "Integer Node :"+super.toString();
	}
	private final int mValue;
	public int getValue()
	{
		return mValue;
	}
  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
