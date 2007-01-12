package org.supremica.external.sag.diagram.edit.parts;

import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;

import org.supremica.external.sag.Named;
import org.supremica.external.sag.SagPackage;
import org.supremica.external.sag.Zone;
import org.supremica.external.sag.diagram.edit.parts.UnboundedZoneEditPart.ZoneFigure;
import org.supremica.external.sag.diagram.edit.policies.BoundedZoneItemSemanticEditPolicy;

/**
 * @generated
 */
public class BoundedZoneEditPart extends ConnectionNodeEditPart {

	/**
	 * @generated
	 */
	public static final int VISUAL_ID = 4007;

	/**
	 * @generated
	 */
	public BoundedZoneEditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
				new BoundedZoneItemSemanticEditPolicy());
	}

	/**
	 * Creates figure for this edit part.
	 * 
	 * Body of this method does not depend on settings in generation model so
	 * you may safely remove <i>generated</i> tag and modify it.
	 * 
	 * @generated
	 */
	protected Connection createConnectionFigure() {

		return new ZoneFigure();
	}

	protected void handleNotificationEvent(Notification notification) {
		Object feature = notification.getFeature();
		if (SagPackage.eINSTANCE.getZone_IsOneway().equals(feature)) {
			((ZoneFigure) getFigure()).updateOnewaySymbol();
			refresh();
		}
		super.handleNotificationEvent(notification);
	}

	/**
	 * @generated NOT
	 */
	public class ZoneFigure extends PolylineConnectionEx {

		/**
		 * @generated NOT
		 */
		public ZoneFigure() {
			this.setFill(true);
			this.setFillXOR(false);
			this.setOutline(true);
			this.setOutlineXOR(false);
			this.setLineWidth(1);
			this.setLineStyle(Graphics.LINE_SOLID);
			updateOnewaySymbol();
		}

		public void updateOnewaySymbol() {
			if (!((Zone) getEdge().getElement()).isIsOneway()) {
				setTargetDecoration(null);
			} else {
				setTargetDecoration(createTargetDecoration());
			}
		}

		/**
		 * @generated
		 */
		private RotatableDecoration createTargetDecoration() {
			PolylineDecoration df = new PolylineDecoration();
			df.setFill(true);
			df.setFillXOR(false);
			df.setOutline(true);
			df.setOutlineXOR(false);
			df.setLineWidth(1);
			df.setLineStyle(Graphics.LINE_SOLID);
			PointList pl = new PointList();
			pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(-3));
			pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(3));
			pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(0));
			pl.addPoint(getMapMode().DPtoLP(-5), getMapMode().DPtoLP(-3));
			pl.addPoint(getMapMode().DPtoLP(-5), getMapMode().DPtoLP(3));
			pl.addPoint(getMapMode().DPtoLP(-2), getMapMode().DPtoLP(0));
			df.setTemplate(pl);
			df.setScale(getMapMode().DPtoLP(7), getMapMode().DPtoLP(3));
			return df;
		}

	}

}
