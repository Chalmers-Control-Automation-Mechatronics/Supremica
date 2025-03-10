/********************* Plantifier.java ***********************************
 * Implements the plantifying trick. This was previously part of MinimizationHelper.
 * What it does:
 * Given a set of Automata, for each spec it adds transitions to the dump state
 * on uc-events common with all the plants. Returned is a set of plants that
 * were originally specs, but now have been plantified.
 *
 * Note that in MinimizationHelper, the methods below were static, but since we
 * now return a set of plantified automata, we need to have Plantifier as its
 * own class.
 * // MF
 */
package org.supremica.automata.algorithms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;

/**
 * @author Martin Fabian
 */
public class Plantifier
{
	final static String PLANTIFY_PREFIX = "p:";
    private static Logger logger = LogManager.getLogger(Plantifier.class);

	private final Automata the_automata; // holds the automata to work with
	private final Automata the_plants;   // holds the  plantified automata when done

	public Plantifier(final Automata automata)
	{
		this.the_automata = automata;
		this.the_plants = new Automata();
	}

	public Automata getPlantifiedPlants()
	{
		logger.debug("Plantifier: " + the_plants.size() + " num plantified plants generated");
		return the_plants;
	}
	// Main entry point, this one makes copies and treats supervisors as specs
	public void plantify()
    {
		plantify(true, false);
	}

	// If copy == false, no copies are made, the existing specs are manipulated
	// If sups_as_plants == true, supervisors are treated as plants, not specs
	public void plantify(final boolean copy, final boolean sups_as_plants)
	{
		logger.debug("plantify(Automata) - new version, does not assume spec uc-alpha is subset of plant alpha");

		final Alphabet uc_alpha = new Alphabet();	// collects the uncontrollable PLANT events

		for(final Automaton aut : the_automata)
		{
			if(aut.isPlant() || (sups_as_plants && aut.isSupervisor()))
			{
				uc_alpha.union(aut.getAlphabet().getUncontrollableAlphabet());
			}
		}

		if(uc_alpha.size() == 0)
		{
			logger.info("Number of uncontrollable plant events are zero - nothing really to plantify");
			// return; // We should still rename and make plants of all specs (and supervisors if !sups_as_plants)
		}

        for (final Automaton aut : the_automata)
        {
			if (aut.isSpecification() || (!sups_as_plants && aut.isSupervisor()))
			{
				Automaton a = null;
				if(copy)
					a = new Automaton(aut);
				else
					a = aut;

				plantify(a, uc_alpha);

				the_plants.addAutomaton(a);
			}
        }
    }
	/*
	 * DO NOT USE THIS ONE, it won't do what you want it to (except under certain circumstances)
	 */
	@SuppressWarnings("unused")
	@Deprecated
    private void plantify(final Automaton aut)
    {
		logger.debug("plantify(Automaton) - old version, DO NOT USE THIS (unless you are very sure you know what you do");

		// Don't come here with anything but spec or sup!
		assert(aut.isSpecification() || aut.isSupervisor());

		/* Note that this is really a nasty bug...
		aut.saturateDump(aut.getAlphabet().getUncontrollableAlphabet()); // BUG! nasty, nasty...
		aut.setComment(PLANTIFY_PREFIX + aut.getName());
		aut.setType(AutomatonType.PLANT);
		 */// But I kept it like this as an example for what can happen - just don't use it!
		do_plantify(aut, aut.getAlphabet().getUncontrollableAlphabet()); // The bug is here, this alphabet is not the correct one to pass
    }

	public static void plantify(final Automaton aut, final Alphabet ucAlpha)
    {
		// Don't come here with anything but spec or sup!
		assert(aut.isSpecification() || aut.isSupervisor());
		// And make sure ucAlpha only contains uc-events
		assert(ucAlpha.getUncontrollableAlphabet().equals(ucAlpha));

		// aut.saturateDump(aut.getAlphabet().getUncontrollableAlphabet()); // BUG! nasty, nasty...
		final Alphabet uc_alpha = aut.getAlphabet().getUncontrollableAlphabet();
		uc_alpha.intersect(ucAlpha);
		if(uc_alpha.size() == 0)
		{
			logger.info("No plant uc-events in " + aut.getName() + " - nothing to plantify");
			// return; // We should still rename and make plants of all specs (and supervisors if !sups_as_plants)
		}
		do_plantify(aut, uc_alpha);
    }


    /**
     * Note nasty bug in plantify method... it was fixed by adding the ucAlpha parameter
     * Plantify was assumed to be a local thing, looking at a spec you could determine which uc-events to dump
     * But that assumes that the spec alphabet is included in (or same as) the plant alphabet, which does not always hold!
     * Correct is to only plantify for uc-events in the plant(s) AND the spec!
     **/
	/**
	 * This private (so far) function does the actual plantifying
	 * It does not care anything about where the alphabet comes from, it just odes it
	 * @param aut automaton to plantify
	 * @param uc_alpha set of uc-events on which to add transitions to the dump state
	 */
	private static void do_plantify(final Automaton aut, final Alphabet uc_alpha)
	{
		aut.saturateDump(uc_alpha);
		aut.setName(PLANTIFY_PREFIX + aut.getName()); // Changes the name here, should we really do this if copy == false ??
		aut.setComment(aut.getName());
		aut.setType(AutomatonType.PLANT);
	}
}
