//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   GlueCompiler
//###########################################################################
//# $Id: GlueCompiler.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class GlueCompiler {

  //#########################################################################
  //# Main Routine
  public static void main(final String[] args)
  {
    if (args.length != 4) {
      System.err.println("USAGE: java " + GlueCompiler.class.getName() +
			 " <indir> <outdir> <srcdir> <jarfile>");
      System.exit(1);
    }
    final File indirname = new File(args[0]);
    final File outdirname = new File(args[1]);
    final File srcdirname = new File(args[2]);
    final File jarfilename = new File(args[3]);
    final GlueCompiler compiler =
      new GlueCompiler(indirname, outdirname, srcdirname, jarfilename);
    try {
      compiler.compile();
      System.exit(0);
    } catch (final AbortException exception) {
      System.err.println("Errors occurred.");
      System.exit(1);
    } catch (final IOException exception) {
      System.err.println("FATAL: " + exception.getMessage());
      System.exit(1);
    } catch (final Throwable exception) {
      System.err.println("PROGRAM ERROR:");
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }


  //#########################################################################
  //# Constructors
  private GlueCompiler(final File indirname,
		       final File outdirname,
		       final File srcdirname,
		       final File jarfilename)
  {
    mInDirName = indirname;
    mOutDirName = outdirname;
    mSourceDirName = srcdirname;
    mClassFileName = new File(mInDirName, CLASSLIST);
    final long classfiletime = mClassFileName.lastModified();
    final long jarfiletime = jarfilename.lastModified();
    if (classfiletime > jarfiletime) {
      mMainInputTime = classfiletime;
    } else {
      mMainInputTime = jarfiletime;
    }
    mTemplateCache = new HashMap(8);
    mOutputNames = new HashSet();
    mNumWritten = 0;
    mClasses = null;
  }


  //#########################################################################
  //# Compilation
  private void compile()
    throws AbortException, IOException
  {
    final GlueFileParser parser = new GlueFileParser(mClassFileName);
    mClasses = parser.parse();
    parser.close();
    if (mClasses == null) {
      throw new AbortException();
    }

    compileTemplate(TEMPLATE_GLUE_H);
    compileTemplate(TEMPLATE_GLUE_CPP);

    final Collection classes = mClasses.getClasses();
    final Iterator iter = classes.iterator();
    while (iter.hasNext()) {
      final ClassGlue glue = (ClassGlue) iter.next();
      if (glue.includesFullImplementation()) {
	final String name = glue.getCppClassName();
	final String hname = name + ".h";
	final String cppname = name + ".cpp";
	compileTemplate(TEMPLATE_PLAIN_H, hname, glue);
	compileTemplate(TEMPLATE_PLAIN_CPP, cppname, glue);
      }
    }

    final FilenameFilter filter = new CppFilenameFilter();
    final String[] filenames = mOutDirName.list(filter);
    for (int i = 0; i < filenames.length; i++) {
      final String name = filenames[i];
      if (!mOutputNames.contains(name)) {
	final File victim = new File(mOutDirName, name);
	mNumWritten++;
	System.err.println("Deleting " + name + " ...");
	if (!victim.delete()) {
	  System.err.println("WARNING: Could not delete file!");
	}
      }
    }

    if (mNumWritten == 0) {
      System.err.println("All files up to date.");
    }
  }

  private void compileTemplate(final String name)
    throws AbortException, IOException
  {
    compileTemplate(name, name, mClasses);
  }

  private void compileTemplate(final String inname,
			       final String outname,
			       final FileWritableGlue glue)
    throws AbortException, IOException
  {
    final File templatefilename = new File(mInDirName, inname);
    final File outfilename = new File(mOutDirName, outname);
    final long outfiletime = outfilename.lastModified();
    mOutputNames.add(outname);
    if (outfiletime != 0 &&
	outfiletime > mMainInputTime &&
	outfiletime > templatefilename.lastModified() &&
	glue.isUpToDate(mSourceDirName, outfiletime)) {
      return;
    } else {
      System.err.println("Generating " + outname + " ...");
      mNumWritten++;
    }
    final Template template = getTemplate(templatefilename);
    final CppGlueWriter generator =
      new CppGlueWriter(outfilename, glue, template);
    final boolean success = generator.generate();
    generator.close();
    if (!success) {
      throw new AbortException();
    }
  }

  private Template getTemplate(final File templatefilename)
    throws AbortException, IOException
  {
    Template template = (Template) mTemplateCache.get(templatefilename);
    if (template == null) {
      final TemplateFileParser parser =
	new TemplateFileParser(templatefilename);
      template = parser.parse();
      parser.close();
      if (template == null) {
	throw new AbortException();
      }
      mTemplateCache.put(templatefilename, template);
    }
    return template;
  }


  //#########################################################################
  //# Local Class AbortException
  private class AbortException extends Exception {
  }


  //#########################################################################
  //# Local Class CppFilenameFiler
  private class CppFilenameFilter implements FilenameFilter {

    //#######################################################################
    //# Interface java.io.FilenameFilter FilenameFilter
    public boolean accept(final File dir, final String name)
    {
      return name.endsWith(".cpp") || name.endsWith(".h");
    }

  }


  //#########################################################################
  //# Data Members
  private final File mInDirName;
  private final File mOutDirName;
  private final File mSourceDirName;
  private final File mClassFileName;
  private final long mMainInputTime;
  private final Map mTemplateCache;
  private final Collection mOutputNames;

  private int mNumWritten;
  private ClassGlueCollection mClasses;

  //#########################################################################
  //# Class Constants
  private static final String CLASSLIST = "Classes.txt";
  private static final String TEMPLATE_GLUE_H = "Glue.h";
  private static final String TEMPLATE_GLUE_CPP = "Glue.cpp";
  private static final String TEMPLATE_PLAIN_H = "PlainGlue.h";
  private static final String TEMPLATE_PLAIN_CPP = "PlainGlue.cpp";

}
