/*
 * A test for the Promela importer in Waters.
 * This tests whether an init block before proctype declarations
 * can be recognised, and whether automata are produced in the order
 * in which they appear in the Promela file.
 */

#define msgtype 33

chan name = [0] of { byte, byte };

byte state;

init
{       
        run A();
        run B()
}

proctype A()
{       
        name!msgtype(124);
        name!msgtype(121)
}

proctype B()
{       
        name?msgtype(state)
}
