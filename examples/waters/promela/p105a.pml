/*                                     
 * A test for the Promela importer in Waters.
 * This model creates two instances of the process type A, such that different
 * message exchange events need to be created for different senders.
 */

#define msgtype 33

chan name = [0] of { byte, byte };

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
        run A();
        run B()
}
