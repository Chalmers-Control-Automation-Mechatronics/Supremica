#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }
proctype B() { ch!b }
proctype C()
{	if
	:: ch?a
	:: ch?b
	fi
}
init { run A(); run B(); run C() }
