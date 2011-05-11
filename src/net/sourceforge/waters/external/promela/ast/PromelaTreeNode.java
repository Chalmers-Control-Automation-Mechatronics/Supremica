package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;



public abstract class PromelaTreeNode extends CommonTree
{
  public PromelaTreeNode(final Token token)
  {
    super(token);
  }

  public abstract Object acceptVisitor(PromelaVisitor visitor);
}
