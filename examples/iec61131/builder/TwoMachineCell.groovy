import org.supremica.external.iec61131.builder.*
import net.sourceforge.waters.subject.module.builder.*

ccb = new ControlCodeBuilder()

boolean verify(FunctionBlock app) {
	if (app.verify()) println "$app.name is ok"
	else println "$app.name is faulty"
}

app = ccb.application('TwoMachineCell') {
	Input 'iProdInM1Queue'
	Input 'iProdInM2Queue'
	//M1
	Output 'qM1Start'
	Input 'iM1Working'
	//M2
	Output 'qM2Start'
	Input 'iM2Working'
	
	LogicProgram('main') {
		RS(Q:'qM1Start', S:'iProdInM1Queue and not iM1Working', R:'iM1Working')
		RS(Q:'qM2Start', S:'iProdInM2Queue and not iM2Working', R:'iM2Working')
	}
	Process('M1') {
		variable 'done'
		LogicProgram('main') {
			P('start', in:'qM1Start')
			SR(Q:'iM1Working', S:'start.Q', R:'done')
		}
	}
}

verify(app)

//Util.openInSupremica(app.toAutomata())
