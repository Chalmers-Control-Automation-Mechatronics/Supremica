import org.supremica.external.iec61131.builder.*
import net.sourceforge.waters.subject.module.builder.*

ccb = new ControlCodeBuilder()

boolean verify(FunctionBlock app) {
	if (app.verify()) println "$app.name is OK"
	else println "$app.name is FAULTY"
}

app = ccb.application('LongLift') {
	Input 'i'
	Output 'q'
	LogicProgram('main') {
		'q := i'()
	}
}

//verify(app)

Util.openInSupremica(app.toAutomata())
