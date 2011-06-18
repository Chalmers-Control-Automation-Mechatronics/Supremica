package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;

public class LabelTreeNode extends PromelaTree
{

   public LabelTreeNode(final Token token){
          super(token);
          mInit = token.getText();
      }
      public String toString(){
          return mInit;
      }
      private final String mInit;
      public String getValue()
      {
          return mInit;
      }
      public Object acceptVisitor(final PromelaVisitor visitor)
      {
        return  visitor.visitLabel(this);

      }

}
