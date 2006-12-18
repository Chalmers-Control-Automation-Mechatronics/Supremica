package org.supremica.external.sag.diagram.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableLabelEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.figures.LabelLocator;
import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @generated
 */
public class SagExtNodeLabelEditPart extends LabelEditPart {

	/**
	 * @generated
	 */
	public SagExtNodeLabelEditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
				new NonResizableLabelEditPolicy() {

					protected void replaceHandleDragEditPartsTracker(
							Handle handle) {
						if (handle instanceof AbstractHandle) {
							((AbstractHandle) handle)
									.setDragTracker(new DragEditPartsTrackerEx(
											getHost()) {

										protected boolean isMove() {
											return true;
										}
									});
						}
					}
				});
	}

	/**
	 * @generated
	 */
	public void refreshBounds() {
		IFigure refFigure = ((GraphicalEditPart) getParent()).getFigure();
		int dx = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE
				.getLocation_X())).intValue();
		int dy = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE
				.getLocation_Y())).intValue();
		Point offset = new Point(dx, dy);
		getFigure().getParent().setConstraint(getFigure(),
				new LabelLocator(refFigure, offset, getKeyPoint()) {

					public void relocate(IFigure target) {
						Point location = getReferencePoint().getTranslated(
								getOffset());
						location.translate(-target.getBounds().width / 2, 0);
						target.setLocation(location);
						target.setSize(new Dimension(
								target.getPreferredSize().width, target
										.getPreferredSize().height));
					}

					protected Point getReferencePoint() {
						return getLabelLocation(parent);
					}
				});
	}

	/**
	 * @generated
	 */
	public Point getReferencePoint() {
		return getLabelLocation(((GraphicalEditPart) getParent()).getFigure());
	}

	/**
	 * @generated
	 */
	protected Point getLabelLocation(IFigure parent) {
		return parent.getBounds().getBottom();
	}
}
