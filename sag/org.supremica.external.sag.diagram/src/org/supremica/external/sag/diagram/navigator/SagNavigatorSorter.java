package org.supremica.external.sag.diagram.navigator;

import org.eclipse.jface.viewers.ViewerSorter;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagNavigatorSorter extends ViewerSorter {

	/**
	 * @generated
	 */
	private static final int GROUP_CATEGORY = 7004;

	/**
	 * @generated
	 */
	public int category(Object element) {
		if (element instanceof SagNavigatorItem) {
			SagNavigatorItem item = (SagNavigatorItem) element;
			return SagVisualIDRegistry.getVisualID(item.getView());
		}
		return GROUP_CATEGORY;
	}

}
