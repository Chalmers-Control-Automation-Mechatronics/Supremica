package net.sourceforge.waters.subject.module.builder
import net.sourceforge.waters.subject.module.*

class Application extends Scope {
	boolean deferred = true
	static final pattern = /(?i)application/
	static final defaultAttr = 'name'
	List inputs = []
	List outputs = []
	List variables = []
	List functionBlocks = []
	MainProgram mainProgram
	Process process
	
	private addDefaultProcessModel(ModuleBuilder mb) {
		mb.event(inputs.collect{it.formatEventName()}, controllable:false)
		mb.eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:inputs.collect{it.formatEventName()})
		mb.plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
			state('q0', marked:true) {
				inputs.each{input -> mb.selfLoop(event:input.formatEventName()){mb.action(new Expression("${input.name} := not ${input.name}").toSupremicaSyntax(this))}}
			}
		}
	}

	def addToModule(ModuleSubject module) {
		ModuleBuilder mb = new ModuleBuilder()
		mb.module(module) {
			[*inputs, *outputs, *variables].grep{it}.each { variable ->
				mb.booleanVariable(variable.name.toSupremicaSyntax(this), initial:variable.value, marked:variable.value ? true : false)
			}
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			booleanVariable(Converter.END_OF_SCANCYCLE_VARIABLE_NAME, marked:true)
			if (process) {
				event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
				eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.PROCESS_SCAN_CYCLE_EVENT_NAME])
			} else {
				addDefaultProcessModel(mb)
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) 
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME) { mb.set(Converter.END_OF_SCANCYCLE_VARIABLE_NAME) }
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME]) {
						reset(Converter.END_OF_SCANCYCLE_VARIABLE_NAME)
					}
				}
			}
			mainProgram?.addToModule(mb, this)
			process?.addToModule(mb)
		}
	}
	def toAutomata() {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax(this)))
	}
}