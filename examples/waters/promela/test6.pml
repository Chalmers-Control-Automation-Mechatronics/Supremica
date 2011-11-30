chan ch = [0] of {byte};

proctype A()
{
 ch!1;
 ch!2;

}

proctype B()
{
 ch?1;
 ch?2;
}

init{
 run A();
 run A();
 run B();
 run B()
}
