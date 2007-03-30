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
	List specifications = []
	final Speed speed = Speed.FAST
	final String eventName = Converter.SCAN_CYCLE_EVENT_NAME
	
	def toAutomata() {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax()))
	}


	ModuleSubject addToModule(ModuleSubject module) {
		ModuleBuilder mb = new ModuleBuilder()
		Scope scope = [self:this, process:this]
		mb.module(module) {

			List assignments = execute(scope)
//			println statements.collect{[it.scope.fullName, it.statement.Q, it.statement.input]}
			
			//Add default process models for those inputs that never are assigned values
			inputs.findAll{input -> assignments.every{it.Q != input.name}}.each { input ->
				ControlCodeBuilder ccb = new ControlCodeBuilder()
				def inputDefaultProcess = ccb.process("Process_${input.name}") {
					ccb.logicProgram('program') {
						ccb."${input.name} := not ${input.name}"()
					}
				}
				processes << inputDefaultProcess
				assignments += inputDefaultProcess.execute(scope)
			}
			Map assignmentsForEachProcess = RuntimeAssignment.separateBasedOnProcess(assignments)
			assignmentsForEachProcess.each { processScope, assForProc ->
				Map stateless = RuntimeAssignment.substituteIntoStateless(assForProc)
				Map withoutUnnecessary = RuntimeAssignment.removeUnnecessary(this, stateless)
				new TreeMap(withoutUnnecessary).each {Q, input -> 
					new RuntimeAssignment(scope:processScope, Q:Q, input:input).addToModule(mb)
				}
				mb.event(processScope.eventName, controllable:false)
				assignmentsForEachProcess.keySet().findAll{processScope.self.speed.value == it.self.speed.value + 1}.each { slowerProc ->
					mb.plant("${processScope.fullName.toSupremicaSyntax()}_vs_${slowerProc.fullName.toSupremicaSyntax()}", defaultEvent:processScope.eventName) {
						state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
							outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME)
						}
						state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
							outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, event:slowerProc.eventName)
							selfLoop()
						}
					}
				}
				assignmentsForEachProcess.keySet().any{processScope.self.speed.value == it.self.speed.value + 2}
				List slowProcesses = assignmentsForEachProcess.keySet().findAll{processScope.self.speed.value == it.self.speed.value + 2}
				if (slowProcesses) {
					plant("${processScope.fullName.toSupremicaSyntax()}_vs_SlowProcesses", defaultEvent:processScope.eventName) {
						state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
							outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME)
						}
						state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
							outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:slowProcesses.eventName)
							selfLoop()
						}
					}
				}
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