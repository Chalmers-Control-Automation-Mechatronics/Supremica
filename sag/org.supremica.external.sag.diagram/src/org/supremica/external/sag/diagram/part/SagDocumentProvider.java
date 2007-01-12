package org.supremica.external.sag.diagram.part;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import org.eclipse.emf.ecore.util.EContentAdapter;

import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.DiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.document.FileEditorInputProxy;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.document.StorageDocumentProvider;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorStatusCodes;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.util.DiagramIOUtil;

import org.eclipse.gmf.runtime.notation.Diagram;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;

import org.eclipse.ui.part.FileEditorInput;

/**
 * @generated
 */
public class SagDocumentProvider extends StorageDocumentProvider implements
		IDiagramDocumentProvider {
	/**
	 * @generated
	 */
	private final String myContentObjectURI;

	/**
	 * @generated
	 */
	public SagDocumentProvider() {
		this(null);
	}

	/**
	 * @generated
	 */
	public SagDocumentProvider(String rootObjectURI) {
		myContentObjectURI = rootObjectURI;
	}

	/**
	 * @generated
	 */
	protected ElementInfo createElementInfo(Object element)
			throws CoreException {
		if (false == element instanceof FileEditorInputProxy) {
			throw new CoreException(new Status(IStatus.ERROR,
					SagDiagramEditorPlugin.ID, 0, "Incorrect element used: "
							+ element + " instead of FileEditorInputProxy",
					null));
		}
		FileEditorInputProxy editorInput = (FileEditorInputProxy) element;
		IDiagramDocument document = (IDiagramDocument) createDocument(editorInput);

		ResourceSetInfo info = new ResourceSetInfo(document, editorInput);
		info.setModificationStamp(computeModificationStamp(info));
		info.fStatus = null;
		ResourceSetModificationListener modificationListener = new ResourceSetModificationListener(
				info);
		info.getResourceSet().eAdapters().add(modificationListener);
		return info;
	}

	/**
	 * @generated
	 */
	private long computeModificationStamp(ResourceSetInfo info) {
		int result = 0;
		for (Iterator it = info.getResourceSet().getResources().iterator(); it
				.hasNext();) {
			Resource nextResource = (Resource) it.next();
			IFile file = WorkspaceSynchronizer.getFile(nextResource);
			if (file != null) {
				if (file.getLocation() != null) {
					result += file.getLocation().toFile().lastModified();
				} else {
					result += file.getModificationStamp();
				}
			}
		}
		return result;
	}

	/**
	 * @generated
	 */
	protected IDocument createEmptyDocument() {
		return new DiagramDocument();
	}

	/**
	 * @generated
	 */
	protected boolean setDocumentContent(IDocument document,
			IEditorInput editorInput) throws CoreException {
		if (editorInput instanceof FileEditorInputProxy
				&& document instanceof IDiagramDocument) {
			FileEditorInputProxy editorInputProxy = (FileEditorInputProxy) editorInput;
			IDiagramDocument diagramDocument = (IDiagramDocument) document;
			diagramDocument.setEditingDomain(editorInputProxy
					.getEditingDomain());
		}
		return super.setDocumentContent(document, editorInput);
	}

	/**
	 * @generated
	 */
	protected void setDocumentContentFromStorage(IDocument document,
			IStorage storage) throws CoreException {
		IDiagramDocument diagramDocument = (IDiagramDocument) document;
		Diagram diagram = diagramDocument.getDiagram();

		TransactionalEditingDomain domain = diagramDocument.getEditingDomain();
		diagram = DiagramIOUtil.load(domain, storage, true,
				getProgressMonitor());
		if (myContentObjectURI != null
				&& diagram != null
				&& diagram.eResource() != null
				&& !diagram.eResource().getURIFragment(diagram).equals(
						myContentObjectURI)) {
			EObject anotherContentObject = diagram.eResource().getEObject(
					myContentObjectURI);
			document.setContent(anotherContentObject);
		} else {
			document.setContent(diagram);
		}
	}

	/**
	 * @generated
	 */
	public long getModificationStamp(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			return computeModificationStamp(info);
		}
		return super.getModificationStamp(element);
	}

	/**
	 * @generated
	 */
	public long getSynchronizationStamp(Object element) {
		if (element instanceof FileEditorInputProxy) {
			ResourceSetInfo info = getResourceSetInfo(element);
			if (info != null) {
				return info.getModificationStamp();
			}
		}
		return super.getSynchronizationStamp(element);
	}

	/**
	 * @generated
	 */
	public boolean isDeleted(Object element) {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) element;
			IPath path = input.getFile().getLocation();
			if (path == null) {
				return true;
			}
			return !path.toFile().exists();
		}
		return super.isDeleted(element);
	}

	/**
	 * @generated
	 */
	public ResourceSetInfo getResourceSetInfo(Object editorInput) {
		return (ResourceSetInfo) super.getElementInfo(editorInput);
	}

	/**
	 * @generated
	 */
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof ResourceSetInfo) {
			ResourceSetInfo resourceSetInfo = (ResourceSetInfo) info;
			resourceSetInfo.dispose();
		}
		super.disposeElementInfo(element, info);
	}

	/**
	 * @generated
	 */
	protected void doValidateState(Object element, Object computationContext)
			throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection files2Validate = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null && file.isReadOnly()) {
					files2Validate.add(file);
				}
			}
			ResourcesPlugin.getWorkspace().validateEdit(
					(IFile[]) files2Validate.toArray(new IFile[files2Validate
							.size()]), computationContext);
		}

		super.doValidateState(element, computationContext);
	}

	/**
	 * @generated
	 */
	public boolean isModifiable(Object element) {
		if (!isStateValidated(element)) {
			if (element instanceof FileEditorInputProxy) {
				return true;
			}
		}
		return super.isModifiable(element);
	}

	/**
	 * @generated
	 */
	protected void updateCache(IStorageEditorInput input) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(input);
		if (info != null) {
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null && file.isReadOnly()) {
					info.fIsReadOnly = true;
					info.fIsModifiable = false;
					return;
				}
			}
			info.fIsReadOnly = false;
			info.fIsModifiable = true;
			return;
		}
		super.updateCache(input);
	}

	/**
	 * @generated
	 */
	public boolean isSynchronized(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			return info.isSynchronized();
		}
		return super.isSynchronized(element);
	}

	/**
	 * @generated
	 */
	protected ISchedulingRule getResetRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(ResourcesPlugin.getWorkspace().getRuleFactory()
							.modifyRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules
					.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected ISchedulingRule getSaveRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(computeSchedulingRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules
					.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected ISchedulingRule getSynchronizeRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(ResourcesPlugin.getWorkspace().getRuleFactory()
							.refreshRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules
					.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected ISchedulingRule getValidateStateRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection files = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it
					.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					files.add(file);
				}
			}
			return ResourcesPlugin.getWorkspace().getRuleFactory()
					.validateEditRule(
							(IFile[]) files.toArray(new IFile[files.size()]));
		}
		return null;
	}

	/**
	 * @generated
	 */
	private ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		if (toCreateOrModify.exists())
			return ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(
					toCreateOrModify);

		IResource parent = toCreateOrModify;
		do {
			/*
			 * XXX This is a workaround for
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=67601
			 * IResourceRuleFactory.createRule should iterate the hierarchy
			 * itself.
			 */
			toCreateOrModify = parent;
			parent = toCreateOrModify.getParent();
		} while (parent != null && !parent.exists());

		return ResourcesPlugin.getWorkspace().getRuleFactory().createRule(
				toCreateOrModify);
	}

	/**
	 * @generated
	 */
	protected void doSynchronize(Object element, IProgressMonitor monitor)
			throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null && element instanceof FileEditorInputProxy) {
			handleResourcesChanged(info, info.getResourceSet().getResources(),
					monitor);
			return;
		}
		super.doSynchronize(element, monitor);
	}

	/**
	 * @generated
	 */
	protected void handleResourcesMoved(Map movedPathToResource) {
		for (Iterator it = movedPathToResource.entrySet().iterator(); it
				.hasNext();) {
			Entry nextEntry = (Entry) it.next();
			IPath newPath = (IPath) nextEntry.getKey();
			Resource resource = (Resource) nextEntry.getValue();
			resource.setURI(URI.createURI(newPath.toString()));
		}
	}

	/**
	 * @generated
	 */
	protected void markWholeResourceSetAsDirty(ResourceSet resourceSet) {
		for (Iterator it = resourceSet.getResources().iterator(); it.hasNext();) {
			Resource nextResource = (Resource) it.next();
			nextResource.setModified(true);
		}
	}

	/**
	 * @generated
	 */
	protected void handleResourcesChanged(ResourceSetInfo info,
			Collection changedResources, IProgressMonitor monitor) {
		info.stopResourceListening();
		for (Iterator it = changedResources.iterator(); it.hasNext();) {
			Resource nextResource = (Resource) it.next();
			IFile file = WorkspaceSynchronizer.getFile(nextResource);
			if (file != null) {
				try {
					file.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					handleCoreException(e,
							"FileDocumentProvider.handleElementContentChanged");
				}
			}
			nextResource.unload();
		}
		info.startResourceListening();

		fireElementContentAboutToBeReplaced(info.getEditorInput());
		removeUnchangedElementListeners(info.getEditorInput(), info);
		info.fStatus = null;
		try {
			setDocumentContent(info.fDocument, info.getEditorInput());
		} catch (CoreException e) {
			info.fStatus = e.getStatus();
		}
		if (!info.fCanBeSaved) {
			info.setModificationStamp(computeModificationStamp(info));
		}
		addUnchangedElementListeners(info.getEditorInput(), info);
		fireElementContentReplaced(info.getEditorInput());
	}

	/**
	 * @generated
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			if (!overwrite && !info.isSynchronized()) {
				throw new CoreException(new Status(IStatus.ERROR,
						SagDiagramEditorPlugin.ID,
						IResourceStatus.OUT_OF_SYNC_LOCAL,
						"The file has been changed on the file system", null));
			}
			info.stopResourceListening();
			fireElementStateChanging(element);
			try {
				monitor.beginTask("Saving diagram editor", info
						.getResourceSet().getResources().size());
				for (Iterator it = info.getResourceSet().getResources()
						.iterator(); it.hasNext();) {
					Resource nextResource = (Resource) it.next();
					monitor.setTaskName("Saving " + nextResource.getURI());
					if (nextResource.isLoaded()
							&& (!nextResource.isTrackingModification() || nextResource
									.isModified())) {
						nextResource.save(Collections.EMPTY_MAP);
					}
					monitor.worked(1);
				}
				monitor.done();
			} catch (IOException e) {
				fireElementStateChangeFailed(element);
				throw new CoreException(new Status(IStatus.ERROR,
						SagDiagramEditorPlugin.ID,
						EditorStatusCodes.RESOURCE_FAILURE, e
								.getLocalizedMessage(), null));
			} catch (RuntimeException x) {
				fireElementStateChangeFailed(element);
				throw x;
			} finally {
				info.startResourceListening();
			}

			if (info != null) {
				info.setModificationStamp(computeModificationStamp(info));
				info.setSynchronized();
			}
		}
		super.doSaveDocument(monitor, element, document, overwrite);
	}

	/**
	 * @generated
	 */
	protected void handleElementMoved(FileEditorInputProxy input, IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile newFile = workspace.getRoot().getFile(path);
		fireElementMoved(input, newFile == null ? null : new FileEditorInput(
				newFile));
	}

	/**
	 * @generated
	 */
	protected void handleElementDeleted(FileEditorInputProxy input) {
		fireElementDeleted(input);
	}

	/**
	 * @generated
	 */
	public IEditorInput createInputWithEditingDomain(IEditorInput editorInput,
			TransactionalEditingDomain domain) {
		if (editorInput instanceof IFileEditorInput) {
			return new FileEditorInputProxy((IFileEditorInput) editorInput,
					domain);
		}
		assert false;
		return null;
	}

	/**
	 * @generated
	 */
	public IDiagramDocument getDiagramDocument(Object element) {
		IDocument doc = getDocument(element);
		if (doc instanceof IDiagramDocument) {
			return (IDiagramDocument) doc;
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected class ResourceSetInfo extends StorageInfo {

		/**
		 * @generated
		 */
		private long myModificationStamp = IResource.NULL_STAMP;

		/**
		 * @generated
		 */
		private ResourceSetSynchronizer mySynchronizer;

		/**
		 * @generated
		 */
		private ResourceSet myResourceSet;

		/**
		 * @generated
		 */
		private Collection myUnSynchronizedResources = new ArrayList();

		/**
		 * @generated
		 */
		private FileEditorInputProxy myEditorInput;

		/**
		 * @generated
		 */
		public ResourceSetInfo(IDiagramDocument document,
				FileEditorInputProxy editorInput) {
			super(document);
			myResourceSet = document.getEditingDomain().getResourceSet();
			myEditorInput = editorInput;
			mySynchronizer = new ResourceSetSynchronizer(this);
			startResourceListening();
		}

		/**
		 * @generated
		 */
		public long getModificationStamp() {
			return myModificationStamp;
		}

		/**
		 * @generated
		 */
		public void setModificationStamp(long modificationStamp) {
			myModificationStamp = modificationStamp;
		}

		/**
		 * @generated
		 */
		public ResourceSetSynchronizer getSynchronizer() {
			return mySynchronizer;
		}

		/**
		 * @generated
		 */
		public ResourceSet getResourceSet() {
			return myResourceSet;
		}

		/**
		 * @generated
		 */
		public FileEditorInputProxy getEditorInput() {
			return myEditorInput;
		}

		/**
		 * @generated
		 */
		public void dispose() {
			stopResourceListening();
		}

		/**
		 * @generated
		 */
		public boolean isSynchronized() {
			return myUnSynchronizedResources.size() == 0;
		}

		/**
		 * @generated
		 */
		public void setSynchronized() {
			myUnSynchronizedResources.clear();
		}

		/**
		 * @generated
		 */
		public void setUnSynchronized(Resource resource) {
			myUnSynchronizedResources.add(resource);
		}

		/**
		 * @generated
		 */
		public void setSynchronized(Resource resource) {
			myUnSynchronizedResources.remove(resource);
		}

		/**
		 * @generated
		 */
		public final void stopResourceListening() {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(
					mySynchronizer);
		}

		/**
		 * @generated
		 */
		public final void startResourceListening() {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(
					mySynchronizer, IResourceChangeEvent.POST_CHANGE);
		}

	}

	/**
	 * @generated
	 */
	protected class ResourceSetSynchronizer implements IResourceChangeListener {

		/**
		 * @generated
		 */
		private ResourceSetInfo myInfo;

		/**
		 * @generated
		 */
		protected ResourceSetSynchronizer(ResourceSetInfo info) {
			myInfo = info;
		}

		/**
		 * @generated
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			final ResourceDeltaVisitor deltaVisitor = new ResourceDeltaVisitor();
			try {
				event.getDelta().accept(deltaVisitor);
			} catch (CoreException e) {
				handleCoreException(e, "FileDocumentProvider.resourceChanged");
			}
			synchronized (myInfo) {
				if (!myInfo.isSynchronized()) {
					return;
				}
			}

			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					if (deltaVisitor.getDeletedResources().size() > 0) {
						// Just closing editor
						handleElementDeleted(myInfo.getEditorInput());
						return;
					}

					Entry diagramEntry = getDiagramResourceEntry(deltaVisitor
							.getMovedResourcesMap());
					if (diagramEntry != null) {
						deltaVisitor.getMovedResourcesMap().remove(
								diagramEntry.getKey());
						// Setting new editor input since diagram file was
						// renamed Could be processed together with the rest of
						// moved resources if FileEditorInputProxy will wupport
						// IFileEditorInput substitution
						handleElementMoved(myInfo.getEditorInput(),
								(IPath) diagramEntry.getKey());
					}
					if (deltaVisitor.getMovedResourcesMap().size() > 0) {
						handleResourcesMoved(deltaVisitor
								.getMovedResourcesMap());
					}
					if (deltaVisitor.getChangedResources().size() > 0
							|| deltaVisitor.getMovedResourcesMap().size() > 0) {
						// reloading changed resources + changing URIs for moved
						// resources
						handleResourcesChanged(myInfo, deltaVisitor
								.getChangedResources(), null);
					}
					if (deltaVisitor.getMovedResourcesMap().size() > 0) {
						// Marking whole ResourceSet as changed to preserve
						// changes in resource URIs made by
						// handleResourcesMoved() call
						markWholeResourceSetAsDirty(myInfo.getResourceSet());
					}
				}
			});
		}

		/**
		 * @generated
		 */
		private Entry getDiagramResourceEntry(Map movedResources) {
			for (Iterator it = movedResources.entrySet().iterator(); it
					.hasNext();) {
				Entry nextEntry = (Entry) it.next();
				Resource nextResource = (Resource) nextEntry.getValue();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null
						&& file.equals(myInfo.getEditorInput().getFile())) {
					return nextEntry;
				}
			}
			return null;
		}

		/**
		 * @generated
		 */
		private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

			/**
			 * @generated
			 */
			private Collection myChangedResources = new ArrayList();

			/**
			 * @generated
			 */
			private Map myMovedResources = new HashMap();

			/**
			 * @generated
			 */
			private Collection myDeletedResources = new ArrayList();

			/**
			 * Can be called from any thread
			 * @generated
			 */
			public boolean visit(IResourceDelta delta) {
				if (delta.getFlags() != IResourceDelta.MARKERS
						&& delta.getResource().getType() == IResource.FILE) {
					if ((delta.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
						Resource resource = myInfo.getResourceSet()
								.getResource(
										URI.createURI(delta.getFullPath()
												.toString()), false);
						if (resource != null && resource.isLoaded()) {
							synchronized (myInfo) {
								if (myInfo.fCanBeSaved) {
									myInfo.setUnSynchronized(resource);
									return false;
								}
							}
							if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
								// element could be either moved/deleted or
								// changed.
								if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
									IPath destination = delta.getMovedToPath();
									myMovedResources.put(destination, resource);
								} else {
									myDeletedResources.add(resource);
								}
							} else {
								myChangedResources.add(resource);
							}
						}
					}
				}

				return true;
			}

			/**
			 * @generated
			 */
			public Collection getChangedResources() {
				return myChangedResources;
			}

			/**
			 * @generated
			 */
			public Collection getDeletedResources() {
				return myDeletedResources;
			}

			/**
			 * @generated
			 */
			public Map getMovedResourcesMap() {
				return myMovedResources;
			}
		}

	}

	/**
	 * @generated
	 */
	private class ResourceSetModificationListener extends EContentAdapter {

		/**
		 * @generated
		 */
		private NotificationFilter myModifiedFilter;

		/**
		 * @generated
		 */
		private ResourceSetInfo myInfo;

		/**
		 * @generated
		 */
		public ResourceSetModificationListener(ResourceSetInfo info) {
			myInfo = info;
			myModifiedFilter = NotificationFilter.createEventTypeFilter(
					Notification.SET).or(
					NotificationFilter
							.createEventTypeFilter(Notification.UNSET)).and(
					NotificationFilter.createFeatureFilter(Resource.class,
							Resource.RESOURCE__IS_MODIFIED));
		}

		/**
		 * @generated
		 */
		public void notifyChanged(Notification notification) {
			if (notification.getNotifier() instanceof ResourceSet) {
				super.notifyChanged(notification);
			}
			if (myModifiedFilter.matches(notification)) {
				if (notification.getNotifier() instanceof Resource) {
					Resource resource = (Resource) notification.getNotifier();
					if (resource.isLoaded()) {
						boolean modified = false;
						for (Iterator it = myInfo.getResourceSet()
								.getResources().iterator(); it.hasNext()
								&& !modified;) {
							Resource nextResource = (Resource) it.next();
							if (nextResource.isLoaded()) {
								modified = nextResource.isModified();
							}
						}
						boolean dirtyStateChanged = false;
						synchronized (myInfo) {
							if (modified != myInfo.fCanBeSaved) {
								myInfo.fCanBeSaved = modified;
								dirtyStateChanged = true;
							}
							if (!resource.isModified()) {
								myInfo.setSynchronized(resource);
							}
						}
						if (dirtyStateChanged) {
							fireElementDirtyStateChanged(myInfo
									.getEditorInput(), modified);
							if (!modified) {
								myInfo
										.setModificationStamp(computeModificationStamp(myInfo));
							}
						}
					}

				}
			}
		}
	}

}
