package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaType;
import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class VardefTreeNode extends PromelaTree
{
  public VardefTreeNode(final int token, final boolean visible, final PromelaType type)
  {
    this((Token)new CommonToken(token,"Vardefinition"));
    mVisible = visible;
    mVariableType = type;
  }

  public VardefTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }

  public String toString(){
    return "Vardefinition";
  }

  public String getValue()
  {
    return mChanState;
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitVar(this);
  }

  /**
   * A method to get the visibility of the variable(s) that are being created
   * @author Ethan Duff
   * @return TRUE if the variable should be included in the system space or FALSE otherwise
   */
  public boolean isVisible()
  {
    return mVisible;
  }

  /**
   * A method to get the type of the variable(s) that are being created
   * @author Ethan Duff
   * @return A promela type matching this variable
   */
  public PromelaType getVariableType()
  {
    return mVariableType;
  }

  /**
   * A method to get the locality of the variable(s) being created
   * @author Ethan Duff
   * @return TRUE if the variable(s) are global, or FALSE if they are local to a method
   */
  public boolean isGlobal()
  {
    return mGlobal;
  }

  private final String mChanState;
  private boolean mVisible;
  private PromelaType mVariableType;
  private boolean mGlobal;
}
