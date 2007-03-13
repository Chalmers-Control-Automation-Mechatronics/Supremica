package net.sourceforge.waters.subject.module.builder;

class Scope extends Named {
	List children = []
	Map aliases = [:]
	Scope parentScope
	Scope getScope() {
		return this 
	}
	void setScope(Scope scope){}
	String getFullName() {
		parentScope?.parentScope ? "${parentScope.fullName}${Converter.SEPARATOR}$name" : name
	}
	Named child(IdentifierExpression expr) {
//		println '---------'
//		println this
//		println this.children.name
//		println expr
//		println '========='
		if (!expr) return null
		if (expr == this.name) return this
		def obj
		def subScope = expr.leftMostPart() ? child(new IdentifierExpression(expr.leftMostPart())) : null
		if (subScope) obj = subScope.child(new IdentifierExpression(expr.exceptLeftMostPart()))
		else {
			obj = children.find{it.name == expr}
			if (!obj) obj = parentScope?.child(expr)
		}
		obj
	}
}