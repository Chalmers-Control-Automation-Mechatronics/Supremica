#define msgtype 33

chan name = [0] of { byte, byte };
hidden byte state;
/* byte test; */
/* this checks that the variable test is not created, as it is in a comment */

proctype A()
{	name!msgtype(124);
	name!msgtype(121)
}
proctype B()
{	
	name?msgtype(state)
}
init
{	run A();
        run B()
}
