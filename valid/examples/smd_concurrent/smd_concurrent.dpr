@project smd_concurrent {
@events {
scan {
  @controllable
  @generateCodeFragment
}
errorscan {
  @uncontrollable
  @generateCodeFragment
}
reset {
  @controllable
  @generateCodeFragment
}
recoverturn {
  @controllable
  @generateCodeFragment
}
counterequals12 {
  @uncontrollable
  @generateCodeFragment
}
recovervacuum {
  @controllable
  @generateCodeFragment
}
doneturn {
  @uncontrollable
  @generateCodeFragment
}
counter {
  @controllable
  @generateCodeFragment
}
donevacuum {
  @uncontrollable
  @generateCodeFragment
}
errorvacuum {
  @uncontrollable
  @generateCodeFragment
}
turn {
  @controllable
  @generateCodeFragment
}
vacuum {
  @controllable
  @generateCodeFragment
}
recoverscan {
  @controllable
  @generateCodeFragment
}
errorturn {
  @uncontrollable
  @generateCodeFragment
}
counterlessthan12 {
  @uncontrollable
  @generateCodeFragment
}
donescan {
  @uncontrollable
  @generateCodeFragment
}
}
@use {
  scantask
  scan
  errorscan
  vacuumtask
  counter
  turn
  vacuum
}
@spec scantask {
  @cloning {scantask}
}
@plant scan {
  @cloning {scan}
}
@spec errorscan {
  @cloning {errorscan}
}
@spec vacuumtask {
  @cloning {vacuumtask}
}
@plant counter {
  @cloning {counter}
}
@plant turn {
  @cloning {turn}
}
@plant vacuum {
  @cloning {vacuum}
}
}

