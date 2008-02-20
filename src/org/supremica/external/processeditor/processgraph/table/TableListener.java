package org.supremica.external.processeditor.processgraph.table;

public interface TableListener {
    public void tableSelectionChanged(TableEvent e);
    
    public void rowAdded(TableEvent e);
    public void columnAdded(TableEvent e);
    
    public void columnRemoved(TableEvent e);
    public void rowRemoved(TableEvent e);
}
