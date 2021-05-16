//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.gui.about.AboutPanel;
import net.sourceforge.waters.gui.about.WelcomeScreen;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.logging.IDEAppender;
import net.sourceforge.waters.gui.logging.IDELogConfigurationFactory;
import net.sourceforge.waters.gui.logging.LogPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import org.supremica.automata.Project;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.ExitAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
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
  implements IDEActionInterface, Observer, Subject,
             ComponentListener, WindowStateListener
{

  //#########################################################################
  //# Constructors
  public IDE()
    throws SAXException, ParserConfigurationException
  {
    // Instantiate all actions
    mObservers = new LinkedList<Observer>();
    mFocusTracker = new FocusTracker(this);
    mFocusTracker.attach(new Observer() {
      @Override
      public void update(final EditorChangedEvent event)
      {
        // Events from focus tracker are forwarded to IDE's listeners.
        fireEditorChangedEvent(event);
      }});
    mActions = new Actions(this);
    mPopupActionManager = new WatersPopupActionManager(this);
    mModuleNameObserver = new ModuleNameObserver();

    // Set frame size and position from configuration file
    final Dimension size = new Dimension(Config.GUI_IDE_WIDTH.getValue(),
                                         Config.GUI_IDE_HEIGHT.getValue());
    getContentPane().setPreferredSize(size);
    if (Config.GUI_IDE_MAXIMIZED.getValue()) {
      setExtendedState(Frame.MAXIMIZED_BOTH);
    } else {
      setLocation(Config.GUI_IDE_XPOS.getValue(),
                  Config.GUI_IDE_YPOS.getValue());
    }
    final List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
    setIconImages(images);

    // Create GUI
    final BorderLayout layout = new BorderLayout();
    final JPanel contents = (JPanel) getContentPane();
    contents.setLayout(layout);
    menuBar = new IDEMenuBar(this);
    setJMenuBar(menuBar);
    mToolBar = new IDEToolBar(this);
    contents.add(mToolBar, BorderLayout.NORTH);
    mWelcomeScreen = new WelcomeScreen(this);
    mLogPanel = new LogPanel(mPopupActionManager);
    mSplitPaneVertical =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, mWelcomeScreen, mLogPanel);
    mSplitPaneVertical.setContinuousLayout(false);
    mSplitPaneVertical.setOneTouchExpandable(false);
    mSplitPaneVertical.setResizeWeight(1.0);
    contents.add(mSplitPaneVertical, BorderLayout.CENTER);
    pack();
    mSplitPaneVertical.setDividerLocation(0.9);

    final File startdir = new File(Config.FILE_OPEN_PATH.getAsString());
    mFileChooser = new JFileChooser(startdir);

    // Initialise document managers and register listeners
    mDocumentContainerManager = new DocumentContainerManager(this);
    attach(this);
    addComponentListener(this);
    addWindowStateListener(this);
    updateWindowTitle();
  }


  //#########################################################################
  //# Simple Access
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

  public LogPanel getLogPanel()
  {
    return mLogPanel;
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
      final int dividerLocation = mSplitPaneVertical.getDividerLocation();
      if (container == null) {
        mSplitPaneVertical.setTopComponent(mWelcomeScreen);
      } else {
        final Component panel = container.getPanel();
        mSplitPaneVertical.setTopComponent(panel);
      }
      mSplitPaneVertical.setDividerLocation(dividerLocation);
      // Sometimes the divider gets set to a strange position. (Why???)
      // The following delayed call seems to fix it ...
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          mSplitPaneVertical.setDividerLocation(dividerLocation);
        }
      });
      mModuleNameObserver.setModuleContainer(container);
      updateWindowTitle();
      break;
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      updateWindowTitle();
      break;
    default:
      break;
    }
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
    for (final Observer observer : copy) {
      observer.update(event);
    }
  }


  //#########################################################################
  //# Interface java.awt.event.ComponentListener
  @Override
  public void componentResized(final ComponentEvent event)
  {
    if (getExtendedState() == Frame.NORMAL) {
      final Container contentPane = getContentPane();
      final int width = contentPane.getWidth();
      final int height = contentPane.getHeight();
      Config.GUI_IDE_WIDTH.setValue(width);
      Config.GUI_IDE_HEIGHT.setValue(height);
    }
  }

  @Override
  public void componentMoved(final ComponentEvent event)
  {
    if (getExtendedState() == Frame.NORMAL) {
      Config.GUI_IDE_XPOS.setValue(getX());
      Config.GUI_IDE_YPOS.setValue(getY());
    }
  }

  @Override
  public void componentShown(final ComponentEvent event)
  {
  }

  @Override
  public void componentHidden(final ComponentEvent event)
  {
  }


  //#########################################################################
  //# Interface java.awt.event.WindowStateListener
  @Override
  public void windowStateChanged(final WindowEvent event)
  {
    final int state = event.getNewState();
    Config.GUI_IDE_MAXIMIZED.setValue(state == Frame.MAXIMIZED_BOTH);
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
  public ComponentEditorPanel getActiveComponentEditorPanel()
  {
    final DocumentContainer active = getActiveDocumentContainer();
    if (active == null) {
      return null;
    }
    final EditorPanel panel = active.getEditorPanel();
    if (panel == null) {
      return null;
    } else {
      return panel.getActiveComponentEditorPanel();
    }
  }

  public Project getActiveProject()
  {
    final DocumentContainer active = getActiveDocumentContainer();
    return active.getSupremicaAnalyzerPanel().getVisualProject();
  }


  //#########################################################################
  //# Logging
  private void initializeLoggers(final boolean showVersionInLogPanel)
  {
    IDEAppender.configure(mLogPanel);
    if (showVersionInLogPanel) {
      final Logger logger = LogManager.getLogger();
      logger.info(Version.getInstance().toString());
      final int mb = 1024 * 1024;
      logger.info("JVM:" + System.getProperty("java.version") +
                  ", Free/Total/Max mem: " +
                  Runtime.getRuntime().freeMemory()/mb + "/" +
                  Runtime.getRuntime().totalMemory()/mb + "/" +
                  Runtime.getRuntime().maxMemory()/mb + " MiB");
    }
  }


  //#########################################################################
  //# Main Program
  public static void main(final String args[])
    throws Exception
  {
    // Process command line arguments and load configuration
    final List<File> files = ProcessCommandLineArguments.process(args);
    // Initialise look & feel, load fonts and icons
    IconAndFontLoader.initialize();
    // If student version: make sure Waters library is loadable
    AboutPanel.performStudentVersionCheck();
    // Start the GUI
    final boolean hasFiles = (files != null && files.size() > 0);
    final IDE ide = new IDE();
    // Open initial module(s)
    if (hasFiles) {
      ide.openFiles(files);
    } else if (Config.GUI_EDITOR_DEFAULT_EMPTY_MODULE.getValue()) {
      ide.openEmptyDocument();
    }
    // Show!
    ide.setVisible(true);
    ide.initializeLoggers(hasFiles ||
                          Config.GUI_EDITOR_DEFAULT_EMPTY_MODULE.getValue());
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
      return mWelcomeScreen.getWindowTitle();
    } else {
      final String title = container.getWindowTitle();
      return title == null || title.length() == 0 ?
             mWelcomeScreen.getWindowTitle() : title;
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
      final net.sourceforge.waters.subject.base.Subject sender = event.getSource();
      final int kind = event.getKind();
      if (sender == mModule && kind == ModelChangeEvent.NAME_CHANGED) {
        updateWindowTitle();
      } else if (kind == ModelChangeEvent.NAME_CHANGED ||
                 kind == ModelChangeEvent.STATE_CHANGED) {
        final ComponentEditorPanel panel = getActiveComponentEditorPanel();
        if (panel != null) {
          final SimpleComponentSubject comp = panel.getComponent();
          if (comp == sender ||
              SubjectTools.isAncestor(comp.getIdentifier(), sender)) {
            updateWindowTitle();
          }
        }
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
    }

    //#######################################################################
    //# Module Switching
    private void setModuleContainer(final DocumentContainer container)
    {
      if (container instanceof ModuleContainer) {
        final ModuleContainer moduleContainer = (ModuleContainer) container;
        final ModuleSubject module = moduleContainer.getModule();
        setModule(module);
      } else {
        setModule(null);
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
  private final WelcomeScreen mWelcomeScreen;
  private final JSplitPane mSplitPaneVertical;
  private final LogPanel mLogPanel;
  private final JFileChooser mFileChooser;


  //#########################################################################
  //# Static Class Constants
  private static final long serialVersionUID = -3896438636773221026L;

  static {
    // Configure loggers in static initialiser,
    // making sure it happens before logging is used.
    final ConfigurationFactory factory = new IDELogConfigurationFactory();
    ConfigurationFactory.setConfigurationFactory(factory);
  }

}
