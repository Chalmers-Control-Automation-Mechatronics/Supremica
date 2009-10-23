//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   CppGlueWriter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;


class CppGlueWriter extends ErrorReporter {

  //#########################################################################
  //# Constructors
  CppGlueWriter(final File outfilename,
		final WritableGlue glue,
		final Template template)
    throws FileNotFoundException
  {
    super(template.getInputFileName());
    final OutputStream stream = new FileOutputStream(outfilename);
    mWriter = new PrintWriter(stream);
    mGlueDescription = glue;
    mTemplate = template;
  }

  CppGlueWriter(final PrintWriter writer,
		final WritableGlue glue,
		final Template template)
  {
    super(template.getInputFileName());
    mWriter = writer;
    mGlueDescription = glue;
    mTemplate = template;
  }


  //#########################################################################
  //# Compilation
  boolean generate()
  {
    final TemplateContext context = new TemplateContext();
    final String text = getInputFileName().toString();
    final ProcessorVariable processor = new DefaultProcessorVariable(text);
    context.registerProcessorVariable("INPUTFILE", processor);
    mGlueDescription.registerProcessors(context);
    mTemplate.writeCppGlue(this, context);
    return getNumErrors() == 0;
  }

  void close()
  {
    mWriter.close();
  }


  //#########################################################################
  //# Writing
  void print(final String text)
  {
    mWriter.print(text);
  }

  void println(final String text)
  {
    mWriter.println(text);
  }

  void println()
  {
    mWriter.println();
  }


  //#########################################################################
  //# Data Members
  private final PrintWriter mWriter;
  private final WritableGlue mGlueDescription;
  private final TemplateFragment mTemplate;

}