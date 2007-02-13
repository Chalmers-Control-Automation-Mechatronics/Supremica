package org.supremica.external.sag.automaton;

import net.sourceforge.waters.model.module.*
import org.supremica.external.sag.*
import net.sourceforge.waters.model.base.EqualCollection
import net.sourceforge.waters.subject.module.VariableSubject
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

class AutomatonGenerator {

	final static AutomatonGenerator instance = new AutomatonGenerator()
	
	private AutomatonGenerator() {
	}
	
	static void main(args) {
		SagBuilder sagBuilder = new SagBuilder();
		
		sagBuilder.project(name:"testproject") {
			controlSignal(name:'u1')
			controlSignal(name:'u2')
			graph(name:"object1", nrOfObjectsIsUnbounded:true) {
				sensor(name:'y1')
				sensor(name:'y2')
				sensor(name:'y3')
				onewayZone(front:'y1', outsideSystemBoundry:true)
				twowayZone(back:'y1', front:'y2', capacity:3, backExitCondition:'y4', forwardCondition:'u1 | u2')
				twowayZone(back:'y2', outsideSystemBoundry:true)
				twowayZone(front:'y2', capacity:1)
				twowayZone(front:'y2', capacity:2)
				onewayZone(back:'y2', front:'y3', capacity:2)
				onewayZone(back:'y3', outsideSystemBoundry:true)
			}
			graph(name:'object2', maxNrOfObjects:2) {
				sensor(name:'y1')
				sensor(name:'y2')
				sensor(name:'y4')
				onewayZone(back:'y1', front:'y2', initialNrOfObjects:2, capacity:2, frontEntryCondition:'u2')
			}
			graph(name:'object3', maxNrOfObjects:1) {
				sensor(name:'y5', initiallyActivated:true)
				sensor(name:'y6')
				sensor(name:'y7')
				twowayZone(front:'y5', outsideSystemBoundry:true)
				twowayZone(back:'y5', front:'y6', capacity:1, forwardCondition:'u1', backwardCondition:'!u1', frontExitCondition:'y2', backEntryCondition:'!y2')
				twowayZone(back:'y6', front:'y7', bounded:false, forwardCondition:'u1 & !y1')
				onewayZone(back:'y7', outsideSystemBoundry:true)
			}
		}
		ModuleProxy generatedModule = instance.generate(sagBuilder.project, false)
		
		def moduleBuilder = new ModuleBuilder()
		
		moduleBuilder.module(name:'testproject') {
			booleanVariable(name:'y1')
			booleanVariable(name:'y2')
			booleanVariable(name:'y3')
			booleanVariable(name:'y4')
			booleanVariable(name:'y5', initial:true)
			booleanVariable(name:'y6')
			booleanVariable(name:'y7')
			booleanVariable(name:'u1')
			booleanVariable(name:'u2')
			integerVariable(name:'object1_between_y1_y2', range:0..3, initial:0)
			integerVariable(name:'object1_beside_y2_0', range:0..1, initial:0)
			integerVariable(name:'object1_beside_y2_1', range:0..2, initial:0)
			integerVariable(name:'object1_between_y2_y3', range:0..2, initial:0)
			integerVariable(name:'object2_between_y1_y2', range:0..2, initial:2)
			booleanVariable(name:'object3_between_y5_y6')
			booleanVariable(name:'object3_between_y6_y7')
			event(name:'object1_from_y1', controllable:false)
			event(name:'object1_to_y1_0', controllable:false)
			event(name:'object1_to_y1_1', controllable:false)
			event(name:'object1_from_y2_0', controllable:false)
			event(name:'object1_from_y2_1', controllable:false)
			event(name:'object1_from_y2_2', controllable:false)
			event(name:'object1_from_y2_3', controllable:false)
			event(name:'object1_from_y2_4', controllable:false)
			event(name:'object1_to_y2_0', controllable:false)
			event(name:'object1_to_y2_1', controllable:false)
			event(name:'object1_to_y2_2', controllable:false)
			event(name:'object1_to_y2_3', controllable:false)
			event(name:'object1_from_y3', controllable:false)
			event(name:'object1_to_y3', controllable:false)
			event(name:'object2_from_y1', controllable:false, ranges:[0..1])
			event(name:'object2_to_y2', controllable:false, ranges:[0..1])
			event(name:'object3_from_y5_0', controllable:false)
			event(name:'object3_from_y5_1', controllable:false)
			event(name:'object3_to_y5_0', controllable:false)
			event(name:'object3_to_y5_1', controllable:false)
			event(name:'object3_from_y6_0', controllable:false)
			event(name:'object3_from_y6_1', controllable:false)
			event(name:'object3_to_y6_0', controllable:false)
			event(name:'object3_to_y6_1', controllable:false)
			event(name:'object3_from_y7_0', controllable:false)
			event(name:'object3_from_y7_1', controllable:false)
			event(name:'object3_to_y7', controllable:false)
			plant(name:'object1_y1', initialState:'false') {
				state(name:'false')
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y1_0'], guard:'!y1') {
					action('y1 = 1')
				}
				transition(from:'false', to:'true', events:['object1_to_y1_1'], guard:'!y1 & object1_between_y1_y2 > 0') {
					action('y1 = 1')
					action('object1_between_y1_y2 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y1'], guard:'object1_between_y1_y2 < 3 & (u1 | u2) & y4') {
					action('y1 = 0')
					action('object1_between_y1_y2 += 1')
				}
			}
			plant(name:'object1_y2', initialState:'false') {
				state(name:'false')
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y2_0'], guard:'!y2 & object1_between_y1_y2 > 0 & (u1 | u2)') {
					action('y2 = 1')
					action('object1_between_y1_y2 -= 1')
				}
				transition(from:'false', to:'true', events:['object1_to_y2_1'], guard:'!y2') {
					action('y2 = 1')
				}
				transition(from:'false', to:'true', events:['object1_to_y2_2'], guard:'!y2 & object1_beside_y2_0 > 0') {
					action('y2 = 1')
					action('object1_beside_y2_0 -= 1')
				}
				transition(from:'false', to:'true', events:['object1_to_y2_3'], guard:'!y2 & object1_beside_y2_1 > 0') {
					action('y2 = 1')
					action('object1_beside_y2_1 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y2_0'], guard:'object1_between_y1_y2 < 3') {
					action('y2 = 0')
					action('object1_between_y1_y2 += 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y2_1']) {
					action('y2 = 0')
				}
				transition(from:'true', to:'false', events:['object1_from_y2_2'], guard:'object1_beside_y2_0 < 1') {
					action('y2 = 0')
					action('object1_beside_y2_0 += 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y2_3'], guard:'object1_beside_y2_1 < 2') {
					action('y2 = 0')
					action('object1_beside_y2_1 += 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y2_4'], guard:'object1_between_y2_y3 < 2') {
					action('y2 = 0')
					action('object1_between_y2_y3 += 1')
				}
			}
			plant(name:'object1_y3', initialState:'false') {
				state(name:'false')
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y3'], guard:'!y3 & object1_between_y2_y3 > 0') {
					action('y3 = 1')
					action('object1_between_y2_y3 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y3']) {
					action('y3 = 0')
				}
			}
			plant(name:'object3', initialState:'y5') {
				state(name:'y5')
				state(name:'y6')
				state(name:'y7')
				state(name:'between_y5_y6')
				state(name:'between_y6_y7')
				state(name:'outside')
				transition(from:'outside', to:'y5', events:['object3_to_y5_0']) {action('y5 = 1')}
				transition(from:'between_y5_y6', to:'y5', events:['object3_to_y5_1'], guard:'!u1 & !y2') {
					action('y5 = 1')
					action('object3_between_y5_y6 = 0')
				}
				transition(from:'y5', to:'outside', events:['object3_from_y5_0']) {action('y5 = 0')}
				transition(from:'y5', to:'between_y5_y6', events:['object3_from_y5_1'], guard:'u1') {
					action('y5 = 0')
					action('object3_between_y5_y6 = 1')
				}
				transition(from:'between_y5_y6', to:'y6', events:['object3_to_y6_0'], guard:'u1') {
					action('y6 = 1')
					action('object3_between_y5_y6 = 0')
				}
				transition(from:'between_y6_y7', to:'y6', events:['object3_to_y6_1']) {
					action('y6 = 1')
					action('object3_between_y6_y7 = 0')
				}
				transition(from:'y6', to:'between_y5_y6', events:['object3_from_y6_0'], guard:'!u1 & y2') {
					action('y6 = 0')
					action('object3_between_y5_y6 = 1')
				}
				transition(from:'y6', to:'between_y6_y7', events:['object3_from_y6_1'], guard:'u1 & !y1') {
					action('y6 = 0')
					action('object3_between_y6_y7 = 1')
				}
				transition(from:'between_y6_y7', to:'y7', events:['object3_to_y7'], guard:'(u1 & !y1)') {
					action('y7 = 1')
					action('object3_between_y6_y7 = 0')
				}
				transition(from:'y7', to:'between_y6_y7', events:['object3_from_y7_0']) {
					action('y7 = 0')
					action('object3_between_y6_y7 = 1')
				}
				transition(from:'y7', to:'outside', events:['object3_from_y7_1']) {action('y7 = 0')}
			}
			foreach(name:'i', range:0..1) {
				plant(name:'object2[i]', initialState:'between_y1_y2') {
					state(name:'y1')
					state(name:'y2')
					state(name:'y4')
					state(name:'between_y1_y2')
					transition(from:'y1', to:'between_y1_y2', events:['object2_from_y1[i]'], guard:'object2_between_y1_y2 < 2') {
						action('y1 = 0')
						action('object2_between_y1_y2 += 1')
					}
					transition(from:'between_y1_y2', to:'y2', events:['object2_to_y2[i]'], guard:'!y2 & u2') {
						action('y2 = 1')
						action('object2_between_y1_y2 -= 1')
					}
				}
			}
		}
		ModuleProxy manualModule = moduleBuilder.module
		Closure collectionsAreEqual = { c1, c2 ->
			def result = [true]
			if (c1.size() != c2.size()) {
				result = [false, "size differs: c1.size()==${c1.size()}, c2.size()==${c2.size()}"]
			}
			c1.eachWithIndex { element, i ->
				if (!element.equalsByContents(c2[i])) {
					result = [false, "Inequality at index $i: ${c1[i]}, ${c2[i]}"] 
				}
			}
			if (!result[0]) {
				println c1
				println c2
			}
			return result
		}
		def result
		result = collectionsAreEqual(generatedModule.eventDeclList, manualModule.eventDeclList); assert result[0], result[1]
		def generatedComponents = generatedModule.componentList.grep(SimpleComponentProxy.class) + generatedModule.componentList.grep(ForeachComponentProxy.class).body 
		def manualComponents = manualModule.componentList.grep(SimpleComponentProxy.class) + manualModule.componentList.grep(ForeachComponentProxy.class).body 
		result = collectionsAreEqual(generatedComponents.graph.nodes, manualComponents.graph.nodes); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.source, manualComponents.graph.edges.source); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.target, manualComponents.graph.edges.target); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.labelBlock, manualComponents.graph.edges.labelBlock); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.guardActionBlock.guards, manualComponents.graph.edges.guardActionBlock.guards); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.guardActionBlock.actions, manualComponents.graph.edges.guardActionBlock.actions); assert result[0], result[1] 
		result = collectionsAreEqual(generatedComponents.graph.edges.guardActionBlock.actions, manualComponents.graph.edges.guardActionBlock.actions); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges.guardActionBlock, manualComponents.graph.edges.guardActionBlock); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph.edges, manualComponents.graph.edges); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.graph, manualComponents.graph); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.variables.type, manualComponents.variables.type); assert result[0], result[1]
		result = collectionsAreEqual(generatedComponents.variables, manualComponents.variables); assert result[0], result[1]
		result = collectionsAreEqual(generatedModule.componentList, manualModule.componentList); assert result[0], result[1]
		assert generatedModule.equalsByContents(manualModule)
		assert false, 'fixa overlap'
		assert false, 'testa mätlyften med fler än en kula'
		assert false, 'fixa guards med zoner'
		assert false, 'fixa köer'
		assert false, 'fixa range och konstanter'
		assert false, 'fixa exceptions'
		assert false, 'fixa resursbokning'
		assert false, 'fixa delade resurser mellan grafer'
	}

	ModuleProxy generate(Project project, boolean forSynthesis) {
		ModuleBuilder builder = new ModuleBuilder()
		
		Closure addStates = { graph ->
			(graph.sensor + graph.zone.findAll{!it.outsideSystemBoundry}).eachWithIndex { location, i ->
				builder.state(name:formatStateName(location))
			}
			if (graph.zone.any{it.outsideSystemBoundry}) {
				builder.state(name:OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME)
			}
		}
		Closure addTransitions  = { graph ->
			graph.sensor.each { sensor ->
				[true, false].each {incoming ->
					(incoming ? incomingZones(sensor) : outgoingZones(sensor)).each { zone ->
						def sensorCondition = incoming ? "!${sensor.name}" : null
						builder.transition(from:formatStateName(incoming ? zone : sensor),
		                                   to:formatStateName(incoming ? sensor: zone),
	                                       events:[formatEventName(sensor, zone, incoming) + (graph.maxNrOfObjects > 1 ? "[${INDEX_NAME}]" : '')],
	                                       guard:formatGuard(sensor, zone, incoming)) {
							action("${sensor.name} = ${incoming ? 1 : 0}")
							if (!zone.outsideSystemBoundry) {
								if (zone.capacity > 1) {
									action("${formatZoneVariableName(zone)} ${incoming ? '-' : '+'}= 1")
								} else {
									action("${formatZoneVariableName(zone)} = ${incoming ? 0 : 1}")
								}
							}
						}
					}
				}
			}
		}

		builder.module(name:project.name) {
			(project.sensorSignal).each { signal -> 
				builder.booleanVariable(name:signal.name, initial:signal.sensor.any{it.initiallyActivated})
			}
			(project.controlSignal).each { signal -> 
				builder.booleanVariable(name:signal.name, initial:false)
			}
			project.graph.zone.findAll{it.bounded && it.graph.maxNrOfObjects != 1}.each { zone ->
				builder.integerVariable(name:formatZoneVariableName(zone),
				                        range:0..zone.capacity,
				                        initial:zone.initialNrOfObjects)
			}
			project.graph.zone.findAll{it.graph.maxNrOfObjects == 1 && !it.outsideSystemBoundry}.each { zone ->
				builder.booleanVariable(name:formatZoneVariableName(zone), initial:zone.initialNrOfObjects)
			}
			project.graph.sensor.each {sensor ->
				outgoingZones(sensor).each { zone ->
					builder.event(name:formatEventName(sensor, zone, false),
					              controllable:false, ranges:sensor.graph.maxNrOfObjects > 1 ? [0..sensor.graph.maxNrOfObjects-1] : null)
				}
				incomingZones(sensor).each { zone ->
					builder.event(name:formatEventName(sensor, zone, true),
				                  controllable:false, ranges:sensor.graph.maxNrOfObjects > 1 ? [0..sensor.graph.maxNrOfObjects-1] : null)
				}
			}
			project.graph.findAll{it.nrOfObjectsIsUnbounded}.sensor.each { sensor ->
				builder.plant(name:"${sensor.graph.name}_${sensor.name}", initialState:"${sensor.initiallyActivated}") {
					builder.state(name:'false')
					builder.state(name:'true')
					[true, false].each {incoming ->
						(incoming ? incomingZones(sensor) : outgoingZones(sensor)).each { zone ->
							builder.transition(from:"${!incoming}",
							                   to:"${incoming}",
						                       events:[formatEventName(sensor, zone, incoming)],
						                       guard:formatGuard(sensor, zone, incoming)) {
								action("${sensor.name} = ${incoming ? 1 : 0}")
								if (!zone.outsideSystemBoundry) {
									action("${formatZoneVariableName(zone)} ${incoming ? '-' : '+'}= 1")
								}
							}
						}
					}
				}
			}
			project.graph.findAll{it.maxNrOfObjects == 1}.each { graph ->
				builder.plant(name:graph.name, initialState:formatStateName(findInitialState(graph))) {
					addStates(graph)
					addTransitions(graph)
				}
			}
			project.graph.findAll{it.maxNrOfObjects > 1}.each { graph ->
				builder.foreach(name:INDEX_NAME, range:0..graph.maxNrOfObjects - 1) {
					builder.plant(name:"${graph.name}[${INDEX_NAME}]", initialState:formatStateName(findInitialState(graph))) {
						addStates(graph)
						addTransitions(graph)
					}
				}
			}
		}
		builder.module
	}
	
	final static OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME = 'outside'
	final static INDEX_NAME = 'i'
	
	private formatStateName(Sensor sensor) {
		sensor.name
	}
	
	private findInitialState(graph) {
		(graph.sensor.findAll{it.initiallyActivated} + graph.zone.findAll{it.initialNrOfObjects} + graph.zone.findAll{it.outsideSystemBoundry})[0]
	}
	
	private formatStateName(Zone zone) {
		if (zone.outsideSystemBoundry) {
			return OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME
		}
		def sensorsOfZone = [zone.back, zone.front].grep(Sensor.class)
		def zonesWithSameSensors = zone.graph.zone.findAll{it.bounded && [it.front, it.back].grep(Sensor.class) == sensorsOfZone}
		def suffix = zonesWithSameSensors.size() > 1 ? "_${zonesWithSameSensors.indexOf(zone)}" : ''
		"${sensorsOfZone.size() == 1 ? 'beside' : 'between'}_${sensorsOfZone.name.join('_')}$suffix"
	}
	
	private outgoingZones(Sensor sensor) {
		sensor.graph.zone.grep(sensor.outgoing + sensor.incoming.findAll{zone -> !zone.oneway})
	}
	
	private incomingZones(Sensor sensor) {
		sensor.graph.zone.grep(sensor.outgoing.findAll{zone -> !zone.oneway} + sensor.incoming)
	}
	
	private String formatZoneVariableName(zone) {
		"${zone.graph.name}_${formatStateName(zone)}"
	}
	
	private String formatEventName(Sensor sensor, zone, isSensorActivation) {
		String prefix = "${sensor.graph.name}_" + (isSensorActivation ? "to_" : "from_")
		def zones = (isSensorActivation ? incomingZones(sensor) : outgoingZones(sensor)) 
		String suffix = zones.size() > 1 ? "_${zones.indexOf(zone)}" : ''
		"$prefix${sensor.name}$suffix"
	}
	
	private String formatGuard(Sensor sensor, zone, isSensorActivation) {
		Closure parenthesisNeeded = {expr ->
			def factory = ModuleSubjectFactory.instance
			def parser = new ExpressionParser(factory.instance, CompilerOperatorTable.instance)
		 //println "$expr, ${parser.parse(expr).dump()}"
		 	parser.parse(expr) instanceof BinaryExpressionProxy
		}
		def comparison = isSensorActivation ? '> 0' : "< ${zone.capacity}"
		def zoneCondition = zone.bounded && zone.graph.maxNrOfObjects != 1 && (!isSensorActivation || zone.graph.nrOfObjectsIsUnbounded) ?
		                        "${formatZoneVariableName(zone)} $comparison" : null
		def sensorCondition = isSensorActivation && sensor.graph.maxNrOfObjects != 1 ? "!${sensor.name}" : null
		def directionCondition = (isSensorActivation && zone.front == sensor) || (!isSensorActivation && zone.back == sensor) ? zone.forwardCondition : zone.backwardCondition;
        if (directionCondition && parenthesisNeeded(directionCondition))	directionCondition = '(' + directionCondition + ')'
		def entryExitCondition
        if (isSensorActivation && zone.front == sensor) entryExitCondition = zone.frontEntryCondition
        else if (isSensorActivation && zone.back == sensor) entryExitCondition = zone.backEntryCondition
        else if (!isSensorActivation && zone.front == sensor) entryExitCondition = zone.frontExitCondition
        else if (!isSensorActivation && zone.back == sensor) entryExitCondition = zone.backExitCondition
        if (entryExitCondition && parenthesisNeeded(entryExitCondition)) entryExitCondition = '(' + entryExitCondition + ')'
        [sensorCondition, zoneCondition, directionCondition, entryExitCondition].findAll{it}.join(' & ')
	}
}