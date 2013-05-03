package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class IntegerTreeNode extends PromelaTree
{
  public IntegerTreeNode(final Token token)
  {
    super(token);
    mValue = Integer.parseInt(token.getText());
  }

  /**
   * A constructor that is used by the SymbolTable class to make some integer constants
   * @param value The value to store in this IntegerTreeNode
   * @author Ethan Duff
   */
  public IntegerTreeNode(final int value)
  {
    super(null);
    mValue = value;
  }

  public String toString()
  {
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