//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: OpenAction.java,v 1.14 2006-09-10 19:01:53 flordal Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import java.util.List;
import javax.swing.KeyStroke;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import org.supremica.gui.FileDialogs;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

import org.xml.sax.SAXException;


public class OpenAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public OpenAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Open...");
        putValue(Action.SHORT_DESCRIPTION, "Open a project");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
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
            if (lowername.endsWith(FileDialogs.WMOD_EXT))
            {
                openFileWmod(file);
            }
            else if (lowername.endsWith(FileDialogs.MAINVMOD_EXT))
            {
                openFileVmod(file);
            }
            else if (filename.endsWith(FileDialogs.VPRJ_EXT))
            {
                openFileVprj(file);
            }
        }
        
        
        
        //###################################################################
        //# Type Specific Opening Methods
        private void openFileWmod(final File file)
        {
            try
            {
                final ModuleProxyFactory factory =
                    ModuleSubjectFactory.getInstance();
                final OperatorTable optable =
                    CompilerOperatorTable.getInstance();
                final ProxyUnmarshaller<ModuleProxy> unmarshaller =
                    new JAXBModuleMarshaller(factory, optable);
                final URI uri = file.toURI();
                final ModuleSubject module =
                    (ModuleSubject) unmarshaller.unmarshal(uri);
                installContainer(module);
            }
            catch (final SAXException exception)
            {
                showParseError("Could not parse module file", file, exception);
            }
            catch (final JAXBException exception)
            {
                showParseError("Could not parse module file", file, exception);
            }
            catch (final WatersUnmarshalException exception)
            {
                showParseError("Could not parse module file", file, exception);
            }
            catch (final IOException exception)
            {
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
            if (vmodfile.exists())
            {
                openFileVmod(vmodfile);
            }
            else
            {
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
            try
            {
                final ModuleProxyFactory factory =
                    ModuleSubjectFactory.getInstance();
                final OperatorTable optable =
                    CompilerOperatorTable.getInstance();
                final ProxyUnmarshaller<ModuleProxy> unmarshaller =
                    new ValidUnmarshaller(factory, optable);
                final URI uri = file.toURI();
                final ModuleSubject module =
                    (ModuleSubject) unmarshaller.unmarshal(uri);
                installContainer(module);
            }
            catch (final SAXException exception)
            {
                showParseError
                    ("Error importing from VALID module", file, exception);
            }
            catch (final JAXBException exception)
            {
                showParseError
                    ("Error importing from VALID module", file, exception);
            }
            catch (final WatersUnmarshalException exception)
            {
                showParseError
                    ("Error importing from VALID module", file, exception);
            }
            catch (final IOException exception)
            {
                showParseError
                    ("Error importing from VALID module", file, exception);
            }
        }
        
        
        
        //###################################################################
        //# Auxiliary Methods
        private void installContainer(final ModuleSubject module)
        {
            final ModuleContainer moduleContainer =
                new ModuleContainer(ide.getIDE(), module);
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
