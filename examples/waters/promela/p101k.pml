#define msgtype 33

chan name = [0] of { byte, byte };
hidden byte state;
/* byte name; 	typo  - this line shouldn't have been here */

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
