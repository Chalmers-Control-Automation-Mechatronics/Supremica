//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   GlueFileScanner
//###########################################################################
//# $Id$
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
      final StringBuffer buffer = new StringBuffer();
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