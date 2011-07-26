/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can recognise two different channels
 * with different data types.
 */                                                                   

chan msg = [0] of { byte, byte };
chan ack = [0] of { byte };

proctype producer()
{
  msg!1(4);
  ack?1;
}

proctype consumer()
{
  byte data;
  msg?1(data);
  ack!1;
}

init
{
  run producer();
  run consumer()
}
