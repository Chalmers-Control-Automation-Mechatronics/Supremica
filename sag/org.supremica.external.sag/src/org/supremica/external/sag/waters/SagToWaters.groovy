package org.supremica.external.sag.waters;

import org.supremica.external.sag.impl.*
import org.supremica.external.sag.*

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import net.sourceforge.waters.model.module.*
import net.sourceforge.waters.subject.module.*
import net.sourceforge.waters.gui.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import javax.swing.*

import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;
class SagToWaters {

	static void main(args) {
    	Project sagProject = loadSagProjectFromFile('C:/runtime-New_configuration/test/BallSystem.sag');
		println sagProject.name
		ModuleProxy watersModule = generateWatersModule(sagProject);
		saveWatersModuleToFile(watersModule, new File("C:/runtime-New_configuration/test/" + watersModule.getName() + WmodFileFilter.WMOD));
	}
	
	static {
		SagPackageImpl.init();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"sag", new XMIResourceFactoryImpl());
	}

	static Project loadSagProjectFromFile(String filename) {
		// Get the serialized sag model model
		final Resource resource = new ResourceSetImpl().getResource(URI
				.createFileURI(filename), true);
		return resource.getContents().get(0);
	}

	static ModuleProxy generateWatersModule(Project sagProject) {
		ModuleSubject watesModule = new ModuleSubject(sagProject.name, null);
	}
	
	static void saveWatersModuleToFile(ModuleProxy watersModule, File fileToSaveIn) {
		try	{
			final ModuleProxyFactory factory =
				ModuleSubjectFactory.getInstance();
			final OperatorTable optable = CompilerOperatorTable.getInstance();
			final ProxyMarshaller<ModuleProxy> marshaller = new JAXBModuleMarshaller(factory, optable);
			marshaller.marshal(watersModule, fileToSaveIn);
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(null, "Error saving module file:" + exception.getMessage());
			//logEntry("JAXBException - Failed to save  '" + wmodf + "'!");
		} catch (final SAXException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage());
			//logEntry("SAXException - Failed to save  '" + wmodf + "'!");
		} catch (final WatersMarshalException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage());
		//logEntry("WatersMarshalException - Failed to save  '" +
		//         wmodf + "'!");
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage());
			//logEntry("IOException - Failed to save  '" + wmodf + "'!");
		}
	}
}


