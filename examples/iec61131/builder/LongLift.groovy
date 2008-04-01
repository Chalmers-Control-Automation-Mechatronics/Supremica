import org.supremica.external.iec61131.builder.*
import net.sourceforge.waters.subject.module.builder.*

ccb = new ControlCodeBuilder()

boolean verify(FunctionBlock app) {
	if (app.verify()) println "$app.name is OK"
	else println "$app.name is FAULTY"
}

/* 
	The long lift (Kulbanehissen), somewhat simplified
	* There is only one ball sensor (on the lift ball seat) insead of three
	* Sensor signals iToMiddle and iToUp indicates where the ball should be sent
	
	Verification
	* Verify reachability of the initial state (no balls, control signals are false, lift is down)
	* Verify that middle pusher is inactive when lift is up
	* Verify that middle pusher is inactive when lift is above middle
*/

app = ccb.application('LongLift') {
	Input('iLiftDown', markedValue:true)
	Input 'iLiftMiddle'
	Input 'iLiftUp'
	Input 'iBallInLift'
	Input 'iToMiddle'
	Input 'iToUp'
	Output 'qToMiddle'
	Output 'qToUp'
	Output 'qMiddleOut'
	Output 'qUpOut'
	Output 'forbidden_middleOutWhenUp'
	Output 'forbidden_middleOutWhenAboveMiddle'
	LogicProgram('main') {
		RS(Q:'qToUp', S:'iBallInLift and iLiftDown and iToUp and not qToMiddle', R:'not iBallInLift')	// (1)
		RS(Q:'qToMiddle', S:'iBallInLift and iLiftDown and iToMiddle and not qToUp', R:'not iBallInLift')	// (2)
		'qMiddleOut := iBallInLift and iLiftMiddle and not qToUp'()
//		'qMiddleOut := iBallInLift and iLiftUp'() //Incorrect, copy'n paste error
		'qUpOut := iBallInLift and iLiftUp'() //Correct
		
		S(Q:'forbidden_middleOutWhenUp', in:'qMiddleOut and iLiftUp')
		S(Q:'forbidden_middleOutWhenAboveMiddle', in:'qMiddleOut and Lift.main.middle_up.X')
	}
	Process('Lift') {
		SequentialProgram('main') {
			Sequence('lift') {
				Step('down') { N 'iLiftDown' }
				'qToUp or qToMiddle'(to:'down_middle')
				Step('down_middle')
				'qToUp or qToMiddle'(to:'middle')
				'not qToUp and not qToMiddle'(to:'down')
				Step('middle') {  N 'iLiftMiddle' }
				'qToUp'(to:'middle_up')
				'not qToUp and not qToMiddle'(to:'down_middle')
				Step('middle_up')
				'qToUp'(to:'up')
				'not qToUp'(to:'middle')
				Step('up') { N 'iLiftUp' }
				'not qToUp'(to:'middle_up')
			}
		}
	}
	Process('Ball') {
		LogicProgram('main') {
			SR(Q:'iBallInLift', S:'iLiftDown', R:'(iLiftMiddle and qMiddleOut) or (iLiftUp and qUpOut)')
		}
	}
}

verify(app)

//Util.openInSupremica(app.toAutomata())
