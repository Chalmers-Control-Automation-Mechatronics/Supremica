package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import net.sourceforge.waters.model.module.GraphProxy;

import org.antlr.runtime.*;

public class MsgTreeNode extends PromelaTreeNode
{
  public MsgTreeNode(final int token)
  {
    // TODO Need int constructor
    this((Token)new CommonToken(token,"MsgArgument"));
    mChanState = "MsgArgument";
  }

  public MsgTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }

  public String toString(){
    return "MsgArgument";
  }

  public String getValue()
  {
    return mChanState;
  }

  public GraphProxy acceptVisitor(final PromelaVisitor visitor)
  {
    return (GraphProxy) visitor.visitMsg(this);
  }

  private String mChanState;

}
