//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.EventSetOption.DefaultKind;
import net.sourceforge.waters.gui.options.GUIOptionContext;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

public class EventSetPanel extends JPanel
{

  public EventSetPanel(final GUIOptionContext context,
                       final EventSetOption option,
                       final Set<EventProxy> currentValue) {

    mContext = context;

    final ProductDESProxy des = context.getProductDES();
    final DefaultKind defaultKind = option.getDefaultKind();
    setLayout(new GridLayout(0, 2));

    mSelectedModel = new DefaultListModel<>();
    mUnselectedModel = new DefaultListModel<>();
    for (final EventProxy event : des.getEvents()) {
      if (!defaultKind.isChoosable(event.getKind())) {
        // skip
      } else if (currentValue.contains(event)) {
        mSelectedModel.addElement(event);
      } else {
        mUnselectedModel.addElement(event);
      }
    }
    mUnselectedList = createListView(mUnselectedModel);
    final JScrollPane unselectedScroll = new JScrollPane(mUnselectedList);
    mSelectedList = createListView(mSelectedModel);
    final JScrollPane selectedScroll = new JScrollPane(mSelectedList);

    final TransferHandler unselectedHandler = new EventListTransferHandler(mSelectedList);
    mUnselectedList.setTransferHandler(unselectedHandler);
    final TransferHandler selectedHandler = new EventListTransferHandler(mUnselectedList);
    mSelectedList.setTransferHandler(selectedHandler);

    mUnselectedList.setDragEnabled(true);
    mSelectedList.setDragEnabled(true);

    final JButton selectButton =
      createButton("\u25b6", mSelectedList, mUnselectedList);
    selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    final JButton unselectButton =
      createButton("\u25c0", mUnselectedList, mSelectedList);
    unselectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    final JLabel unselectedLabel = new JLabel(option.getUnselectedTitle());
    unselectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    final JLabel selectedLabel = new JLabel(option.getSelectedTitle());
    selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    final JPanel unselectedPanel = new JPanel();
    unselectedPanel.setLayout(new BoxLayout(unselectedPanel, BoxLayout.Y_AXIS));
    unselectedPanel.add(unselectedLabel);
    unselectedPanel.add(unselectedScroll);
    unselectedPanel.add(selectButton);

    final JPanel selectedPanel = new JPanel();
    selectedPanel.setLayout(new BoxLayout(selectedPanel, BoxLayout.Y_AXIS));
    selectedPanel.add(selectedLabel);
    selectedPanel.add(selectedScroll);
    selectedPanel.add(unselectButton);

    final JPanel eventListPane = new JPanel();
    eventListPane.setLayout(new GridLayout(0, 2));
    eventListPane.add(unselectedPanel);
    eventListPane.add(selectedPanel);


    final JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new GridLayout(0, 2));

    final AutomatonProxy aut = des.getAutomata().iterator().next();
    final Set<EventProxy> allOtherEvents = context
      .getWatersAnalyzerPanel()
      .getAutomataTable()
      .getAllSelectableItems()
      .stream()
      .filter(a->a != aut)
      .flatMap(a->a.getEvents().stream())
      .collect(Collectors.toSet());


    if (defaultKind != DefaultKind.PROPOSITION) {
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

      addSelectionButtons(buttonPane, mControllableEvents, uncontrollableEventExists,
                          "Select Controllable", "Select Uncontrollable");
      addSelectionButtons(buttonPane, mObservableEvents, unobservableEventExists,
                          "Select Observable", "Select Unobservable");
      addSelectionButtons(buttonPane, mLocalEvents, sharedEventExists,
                          "Select Local", "Select Shared");
    }

    setLayout(new BorderLayout());
    add(eventListPane, BorderLayout.CENTER);
    add(buttonPane, BorderLayout.SOUTH);

  }

  private void addSelectionButtons(final JPanel pane,
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
          setListSelection(events, false);
        }
      });
      pane.add(controllableEventsButton);
      final JButton uncontrollableEventsButton = new JButton(otherButtonLabel);
      uncontrollableEventsButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          setListSelection(events, true);
        }
      });
      pane.add(uncontrollableEventsButton);
    }

  }

  public void setSelectedEvents(final Set<EventProxy> selection) {
    for (int e=0; e<mSelectedModel.getSize(); e++) {
      final EventProxy event = mSelectedModel.get(e);
      mUnselectedModel.addElement(event);
    }
    mSelectedModel.removeAllElements();
    for (final EventProxy event : selection) {
      mUnselectedModel.removeElement(event);
      mSelectedModel.addElement(event);
    }
    sortModel(mSelectedModel);
    sortModel(mUnselectedModel);
  }

  public void setListSelection(final Set<EventProxy> selectedEvents, final boolean inverted) {
    mSelectedList.clearSelection();
    for (int e=0; e<mSelectedModel.getSize(); e++) {
      if (inverted != selectedEvents.contains(mSelectedModel.get(e))) {
        mSelectedList.addSelectionInterval(e, e);
      }
    }
    mUnselectedList.clearSelection();
    for (int e=0; e<mUnselectedModel.getSize(); e++) {
      if (inverted != selectedEvents.contains(mUnselectedModel.get(e))) {
        mUnselectedList.addSelectionInterval(e, e);
      }
    }
  }


  //#######################################################################
  //# Simple Access
  public Set<EventProxy> getSelectedEvents()
  {
    final int size = mSelectedModel.getSize();
    final Set<EventProxy> set = new THashSet<>(size);
    for (int i = 0; i < size; i++) {
      final EventProxy event = mSelectedModel.get(i);
      set.add(event);
    }
    return set;
  }

//  public boolean isKeepOriginal() {
//
//  }
//
//  //public boolean is


  //#######################################################################
  //# Auxiliary Methods
  private JList<EventProxy> createListView(final DefaultListModel<EventProxy> model)
  {
    sortModel(model);
    final JList<EventProxy> listView = new JList<>();
    listView.setModel(model);
    listView.setCellRenderer(new EventCellRenderer(mContext));
    listView.setLayoutOrientation(JList.VERTICAL);
    return listView;
  }

   private JButton createButton(final String label,
                                final JList<EventProxy> toList,
                                final JList<EventProxy> fromList)
  {
    final JButton button = new JButton(label);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final DefaultListModel<EventProxy> toModel =
          (DefaultListModel<EventProxy>) toList.getModel();
        final DefaultListModel<EventProxy> fromModel =
          (DefaultListModel<EventProxy>) fromList.getModel();

        final Set<EventProxy> selectedEvents =
          new THashSet<>(fromList.getSelectedValuesList());
        for (final EventProxy proxy : selectedEvents) {
          fromModel.removeElement(proxy);
          toModel.addElement(proxy);
        }
        sortModel(toModel);
        for (int e=0; e<toModel.getSize(); e++) {
          if (selectedEvents.contains(toModel.get(e))) {
            toList.addSelectionInterval(e, e);
          }
        }
      }
    });
    return button;
  }

  private void sortModel(final DefaultListModel<EventProxy> model)
  {
    final Object[] data =  model.toArray();
    Arrays.sort(data);
    model.clear();
    for (final Object o : data) {
      final EventProxy event = (EventProxy) o;
      model.addElement(event);
    }
  }


  //#######################################################################
  //# Data Members
  final JList<EventProxy> mSelectedList;
  final JList<EventProxy> mUnselectedList;
  private final DefaultListModel<EventProxy> mSelectedModel;
  private final DefaultListModel<EventProxy> mUnselectedModel;
  private final GUIOptionContext mContext;

  private final Set<EventProxy> mControllableEvents = new THashSet<EventProxy>();
  private final Set<EventProxy> mObservableEvents = new THashSet<EventProxy>();
  private final Set<EventProxy> mLocalEvents = new THashSet<EventProxy>();

  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 7396539823426021453L;


  //#########################################################################
  //# Inner Class EventCellRenderer
  private class EventCellRenderer extends JLabel
    implements ListCellRenderer<EventProxy>
  {
    //#######################################################################
    //# Constructors
    EventCellRenderer(final GUIOptionContext context) {
      mContext = context;
    }

    //#######################################################################
    //# Interface javax.swing.ListCellRenderer<EventProxy>
    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy event,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      setText(event.getName());
      setIcon(mContext.getEventIcon(event));
      setBorder(new EmptyBorder(2, 2, 2, 2));
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setOpaque(true);
      return this;
    }

    //#######################################################################
    //# Data Members
    private final GUIOptionContext mContext;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 760104252849112475L;
  }


  //#########################################################################
  //# Inner Class EventListTransferHandler
  private class EventListTransferHandler extends TransferHandler {

    //#########################################################################
    //# Constructors
    public EventListTransferHandler(final JList<EventProxy> fromList) {
      mFromList = fromList;
    }

    //#########################################################################
    //# Overrides for javax.swing.TransferHandler
    @Override
    public boolean canImport(final TransferHandler.TransferSupport info) {
      if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return false;
      }
      return true;
    }

    @Override
    protected Transferable createTransferable(final JComponent c)
    {
      return new StringSelection("");
    }

    @Override
    public int getSourceActions(final JComponent c)
    {
      return TransferHandler.MOVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(final TransferSupport info)
    {
      if (!info.isDrop()) return false;

      final JList<EventProxy> toList = (JList<EventProxy>)info.getComponent();
      final DefaultListModel<EventProxy> toModel =
        (DefaultListModel<EventProxy>) toList.getModel();
      final DefaultListModel<EventProxy> fromModel =
        (DefaultListModel<EventProxy>) mFromList.getModel();

      final Set<EventProxy> selectedEvents =
        new THashSet<>(mFromList.getSelectedValuesList());
      for (final EventProxy proxy : selectedEvents) {
        fromModel.removeElement(proxy);
        toModel.addElement(proxy);
      }
      sortModel(toModel);
      for (int e=0; e<toModel.getSize(); e++) {
        if (selectedEvents.contains(toModel.get(e))) {
          toList.addSelectionInterval(e, e);
        }
      }

      return true;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void sortModel(final DefaultListModel<EventProxy> model)
    {
      final Object[] data =  model.toArray();
      Arrays.sort(data);
      model.clear();
      for (final Object o : data) {
        final EventProxy event = (EventProxy) o;
        model.addElement(event);
      }
    }

    //#######################################################################
    //# Data Members
    private final JList<EventProxy> mFromList;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 7742226094815575365L;

  }

}
