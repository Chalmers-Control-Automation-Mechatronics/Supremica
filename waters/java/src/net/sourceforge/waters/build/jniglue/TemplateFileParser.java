//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;


class TemplateFileParser extends ErrorReporter {

  //#########################################################################
  //# Constructors
  TemplateFileParser(final File filename)
    throws FileNotFoundException
  {
    super(filename);
    mReader = new FileReader(filename);
    mFragmentStack = new LinkedList<StackEntry>();
    mCurrentSequence = new Template(filename);
    mCurrentContainer = mCurrentSequence;
    mPutbackCharacter = -2;
    mLineNo = 1;
  }

  TemplateFileParser(final Reader reader)
  {
    mReader = reader;
    mFragmentStack = new LinkedList<StackEntry>();
    mCurrentSequence = new Template();
    mCurrentContainer = mCurrentSequence;
    mPutbackCharacter = -2;
    mLineNo = 1;
  }


  //#########################################################################
  //# Simple Access
  int getLineNo()
  {
    return mLineNo;
  }


  //#########################################################################
  //# Parsing
  Template parse()
  {
    try {
      parseFile();
    } catch (final IOException exception) {
      reportError(exception);
    }
    if (getNumErrors() == 0) {
      return (Template) mCurrentContainer;
    } else {
      return null;
    }
  }

  void close()
    throws IOException
  {
    mReader.close();
  }


  //#########################################################################
  //# Auxiliary Methods for Parsing
  private void parseFile()
    throws IOException
  {
    StringBuilder buffer = new StringBuilder();
    int code = getNextCharacter();
    while (code != -1) {
      if (code == '$') {
        int numhats = 0;
        int secondcode = getNextCharacter();
        while (secondcode == '^') {
          numhats++;
          secondcode = getNextCharacter();
        }
        if (secondcode >= 'A' && secondcode <= 'Z') {
          storeTextFragment(buffer);
          putback(secondcode);
          parseDollarVar(numhats);
          skipWhiteSpace();
          buffer = new StringBuilder();
        } else {
          if (numhats > 0) {
            final ParseException exception = createParseException
             ("Reference character '^' cannot be used without variable name!");
            reportError(exception);
          }
          if (secondcode == '-') {
            do {
              code = getNextCharacter();
              switch (code) {
              case -1:
                putback(code);
                break;
              case '$':
                secondcode = getNextCharacter();
                if (secondcode == '+') {
                  code = -1;
                } else {
                  putback(secondcode);
                }
                break;
              }
            } while (code != -1);
            skipWhiteSpace();
          } else if (secondcode == '=') {
            skipWhiteSpace(true);   
          } else if (secondcode == -1) {
            buffer.append('$');
            putback(secondcode);
          } else {
            final char ch = (char) secondcode;
            buffer.append(ch);
          }
        }
      } else {
        final char ch = (char) code;
        buffer.append(ch);
      }
      code = getNextCharacter();
    }
    storeTextFragment(buffer);
    if (!mFragmentStack.isEmpty()) {
      String kind;
      if (mCurrentContainer instanceof TemplateFragmentForeach) {
        kind = "FOREACH";
      } else if (mCurrentContainer instanceof TemplateFragmentConditional) {
        kind = "IF";
      } else {
        throw new IllegalStateException("Unexpected container class!");
      }
      final ParseException exception =
        createParseException("No matching $END for $" + kind + " in line " +
                             mCurrentContainer.getLineNo() + "!");
      reportError(exception);
    }
  }

  private void parseDollarVar(final int numhats)
    throws IOException
  {
    try {
      final String name = parseUpperCaseName();
      if (name.equals("FOREACH")) {
        final String suffix = parseUpperCaseSuffix(name);
        openForeachFragment(suffix, numhats);
      } else if (name.equals("ENDFOR")) {
        closeForeachFragment();
      } else if (name.equals("IF")) {
        final String suffix = parseUpperCaseSuffix(name);
        openConditionalFragment(suffix, numhats);
      } else if (name.equals("ELSEIF")) {
        final String suffix = parseUpperCaseSuffix(name);
        openConditionalAlternative(suffix, numhats);
      } else if (name.equals("ELSE")) {
        openConditionalAlternative();
      } else if (name.equals("ENDIF")) {
        closeConditionalFragment();
      } else {
        storeVariableFragment(name, numhats);
      }
    } catch (final ParseException exception) {
      reportError(exception);
    }
  }

  private String parseUpperCaseSuffix(final String prefix)
    throws IOException, ParseException
  {
    final int code = getNextCharacter();
    if (code != '-') {
      putback(code);
      throw createBadNameException(prefix);
    }
    return parseUpperCaseName(prefix);
  }

  private String parseUpperCaseName()
    throws IOException, ParseException
  {
    return parseUpperCaseName(null);
  }

  private String parseUpperCaseName(final String prefix)
    throws IOException, ParseException
  {
    final StringBuilder buffer = new StringBuilder();
    while (true) {
      final int code = getNextCharacter();
      if (code >= 'A' && code <= 'Z') {
        final char ch = (char) code;
        buffer.append(ch);
      } else {
        putback(code);
        break;
      }
    }
    if (buffer.length() > 0) {
      return buffer.toString();
    } else if (prefix == null) {
      throw createBadNameException("");
    } else {
      throw createBadNameException(prefix + "-");
    }
  }


  //#########################################################################
  //# Storing Results
  private void storeTextFragment(final StringBuilder buffer)
  {
    final String text = buffer.toString();
    final TemplateFragment fragment =
      new TemplateFragmentText(text, mLineNo);
    mCurrentSequence.addFragment(fragment);
  }

  private void storeVariableFragment(final String name, final int numhats)
  {
    final TemplateFragment fragment =
      new TemplateFragmentVariable(name, numhats, mLineNo);
    mCurrentSequence.addFragment(fragment);
  }

  private void openForeachFragment(final String name, final int numhats)
  {
    final TemplateFragmentSequence parent = mCurrentSequence;
    pushCurrentContainer();
    mCurrentSequence = new TemplateFragmentSequence();
    mCurrentContainer =
      new TemplateFragmentForeach(name, mCurrentSequence, numhats, mLineNo);
    parent.addFragment(mCurrentContainer);
  }

  private void closeForeachFragment()
    throws ParseException
  {
    if (mCurrentContainer instanceof TemplateFragmentForeach) {
      popCurrentContainer();
    } else {
      throw createParseException("$ENDFOR without preceeding $FOREACH!");
    }
  }

  private void openConditionalFragment(final String name, final int numhats)
  {
    final TemplateFragmentSequence parent = mCurrentSequence;
    pushCurrentContainer();
    mCurrentSequence = new TemplateFragmentSequence();
    mCurrentContainer = 
      new TemplateFragmentConditional(name, mCurrentSequence,
                                      numhats, mLineNo);
    parent.addFragment(mCurrentContainer);
  }

  private void openConditionalAlternative()
    throws ParseException
  {
    final TemplateFragmentConditional conditional = verifyElseParent();
    mCurrentSequence = new TemplateFragmentSequence();
    conditional.setElse(mCurrentSequence);
  }

  private void openConditionalAlternative(final String name, final int numhats)
    throws ParseException
  {
    final TemplateFragmentConditional conditional = verifyElseParent();
    mCurrentSequence = new TemplateFragmentSequence();
    mCurrentContainer = 
      new TemplateFragmentConditional(name, mCurrentSequence,
                                      numhats, mLineNo);
    conditional.setElse(mCurrentContainer);
  }

  private void closeConditionalFragment()
    throws ParseException
  {
    if (mCurrentContainer instanceof TemplateFragmentConditional) {
      popCurrentContainer();
    } else {
      throw createParseException("$ENDIF without preceeding $IF!");
    }
  }

  private TemplateFragmentConditional verifyElseParent()
    throws ParseException
  {
    if (mCurrentContainer instanceof TemplateFragmentConditional) {
      final TemplateFragmentConditional conditional =
        (TemplateFragmentConditional) mCurrentContainer;
      if (conditional.getElse() == null) {
        return conditional;
      }
    }
    throw createParseException("$ELSE without preceeding $IF!");
  }

  private void pushCurrentContainer()
  {
    final StackEntry entry =
      new StackEntry(mCurrentSequence, mCurrentContainer);
    mFragmentStack.add(0, entry);
  }

  private void popCurrentContainer()
  {
    if (mFragmentStack.isEmpty()) {
      throw new IllegalStateException("Popping from empty stack!");
    } else {
      final StackEntry entry = (StackEntry) mFragmentStack.remove(0);
      mCurrentSequence = entry.getSequence();
      mCurrentContainer = entry.getContainer();
    }
  }


  //#########################################################################
  //# Stream Reading
  private void skipWhiteSpace()
    throws IOException
  {
    skipWhiteSpace(false);
  }

  private void skipWhiteSpace(final boolean skipnl)
    throws IOException
  {
    while (true) {
      final int code = getNextCharacter();
      switch (code) {
      case ' ':
      case '\t':
        break;
      case '\n':
        if (!skipnl) {
          return;
        }
        break;
      default:
        putback(code);
        return;
      }
    }
  }

  private int getNextCharacter()
    throws IOException
  {
    if (mPutbackCharacter == -2) {
      final int ch = mReader.read();
      if (ch == '\n') {
        mLineNo++;
      }
      return ch;
    } else {
      final int ch = mPutbackCharacter;
      mPutbackCharacter = -2;
      return ch;
    }
  }

  private void putback(final int ch)
  {
    if (mPutbackCharacter == -2) {
      mPutbackCharacter = ch;
    } else {
      throw new IllegalStateException
        ("Trying to put back more than one character!");
    }
  }


  //#########################################################################
  //# Error Handling
  private ParseException createBadNameException(final String name)
  {
    return createParseException("Bad instruction name '$" + name + "'!");
  }


  //#########################################################################
  //# Local Class StackEntry
  private static class StackEntry {

    //#######################################################################
    //# Constructors
    private StackEntry(final TemplateFragmentSequence seq,
                       final TemplateFragment container)
    {
      mSequence = seq;
      mContainer = container;
    }

    //#######################################################################
    //# Simple Access
    private TemplateFragmentSequence getSequence()
    {
      return mSequence;
    }

    private TemplateFragment getContainer()
    {
      return mContainer;
    }

    //#######################################################################
    //# Simple Access
    private final TemplateFragmentSequence mSequence;
    private final TemplateFragment mContainer;

  }


  //#########################################################################
  //# Data Members
  private final Reader mReader;
  private final List<StackEntry> mFragmentStack;

  private TemplateFragmentSequence mCurrentSequence;
  private TemplateFragment mCurrentContainer;
  private int mPutbackCharacter;
  private int mLineNo;

}
