/*                                     
 * A test for the Promela importer in Waters.
 * In this model, two different processes send different messages on the same
 * channel, so there is no need to generate different message exchange events
 * for the different senders.
 */

chan ch = [1] of { byte };

proctype A()
{
        ch!1;
}

proctype B()
{
        ch!2;
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
        run C();
}
