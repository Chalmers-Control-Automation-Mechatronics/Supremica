package org.supremica.gui.ide.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;

import javax.xml.bind.JAXBException;
import java.io.File;
import org.supremica.automata.IO.FileFormats;
import org.supremica.gui.FileDialogs;
import org.supremica.gui.ide.*;

import org.supremica.gui.ide.IDE;

public class OpenAction
	extends AbstractAction
	implements IDEAction
{
	private IDE ide;

	public OpenAction(IDE ide)
	{
		this.ide = ide;

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

		new FileImporter(FileDialogs.getImportFileChooser(FileFormats.WMOD), ide)    // anonymous class
		{
			void openFile(IDE ide, File f)
			{
				ModuleProxy module = null;
				try
				{
					final ProxyMarshaller marshaller = new ModuleMarshaller();

					module = (ModuleProxy) marshaller.unmarshal(f);
				}
				catch (final JAXBException exception)
				{

					// Something bad happened
					JOptionPane.showMessageDialog(ide, "Error loading module file! (JAXBException)");
					// logEntry("JAXBException - Failed to load: " + wmodf);

					//exception.printStackTrace(System.err);
				}
				catch (final ModelException exception)
				{
					JOptionPane.showMessageDialog(ide, "Error loading module file! (ModelException)");
					// logEntry("ModelException - Failed to load: " + wmodf);
				}

				ModuleContainer moduleContainer = new ModuleContainer(ide, module);
				ide.add(moduleContainer);
				ide.setActive(moduleContainer);
			}
		};

	}
}
