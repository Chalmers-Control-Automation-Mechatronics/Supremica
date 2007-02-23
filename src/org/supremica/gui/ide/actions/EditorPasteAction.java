//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorPasteAction
//###########################################################################
//# $Id: EditorPasteAction.java,v 1.4 2007-02-23 02:42:55 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CopyGraphCommand;
import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.gui.transfer.ObjectTransfer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;

import org.supremica.gui.ide.ModuleContainer;


public class EditorPasteAction
    extends IDEAction
    implements FlavorListener
{

    public EditorPasteAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.NAME, "Paste");
        putValue(Action.SHORT_DESCRIPTION, "Paste Graph");
        
        setEnabled(CLIPBOARD.isDataFlavorAvailable(COPYGRAPH));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
		if (CLIPBOARD.isDataFlavorAvailable(COPYGRAPH)) {
			try {
				final EditorWindowInterface iface =
					ide.getActiveEditorWindowInterface();
				final ControlledSurface surface = iface.getControlledSurface();
				final GraphContainer cont =
					(GraphContainer) CLIPBOARD.getData(COPYGRAPH);
				final Command command =
					new CopyGraphCommand(surface.getGraph(),
										 cont,
										 surface.getPastePosition());
				iface.getUndoInterface().executeCommand(command);
			} catch (final IOException exception) {
				throw new WatersRuntimeException(exception);
			} catch (final UnsupportedFlavorException exception) {
				throw new WatersRuntimeException(exception);
			}
		}
    }
    
    public void flavorsChanged(FlavorEvent e)
    {
		setEnabled(CLIPBOARD.isDataFlavorAvailable(COPYGRAPH));
    }


    private static final long serialVersionUID = 1L;
    
    private static DataFlavor COPYGRAPH = 
		new DataFlavor(GraphContainer.class, GraphContainer.class.getName());
    private static final Clipboard CLIPBOARD =
		Toolkit.getDefaultToolkit().getSystemClipboard();
    
}
