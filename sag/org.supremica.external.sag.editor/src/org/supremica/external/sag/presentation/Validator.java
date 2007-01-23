package org.supremica.external.sag.presentation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.part.ISetSelectionTarget;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.emf.edit.ui.action.ValidateAction.EclipseResourcesUtil;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;

public class Validator {

	public static Validator INSTANCE = new Validator();

	private Validator() {
	}

	public static class EclipseResourcesUtil extends EditUIMarkerHelper {
		public IRunnableWithProgress getWorkspaceModifyOperation(
				IRunnableWithProgress runnableWithProgress) {
			return new WorkspaceModifyDelegatingOperation(runnableWithProgress);
		}

		protected String getMarkerID() {
			return EValidator.MARKER;
		}

		public void createMarkers(Resource resource, Diagnostic diagnostic) {
			try {
				createMarkers(getFile(resource), diagnostic, null);
			} catch (CoreException e) {
				EMFEditUIPlugin.INSTANCE.log(e);
			}
		}

		protected String composeMessage(Diagnostic diagnostic,
				Diagnostic parentDiagnostic) {
			String message = diagnostic.getMessage();
			if (parentDiagnostic != null) {
				String parentMessage = parentDiagnostic.getMessage();
				if (parentMessage != null) {
					message = message != null ? parentMessage + ". " + message
							: parentMessage;
				}
			}
			return message;
		}

		protected void adjustMarker(IMarker marker, Diagnostic diagnostic,
				Diagnostic parentDiagnostic) throws CoreException {
			List data = diagnostic.getData();
			if (!data.isEmpty()) {
				Object target = data.get(0);
				if (target instanceof EObject) {
					marker.setAttribute(EValidator.URI_ATTRIBUTE, EcoreUtil
							.getURI((EObject) target).toString());
				}
			}

			super.adjustMarker(marker, diagnostic, parentDiagnostic);
		}
	}

	protected EclipseResourcesUtil eclipseResourcesUtil = Platform
			.getBundle("org.eclipse.core.resources") != null ? new EclipseResourcesUtil()
			: null;

	public Diagnostic validate(EObject objectToValidate, EditingDomain domain) {
		final Shell shell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		final AdapterFactory adapterFactory = domain instanceof AdapterFactoryEditingDomain ? ((AdapterFactoryEditingDomain) domain)
				.getAdapterFactory()
				: null;

		Diagnostician diagnostician = new Diagnostician() {
			public String getObjectLabel(EObject eObject) {
				if (adapterFactory != null && !eObject.eIsProxy()) {
					IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory
							.adapt(eObject, IItemLabelProvider.class);
					if (itemLabelProvider != null) {
						return itemLabelProvider.getText(eObject);
					}
				}
				return super.getObjectLabel(eObject);
			}
		};

		Diagnostic diagnostic = diagnostician.validate(objectToValidate);

		handleDiagnostic(diagnostic, domain);

		return diagnostic;
	}

	protected void handleDiagnostic(Diagnostic diagnostic, EditingDomain domain) {
		int severity = diagnostic.getSeverity();
		String title = null;
		String message = null;

		if (severity == Diagnostic.ERROR || severity == Diagnostic.WARNING) {
			title = EMFEditUIPlugin.INSTANCE
					.getString("_UI_ValidationProblems_title");
			message = EMFEditUIPlugin.INSTANCE
					.getString("_UI_ValidationProblems_message");
		} else {
			title = EMFEditUIPlugin.INSTANCE
					.getString("_UI_ValidationResults_title");
			message = EMFEditUIPlugin.INSTANCE
					.getString(severity == Diagnostic.OK ? "_UI_ValidationOK_message"
							: "_UI_ValidationResults_message");
		}
		int result = ErrorDialog.openError(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message,
				BasicDiagnostic.toIStatus(diagnostic));

		// No error dialog is displayed if the status severity is OK; pop up a
		// dialog so the user knows something happened.
		//
/*		if (diagnostic.getSeverity() == Diagnostic.OK) {
			MessageDialog.openInformation(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), title, message);
			result = Window.CANCEL;
		}
*/

		if (eclipseResourcesUtil != null) {
			Resource resource = (Resource) domain.getResourceSet()
					.getResources().get(0);
			if (resource != null) {
				eclipseResourcesUtil.deleteMarkers(resource);
			}

			if (result == Window.OK) {
				if (!diagnostic.getChildren().isEmpty()) {
					List data = ((Diagnostic) diagnostic.getChildren().get(0))
							.getData();
					if (!data.isEmpty() && data.get(0) instanceof EObject) {
						Object part = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.getActivePart();
						if (part instanceof ISetSelectionTarget) {
							((ISetSelectionTarget) part)
									.selectReveal(new StructuredSelection(data
											.get(0)));
						} else if (part instanceof IViewerProvider) {
							Viewer viewer = ((IViewerProvider) part)
									.getViewer();
							if (viewer != null) {
								viewer.setSelection(new StructuredSelection(
										data.get(0)), true);
							}
						}
					}
				}

				if (resource != null) {
					for (Iterator i = diagnostic.getChildren().iterator(); i
							.hasNext();) {
						Diagnostic childDiagnostic = (Diagnostic) i.next();
						eclipseResourcesUtil.createMarkers(resource,
								childDiagnostic);
					}
				}
			}
		}
	}
}
