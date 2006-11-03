//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   InputPreprocessor
//###########################################################################
//# $Id: InputPreprocessor.java,v 1.5 2006-11-03 15:01:56 torda Exp $
//###########################################################################


package net.sourceforge.waters.valid;

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
