chan ch = [1] of {byte};

proctype A(){
 ch!1;
 ch!2;

}

proctype B(){
L1:ch?2;
L2:ch?1;
if
 ::ch?1 -> goto L1;
 ::ch?2 -> goto L2;
fi;
}

init{
 run A();
 run B()
}
