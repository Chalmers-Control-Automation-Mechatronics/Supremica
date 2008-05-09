import org.supremica.external.iec61131.builder.*
import net.sourceforge.waters.subject.module.builder.*

ccb = new ControlCodeBuilder()

boolean verify(FunctionBlock app) {
	if (app.verify()) println "$app.name is OK"
	else println "$app.name is FAULTY"
}
/*
 An example of a system where process models are needed for verification.

The system:
  * A machine M is started with qStart rising and it sets iWorking to false when finished.
  * Occasionally, M detects that it needs calibration before it can finish working
  * The controller calls for a calibration operator with qCalibrate

The verification problem:
  * Verify that the initial state can be reached. (The default verification problem)

Correct program:
  * Correct program must include the call for an operator (statement (2))
  * Incorrect program lacks (2)
  
Verification of incorrect program:
  * Without process model, the verification returns OK
  * With process model Machine, the verification returns OK
  * With process model Machine and Calibration, the verification returns FAULT
  
Verification of correct program
*/

app = ccb.application('MachineThatNeedsCalibration') {
	Input 'iPieceInQueue'
	Output 'qCalibrate'
	//Comm with machine
	Output 'qStart'
	Input 'iWorking'
	
	//Input to machine from somewhere
	Input 'iCalibrationNeeded'
	
	LogicProgram('main') {
		'qStart := iPieceInQueue and not iWorking'()	// (1)
//		'qCalibrate := iCalibrationNeeded'()			// (2)
	}
	Process('Machine') {
		LogicProgram('main') {
			P('startEvent', in:'qStart')
			SR(Q:'iWorking', S:'startEvent.Q', R:'iWorking and not iCalibrationNeeded')
		}
	}
/*	Process('Calibration') {
		LogicProgram('main') {
			S(Q:'iCalibrationNeeded', in:'iWorking')
		}
	}
	Process('Operator') {
		LogicProgram('main') {
			R(Q:'iCalibrationNeeded', in:'qCalibrate')
		}
	}*/
}

verify(app)

//Util.openInSupremica(app.toAutomata())
