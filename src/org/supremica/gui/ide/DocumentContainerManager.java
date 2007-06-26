//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   DocumentContainerManager
//###########################################################################
//# $Id: DocumentContainerManager.java,v 1.4 2007-06-26 13:07:11 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.observer.ContainerSwitchEvent;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.MainPanelSwitchEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import org.supremica.automata.Project;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.xml.sax.SAXException;


public class DocumentContainerManager
{

    //#######################################################################
    //# Constructor
    DocumentContainerManager(final IDE ide)
        throws JAXBException, SAXException
    {
        mIDE = ide;
        mAllContainers = new HashSet<DocumentContainer>();
        mURIContainerMap = new HashMap<URI,DocumentContainer>();
        mRecentList = new LinkedList<DocumentContainer>();
		mObservers = new LinkedList<Observer>();

        // Set up document manager ...
        mDocumentManager = new DocumentManager();
        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
        final OperatorTable opTable = CompilerOperatorTable.getInstance();
        final JAXBModuleMarshaller moduleMarshaller =
            new JAXBModuleMarshaller(factory, opTable);
        final ProxyUnmarshaller<Project> supremicaUnmarshaller =
            new SupremicaUnmarshaller(factory);
        final ProxyMarshaller<Project> supremicaMarshaller =
            new SupremicaMarshaller();
        final ProxyUnmarshaller<ModuleProxy> validUnmarshaller =
            new ValidUnmarshaller(factory, opTable);
        final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller =
            new HISCUnmarshaller(factory);
        final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller =
            new UMDESUnmarshaller(factory);
        final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller =
            new ADSUnmarshaller(factory);
        // Add marshallers in order of importance ...
        mDocumentManager.registerMarshaller(moduleMarshaller);
        mDocumentManager.registerMarshaller(supremicaMarshaller);
        // Add unmarshallers in order of importance ...
        // (shows up in the file-open dialog)
        mDocumentManager.registerUnmarshaller(moduleMarshaller);
        mDocumentManager.registerUnmarshaller(supremicaUnmarshaller);
        mDocumentManager.registerUnmarshaller(validUnmarshaller);
        mDocumentManager.registerUnmarshaller(hiscUnmarshaller);
        mDocumentManager.registerUnmarshaller(umdesUnmarshaller);
        mDocumentManager.registerUnmarshaller(adsUnmarshaller);

        mImporter = new ProductDESImporter(factory);
        mWasCancelled = false;
    }


	//#######################################################################
	//# Interface net.sourceforge.waters.gui.observer.Subject
    public void attach(final Observer observer)
    {
        mObservers.add(observer);
    }

    public void detach(final Observer observer)
    {
        mObservers.remove(observer);
    }

    public void fireEditorChangedEvent(final EditorChangedEvent event)
    {
		// Just in case they try to register or deregister observers
		// in response to the update ...
		final List<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy) {
            observer.update(event);
        }
        mIDE.fireEditorChangedEvent(event);
    }
    

    //#######################################################################
    //# Simple Access
    public DocumentManager getDocumentManager()
    {
        return mDocumentManager;
    }

    public List<DocumentContainer> getRecent()
    {
        return Collections.unmodifiableList(mRecentList);
    }

    public DocumentContainer getActiveContainer()
    {
        if (mRecentList.isEmpty()) {
            return null;
        } else {
            return mRecentList.iterator().next();
        }
    }


    //#######################################################################
    //# Opening and Closing Documents
    public void setActiveContainer(final DocumentContainer container)
    {
        final DocumentContainer active = getActiveContainer();
        if (container == active) {
            // nothing
        } else if (mAllContainers.contains(container)) {
            mRecentList.remove(container);
            mRecentList.add(0, container);
            fireContainerSwitch();
        } else {
            throw new IllegalArgumentException
                ("DocumentContainer to be activated not found!");
        }
    }

    public DocumentContainer newContainer(final DocumentProxy doc)
    {
        final DocumentContainer container = createContainer(doc);
        addContainer(container);
        return container;
    }

    public ModuleContainer newModuleContainer()
    {
        final String name = getNewModuleName();
        final ModuleSubject module = new ModuleSubject(name, null);
        final ModuleContainer container = new ModuleContainer(mIDE, module);
        addContainer(container);
        return container;
    }

    public boolean openContainers(final Iterable<File> files)
    {
        mWasCancelled = false;
        final Iterator<File> iter = files.iterator();
        while (iter.hasNext() && !mWasCancelled) {
            final File file = iter.next();
            final URI uri = file.toURI();
            final boolean maycancel = iter.hasNext();
            openContainer(uri, maycancel);
        }
        return !mWasCancelled;
    }

    public DocumentContainer openContainer(final File file)
    {
        final URI uri = file.toURI();
        return openContainer(uri);
    }

    public DocumentContainer openContainer(final URI uri)
    {
        return openContainer(uri, false);
    }

    public void saveContainer(final DocumentContainer container,
                              final File file)
    {
        if (mAllContainers.contains(container)) {
            final URI uri = file.toURI();
            final DocumentContainer other = mURIContainerMap.get(uri);
            final DocumentProxy doc = container.getDocument();
            final URI olduri = doc.getLocation();
            if (other != null && other != container) {
                final String text = getWarningText(file, WARN_FILE_OPEN);
                final int choice = 
                    showWarningDialog(text, JOptionPane.OK_CANCEL_OPTION);
                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }
                closeContainer(other);
            } else if (file.exists() && !uri.equals(olduri)) {
                final String text = getWarningText(file, WARN_FILE_EXISTS);
                final int choice = 
                    showWarningDialog(text, JOptionPane.OK_CANCEL_OPTION);
                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            try {
                mDocumentManager.saveAs(doc, file);
                final String type = getTypeString(doc);
                final String msg = type + " saved to " + file;
                mIDE.info(msg);
                if (!uri.equals(olduri)) {
                    mURIContainerMap.remove(olduri);
                    mURIContainerMap.put(uri, container);
                    fireContainerSwitch();
                }
            } catch (final WatersMarshalException exception) {
                showIOError(exception, false);
            } catch (final IOException exception) {
                showIOError(exception, false);
            }
        } else {
            throw new IllegalArgumentException
                ("DocumentContainer to be saved not found!");
        }
    }

    public void closeContainer(final DocumentContainer container)
    {
        if (mAllContainers.contains(container)) {
            final DocumentProxy doc = container.getDocument();
            final URI uri = doc.getLocation();
            mAllContainers.remove(container);
            mURIContainerMap.remove(uri);
            mRecentList.remove(container);
            mDocumentManager.remove(uri);
            fireContainerSwitch();
        }
    }

    //#######################################################################
    //# Static Class Methods
    public static String getTypeString(final DocumentProxy doc)
    {
        if (doc instanceof Project) {
            return AutomataContainer.TYPE_STRING;
        } else if (doc instanceof ModuleProxy) {
            return ModuleContainer.TYPE_STRING;
        } else {
            throw new ClassCastException
                ("Unknown document type: " + doc.getClass().getName() + "!");
        }
    }


    //#######################################################################
    //# Auxiliary Methods
    private DocumentContainer openContainer(final URI uri,
                                            final boolean maycancel)
    {
        final DocumentContainer found = mURIContainerMap.get(uri);
        if (found == null) {
            final DocumentProxy doc = load(uri, maycancel);
            if (doc == null) {
                return null;
            }
            final DocumentContainer container = createContainer(doc);
            if (container == null) {
                return null;
            }
            addContainer(container);
            return container;
        } else {
            setActiveContainer(found);
            return found;
        }
    }

    private DocumentProxy load(final URI uri, final boolean maycancel)
    {
        try {
            // The documentmanager does the loading, by extension.
            return mDocumentManager.load(uri);
        } catch (final WatersUnmarshalException exception) {
            showIOError(exception, maycancel);
            return null;
        } catch (final IOException exception) {
            showIOError(exception, maycancel);
            return null;
        }
    }

    private DocumentContainer createContainer(final DocumentProxy doc)
    {
        if (doc instanceof ModuleSubject) {
            final ModuleSubject module = (ModuleSubject) doc;
            return new ModuleContainer(mIDE, module);
        } else if (doc instanceof Project) {
            final Project project = (Project) doc;
            if (SupremicaUnmarshaller.validate(project)) {
                final ModuleSubject module =
                    (ModuleSubject) mImporter.importModule(project);
                return new ModuleContainer(mIDE, module);
            } else {
                final String text = getWarningText(doc, WARN_CONVERSION);
                final int choice =
                    showWarningDialog(text, JOptionPane.YES_NO_CANCEL_OPTION);
                switch (choice) {
                case JOptionPane.YES_OPTION:
                    final ModuleSubject module =
                        (ModuleSubject) mImporter.importModule(project);
                    return new ModuleContainer(mIDE, module);
                case JOptionPane.NO_OPTION:
                    return new AutomataContainer(mIDE, project);
                default:
                    return null;
                }
            }
        } else {
            throw new ClassCastException
                ("Unknown document type " + doc.getClass().getName() + "!");
        }
    }

    private void addContainer(final DocumentContainer container)
    {
        final DocumentProxy doc = container.getDocument();
        final URI uri = doc.getLocation();
        mAllContainers.add(container);
        mURIContainerMap.put(uri, container);
        mRecentList.add(0, container);
        fireContainerSwitch();
    }

    private void fireContainerSwitch()
    {
        final DocumentContainer container = getActiveContainer();
        final ContainerSwitchEvent event =
            new ContainerSwitchEvent(this, container);
        fireEditorChangedEvent(event);
    }

    private String getNewModuleName()
    {
        return NEW_MODULE_NAME;
    }

    private int showIOError(final Exception exception,
                            final boolean maycancel)
    {
        final JFrame frame = mIDE.getFrame();
        final String msg = exception.getMessage();
        final String text =
            "Error accesing file:\n" + wrapExceptionMessage(msg); 
        final String title = "I/O Error";
        if (maycancel) {
            final int choice = JOptionPane.showConfirmDialog
                (frame, text, title,
                 JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            mWasCancelled = choice == JOptionPane.CANCEL_OPTION;
            return choice;
        } else {
            JOptionPane.showMessageDialog
                (frame, text, title, JOptionPane.ERROR_MESSAGE);
            mWasCancelled = false;
            return JOptionPane.OK_OPTION;
        }
    }

    private int showWarningDialog(final String text, final int optionType)
    {
        final JFrame frame = mIDE.getFrame();
        final String title = "Warning";
        final int choice = JOptionPane.showConfirmDialog
            (frame, text, title, optionType, JOptionPane.QUESTION_MESSAGE);
        mWasCancelled = choice == JOptionPane.CANCEL_OPTION;
        return choice;
    }

    private String getWarningText(final DocumentProxy doc, final String msg)
    {
        final File file = DocumentContainer.getFileLocation(doc);
        if (file != null) {
            return getWarningText(file, msg);
        } else if (doc instanceof ModuleProxy) {
            return "This module " + msg;
        } else if (doc instanceof Project) {
            return "This project " + msg;
        } else {
            throw new ClassCastException
                ("Unknown document type " + doc.getClass().getName() + "!");
        }
    }

    private String getWarningText(final File file, final String msg)
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("File '");
        buffer.append(file);
        buffer.append("'\n");
        buffer.append(msg);
        return buffer.toString();
    }

    private String wrapExceptionMessage(final String msg)
    {
        return msg.replaceAll(": +", ":\n");
    }


    //#######################################################################
    //# Data Members
    private final IDE mIDE;
    private final Set<DocumentContainer> mAllContainers;
    private final Map<URI,DocumentContainer> mURIContainerMap;
    private final List<DocumentContainer> mRecentList;
    private final DocumentManager mDocumentManager;
    private final ProductDESImporter mImporter;
    private final List<Observer> mObservers;

    private boolean mWasCancelled;


    //#######################################################################
    //# Static Class Constants
    private static final String NEW_MODULE_NAME = "New Module";

    private static final String WARN_CONVERSION =
        "contains attributes not supported by the editor.\nDo you want to edit it (and lose the unsupported features)?";

    private static final String WARN_FILE_OPEN =
        "is already being edited by you.\nWould you like to replace?";

    private static final String WARN_FILE_EXISTS =
        "exists already.\nWould you like to overwrite?";

}
