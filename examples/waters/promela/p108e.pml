/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether the compiler can distinguish mtype and byte arguments
 * in the same channel.
 */

mtype = { DATA, FV }

chan ch = [0] of { mtype, byte };

proctype A()
{
  do
  :: ch!DATA(0)
  :: ch!DATA(1)
  :: ch!FV(0) ; break
  od
}

proctype B()
{
  mtype data;
  byte value;
  ch?data(value);
}

init
{
  run A();
  run B()
}
