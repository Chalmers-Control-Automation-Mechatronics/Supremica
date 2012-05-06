#define msgtype 33

chan name = [0] of { byte, byte };

proctype A()
{
	name!msgtype(124);
	name!msgtype(121)
}
proctype B()
{
	byte state = 60;
	name?msgtype(state)
}
init
{
	atomic { run A(); run B() }
}
