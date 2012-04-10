package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

public class VardefTreeNode extends PromelaTree
{

  public VardefTreeNode(final int token, final boolean visible)
  {
    // TODO Need int constructor
    this((Token)new CommonToken(token,"Vardefinition"));
    mVisible = visible;
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

  //NEW<============================================================================================================================================
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
   * A method to get the locality of the variable(s) being created
   * @author Ethan Duff
   * @return TRUE if the variable(s) are global, or FALSE if they are local to a method
   */ /* This method may or may not be included
  public boolean isGlobal()
  {
    return mGlobal;
  }
  */
  //===============================================================================================================================================>

  private final String mChanState;

  //NEW<============================================================================================================================================
  private boolean mVisible;
  //private boolean mGlobal; //This member may or may not be included
  //===============================================================================================================================================>

}
