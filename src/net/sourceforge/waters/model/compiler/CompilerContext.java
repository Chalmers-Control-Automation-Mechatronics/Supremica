//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   CompilerContext
//###########################################################################
//# $Id: CompilerContext.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.expr.Context;
import net.sourceforge.waters.model.expr.DuplicateIdentifierException;
import net.sourceforge.waters.model.expr.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ModuleProxy;


class CompilerContext implements Context
{

  //#########################################################################
  //# Constructors
  public CompilerContext(final ModuleProxy module)
  {
    this(module, null);
  }

  public CompilerContext(final ModuleProxy module,
			 final String prefix)
  {
    mPrefix = prefix;
    mPath = module.getLocation().getParentFile();
    mSymbolTable = new HashMap();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.Context
  public Value get(final String name)
  {
    return (Value) mSymbolTable.get(name);
  }

  public Value find(final String name)
    throws UndefinedIdentifierException
  {
    final Value value = get(name);
    if (value == null) {
      throw new UndefinedIdentifierException(name);
    }
    return value;
  }

  public void set(final String name, final Value value)
    throws DuplicateIdentifierException
  {
    if (mSymbolTable.containsKey(name)) {
      throw new DuplicateIdentifierException(name);
    } else {
      mSymbolTable.put(name, value);
    }
  }

  public void unset(final String name)
  {
    mSymbolTable.remove(name);
  }


  //#########################################################################
  //# Extension to Context Interface for Compiler
  String getPrefixedName(final String name)
  {
    return mPrefix == null ? name : mPrefix + "." + name;
  }

  File getPath()
  {
    return mPath;
  }


  //#########################################################################
  //# Data Members
  private final String mPrefix;
  private final File mPath;
  private final Map mSymbolTable;

}
