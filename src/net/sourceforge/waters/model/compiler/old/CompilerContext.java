//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompilerContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.old;

import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.
  DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class CompilerContext
{

  //#########################################################################
  //# Constructors
  CompilerContext(final ModuleProxy module)
  {
    this(module, null);
  }

  CompilerContext(final ModuleProxy module, final String prefix)
  {
    mURI = module.getLocation();
    mPrefix = prefix;
    mSymbolTable = new HashMap<String,Value>();
  }


  //#########################################################################
  //# Accessing the Symbol Table
  void add(final CompiledEventDecl decl)
    throws DuplicateIdentifierException
  {
    final String name = decl.getName();
    final EventValue value = decl.getValue();
    add(name, value);
  }

  void add(final String name, final Value value)
    throws DuplicateIdentifierException
  {
    if (mSymbolTable.containsKey(name)) {
      throw new DuplicateIdentifierException(name);
    } else {
      mSymbolTable.put(name, value);
    }
  }

  Value get(final String name)
  {
    return mSymbolTable.get(name);
  }

  Value find(final String name)
    throws UndefinedIdentifierException
  {
    final Value value = mSymbolTable.get(name);
    if (value == null) {
      throw new UndefinedIdentifierException(name);
    } else {
      return value;
    }
  }

  Value find(final String name,
             final List<IndexValue> indexValues,
             final List<SimpleExpressionProxy> indexExpressions)
    throws EvalException
  {
    Value value = mSymbolTable.get(name);
    if (value == null) {
      throw new UndefinedIdentifierException(name);
    } else {
      int pos = 0;
      for (final IndexValue index : indexValues) {
        if (value instanceof ArrayValue) {
          try {
            final ArrayValue array = (ArrayValue) value;
            value = array.find(index);
            pos++;
          } catch (final EvalException exception) {
            final SimpleExpressionProxy expr = indexExpressions.get(pos);
            exception.provideLocation(expr);
            throw exception;
          }
        } else {
          throw new IndexOutOfRangeException(value);
        }
      }
    }
    return value;
  }

  void unset(final String name)
  {
    mSymbolTable.remove(name);
  }


  //#########################################################################
  //# Accessing the Prefix
  String getPrefixedName(final String name)
  {
    return mPrefix == null ? name : mPrefix + "." + name;
  }

  URI getURI()
  {
    return mURI;
  }


  //#########################################################################
  //# Data Members
  private final String mPrefix;
  private final URI mURI;
  private final Map<String,Value> mSymbolTable;

}
