package org.supremica.external.sag.diagram.part;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @generated
 */
public class SagCreationWizardPage extends WizardNewFileCreationPage {

	/**
	 * @generated
	 */
	private static final String DOMAIN_EXT = ".sag"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	private static final String DIAGRAM_EXT = ".sag_diagram"; //$NON-NLS-1$

	/**
	 * @generated
	 */
	public SagCreationWizardPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
	}

	/**
	 * @generated
	 */
	protected String getDefaultFileName() {
		return "default"; //$NON-NLS-1$
	}

	/**
	 * @generated
	 */
	public String getFileName() {
		String fileName = super.getFileName();
		if (fileName != null && !fileName.endsWith(DIAGRAM_EXT)) {
			fileName += DIAGRAM_EXT;
		}
		return fileName;
	}

	/**
	 * @generated
	 */
	private String getUniqueFileName(IPath containerPath, String fileName) {
		String newFileName = fileName;
		IPath diagramFilePath = containerPath.append(newFileName + DIAGRAM_EXT);
		IPath modelFilePath = containerPath.append(newFileName + DOMAIN_EXT);
		int i = 1;
		while (exists(diagramFilePath) || exists(modelFilePath)) {
			i++;
			newFileName = fileName + i;
			diagramFilePath = containerPath.append(newFileName + DIAGRAM_EXT);
			modelFilePath = containerPath.append(newFileName + DOMAIN_EXT);
		}
		return newFileName;
	}

	/**
	 * @generated
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		IPath path = getContainerFullPath();
		if (path != null) {
			String fileName = getUniqueFileName(path, getDefaultFileName());
			setFileName(fileName);
		} else {
			setFileName(getDefaultFileName());
		}
		setPageComplete(validatePage());
	}

	/**
	 * @generated
	 */
	protected boolean validatePage() {
		if (super.validatePage()) {
			String fileName = getFileName();
			if (fileName == null) {
				return false;
			}
			fileName = fileName.substring(0, fileName.length()
					- DIAGRAM_EXT.length())
					+ DOMAIN_EXT;
			IPath path = getContainerFullPath().append(fileName);
			if (exists(path)) {
				setErrorMessage("Model file already exists: "
						+ path.lastSegment());
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @generated
	 */
	public static boolean exists(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().exists(path);
	}
}
