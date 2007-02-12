package org.supremica.external.sag.automaton;

import net.sourceforge.waters.model.module.*
import org.supremica.external.sag.*
import net.sourceforge.waters.model.base.EqualCollection

class AutomatonGenerator {

	final static AutomatonGenerator instance = new AutomatonGenerator()
	
	private AutomatonGenerator() {
	}
	
	static void main(args) {
		SagBuilder sagBuilder = new SagBuilder();
		
		sagBuilder.project(name:"testproject") {
			graph(name:"object1", nrOfObjectsIsUnbounded:true) {
				sensor(name:'y1')
				sensor(name:'y2')
				sensor(name:'y3')
				onewayZone(front:'y1', outsideSystemBoundry:true)
				twowayZone(back:'y1', front:'y2', capacity:3)
				twowayZone(back:'y2', outsideSystemBoundry:true)
				twowayZone(front:'y2', capacity:1)
				twowayZone(front:'y2', capacity:2)
				onewayZone(back:'y2', front:'y3', capacity:2)
				onewayZone(back:'y3', outsideSystemBoundry:true)
			}
			graph(name:'object2', maxNrOfObjects:3) {
				sensor(name:'y1')
				sensor(name:'y2')
				sensor(name:'y4')
				onewayZone(back:'y1', front:'y2', capacity:2)
			}
			graph(name:'object3', maxNrOfObjects:1) {
				sensor(name:'y5')
				sensor(name:'y6')
				sensor(name:'y7')
				twowayZone(front:'y5', outsideSystemBoundry:true)
				twowayZone(back:'y5', front:'y6', capacity:1)
				twowayZone(back:'y6', front:'y7', bounded:false)
				onewayZone(back:'y7', outsideSystemBoundry:true)
			}
		}
		ModuleProxy generatedModule = instance.generate(sagBuilder.project)
		
		def moduleBuilder = new ModuleBuilder()
		
		moduleBuilder.module(name:'testproject') {
			booleanVariable(name:'y1')
			booleanVariable(name:'y2')
			booleanVariable(name:'y3')
			booleanVariable(name:'y4')
			booleanVariable(name:'y5')
			booleanVariable(name:'y6')
			booleanVariable(name:'y7')
			integerVariable(name:'object1_between_y1_y2', range:0..3, initial:0)
			integerVariable(name:'object1_beside_y2_0', range:0..1, initial:0)
			integerVariable(name:'object1_beside_y2_1', range:0..2, initial:0)
			integerVariable(name:'object1_between_y2_y3', range:0..2, initial:0)
			integerVariable(name:'object2_between_y1_y2', range:0..2, initial:0)
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
			event(name:'object2_from_y1', controllable:false, ranges:[0..2])
			event(name:'object2_to_y2', controllable:false, ranges:[0..2])
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
			plant(name:'object1_y1') {
				state(name:'false', initial:true)
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y1_0'], guard:'!y1') {
					action('y1 = 1')
				}
				transition(from:'false', to:'true', events:['object1_to_y1_1'], guard:'!y1 & object1_between_y1_y2 > 0') {
					action('y1 = 1')
					action('object1_between_y1_y2 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y1'], guard:'object1_between_y1_y2 < 3') {
					action('y1 = 0')
					action('object1_between_y1_y2 += 1')
				}
			}
			plant(name:'object1_y2') {
				state(name:'false', initial:true)
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y2_0'], guard:'!y2 & object1_between_y1_y2 > 0') {
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
			plant(name:'object1_y3') {
				state(name:'false', initial:true)
				state(name:'true')
				transition(from:'false', to:'true', events:['object1_to_y3'], guard:'!y3 & object1_between_y2_y3 > 0') {
					action('y3 = 1')
					action('object1_between_y2_y3 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y3']) {
					action('y3 = 0')
				}
			}
			plant(name:'object3') {
				state(name:'y5', initial:true)
				state(name:'y6')
				state(name:'y7')
				state(name:'between_y5_y6')
				state(name:'between_y6_y7')
				state(name:'outside')
				transition(from:'outside', to:'y5', events:['object3_to_y5_0'], guard:'!y5') {action('y5 = 1')}
				transition(from:'between_y5_y6', to:'y5', events:['object3_to_y5_1'], guard:'!y5') {action('y5 = 1')}
				transition(from:'y5', to:'outside', events:['object3_from_y5_0']) {action('y5 = 0')}
				transition(from:'y5', to:'between_y5_y6', events:['object3_from_y5_1']) {action('y5 = 0')}
				transition(from:'between_y5_y6', to:'y6', events:['object3_to_y6_0'], guard:'!y6') {action('y6 = 1')}
				transition(from:'between_y6_y7', to:'y6', events:['object3_to_y6_1'], guard:'!y6') {action('y6 = 1')}
				transition(from:'y6', to:'between_y5_y6', events:['object3_from_y6_0']) {action('y6 = 0')}
				transition(from:'y6', to:'between_y6_y7', events:['object3_from_y6_1']) {action('y6 = 0')}
				transition(from:'between_y6_y7', to:'y7', events:['object3_to_y7'], guard:'!y7') {action('y7 = 1')}
				transition(from:'y7', to:'between_y6_y7', events:['object3_from_y7_0']) {action('y7 = 0')}
				transition(from:'y7', to:'outside', events:['object3_from_y7_1']) {action('y7 = 0')}
			}
			foreach(name:'i', range:0..2) {
				plant(name:'object2[i]') {
					state(name:'y1', initial:true)
					state(name:'y2')
					state(name:'y4')
					state(name:'between_y1_y2')
					transition(from:'between_y1_y2', to:'y2', events:['object2_to_y2[i]'], guard:'!y2 & object2_between_y1_y2 > 0') {
						action('y2 = 1')
						action('object2_between_y1_y2 -= 1')
					}
					transition(from:'y1', to:'between_y1_y2', events:['object2_to_y2[i]'], guard:'object2_between_y1_y2 < 2') {
						action('y1 = 0')
						action('object2_between_y1_y2 += 1')
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
		assert false, 'fixa köer'
		assert false, 'fixa initialtillstånd'
		assert false, 'fixa range och konstanter'
		assert false, 'fixa sensor -> sensor signal'
		assert false, 'fixa control signal'
		
	}

	ModuleProxy generate(Project project) {
		ModuleBuilder builder = new ModuleBuilder()
		
		Closure addStates = { graph ->
			(graph.sensor + graph.zone.findAll{!it.outsideSystemBoundry}).eachWithIndex { location, i ->
				builder.state(name:formatStateName(location), initial:i == 0)
				println formatStateName(location)
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
	                                       events:[formatEventName(sensor, zone, incoming) + (graph.maxNrOfObjects > 1 ? "[INDEX_NAME]" : '')],
	                                       guard:incoming ? "!${sensor.name}" : null) {
							action("${sensor.name} = ${incoming ? 1 : 0}")
							if (graph.maxNrOfObjects != 1 && !zone.outsideSystemBoundry) {
								action("${formatZoneVariableName(zone)} ${incoming ? '-' : '+'}= 1")
							}
						}
					}
				}
			}
		}

		builder.module(name:project.name) {
			project.sensor.each { sensor -> 
				builder.booleanVariable(name:sensor.name, initial:false)
			}
			project.graph.zone.findAll{it.bounded && it.graph.maxNrOfObjects != 1}.each { zone ->
				builder.integerVariable(name:formatZoneVariableName(zone),
				                        range:0..zone.capacity,
				                        initial:0)
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
				builder.plant(name:"${sensor.graph.name}_${sensor.name}") {
					builder.state(name:'false', initial:true)
					builder.state(name:'true')
					[true, false].each {incoming ->
						(incoming ? incomingZones(sensor) : outgoingZones(sensor)).each { zone ->
							def comparison = incoming ? '> 0' : "< ${zone.capacity}"
							def zoneCondition = zone.outsideSystemBoundry ? null : "${formatZoneVariableName(zone)} $comparison"
							def sensorCondition = incoming ? "!${sensor.name}" : null
							builder.transition(from:"${!incoming}",
							                   to:"${incoming}",
						                       events:[formatEventName(sensor, zone, incoming)],
						                       guard:[sensorCondition, zoneCondition].findAll{it}.join(' & ')) {
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
				builder.plant(name:graph.name) {
					addStates(graph)
					addTransitions(graph)
				}
			}
			project.graph.findAll{it.maxNrOfObjects > 1}.each { graph ->
				builder.foreach(name:INDEX_NAME, range:0..graph.maxNrOfObjects) {
					builder.plant(name:"${graph.name}[${INDEX_NAME}]") {
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
	
	private formatStateName(SensorNode sensor) {
		sensor.name
	}
	
	private formatStateName(Zone zone) {
		if (zone.outsideSystemBoundry) {
			return OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME
		}
		def sensorsOfZone = [zone.back, zone.front].grep(SensorNode.class)
		def zonesWithSameSensors = zone.graph.zone.findAll{it.bounded && [it.front, it.back].grep(SensorNode.class) == sensorsOfZone}
		def suffix = zonesWithSameSensors.size() > 1 ? "_${zonesWithSameSensors.indexOf(zone)}" : ''
		"${sensorsOfZone.size() == 1 ? 'beside' : 'between'}_${sensorsOfZone.name.join('_')}$suffix"
	}
	
	private outgoingZones(SensorNode sensor) {
		sensor.graph.zone.grep(sensor.outgoing + sensor.incoming.findAll{zone -> !zone.oneway})
	}
	
	private incomingZones(SensorNode sensor) {
		sensor.graph.zone.grep(sensor.outgoing.findAll{zone -> !zone.oneway} + sensor.incoming)
	}
	
	private String formatZoneVariableName(zone) {
		"${zone.graph.name}_${formatStateName(zone)}"
	}
	
	private String formatEventName(SensorNode sensor, zone, isSensorActivation) {
		String prefix = "${sensor.graph.name}_" + (isSensorActivation ? "to_" : "from_")
		def zones = (isSensorActivation ? incomingZones(sensor) : outgoingZones(sensor)) 
		String suffix = zones.size() > 1 ? "_${zones.indexOf(zone)}" : ''
		"$prefix${sensor.name}$suffix"
	}
}