/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.IO;

import java.io.FileWriter;
import java.io.PrintWriter;
import org.supremica.automata.Automaton;

/**
 *
 * @author Fabian
 */
public class AutomatonToSMC
    implements AutomataSerializer
{

	private final Automaton the_automaton;
	
	public AutomatonToSMC(final Automaton aut)
	{
		this.the_automaton = aut;
	}
	
	@Override
	public void serialize(final PrintWriter pw) throws Exception
	{
		pw.println("SMC output should appear here... eventually");
		
		pw.flush();
		pw.close();		
	}

	@Override
	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}
}
