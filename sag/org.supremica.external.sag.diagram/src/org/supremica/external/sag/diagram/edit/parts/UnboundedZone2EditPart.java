package org.supremica.external.sag.diagram.edit.parts;

import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.draw2d.Connection;

import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;

import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;

import org.supremica.external.sag.diagram.edit.policies.UnboundedZone2ItemSemanticEditPolicy;

/**
 * @generated
 */
public class UnboundedZone2EditPart extends ConnectionNodeEditPart {

	/**
	 * @generated
	 */
	public static final int VISUAL_ID = 3004;

	/**
	 * @generated
	 */
	public UnboundedZone2EditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
				new UnboundedZone2ItemSemanticEditPolicy());

	}

	/**
	 * Creates figure for this edit part.
	 * 
	 * Body of this method does not depend on settings in generation model
	 * so you may safely remove <i>generated</i> tag and modify it.
	 * 
	 * @generated
	 */
	protected Connection createConnectionFigure() {
		return new PolylineConnectionEx();
	}

}
