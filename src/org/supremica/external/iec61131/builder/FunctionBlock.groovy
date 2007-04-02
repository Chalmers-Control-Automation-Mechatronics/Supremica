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
	
	def toAutomata(boolean forSynthesis = false) {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax()), forSynthesis)
	}
	
	ModuleSubject addToModule(ModuleSubject module, boolean forSynthesis = false) {
		ModuleBuilder mb = new ModuleBuilder()
		Scope scope = [self:this, process:this]
		mb.module(module) {
			List assignments = getRuntimeAssignments(scope)
			List freeVariables = allControllableVariables.findAll{var -> assignments.every{it.Q.fullyQualified(it.scope) != var}}
			if (forSynthesis) assert freeVariables //Must exist free variables for synthesis
			else assert !freeVariables //Must not exist any free variables if not synthesis
			//Add default process models for those inputs that never are assigned values
			inputs.findAll{input -> assignments.every{it.Q != input.name}}.each { input ->
				ControlCodeBuilder ccb = new ControlCodeBuilder()
				def inputDefaultProcess = ccb.process("Process_${input.name}") {
					ccb.logicProgram('program') {
						ccb."${input.name} := not ${input.name}"()
					}
				}
				processes << inputDefaultProcess
				assignments += inputDefaultProcess.getRuntimeAssignments(scope)
			}
			Map assignmentsForEachProcess = RuntimeAssignment.separateBasedOnProcess(assignments)
			if (forSynthesis) {
				event(Converter.START_SCAN_EVENT_NAME, controllable:true)
				mb.plant("ScanCycle", defaultEvent:Converter.START_SCAN_EVENT_NAME) {
					state('start', marked:true) {
						outgoing(to:'main', event:Converter.START_SCAN_EVENT_NAME)
					}
					state('main', marked:true) {
						outgoing(to:'start', event:Converter.SCAN_CYCLE_EVENT_NAME)
					}
				}
				freeVariables.each{ variable ->
					def variableName = variable.toSupremicaSyntax()
					mb.booleanVariable(variableName, markedValue:true)
					mb.plant("ASSIGN_${variable.toSupremicaSyntax()}", defaultEvent:Converter.START_SCAN_EVENT_NAME, deterministic:false) {
						state('q0', marked:true) {
							selfLoop() { set(variable.toSupremicaSyntax()) }
							selfLoop() { reset(variable.toSupremicaSyntax()) }
						}
					}
				}
			}
			assignmentsForEachProcess.each { processScope, assForProc ->
				Map stateless = RuntimeAssignment.substituteIntoStateless(assForProc)
				Map withoutUnnecessary = RuntimeAssignment.removeUnnecessary(this, stateless)
				new TreeMap(withoutUnnecessary).each {Q, input -> 
					new RuntimeAssignment(scope:processScope, Q:Q, input:input).addToModule(mb)
				}
				mb.event(processScope.eventName, controllable:false)
				assignmentsForEachProcess.keySet().findAll{processScope.self.speed.value == it.self.speed.value + 1}.each { slowerProc ->
					processScope.self.addPriorityAutomaton(mb, processScope, slowerProc, forSynthesis)
				}
				assignmentsForEachProcess.keySet().any{processScope.self.speed.value == it.self.speed.value + 2}
				List muchSlowerProcesses = assignmentsForEachProcess.keySet().findAll{processScope.self.speed.value == it.self.speed.value + 2}
				processScope.self.addPriorityAutomaton(mb, processScope, muchSlowerProcesses)
			}
		}
	}

	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, Scope scopeOfSlowerProcess, forSynthesis) {
		String start = Converter.START_OF_SCANCYCLE_STATE_NAME
		String main = forSynthesis ? Converter.SCAN_CYCLE_MAIN_STATE_NAME : Converter.END_OF_SCANCYCLE_STATE_NAME
		String end = Converter.END_OF_SCANCYCLE_STATE_NAME
		String startEvent = forSynthesis ? Converter.START_SCAN_EVENT_NAME : Converter.SCAN_CYCLE_EVENT_NAME 
		String mainEvent = Converter.SCAN_CYCLE_EVENT_NAME
		mb.plant("${scope.fullName.toSupremicaSyntax()}_vs_${scopeOfSlowerProcess.fullName.toSupremicaSyntax()}", defaultEvent:startEvent) {
			state(start, marked:true) {
				outgoing(to:main)
			}
			if (forSynthesis) {
				state(main, marked:true) {
					outgoing(to:end, event:mainEvent)
				}
			}
			state(end, marked:true) {
				outgoing(to:start, event:scopeOfSlowerProcess.eventName)
				outgoing(to:main)
			}
		}
	}
	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, List scopesOfMuchSlowerProcesses) {
		if(scopesOfMuchSlowerProcesses) {
			mb.plant("${scope.fullName.toSupremicaSyntax()}_vs_SlowProcesses", defaultEvent:scope.eventName) {
				state(Converter.START_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.END_OF_SCANCYCLE_STATE_NAME)
				}
				state(Converter.END_OF_SCANCYCLE_STATE_NAME, marked:true) {
					outgoing(to:Converter.START_OF_SCANCYCLE_STATE_NAME, events:scopesOfMuchSlowerProcesses.eventName)
					selfLoop()
				}
			}
		}
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, parent)
	}
	List getRuntimeAssignments(Scope parent) {
		[*programs, *processes]*.getRuntimeAssignments(parent).flatten()
	}
	
	List getNamedElements() {
		[this, *types, *inputs, *outputs, *variables, *programs, *processes]
	}
	List getSubScopeElements() {
		[*programs, *processes, *variables]
	}
	List getAllControllableVariables() {
		[*outputs.name, *getControllableVariables([self:this, process:this] as Scope)]
	}
	List getControllableVariables(Scope parent) {
		[variables*.getControllableVariables(parent), programs*.getControllableVariables(parent)].flatten()
	}
}