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

/**
 * The files in this package are not pure test cases, instead they are used
 * by test cases in other packages. Please, do not store pure test cases in
 * this directory.
 */
package org.supremica.testhelpers;

import java.util.*;
import java.io.*;

public class TestFiles
{
    private static List<TestFiles> collection = new LinkedList<TestFiles>();

    // Sorted alphabetically (for convenience), please comply.  :o)
    public static final TestFiles AGV = new TestFiles("agv.xml");
    public static final TestFiles AutomaticCarParkGate = new TestFiles("AutomaticCarParkGate.xml");
    public static final TestFiles BallProcess = new TestFiles("ballProcess.xml");
    public static final TestFiles BallProcessJava = new TestFiles("BallProcess.java");
    public static final TestFiles BisimulationEquivalence = new TestFiles("bisimulationEquivalence.xml");
    public static final TestFiles CatMouse = new TestFiles("catmouse.xml");
    public static final TestFiles CatMouseJava = new TestFiles("CatAndMouse.java");
    public static final TestFiles CentralLocking3Doors = new TestFiles("centralLocking3Doors.xml");
    public static final TestFiles CircularTable = new TestFiles("circularTable.xml");
    public static final TestFiles ConflictEquivalence = new TestFiles("ConflictEquivalenceExamples.xml");
    public static final TestFiles EOPExample = new TestFiles("EOPExample.xml");
    public static final TestFiles Ex4_5_a = new TestFiles("ex4_5_a.xml");
    public static final TestFiles Ex4_5_b = new TestFiles("ex4_5_b.xml");
    public static final TestFiles FlexibleManufacturingCell = new TestFiles("flexibleManufacturingCell.xml");
    public static final TestFiles FlexibleManufacturingSystem = new TestFiles("flexibleManufacturingSystem.xml");
    public static final TestFiles ILExample = new TestFiles("ILExample.xml");
    public static final TestFiles MachineBufferMachine = new TestFiles("MachineBufferMachine.xml");
    public static final TestFiles NondeterministicComposition = new TestFiles("NondeterministicComposition.xml");
    public static final TestFiles ObservationEquivalence = new TestFiles("observationEquivalence.xml");
    public static final TestFiles RobotAssemblyCell = new TestFiles("robotAssemblyCell.xml");
    public static final TestFiles Verriegel3 = new TestFiles("verriegel3.xml");
    public static final TestFiles Verriegel3LanguageInclusion = new TestFiles("verriegel3_language_inclusion.xml");
    public static final TestFiles Verriegel3LanguageExclusion = new TestFiles("verriegel3_language_exclusion.xml");
    public static final TestFiles Verriegel3Uncontrollable = new TestFiles("verriegel3_uncontrollable.xml");
    public static final TestFiles SimpleManufacturingExample = new TestFiles("SimpleManufacturingExample.xml");
    public static final TestFiles Split = new TestFiles("split.xml");
    public static final TestFiles UMDES_1 = new TestFiles("umdes_test1.fsm");
    public static final TestFiles UMDES_2 = new TestFiles("umdes_test2.fsm");

    private final String filename;

    private TestFiles(final String filename)
    {
        collection.add(this);
        this.filename = filename;
    }

    public static Iterator<TestFiles> iterator()
    {
        return collection.iterator();
    }

    @Override
    public String toString()
    {
        return filename;
    }

    public static Object[] toArray()
    {
        return collection.toArray();
    }

    public static File getFile(final TestFiles theTestFile)
    {
        final String dir = System.getProperty("supremica.test.inputdir");
        return new File(dir, theTestFile.filename);
    }
}
