chan ch = [1] of {byte};

proctype A(){
 ch!1;
 ch!2;
}

proctype B(){
if
 ::ch?1;
 ::ch?2;
fi;
}

init{
 run A();
 run B()
}
