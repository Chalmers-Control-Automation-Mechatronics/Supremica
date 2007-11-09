package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

public interface EditableCellListener {

    public void copy(BaseCell cell);

    public void delete(BaseCell cell);

    public void paste(BaseCell cell);
	
	public void replace(BaseCell oldCell, BaseCell newCell);
	
	public void modified(BaseCell cell);

    /*
    public void elementDelete(Object element);

    public void elementAdd(Object oldElement, Object newElement);
        
    public void elementReplace(Object oldElement, Object newElement);

    public void elementPaste(Object newElement);
    */
}
