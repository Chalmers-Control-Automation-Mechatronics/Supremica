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

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.set.hash.THashSet;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;

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
    final JList<EventProxy> unselectedList = createListView(mUnselectedModel);
    final JScrollPane unselectedScroll = new JScrollPane(unselectedList);
    final JList<EventProxy> selectedList = createListView(mSelectedModel);
    final JScrollPane selectedScroll = new JScrollPane(selectedList);

    final TransferHandler unselectedHandler = new EventListTransferHandler(selectedList);
    unselectedList.setTransferHandler(unselectedHandler);
    final TransferHandler selectedHandler = new EventListTransferHandler(unselectedList);
    selectedList.setTransferHandler(selectedHandler);

    unselectedList.setDragEnabled(true);
    selectedList.setDragEnabled(true);

    final JButton selectButton =
      createButton("\u25b6", unselectedList, mUnselectedModel, mSelectedModel);
    final JButton unselectButton =
      createButton("\u25c0", selectedList, mSelectedModel, mUnselectedModel);


    setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = INSETS;
    constraints.gridy = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.fill = GridBagConstraints.NONE;
    add(new JLabel(option.getUnselectedTitle()), constraints);
    add(new JLabel(option.getSelectedTitle()), constraints);

    constraints.gridy++;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    add(unselectedScroll, constraints);
    add(selectedScroll, constraints);

    constraints.gridy++;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.fill = GridBagConstraints.NONE;
    add(selectButton, constraints);
    add(unselectButton, constraints);
    constraints.gridy++;

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
                                final JList<EventProxy> fromList,
                                final DefaultListModel<EventProxy> fromModel,
                                final DefaultListModel<EventProxy> toModel)
  {
    final JButton button = new JButton(label);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        for (final EventProxy proxy : fromList.getSelectedValuesList()) {
          fromModel.removeElement(proxy);
          toModel.addElement(proxy);
        }
        sortModel(toModel);
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
  private final DefaultListModel<EventProxy> mSelectedModel;
  private final DefaultListModel<EventProxy> mUnselectedModel;
  private final GUIOptionContext mContext;

  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = 7396539823426021453L;

  private static final Insets INSETS = new Insets(2, 4, 2, 4);


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

      for (final EventProxy proxy : mFromList.getSelectedValuesList()) {
        fromModel.removeElement(proxy);
        toModel.addElement(proxy);
      }
      sortModel(toModel);

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
