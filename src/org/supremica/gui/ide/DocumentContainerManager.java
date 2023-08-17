//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.external.despot.DESpotImporter;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.gui.observer.ContainerSwitchEvent;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.PendingSaveEvent;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProductDESToModuleUnmarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.samples.maze.MazeCompiler;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Project;
import org.supremica.automata.IO.ADSUnmarshaller2;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.TCTUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;

import org.xml.sax.SAXException;


public class DocumentContainerManager
{

  //#########################################################################
  //# Constructor
  DocumentContainerManager(final IDE ide)
      throws SAXException, ParserConfigurationException
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
    final SAXModuleMarshaller moduleMarshaller =
      new SAXModuleMarshaller(factory, opTable);
    final ProxyUnmarshaller<Project> supremicaUnmarshaller =
      new SupremicaUnmarshaller(factory);
    final ProxyMarshaller<Project> supremicaMarshaller =
      new SupremicaMarshaller();
    final ProxyUnmarshaller<ModuleProxy> desUnmarshaller =
      new ProductDESToModuleUnmarshaller(factory);
    final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller =
      new HISCUnmarshaller(factory);
    final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller =
      new UMDESUnmarshaller(factory);
    /* ADSUnmarshaller is replaced by ADSUnmarshaller2 since the
     * former does not work!!! /Mohammad~Reza */
    final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller =
      new ADSUnmarshaller2(factory);
    final ProxyUnmarshaller<ModuleProxy> validUnmarshaller =
      new ValidUnmarshaller(factory, opTable);
    final ProxyUnmarshaller<ModuleProxy> tctUnmarshaller =
      new TCTUnmarshaller(factory);
    // Add marshallers in order of importance ...
    mDocumentManager.registerMarshaller(moduleMarshaller);
    mDocumentManager.registerMarshaller(supremicaMarshaller);
    // Add unmarshallers in order of importance ...
    // (shows up in the file-open dialog)
    mDocumentManager.registerUnmarshaller(moduleMarshaller);
    mDocumentManager.registerUnmarshaller(supremicaUnmarshaller);
    mDocumentManager.registerUnmarshaller(desUnmarshaller);
    mDocumentManager.registerUnmarshaller(hiscUnmarshaller);
    mDocumentManager.registerUnmarshaller(umdesUnmarshaller);
    mDocumentManager.registerUnmarshaller(adsUnmarshaller);
    mDocumentManager.registerUnmarshaller(tctUnmarshaller);
    mDocumentManager.registerUnmarshaller(validUnmarshaller);

    mProductDESImporter = new ProductDESImporter(factory);
    mModuleImporters =
      new LinkedList<CopyingProxyUnmarshaller<ModuleProxy>>();
    final File mazeinputs = null;
    final CopyingProxyUnmarshaller<ModuleProxy> despotImporter =
      new DESpotImporter(factory, mDocumentManager);
    final CopyingProxyUnmarshaller<ModuleProxy> mazeImporter =
      new MazeCompiler(mazeinputs, factory, mDocumentManager);
    mModuleImporters.add(despotImporter);
    mModuleImporters.add(mazeImporter);
  }


  //#########################################################################
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
    // Just in case they try to register or unregister observers
    // in response to the update ...
    final List<Observer> copy = new LinkedList<Observer>(mObservers);
    for (final Observer observer : copy) {
      observer.update(event);
    }
    mIDE.fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Simple Access
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public List<CopyingProxyUnmarshaller<ModuleProxy>> getModuleImporters()
  {
    return mModuleImporters;
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


  //#########################################################################
  //# Opening and Closing Documents
  public void setActiveContainer(final DocumentContainer container)
  {
    final DocumentContainer active = getActiveContainer();
    if (container == active) {
      // nothing
    } else if (mAllContainers.contains(container)) {
      mRecentList.remove(container);
      mRecentList.add(0, container);
      fireContainerSwitch(active);
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
    mWasCancelled = false;
    final URI uri = file.toURI();
    return openContainer(uri);
  }

  public DocumentContainer openContainer(final URI uri)
  {
    mWasCancelled = false;
    return openContainer(uri, false);
  }

  public DocumentContainer openContainer(final DocumentProxy doc)
  {
    final URI uri = doc.getLocation();
    final DocumentContainer found = mURIContainerMap.get(uri);
    if (found == null) {
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

  public void saveActiveContainer()
  {
    mWasCancelled = false;
    saveActiveContainer(false);
  }

  public void saveActiveContainerAs()
  {
    mWasCancelled = false;
    saveActiveContainerAs(false);
  }

  public void closeActiveContainer()
  {
    mWasCancelled = false;
    if (confirmUnsavedChanges()) {
      final DocumentContainer container = getActiveContainer();
      closeContainer(container);
    }
  }

  public boolean closeAllContainers()
  {
    mWasCancelled = false;
    while (getActiveContainer() != null && !mWasCancelled) {
      closeActiveContainer();
    }
    return !mWasCancelled;
  }

  public void showIOError(final IOException exception)
  {
    showIOError(exception, false);
  }


  //#########################################################################
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


  //#########################################################################
  //# Auxiliary Methods --- Opening
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
      // The document manager does the loading, by extension.
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
      try {
        final Project project = (Project) doc;
        if (SupremicaUnmarshaller.isWatersCompatible(project)) {
          final ModuleSubject module =
            (ModuleSubject) mProductDESImporter.importModule(project);
          return new ModuleContainer(mIDE, module);
        } else {
          final String text = getWarningText(doc, WARN_CONVERSION);
          final int choice =
            showWarningDialog(text, JOptionPane.YES_NO_CANCEL_OPTION);
          switch (choice) {
          case JOptionPane.YES_OPTION:
            final ModuleSubject module =
              (ModuleSubject) mProductDESImporter.importModule(project);
            return new ModuleContainer(mIDE, module);
          case JOptionPane.NO_OPTION:
            return new AutomataContainer(mIDE, project);
          default:
            return null;
          }
        }
      } catch (final ParseException exception) {
        showParseError(exception);
        return null;
      }
    } else {
      throw new ClassCastException("Unknown document type " +
                                   doc.getClass().getName() + "!");
    }
  }

  public void addContainer(final DocumentContainer container)
  {
    final DocumentContainer previous = getActiveContainer();
    final DocumentProxy doc = container.getDocument();
    final URI uri = doc.getLocation();
    mAllContainers.add(container);
    mURIContainerMap.put(uri, container);
    mRecentList.add(0, container);
    fireContainerSwitch(previous);
  }

  private void closeContainer(final DocumentContainer container)
  {
    final DocumentProxy doc = container.getDocument();
    final URI uri = doc.getLocation();
    mAllContainers.remove(container);
    mURIContainerMap.remove(uri);
    mRecentList.remove(container);
    mDocumentManager.remove(uri);
    container.close();
    fireContainerSwitch(null);
  }


  //#########################################################################
  //# Auxiliary Methods --- Saving
  private void saveActiveContainer(final boolean mayCancel)
  {
    final DocumentContainer container = getActiveContainer();
    final DocumentProxy doc = container.getDocument();
    try {
      final File file = doc.getFileLocation();
      if (file == null) {
        saveActiveContainerAs(mayCancel);
      } else {
        saveDocument(container, doc, file, mayCancel);
      }
    } catch (final MalformedURLException exception) {
      saveActiveContainerAs(mayCancel);
    }
  }

  private void saveActiveContainerAs(final boolean maycancel)
  {
    final JFileChooser chooser = getSaveFileChooser();
    chooser.setDialogTitle("Save as ...");
    final int returnVal = chooser.showSaveDialog(mIDE.getFrame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      final FileFilter filter = chooser.getFileFilter();
      if (filter instanceof StandardExtensionFileFilter) {
        final StandardExtensionFileFilter extender =
          (StandardExtensionFileFilter) filter;
        file = extender.ensureDefaultExtension(file);
      }
      final DocumentContainer container = getActiveContainer();
      container.adjustDocumentName(file);
      saveDocument(file, maycancel);
    } else {
      mWasCancelled = true;
    }
  }

  private void saveDocument(final File file,
                            final boolean mayCancel)
  {
    final DocumentContainer container = getActiveContainer();
    final DocumentProxy doc = container.getDocument();
    final Class<? extends DocumentProxy> clazz = doc.getClass();
    final ProxyMarshaller<? extends DocumentProxy> marshaller =
      mDocumentManager.findProxyMarshaller(clazz);
    final FileFilter docFilter = marshaller.getDefaultFileFilter();
    if (doc instanceof ModuleProxy ||
        doc instanceof Project && docFilter.accept(file)) {
      final File extFile = ensureDefaultExtension(file, doc);
      saveDocument(container, doc, extFile, mayCancel);
    } else if (doc instanceof Project) {
      // Converting Supremica >> Waters ...
      // Check if there are unsupported features in the project ...
      final Project project = (Project) doc;
      if (!SupremicaUnmarshaller.isWatersCompatible(project)) {
        final int choice = JOptionPane.showConfirmDialog
          (mIDE.getFrame(), WARN_UNSUPPORTED,
           "Warning", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
          return;
        }
      }
      try {
        // Convert the project to Waters and save
        final ModuleProxy module = mProductDESImporter.importModule(project);
        final File extFile = ensureDefaultExtension(file, module);
        saveDocument(container, module, extFile, mayCancel);
        // Open the converted project in the editor
        closeContainer(container);
        newContainer(module);
      } catch (final ParseException exception) {
        showParseError(exception);
      }
    } else {
      throw new ClassCastException("Unknown document type " +
                                   ProxyTools.getShortClassName(clazz) + "!");
    }
  }

  /*
   * This alternative method could support saving modules as .xml or .wdes,
   * but it needs more work ... ~~~Robi
  private void saveDocument(final FileFilter filter,
                            final File file,
                            final boolean mayCancel)
  {
    final DocumentContainer container = getActiveContainer();
    final DocumentProxy doc = container.getDocument();
    final ProxyMarshaller<? extends DocumentProxy> marshaller =
      mDocumentManager.findProxyMarshaller(filter);
    // But what if the user selected "all files" and typed a name?
    final Class<? extends DocumentProxy> marshalledClazz =
      marshaller.getDocumentClass();
    final DocumentProxy marshalledDoc;
    if (marshalledClazz.isAssignableFrom(doc.getClass())) {
      firePendingSave(container);
      marshalledDoc = marshalledClazz.cast(doc);
    } else if (doc instanceof ProductDESProxy &&
               ModuleProxy.class.isAssignableFrom(marshalledClazz)) {
      // Converting Supremica >> Waters ...
      // If analyser active, check if there are unsupported features
      // in the project ...
      final AnalyzerPanel analyzer = container.getAnalyzerPanel();
      if (container.getActivePanel() == analyzer &&
          !SupremicaUnmarshaller.validate(analyzer.getVisualProject())) {
        final int choice = JOptionPane.showConfirmDialog
          (mIDE.getFrame(), WARN_UNSUPPORTED,
           "Warning", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
          return;
        }
      }
      if (container instanceof ModuleContainer) {
        firePendingSave(container);
        marshalledDoc = container.getEditorPanel().getModuleSubject();
      } else if (container instanceof AutomataContainer) {
        try {
          final Project project = (Project) doc;
          marshalledDoc = mProductDESImporter.importModule(project);
        } catch (final ParseException exception) {
          showParseError(exception);
          return;
        }
      } else {
        throw new ClassCastException("Unknown document container type: " +
                                     container.getClass().getName() + "!");
      }
    } else if (doc instanceof ModuleProxy &&
               Project.class.isAssignableFrom(marshalledClazz)) {
      // Converting Waters >> Supremica ...
      // Must wait for background compiler to finish, check for errors,
      // if possible convert compiled DES to Supremica project
    }
    // Finally save the marshalledDoc using the marshaller
  }
  */

  private void saveDocument(final DocumentContainer container,
                            final DocumentProxy doc,
                            final File file,
                            final boolean maycancel)
  {
    final URI uri = file.toURI();
    final DocumentContainer other = mURIContainerMap.get(uri);
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
    firePendingSave(container);
    try {
      mDocumentManager.saveAs(doc, file);
      container.setCheckPoint();
      final Logger logger = LogManager.getLogger();
      logger.info("{} saved to {}", getTypeString(doc), file);
      if (!uri.equals(olduri)) {
        mURIContainerMap.remove(olduri);
        mURIContainerMap.put(uri, container);
        fireContainerSwitch(container);
      }
    } catch (final WatersMarshalException exception) {
      showIOError(exception, maycancel);
    } catch (final IOException exception) {
      showIOError(exception, maycancel);
    }
  }

  private File ensureDefaultExtension(final File file,
                                      final DocumentProxy doc)
  {
    @SuppressWarnings("unchecked")
    final Class<DocumentProxy> clazz = (Class<DocumentProxy>) doc.getClass();
    final ProxyMarshaller<DocumentProxy> marshaller =
      mDocumentManager.findProxyMarshaller(clazz);
    final String ext = marshaller.getDefaultExtension();
    return StandardExtensionFileFilter.ensureDefaultExtension(file, ext);
  }


  //#########################################################################
  //# Auxiliary Methods --- Dialogs
  private String getNewModuleName()
  {
    return NEW_MODULE_NAME;
  }

  private JFileChooser getSaveFileChooser()
  {
    final JFileChooser chooser = mIDE.getFileChooser();
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setMultiSelectionEnabled(false);
    chooser.resetChoosableFileFilters();
    final FileFilter modFilter = mDocumentManager.
      findProxyMarshaller(ModuleProxy.class).getDefaultFileFilter();
    chooser.addChoosableFileFilter(modFilter);
    if (getActiveContainer() instanceof AutomataContainer) {
      final FileFilter supFilter= mDocumentManager.
        findProxyMarshaller(Project.class).getDefaultFileFilter();
      chooser.addChoosableFileFilter(supFilter);
      chooser.setFileFilter(supFilter);
    } else {
      chooser.setFileFilter(modFilter);
    }
    return chooser;
  }

  private int showIOError(final Exception exception,
                          final boolean maycancel)
  {
    final JFrame frame = mIDE.getFrame();
    final String msg = exception.getMessage();
    final String text = wrapExceptionMessageInHTML("Error accessing file", msg);
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

  private boolean confirmUnsavedChanges()
  {
    final DocumentContainer container = getActiveContainer();
    if (container.hasUnsavedChanges()) {
      final DocumentProxy doc = container.getDocument();
      final String text = getWarningText(doc, WARN_UNSAVED_CHANGES);
      final int choice =
        showWarningDialog(text, JOptionPane.YES_NO_CANCEL_OPTION);
      switch (choice) {
      case JOptionPane.YES_OPTION:
        saveActiveContainer(true);
        return !mWasCancelled;
      case JOptionPane.NO_OPTION:
      case JOptionPane.CLOSED_OPTION:
        return true;
      case JOptionPane.CANCEL_OPTION:
        mWasCancelled = true;
        return false;
      default:
        throw new IllegalStateException
          ("Unexpected result from JOptionPane: " + choice + "!");
      }
    } else {
      return true;
    }
  }

  private void showParseError(final ParseException exception)
  {
    final String msg = "Problem importing module: " + exception.getMessage();
    JOptionPane.showMessageDialog
      (mIDE.getFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
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
      throw new ClassCastException("Unknown document type " +
                                   doc.getClass().getName() + "!");
    }
  }

  private String getWarningText(final File file, final String msg)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("File '");
    buffer.append(file);
    buffer.append("'\n");
    buffer.append(msg);
    return buffer.toString();
  }

  private static String wrapExceptionMessageInHTML(final String title,
                                                   final String msg)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("<html><body style='width: 400px;'>");
    buffer.append("<h2>");
    buffer.append(title);
    buffer.append("</h2>");
    boolean gobble = true;
    for (int i = 0; i < msg.length(); i++) {
      final char ch = msg.charAt(i);
      switch (ch) {
      case ' ':
      case '\t':
        if (!gobble) {
          buffer.append(' ');
        }
        gobble = true;
        break;
      case ':':
        buffer.append(':');
        if (i + 1 >= msg.length() || msg.charAt(i + 1) != ' ') {
          gobble = false;
          break;
        }
        // fall through ...
      case '\n':
        buffer.append("<br>");
        gobble = true;
        break;
      case '&':
        buffer.append("&amp;");
        gobble = false;
        break;
      case '<':
        buffer.append("&lt;");
        gobble = false;
        break;
      case '>':
        buffer.append("&gt;");
        gobble = false;
        break;
      case '"':
        buffer.append("&quot;");
        gobble = false;
        break;
      default:
        buffer.append(ch);
        gobble = false;
        break;
      }
    }
    buffer.append("</body></html>");
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods --- Notifications
  private void fireContainerSwitch(final DocumentContainer previous)
  {
    final DocumentContainer container = getActiveContainer();
    if (previous != null && previous != container) {
      previous.deactivate();
    }
    final ContainerSwitchEvent event =
      new ContainerSwitchEvent(this, container);
    fireEditorChangedEvent(event);
    if (container != previous) {
      container.activate();
    }
  }

  private void firePendingSave(final DocumentContainer container)
  {
    if (container instanceof ModuleContainer) {
      final ModuleContainer moduleContainer = (ModuleContainer) container;
      firePendingSave(moduleContainer);
    }
  }

  private void firePendingSave(final ModuleContainer container)
  {
    final PendingSaveEvent event = new PendingSaveEvent(container);
    container.fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private final Set<DocumentContainer> mAllContainers;
  private final Map<URI,DocumentContainer> mURIContainerMap;
  private final List<DocumentContainer> mRecentList;
  private final DocumentManager mDocumentManager;
  private final ProductDESImporter mProductDESImporter;
  private final List<CopyingProxyUnmarshaller<ModuleProxy>> mModuleImporters;
  private final List<Observer> mObservers;

  private boolean mWasCancelled;


  //#########################################################################
  //# Static Class Constants
  private static final String NEW_MODULE_NAME = "New Module";

  private static final String WARN_CONVERSION =
    "contains attributes not supported by the editor.\n" +
    "Do you want to edit it (and lose the unsupported features)?";

  private static final String WARN_UNSUPPORTED =
    "This project contains attributes not supported by the WMOD format." +
    "\nDo you want to save (and lose the unsupported features)?";

  private static final String WARN_FILE_OPEN =
    "is already being edited by you.\nWould you like to replace?";

  private static final String WARN_FILE_EXISTS =
    "exists already.\nWould you like to overwrite?";

  private static final String WARN_UNSAVED_CHANGES =
    "has unsaved changes.\nWould you like to save it before closing?";
}
