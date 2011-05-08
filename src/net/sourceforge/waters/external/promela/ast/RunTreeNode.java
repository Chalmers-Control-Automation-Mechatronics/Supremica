package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.*;

public class RunTreeNode extends PromelaTreeNode
{
  public RunTreeNode(final Token token){
    super(token);
    mRun = token.getText();
  }
  public String toString(){
    return "run";
  }
  private final String mRun;
  public String getValue()
  {
    return mRun;
  }

  public void acceptVisitor(final PromelaVisitor visitor)
   {
     visitor.visitRun(this);

   }
}
