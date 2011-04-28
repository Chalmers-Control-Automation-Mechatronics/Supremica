package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.*;

public class ChannelStatementTreeNode extends PromelaTreeNode
{

  public ChannelStatementTreeNode(final int token)
  {
    // TODO Need int constructor
    super(null);
    mChanState = null;
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

  void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitChannelStatement(this);
  }

  private final String mChanState;

}
