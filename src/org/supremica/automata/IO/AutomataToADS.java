/******************* AutomataToADS.java **********************
 * Conversion from Supremica format to PiTCT .ads format
 * Takes a set of automata and guarantees that the event
 * indices match up across the individual automata.
 * If all events have numeric labels, those labels are used.
 * Called from AnalyzerExportAction
 */
package org.supremica.automata.IO;

import java.util.*;
import java.io.*;
import javax.swing.JFileChooser;
import java.util.concurrent.atomic.AtomicBoolean;

import org.supremica.automata.Arc;
import org.supremica.automata.State;
import org.supremica.automata.Alphabet;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomatonToADS;
import org.supremica.gui.texteditor.TextFrame;
import org.supremica.gui.FileDialogs;
import org.supremica.gui.ide.IDE;

public class AutomataToADS
{
	private final Automata automata;
	private final Map<String, Integer> eventMap;

	public AutomataToADS(final Automata theAutomata)
	{
		this.automata = theAutomata;
		final Alphabet unionAlphabet = theAutomata.getUnionAlphabet();
		this.eventMap = buildEventMap(unionAlphabet);
	}

	public void doDebugView() throws Exception
	{
		for(final Automaton currAutomaton : this.automata)
		{
			final AutomatonToADS exporter = new AutomatonToADS(currAutomaton, eventMap);
			final TextFrame textframe = new TextFrame("ADS debug output");

			exporter.serialize(textframe.getPrintWriter());
		}
		return;
	}

	public void doSave() throws Exception
	{
		for(final Automaton currAutomaton : this.automata)
		{
			JFileChooser fileChooser = FileDialogs.getADSFileExporter(currAutomaton.getName());
			fileChooser.setDialogTitle("Save " + currAutomaton.getName() + " as ...");

			// Some vibe coding to distinguish between Cancel and Close
			// 1. Thread-safe object wrapper that works inside Lambdas
			AtomicBoolean cancelClicked = new AtomicBoolean(false);
			// 2. Listen for the internal Cancel button action
			fileChooser.addActionListener(e ->
			{
            	if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand()))
            	{
            	    cancelClicked.set(true);
            	}
        	});
			// End of vide coding

			if (fileChooser.showSaveDialog(IDE.getTheIDE()) == JFileChooser.APPROVE_OPTION)
			{
				final File currFile = fileChooser.getSelectedFile();
				if (currFile != null && !currFile.isDirectory())
				{
					final AutomatonToADS exporter = new AutomatonToADS(currAutomaton, eventMap);
					exporter.serialize(currFile.getAbsolutePath());
				}
			}
			else // Cancel or Closed was clicked, act accordingly
			{
				if(!cancelClicked.get()) // then it was Close, abort the rest
				{
					return;
				}
			}
			// The above JFileChooser now distinguishes between clicking Cancel,
			// which skips the current save, and Close, which aborts the whole
			// save process skipping the rest of the saves
		}
		return;
	}

	//** Private parts
	private Map<String, Integer> buildEventMap(final Alphabet unionAlphabet)
	{
		final Map<String, Integer> eventMap = new HashMap<>();

		/**
		 * If all event labels are numeric, we assume that that they originally
		 * came from PiTCT .ads files, so we use those labels as they are
		**/
		if(isAllNumeric(unionAlphabet))
		{
			for(final LabeledEvent event : unionAlphabet)
			{
				final String label = event.getLabel();
				eventMap.put(label, Integer.parseInt(label));
			}
		}
		else
		{
			int controllableInt = 11;
			int uncontrollableInt = 10;

			for(final LabeledEvent event : unionAlphabet)
			{
				final String label = event.getLabel();
				if (event.isControllable())
				{
					eventMap.put(label, controllableInt);
					controllableInt += 2;
				}
				else
				{
					eventMap.put(label, uncontrollableInt);
					uncontrollableInt += 2;
				}
			}
		}

		return eventMap;
	}

	private boolean isAllNumeric(final Alphabet alpha)
	{
		/*
		 * If all events are already labeled by numeric strings, these automata (probably)
		 * came from a bunch of .ads files, then the numerics should be retained.
		 * So we need to know.
		 */
		boolean allEventsNumeric = true;
		for(final LabeledEvent ev : alpha)
		{
			if(!ev.getLabel().chars().allMatch(Character::isDigit))
			{
				allEventsNumeric = false;
				break;
			}
		}
		// System.err.println("allEventsNumeric: " + (allEventsNumeric ? "true" : "false"));
		return allEventsNumeric;
	}
}