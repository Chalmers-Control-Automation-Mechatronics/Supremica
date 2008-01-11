package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.builder.ModuleBuilder
import net.sourceforge.waters.subject.module.builder.Util

class FunctionBlock {
	static final pattern = /(?i)application|functionblock/
	static final defaultAttr = 'name'
	static final parentAttr = 'types'
	Identifier name
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
	
	boolean verify() {
		Util.verifyNonblocking(toAutomata(false))	
	}
	
	def toAutomata(boolean forSynthesis = false) {
		addToModule(new ModuleBuilder().module(name.toSupremicaSyntax()), forSynthesis)
	}
	
	ModuleSubject addToModule(ModuleSubject module, boolean forSynthesis = false) {
		ModuleBuilder mb = new ModuleBuilder()
		Scope scope = new Scope(self:this, process:this)
		mb.module(module) {
			List assignments = getRuntimeAssignments(scope)
			List freeVariables = allControllableVariables.findAll{var -> assignments.every{it.Q.fullyQualified(it.scope) != var}}
			if (forSynthesis) assert freeVariables //Must exist free variables for synthesis
			//else assert !freeVariables //Must not exist any free variables if not synthesis
			//Add default process models for those inputs that never are assigned values
			List freeInputs = inputs.findAll{input -> assignments.every{it.Q != input.name}}
			if (freeInputs) {
				ControlCodeBuilder ccb = new ControlCodeBuilder()
				def processForFreeInputs = ccb.process("FreeInputs") {
					ccb.logicProgram('main') {
						freeInputs.each { input ->
							ccb.assignment(Q:input.name, input:null)
						}
					}
				}
				processes << processForFreeInputs
				assignments += processForFreeInputs.getRuntimeAssignments(scope)
			}
			if (forSynthesis) {
				event(Converter.START_SCAN_EVENT_NAME, controllable:true)
				mb.plant("ScanCycle", defaultEvent:Converter.START_SCAN_EVENT_NAME) {
					state('start', marked:true) {
						outgoing(to:'main', event:Converter.START_SCAN_EVENT_NAME)
					}
					state('main', marked:false) {
						outgoing(to:'start', event:Converter.SCAN_CYCLE_EVENT_NAME)
					}
				}
				freeVariables.each{ variableName ->
					def suprVariableName = variableName.toSupremicaSyntax()
					Variable variable = scope.namedElement(variableName)
					boolean markedValue
					if (variable.markedValue != null) markedValue = variable.markedValue
					else if (variable.value != null)  markedValue = variable.value
					else markedValue = false
					mb.booleanVariable(suprVariableName, markedValue:markedValue)
					mb.plant("ASSIGN_${suprVariableName}", defaultEvent:Converter.START_SCAN_EVENT_NAME, deterministic:false) {
						state('q0', marked:true) {
							selfLoop() { set(suprVariableName) }
							selfLoop() { reset(suprVariableName) }
						}
					}
				}
			} else {
				def ccb = new ControlCodeBuilder()
				freeVariables.each { variableName -> 
					assignments += ccb."$variableName := $variableName"().getRuntimeAssignments(scope)	
				}
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
					processScope.self.addPriorityAutomaton(mb, processScope, slowerProc, forSynthesis)
				}
				assignmentsForEachProcess.keySet().any{processScope.self.speed.value == it.self.speed.value + 2}
				List muchSlowerProcesses = assignmentsForEachProcess.keySet().findAll{processScope.self.speed.value == it.self.speed.value + 2}
				processScope.self.addPriorityAutomaton(mb, processScope, muchSlowerProcesses, forSynthesis)
			}
		}
	}

	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, Scope scopeOfSlowerProcess, forSynthesis) {
		addPriorityAutomaton(mb, scope, [scopeOfSlowerProcess.eventName], scopeOfSlowerProcess.fullName.toSupremicaSyntax(), forSynthesis)
	}
	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, List events, nameOfSlow, forSynthesis) {
		String start = Converter.AFTER_SLOWER_PROCESS_STATE_NAME
		String main = forSynthesis ? Converter.SCAN_CYCLE_MAIN_STATE_NAME : Converter.BEFORE_SLOWER_PROCESS_STATE_NAME
		String end = Converter.BEFORE_SLOWER_PROCESS_STATE_NAME
		String startEvent = forSynthesis ? Converter.START_SCAN_EVENT_NAME : Converter.SCAN_CYCLE_EVENT_NAME 
		String mainEvent = Converter.SCAN_CYCLE_EVENT_NAME
		mb.plant("${scope.fullName.toSupremicaSyntax()}_vs_${nameOfSlow}", defaultEvent:startEvent) {
			state(start, marked:false) {
				outgoing(to:main)
			}
			if (forSynthesis) {
				state(main, marked:false) {
					outgoing(to:end, event:mainEvent)
				}
			}
			state(end, marked:true) {
				outgoing(to:start, events:events)
				outgoing(to:main)
			}
		}
	}
	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, List scopesOfMuchSlowerProcesses, forSynthesis) {
		if(scopesOfMuchSlowerProcesses) {
			addPriorityAutomaton(mb, scope, scopesOfMuchSlowerProcesses.eventName, 'SlowProcesses', forSynthesis)
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