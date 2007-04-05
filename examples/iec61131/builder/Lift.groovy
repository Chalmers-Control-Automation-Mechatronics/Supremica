import org.supremica.external.iec61131.builder.*
import net.sourceforge.waters.subject.module.builder.*

ccb = new ControlCodeBuilder()

boolean verify(FunctionBlock app) {
	if (app.verify()) println "$app.name is ok"
	else println "$app.name is faulty"
}

//The lift in the ball process with one simplification,
//the lift has a ball sensor on it.
//
app = ccb.application('CorrectLift') {
	input 'iLiftDown := true'
	input 'iLiftUp'
	input 'iBallInLift'
	output 'qUpLift'
	output 'qOutFromLift'
	output 'forbidden_LiftUpAndOutAtTheSameTime' //spec variable
	output 'forbidden_LiftUpWithoutBall' //spec variable
	output 'forbidden_LiftDownWithBall' //spec variable
	LogicProgram('main') {
		'qUpLift := iBallInLift'()
		'qOutFromLift := iBallInLift and iLiftUp'()
		//Specfications (invariants)
		S(Q:'forbidden_LiftUpAndOutAtTheSameTime',
		  in:'qOutFromLift and qUpLift and iBallInLift and not iLiftUp')
		S(Q:'forbidden_LiftUpWithoutBall',
		  in:'qUpLift and not iBallInLift')
		S(Q:'forbidden_LiftDownWithBall',
		  in:'qUpLift and not iBallInLift')
	}
}

//verify(app)
Util.openInSupremica(app.toAutomata())

app = ccb.application('LiftWhereUpAndOutCylindersCrash') {
	input 'iLiftDown := true'
	input 'iLiftUp'
	input 'iBallInLift'
	output 'qUpLift'
	output 'qOutFromLift'
	output 'forbidden_LiftUpAndOutAtTheSameTime' //spec variable
	output 'forbidden_LiftUpWithoutBall' //spec variable
	output 'forbidden_LiftDownWithBall' //spec variable
	LogicProgram('main') {
		'qUpLift := iBallInLift'()
		'qOutFromLift := iBallInLift'()
		//Specfications (invariants)
		S(Q:'forbidden_LiftUpAndOutAtTheSameTime',
		  in:'qOutFromLift and qUpLift and iBallInLift and not iLiftUp')
		S(Q:'forbidden_LiftUpWithoutBall',
		  in:'qUpLift and not iBallInLift')
		S(Q:'forbidden_LiftDownWithBall',
		  in:'qUpLift and not iBallInLift')
	}
}

//verify(app)
//Util.openInSupremica(app.toAutomata())

app = ccb.application('LiftForSynthesis') {
	input 'iLiftDown := true'
	input 'iLiftUp'
	input 'iBallInLift'
	output 'qUpLift' //Synthesize
	output 'qOutFromLift'
	output 'forbidden_LiftUpAndOutAtTheSameTime' //spec variable
	output 'forbidden_LiftUpWithoutBall' //spec variable
	output 'forbidden_LiftDownWithBall' //spec variable
	LogicProgram('main') {
		'qOutFromLift := iBallInLift and iLiftUp'()
		//Specfications (invariants)
		S(Q:'forbidden_LiftUpAndOutAtTheSameTime',
		  in:'qOutFromLift and qUpLift and iBallInLift and not iLiftUp')
		S(Q:'forbidden_LiftUpWithoutBall',
		  in:'qUpLift and not iBallInLift')
		S(Q:'forbidden_LiftDownWithBall',
		  in:'qUpLift and not iBallInLift')
	}
	process('Lift') {
		LogicProgram('main') {
			SR(Q:'iLiftDown', S:'not iLiftUp and not qUpLift', R:'not iLiftUp and qUpLift')
			SR(Q:'iLiftUp', S:'not iLiftDown and qUpLift', R:'not iLiftDown and not qUpLift')
		}
	}
	process('Ball') {
		LogicProgram('main') {
			SR(Q:'iBallInLift', S:'iLiftDown', R:'iLiftUp and qOutFromLift')
		}
	}
}

//Util.openInSupremica(app.toAutomata(true))
