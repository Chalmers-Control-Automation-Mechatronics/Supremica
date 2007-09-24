package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.*;
import javax.swing.*;

/**
 * Handles the communication between a attribute object and 
 * its container.
 */
public interface AttributeListener {   
    /**
     * Is invoked when a change to the attribute has been made.
     */
    public void attributeChanged();
    /**
     * Removes selection.
     */
    public void removeSelection();
    /**
     * Repaint this object and its container.
     */
    public void repaint();
    /**
     * Rebuild this object's container.
     */
    public void rebuild();    
    /**
     * Sets the list.
     *
     * @param list the concerned list
     */
    public void setList(JList list);
    /**
     * Packs this object and its container.
     */
    public void upPack();   
    /**
     * Sets the attribute type color.
     */
    public void setAttributeTypeColor();
}
