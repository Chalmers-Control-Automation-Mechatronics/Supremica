package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class FunctionBlock {
	static final pattern = /(?i)application|functionblock/
	static final defaultAttr = 'name'
	static final parentAttr = 'types'
	IdentifierExpression name
	String namePattern
	String getNamePattern() {
		namePattern ? /(?i)$namePattern/ : /(?i)${name.text}/
	}
	List inputs = []
	List outputs = []
	List variables = []
	List programs = []
	List processes = []
	List types = []

	def toAutomata() {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax([self:this] as Scope)))
	}


	ModuleSubject addToModule(ModuleSubject module) {
		ModuleBuilder mb = new ModuleBuilder()
		Scope scope = [self:this]
		mb.module(module) {
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(Converter.NO_PROCESS_CHANGE_EVENT_NAME, controllable:false)
			eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[Converter.NO_PROCESS_CHANGE_EVENT_NAME])
			plant(Converter.CONTROL_UNIT_PLANT_NAME) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) 
				transition(event:Converter.SCAN_CYCLE_EVENT_NAME)
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:[Converter.PROCESS_EVENTS_ALIAS_NAME])
				}
			}
			List statements = execute(scope)
//			println statements.collect{[it.scope.fullName, it.statement.Q, it.statement.input]}
			
			//Add default process models for those inputs that never are assigned values
			inputs.findAll{input -> statements.statement.every{it.Q != input.name}}.each { input ->
				ControlCodeBuilder ccb = new ControlCodeBuilder()
				def inputDefaultProcess = ccb.process("Process_${input.name}") {
					ccb.logicProgram('program') {
						ccb."${input.name} := not ${input.name}"()
					}
				}
				processes << inputDefaultProcess
				statements += inputDefaultProcess.execute(scope)
			}
			addProcessEvents(mb, scope)
			(0..<statements.size()).each { i ->
				statements[i].statement.addToModule(mb, statements, i)
			}
		}
	}

	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, parent)
	}
	List execute(Scope parent) {
		List statements = []
		programs.each{ statements += it.execute(parent) }
		processes.each { statements += it.execute(parent) }
		statements
	}
	
	List getNamedElements() {
		[this, *types, *inputs, *outputs, *variables, *programs, *processes]
	}
	List getSubScopeElements() {
		[*programs, *processes, *variables]
	}
}