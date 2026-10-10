/******************************** SaveFileChooser.java *************************
 * Specific version of a FileChooser that distinguishes between Cancel and Close
 * so different actions can be taken for each of them
 * Clicking Cancel returns JFileChooser.CANCEL_OPTION
 * Clicking Close returns JFileChooser.ERROR_OPTION
 *
 * For now, only AutomataToADS uses this file chooser so the user can close
 * the whole save operation half-way through, or just cancel a single save.
 **/
package org.supremica.gui;

import javax.swing.JFileChooser;
import java.util.concurrent.atomic.AtomicBoolean;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;

public class SaveFileChooser
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