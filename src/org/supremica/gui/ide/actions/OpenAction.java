//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: OpenAction.java,v 1.8 2005-03-24 10:09:16 torda Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;

import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import org.supremica.gui.FileDialogs;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;



public class OpenAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public OpenAction(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Open...");
		putValue(Action.SHORT_DESCRIPTION, "Open a new project");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(Action.SMALL_ICON,
				 new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		final FileImporter importer = new WatersFileImporter();
	}



	//#######################################################################
	//# Local Class WatersFileImporter
	private class WatersFileImporter extends FileImporter
	{

		//###################################################################
		//# Constructors
		private WatersFileImporter()
		{
			super(FileDialogs.getWatersImportFileChooser(), ide);
		}



		//###################################################################
		//# Overrides for Base Class org.supremica.gui.ide.actions.FileImporter
		void openFile(final File file)
		{
			final String filename = file.getName();
			final String lowername = filename.toLowerCase();
			if (lowername.endsWith(FileDialogs.WMOD_EXT)) {
				openFileWmod(file);
			} else if (lowername.endsWith(FileDialogs.MAINVMOD_EXT)) {
				openFileVmod(file);
			} else if (filename.endsWith(FileDialogs.VPRJ_EXT)) {
				openFileVprj(file);
			}
		}



		//###################################################################
		//# Type Specific Opening Methods
		private void openFileWmod(final File file)
		{
			try	{
				final ProxyMarshaller marshaller = new ModuleMarshaller();
				final ModuleProxy module =
					(ModuleProxy) marshaller.unmarshal(file);
				installContainer(module);
			} catch (final JAXBException exception) {
				showParseError("Could not parse module file", file, exception);
			} catch (final ModelException exception) {
				showParseError("Could not parse module file", file, exception);
			}
		}



		private void openFileVprj(final File vprjfile)
		{
			final String vprjname = vprjfile.getName();
			final StringBuffer buffer = new StringBuffer(vprjname);
			final int len = buffer.length();
			final int start = len - FileDialogs.VPRJ_EXT.length();
			buffer.delete(start, len);
			buffer.append(FileDialogs.MAINVMOD_EXT);
			final String vmodname = buffer.toString();
			final File dir = vprjfile.getParentFile();
			final File vmodfile = new File(dir, vmodname);
			if (vmodfile.exists()) {
				openFileVmod(vmodfile);
			} else {
				buffer.delete(0, buffer.length());
				buffer.append
					("Can't import VALID project: main module file '");
				buffer.append(vmodfile.toString());
				buffer.append("' not found!");
				final String shown = buffer.toString();
				JOptionPane.showMessageDialog(ide.getFrame(), shown);
			}
		}



		private void openFileVmod(final File file)
		{
			try {
				final ValidUnmarshaller unmarshaller = new ValidUnmarshaller();
				final ModuleProxy module = unmarshaller.unmarshal(file);
				installContainer(module);
			} catch (final IOException exception) {
				showParseError
					("Error importing from VALID module", file, exception);
			} catch (final JAXBException exception) {
				showParseError
					("Error importing from VALID module", file, exception);
			} catch (final ModelException exception) {
				showParseError
					("Error importing from VALID module", file, exception);
			} catch (final TransformerConfigurationException exception) {
				showParseError
					("Error importing from VALID module", file, exception);
			}
		}



		//###################################################################
		//# Auxiliary Methods
		private void installContainer(final ModuleProxy module)
		{
			ModuleContainer moduleContainer = new ModuleContainer(ide.getIDE(), module);
			ide.add(moduleContainer);
			ide.setActive(moduleContainer);
		}



		//###################################################################
		//# Error Handling
		private void showParseError(final String msg,
									final File file,
									final Exception exception)
		{
			final StringBuffer buffer = new StringBuffer(msg);
			buffer.append(" '");
			buffer.append(file.toString());
			buffer.append("' - ");
			buffer.append(exception.getMessage());
			final String shown = buffer.toString();
			JOptionPane.showMessageDialog(ide.getFrame(), shown);
		}

	}

}
