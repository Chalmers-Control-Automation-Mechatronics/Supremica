package org.supremica.external.sag.diagram.edit.parts;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.supremica.external.sag.diagram.part.SagVisualIDRegistry;

/**
 * @generated
 */
public class SagEditPartFactory implements EditPartFactory {

	/**
	 * @generated
	 */
	public static final String EXTERNAL_NODE_LABELS_LAYER = "External Node Labels"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof View) {
			View view = (View) model;
			int viewVisualID = SagVisualIDRegistry.getVisualID(view);
			switch (viewVisualID) {
			case GraphEditPart.VISUAL_ID:
				return new GraphEditPart(view);
			case GraphNameEditPart.VISUAL_ID:
				return new GraphNameEditPart(view);
			case NodeEditPart.VISUAL_ID:
				return new NodeEditPart(view);
			case NodeSensorEditPart.VISUAL_ID:
				return new NodeSensorEditPart(view);
			case Node2EditPart.VISUAL_ID:
				return new Node2EditPart(view);
			case GraphGraphCompartmentEditPart.VISUAL_ID:
				return new GraphGraphCompartmentEditPart(view);
			case ProjectEditPart.VISUAL_ID:
				return new ProjectEditPart(view);
			case BoundedZoneEditPart.VISUAL_ID:
				return new BoundedZoneEditPart(view);
			case BoundedZoneCapacityEditPart.VISUAL_ID:
				return new BoundedZoneCapacityEditPart(view);
			case BoundedZone2EditPart.VISUAL_ID:
				return new BoundedZone2EditPart(view);
			case BoundedZoneCapacity2EditPart.VISUAL_ID:
				return new BoundedZoneCapacity2EditPart(view);
			case UnboundedZoneEditPart.VISUAL_ID:
				return new UnboundedZoneEditPart(view);
			case UnboundedZone2EditPart.VISUAL_ID:
				return new UnboundedZone2EditPart(view);
			case UnboundedZone3EditPart.VISUAL_ID:
				return new UnboundedZone3EditPart(view);
			case UnboundedZone4EditPart.VISUAL_ID:
				return new UnboundedZone4EditPart(view);
			}
		}
		return createUnrecognizedEditPart(context, model);
	}

	/**
	 * @generated
	 */
	private EditPart createUnrecognizedEditPart(EditPart context, Object model) {
		// Handle creation of unrecognized child node EditParts here
		return null;
	}

	/**
	 * @generated
	 */
	public static CellEditorLocator getTextCellEditorLocator(
			ITextAwareEditPart source) {
		if (source.getFigure() instanceof WrapLabel)
			return new TextCellEditorLocator((WrapLabel) source.getFigure());
		else {
			IFigure figure = source.getFigure();
			return new LabelCellEditorLocator((Label) figure);
		}
	}

	/**
	 * @generated
	 */
	static private class TextCellEditorLocator implements CellEditorLocator {

		/**
		 * @generated
		 */
		private WrapLabel wrapLabel;

		/**
		 * @generated
		 */
		public TextCellEditorLocator(WrapLabel wrapLabel) {
			super();
			this.wrapLabel = wrapLabel;
		}

		/**
		 * @generated
		 */
		public WrapLabel getWrapLabel() {
			return wrapLabel;
		}

		/**
		 * @generated
		 */
		public void relocate(CellEditor celleditor) {
			Text text = (Text) celleditor.getControl();
			Rectangle rect = getWrapLabel().getTextBounds().getCopy();
			getWrapLabel().translateToAbsolute(rect);

			if (getWrapLabel().isTextWrapped()
					&& getWrapLabel().getText().length() > 0)
				rect.setSize(new Dimension(text.computeSize(rect.width,
						SWT.DEFAULT)));
			else {
				int avr = FigureUtilities.getFontMetrics(text.getFont())
						.getAverageCharWidth();
				rect.setSize(new Dimension(text.computeSize(SWT.DEFAULT,
						SWT.DEFAULT)).expand(avr * 2, 0));
			}

			if (!rect.equals(new Rectangle(text.getBounds())))
				text.setBounds(rect.x, rect.y, rect.width, rect.height);
		}

	}

	/**
	 * @generated
	 */
	private static class LabelCellEditorLocator implements CellEditorLocator {

		/**
		 * @generated
		 */
		private Label label;

		/**
		 * @generated
		 */
		public LabelCellEditorLocator(Label label) {
			this.label = label;
		}

		/**
		 * @generated
		 */
		public Label getLabel() {
			return label;
		}

		/**
		 * @generated
		 */
		public void relocate(CellEditor celleditor) {
			Text text = (Text) celleditor.getControl();
			Rectangle rect = getLabel().getTextBounds().getCopy();
			getLabel().translateToAbsolute(rect);

			int avr = FigureUtilities.getFontMetrics(text.getFont())
					.getAverageCharWidth();
			rect.setSize(new Dimension(text.computeSize(SWT.DEFAULT,
					SWT.DEFAULT)).expand(avr * 2, 0));

			if (!rect.equals(new Rectangle(text.getBounds())))
				text.setBounds(rect.x, rect.y, rect.width, rect.height);
		}
	}
}
