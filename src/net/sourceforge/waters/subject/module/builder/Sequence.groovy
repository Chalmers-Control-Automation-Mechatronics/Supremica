package net.sourceforge.waters.subject.module.builder;

class Sequence extends Scope {
	static final pattern = /(?i)sequence/
	static final defaultAttr = 'name'
	static final parentAttr = 'sequences'
	List steps = []
	List transitions = []
	def addToModule(ModuleBuilder mb, String scanEvent) {
		steps.each { step ->
			mb.booleanVariable("${step.name.toSupremicaSyntax(this)}_X", initial:step == steps[0], marked:step == steps[0])
		}
		mb.plant(supremicaName, defaultEvent:scanEvent, deterministic:false) {
			steps.each { step ->
				mb.state(step.name.text, marked:true) {
					selfLoop(guard:new Expression("!(${transitions.findAll{it.from == step.name}.guard.text.join(') and !(')})").toSupremicaSyntax(this))
					transitions.findAll{it.from == step.name}.each { outgoing ->
						mb.outgoing(to:outgoing.to.text, guard:outgoing.guard.toSupremicaSyntax(this)) {
							def targetStep = steps.find{it.name==outgoing.to}
							targetStep.resetQualifiers.each{mb.reset(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							[*targetStep.setQualifiers, *targetStep.nonstoredQualifiers].grep{it}.each{mb.set(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							step.nonstoredQualifiers.each{mb.reset(new IdentifierExpression(it.name).toSupremicaSyntax(this))}
							assert step.scope == step
							reset(new IdentifierExpression('X').toSupremicaSyntax(step))
							set(new IdentifierExpression('X').toSupremicaSyntax(targetStep))
						}
					}
				}
			}
		}
	}
}

class Step extends Scope {
	Step() {
		super()
		def x = new Variable()
		x.name = new IdentifierExpression('X')
		x.scope = this
		children << x
	}
	static final pattern = /(?i)step/
	static final defaultAttr = 'name'
	static final parentAttr = 'steps'
	List setQualifiers = []
	List resetQualifiers = []
	List nonstoredQualifiers = []
}

class Transition extends NamedImpl {
	static final pattern = /(?i)tran(?:sition)?/
	static final defaultAttr = 'guard'
	static final parentAttr = 'transitions'
	IdentifierExpression from
	IdentifierExpression to
	Expression guard
}