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
class SagToWaters {

	static void main(args) {
    	Project sagProject = loadSagProjectFromFile('C:/runtime-New_configuration/test/BallSystem.sag')
		println sagProject.name
		ModuleProxy watersModule = generateWatersModule(sagProject)
		File watersFile = new File("C:/runtime-New_configuration/test/" + watersModule.getName() +'.'+ WmodFileFilter.WMOD)
		saveWatersModuleToFile(watersModule, watersFile)
		watersFile.eachLine{println it}
	}
	
	static {
		SagPackageImpl.init()
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"sag", new XMIResourceFactoryImpl())
	}

	static Project loadSagProjectFromFile(String filename) {
		// Get the serialized sag model model
		final Resource resource = new ResourceSetImpl().getResource(URI
				.createFileURI(filename), true)
		return resource.getContents().get(0)
	}

	static ModuleProxy generateWatersModule(Project sagProject) {
		ModuleSubject watersModule = new ModuleSubject(sagProject.name, null)
		
		//Create one component for every sensor
		Map sensorComponents = sagProject.sensor.inject([:]){components, sensor -> //and create a waters component for each
			GraphSubject watersGraph = new GraphSubject()
			["false","true"].each{watersGraph.nodesModifiable << new SimpleNodeSubject(it)}
			watersGraph.nodesModifiable.find{it.name=="false"}.initial = true
			components[sensor] = new SimpleComponentSubject(new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance()).parseIdentifier(sensor.name),
					                                                   ComponentKind.PLANT, watersGraph)
			watersModule.componentListModifiable << components[sensor]
			components
		}
		//Create one component for every bounded zone
		int i = 0
		Map boundedZoneComponents = sagProject.graph.collect{it.zone}.flatten(). //collect all zones
			                                  findAll{it instanceof BoundedZone}.        //that are bounded
			                                  inject([:]){components, zone -> //and create a waters component for each
			GraphSubject watersGraph = new GraphSubject()
			(0..zone.capacity).each{watersGraph.nodesModifiable << new SimpleNodeSubject(it.toString())}
			watersGraph.nodesModifiable.find{it.name=="0"}.initial = true
			components[zone] = new SimpleComponentSubject(new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance()).parseIdentifier("zone"+i.toString()),
					                                                   ComponentKind.PLANT, watersGraph)
			++i
			watersModule.componentListModifiable << components[zone]
			components
		}
		//Create one event for every sensor entry
		Map sensorsBeingEntered = sagProject.graph.collect{it.zone}.flatten().inject([:]){sensors, zone ->
			if (zone.front != null && zone.front.sensor != null) {
				sensors[zone] = zone.front.sensor
			}
			if (zone.back != null && zone.back.sensor != null && !zone.isOneway) {
				sensors[zone] = zone.back.sensor
			}
			sensors
		}
		Map sensorEnteringEventCounter = sensorsBeingEntered.values().inject([:]){counters, sensor->
			counters[sensor] = 0
			counters
		}
		sensorsBeingEntered.each {zoneAndSensorKeyValuePair -> //zone as key and sensor as value
			Sensor sensor = zoneAndSensorKeyValuePair.value
			
			//Create an event
			String eventLabel = "to_" + sensor.name
			if (sensorsBeingEntered.values().findAll{it.is(sensor)}.size() > 1) {
				eventLabel = eventLabel + "_" + sensorEnteringEventCounter[sensor].toString()
				sensorEnteringEventCounter[sensor] = sensorEnteringEventCounter[sensor] + 1
			}
			EventDeclSubject event = new EventDeclSubject(eventLabel, EventKind.UNCONTROLLABLE)
			watersModule.eventDeclListModifiable << event
			
			//Create an edge in the sensor component and add the event label to the edge
			ComponentProxy component = sensorComponents[sensor]
            SagToWaters.addEventToComponent(component, "false", "true", event)

			//Create an edge in the zone component (if bounded) and add the event
			Zone zone = zoneAndSensorKeyValuePair.key
			component = boundedZoneComponents[zone]
			if (component != null) {
				(zone.capacity..1).each {
					SagToWaters.addEventToComponent(component, it.toString(), (it-1).toString(), event)
				}
			}
		}
		watersModule
	}
	
	static void addEventToComponent(ComponentProxy component, String sourceNodeName, String targetNodeName, EventDeclSubject event) {
		NodeProxy sourceNode = component.graph.nodes.find{it.name==sourceNodeName}
		NodeProxy targetNode = component.graph.nodes.find{it.name==targetNodeName}
		EdgeSubject edge = component.graph.edges.find{it.source == sourceNode && it.target == targetNode}
		if (edge == null) {
			edge = new EdgeSubject(sourceNode, targetNode)
			component.graph.edgesModifiable << edge
		}
		edge.labelBlock.eventListModifiable << ModuleSubjectFactory.instance.createSimpleIdentifierProxy(event.name)
	}
	
	static void saveWatersModuleToFile(ModuleProxy watersModule, File fileToSaveIn) {
		try	{
			final ModuleProxyFactory factory =
				ModuleSubjectFactory.getInstance()
			final OperatorTable optable = CompilerOperatorTable.getInstance()
			final ProxyMarshaller<ModuleProxy> marshaller = new JAXBModuleMarshaller(factory, optable)
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


