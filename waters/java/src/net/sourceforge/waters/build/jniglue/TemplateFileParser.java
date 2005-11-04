//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFileParser
//###########################################################################
//# $Id: TemplateFileParser.java,v 1.3 2005-11-04 02:21:17 robi Exp $
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
    StringBuffer buffer = new StringBuffer();
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
          buffer = new StringBuffer();
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
    final StringBuffer buffer = new StringBuffer();
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
  private void storeTextFragment(final StringBuffer buffer)
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
