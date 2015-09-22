//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.io.IOException;
import java.io.Reader;


class GlueFileScanner {

  //#########################################################################
  //# Constructors
  GlueFileScanner(final Reader reader)
  {
    mReader = reader;
    mLineNo = 1;
    mPutbackCharacter = -2;
  }


  //#########################################################################
  //# Scanning
  Token nextToken()
    throws IOException, ParseException
  {
    int code = getNextTokenCharacter();
    switch (code) {
    case -1:
      return TokenTable.T_EOF;
    case '{':
      return TokenTable.T_OPENBRACE;
    case '}':
      return TokenTable.T_CLOSEBRACE;
    case '(':
      return TokenTable.T_OPENPAREN;
    case ')':
      return TokenTable.T_CLOSEPAREN;
    case ',':
      return TokenTable.T_COMMA;
    case ';':
      return TokenTable.T_SEMICOLON;
    case '.':
      return TokenTable.T_DOT;
    }
    char ch = (char) code;
    if (Character.isJavaIdentifierStart(ch)) {
      final StringBuilder buffer = new StringBuilder();
      buffer.append(ch);
      while (true) {
	code = getNextCharacter();
	if (code == -1) {
	  break;
	}
	ch = (char) code;
	if (Character.isJavaIdentifierPart(ch)) {
	  buffer.append(ch);
	} else {
	  break;
	}
      }
      putback(code);
      final String text = buffer.toString();
      return TokenTable.createToken(text);
    } else if (ch >= 32) {
      throw new ParseException
	("Illegal character '" + (char) ch + "'!", mLineNo);
    } else {
      throw new ParseException
	("Illegal character code " + ch + "!", mLineNo);
    }
  }

  int getLineNo()
  {
    return mLineNo;
  }

  void close()
    throws IOException
  {
    mReader.close();
  }


  //#########################################################################
  //# Stream Reading
  private int getNextTokenCharacter()
    throws IOException
  {
    int code;
    char ch;
    do {
      code = getNextCharacter();
      if (code == -1) {
	return code;
      } else if (code == '/') {
	int nextcode = getNextCharacter();
	switch (nextcode) {
	case '/':
	  do {
	    code = getNextCharacter();
	    if (code == -1) {
	      return code;
	    }
	  } while (code != '\n');
	  break;
	case '*':
	  do {
	    code = getNextCharacter();
	    switch (code) {
	    case -1:
	      return code;
	    case '*':
	      nextcode = getNextCharacter();
	      if (nextcode != '/') {
		putback(nextcode);
	      }
	    }
	  } while (nextcode != '/');
	  code = ' ';
	  break;
	default:
	  putback(nextcode);
	  return code;
	}
      }
      ch = (char) code;
    } while (Character.isWhitespace(ch));
    return code;
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
  //# Data Members
  private final Reader mReader;

  private int mLineNo;
  private int mPutbackCharacter;

}







