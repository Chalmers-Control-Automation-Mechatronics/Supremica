//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.HashMap;
import java.util.Map;


class TokenTable {

  //#########################################################################
  //# Class Constants
  static final int C_EOF = 0;
  static final int C_OPENBRACE = 1;
  static final int C_CLOSEBRACE = 2;
  static final int C_OPENPAREN = 3;
  static final int C_CLOSEPAREN = 4;
  static final int C_COMMA = 5;
  static final int C_SEMICOLON = 6;
  static final int C_DOT = 7;
  static final int C_PACKAGE = 8;
  static final int C_CLASS = 9;
  static final int C_ENUM = 10;
  static final int C_EXTENDS = 11;
  static final int C_STATIC = 12;
  static final int C_ARG = 13;
  static final int C_REF = 14;
  static final int C_GLUE = 15;
  static final int C_SIMPLETYPE = 16;
  static final int C_IDENTIFIER = 17;

  static final Token T_EOF = new Token(C_EOF, null);
  static final Token T_OPENBRACE = new Token(C_OPENBRACE, "{");
  static final Token T_CLOSEBRACE = new Token(C_CLOSEBRACE, "}");
  static final Token T_OPENPAREN = new Token(C_OPENPAREN, "(");
  static final Token T_CLOSEPAREN = new Token(C_CLOSEPAREN, ")");
  static final Token T_COMMA = new Token(C_COMMA, ",");
  static final Token T_SEMICOLON = new Token(C_SEMICOLON, ";");
  static final Token T_DOT = new Token(C_DOT, ".");
  static final Token T_PACKAGE = new Token(C_PACKAGE, "package");
  static final Token T_CLASS = new Token(C_CLASS, "class");
  static final Token T_ENUM = new Token(C_ENUM, "enum");
  static final Token T_EXTENDS = new Token(C_EXTENDS, "extends");
  static final Token T_STATIC = new Token(C_STATIC, "static");

  static final Token T_REF =
    new ClassModifierToken(C_REF, "ref", ClassModifier.M_REF);
  static final Token T_ARG =
    new ClassModifierToken(C_ARG, "arg", ClassModifier.M_ARG);
  static final Token T_GLUE =
    new ClassModifierToken(C_GLUE, "glue", ClassModifier.M_GLUE);

  static final Token T_TYPE_VOID =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_VOID);
  static final Token T_TYPE_BOOLEAN =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_BOOLEAN);
  static final Token T_TYPE_CHAR =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_CHAR);
  static final Token T_TYPE_DOUBLE =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_DOUBLE);
  static final Token T_TYPE_INT =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_INT);
  static final Token T_TYPE_LONG =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_LONG);
  static final Token T_TYPE_STRING =
    new SimpleTypeToken(SimpleTypeGlue.TYPE_STRING);


  //#########################################################################
  //# The Token Map
  static Token createToken(final String name)
  {
    final Token token = sNameMap.get(name);
    if (token == null) {
      return new IdentifierToken(C_IDENTIFIER, name);
    } else {
      return token;
    }
  }

  static Token getToken(final String name)
  {
    return sNameMap.get(name);
  }


  //#########################################################################
  //# Static Initialisation
  static {
    sNameMap = new HashMap<String,Token>(16);
    recordToken(T_PACKAGE);
    recordToken(T_CLASS);
    recordToken(T_ENUM);
    recordToken(T_EXTENDS);
    recordToken(T_STATIC);
    recordToken(T_REF);
    recordToken(T_ARG);
    recordToken(T_GLUE);
    recordToken(T_TYPE_VOID);
    recordToken(T_TYPE_BOOLEAN);
    recordToken(T_TYPE_CHAR);
    recordToken(T_TYPE_DOUBLE);
    recordToken(T_TYPE_INT);
    recordToken(T_TYPE_LONG);
    recordToken(T_TYPE_STRING);
  }

  private static void recordToken(final Token token)
  {
    final String name = token.getTokenText();
    sNameMap.put(name, token);
  }


  //#########################################################################
  //# Static Class Variables
  private static final Map<String,Token> sNameMap;

}
