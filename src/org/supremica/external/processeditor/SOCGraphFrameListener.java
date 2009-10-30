package org.supremica.external.processeditor;

import org.supremica.external.processeditor.xgraph.Selection;

/**
 * Enables the parent class to listen to the child <code>SOCGraphFrame</code>
 * instance.
 */
public interface SOCGraphFrameListener {

    /**
     * The <code>Selection</code> in the child frame has been changed.
     *
     * @param s the selection that is changed
     */
    public void selectionChanged(Selection s);

}
