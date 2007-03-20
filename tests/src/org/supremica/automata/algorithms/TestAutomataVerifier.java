/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.minimization.*;
import org.supremica.automata.IO.*;
import org.supremica.testcases.Arbiter;

public class TestAutomataVerifier
	extends TestCase
{
	public TestAutomataVerifier(String name)
	{
		super(name);
	}

	/**
	 * Sets up the test fixture.
	 * Called before every test case method.
	 */
	protected void setUp()
	{
	}

	/**
	 * Tears down the test fixture.
	 * Called after every test case method.
	 */
	protected void tearDown()
	{
	}

	/**
	 * Assembles and returns a test suite
	 * for all the test methods of this test case.
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite(TestAutomataVerifier.class);
		return suite;
	}

	public void testModularControllable()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Verriegel3));
			SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
			VerificationOptions verificationOptions = new VerificationOptions();
			verificationOptions.setVerificationType(VerificationType.CONTROLLABILITY);
			AutomataVerifier theVerifier = new AutomataVerifier(theProject, verificationOptions, 
																synchronizationOptions, null);
			assertTrue(theVerifier.verify());
			// The same test again (hopefully)
			assertTrue(AutomataVerifier.verifyModularControllability(theProject));
			assertTrue(AutomataVerifier.verifyCompositionalControllability(theProject));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testModularUncontrollable()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Verriegel3Uncontrollable));
			SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
			VerificationOptions verificationOptions = new VerificationOptions();
			verificationOptions.setVerificationType(VerificationType.CONTROLLABILITY);
			AutomataVerifier theVerifier = new AutomataVerifier(theProject, verificationOptions, 
																synchronizationOptions, null);
			assertTrue(!theVerifier.verify());
			// The same test again (hopefully)
			assertTrue(!AutomataVerifier.verifyModularControllability(theProject));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testMonolithicNonblocking()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.SimpleManufacturingExample));

			assertTrue(AutomataVerifier.verifyMonolithicNonblocking(theProject));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	public void testMonolithicBlocking()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.AutomaticCarParkGate));

			assertTrue(!AutomataVerifier.verifyMonolithicNonblocking(theProject));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);			
		}
	}

	public void testModularNonblocking()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.SimpleManufacturingExample));

			VerificationOptions verificationOptions;
			SynchronizationOptions synchronizationOptions;
			MinimizationOptions minimizationOptions;
			
			AutomataVerifier verifier;

			verificationOptions = VerificationOptions.getDefaultNonblockingOptions();
			synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
			minimizationOptions = MinimizationOptions.getDefaultNonblockingOptions();
			minimizationOptions.setMinimizationStrategy(MinimizationStrategy.MostStatesFirst);
			minimizationOptions.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
 			verifier = new AutomataVerifier(theProject, verificationOptions, synchronizationOptions, minimizationOptions);
			assertTrue(verifier.verify());

			minimizationOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
			minimizationOptions.setMinimizationHeuristic(MinimizationHeuristic.MostLocal);
			verifier = new AutomataVerifier(theProject, verificationOptions, synchronizationOptions, minimizationOptions);
			assertTrue(verifier.verify());

			minimizationOptions.setMinimizationStrategy(MinimizationStrategy.AtLeastOneLocal);
			minimizationOptions.setMinimizationHeuristic(MinimizationHeuristic.FewestAutomata);
			verifier = new AutomataVerifier(theProject, verificationOptions, synchronizationOptions, minimizationOptions);
			assertTrue(verifier.verify());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);			
		}
	}

	public void testCompositionalNonblockingArbiter()
	{
		// Arbiter example, turned out not to work for some sizes (22,
		// 24, 32, 33) at one point so it is now a testcase...
		try
		{
			for (int i=22; i<=22; i++)
			{
				Arbiter arbiter = new Arbiter(i, false);
				Project theProject = arbiter.getProject();
				
				assertTrue(AutomataVerifier.verifyCompositionalNonblocking(theProject));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);			
		}
	}

	public void testCompositionalBlocking()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.AutomaticCarParkGate));

			assertTrue(!AutomataVerifier.verifyCompositionalNonblocking(theProject));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);			
		}
	}

	public void testModularLanguageInclusion()
	{
	 	try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Verriegel3LanguageInclusion));
			SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
			VerificationOptions verificationOptions = new VerificationOptions();
			verificationOptions.setVerificationType(VerificationType.LANGUAGEINCLUSION);
			AutomataVerifier theVerifier = new AutomataVerifier(theProject, verificationOptions, 
																synchronizationOptions, null);
			Automata inclusionAutomata = new Automata(theProject,true);
			inclusionAutomata.removeAutomaton("sicherheit_vr3");
			Automata targetAutomata = new Automata(theProject.getAutomaton("sicherheit_vr3"));
			verificationOptions.setInclusionAutomata(inclusionAutomata);
			assertTrue(theVerifier.verify());
			// The same test again (hopefully)
			assertTrue(AutomataVerifier.verifyModularInclusion(inclusionAutomata, targetAutomata));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}		
	}
	
	public void testModularLanguageExclusion()
	{
		try
		{
			ProjectBuildFromXML builder = new ProjectBuildFromXML();
			Project theProject = builder.build(TestFiles.getFile(TestFiles.Verriegel3LanguageExclusion));
			SynchronizationOptions synchronizationOptions = new SynchronizationOptions();
			VerificationOptions verificationOptions = new VerificationOptions();
			verificationOptions.setVerificationType(VerificationType.LANGUAGEINCLUSION);
			AutomataVerifier theVerifier = new AutomataVerifier(theProject, verificationOptions, 
																synchronizationOptions, null);
			Automata inclusionAutomata = new Automata(theProject,true);
			inclusionAutomata.removeAutomaton("sicherheit_er");
			Automata targetAutomata = new Automata(theProject.getAutomaton("sicherheit_er"));
			verificationOptions.setInclusionAutomata(inclusionAutomata);
			assertTrue(!theVerifier.verify());
			// The same test again (hopefully)
			assertTrue(!AutomataVerifier.verifyModularInclusion(inclusionAutomata, targetAutomata));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);
		}		
	}

	/*
	public void testGrande()
	{
		try
		{
			ProjectBuildFromXml builder = new ProjectBuildFromXml();
			Project theProject = builder.build(new java.io.File("/users/s2/flordal/AIP_minus_AS3_TU4.xml"));

			VerificationOptions verificationOptions;
			SynchronizationOptions synchronizationOptions;
			MinimizationOptions minimizationOptions;
			
			AutomataVerifier verifier;

			verificationOptions = VerificationOptions.getDefaultNonblockingOptions();
			synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
			minimizationOptions = MinimizationOptions.getDefaultNonblockingOptions();
			minimizationOptions.setMinimizationStrategy(MinimizationStrategy.FewestTransitionsFirst);
			verifier = new AutomataVerifier(theProject, verificationOptions, synchronizationOptions, minimizationOptions);
			assertTrue(verifier.verify());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			assertTrue(false);			
		}
	}
	*/
}
