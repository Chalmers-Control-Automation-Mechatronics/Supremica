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
/*
 * Created on Apr 14, 2004
 *
 */
package org.supremica.automata.IO;
import java.io.*;
import org.supremica.automata.Project;
import org.supremica.testhelpers.TestFiles;
import junit.framework.*;
/**
 * @author torda
 *
 */
public class TestAutomataToJava extends TestCase {
	public TestAutomataToJava(String name) {
		super(name);
	}
	public void disabled_testSameAsReferenceFileWithoutSignals() {
		testSameAsReferenceFile("CatAndMouse",
				TestFiles.getFile(TestFiles.CatMouse),
				TestFiles.getFile(TestFiles.CatMouseJava));
	}
	public void disabled_testSameAsReferenceFileWithSignals() {
		testSameAsReferenceFile("BallProcess",
				TestFiles.getFile(TestFiles.BallProcess),
				TestFiles.getFile(TestFiles.BallProcessJava));
	}
	/**
	 * Compares the generated java file with the hand-written reference file
	 */
	public void testSameAsReferenceFile(String classname, File projFile,
			File refFile) {
		try {
			//Create an empty temporary file that is deleted upon exit
			File generatedTempJavaFile = File
					.createTempFile(classname, ".java");
			//generatedTempJavaFile.deleteOnExit();
			//Generate java code into the temporary file from the ball process
			// project
			Project proj = new ProjectBuildFromXml().build(projFile);
			AutomataToJava javaExporter = new AutomataToJava(proj,
					classname);
			PrintWriter pw = new PrintWriter(new FileWriter(
					generatedTempJavaFile));
			javaExporter.serialize(pw);
			pw.flush(); //needed!!!
			pw.close();
			//Open a reader for the reference file and the generated file
			// respectively
			LineNumberReader refReader = new LineNumberReader(new FileReader(
					refFile));
			LineNumberReader genReader = new LineNumberReader(new FileReader(
					generatedTempJavaFile));
			//Compare each line except those where date and version are written
			String refLine = refReader.readLine();
			String genLine = genReader.readLine();
			while (refLine != null && genLine != null) {
				if (!(genLine.startsWith(" * Supremica version:") && refLine
						.startsWith(" * Supremica version:"))
						&& !(genLine
								.startsWith(" * This file was generated at:") && refLine
								.startsWith(" * This file was generated at:")))
					assertEquals("Line " + genReader.getLineNumber(), refLine,
							genLine);
				refLine = refReader.readLine();
				genLine = genReader.readLine();
			}
			assertNull("The reference file is shorter than the generated file",
					genLine);
			assertNull(
					"The generated file is shorter than the reference file ",
					refLine);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}
	/**
	 * Assembles and returns a test suite for all the test methods of this test
	 * case.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestAutomataToJava.class);
		return suite;
	}
}