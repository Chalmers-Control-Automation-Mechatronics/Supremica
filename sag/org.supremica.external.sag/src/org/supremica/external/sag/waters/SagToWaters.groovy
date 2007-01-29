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
import net.sourceforge.waters.model.expr.Operator

import org.supremica.gui.ide.IDE
import org.supremica.gui.InterfaceManager

import javax.swing.*

import javax.xml.bind.JAXBException
import org.xml.sax.SAXException

// NOTE THAT all generics used here are only for documentation.
// Groovy does not enforce anything based on generics, neither dynamically or statically

class SagToWaters {

	static void main(args) {
		def path = '../org.supremica.external.sag.tests/testcases'
    	Project sagProject = loadSagProjectFromFile(path+'/testcase1.sag')
		
    	ModuleProxy watersModule = generateAutomata(sagProject)
		File automataFile = new File("${path}/${watersModule.name}.${WmodFileFilter.WMOD}")
		saveWatersModuleToFile(watersModule, automataFile)
//		automataFile.eachLine{println it}
		watersModule = generateExtendedAutomata(sagProject)
		File extendedAutomataFile = new File("${path}/${watersModule.name}_efa.${WmodFileFilter.WMOD}")
		saveWatersModuleToFile(watersModule, extendedAutomataFile)
//		extendedAutomataFile.eachLine{println it}
        
		def launchIDE = {
			InterfaceManager.instance.initLookAndFeel();
			IDE ide = new IDE()
			ide.visible = true
			ide.openFiles([automataFile, extendedAutomataFile])
    	}
		launchIDE()
	}
	
	static {
		SagPackageImpl.init()
		Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.sag = new XMIResourceFactoryImpl()
	}

	static Project loadSagProjectFromFile(String filename) {
		// Get the serialized sag model
		final Resource resource = new ResourceSetImpl().getResource(URI.createFileURI(filename), true)
		return resource.contents[0]
	}

	private static final parser = new ExpressionParser(ModuleSubjectFactory.instance, CompilerOperatorTable.instance)
	
	private static newComponent(module, componentName, componentGraph) {
		def component = new SimpleComponentSubject(parser.parseIdentifier(componentName),
                ComponentKind.PLANT, componentGraph)
		module.componentListModifiable << component
		component
	}
	
	private static newBooleanComponent(module, componentName) {
		GraphSubject watersGraph = new GraphSubject()
		["false","true"].each{watersGraph.nodesModifiable << new SimpleNodeSubject(it)}
		watersGraph.nodesModifiable.find{it.name=="false"}.initial = true
		newComponent(module, componentName, watersGraph)
	}
	
	private static String createZoneName(graph, index) {
		"${graph.name}_zone${index}"
	}
	
	static ModuleProxy generateExtendedAutomata(sagProject) {
		def watersModule = new ModuleSubject(sagProject.name, null)
		
		def componentForSensorVariables = newComponent(watersModule, "sensors", new GraphSubject())
		componentForSensorVariables.graph.nodesModifiable << new SimpleNodeSubject('dummy', null, true, null, null, null)
		def sensorComponents = sagProject.sensor.inject([:]){components, sensor -> //and create a waters component for each
			components[sensor] = SagToWaters.newBooleanComponent(watersModule, sensor.name+"_automata")
			componentForSensorVariables.variablesModifiable << new VariableSubject(sensor.name, 0, 1, 0, null);
			components
		}
		def componentForZoneVariables = newComponent(watersModule, "zones", new GraphSubject())
		componentForZoneVariables.graph.nodesModifiable << new SimpleNodeSubject('dummy', null, true, null, null, null)
		int i = 0
		def boundedZoneVariables = sagProject.graph.collect{graph -> i = 0; graph.zone.findAll{zone -> zone instanceof BoundedZone}}.flatten().
			                                         inject([:]){variables, zone ->
			variables[zone] = new VariableSubject(SagToWaters.createZoneName(zone.graph, i), 0, zone.capacity, 0, null)
			componentForZoneVariables.variablesModifiable << variables[zone]
			++i
			variables
		}
		def changes = createEvents(watersModule, sagProject)
		changes.each {change ->
			//Add to sensor component
			SagToWaters.addEventToComponent(sensorComponents[change.sensor], !change.isSensorActivation, change.isSensorActivation, change.automatonEvent, true)
			
			// Add guards and actions
			// Start with action to update sensor variable
			SagToWaters.addActionToComponent(sensorComponents[change.sensor], change.automatonEvent, "${change.sensor.name} = ${change.isSensorActivation ? 1 : 0}")
			
			def zoneVariable = boundedZoneVariables[change.zone]
			if (zoneVariable) {
				if (change.isSensorActivation) {
					SagToWaters.addGuardToComponent(sensorComponents[change.sensor], change.automatonEvent, "${zoneVariable.name} > 0 ")
					SagToWaters.addActionToComponent(sensorComponents[change.sensor], change.automatonEvent, "${zoneVariable.name} -= 1 ")
				} else {
					SagToWaters.addGuardToComponent(sensorComponents[change.sensor], change.automatonEvent, "${zoneVariable.name} < ${change.zone.capacity}")
					SagToWaters.addActionToComponent(sensorComponents[change.sensor], change.automatonEvent, "${zoneVariable.name} += 1 ")
				}
			}
		}

		watersModule
	}

	private static void addActionToComponent(component, event, String action) {
		def edge = component.graph.edges.find{it.labelBlock.eventList.any{it.name==event.name}}
		if (!edge.guardActionBlock) {
			edge.guardActionBlock = ModuleSubjectFactory.instance.createGuardActionBlockProxy()
		}
		edge.guardActionBlock.actionsModifiable << parser.parse(action)
	}
	
	private static void addGuardToComponent(component, event, String guard) {
		def edge = component.graph.edges.find{it.labelBlock.eventList.any{it.name==event.name}}
		if (!edge.guardActionBlock) {
			edge.guardActionBlock = ModuleSubjectFactory.instance.createGuardActionBlockProxy()
		}
		edge.guardActionBlock.guardsModifiable << parser.parse(guard, Operator.TYPE_BOOLEAN)
	}
	
	//Returns a list of maps like this [[sensor:<a sensor>, zone:<a zone>, isSensorActivation:<true or false>, automatonEvent:<a waters event>], [...], ...]
	private static createEvents(watersModule, sagProject) {
		def changes = sagProject.sensor.inject([]){changes, sensor ->
			(sensor.node.outgoing + sensor.node.incoming.findAll{zone -> !zone.isOneway}).eachWithIndex {zone, i ->
				changes << [sensor: sensor,
				            zone: zone,
				            isSensorActivation: false,
				            index: i]
			}
			(sensor.node.incoming + sensor.node.outgoing.findAll{zone -> !zone.isOneway}).eachWithIndex {zone, i ->
				changes << [sensor: sensor,
			                zone: zone,
			                isSensorActivation: true,
			                index: i]
			}
			changes
		}
		changes.each {change ->
			//Create an event
			String eventLabel = (change.isSensorActivation ? "to_" : "from_") + change.sensor.name
			if (changes.findAll{it.sensor==change.sensor && it.isSensorActivation == change.isSensorActivation}.size() > 1) {
				eventLabel = "${eventLabel}_${change.index.toString()}"
			}
			EventDeclSubject event = new EventDeclSubject(eventLabel, EventKind.UNCONTROLLABLE)
			watersModule.eventDeclListModifiable << event
			change.automatonEvent = event
		}
		changes
	}
	
/*	private static createEvents(watersModule, sagProject) {
		def changes = getChanges(sagProject)
		changes.inject([]) {events, change ->
			//Create an event
			String eventLabel = (change.isSensorActivation ? "to_" : "from_") + change.sensor.name
			if (changes.findAll{it.sensor==change.sensor && it.isSensorActivation == change.isSensorActivation}.size() > 1) {
				eventLabel = "${eventLabel}_${change.index.toString()}"
			}
			EventDeclSubject event = new EventDeclSubject(eventLabel, EventKind.UNCONTROLLABLE)
			watersModule.eventDeclListModifiable << event
			events << [automatonEvent:event, *:change]
		}				
	}
*/
	
	static ModuleProxy generateAutomata(Project sagProject) {
		ModuleSubject watersModule = new ModuleSubject(sagProject.name, null)
		
		//Create one component for every sensor
		final Map<Sensor, SimpleComponentSubject> sensorComponents = sagProject.sensor.inject([:]){components, sensor -> //and create a waters component for each
			components[sensor] =SagToWaters.newBooleanComponent(watersModule, sensor.name)
			components
		}
		
		//Create one component for every bounded zone
		int i = 0
		final Map<BoundedZone, SimpleComponentSubject> boundedZoneComponents = sagProject.graph.collect{graph -> i = 0; graph.zone.findAll{zone -> zone instanceof BoundedZone}}.flatten().
			                                         inject([:]){components, zone ->
			GraphSubject watersGraph = new GraphSubject()
			(0..zone.capacity).each{watersGraph.nodesModifiable << new SimpleNodeSubject(it.toString())}
			watersGraph.nodesModifiable.find{it.name=="0"}.initial = true
			components[zone] = SagToWaters.newComponent(watersModule, SagToWaters.createZoneName(zone.graph, i), watersGraph)
			++i
			components
		}
		
		def changes = createEvents(watersModule, sagProject)
		changes.each {change ->
			//Add to sensor component
			SagToWaters.addEventToComponent(sensorComponents[change.sensor], !change.isSensorActivation, change.isSensorActivation, change.automatonEvent, false)
			
			//Add to zone component
			def component = boundedZoneComponents[change.zone]
			if (component) {
				(0..change.zone.capacity-1).each {nodeIndex ->
					SagToWaters.addEventToComponent(component, (nodeIndex+(change.isSensorActivation ? 1 : 0)), (nodeIndex+(change.isSensorActivation ? 0 : 1)), change.automatonEvent, false)
				}
			}
		}
		watersModule
	}
	
	private static void addEventToComponent(component, sourceNodeName, targetNodeName, event, alwaysCreateNewEdge=false) {
		NodeProxy sourceNode = component.graph.nodes.find{it.name==sourceNodeName.toString()}
		NodeProxy targetNode = component.graph.nodes.find{it.name==targetNodeName.toString()}
		EdgeSubject edge = alwaysCreateNewEdge ? null : component.graph.edges.find{it.source == sourceNode && it.target == targetNode}
		if (!edge) {
			edge = new EdgeSubject(sourceNode, targetNode)
			component.graph.edgesModifiable << edge
		}
		edge.labelBlock.eventListModifiable << ModuleSubjectFactory.instance.createSimpleIdentifierProxy(event.name)
	}

	
	static void saveWatersModuleToFile(ModuleProxy watersModule, File fileToSaveIn) {
		try	{
			final ProxyMarshaller<ModuleProxy> marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.instance,
			                                                                   CompilerOperatorTable.instance)
			marshaller.marshal(watersModule, fileToSaveIn)
		} catch (final JAXBException exception) {
			JOptionPane.showMessageDialog(null, "Error saving module file:" + exception.message)
			//logEntry("JAXBException - Failed to save  '" + wmodf + "'!")
		} catch (final SAXException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.essage)
			//logEntry("SAXException - Failed to save  '" + wmodf + "'!")
		} catch (final WatersMarshalException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.message)
		//logEntry("WatersMarshalException - Failed to save  '" +
		//         wmodf + "'!")
		} catch (final IOException exception) {
			JOptionPane.showMessageDialog(null,
									  "Error saving module file:" +
									  exception.message)
			//logEntry("IOException - Failed to save  '" + wmodf + "'!")
		}
	}
}