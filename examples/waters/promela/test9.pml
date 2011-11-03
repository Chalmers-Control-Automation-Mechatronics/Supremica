chan ch = [1] of {byte};

proctype A(){
 ch!1;
 ch!2;
}

proctype B(){
do
 ::ch?1;
 ::ch?2;
od;
}

init{
 run A();
 run B()
}
