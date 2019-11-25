//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.actions;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.EventSetOption.DefaultKind;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.gui.LoggerErrorDisplay;
import net.sourceforge.waters.gui.analyzer.AutomataTable;
import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.EventSetPanel;
import net.sourceforge.waters.gui.analyzer.EventStatusPanel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;

import org.supremica.gui.ide.IDE;


/**
 * The action to invoke the Hide Events dialog in the Waters analyser.
 *
 * @author Benjamin Wheeler
 */

public class AnalyzerHideAction extends WatersAnalyzerAction
{
  //#########################################################################
  //# Constructor
  protected AnalyzerHideAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Hide Events...");
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent arg0)
  {
    final IDE ide = getIDE();
    if (ide != null) {
      final WatersAnalyzerPanel panel = getAnalyzerPanel();
      final AutomataTable table = panel.getAutomataTable();
      final int rowCount = table.getSelectedRowCount();
      if (rowCount == 1) {
        new HiderDialog(ide);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final AutomataTable table = getAnalyzerTable();
    if (table == null) {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION,
               "Hide events in an automaton");
    } else if (table.getSelectedRowCount() == 1) {
      setEnabled(true);
      putValue(Action.SHORT_DESCRIPTION,
               "Hide events in the selected automaton");
    } else {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION,
               "Hide events in an automaton");
    }
  }

  //#########################################################################
  //# Inner Class HiderDialog
  private class HiderDialog extends JDialog {

    //#########################################################################
    //# Constructors
    public HiderDialog(final IDE ide) {

      setTitle("Hide Events");

      final EventSetOption option =
        new EventSetOption(null,
                           "Name",
                           null,
                           null,
                           DefaultKind.PROPER_EVENT,
                           "Visible Events",
                           "Hidden Events");
      final LoggerErrorDisplay error = new LoggerErrorDisplay();
      final WatersAnalyzerPanel panel = getAnalyzerPanel();
      final GUIOptionContext context = new GUIOptionContext(panel,
                                                      ide,
                                                      error);

      final ProductDESProxy des = context.getProductDES();
      final Set<EventProxy> events = des.getEvents();
      final AutomatonProxy aut = des.getAutomata().iterator().next();
      final EventEncoding enc = aut instanceof TRAutomatonProxy ?
        ((TRAutomatonProxy)aut).getEventEncoding() : new EventEncoding();

      final Set<EventProxy> currentValue = new THashSet<>(events.size());
      for (final EventProxy event : events) {
        final int code = enc.getEventCode(event);
        final byte status = code != -1 ? enc.getProperEventStatus(code) : 0;
        if (!EventStatus.isLocalEvent(status)) {
          currentValue.add(event);
        }
      }

      mEventPanel = new EventSetPanel(context, option, currentValue);
      mStatusPanel = new EventStatusPanel(context, events, enc);

      final JPanel pane = new JPanel();
      pane.setLayout(new BorderLayout());
      setContentPane(pane);

      final JTabbedPane tabbedPane = new JTabbedPane();
      pane.add(tabbedPane, BorderLayout.CENTER);
      tabbedPane.add("Basic", mEventPanel);
      tabbedPane.add("Advanced", mStatusPanel);
      tabbedPane.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(final ChangeEvent e)
        {
          final Component comp = tabbedPane.getSelectedComponent();
          if (comp == mEventPanel) {
            updateEventPanel();
          }
          else {
            updateStatusPanel();
          }
        }
      });



      final JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));

      final HiderDialog dialog = this;
      final JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          try {
            if (tabbedPane.getSelectedComponent() == mEventPanel) {
              updateStatusPanel();
            }
            dialog.hideEvents(context, enc);
            dispose();
          } catch (final AnalysisException ex) {
            LogManager.getLogger().error(ex.getMessage());
          }
        }
      });
      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });

      buttonPane.add(okButton);
      buttonPane.add(cancelButton);

      final JPanel controlPane = new JPanel();
      controlPane.setLayout(new GridLayout(0, 2));

      final Set<EventProxy> allOtherEvents = panel
        .getAutomataTable()
        .getAllSelectableItems()
        .stream()
        .filter(a->a != aut)
        .flatMap(a->a.getEvents().stream())
        .collect(Collectors.toSet());

      boolean uncontrollableEventExists = false;
      boolean unobservableEventExists = false;
      boolean sharedEventExists = false;
      for (final EventProxy event : aut.getEvents()) {
        if (event.getKind() == EventKind.PROPOSITION) continue;
        if (event.getKind() == EventKind.CONTROLLABLE) {
          mControllableEvents.add(event);
        } else uncontrollableEventExists = true;
        if (event.isObservable()) {
          mObservableEvents.add(event);
        } else unobservableEventExists = true;
        if (!allOtherEvents.contains(event)) {
          mLocalEvents.add(event);
        } else sharedEventExists = true;
      }

      addSelectionButtons(controlPane, mControllableEvents, uncontrollableEventExists,
                          "Select Controllable", "Select Uncontrollable");
      addSelectionButtons(controlPane, mObservableEvents, unobservableEventExists,
                          "Select Observable", "Select Unobservable");
      addSelectionButtons(controlPane, mLocalEvents, sharedEventExists,
                          "Select Local", "Select Shared");

      mKeepOriginalCheckBox =
        new JCheckBox("Keep Original", true);
      mKeepOriginalCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
      controlPane.add(mKeepOriginalCheckBox);
      mPreserveControllabilityCheckBox =
        new JCheckBox("Preserve Controllability");
      mPreserveControllabilityCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
      controlPane.add(mPreserveControllabilityCheckBox);

      final JPanel allButtonsPane = new JPanel();
      pane.add(allButtonsPane, BorderLayout.SOUTH);
      allButtonsPane.setLayout(new BorderLayout());
      allButtonsPane.add(controlPane, BorderLayout.NORTH);
      allButtonsPane.add(buttonPane, BorderLayout.SOUTH);

      final Component parent = context.getDialogParent();
      setLocationRelativeTo(parent);
      pack();
      setVisible(true);

    }

    private void addSelectionButtons(final JPanel controlPane,
                                     final Set<EventProxy> events,
                                     final boolean otherExists,
                                     final String givenButtonLabel,
                                     final String otherButtonLabel) {

      if (events.size() != 0 && otherExists) {
        final JButton controllableEventsButton = new JButton(givenButtonLabel);
        controllableEventsButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e)
          {
            mEventPanel.setListSelection(events, false);
          }
        });
        controlPane.add(controllableEventsButton);
        final JButton uncontrollableEventsButton = new JButton(otherButtonLabel);
        uncontrollableEventsButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e)
          {
            mEventPanel.setListSelection(events, true);
          }
        });
        controlPane.add(uncontrollableEventsButton);
      }

    }

    private void hideEvents(final GUIOptionContext context, final EventEncoding enc)
      throws AnalysisException {

      final ProductDESProxyFactory factory = context.getProductDESProxyFactory();

      final AutomatonProxy aut = context.getProductDES()
        .getAutomata()
        .iterator()
        .next();

      final EventEncoding newEnc = new EventEncoding();
      final KindTranslator translator = IdenticalKindTranslator.getInstance();

      final Object[][] data = mStatusPanel.getData();
      final Set<EventProxy> localEvents = new THashSet<>();
      for (final Object[] row : data) {
        final EventProxy event = (EventProxy) row[0];
        final boolean local = (boolean)row[1];
        if (local) {
          localEvents.add(event);
        } else {
          final int newStatus =
            (((boolean)row[2]) ? EventStatus.STATUS_SELFLOOP_ONLY : 0)
          | (((boolean)row[3]) ? EventStatus.STATUS_ALWAYS_ENABLED : 0)
          | (((boolean)row[4]) ? EventStatus.STATUS_BLOCKED : 0)
          | (((boolean)row[5]) ? EventStatus.STATUS_FAILING : 0);
          newEnc.addEvent(event, translator, newStatus);
        }
      }
      for (final EventProxy event : aut.getEvents()) {
        if (event.getKind() == EventKind.PROPOSITION) {
          newEnc.addProposition(event, true);
        }
      }

      if (localEvents.size() != 0) {
        if (mPreserveControllabilityCheckBox.isSelected()) {
          final Set<EventProxy> controllableEvents =
            new THashSet<EventProxy>();
          final Set<EventProxy> uncontrollableEvents =
            new THashSet<EventProxy>();
          for (final EventProxy event : localEvents) {
            if (event.getKind() == EventKind.CONTROLLABLE) {
              controllableEvents.add(event);
            } else {
              uncontrollableEvents.add(event);
            }
          }
          final String nameC = generateTauName("tauC", aut.getEvents());
          final String nameU = generateTauName("tauU", aut.getEvents());

          final EventProxy tauC =
            factory.createEventProxy(nameC, EventKind.CONTROLLABLE);
          final EventProxy tauU =
            factory.createEventProxy(nameU, EventKind.UNCONTROLLABLE);

          for (final EventProxy event : controllableEvents) {
            newEnc.addEventAlias(event,
                                 tauC,
                                 IdenticalKindTranslator.getInstance(),
                                 EventStatus.STATUS_CONTROLLABLE
                                 | EventStatus.STATUS_LOCAL);
          }
          for (final EventProxy event : uncontrollableEvents) {
            newEnc.addEventAlias(event,
                                 tauU,
                                 IdenticalKindTranslator.getInstance(),
                                 EventStatus.STATUS_LOCAL);
          }
        }
        else {
          if (localEvents.contains(enc.getTauEvent())) {
            newEnc.addSilentEvent(enc.getTauEvent());
          }
          else {
            final String name = generateTauName("tau", aut.getEvents());
            final EventProxy silent =
              factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
            newEnc.addSilentEvent(silent);
          }
          for (final EventProxy event : localEvents) {
            newEnc.addSilentEvent(event);
          }
        }
      }

      final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      final TRAutomatonProxy result = new TRAutomatonProxy(aut, newEnc, config);

      final WatersAnalyzerPanel panel = context.getWatersAnalyzerPanel();
      final AutomataTableModel model = panel.getAutomataTableModel();

      if (mKeepOriginalCheckBox.isSelected()) {
        model.insertRow(result);
      } else {
        model.replaceAutomaton(aut, result);
      }
    }

    private String generateTauName(final String baseName,
                                 final Collection<EventProxy> allEvents) {
      String name = null;
      for (int n=0; n<1000; n++) {
        name = (n == 0) ? baseName : baseName+":"+n;
        boolean found = false;
        for (final EventProxy event : allEvents) {
          if (event.getName().equals(name)) {
            found = true;
            break;
          }
        }
        if (!found) {
          break;
        }
      }
      return name;
    }

    private void updateEventPanel() {
      final Set<EventProxy> visibleEvents = new THashSet<EventProxy>();
      for (final Object[] row : mStatusPanel.getData()) {
        final EventProxy event = (EventProxy) row[0];
        final boolean local = (boolean) row[1];
        if (!local) {
          visibleEvents.add(event);
        }
      }
      mEventPanel.setSelectedEvents(visibleEvents);
    }

    private void updateStatusPanel() {
      final Set<EventProxy> visibleEvents = mEventPanel.getSelectedEvents();
      for (final Object[] row : mStatusPanel.getData()) {
        final EventProxy event = (EventProxy) row[0];
        row[1] = !visibleEvents.contains(event);
      }
    }

    //#########################################################################
    //# Data Members
    private final EventSetPanel mEventPanel;
    private final EventStatusPanel mStatusPanel;

    private final Set<EventProxy> mControllableEvents = new THashSet<EventProxy>();
    private final Set<EventProxy> mObservableEvents = new THashSet<EventProxy>();
    private final Set<EventProxy> mLocalEvents = new THashSet<EventProxy>();

    private final JCheckBox mKeepOriginalCheckBox;
    private final JCheckBox mPreserveControllabilityCheckBox;

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = -8543364648387189552L;

  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 636028154288275788L;

}
