package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class ChannelStatementTreeNode extends PromelaTree
{

  public ChannelStatementTreeNode(final int token)
  {
    // TODO Need int constructor
    this((Token)new CommonToken(token,"ChanState"));
    mChanState = "ChanState";
  }

  public ChannelStatementTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }

  public String toString(){
    return "CHAN_STATEMENT";
  }

  public String getValue()
  {
    return mChanState;
  }

  public Object acceptVisitor(final PromelaVisitor visitor)
  {
    return  visitor.visitChannelStatement(this);
  }

  private String mChanState;

}
