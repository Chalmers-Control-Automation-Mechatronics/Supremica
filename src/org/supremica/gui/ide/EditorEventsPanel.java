//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorEventsPanel
//###########################################################################
//# $Id: EditorEventsPanel.java,v 1.20 2007-01-30 08:51:28 flordal Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventDeclListView;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.gui.WhiteScrollPane;


class EditorEventsPanel
    extends WhiteScrollPane
{
    private static final long serialVersionUID = 1L;
    
    private String name;
    private ModuleContainer moduleContainer;
    
    private final JList mEventList;
    
    EditorModuleEventPopupMenu popup = new EditorModuleEventPopupMenu();
    PopupListener popupListener;
    
    EditorEventsPanel(ModuleContainer moduleContainer, String name)
    {
        this.moduleContainer = moduleContainer;
        this.name = name;
        final ModuleSubject module = moduleContainer.getModule();
        mEventList = new EventDeclListView(module);
        getViewport().add(mEventList);
        
        popupListener = new PopupListener();
        addMouseListener(popupListener);
        mEventList.addMouseListener(popupListener);
        JMenu setKindMenu = new JMenu("Set controllability");
        popup.add(setKindMenu);
        
        JMenuItem setControllabilityMenu = new JMenuItem("Controllable");
        setKindMenu.add(setControllabilityMenu);
        setControllabilityMenu.addActionListener(new SetControllableAction());
        
        JMenuItem setUncontrollabilityMenu = new JMenuItem("Uncontrollable");       
        setKindMenu.add(setUncontrollabilityMenu);
        setUncontrollabilityMenu.addActionListener(new SetUncontrollableAction());

        JMenuItem setDeleteMenu = new JMenuItem("Delete event");       
        popup.add(setDeleteMenu);
        setDeleteMenu.addActionListener(new SetDeleteAction());        
    }
    
    public String getName()
    {
        return name;
    }
    
    public void addModuleEvent()
    {
        final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, false, false);
    }
    
    public void addComponentEvent()
    {
        final EditorPanel panel = moduleContainer.getEditorPanel();
        new EventEditorDialog(panel, false, false);
    }
    
    class PopupListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e)
        {
            if (e.isPopupTrigger() && 
                    !mEventList.isSelectionEmpty() && 
                    (mEventList.locationToIndex(e.getPoint()) == mEventList.getSelectedIndex()))
            {
                popup.show(mEventList, e.getX(), e.getY());
            }
        }
    }
    
    class SetControllableAction
        implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            int selectedIndex = mEventList.getSelectedIndex();
            Object selectedObject = mEventList.getModel().getElementAt(selectedIndex);
            if (selectedObject instanceof EventDeclSubject)
            {
                EventDeclSubject selectedEvent = (EventDeclSubject)selectedObject;
                if (selectedEvent.getKind() == EventKind.UNCONTROLLABLE)
                {
                    selectedEvent.setKind(EventKind.CONTROLLABLE);
                }
            }
            else
            {
                System.err.println("SetControllableAction: Unknown selectedObject type");
            }
        }
    }
    
    class SetUncontrollableAction
        implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            int selectedIndex = mEventList.getSelectedIndex();
            Object selectedObject = mEventList.getModel().getElementAt(selectedIndex);
            if (selectedObject instanceof EventDeclSubject)
            {
                EventDeclSubject selectedEvent = (EventDeclSubject)selectedObject;
                if (selectedEvent.getKind() == EventKind.CONTROLLABLE)
                {
                    selectedEvent.setKind(EventKind.UNCONTROLLABLE);
                }
            }
            else
            {
                System.err.println("SetUncontrollableAction: Unknown selectedObject type");
            }            
        }
    }   

    class SetDeleteAction
        implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            int selectedIndex = mEventList.getSelectedIndex();
            Object selectedObject = mEventList.getModel().getElementAt(selectedIndex);
            if (selectedObject instanceof EventDeclSubject)
            {
                EventDeclSubject selectedEvent = (EventDeclSubject)selectedObject;
                if (selectedEvent.getKind() == EventKind.CONTROLLABLE || selectedEvent.getKind() == EventKind.UNCONTROLLABLE)
                {
                    IndexedListSubject<EventDeclSubject> events = moduleContainer.getModule().getEventDeclListModifiable();
                    events.remove(selectedEvent);
                }
            }
            else
            {
                System.err.println("SetUncontrollableAction: Unknown selectedObject type");
            }            
        }
    }   
}
