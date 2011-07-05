/*                                     
 * A test for the Promela importer in Waters.
 * This model creates two instances of the process type B, such that different
 * message receive events need to be created for different recipients.
 */

#define msgtype 33

chan name = [1] of { byte, byte };

proctype A()
{
        name!msgtype(124);
        name!msgtype(121)
}

proctype B()
{
        byte state;
        name?msgtype(state)
}

init
{
        run A();
        run B();
        run B()
}
