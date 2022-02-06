/*
 * SATAutomata.java
 *
 * Created on den 19 september 2007, 16:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;

import java.io.File;

import net.sourceforge.fsa2sat.BlackBox;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.ProjectBuildFromXML;

import org.sat4j.specs.TimeoutException;
/**
 *
 * @author voronov
 */
public class SATAutomata {

    Automata automata;
    BlackBox bb = new BlackBox();

    /** Creates a new instance of SATAutomata
     */
    public SATAutomata(final Automata theAutomata)
    {
        automata = theAutomata;
    }

    public boolean isControllable(final int steps) throws TimeoutException{
        return bb.isControllable(convertAts(automata), steps);
    }
    public boolean isControllableByInduction() throws TimeoutException{
        return bb.isControllableByInduction(convertAts(automata));
    }
    public boolean markedStateIsReachable(final int steps) throws TimeoutException{
        return bb.markedStateIsReachable(convertAts(automata), steps);
    }


    private net.sourceforge.fsa2sat.fsa.Automata convertAts(final Automata ats){
        return (new ConverterSupToSatFsa()).convert(ats);
    }

    public static void main(final String[] args) throws Exception {
        check((new SATAutomata(atsFromFilename("agv.xml"))).isControllable(5), false);
        check((new SATAutomata(atsFromFilename("toaster.xml"))).isControllable(5), false);
        check((new SATAutomata(atsFromFilename("two_contr.xml"))).isControllable(5), true);

        check((new SATAutomata(atsFromFilename("agv.xml"))).isControllableByInduction(), false);
        check((new SATAutomata(atsFromFilename("toaster.xml"))).isControllableByInduction(), false);
        check((new SATAutomata(atsFromFilename("two_contr.xml"))).isControllableByInduction(), true);

        check((new SATAutomata(atsFromFilename("two-b.xml"))).markedStateIsReachable(5), true);
        check((new SATAutomata(atsFromFilename("two-c.xml"))).markedStateIsReachable(5), false);
        check((new SATAutomata(atsFromFilename("two-d.xml"))).markedStateIsReachable(8), true);
        check((new SATAutomata(atsFromFilename("two-d.xml"))).markedStateIsReachable(3), false);
        check((new SATAutomata(atsFromFilename("one-long.xml"))).markedStateIsReachable(8), true);
        check((new SATAutomata(atsFromFilename("one-long.xml"))).markedStateIsReachable(2), false);
    }

    private static void check(final boolean actual, final boolean expected){
        System.out.println((expected==actual)?"OK":"FAIL");
    }

    private static Automata atsFromFilename(final String fileName) throws Exception{
        return (new ProjectBuildFromXML()).build(new File(fileName));
    }
}
