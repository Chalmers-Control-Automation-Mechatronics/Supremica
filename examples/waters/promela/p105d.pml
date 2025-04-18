/*                                     
 * A test for the Promela importer in Waters.
 * In this model, two different processes send on the same channel, so
 * different message exchange events need to be created for different senders.
 */

chan ch = [0] of { byte };

proctype A()
{
        ch!1;
}

proctype B()
{
        ch!1;
}

proctype C()
{
        byte val;
        ch?val;
}

init
{
        run A();
        run B();
        run C()
}
