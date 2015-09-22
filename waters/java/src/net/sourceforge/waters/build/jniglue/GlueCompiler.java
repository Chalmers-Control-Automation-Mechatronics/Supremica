//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
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
    mTemplateCache = new HashMap<File,Template>(8);
    mOutputNames = new HashSet<String>();
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

    final Collection<ClassGlue> classes = mClasses.getClasses();
    for (final ClassGlue glue : classes) {
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
	public static final long serialVersionUID = 1;
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
  private final Map<File,Template> mTemplateCache;
  private final Collection<String> mOutputNames;

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








