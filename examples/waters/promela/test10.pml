chan ch = [1] of {byte};

proctype A(){
   ch!1;
   ch!2;
}

proctype B(){
endX: ch?2;
do
 ::ch?1;break;
 ::ch?2;
 ::break;
od;
}

init{
 run A();
 run B()
}
