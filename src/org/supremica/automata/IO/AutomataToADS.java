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
import org.supremica.gui.SaveFileChooser;

// import javax.swing.JFileChooser;
// import java.util.concurrent.atomic.AtomicBoolean;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;

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
			final AutomatonToADS toADS = new AutomatonToADS(currAutomaton, eventMap);
			final TextFrame textframe = new TextFrame("ADS debug output");

			toADS.serialize(textframe.getPrintWriter());
		}
		return;
	}

	public void doSave() throws Exception
	{
		final SaveFileChooser sfc = new SaveFileChooser(AutomatonToADS.getFileFilter());

		for(final Automaton currAutomaton : this.automata)
		{
			sfc.setDialogTitle("Save " + currAutomaton.getName() + " as ...");
			sfc.setSelectedFile(currAutomaton.getName());

			int ret = sfc.showSaveDialog(IDE.getTheIDE());
			if(ret == SaveFileChooser.APPROVE_OPTION)
			{
				final File currFile = sfc.getSelectedFile();
				if (currFile != null && !currFile.isDirectory())
				{
					final AutomatonToADS toADS = new AutomatonToADS(currAutomaton, eventMap);
					toADS.serialize(currFile.getAbsolutePath());
				}
			}
			else // Cancel or Closed was clicked, act accordingly
			{
				if(ret == SaveFileChooser.ERROR_OPTION) // then it was Close, abort the rest
				{
					return;
				}
			}
		}
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
/***
// Specific version of a FileChooser that distinguishes between Cancel and Close
// so different actions can be taken for each of them
// Clicking Cancel returns JFileChooser.CANCEL_OPTION
// Clicking Close returns JFileChooser.ERROR_OPTION
class SaveFileChooser
{
	private JFileChooser jfc;
	private StandardExtensionFileFilter filter;
	private AtomicBoolean cancelClicked;

	public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
	public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
	public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

	public SaveFileChooser(final StandardExtensionFileFilter filter)
	{
		this.filter = filter;
		this.jfc = new JFileChooser();
		//// Some vibe coding to distinguish between Cancel and Close
		// 1. Thread-safe object wrapper that works inside Lambdas
		this.cancelClicked = new AtomicBoolean(false);
		// 2. Listen for the internal Cancel button action
		jfc.addActionListener(e ->
		{
			if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand()))
			{
				cancelClicked.set(true);
			}
		});
		//// End of vide coding
        jfc.resetChoosableFileFilters();
        jfc.setFileFilter(filter);
	}

	public void setDialogTitle(final String dlgTitle)
	{
		jfc.setDialogTitle(dlgTitle);
	}
	public int showSaveDialog(java.awt.Component parent)
	{
		this.cancelClicked.set(false);
		if(jfc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			return JFileChooser.APPROVE_OPTION;
		}
		else // either Cancel or Close
		{
			if(this.cancelClicked.get())
			{
				return JFileChooser.CANCEL_OPTION;
			}
			else // Close was clicked - not sure this is the best return value, but for now...
			{
				return JFileChooser.ERROR_OPTION;
			}
		}
	}
	public void setSelectedFile(final String name)
	{
		jfc.setSelectedFile(new java.io.File(name + this.filter.getExtension()));
	}
	public java.io.File getSelectedFile()
	{
		return jfc.getSelectedFile();
	}
}
***/