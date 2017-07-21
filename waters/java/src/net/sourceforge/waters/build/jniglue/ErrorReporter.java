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
