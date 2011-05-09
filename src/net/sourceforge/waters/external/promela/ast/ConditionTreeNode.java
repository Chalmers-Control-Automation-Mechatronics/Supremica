package net.sourceforge.waters.external.promela.ast;


import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class ConditionTreeNode extends PromelaTreeNode
{
	public ConditionTreeNode(final Token token){
		super(token);
		mCondition = token.getText();
	}
	public String toString(){
		return "Condition Node "+super.toString();
	}
	private final String mCondition;
	public String getValue()
	{
		return mCondition;
	}
  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
