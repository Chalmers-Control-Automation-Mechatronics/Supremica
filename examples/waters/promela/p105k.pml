/*
 * A test for the Promela importer in Waters.
 * In this model, two different processes send and receive on the
 * same channel. Messages need to be indexed by senders and receivers,
 * but careful care needs to be taken as to who sends and receives what.
 */

chan ch = [0] of { byte };

proctype A()
{
        ch!0;
        ch!2;
}

proctype B()
{
        ch!2;
        ch!3;
}

proctype C()
{
        byte val;
        ch?val;
}

proctype D()
{
        ch?2;
}

init
{
        run A();
        run B();
        run C();
        run D()
}
