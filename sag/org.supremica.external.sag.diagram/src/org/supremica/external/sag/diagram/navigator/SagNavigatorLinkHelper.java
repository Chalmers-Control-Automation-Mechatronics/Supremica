package org.supremica.external.sag.diagram.navigator;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.gef.EditPart;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditorInput;

import org.eclipse.gmf.runtime.notation.View;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.ui.navigator.ILinkHelper;

/**
 * @generated
 */
public class SagNavigatorLinkHelper implements ILinkHelper {

	/**
	 * @generated
	 */
	public IStructuredSelection findSelection(IEditorInput anInput) {
		return StructuredSelection.EMPTY;
	}

	/**
	 * @generated
	 */
	public void activateEditor(IWorkbenchPage aPage,
			IStructuredSelection aSelection) {
		if (aSelection == null || aSelection.isEmpty()) {
			return;
		}
		if (false == aSelection.getFirstElement() instanceof SagAbstractNavigatorItem) {
			return;
		}

		SagAbstractNavigatorItem navigatorItem = (SagAbstractNavigatorItem) aSelection
				.getFirstElement();
		View navigatorView = null;
		if (navigatorItem instanceof SagNavigatorItem) {
			navigatorView = ((SagNavigatorItem) navigatorItem).getView();
		} else if (navigatorItem instanceof SagNavigatorGroup) {
			SagNavigatorGroup group = (SagNavigatorGroup) navigatorItem;
			if (group.getParent() instanceof SagNavigatorItem) {
				navigatorView = ((SagNavigatorItem) group.getParent())
						.getView();
			}
		}
		if (navigatorView == null) {
			return;
		}
		DiagramEditorInput editorInput = new DiagramEditorInput(navigatorView
				.getDiagram());
		IEditorPart editor = aPage.findEditor(editorInput);
		if (editor == null) {
			return;
		}
		aPage.bringToTop(editor);
		if (editor instanceof DiagramEditor) {
			DiagramEditor diagramEditor = (DiagramEditor) editor;
			Resource diagramResource = diagramEditor.getDiagram().eResource();

			EObject selectedView = diagramResource.getEObject(navigatorView
					.eResource().getURIFragment(navigatorView));
			if (selectedView == null) {
				return;
			}
			EditPart selectedEditPart = (EditPart) diagramEditor
					.getDiagramGraphicalViewer().getEditPartRegistry().get(
							selectedView);
			if (selectedEditPart != null) {
				diagramEditor.getDiagramGraphicalViewer().select(
						selectedEditPart);
			}
		}
	}

}
