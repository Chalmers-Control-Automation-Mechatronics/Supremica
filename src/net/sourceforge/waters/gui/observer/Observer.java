package net.sourceforge.waters.gui.observer;

/**
 * The Observer Interface for the Observer Design Pattern.
 *
 * It makes So many it so that many method of viewing and Modifying Concrete data
 * representation can Operate on the Same data representation at the Same Time and 
 * can be Updated Simultaneously
 *
 * @author Simon Ware
 */

public interface Observer
{
    /**
     * called to update the observer when its subject is modified
     */
    public void update(EditorChangedEvent e);
}
