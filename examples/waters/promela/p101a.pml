#define msgtype 33

chan name = [0] of { byte, byte };

proctype A()
{
	name!msgtype(124);
	name!msgtype(121)
}

proctype B()
{	
  hidden byte state;
  name?msgtype(state)
}

init
{
  run A();
  run B()
}
