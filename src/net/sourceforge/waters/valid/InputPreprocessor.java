//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.valid
//# CLASS:   InputPreprocessor
//###########################################################################
//# $Id: InputPreprocessor.java,v 1.1 2005-02-17 01:43:36 knut Exp $
//###########################################################################


package net.sourceforge.waters.valid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;


class InputPreprocessor extends Thread
{

  //#########################################################################
  //# Constructor
  InputPreprocessor(final File filename)
    throws IOException
  {
    final InputStream finput = new FileInputStream(filename);
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
      String line = mReader.readLine();
      while (line != null) {
	if (line.startsWith("<!DOCTYPE ")) {
	  // skip line
	} else {
	  mWriter.println(line);
	}
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