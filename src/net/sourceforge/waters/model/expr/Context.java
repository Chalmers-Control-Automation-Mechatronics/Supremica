//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   Context
//###########################################################################
//# $Id: Context.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public interface Context {

  public Value get(String name);

  public Value find(String name)
    throws UndefinedIdentifierException;

  public void set(String name, Value value)
    throws DuplicateIdentifierException;

  public void unset(final String name);

}