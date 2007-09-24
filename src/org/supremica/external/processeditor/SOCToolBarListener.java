package org.supremica.external.processeditor;

/**
 * Handles the icon events.
 */
public interface SOCToolBarListener {

    /** 
     * The <i>New icon</i> is pressed.
     *
     * @version 0.1
     */
    public void newSheet();     
    
    /**
     * The <i>Open icon</i> is pressed.
     *
     * @version 0.1
     */
    public void open();     
    
    /**
     * The <i>Save icon</i> is pressed.
     *
     * @version 0.1
     */
    public void save();				    
    
     /**
     * The <i>Cut icon</i> is pressed.
     *
     * @version 0.1
     */
    public void cut();     
    
     /**
     * The <i>Copy icon</i> is pressed.
     *
     * @version 0.1
     */
    public void copy();     
    
     /**
     * The <i>Paste icon</i> is pressed.
     *
     * @version 0.1
     */
    public void paste();     

     /**
     * The <i>Delete icon</i> is pressed.
     *
     * @version 0.1
     */
    public void delete();     
    
}

