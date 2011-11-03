/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether the compiler can distinguish channels with
 * different types.
 */

mtype = { ACK, NACK };

chan ch = [1] of { byte };
chan ack = [1] of { mtype };

proctype A()
{
  do
  :: ch!1;
     if
     :: ack?ACK;
     :: ack?NACK -> break
     fi    
  od
}

proctype B()
{
end:
  do
  :: ch?1 -> ack!ACK
  :: ch?2 -> ack!NACK
  od
}

init
{
  run A();
  run A();
  run B()
}
