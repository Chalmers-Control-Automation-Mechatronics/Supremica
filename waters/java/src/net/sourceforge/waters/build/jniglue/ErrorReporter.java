//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ErrorReporter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;
import java.io.IOException;


abstract class ErrorReporter {

  //#########################################################################
  //# Constructors
  ErrorReporter()
  {
    this(null);
  }

  ErrorReporter(final File filename)
  {
    mInputFileName = filename;
    mNumErrors = 0;
  }


  //#########################################################################
  //# Simple Access
  File getInputFileName()
  {
    return mInputFileName;
  }

  int getNumErrors()
  {
    return mNumErrors;
  }

  int getLineNo()
  {
    return -1;
  }


  //#########################################################################
  //# Error Handling
  ParseException createParseException(final String msg)
  {
    final int lineno = getLineNo();
    return new ParseException(msg, lineno);
  }

  //#########################################################################
  //# Error Recovery
  void reportError(final ParseException exception)
  {
    final String msg = exception.getMessage();
    final int lineno = exception.getLineNo();
    reportError(msg, lineno);
  }

  void reportError(final String msg)
  {
    reportError(msg, getLineNo());
  }

  void reportError(final String msg, final int lineno)
  {
    System.err.print("ERROR in line ");
    System.err.print(lineno);
    if (mInputFileName != null) {
      System.err.print(" of ");
      System.err.print(mInputFileName);
    }
    System.err.println(':');
    System.err.println(msg);
    mNumErrors++;
  }

  void reportError(final IOException exception)
  {
    System.err.print("ERROR while reading");
    if (mInputFileName != null) {
      System.err.print(" from ");
      System.err.print(mInputFileName);
    }
    System.err.println(':');
    System.err.println(exception.getMessage());
    mNumErrors++;
  }


  //#########################################################################
  //# Data Members
  private final File mInputFileName;
  private int mNumErrors;

}