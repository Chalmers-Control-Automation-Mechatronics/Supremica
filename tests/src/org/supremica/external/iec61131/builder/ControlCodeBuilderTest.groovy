package org.supremica.external.iec61131.builder;

import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.builder.ModuleBuilder
import net.sourceforge.waters.subject.module.builder.Util

class ControlCodeBuilderTest extends GroovyTestCase {
	static void main(String[] args) {
		Util.openInSupremica(ASSIGNMENT_APP.toAutomata(true))
//		Util.openInSupremica(SFC_APP_MODULE)
//		Util.openInSupremica(SFC_DEFERRED_APP_MODULE)
//		Util.openInSupremica(STATELESS_CYCLE_APP_MODULE)
//		Util.openInSupremica(FB_APP_MODULE)
	}
	static final CCB = new ControlCodeBuilder()
	static final ASSIGNMENT_APP = CCB.application('assignmentTest') {
		input 'y1'
		input 'y2'
		input 'y3'
		output 'u1 := true'
		output 'u2 := false'
		output 'u3'
		output 'u5'
		output 'u6'
		output 'u7'
		output 'u4'
		variable 'x2'
		variable 'SR1_S'
		variable 'SR1_R'
		variable 'SR1_Q'
		variable 'RS1_Q'
		variable 'RS1_S'
		variable 'RS1_R'
		variable 'Py2_in'
		variable 'Py2_Q'
		variable 'Py2_old'
		logicProgram('program') {
			'u2 := not y1'()
			'u1 := y1 and u2'()
			'SR1_S := y1'()
			'SR1_R := y2 and u2'()
			'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
			'u5 := SR1_Q'()
			'RS1_Q := u6'()
			'RS1_S := y1'()
			'RS1_R := y2 and u2'()
			'RS1_Q := not RS1_R and (RS1_Q or RS1_S)'()
			'u6 := RS1_Q'()
			'Py2_in := y2'()
			'Py2_Q := Py2_in and not Py2_old '()
			'Py2_old := Py2_in'()
			'u4 := Py2_Q and y1'()
			'x2 := True'()
		}
		process('someProcess') {
			logicProgram('program') {
				'SR1_Q := y1'()
				'SR1_S := y2 and u3'()
				'SR1_R := not u3'()
				'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
				'y1 := SR1_Q'()
			}
		}
	}
	//static final ASSIGNMENT_APP_MODULE = ASSIGNMENT_APP.toAutomata()
	void testAssignment() {
		def appStateless = CCB.application('assignmentTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output('u1', value:true, markedValue:true)
			output('u2', value:false, markedValue:false)
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
	//		variable 'x2'
	//		variable 'SR1_S'
	//		variable 'SR1_R'
			variable 'SR1_Q'
	//		variable 'RS1_Q'
	//		variable 'RS1_S'
	//		variable 'RS1_R'
	//		variable 'Py2_in'
	//		variable 'Py2_Q'
			variable 'Py2_old'
			logicProgram('program') {
				//'x2 := True'()
				'u4 := (y2 and not Py2_old) and y1'()
				'Py2_old := y2'()
				//'Py2_Q := y2 and not Py2_old'()
				//'Py2_in := y2'()
				'u6 := not (y2 and not y1) and (u6 or y1)'()
				//'RS1_Q := not (y2 and not y1) and (u6 or y1)'()
				//'RS1_R := y2 and not y1'()
				//'RS1_S := y1'()
				//'RS1_Q := u6'()
				'u5 := not (y2 and not y1) and SR1_Q or y1'()
				'SR1_Q := not (y2 and not y1) and SR1_Q or y1'()
				//'SR1_R := y2 and not y1'()
				//'SR1_S := y1'()
				'u1 := y1 and not y1'()
				'u2 := not y1'()
			}
			process('someProcess') {
				logicProgram('program') {
					//'SR1_Q := y1'()
					//'SR1_S := y2 and u3'()
					//'SR1_R := not u3'()
					//'SR1_Q := not (not u3) and y1 or (y2 and u3)'()
					'y1 := not (not u3) and y1 or (y2 and u3)'()
				}
			}
		}
		ModuleSubject generatedModuleFromStateless = appStateless.toAutomata(true)
		Util.assertGeneratedModuleEqualsManual(ASSIGNMENT_APP.toAutomata(true), generatedModuleFromStateless)
//		Util.openInSupremica(generatedModuleFromAssignmentOnly)
	}
	static final SFC_APP = CCB.application('sfcapp') {
		input 'y1 := true'
		input 'y2 := true'
		output 'u1'
		output 'u2'
		output 'u3'
		output 'u4'
		output 'u5'
		output 'u6'
		sequentialProgram('program') {
			sequence('mySequence') {
				Step ('S1'){
					N('u5')
					P0_Action {
						'u2 := y1'()
					}
				}
				'y1'();    'y2'(to:'S3')
				Step('S2', marked:true){
					S('u4')
					N_action {
						'u1 := y1 or y2'()
					}
				}
				'not y1'(to:'S1')
							Step('S3'){
								P1_action{'u3 := y2'()}
								R('u4')
							}
							'true'()
			}
		}
	}
	//static final SFC_APP_MODULE = SFC_APP.toAutomata()
	
	void testSfc() {
		def appSfcLess = CCB.application('sfcapp') {
			input('y1', value:true)
			input('y2', value:true)
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u4'
			output 'u5'
			output 'u6'
			logicProgram('program') {
				variable 'S1_X'
				variable('S2_X', markedValue:true)
				variable 'S3_X'
				variable("${Converter.NOT_INIT_VARIABLE_NAME}", markedValue:true)
				variable 'mySequence_T1_enabled'
				variable 'mySequence_T2_enabled'
				variable 'mySequence_T3_enabled'
				variable 'mySequence_T4_enabled'
				variable 'S1_activation'
				variable 'S2_activation'
				variable 'S3_activation'
				variable 'S1_deactivation'
				variable 'S2_deactivation'
				variable 'S3_deactivation'
				'mySequence_T1_enabled := S1_X and y1'()
				'mySequence_T2_enabled := S1_X and y2 and not mySequence_T1_enabled'()
				'mySequence_T3_enabled := S2_X and not y1'()
				'mySequence_T4_enabled := S3_X and true'()
				"S1_activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or mySequence_T3_enabled or mySequence_T4_enabled"()
				'S1_deactivation := mySequence_T1_enabled or mySequence_T2_enabled'()
				SR(Q:'S1_X', S:"S1_activation", R:'S1_deactivation')
				SR(Q:'u5', S:'S1_X', R:'S1_deactivation') 
				'u2 := y1'('S1_deactivation')
				'S2_activation := mySequence_T1_enabled'()
				'S2_deactivation := mySequence_T3_enabled'()
				SR(Q:'S2_X', S:'S2_activation', R:'S2_deactivation')
				'u4 := S2_activation or u4'()
				'u1 := y1 or y2'('S2_X')
				'S3_activation := mySequence_T2_enabled'()
				'S3_deactivation := mySequence_T4_enabled'()
				SR(Q:'S3_X', S:'S3_activation', R:'S3_deactivation')
				'u3 := y2'('S3_activation')
				'u4 := not S3_activation and u4'()
				"${Converter.NOT_INIT_VARIABLE_NAME} := true"()
				'u6 := u6'()
			}
		}
		//ModuleSubject sfcModule = sfcApp.toAutomata()
		ModuleSubject sfcLessModule = appSfcLess.toAutomata()
		Util.assertGeneratedModuleEqualsManual(SFC_APP.toAutomata(), sfcLessModule)
		//Util.openInSupremica(sfcModule)
	}
	static final SFC_PARALLEL_APP = CCB.application('sfcParallelApp') {
		input 'y1'
		input 'y2'
		sequentialProgram('program') {
			sequence('mySequence') {
				Step 'S1'
				'y1'(to:['L1', 'R1'])
				Step('L1')
				'not y1'()
				Step 'L2'
				Step 'R1'
				'y2'(from:['L2', 'R1'])
			}
		}
	}
	
	void testSfcParallelism() {
		def appSfcLess = CCB.application('sfcParallelApp') {
			input 'y1'
			input 'y2'
			logicProgram('program') {
				variable('S1_X', markedValue:true)
				variable 'L1_X'
				variable 'L2_X'
				variable 'R1_X'
				variable("${Converter.NOT_INIT_VARIABLE_NAME}", markedValue:true)
				variable 'mySequence_T1_enabled'
				variable 'mySequence_T2_enabled'
				variable 'mySequence_T3_enabled'
				variable 'S1_activation'
				variable 'S1_deactivation'
				variable 'L1_activation'
				variable 'L1_deactivation'
				variable 'L2_activation'
				variable 'L2_deactivation'
				variable 'R1_activation'
				variable 'R1_deactivation'
				'mySequence_T1_enabled := S1_X and y1'()
				'mySequence_T2_enabled := L1_X and not y1'()
				'mySequence_T3_enabled := L2_X and R1_X and y2'()
				"S1_activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or mySequence_T3_enabled"()
				'S1_deactivation := mySequence_T1_enabled'()
				SR(Q:'S1_X', S:"S1_activation", R:'S1_deactivation')
				'L1_activation := mySequence_T1_enabled'()
				'L1_deactivation := mySequence_T2_enabled'()
				SR(Q:'L1_X', S:'L1_activation', R:'L1_deactivation')
				'L2_activation := mySequence_T2_enabled'()
				'L2_deactivation := mySequence_T3_enabled'()
				SR(Q:'L2_X', S:'L2_activation', R:'L2_deactivation')
				'R1_activation := mySequence_T1_enabled'()
				'R1_deactivation := mySequence_T3_enabled'()
				SR(Q:'R1_X', S:'R1_activation', R:'R1_deactivation')
				"${Converter.NOT_INIT_VARIABLE_NAME} := true"()
			}
		}
		//ModuleSubject sfcModule = sfcApp.toAutomata()
		ModuleSubject sfcLessModule = appSfcLess.toAutomata()
		Util.assertGeneratedModuleEqualsManual(SFC_PARALLEL_APP.toAutomata(), sfcLessModule)
		//Util.openInSupremica(sfcModule)
	}
	static final SFC_DEFERRED_APP = CCB.application('testSfcDeferred') {
		sequentialProgram('deferred', deferred:true) {
			sequence('sfcA') {
				Step ('A1')
				'B1.X'()
				Step('A2')
				'true'()
			}
			sequence('sfcB') {
				Step ('B1')
				'A1.X'()
				Step('B2')
				'true'()
			}
		}
	}
	//static final SFC_DEFERRED_APP_MODULE = SFC_DEFERRED_APP.toAutomata()
	void testSfcDeferred() {
		def appSfcLess = CCB.application('testSfcDeferred') {
			logicProgram('deferred') {
				variable('A1_X', markedValue:true)
				variable 'A2_X'
				variable('B1_X', markedValue:true)
				variable 'B2_X'
				variable("${Converter.NOT_INIT_VARIABLE_NAME}", markedValue:true)
				variable 'sfcA_T1_enabled'
				variable 'sfcA_T2_enabled'
				variable 'sfcB_T1_enabled'
				variable 'sfcB_T2_enabled'
				variable 'A1_activation'
				variable 'A2_activation'
				variable 'B1_activation'
				variable 'B2_activation'
				variable 'A1_deactivation'
				variable 'A2_deactivation'
				variable 'B1_deactivation'
				variable 'B2_deactivation'
				'sfcA_T1_enabled := A1_X and B1_X'()
				'sfcA_T2_enabled := A2_X and true'()
				'sfcB_T1_enabled := B1_X and A1_X'()
				'sfcB_T2_enabled := B2_X and true'()
				"A1_activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or sfcA_T2_enabled"()
				'A1_deactivation := sfcA_T1_enabled'()
				SR(Q:'A1_X', S:"A1_activation", R:'A1_deactivation')
				'A2_activation := sfcA_T1_enabled'()
				'A2_deactivation := sfcA_T2_enabled'()
				SR(Q:'A2_X', S:"A2_activation", R:'A2_deactivation')
				"B1_Activation := not ${Converter.NOT_INIT_VARIABLE_NAME} or sfcB_T2_enabled"()
				'B1_deactivation := sfcB_T1_enabled'()
				SR(Q:'B1_X', S:"B1_activation", R:'B1_deactivation')
				'B2_activation := sfcB_T1_enabled'()
				'B2_deactivation := sfcB_T2_enabled'()
				SR(Q:'B2_X', S:"B2_activation", R:'B2_deactivation')
				"${Converter.NOT_INIT_VARIABLE_NAME} := true"()
			}
		}
//		ModuleSubject sfcModule = appSfc.toAutomata()
		ModuleSubject sfcLessModule = appSfcLess.toAutomata()
		Util.assertGeneratedModuleEqualsManual(SFC_DEFERRED_APP.toAutomata(), sfcLessModule)
//		Util.openInSupremica(sfcModule)
	}
	static final STATELESS_CYCLE_APP = CCB.application('testapp2') {
		input 'y1'
		input 'y2'
		input 'y3'
		input 'y4'
		input 'y5'
		output('u3', markedValue:true)
		output 'u7'
		variable 'FB1instance_x' 
		logicProgram('program') {
			'u7 := not y2 and u7 or (FB1instance_x and y3)'()
			'FB1instance_x := not y3 and FB1instance_x or y2'()
			'u3 := true'()
		}
		process('someProcess') {
			logicProgram('program') {
				'y1 := not (not u3) and y1 or (y2 and u3)'()
			}
		}
		process('slowProcess1', speed:Speed.SLOW) {
			logicProgram('program') {
				'y4 := u7'()
			}
		}
		process('slowProcess2', speed:Speed.SLOW) {
			logicProgram('program') {
				'y5 := u7'()
			}
		}
	}
	//static final STATELESS_CYCLE_APP_MODULE = STATELESS_CYCLE_APP.toAutomata()
	void testAutomataGenerator() {
		ModuleSubject correctModule = new ModuleBuilder().module('testapp2') {
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			booleanVariable(['y1', 'y2', 'y3', 'y4', 'y5', 'FB1instance_x', 'u7'], initialValue:false, markedValue:false)
			booleanVariable('u3', initialValue:false, markedValue:true)
			event(['someProcess_change', 'FreeInputs_change', 'slowProcess1_change', 'slowProcess2_change'], controllable:false)
			plant('ControlUnit_vs_someProcess', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:false) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'someProcess_change')
					selfLoop()
				}
			}
			plant('ControlUnit_vs_FreeInputs', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:false) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'FreeInputs_change')
					selfLoop()
				}
			}
			plant('ControlUnit_vs_SlowProcesses', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:false) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, events:['slowProcess1_change', 'slowProcess2_change'])
					selfLoop()
				}
			}
			plant('someProcess_vs_slowProcess1', defaultEvent:'someProcess_change') {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'slowProcess1_change')
					selfLoop()
				}
			}
			plant('someProcess_vs_slowProcess2', defaultEvent:'someProcess_change') {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'slowProcess2_change')
					selfLoop()
				}
			}
			plant('FreeInputs_vs_slowProcess1', defaultEvent:'FreeInputs_change') {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'slowProcess1_change')
					selfLoop()
				}
			}
			plant('FreeInputs_vs_slowProcess2', defaultEvent:'FreeInputs_change') {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'slowProcess2_change')
					selfLoop()
				}
			}
			plant('ASSIGN_u3', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'1') { set('u3') }
					selfLoop(guard:'!1') { reset('u3') }
				}
			}
			plant('ASSIGN_FB1instance_x', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y3 & FB1instance_x | y2') { set('FB1instance_x') }
					selfLoop(guard:'!(!y3 & FB1instance_x | y2)') { reset('FB1instance_x') }
				}
			}
			plant('ASSIGN_u7', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'!y2 & u7 | (FB1instance_x & y3)') { set('u7') }
					selfLoop(guard:'!(!y2 & u7 | (FB1instance_x & y3))') { reset('u7') }
				}
			}
			plant('ASSIGN_y1', defaultEvent:'someProcess_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u3 & y1 | (y2 & u3)') { set('y1') }
					selfLoop(guard:'!(u3 & y1 | (y2 & u3))') { reset('y1') }
				}
			}
			plant('ASSIGN_y2', defaultEvent:'FreeInputs_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop { set('y2') }
					selfLoop { reset('y2') }
				}
			}
			plant('ASSIGN_y3', defaultEvent:'FreeInputs_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop { set('y3') }
					selfLoop { reset('y3') }
				}
			}
			plant('ASSIGN_y4', defaultEvent:'slowProcess1_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u7') { set('y4') }
					selfLoop(guard:'!u7') { reset('y4') }
				}
			}
			plant('ASSIGN_y5', defaultEvent:'slowProcess2_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u7') { set('y5') }
					selfLoop(guard:'!u7') { reset('y5') }
				}
			}
		}
		
		Util.assertGeneratedModuleEqualsManual(STATELESS_CYCLE_APP.toAutomata(), correctModule)
//		Util.openInSupremica(generatedModuleFromStateless)
	}
	static final SYNTHESIS_APP = CCB.application('synthesisapp') {
		input 'y1'
		input 'y2'
		output('u3', markedValue:true)
		output 'u7'
		logicProgram('program') {
			'u7 := u3 and y1 and not y2'()
		}
		process('someProcess') {
			logicProgram('program') {
				'y1 := u3 and y1 or (y2 and u3)'()
			}
		}
		process('slowProcess1', speed:Speed.SLOW) {
			logicProgram('program') {
				'y2 := u7'()
			}
		}
	}
	//static final STATELESS_CYCLE_APP_MODULE = STATELESS_CYCLE_APP.toAutomata()
	void testSynthesisAutomataGenerator() {
		ModuleSubject correctModule = new ModuleBuilder().module('synthesisapp') {
			event(Converter.SCAN_CYCLE_EVENT_NAME, controllable:false)
			event(Converter.START_SCAN_EVENT_NAME, controllable:true)
			booleanVariable(['y1', 'y2', 'u7'], initialValue:false, markedValue:false)
			booleanVariable('u3', initialValue:false, markedValue:true)
			event(['someProcess_change', 'slowProcess1_change'], controllable:false)
			plant('ScanCycle') {
				state('start', marked:true) {
					outgoing(to:'main', event:Converter.START_SCAN_EVENT_NAME)
				}
				state('main', marked:false) {
					outgoing(to:'start', event:Converter.SCAN_CYCLE_EVENT_NAME)
				}
			}
			plant('ControlUnit_vs_someProcess', defaultEvent:Converter.START_SCAN_EVENT_NAME) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:false) {
					outgoing(to:Converter.SCAN_CYCLE_MAIN_STATE_NAME)
				}
				state(Converter.SCAN_CYCLE_MAIN_STATE_NAME, marked:false) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'someProcess_change')
					outgoing(to:Converter.SCAN_CYCLE_MAIN_STATE_NAME)
				}
			}
			plant('ControlUnit_vs_SlowProcesses', defaultEvent:Converter.START_SCAN_EVENT_NAME) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:false) {
					outgoing(to:Converter.SCAN_CYCLE_MAIN_STATE_NAME)
				}
				state(Converter.SCAN_CYCLE_MAIN_STATE_NAME, marked:false) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, event:Converter.SCAN_CYCLE_EVENT_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, events:['slowProcess1_change'])
					outgoing(to:Converter.SCAN_CYCLE_MAIN_STATE_NAME)
				}
			}
			plant('someProcess_vs_slowProcess1', defaultEvent:'someProcess_change') {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:'slowProcess1_change')
					selfLoop()
				}
			}
			plant('ASSIGN_u3', defaultEvent:Converter.START_SCAN_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop() { set 'u3' }
					selfLoop() { reset 'u3' }
				}
			}
			plant('ASSIGN_u7', defaultEvent:Converter.SCAN_CYCLE_EVENT_NAME, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u3 & y1 & !y2') { set 'u7' }
					selfLoop(guard:'!(u3 & y1 & !y2)') { reset 'u7' }
				}
			}
			plant('ASSIGN_y1', defaultEvent:'someProcess_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u3 & y1 | (y2 & u3)') { set 'y1' }
					selfLoop(guard:'!(u3 & y1 | (y2 & u3))') { reset 'y1' }
				}
			}
			plant('ASSIGN_y2', defaultEvent:'slowProcess1_change', deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:'u7') { set('y2') }
					selfLoop(guard:'!u7') { reset('y2') }
				}
			}
		}
		Util.assertGeneratedModuleEqualsManual(SYNTHESIS_APP.toAutomata(true), correctModule)
//		Util.openInSupremica(generatedModuleFromStateless)
	}
	static final FB_APP = CCB.application('functionBlockTest') {
		input 'y1'
		input 'y2'
		input 'y3'
		output 'u1'
		output 'u2'
		output 'u3'
		output 'u5'
		output 'u6'
		output 'u7'
		output 'u4'
		output 'u8'
		RS 'RS1'
		functionblock('FB1') {
			input 'y1'
			input 'y2'
			output 'u1'
			variable 'x'
			logicProgram('program') {
				SR(Q:'u1', S:'x AND y2', R:'y1')
				SR(Q:'x', S:'y1', R:'y2')
			}
		}
		logicProgram('program') {
			'u2 := not y1'()
			SR('SR1', S:'y1', R:'y2 and u2')
			'u5 := SR1.Q'()
			RS1(S:'y1', R:'y2 and u2', Q:'u6')
			R_TRIG('Py2', in:'y2')
			'u4 := Py2.Q and y1'()
			FB1('FB1instance', y1:'y2', y2:'y3', u1:'u7')
			'u8 := y1 or y2'('y3')
		}
		process('someProcess') {
			variable 'x'
			logicProgram('program') {
				SR(Q:'y1', S:'y2 and u3 or x', R:'not u3')
				'x := not u2'()
			}
		}
		process('process3') {
			functionblock('ProcessFb') {
				input 'in'
				output 'q'
				logicProgram('program') {
					'q := not in'()
				}
			}
			logicProgram('program') {
				ProcessFb(q:'y3', in:'u2')
			}
		}
	}
	//static final FB_APP_MODULE = FB_APP.toAutomata()
	void testFunctionBlocks() {
		def appAssignmentOnly = CCB.application('functionBlockTest') {
			input 'y1'
			input 'y2'
			input 'y3'
			output 'u1'
			output 'u2'
			output 'u3'
			output 'u5'
			output 'u6'
			output 'u7'
			output 'u4'
			output 'u8'
			logicProgram('program') {
				variable 'FB1instance_u1'
				variable 'FB1instance_y1'
				variable 'FB1instance_y2'
				variable 'FB1instance_program_SR1_Q'
				variable 'FB1instance_program_SR1_S'
				variable 'FB1instance_program_SR1_R'
				variable 'FB1instance_program_SR2_Q'
				variable 'FB1instance_program_SR2_S'
				variable 'FB1instance_program_SR2_R'
				variable 'FB1instance_x'
				variable 'SR1_S'
				variable 'SR1_R'
				variable 'SR1_Q'
				variable 'RS1_Q'
				variable 'RS1_S'
				variable 'RS1_R'
				variable 'Py2_in'
				variable 'Py2_Q'
				variable 'Py2_old'
				'u2 := not y1'()
				'SR1_S := y1'()
				'SR1_R := y2 and u2'()
				'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
				'u5 := SR1_Q'()
				'RS1_Q := u6'()
				'RS1_S := y1'()
				'RS1_R := y2 and u2'()
				'RS1_Q := not RS1_R and (RS1_Q or RS1_S)'()
				'u6 := RS1_Q'()
				'Py2_in := y2'()
				'Py2_Q := Py2_in and not Py2_old '()
				'Py2_old := Py2_in'()
				'u4 := Py2_Q and y1'()
				'FB1instance_u1 := u7'()
				'FB1instance_y1 := y2'()
				'FB1instance_y2 := y3'()
				'FB1instance_program_SR1_Q := FB1instance_u1'()
				'FB1instance_program_SR1_S := FB1instance_x and FB1instance_y2'()
				'FB1instance_program_SR1_R := FB1instance_y1'()
				'FB1instance_program_SR1_Q := not FB1instance_program_SR1_R and FB1instance_program_SR1_Q or FB1instance_program_SR1_S'()
				'FB1instance_u1 := FB1instance_program_SR1_Q'()
				'FB1instance_program_SR2_Q := FB1instance_x'()
				'FB1instance_program_SR2_S := FB1instance_y1'()
				'FB1instance_program_SR2_R := FB1instance_y2'()
				'FB1instance_program_SR2_Q := not FB1instance_program_SR2_R and FB1instance_program_SR2_Q or FB1instance_program_SR2_S'()
				'FB1instance_x := FB1instance_program_SR2_Q'()
				'u7 := FB1instance_u1'()
				'u8 := (y3) and (y1 or y2) or (not (y3) and u8)'()
			}
			process('someProcess') {
				variable 'x'
				logicProgram('program') {
					variable 'SR1_Q'
					variable 'SR1_S'
					variable 'SR1_R'
					'SR1_Q := y1'()
					'SR1_S := y2 and u3 or x'()
					'SR1_R := not u3'()
					'SR1_Q := not SR1_R and SR1_Q or SR1_S'()
					'y1 := SR1_Q'()
					'x := not u2'()
				}
			}
			process('process3') {
				logicProgram('program') {
					'y3 := not u2'()
				}
			}
		}
		ModuleSubject generatedModuleFromAssignmentOnly = appAssignmentOnly.toAutomata(true)
		Util.assertGeneratedModuleEqualsManual(FB_APP.toAutomata(true), generatedModuleFromAssignmentOnly)
//		Util.openInSupremica(generatedModule)
	}
}