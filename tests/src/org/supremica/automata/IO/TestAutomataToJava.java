//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   TestAutomataToJava
//###########################################################################
//# $Id$
//###########################################################################


/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain However, it is freely
 * available without fee for education, research, and non-profit purposes. By
 * obtaining copies of this and other files that comprise the Supremica
 * software, you, the Licensee, agree to abide by the following conditions and
 * understandings with respect to the copyrighted software:
 *
 * The software is copyrighted in the name of Supremica, and ownership of the
 * software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its documentation for
 * education, research, and non-profit purposes is hereby granted to Licensee,
 * provided that the copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all such copies, and
 * that no charge be made for such copies. Any entity desiring permission to
 * incorporate this software into commercial products or to use it for
 * commercial purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org Supremica, Haradsgatan 26A 431 42
 * Molndal SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are available.
 *
 * Licensee may not use the name, logo, or any other symbol of Supremica nor the
 * names of any of its employees nor any adaptation thereof in advertising or
 * publicity pertaining to the software without specific prior written approval
 * of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THE
 * SOFTWARE FOR ANY PURPOSE. IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED
 * WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages suffered by Licensee from
 * the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.automata.IO;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.sourceforge.waters.config.Version;

import org.supremica.automata.Project;
import org.supremica.testhelpers.TestFiles;


/**
 * @author torda
 */
public class TestAutomataToJava extends TestCase {

	public TestAutomataToJava(final String name)
	{
		super(name);
	}

	public void testSameAsReferenceFileWithoutSignals()
		throws Exception
	{
		testSameAsReferenceFile("CatAndMouse",
								TestFiles.getFile(TestFiles.CatMouse),
								TestFiles.getFile(TestFiles.CatMouseJava));
	}

	public void disabled_testSameAsReferenceFileWithSignals()
		throws Exception
	{
		testSameAsReferenceFile("BallProcess",
								TestFiles.getFile(TestFiles.BallProcess),
								TestFiles.getFile(TestFiles.BallProcessJava));
	}

	/**
	 * Compares the generated java file with the hand-written reference file
	 */
    public void testSameAsReferenceFile(final String classname, final File projFile,
                                        final File refFile)
      throws Exception
    {
      //Create an empty temporary file that is deleted upon exit
      final File generatedTempJavaFile = File.createTempFile(classname, ".java");
      //generatedTempJavaFile.deleteOnExit();
      //Generate java code into the temporary file from the ball process
      // project
      final Project proj = new ProjectBuildFromXML().build(projFile);
      final AutomataToJava javaExporter = new AutomataToJava(proj, classname);
      final PrintWriter pw = new PrintWriter(new FileWriter(generatedTempJavaFile));
      javaExporter.serialize(pw);
      pw.flush(); // needed!!!
      pw.close();
      // Open a reader for the reference file and the generated file
      // respectively
      LineNumberReader refReader = null;
      LineNumberReader genReader = null;
      try {
        refReader = new LineNumberReader(new FileReader(refFile));
        genReader = new LineNumberReader(new FileReader(generatedTempJavaFile));
        // Compare each line except those where date and version are written
        String refLine = refReader.readLine();
        String genLine = genReader.readLine();
        final Pattern versionPattern =
          Pattern.compile("Supremica version: [0-9]+");
        final Pattern datePattern =
          Pattern.compile("This file was generated at:");
        while (refLine != null && genLine != null) {
          final Matcher versionMatcher = versionPattern.matcher(refLine);
          if (versionMatcher.find()) {
            final String versionInfo = Version.getInstance().toString();
            refLine = versionMatcher.replaceFirst(versionInfo);
          } else if (datePattern.matcher(refLine).find()) {
            assertTrue("Date keyword not found in generated file!",
                       datePattern.matcher(genLine).find());
            refLine = genLine = null;
          }
          assertEquals("Line " + genReader.getLineNumber(), refLine, genLine);
          refLine = refReader.readLine();
          genLine = genReader.readLine();
        }
        assertNull("The reference file is shorter than the generated file!",
                   genLine);
        assertNull("The generated file is longer than the reference file!",
                   refLine);
      } finally {
        if (genReader != null) {
          genReader.close();
        }
        if (refReader != null) {
          refReader.close();
        }
      }
    }

	/**
	 * Assembles and returns a test suite for all the test methods of this test
	 * case.
	 */
	public static Test suite()
	{
		final TestSuite suite = new TestSuite(TestAutomataToJava.class);
		return suite;
	}
}
