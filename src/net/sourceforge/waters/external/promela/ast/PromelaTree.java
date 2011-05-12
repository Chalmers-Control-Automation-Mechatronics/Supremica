package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;



public abstract class PromelaTree extends CommonTree
{
  public PromelaTree(final Token token)
  {
    super(token);
  }

  public abstract Object acceptVisitor(PromelaVisitor visitor);
}
