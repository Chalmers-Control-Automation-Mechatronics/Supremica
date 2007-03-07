package org.supremica.external.sag.automaton;

import net.sourceforge.waters.subject.module.builder.*
import org.supremica.external.sag.*
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
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.*;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

class AutomatonGenerator {

	final static AutomatonGenerator instance = new AutomatonGenerator()
	
	private AutomatonGenerator() {
	}
	
	static void main(args) {
		SagBuilder sagBuilder = new SagBuilder();
		
		sagBuilder.project(name:"testproject") {
			controlSignal(name:'u1')
			controlSignal(name:'u2')
			controlSignal(name:'u3', synthesize:false)
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
				onewayZone(back:'y1', front:'y2', initialNrOfObjects:2, capacity:2, ordered:true, frontEntryCondition:'u2 & TordsZone')
			}
			graph(name:'object3', maxNrOfObjects:1) {
				sensor(name:'y5', initiallyActivated:true)
				sensor(name:'y6')
				sensor(name:'y7')
				twowayZone(front:'y5', outsideSystemBoundry:true)
				twowayZone(name:'TordsZone', back:'y5', front:'y6', capacity:1, forwardCondition:'u1', backwardCondition:'!u1', frontExitCondition:'y2', backEntryCondition:'!y2')
				twowayZone(back:'y6', front:'y7', bounded:false, forwardCondition:'u1 & !y1')
				onewayZone(back:'y7', outsideSystemBoundry:true)
			}
			graph(name:'tank', maxNrOfObjects:1) {
				sensor(name:'low')
				sensor(name:'high')
				onewayZone(front:'low', initialNrOfObjects:1)
				onewayZone(back:'low', front:'high', overlapped:true, backExitCondition:'false')
			}
			graph(name:'overlappingObject', nrOfObjectsIsUnbounded:true) {
				sensor(name:'y8')
				sensor(name:'y9')
				onewayZone(front:'y8', outsideSystemBoundry:true)
				twowayZone(back:'y8', front:'y9', overlapped:true)
				onewayZone(back:'y9', outsideSystemBoundry:true)
			}
		}
		ModuleProxy generatedModule = instance.generate(sagBuilder.project)
		
		def moduleBuilder = new ModuleBuilder()
		
		ModuleProxy manualModule = moduleBuilder.module(name:'testproject') {
			booleanVariable(name:['y1', 'y2', 'y3', 'y4'], marked:false)
			booleanVariable(name:'y5', initial:true, marked:true)
			booleanVariable(name:'y6', marked:false)
			booleanVariable(name:'y7', marked:false)
			booleanVariable(name:'low', marked:false)
			booleanVariable(name:'high', marked:false)
			booleanVariable(name:'y8', marked:false)
			booleanVariable(name:'y9', marked:false)
			booleanVariable(name:'u1', marked:false)
			booleanVariable(name:'u2', marked:false)
			booleanVariable(name:'u3', marked:false)
			integerVariable(name:'object1_between_y1_y2', range:0..3, initial:0, marked:0)
			integerVariable(name:'object1_beside_y2_0', range:0..1, initial:0, marked:0)
			integerVariable(name:'object1_beside_y2_1', range:0..2, initial:0, marked:0)
			integerVariable(name:'object1_between_y2_y3', range:0..2, initial:0, marked:0)
			integerVariable(name:'object2_between_y1_y2', range:0..2, initial:2, marked:2)
			integerVariable(name:'overlappingObject_both_y8_y9', range:0..1, initial:0, marked:0)
			booleanVariable(name:'TordsZone', marked:false)
			booleanVariable(name:'object3_between_y6_y7', marked:false)
			booleanVariable(name:'tank_beside_low', initial:true, marked:true)
			booleanVariable(name:'tank_both_low_high', marked:false)
			booleanVariable(name:'endOfScanCycle', initial:false, marked:true)
			event(['object1_from_y1', 'object1_to_y1_0', 'object1_to_y1_1', 'object1_from_y2_0',
			       'object1_from_y2_1', 'object1_from_y2_2', 'object1_from_y2_3', 'object1_from_y2_4',
			       'object1_to_y2_0', 'object1_to_y2_1', 'object1_to_y2_2', 'object1_to_y2_3',
			       'object1_from_y3', 'object1_to_y3'], controllable:false)
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
			event(name:'tank_from_low', controllable:false)
			event(name:'tank_to_low', controllable:false)
			event(name:'tank_to_high', controllable:false)
			event(name:'overlappingObject_from_y8', controllable:false)
			event(name:'overlappingObject_to_y8_0', controllable:false)
			event(name:'overlappingObject_to_y8_1', controllable:false)
			event(name:'overlappingObject_from_y9_0', controllable:false)
			event(name:'overlappingObject_from_y9_1', controllable:false)
			event(name:'overlappingObject_to_y9', controllable:false)
			event(name:'doSignalChange', controllable:true)
			event(name:'skipSignalChange', controllable:true)
			eventAlias(name:'sensorEvent', events:['object1_from_y1', 'object1_to_y1_0', 'object1_to_y1_1', 'object1_from_y2_0', 'object1_from_y2_1',
			                                        'object1_from_y2_2', 'object1_from_y2_3', 'object1_from_y2_4', 'object1_to_y2_0', 'object1_to_y2_1',
			                                        'object1_to_y2_2', 'object1_to_y2_3', 'object1_from_y3', 'object1_to_y3', 'object2_from_y1',
			                                        'object2_to_y2', 'object3_from_y5_0', 'object3_from_y5_1', 'object3_to_y5_0', 'object3_to_y5_1',
			                                        'object3_from_y6_0', 'object3_from_y6_1', 'object3_to_y6_0', 'object3_to_y6_1', 'object3_from_y7_0',
			                                        'object3_from_y7_1', 'object3_to_y7', 'tank_from_low', 'tank_to_low', 'tank_to_high', 'overlappingObject_from_y8',
			                                        'overlappingObject_to_y8_0', 'overlappingObject_to_y8_1', 'overlappingObject_from_y9_0',
			                                        'overlappingObject_from_y9_1', 'overlappingObject_to_y9'])
			plant(name:'object1_y1', initialState:'false') {
				state(name:'false', marked:true)
				state(name:'true', marked:true)
				transition(from:'false', to:'true', events:['object1_to_y1_0'], guard:'!y1') {action 'y1 = 1'}
				transition(from:'false', to:'true', events:['object1_to_y1_1'], guard:'!y1 & object1_between_y1_y2 > 0') {
					action 'y1 = 1'
					action 'object1_between_y1_y2 -= 1'
				}
				transition(from:'true', to:'false', events:['object1_from_y1'], guard:'object1_between_y1_y2 < 3 & (u1 | u2) & y4') {
					action 'y1 = 0'
					action 'object1_between_y1_y2 += 1'
				}
			}
			plant(name:'object1_y2', initialState:'false') {
				state(name:'false', marked:true)
				state(name:'true', marked:true)
				transition(from:'false', to:'true', events:['object1_to_y2_0'], guard:'!y2 & object1_between_y1_y2 > 0 & (u1 | u2)') {
					action 'y2 = 1'
					action 'object1_between_y1_y2 -= 1'
				}
				transition(from:'false', to:'true', events:['object1_to_y2_1'], guard:'!y2') {action 'y2 = 1'}
				transition(from:'false', to:'true', events:['object1_to_y2_2'], guard:'!y2 & object1_beside_y2_0') {
					action 'y2 = 1'
					action 'object1_beside_y2_0 = 0'
				}
				transition(from:'false', to:'true', events:['object1_to_y2_3'], guard:'!y2 & object1_beside_y2_1 > 0') {
					action 'y2 = 1'
					action 'object1_beside_y2_1 -= 1'
				}
				transition(from:'true', to:'false', events:['object1_from_y2_0'], guard:'object1_between_y1_y2 < 3') {
					action 'y2 = 0'
					action 'object1_between_y1_y2 += 1'
				}
				transition(from:'true', to:'false', events:['object1_from_y2_1']) {action 'y2 = 0'}
				transition(from:'true', to:'false', events:['object1_from_y2_2'], guard:'!object1_beside_y2_0') {
					action 'y2 = 0'
					action 'object1_beside_y2_0 = 1'
				}
				transition(from:'true', to:'false', events:['object1_from_y2_3'], guard:'object1_beside_y2_1 < 2') {
					action 'y2 = 0'
					action 'object1_beside_y2_1 += 1'
				}
				transition(from:'true', to:'false', events:['object1_from_y2_4'], guard:'object1_between_y2_y3 < 2') {
					action 'y2 = 0'
					action 'object1_between_y2_y3 += 1'
				}
			}
			plant(name:'object1_y3', initialState:'false') {
				state(name:'false', marked:true)
				state(name:'true', marked:true)
				transition(from:'false', to:'true', events:['object1_to_y3'], guard:'!y3 & object1_between_y2_y3 > 0') {
					action('y3 = 1')
					action('object1_between_y2_y3 -= 1')
				}
				transition(from:'true', to:'false', events:['object1_from_y3']) {action 'y3 = 0'}
			}
			plant(name:'overlappingObject_y8', initialState:'false') {
				state(name:'false', marked:true)
				state(name:'true', marked:true)
				transition(from:'false', to:'true', events:['overlappingObject_to_y8_0'], guard:'!y8') {
					action 'y8 = 1'
				}
				transition(from:'false', to:'true', events:['overlappingObject_from_y9_0'], guard:'overlappingObject_both_y8_y9') {
					action 'y9 = 0'
					action 'overlappingObject_both_y8_y9 = 0'
				}
				transition(from:'true', to:'false', events:['overlappingObject_to_y9'], guard:'!y9') {
					action 'y9 = 1'
					action 'overlappingObject_both_y8_y9 = 1'
				}
			}
			plant(name:'overlappingObject_y9', initialState:'false') {
				state(name:'false', marked:true)
				state(name:'true', marked:true)
				transition(from:'false', to:'true', events:['overlappingObject_from_y8'], guard:'overlappingObject_both_y8_y9') {
					action 'y8 = 0'
					action 'overlappingObject_both_y8_y9 = 0'
				}
				transition(from:'true', to:'false', events:['overlappingObject_to_y8_1'], guard:'!y8') {
					action 'y8 = 1'
					action 'overlappingObject_both_y8_y9 = 1'
				}
				transition(from:'true', to:'false', events:['overlappingObject_from_y9_1']) {
					action 'y9 = 0'
				}
			}
			plant(name:'object3', initialState:'y5') {
				state(name:'y5', marked:true)
				state(name:'y6', marked:true)
				state(name:'y7', marked:true)
				state(name:'TordsZone', marked:true)
				state(name:'between_y6_y7', marked:true)
				state(name:'outside', marked:true)
				transition(from:'outside', to:'y5', events:['object3_to_y5_0']) {action('y5 = 1')}
				transition(from:'TordsZone', to:'y5', events:['object3_to_y5_1'], guard:'!u1 & !y2') {
					action('y5 = 1')
					action('TordsZone = 0')
				}
				transition(from:'y5', to:'outside', events:['object3_from_y5_0']) {action('y5 = 0')}
				transition(from:'y5', to:'TordsZone', events:['object3_from_y5_1'], guard:'u1') {
					action('y5 = 0')
					action('TordsZone = 1')
				}
				transition(from:'TordsZone', to:'y6', events:['object3_to_y6_0'], guard:'u1') {
					action('y6 = 1')
					action('TordsZone = 0')
				}
				transition(from:'between_y6_y7', to:'y6', events:['object3_to_y6_1']) {
					action('y6 = 1')
					action('object3_between_y6_y7 = 0')
				}
				transition(from:'y6', to:'TordsZone', events:['object3_from_y6_0'], guard:'!u1 & y2') {
					action('y6 = 0')
					action('TordsZone = 1')
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
			plant(name:'tank', initialState:'beside_low') {
				state(name:'low', marked:true)
				state(name:'high', marked:true)
				state(name:'beside_low', marked:true)
				state(name:'both_low_high', marked:true)
				transition(from:'beside_low', to:'low', events:['tank_to_low']){
					action 'low = 1'
					action 'tank_beside_low = 0'
				}
				transition(from:'low', to:'both_low_high', events:['tank_to_high']){
					action 'high = 1'
					action 'tank_both_low_high = 1'
				}
				transition(from:'both_low_high', to:'high', events:['tank_from_low'], guard:'false'){
					action 'low = 0'
					action 'tank_both_low_high = 0'
				}
			}
			foreach(name:'i', range:0..1) {
				plant(name:'object2[i]', initialState:'between_y1_y2') {
					state(name:'y1', marked:true)
					state(name:'y2', marked:true)
					state(name:'y4', marked:true)
					state(name:'between_y1_y2', marked:true)
					transition(from:'y1', to:'between_y1_y2', events:['object2_from_y1[i]'], guard:'object2_between_y1_y2 < 2') {
						action 'y1 = 0'
						action 'object2_between_y1_y2 += 1'
					}
					transition(from:'between_y1_y2', to:'y2', events:['object2_to_y2[i]'], guard:'!y2 & (u2 & TordsZone)') {
						action 'y2 = 1'
						action 'object2_between_y1_y2 -= 1'
					}
				}
			}
			plant(name:'object2_between_y1_y2_queue', initialState:'0_1') {
				state(name:'empty')
				state(name:'0')
				state(name:'1')
				state(name:'0_1')
				state(name:'1_0')
				transition(from:'empty', to:'0', events:['object2_from_y1[0]'])
				transition(from:'0', to:'empty', events:['object2_to_y2[0]'])
				transition(from:'1', to:'1_0', events:['object2_from_y1[0]'])
				transition(from:'0_1', to:'1', events:['object2_to_y2[0]'])
				transition(from:'empty', to:'1', events:['object2_from_y1[1]'])
				transition(from:'1', to:'empty', events:['object2_to_y2[1]'])
				transition(from:'0', to:'0_1', events:['object2_from_y1[1]'])
				transition(from:'1_0', to:'0', events:['object2_to_y2[1]'])
			}
			plant(name:'ControlUnit', initialState:'u1') {
				state(name:'u1', marked:false)
				state(name:'u2', marked:false)
				state(name:'endOfScanCycle', marked:true)
				transition(from:'u1', to:'u2', events:['doSignalChange']) { action 'u1 = !u1' }
				transition(from:'u1', to:'u2', events:['skipSignalChange'])
				transition(from:'u2', to:'endOfScanCycle', events:['doSignalChange']) {
					action 'u2 = !u2'
					action 'endOfScanCycle = 1'
				}
				transition(from:'u2', to:'endOfScanCycle', events:['skipSignalChange']) {
					action 'endOfScanCycle = 1'
				}
				transition(from:'endOfScanCycle', to:'u1', events:['sensorEvent']) {
					action 'endOfScanCycle = 0'
				}
			}
		}
				
		Util.assertGeneratedModuleEqualsManual(generatedModule, manualModule)
		ModuleCompiler compiler = new ModuleCompiler(new DocumentManager(), ProductDESElementFactory.getInstance(), generatedModule);
        assert compiler.compile()
 
        assert false, 'kolla simantic net'
		assert false, 'skissa på artikel'
		assert false, 'fixa guards med zoner'
		assert false, 'fixa exceptions'
		assert false, 'fixa resursbokning'
		assert false, 'fixa delade resurser mellan grafer'
		assert false, 'fixa range och konstanter'
	}

	ModuleProxy generate(Project project) {
		ModuleBuilder builder = new ModuleBuilder()
		
		Closure addStates = { graph ->
			(graph.sensor + graph.zone.findAll{!it.outsideSystemBoundry}).eachWithIndex { location, i ->
				builder.state(name:formatStateName(location), marked:true)
			}
			if (graph.zone.any{it.outsideSystemBoundry}) {
				builder.state(name:OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME, marked:true)
			}
		}
		Closure addTransitions  = { sensor ->
			[true, false].each {movementFromZone ->
				(movementFromZone ? incomingZones(sensor) : outgoingZones(sensor)).each { zone ->
					def sensorCondition = movementFromZone ? "!${sensor.name}" : null
					def otherSensor = (zone.front == sensor) ? zone.back : zone.front
					def sourceStateName
					def targetStateName
					if (sensor.graph.nrOfObjectsIsUnbounded) {
						sourceStateName = "${!movementFromZone}"
						targetStateName = "${movementFromZone}"
					} else {
						sourceStateName = formatStateName(!movementFromZone ? sensor : zone)
						targetStateName = formatStateName(movementFromZone ? sensor : zone)
					}
					builder.transition(from:sourceStateName,
		                               to:targetStateName,
	                                   events:[!zone.overlapped ? formatEventName(sensor, zone, movementFromZone, INDEX_NAME) : formatEventName(otherSensor, zone, !movementFromZone, INDEX_NAME)],
	                                   guard:!zone.overlapped ? formatGuard(sensor, zone, movementFromZone) : formatGuard(otherSensor, zone, !movementFromZone)) {
						if (!zone.overlapped) action "${sensor.name} = ${movementFromZone ? 1 : 0}"
						else action "${otherSensor.name} = ${!movementFromZone ? 1 : 0}"
						if (!zone.outsideSystemBoundry) {
							if (zone.capacity > 1) action "${formatZoneVariableName(zone)} ${movementFromZone ? '-' : '+'}= 1"
							else action "${formatZoneVariableName(zone)} = ${movementFromZone ? 0 : 1}"
						}
					}
				}
			}
		}
		Closure addPlant = { graph ->
			builder.plant(name:formatPlantName(graph), initialState:formatStateName(findInitialState(graph))) {
				addStates(graph)
				graph.sensor.each { sensor ->
					addTransitions(sensor)
				}
			}
		}
		
		builder.module(name:project.name) {
			(project.sensorSignal).each { signal -> 
				builder.booleanVariable(name:signal.name, initial:signal.sensor.any{findInitialState(it.graph) == it}, marked:signal.sensor.any{it.initiallyActivated})
			}
			(project.controlSignal).each { signal -> 
				builder.booleanVariable(name:signal.name, initial:false, marked:false)
			}
			project.graph.zone.findAll{it.bounded && it.graph.maxNrOfObjects != 1}.each { zone ->
				builder.integerVariable(name:formatZoneVariableName(zone),
				                        range:0..zone.capacity,
				                        initial:findInitialState(zone.graph) == zone ? (zone.initialNrOfObjects ? zone.initialNrOfObjects : zone.graph.maxNrOfObjects) : 0,
				                        marked:findInitialState(zone.graph) == zone ? (zone.initialNrOfObjects ? zone.initialNrOfObjects : zone.graph.maxNrOfObjects) : 0)
			}
			project.graph.zone.findAll{it.graph.maxNrOfObjects == 1 && !it.outsideSystemBoundry}.each { zone ->
				builder.booleanVariable(name:formatZoneVariableName(zone), initial:findInitialState(zone.graph) == zone, marked:findInitialState(zone.graph) == zone)
			}
			if (project.controlSignal.any{it.synthesize}) {
				booleanVariable(name:END_OF_SCANCYCLE_VARIABLE_NAME, initial:false, marked:true)
			}
			def sensorEvents = []
			project.graph.sensor.each {sensor ->
				[false, true].each { incoming ->
					(incoming ? incomingZones(sensor) : outgoingZones(sensor)).each { zone ->
						builder.event(name:formatEventName(sensor, zone, incoming),
					                  controllable:false, ranges:sensor.graph.maxNrOfObjects > 1 ? [0..sensor.graph.maxNrOfObjects-1] : null)
						sensorEvents << formatEventName(sensor, zone, incoming)
					}
				}
			}
			if (project.controlSignal.any{it.synthesize}) {
				event(name:DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME, controllable:true)
				event(name:SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME, controllable:true)
				eventAlias(name:SENSOR_EVENTS_ALIAS_NAME, events:sensorEvents)
				project.graph.findAll{it.nrOfObjectsIsUnbounded}.sensor.each { sensor ->
					builder.plant(name:formatPlantName(sensor), initialState:"${sensor.initiallyActivated}") {
						builder.state(name:'false', marked:true)
						builder.state(name:'true', marked:true)
						addTransitions(sensor)
					}
				}
			}
			project.graph.findAll{it.maxNrOfObjects == 1}.each { graph ->
				addPlant(graph)
			}
			project.graph.findAll{it.maxNrOfObjects > 1}.each { graph ->
				builder.foreach(name:INDEX_NAME, range:0..graph.maxNrOfObjects - 1) {
					addPlant(graph)
				}
			}
			project.graph.findAll{it.maxNrOfObjects > 1}.zone.findAll{it.ordered && !it.overlapped && (it.capacity > 1 || !it.bounded) && !it.outsideSystemBoundry}.each { zone ->
				builder.plant(name:"${formatZoneVariableName(zone)}_queue", initialState:formatQueueStateName(0..<zone.initialNrOfObjects)) {
					def maxNrOfObjects = zone.bounded ? Math.min(zone.graph.maxNrOfObjects, zone.capacity) : zone.graph.maxNrOfObjects 
					(0..maxNrOfObjects).each { nrOfObjectsInZone ->
						Permutator.getPermutationsOf(0..<zone.graph.maxNrOfObjects, nrOfObjectsInZone).each { permutation ->
							builder.state(name:formatQueueStateName(permutation))
						}
					}
					(0..<zone.graph.maxNrOfObjects).each { object ->
						(0..<maxNrOfObjects).each { nrOfObjectsInZone ->
							Permutator.getPermutationsOf((0..<zone.graph.maxNrOfObjects) - object, nrOfObjectsInZone).each { objectsInZone ->
								if (zone in outgoingZones(zone.back)) transition(from:formatQueueStateName(objectsInZone), to:formatQueueStateName([*objectsInZone, object]), events:[formatEventName(zone.back, zone, false, object)])
								if (zone in outgoingZones(zone.front)) transition(from:formatQueueStateName(objectsInZone), to:formatQueueStateName([object, *objectsInZone]), events:[formatEventName(zone.front, zone, false, object)])
								if (zone in incomingZones(zone.back)) transition(from:formatQueueStateName([*objectsInZone, object]), to:formatQueueStateName(objectsInZone), events:[formatEventName(zone.back, zone, true, object)])
								if (zone in incomingZones(zone.front)) transition(from:formatQueueStateName([object, *objectsInZone]), to:formatQueueStateName(objectsInZone), events:[formatEventName(zone.front, zone, true, object)])
							}
						}
					}
				}
			}
			if (project.controlSignal.any{it.synthesize}) {
				def controlSignalsToBeSynthesized = project.controlSignal.findAll{it.synthesize}
				plant(name:'ControlUnit', initialState:controlSignalsToBeSynthesized[0].name) {
					controlSignalsToBeSynthesized.each { signal ->
						builder.state(name:"${signal.name}", marked:false)
					}
					state(name:END_OF_SCANCYCLE_STATE_NAME, marked:true)
					(0..<controlSignalsToBeSynthesized.size()-1).each { i -> 
						builder.transition(from:controlSignalsToBeSynthesized[i].name,
								           to:controlSignalsToBeSynthesized[i+1].name, events:[DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME]) {
							action "${controlSignalsToBeSynthesized[i].name} = !${controlSignalsToBeSynthesized[i].name}"
						}
						builder.transition(from:controlSignalsToBeSynthesized[i].name,
								           to:controlSignalsToBeSynthesized[i+1].name, events:[SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME])
					}
					transition(from:controlSignalsToBeSynthesized[-1].name,
					           to:END_OF_SCANCYCLE_STATE_NAME, events:[DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME]) {
						action "${controlSignalsToBeSynthesized[-1].name} = !${controlSignalsToBeSynthesized[-1].name}"
						action "${END_OF_SCANCYCLE_VARIABLE_NAME} = 1"
					}
					transition(from:controlSignalsToBeSynthesized[-1].name,
					           to:END_OF_SCANCYCLE_STATE_NAME, events:[SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME]) {
						action "${END_OF_SCANCYCLE_VARIABLE_NAME} = 1"
					}
					transition(from:END_OF_SCANCYCLE_STATE_NAME, to:controlSignalsToBeSynthesized[0].name, events:[SENSOR_EVENTS_ALIAS_NAME]) {
						action "${END_OF_SCANCYCLE_VARIABLE_NAME} = 0"
					}
				}
			}
		}
	}
	
	final static OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME = 'outside'
	final static INDEX_NAME = 'i'
	final static END_OF_SCANCYCLE_VARIABLE_NAME = 'endOfScanCycle'
	final static END_OF_SCANCYCLE_STATE_NAME = 'endOfScanCycle'
	final static DO_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'doSignalChange'
	final static SKIP_CONTROL_SIGNAL_CHANGE_EVENT_NAME = 'skipSignalChange'
	final static SENSOR_EVENTS_ALIAS_NAME = 'sensorEvent'
	final static QUEUE_STATE_NAME_SEPARATOR = '_'						
	final static EMPTY_QUEUE_STATE_NAME = 'empty'
	
	private findInitialState(graph) {
		(graph.sensor.findAll{it.initiallyActivated} +
				graph.zone.findAll{it.initialNrOfObjects} +
				graph.zone.findAll{it.outsideSystemBoundry} + 
				graph.sensor.findAll{graph.maxNrOfObjects == 1} +
				graph.zone.findAll{it.capacity >= graph.maxNrOfObjects || !it.bounded})[0]
	}

	private formatPlantName(Sensor sensor) {
		"${sensor.graph.name}_${sensor.name}"
	}
	
	private formatPlantName(Graph graph) {
		graph.name + ((graph.maxNrOfObjects > 1) ? "[${INDEX_NAME}]" : '')
	}
	
	private formatStateName(Sensor sensor) {
		sensor.name
	}
	
	private formatStateName(Zone zone) {
		if (zone.outsideSystemBoundry) return OUTSIDE_SYSTEM_BOUNDRY_STATE_NAME
		if (zone.name?.trim()) return zone.name
		def sensorsOfZone = [zone.back, zone.front].grep(Sensor.class)
		def zonesWithSameSensors = zone.graph.zone.findAll{it.bounded && [it.front, it.back].grep(Sensor.class) == sensorsOfZone}
		def prefix = zone.overlapped ? 'both' : 'between'
		if (sensorsOfZone.size() == 1) prefix = 'beside'
		def suffix = zonesWithSameSensors.size() > 1 ? "${zonesWithSameSensors.indexOf(zone)}" : null
		([prefix] + sensorsOfZone.name + [suffix]).findAll{it}.join('_')
	}
	
	private formatQueueStateName(objects) {
		objects ? objects.join(QUEUE_STATE_NAME_SEPARATOR) : EMPTY_QUEUE_STATE_NAME
	}
	
	private outgoingZones(sensor) {
		sensor instanceof Sensor ? sensor?.graph.zone.grep(sensor.outgoing + sensor.incoming.findAll{zone -> !zone.oneway}) : null
	}
	
	private incomingZones(sensor) {
		sensor instanceof Sensor ? sensor?.graph.zone.grep(sensor.outgoing.findAll{zone -> !zone.oneway} + sensor.incoming) : null
	}
	
	private String formatZoneVariableName(zone) {
		zone.name?.trim() ? zone.name : "${zone.graph.name}_${formatStateName(zone)}"
	}
	
	private String formatEventName(Sensor sensor, zone, movementFromZone, index) {
		formatEventName(sensor, zone, movementFromZone) + (sensor.graph.maxNrOfObjects > 1 ? "[${index}]" : '')
	}
	
	private String formatEventName(Sensor sensor, zone, movementFromZone) {
		String prefix = "${sensor.graph.name}_" + (movementFromZone ? "to_" : "from_")
		def zones = (movementFromZone ? incomingZones(sensor) : outgoingZones(sensor)) 
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
		def comparison = ''
		if (zone.capacity > 1) comparison = isSensorActivation ? ' > 0' : " < ${zone.capacity}"
		def negation = ''
		if (zone.capacity == 1) negation = isSensorActivation^zone.overlapped ? '' : '!'
		def zoneCondition = zone.bounded && zone.graph.maxNrOfObjects != 1 && (!isSensorActivation || zone.graph.nrOfObjectsIsUnbounded && !zone.overlapped) ?
		                        "${negation}${formatZoneVariableName(zone)}$comparison" : null
		def sensorCondition = isSensorActivation && sensor.graph.maxNrOfObjects != 1 ? "!${sensor.name}" : null
		def directionCondition = (isSensorActivation && zone.front == sensor) || (!isSensorActivation && zone.back == sensor) ? zone.forwardCondition : zone.backwardCondition;
        if (directionCondition && parenthesisNeeded(directionCondition)) directionCondition = '(' + directionCondition + ')'
		def entryExitCondition
        if (isSensorActivation && zone.front == sensor) entryExitCondition = zone.frontEntryCondition
        else if (isSensorActivation && zone.back == sensor) entryExitCondition = zone.backEntryCondition
        else if (!isSensorActivation && zone.front == sensor) entryExitCondition = zone.frontExitCondition
        else if (!isSensorActivation && zone.back == sensor) entryExitCondition = zone.backExitCondition
        if (entryExitCondition && parenthesisNeeded(entryExitCondition)) entryExitCondition = '(' + entryExitCondition + ')'
        [sensorCondition, zoneCondition, directionCondition, entryExitCondition].findAll{it}.join(' & ')
	}
	
	ModuleProxy generateAndSaveToFile(Project sagProject, String filename) {
		ModuleProxy module = generate(sagProject)
		JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
		marshaller.marshal(module, new File(filename));
		module
	}
	
}
