package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.CommonToken;

public class UnaryStatementTreeNode extends PromelaTree
{
  final private String mOperator;

  public UnaryStatementTreeNode(final int token, final String operator)
  {
    super(new CommonToken(token));
    mOperator = operator;
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public String getOperator()
  {
    return mOperator;
  }
}
