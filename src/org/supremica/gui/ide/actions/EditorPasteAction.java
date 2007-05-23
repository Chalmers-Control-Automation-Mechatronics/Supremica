//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorPasteAction
//###########################################################################
//# $Id: EditorPasteAction.java,v 1.6 2007-05-23 15:47:29 flordal Exp $
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
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import org.supremica.gui.ide.IDE;


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
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Paste16.gif")));
        
        setEnabled(CLIPBOARD.isDataFlavorAvailable(COPYGRAPH));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        if (CLIPBOARD.isDataFlavorAvailable(COPYGRAPH))
        {
            try
            {
                final EditorWindowInterface iface =
                    ide.getActiveEditorWindowInterface();
                final ControlledSurface surface = iface.getControlledSurface();
                final GraphContainer cont =
                    (GraphContainer) CLIPBOARD.getData(COPYGRAPH);
                surface.doPasteNodesAndEdges(cont);
            }
            catch (final IOException exception)
            {
                throw new WatersRuntimeException(exception);
            }
            catch (final UnsupportedFlavorException exception)
            {
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
