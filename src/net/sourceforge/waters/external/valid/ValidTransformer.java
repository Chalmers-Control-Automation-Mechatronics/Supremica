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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;


class ValidTransformer
  extends Thread
  implements URIResolver
{

  //#########################################################################
  //# Constructor
  ValidTransformer(final URI vmodfile)
    throws IOException,
	   TransformerConfigurationException
  {
    // Get the directory and open the input file
    mPath = vmodfile;

    // Create an XSL transformer
    final ClassLoader loader = currentThread().getContextClassLoader();
    final InputStream stream = loader.getResourceAsStream
      ("net/sourceforge/waters/external/valid/vw.xsl");
    final Source xslsource = new StreamSource(stream);
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.setURIResolver(this);
    mTransformer = factory.newTransformer(xslsource);
    stream.close();

    // Create a preprocessor task to remove the DOCTYPE information
    mPreprocessor = new InputPreprocessor(vmodfile);

    // Create a pipe for the output
    mOutPipe = new PipedOutputStream();

    // Provide a SAXSource for the other end of the pipe
    final PipedInputStream pipedInput = new PipedInputStream(mOutPipe);
    mSource = new InputSource(pipedInput);
  }


  //#########################################################################
  //# Thread Body
  @Override
  public void run()
  {
    try {
      final Source source = mPreprocessor.getSource();
      final StreamResult result = new StreamResult(mOutPipe);
      mPreprocessor.start();
      mTransformer.transform(source, result);
    } catch (final TransformerException exception) {
      mException = exception;
    } finally {
      closeOutPipe();
    }
  }


  //#########################################################################
  //# Interface javax.xml.transform.URIResolver
  @Override
  public Source resolve(final String href, final String base)
    throws TransformerException
  {
    try {
      final URI uri = mPath.resolve(href);
      final InputPreprocessor preprocessor = new InputPreprocessor(uri);
      preprocessor.start();
      return preprocessor.getSource();
    } catch (final IOException exception) {
      throw new TransformerException(exception);
    }
  }


  //#########################################################################
  //# Accessing the Outputs
  InputSource getSource()
  {
    return mSource;
  }

  Exception getException()
  {
    return mException;
  }


  //#########################################################################
  //# Closing Streams
  private void closeOutPipe()
  {
    try {
      if (mOutPipe != null) {
	mOutPipe.close();
      }
    } catch (final IOException exception) {
      if (mException == null) {
	mException = exception;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final URI mPath;
  private final Transformer mTransformer;
  private final InputPreprocessor mPreprocessor;
  private final PipedOutputStream mOutPipe;
  private final InputSource mSource;

  private Exception mException;

}
