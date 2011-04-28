package net.sourceforge.waters.external.promela.ast;

import net.sourceforge.waters.external.promela.PromelaVisitor;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

public abstract class PromelaTreeNode extends CommonTree
{
  public PromelaTreeNode(final Token token)
  {
    super(token);
  }

  abstract void acceptVisitor(PromelaVisitor visitor);
}
