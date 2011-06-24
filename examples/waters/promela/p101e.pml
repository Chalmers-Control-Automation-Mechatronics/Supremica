/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether skip statements are recognised.                 
 */                                                                   

#define msgtype 33

chan name = [0] of { byte, byte };

proctype A()
{
	name!msgtype(124);
	skip;
	name!msgtype(121)
}
proctype B()
{
	byte state;
	name?msgtype(state);
	skip;
}
init
{
	skip;
	run A();
        run B()
}
