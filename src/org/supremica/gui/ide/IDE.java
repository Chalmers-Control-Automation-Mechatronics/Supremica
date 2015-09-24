//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.about.WelcomeScreen;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.automata.Project;
import org.supremica.comm.xmlrpc.Server;
import org.supremica.gui.InterfaceManager;
import org.supremica.gui.SupremicaLoggerFactory;
import org.supremica.gui.Utility;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.log.Logger;
import org.supremica.properties.Config;
import org.supremica.util.ProcessCommandLineArguments;
import org.xml.sax.SAXException;


/**
 * The IDE's main window.
 *
 * This class represents the IDE's main window, and provides a central
 * access point to all components of the graphical user interface. It
 * also is the main entry point to the program.
 *
 * @author Knut &Aring;kesson, Robi Malik
 */

public class IDE
    extends JFrame
    implements IDEActionInterface, Observer, Subject
{

  //#########################################################################
  //# Constructors
  public IDE()
    throws JAXBException, SAXException
  {
    this(true);
  }

  public IDE(final boolean showVersion)
    throws JAXBException, SAXException
  {
    // Instantiate all actions
    mObservers = new LinkedList<Observer>();
    mFocusTracker = new FocusTracker(this);
    mFocusTracker.attach(this);
    mActions = new Actions(this);
    mPopupActionManager = new WatersPopupActionManager(this);
    mModuleNameObserver = new ModuleNameObserver();

    // Create GUI
    Utility.setupFrame(this, IDEDimensions.mainWindowPreferredSize);
    setTitle(Version.getInstance().getTitle());
    final BorderLayout layout = new BorderLayout();
    final JPanel contents = (JPanel) getContentPane();
    contents.setLayout(layout);
    menuBar = new IDEMenuBar(this);
    setJMenuBar(menuBar);
    mToolBar = new IDEToolBar(this);
    contents.add(mToolBar, BorderLayout.NORTH);
    mWelcomeScreen = new WelcomeScreen(this);
    mWelcomeScreen.setPreferredSize(IDEDimensions.mainWindowPreferredSize);
    mLogPanel = new LogPanel(this, "Logger");
    mSplitPaneVertical =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, mWelcomeScreen, mLogPanel);
    mSplitPaneVertical.setContinuousLayout(false);
    mSplitPaneVertical.setOneTouchExpandable(false);
    mSplitPaneVertical.setDividerLocation(0.8);
    mSplitPaneVertical.setResizeWeight(1.0);
    contents.add(mSplitPaneVertical, BorderLayout.CENTER);

    final File startdir = new File(Config.FILE_OPEN_PATH.getAsString());
    mFileChooser = new JFileChooser(startdir);

    // Initialise document managers
    mDocumentContainerManager = new DocumentContainerManager(this);
    mDocumentContainerManager.attach(this);

    if (showVersion) {
      // Show Version number
      info(Version.getInstance().toString());
      // Show memory
      final int MB = 1024*1024;
      info("JVM:" + System.getProperty("java.version") +
           ", Free/Total/Max mem: " +
           Runtime.getRuntime().freeMemory()/MB + "/" +
           Runtime.getRuntime().totalMemory()/MB + "/" +
           Runtime.getRuntime().maxMemory()/MB + " MB");
    }
    // Initialise XML_RPC
    if (Config.XML_RPC_ACTIVE.isTrue()) {
      try {
        new Server(this, Config.XML_RPC_PORT.get());
        info("XML-RPC server running on port " + Config.XML_RPC_PORT.get());
      } catch (final Exception exception) {
        warn("Another server already running on port " +
             Config.XML_RPC_PORT.get() + ". XML-RPC server not started!");
      }
    }
  }


  //#########################################################################
  //# Simple Access
  @Override
  public String getName()
  {
    return IDENAME;
  }

  @Override
  public JFrame getFrame()
  {
    return this;
  }

  @Override
  public IDE getIDE()
  {
    return this;
  }

  public IDEToolBar getToolBar()
  {
    return mToolBar;
  }

  @Override
  public Actions getActions()
  {
    return mActions;
  }

  public FocusTracker getFocusTracker()
  {
    return mFocusTracker;
  }

  public WatersPopupActionManager getPopupActionManager()
  {
    return mPopupActionManager;
  }

  public DocumentContainerManager getDocumentContainerManager()
  {
    return mDocumentContainerManager;
  }

  public DocumentManager getDocumentManager()
  {
    return mDocumentContainerManager.getDocumentManager();
  }

  public JFileChooser getFileChooser()
  {
    return mFileChooser;
  }


  //#########################################################################
  //# Listeners
  /**
   * Overridden to exit safely when the application window is closed.
   */
  @Override
  protected void processWindowEvent(final WindowEvent event)
  {
    if (event.getID() == WindowEvent.WINDOW_CLOSING) {
      final Action action = mActions.getAction(ExitAction.class);
      final ActionEvent aevent =
        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
      action.actionPerformed(aevent);
    } else {
      super.processWindowEvent(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
      final DocumentContainer container =
        mDocumentContainerManager.getActiveContainer();
      if (container == null) {
        mSplitPaneVertical.setTopComponent(mWelcomeScreen);
      } else {
        final Component panel = container.getPanel();
        mSplitPaneVertical.setTopComponent(panel);
      }
      mModuleNameObserver.setModule(container);
      updateWindowTitle();
      break;
    default:
      break;
    }
    fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer observer)
  {
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
  }

  @Override
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    // Just in case they try to register or unregister observers
    // in response to the update ...
    final List<Observer> copy = new LinkedList<Observer>(mObservers);
    for (final Observer observer : copy)
    {
      observer.update(event);
    }
  }


  //#########################################################################
  //# Public Shortcuts
  //# (use with caution --- these should be considered as @deprecated)
  @Override
  public DocumentContainer getActiveDocumentContainer()
  {
    if (mDocumentContainerManager == null) {
      return null;
    } else {
      return mDocumentContainerManager.getActiveContainer();
    }
  }

  @Override
  public EditorWindowInterface getActiveEditorWindowInterface()
  {
    return getActiveDocumentContainer().getEditorPanel().getActiveEditorWindowInterface();
  }


  public Project getActiveProject()
  {
    final DocumentContainer active =
      mDocumentContainerManager.getActiveContainer();
    return active.getAnalyzerPanel().getVisualProject();
  }


  //#########################################################################
  //# Interface org.supremica.gui.ide.IDEReportInterface
  @Override
  public void error(final String msg)
  {
    LOGGER.error(msg);
  }

  @Override
  public void error(final String msg, final Throwable t)
  {
    LOGGER.error(msg, t);
  }

  @Override
  public void info(final String msg)
  {
    LOGGER.info(msg);
  }

  public void warn(final String msg)
  {
    LOGGER.warn(msg);
  }

  @Override
  public void debug(final String msg)
  {
    LOGGER.debug(msg);
  }


  //#########################################################################
  //# Main Program
  public static void main(final String args[])
    throws Exception
  {
    // Process command line arguments
    final List<File> files = ProcessCommandLineArguments.process(args);

    // Initialise logging
    SupremicaLoggerFactory.initialiseSupremicaLoggerFactory();
    LOGGER = SupremicaLoggerFactory.createLogger(IDE.class);

    // Now start the gui...
    InterfaceManager.getInstance().initLookAndFeel();
    //WatersDragSourceListener.setup();
    final boolean hasFiles = (files != null && files.size() > 0);
    final boolean showVersion =
      hasFiles || Config.GUI_EDITOR_DEFAULT_EMPTY_MODULE.isTrue();
    final IDE ide = new IDE(showVersion);
    // Open initial module(s)
    if (hasFiles) {
      ide.openFiles(files);
    } else if (Config.GUI_EDITOR_DEFAULT_EMPTY_MODULE.isTrue()) {
      ide.openEmptyDocument();
    }

    // Show!
    ide.setVisible(true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean openFiles(final List<File> filesToOpen)
  {
    return mDocumentContainerManager.openContainers(filesToOpen);
  }

  private void openEmptyDocument()
  {
    mDocumentContainerManager.newModuleContainer();
  }

  private String getWindowTitle()
  {
    final DocumentContainer container =
      mDocumentContainerManager.getActiveContainer();
    if (container == null) {
      return IDENAME;
    } else {
      final DocumentProxy doc = container.getDocument();
      final String name = doc.getName();
      final File file = container.getFileLocation();
      final StringBuilder buffer = new StringBuilder(IDENAME + " - Module");
      if (name != null && !name.equals("")) {
        buffer.append(": ");
        buffer.append(name);
      }
      if (file != null) {
        buffer.append(" [");
        buffer.append(file);
        buffer.append(']');
      }
      return buffer.toString();
    }
  }

  private void updateWindowTitle()
  {
    final String title = getWindowTitle();
    setTitle(title);
  }


  //#########################################################################
  //# Inner Class ModuleNameObserver
  /**
   * This observer is attached to the current module, if any, to receive
   * notifications about changes of the module name and update the window
   * title accordingly.
   */
  private class ModuleNameObserver implements ModelObserver
  {

    //#######################################################################
    //# Constructor
    private ModuleNameObserver()
    {
      mModule = null;
    }

    //#######################################################################
    //# Interface net.sourceforge.window.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      if (event.getSource() == mModule &&
          event.getKind() == ModelChangeEvent.NAME_CHANGED) {
        updateWindowTitle();
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
    }

    //#######################################################################
    //# Module Switching
    private void setModule(final DocumentContainer container)
    {
      if (container instanceof ModuleContainer) {
        final ModuleContainer moduleContainer = (ModuleContainer) container;
        final ModuleSubject module = moduleContainer.getModule();
        setModule(module);
      } else {
        final ModuleSubject module = null;
        setModule(module);
      }
    }

    private void setModule(final ModuleSubject module)
    {
      if (mModule != module) {
        if (mModule != null) {
          mModule.removeModelObserver(this);
        }
        mModule = module;
        if (mModule != null) {
          mModule.addModelObserver(this);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private ModuleSubject mModule;
  }


  //#########################################################################
  //# Data Members
  // Actions
  private final List<Observer> mObservers;
  private final FocusTracker mFocusTracker;
  private final Actions mActions;
  private final WatersPopupActionManager mPopupActionManager;
  private final ModuleNameObserver mModuleNameObserver;

  // GUI Components
  private final DocumentContainerManager mDocumentContainerManager;
  private final IDEMenuBar menuBar;
  private final IDEToolBar mToolBar;
  private final JPanel mWelcomeScreen;
  private final JSplitPane mSplitPaneVertical;
  private final LogPanel mLogPanel;
  private final JFileChooser mFileChooser;

  // Logger. Must not be initialised until ProcessCommandLineArguments
  // has finished (or messages WILL disappear).  Try running "IDE -h" and
  // "IDE", _both_ should give output, to console and log display,
  // respectively.
  private static Logger LOGGER = null;
  public static void setLogger(final Logger aLogger) {
    LOGGER = aLogger;
  }


  //#########################################################################
  //# Static Class Constants
  private static final long serialVersionUID = 1L;
  private static final String IDENAME = "Supremica";

  static
  {
    Locale.setDefault(Locale.ENGLISH);
    Config.XML_RPC_ACTIVE.set(false);
    Config.DOT_USE.set(true);
    Config.LOG_TO_CONSOLE.set(false); // why?
    Config.LOG_TO_GUI.set(true);
  }
}
