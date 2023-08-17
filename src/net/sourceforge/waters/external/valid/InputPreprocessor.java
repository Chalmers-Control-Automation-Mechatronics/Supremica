//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.external.valid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;


class InputPreprocessor extends Thread
{

  //#########################################################################
  //# Constructor
  InputPreprocessor(final URI uri)
    throws IOException
  {
    final URL url = uri.toURL();
    final InputStream finput = url.openStream();
    try {
      final PipedOutputStream poutput = new PipedOutputStream();
      final PipedInputStream pinput = new PipedInputStream(poutput);
      final InputSource source = new InputSource(pinput);
      final InputStreamReader reader = new InputStreamReader(finput);
      mReader = new BufferedReader(reader);
      mWriter = new PrintWriter(poutput);
      mSource = new SAXSource(source);
      mException = null;
    } catch (final IOException exception) {
      finput.close();
      closeReader();
      closeWriter();
      throw exception;
    }
  }


  //#########################################################################
  //# Thread Body
  public void run()
  {
    try {
      boolean indoctype = false;
      String line = mReader.readLine();
      while (line != null) {
        final int start = indoctype ? 0 : line.indexOf("<!DOCTYPE");
        if (start >= 0) {
          if (start > 0) {
            final String output = line.substring(0, start);
            mWriter.print(output);
          }
          final int end = line.indexOf('>', start);
          indoctype = end < 0;
          if (!indoctype) {
            line = line.substring(end + 1);
            mWriter.println(line);
            line = mReader.readLine();
            break;
          }
        } else {
          mWriter.println(line);
        }
        line = mReader.readLine();
      }
      while (line != null) {
        mWriter.println(line);
        line = mReader.readLine();
      }
    } catch (final IOException exception) {
      mException = exception;
    } finally {
      closeReader();
      closeWriter();
    }
  }


  //#########################################################################
  //# Accessing the Outputs
  Source getSource()
  {
    return mSource;
  }

  IOException getException()
  {
    return mException;
  }


  //#########################################################################
  //# Closing Streams
  private void closeReader()
  {
    try {
      if (mReader != null) {
        mReader.close();
      }
    } catch (final IOException exception) {
      if (mException == null) {
        mException = exception;
      }
    }
  }

  private void closeWriter()
  {
    if (mWriter != null) {
      mWriter.close();
    }
  }


  //#######################################################################
  //# Data Members
  private final BufferedReader mReader;
  private final PrintWriter mWriter;
  private final Source mSource;
  private IOException mException;

}
