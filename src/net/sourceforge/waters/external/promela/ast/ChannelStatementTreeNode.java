package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;

public class ChannelStatementTreeNode extends PromelaTreeNode
{

  public ChannelStatementTreeNode(final Token token)
  {
    super(token);
    mChanState = token.getText();
  }
  public String toString(){
    return "CHAN_STATEMENT";
  }
  private final String mChanState;
  public String getValue()
  {
    return mChanState;
  }
  void acceptVisitor(final PromelaVisitor visitor)
  {
    visitor.visitChannelStatement(this);

  }

}
