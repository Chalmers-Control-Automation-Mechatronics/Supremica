package org.supremica.external.processeditor.xgraph;

/**
 * Handles the communication from a cell to its listener.
 */
public interface CellListener {

    /**
     * Invoked when a cell is pressed.
     */
    public void cellPressed(CellEvent c);
    /**
     * Invoked when a cell is released.
     */
    public void cellReleased(CellEvent c);
    /**
     * Invoked when a cell is dragged
     */
    public void cellDragged(CellEvent c);
    /**
     * Invoked when a cell is moved
     */
    public void cellMove(CellEvent c);
    /**
     * Removes the specified cell
     */
    public void remove(GraphCell c);
    /**
     * Repaints the contents
     */
    public void repaint();    
    /**
     * Starts a downwarded recursive search
     */
    public void downPack();
    /**
     * Starts a upwarded recursive search
     */
    public void upPack();
    /**
     * Removes selection.
     */
    public void removeSelection();

}
