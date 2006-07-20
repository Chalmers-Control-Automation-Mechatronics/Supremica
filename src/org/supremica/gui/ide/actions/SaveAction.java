//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   SaveAction
//###########################################################################
//# $Id: SaveAction.java,v 1.9 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

import org.xml.sax.SAXException;


public class SaveAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	private static Logger logger =
		LoggerFactory.createLogger(SaveAction.class);

	private JFileChooser fileSaveChooser = new JFileChooser(".");

	public SaveAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "Save...");
		putValue(Action.SHORT_DESCRIPTION, "Save the project");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		WmodFileFilter filter = new WmodFileFilter();
		fileSaveChooser.setFileFilter(filter);
		int returnVal = fileSaveChooser.showSaveDialog(ide.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fileSaveChooser.getSelectedFile();
			if (!filter.accept(file))
			{
				file = new File(file.getPath() + "." + WmodFileFilter.WMOD);
			}

			saveWmodFile(file);

			//modified = false;

			//logEntry("File saved: " + file);
		}
		else
		{
			// SaveAs cancelled...  do nothing
		}
	}

	private void saveWmodFile(File wmodf)
	{
		//logEntry("Saving module to: " + wmodf);
		try	{
			final ModuleProxyFactory factory =
				ModuleSubjectFactory.getInstance();
			final OperatorTable optable = CompilerOperatorTable.getInstance();
			final ProxyMarshaller<ModuleProxy> marshaller =
				new JAXBModuleMarshaller(factory, optable);
			marshaller.marshal(ide.getActiveModuleContainer().getModule(),
							   wmodf);
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(ide.getFrame(),
										  "Error saving module file:" +
										  exception.getMessage());
			//logEntry("JAXBException - Failed to save  '" + wmodf + "'!");
		} catch (final SAXException exception) {
			JOptionPane.showMessageDialog(ide.getFrame(),
										  "Error saving module file:" +
										  exception.getMessage());
			//logEntry("SAXException - Failed to save  '" + wmodf + "'!");
		} catch (final WatersMarshalException exception) {
			JOptionPane.showMessageDialog(ide.getFrame(),
										  "Error saving module file:" +
										  exception.getMessage());
			//logEntry("WatersMarshalException - Failed to save  '" +
			//         wmodf + "'!");
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(ide.getFrame(),
										  "Error saving module file:" +
										  exception.getMessage());
			//logEntry("IOException - Failed to save  '" + wmodf + "'!");
		}
	}
}
