#define msgtype 33

chan name = [0] of { byte, byte };

/* byte name; 	typo  - this line shouldn't have been here */

proctype A()
{	name!msgtype(124);
	name!msgtype(121);
	name!msgtype(124)
}
proctype B()
{	byte state;
	name?msgtype(state)
}
init
{	run A(); run B()
}
