//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.set.hash.THashSet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

public class AnalyzerHideDialog extends JDialog {

  //#######################################################################
  //# Constructors
  public AnalyzerHideDialog(final IDE ide, final WatersAnalyzerPanel panel) {

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

    final AnalyzerHideDialog dialog = this;
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
    mKeepOriginalCheckBox =
      new JCheckBox("Keep Original", true);
    mKeepOriginalCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    controlPane.add(mKeepOriginalCheckBox);
    mUseTauEventCheckBox =
      new JCheckBox("Use TAU Event");
    mUseTauEventCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    controlPane.add(mUseTauEventCheckBox);


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

  private void hideEvents(final GUIOptionContext context,
                          final EventEncoding enc)
                            throws AnalysisException {

    final ProductDESProxyFactory factory = context.getProductDESProxyFactory();

    final AutomatonProxy aut = context.getProductDES()
      .getAutomata()
      .iterator()
      .next();

    final EventEncoding newEnc = new EventEncoding();
    final KindTranslator translator = IdenticalKindTranslator.getInstance();

    final boolean useTauEvent = mUseTauEventCheckBox.isSelected();

    final Object[][] data = mStatusPanel.getData();
    final Set<EventProxy> localEvents = new THashSet<>();
    for (final Object[] row : data) {
      final EventProxy event = (EventProxy) row[0];
      final boolean local = (boolean)row[1];
      if (useTauEvent && local) {
        localEvents.add(event);
      } else {
        final int newStatus =
          (((boolean)row[1]) ? EventStatus.STATUS_LOCAL : 0)
        | (((boolean)row[2]) ? EventStatus.STATUS_SELFLOOP_ONLY : 0)
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
      if (useTauEvent) {
        if (localEvents.contains(enc.getTauEvent())) {
          newEnc.addSilentEvent(enc.getTauEvent());
        }
        else {
          final String name =
            generateTauName(":tau",
                            aut.getEvents());
          final EventProxy silent =
            factory.createEventProxy(name, EventKind.UNCONTROLLABLE, false);
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
      final String newName = model.getUnusedName(result.getName());
      result.setName(newName);
      model.insertRow(result);
      final List<AutomatonProxy> list = Arrays.asList(new AutomatonProxy[] {result});
      final AutomataTable table = panel.getAutomataTable();
      panel.getAutomataTable().scrollToVisible(list);
      table.clearSelection();
      table.addToSelection(list);
    } else {
      model.replaceAutomaton(aut, result);
    }
  }

  private String generateTauName(final String baseName,
                                 final Collection<EventProxy> allEvents) {
    String name = null;
    for (int n=0;; n++) {
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
    final Set<EventProxy> visibleEvents = new THashSet<>();
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

  //#######################################################################
  //# Data Members
  private final EventSetPanel mEventPanel;
  private final EventStatusPanel mStatusPanel;
  private final JCheckBox mKeepOriginalCheckBox;
  private final JCheckBox mUseTauEventCheckBox;

  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = -8543364648387189552L;

}
