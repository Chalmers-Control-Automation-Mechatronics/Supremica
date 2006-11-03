//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidTransformer
//###########################################################################
//# $Id: ValidTransformer.java,v 1.3 2006-11-03 15:01:56 torda Exp $
//###########################################################################


package net.sourceforge.waters.valid;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
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
    final InputStream stream =
      loader.getResourceAsStream("net/sourceforge/waters/valid/vw.xsl");
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
    final PipedInputStream pinput = new PipedInputStream(mOutPipe);
    final InputSource psource = new InputSource(pinput);
    mSource = new SAXSource(psource);
  }


  //#########################################################################
  //# Thread Body
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
  Source getSource()
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
  private final Source mSource;

  private Exception mException;

}
