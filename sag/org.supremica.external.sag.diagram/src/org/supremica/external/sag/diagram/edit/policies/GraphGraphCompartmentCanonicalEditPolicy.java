package org.supremica.external.sag.diagram.edit.policies;

import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;

import org.supremica.external.sag.Graph;

import org.supremica.external.sag.diagram.edit.parts.EndNodeEditPart;
import org.supremica.external.sag.diagram.edit.parts.SensorEditPart;

import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class GraphGraphCompartmentCanonicalEditPolicy extends
		CanonicalEditPolicy {

	/**
	 * @generated
	 */
	protected List getSemanticChildrenList() {
		List result = new LinkedList();
		EObject modelObject = ((View) getHost().getModel()).getElement();
		View viewObject = (View) getHost().getModel();
		EObject nextValue;
		int nodeVID;
		for (Iterator values = ((Graph) modelObject).getNode().iterator(); values
				.hasNext();) {
			nextValue = (EObject) values.next();
			nodeVID = SagVisualIDRegistry
					.getNodeVisualID(viewObject, nextValue);
			switch (nodeVID) {
			case SensorEditPart.VISUAL_ID: {
				result.add(nextValue);
				break;
			}
			case EndNodeEditPart.VISUAL_ID: {
				result.add(nextValue);
				break;
			}
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected boolean shouldDeleteView(View view) {
		return view.isSetElement() && view.getElement() != null
				&& view.getElement().eIsProxy();
	}

	/**
	 * @generated
	 */
	protected String getDefaultFactoryHint() {
		return null;
	}
}
