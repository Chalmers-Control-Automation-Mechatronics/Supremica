package org.supremica.external.processeditor.xgraph;
/**
 * Handles the communication from a selection object to its listener.
 */
public interface SelectionListener {
    /**
     * Notifies the listener that changes to the selection object has been made.
     */
    public void selectionChanged(Selection s);

}
