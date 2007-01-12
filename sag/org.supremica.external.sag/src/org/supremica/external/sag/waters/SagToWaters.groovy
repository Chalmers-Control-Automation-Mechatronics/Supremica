package org.supremica.external.sag.waters

import org.supremica.external.sag.impl.*
import org.supremica.external.sag.*

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl

import net.sourceforge.waters.model.module.*
import net.sourceforge.waters.subject.module.*
import net.sourceforge.waters.gui.*
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.model.expr.OperatorTable
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller
import net.sourceforge.waters.model.marshaller.ProxyMarshaller
import net.sourceforge.waters.model.marshaller.WatersMarshalException
import net.sourceforge.waters.model.module.ModuleProxy
import net.sourceforge.waters.model.module.ModuleProxyFactory
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.xsd.base.ComponentKind
import net.sourceforge.waters.xsd.base.EventKind
import net.sourceforge.waters.subject.module.EdgeSubject

import javax.swing.*

import javax.xml.bind.JAXBException
import org.xml.sax.SAXException

// NOTE THAT all generics used here are only for documentation.
// Groovy does not enforce anything based on generics, neither dynamically or statically

class SagToWaters {

	static void main(args) {
    	Project sagProject = loadSagProjectFromFile('C:/runtime-New_configuration/test/BallSystem.sag')
		ModuleProxy watersModule = generateWatersModule(sagProject)
		File watersFile = new File("C:/runtime-New_configuration/test/${watersModule.getName()}.${WmodFileFilter.WMOD}")
		saveWatersModuleToFile(watersModule, watersFile)
		watersFile.eachLine{println it}
	}
	
	static {
		SagPackageImpl.init()
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"sag", new XMIResourceFactoryImpl())
	}

	static Project loadSagProjectFromFile(String filename) {
		// Get the serialized sag model
		final Resource resource = new ResourceSetImpl().getResource(URI.createFileURI(filename), true)
		return resource.getContents().get(0)
	}

	static ModuleProxy generateWatersModule(Project sagProject) {
		ModuleSubject watersModule = new ModuleSubject(sagProject.name, null)
		
		Closure newComponent = {componentName, graph ->
			def component = new SimpleComponentSubject(new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance()).parseIdentifier(componentName),
                ComponentKind.PLANT, graph)
			watersModule.componentListModifiable << component
			component
		}
		//Create one component for every sensor
		final Map<Sensor, SimpleComponentSubject> sensorComponents = sagProject.sensor.inject([:]){components, sensor -> //and create a waters component for each
			GraphSubject watersGraph = new GraphSubject()
			["false","true"].each{watersGraph.nodesModifiable << new SimpleNodeSubject(it)}
			watersGraph.nodesModifiable.find{it.name=="false"}.initial = true
			components[sensor] = newComponent(sensor.name, watersGraph)
			components
		}
		
		//Create one component for every bounded zone
		int i = 0
		final Map<BoundedZone, SimpleComponentSubject> boundedZoneComponents = sagProject.graph.collect{graph -> i = 0; graph.zone.findAll{zone -> zone instanceof BoundedZone}}.flatten().
			                                         inject([:]){components, zone ->
			GraphSubject watersGraph = new GraphSubject()
			(0..zone.capacity).each{watersGraph.nodesModifiable << new SimpleNodeSubject(it.toString())}
			watersGraph.nodesModifiable.find{it.name=="0"}.initial = true
			components[zone] = newComponent("${zone.graph.name}_zone${i}", watersGraph)
			++i
			components
		}
		
		//Create one event for every sensor entry and exit
		//Get a map with sensor as key and a list of zones from which objects can enter the sensor as value
		final Map<Sensor, List<Zone>> sensorEntrances = sagProject.sensor.inject([:]) {entrances, sensor ->
			entrances[sensor] = sensor.node.incoming + sensor.node.outgoing.findAll{zone -> !zone.isOneway}
			entrances
		}
		//Get a map with sensor as key and a list of zones to which objects can exit the sensor as value
		final Map<Sensor, List<Zone>> sensorExits = sagProject.sensor.inject([:]) {exits, sensor ->
			exits[sensor] = sensor.node.outgoing + sensor.node.incoming.findAll{zone -> !zone.isOneway}
			exits
		}
		Closure createEventsAndEdges = {Map sensorChanges, boolean isEntrance ->
			sensorChanges.each {sensor, zones ->
				zones.eachWithIndex {zone, zoneIndex  ->
					//Create an event
					String eventLabel = (isEntrance ? "to_" : "from_") + sensor.name
					if (zones.size() > 1) {
						eventLabel = "${eventLabel}_${zoneIndex.toString()}"
					}	
					EventDeclSubject event = new EventDeclSubject(eventLabel, EventKind.UNCONTROLLABLE)
					watersModule.eventDeclListModifiable << event

					//Create an edge in the sensor component and add the event label to the edge
					ComponentProxy component = sensorComponents[sensor]
	    	        SagToWaters.addEventToComponent(component, !isEntrance, isEntrance, event)
				
	    	        //Create an edge in the zone component (if bounded) and add the event
					component = boundedZoneComponents[zone]
					if (component != null) {
						(0..zone.capacity-1).each {nodeIndex ->
							SagToWaters.addEventToComponent(component, (nodeIndex+(isEntrance ? 1 : 0)), (nodeIndex+(isEntrance ? 0 : 1)), event)	
						}
					}
				}
			}
		}
		createEventsAndEdges(sensorEntrances, true)
		createEventsAndEdges(sensorExits, false)
		
		watersModule
	}
	
	static void addEventToComponent(ComponentProxy component, sourceNodeName, targetNodeName, EventDeclSubject event) {
		NodeProxy sourceNode = component.graph.nodes.find{it.name==sourceNodeName.toString()}
		NodeProxy targetNode = component.graph.nodes.find{it.name==targetNodeName.toString()}
		EdgeSubject edge = component.graph.edges.find{it.source == sourceNode && it.target == targetNode}
		if (edge == null) {
			edge = new EdgeSubject(sourceNode, targetNode)
			component.graph.edgesModifiable << edge
		}
		edge.labelBlock.eventListModifiable << ModuleSubjectFactory.instance.createSimpleIdentifierProxy(event.name)
	}
	
	static void saveWatersModuleToFile(ModuleProxy watersModule, File fileToSaveIn) {
		try	{
			final ProxyMarshaller<ModuleProxy> marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.getInstance(),
			                                                                   CompilerOperatorTable.getInstance())
			marshaller.marshal(watersModule, fileToSaveIn)
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(null, "Error saving module file:" + exception.getMessage())
			//logEntry("JAXBException - Failed to save  '" + wmodf + "'!")
		} catch (final SAXException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage())
			//logEntry("SAXException - Failed to save  '" + wmodf + "'!")
		} catch (final WatersMarshalException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage())
		//logEntry("WatersMarshalException - Failed to save  '" +
		//         wmodf + "'!")
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.getMessage())
			//logEntry("IOException - Failed to save  '" + wmodf + "'!")
		}
	}
}


