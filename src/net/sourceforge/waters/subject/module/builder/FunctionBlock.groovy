package net.sourceforge.waters.subject.module.builder
import net.sourceforge.waters.subject.module.*

class FunctionBlock extends Named {
	static final pattern = /(?i)application|functionblock/
	static final defaultAttr = 'name'
	static final parentAttr = null
	String namePattern
	String getNamePattern() {
		namePattern ? /(?i)$namePattern/ : /(?i)${name.text}/
	}
	List inputs = []
	List outputs = []
	List variables = []
	MainProgram mainProgram
	List processes = []
	                     
	private addDefaultProcessModel(ModuleBuilder mb, Scope scope) {
		mb.event(inputs.collect{it.formatEventName(scope)}, controllable:false)
		mb.eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:inputs.collect{it.formatEventName(scope)})
		mb.plant(Converter.DEFAULT_PROCESS_PLANT_NAME) {
			state('q0', marked:true) {
				inputs.each{input -> mb.selfLoop(event:input.formatEventName(scope)){mb.action("${input.name} = !${input.name}")}}
			}
		}
	}

	def toAutomata() {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax([self:this] as Scope)))
	}


	ModuleSubject addToModule(ModuleSubject module) {
		ModuleBuilder mb = new ModuleBuilder()
		Scope scope = [self:this]
		mb.module(module) {
			//inputs.grep{it}.each { variable ->
			//	mb.booleanVariable(variable.name.toSupremicaSyntax(scope), initial:variable.value, marked:variable.value ? true : false)
			//}
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			if (processes) {
				//event(Converter.PROCESS_SCAN_CYCLE_EVENT_NAME, controllable:false)
				//eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.PROCESS_SCAN_CYCLE_EVENT_NAME])
			} else {
				addDefaultProcessModel(mb, scope)
			}
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) 
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME)
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME])
				}
			}
			List statements = execute(scope)
			(0..<statements.size()).each { i ->
				statements[i].statement.addToModule(mb, statements, i)
			}
		}
	}

	List execute(Scope parent) {
		List statements = mainProgram.execute(parent)
		processes*.execute(parent).each { statements += it }
		statements
	}
	
	List getNamedElements() {
		return [this, *inputs, *outputs, *variables, *mainProgram.statements, *processes]
	}
	List getSubScopeElements() {
		return [*mainProgram.statements, *processes]
	}
}