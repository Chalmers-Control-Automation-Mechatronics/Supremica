/*                                     
 * A test for the Promela importer in Waters.
 * In this model, two different processes receive on the same channel, so
 * different message exchange events need to be created for different
 * recipients.
 */

chan ch = [0] of { byte };

proctype A()
{
        ch!1;
}

proctype B()
{
        byte val;
        ch?val;
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
