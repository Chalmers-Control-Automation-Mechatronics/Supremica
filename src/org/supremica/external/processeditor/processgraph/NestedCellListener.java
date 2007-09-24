package org.supremica.external.processeditor.processgraph;

import java.awt.*;
import javax.swing.*;
/**
 * Handles the communication from a nested cell and to its listener.
 */
public interface NestedCellListener {   
    /**
     * Rebuilds this object.
     */
    public void rebuild();    
    /**
     * Returns a copy of this object.
     */
    public NestedCell copy();
    /**
     * Deletes this object.
     */
    public void delete();
    /**
     * Pastes <code>o</code> to this object.
     *
     * @param o the object to be pasted
     */
    public void paste(Object o);
    /**
     * Creates a outer relation.
     */
    public void createOuterRelation();
    /**
     * Removes a outer relation.
     */
    public void removeOuterRelation();
    /**
     * Removes a outer relation and replace this object with
     *  <code>element</code>.
     *
     * @param element the object that will replace this object
     */
    public void removeOuterRelation(Object element);
    /**
     * Deletes <code>element</code> from this object.
     *
     * @param element the object to be deleted
     */
    public void elementDelete(Object element);
    /**
     * Adds <code>newElement</code> to this object. 
     * <p>
     * The <code>newElement</code> is inserted next to the
     * <code>oldElement</code>.
     *
     * @param oldElement the object where to insert the new object next to
     * @param newElement the object is to  be added
     */
    public void elementAdd(Object oldElement, Object newElement);
    /**
     * Replaces the <code>oldElement</code> with the the 
     * <code>newElement</code>.
     *
     * @param oldElement the object that is to be replaced
     * @param newElement the object that is to replace the old object
     */
    public void elementReplace(Object oldElement, Object newElement);
    /**
     * Replaces this object with the <code>newElement</code>.
     * 
     * @param newElement the object that is to replace this object
     */
    public void elementReplace(Object newElement);
    /**
     * Pastes the <code>newElement</code> next to this object.
     *
     * @param newElement the object to be pasted
     */
    public void elementPaste(Object newElement);    
    /**
     * Sets the list.
     *
     * @param list the concerned list
     */
    public void setList(JList list);
    /**
     * Sets the attribute type color.
     */
    public void setAttributeTypeColor();
    /**
     * Sets unique names for the object <code>o</code>.
     *
     * @param o the object to give unique name
     * @return the object with unique name
     */
    public Object setUniqueNames(Object o);        
}
