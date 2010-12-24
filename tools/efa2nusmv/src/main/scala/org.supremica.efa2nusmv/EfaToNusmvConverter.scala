package org.supremica.efa2nusmv

import scala.xml.Node 

import java.io.{File, PrintWriter}

import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

  
object EfaToNusmvConverter {
  
  var pw: PrintWriter = new PrintWriter(System.out)
  
  def main(args: Array[String]) {
	
  	if(args.size > 2 || (args.size > 0 && args(0) == "-h")) {
  		println("\nUsege: java -jar efa2smv [inFile [outFile]]")
  	} else {
  	  val inoutFilesOpt = args.size match {
  	    case 2 => Some((new File(args(0)), new File(args(1))))
  	    case 1 => Some((new File(args(0)), new File(args(0) + ".smv")))
  	    case 0 => {
  	                val f = getFile
  	                if(f.isEmpty) 
  	                  None 
  	                else 
  	                  Some((new File(f.get), new File(f.get + ".smv")))
  	              }
  	  }
  	  if(inoutFilesOpt.isDefined){
  		  val (inFile, outFile): (File, File) = inoutFilesOpt.get 
          
          convert(inFile, outFile)

  	  } else {
  		  // do nothing and exit
  	  }
  	}
  }

  def convert(inputFile: File, outputFile: File) {
  	    val module = scala.xml.XML.loadFile(inputFile)
  	    pw = new PrintWriter(outputFile)

        printSmv(module)    
  }
  
  def getFile(): Option[String] = {
      val chooser = new JFileChooser
	    chooser.setFileFilter(new FileNameExtensionFilter(
	        "Supremica Waters modules", "wmod"))
	    val returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	      Some(chooser.getSelectedFile.getPath)
	    else 
	      None
  }


  def printSmv(module: Node) {
    pw.println("MODULE main")
    pw.println("VAR")
    automataVarDecl(module)
    efavarVarDecl(module)
    pw.println("IVAR")
    eventsDecl(module)
    pw.println("INIT")
    inits(module)
    pw.println("DEFINE")
    allTransitions(module)
    pw.println("TRANS")
    automataTransitions(module)
    //pw.println("DEFINE")    
    variableUpdateDesires(module)
    //pw.println("DEFINE")    
    combinationOfDesires(module)
    //pw.println("CTLSPEC AG (EF (event=" + escape(":accepting") + "))")
    printAcceptingReachableSpec(module)
    pw.flush
  }
  

  def automataVarDecl(module: Node): Unit = {
    for(sc <- module \ "ComponentList" \ "SimpleComponent" ) {
      val vd = automatonVarName(sc) + " : {" + 
        interleaveWith(", ", automatonStateNames(sc)) + "};"
      pw.println(vd)
    }
  }

  
  def automatonVarName(sc: Node): String = {
    "q_" + name(sc)
  }
  
  def automatonStateNames(sc: Node): Seq[String] = {
    for(s <- sc \ "Graph" \ "NodeList" \ "SimpleNode") yield 
      name(s)
  }

  def name(node: Node): String = {
    try{
      escape(getAttribute("Name", node))
    } catch {
      case iae: IllegalArgumentException => 
        if(!((node \ "SimpleIdentifier").isEmpty)){
          name((node \ "SimpleIdentifier")(0))
        } else {
          throw new IllegalArgumentException("node " + node.toString +
            " has no attribute Name nor child SimpleIdentifier")
        }
    }
  }
  
  def getAttribute(name:String, node: Node): String = {
    node.attribute(name) match {
      case Some(value) => value.toString
      case _ => throw new IllegalArgumentException("node " + node.toString +
            " has no attribute " + name)        
    }
  }

  
  def interleaveWith(sep:String, s:Seq[String]): String = {
    val s1 = s.filter(!_.isEmpty)
    if(s1.isEmpty) {""}
    else {s1.reduceLeft((x,y) => x + sep + y)}
  }

  
  def efavarVarDecl(module: Node): Unit = {
    for(vc <- module \ "ComponentList" \ "VariableComponent" ) {
      val vd = name(vc) + " : " + efaVarDomain(vc) + " ;"
      pw.println(vd)
    }  
  }

  

  def efaVarDomain(vc: Node): String = {
    val e = vc \ "VariableRange" \ "BinaryExpression"
    e(0).attribute("Operator") match {
      case Some(node) =>
        if (node.toString == "..") {
          val lr = e \ "IntConstant"
          val l = java.lang.Integer.parseInt(getAttribute("Value", lr(0)))
          val r = java.lang.Integer.parseInt(getAttribute("Value", lr(1)))
          l.toString + ".." + r.toString
        } else {
          throw new IllegalArgumentException("Operator should be ..")
        }
      case _ => throw new IllegalArgumentException("BinaryExpression has no Operator")
    }
  }

  

  def eventsDecl(module: Node): Unit = {
    val events = (module \ "EventDeclList" \ "EventDecl") map name 
    pw.println("event : {" + interleaveWith(", ",
      List("_dummy_event_when_all_stay_") ++ events) + "};")
  }

  
  def escape(s:String):String = {
    val rep = s
//     .replace("_", "_underscore_")
     .replace(":" , "_pp_")
     .replace("(" , "_lBr_")
     .replace(")" , "_rBr_")
     .replace("||" , "_par_")
     .replace("|" , "_bar_")
     .replace(" " , "_space_")
     .replace("." , "_dot_")
     .replace("," , "_comma_")
     .replace("'" , "_prime_")
     .replace("=" , "_eq_")
     .replace("!" , "_exclim_")
     .replace("INIT", "_init_")
     if(java.lang.Character.isDigit(rep(0)))
       "d"+rep
     else
       rep
  }
  
  
  def inits(module: Node): Unit = {
    pw.println(
      interleaveWith(" & ",
        (( module \ "ComponentList" \ "SimpleComponent" ) map ((sc: Node) =>
          automatonVarName(sc) + "=" +
          interleaveWith(" | ", initialStates(sc) map name)
        ))
        ++
        (( module \ "ComponentList" \ "VariableComponent" ) map ((vc: Node) =>
          interleaveWith(" & ",
            ((vc \ "VariableInitial" \ "_" )) map parse map printExpr)
        ))
      )
    )
  }

  def initialStates(sc: Node): Seq[Node] = {
    (sc \ "Graph" \ "NodeList" \ "SimpleNode") filter ( s=>
      s.attribute("Initial") match {
        case Some(value) => value.toString == "true"
        case _ => false
      } )
  }

  
  abstract class ArExpr
  case class BinExpr(l: ArExpr, r: ArExpr, op: String) extends ArExpr
  case class UnExpr(e: ArExpr, op: String) extends ArExpr
  case class IntConst(value: Int) extends ArExpr
  case class SimpleId(id: String) extends ArExpr

  def parse(node: Node): ArExpr = {
    node match {
      case <BinaryExpression>{ _* }</BinaryExpression> => 
        new BinExpr(
          parse((node \ "_")(0)),
          parse((node \ "_")(1)),
          getAttribute("Operator", node))      
      
      case <UnaryExpression>{ _* }</UnaryExpression> => 
        new UnExpr(
          parse((node \ "_")(0)),
          getAttribute("Operator", node))

      case <SimpleIdentifier>{ _* }</SimpleIdentifier> =>
        SimpleId(name(node))

      case <IntConstant>{ _* }</IntConstant> =>
        IntConst(java.lang.Integer.parseInt(getAttribute("Value", node)))

      case _ =>
        throw new IllegalArgumentException("can't parse " + node.toString)
    }
  }

  
  def printExpr(e: ArExpr): String = {
    e match {
      case BinExpr(l,r, op) => "(" + printExpr(l) + printBinOp(op) + printExpr(r) + ")"
      case UnExpr(e1, op) => printUnOp(op) + printExpr(e1)
      case IntConst(i) => i.toString
      case SimpleId(id) => id
    }
  }

  def printBinOp(op: String): String = " " + (op match {
      case "==" => "="
      case "!=" => "!="
      case "&" => "&"
      case ">" => ">"
      case ">=" => ">="
      case "<" => "<"
      case "<=" => "<="
      case "&gt;" => ">"
      case "&lt;" => "<"
      case "&lt;=" => "<="
      case "&gt;=" => ">="
      case "&amp;" => "&"
      case "+" => "+"
      case "-" => "-"
      case "*" => "*"
      case "/" => "/"
      case "%" => " mod "
      case "|" => "|"
  }) + " "

  def printUnOp(op: String): String = op match {
      case "-" => "-"
      case "!" => "!"
  }


  

  def tranName(sc: Node, id: String): String = { "t__" + name(sc) + "__" + id }
  
  def defineTransition(sc: Node, edgeAndId: Pair[Node, Int]): String = edgeAndId match {
    case (edge, id) => {
      val expr = interleaveWith(" & ", 
        List(automatonVarName(sc) + "=" + escape(getAttribute("Source", edge)) )
        :::
        List ("(" + interleaveWith(" | ",
          edge \ "LabelBlock" \ "SimpleIdentifier"
            map name
            map ((ev)=> "event=" + ev) ) + ")")
        :::
        List(interleaveWith(" & ",
          edge \ "GuardActionBlock" \ "Guards" \ "_" map parse map printExpr))
      )
      tranName(sc, id.toString) + " := " + expr + ";"
    }
  }

  def allTransitions(model: Node): Unit = {
    for(sc <- model \ "ComponentList" \ "SimpleComponent") {
      val edges = sc \ "Graph" \ "EdgeList" \ "Edge"
      val edgeAndIds = edges.toList.zipWithIndex      
      for(edgeAndId <- edgeAndIds) {
        pw.println(defineTransition(sc, edgeAndId) )
      }
      val notInAlphabet = "!(event in {" + interleaveWith(", ", alphabet(sc) )+ "})"
      val nodes = sc \ "Graph" \ "NodeList" \ "SimpleNode"
      val nodesPropositions = nodes.map((node: Node) => {(node \ "EventList" \ "SimpleIdentifier").map(name)})
      val nodesAndNodePropositions = nodes.toList.zip(nodesPropositions.toList)
      val propositionExprs = nodesAndNodePropositions
          .filter( x => x match {case Pair(node, prop) => !prop.isEmpty})
          .map( x => x match{ case Pair(node, prop) => {
            "(" + automatonVarName(sc) + "=" + name(node) + " & (" +
              interleaveWith(" | ", prop.map("event="+_))  + "))"
            }}
           )      
      val stay = tranName(sc, "stay") + " := " +
        interleaveWith(" | ", List(notInAlphabet) ++ propositionExprs) + " ;"
      pw.println(stay)
      pw.println();
    }
  }

  // very nice, all iterations are hidden  
  def alphabet(sc: Node): List[String] = {
    removeDuplicates((
      // propositions
      (sc \ "Graph" \ "NodeList" \ "SimpleNode" \ "EventList"  \ "SimpleIdentifier") 
      ++
      // transition labels
      (sc \ "Graph" \ "EdgeList" \ "Edge"       \ "LabelBlock" \ "SimpleIdentifier") 
      ++
      // blockedEvents
      (sc \ "Graph" \ "LabelBlock" \ "SimpleIdentifier")                             
    ).map(name).toList)
  }

  def removeDuplicates[A](xs: List[A]): List[A] = {
    if (xs.isEmpty)
      xs
    else
      xs.head :: removeDuplicates(for (x <- xs.tail if x != xs.head) yield x)
  }


  

  def exprToSpecifyNextAutomatonState(sc: Node, edgeAndIds: Seq[Pair[Node, Int]]): String = {
    val edgeExprs = (edgeAndIds map ( (x:Pair[Node,Int]) => x match {
        case Pair (edge,id) => {
          tranName(sc, id.toString) + " & next(" + automatonVarName(sc) + ") = " +
            escape(getAttribute("Target", edge))
        }
      }
    )) ++ List(tranName(sc,"stay") + " & next(" + automatonVarName(sc) +
             ") = " + automatonVarName(sc) )

    interleaveWith(" | \n", edgeExprs )
    
  }

  def automataTransitions(model: Node): Unit = {
    val scs = model \ "ComponentList" \ "SimpleComponent"
    
    val oneTranConds = scs map ( (sc:Node) => {
        oneTranCond(sc, (sc \ "Graph" \ "EdgeList" \ "Edge").toList.zipWithIndex)
      } )

    val tranConds = scs map (sc => {
        exprToSpecifyNextAutomatonState(sc, (sc \ "Graph" \ "EdgeList" \ "Edge").toList.zipWithIndex)
      } )

    val expr = "(\n" +
      interleaveWith("\n) & ( \n",      
        tranConds ++ oneTranConds
      ) +
      "\n)"
    pw.println(expr)
  }

  def oneTranCond(sc: Node, edgeAndIds: Seq[Pair[Node, Int]]): String = {
    interleaveWith(" + ", (edgeAndIds map (
        x => x match {
          case (_,id) => tranName(sc,id.toString)
        }
      )) ++ List(tranName(sc, "stay"))
    ) + " = 1 "    
  }


  

  def variableUpdateDesires(model: Node): Unit = {
    pw.println("-- automata desires about EFA variables: 0 - don't care, 1 - care, 2 - conflict")
    for(vc <- model \ "ComponentList" \ "VariableComponent") {
      for(sc <- model \ "ComponentList" \ "SimpleComponent" if everCaresAbout(vc, sc)) {
        val edgesAndIds = (sc \ "Graph" \ "EdgeList" \ "Edge").toList.zipWithIndex
        pw.println("DEFINE")        
        pw.println(name(vc) + "__" + name(sc) + "__desire := case")
        for((edge, id) <- edgesAndIds){
          if(care(edge, vc)){
            pw.println("  " + tranName(sc, id.toString) + " : 1;")
          }
        }
        pw.println("1 : 0;")
        pw.println("esac;")

        pw.println(name(vc) + "__" + name(sc) + "__desiredVal := case")
        for((edge, id) <- edgesAndIds){
          if(care(edge, vc)){
            pw.println("  " + tranName(sc, id.toString) + " : " + actionValue(edge, vc) + ";")
          }
        }
        pw.println("1 : " + name(vc) + ";")
        pw.println("esac;")
      }
    }
  }

  // returns true if given automaton sc has any node that updates given variable
  def everCaresAbout(vc: Node, sc: Node): Boolean = {
    !((sc \ "Graph" \ "EdgeList" \ "Edge")
      .filter(edge => care(edge, vc))
      .isEmpty)
  }

  // returns true if a given edge has some action with a given variable
  def care(edge: Node, vc: Node): Boolean = {
    left(careAndVal(edge, vc))
  }

  def actionValue(edge: Node, vc: Node): String = {
    right(careAndVal(edge, vc)) match {
      case Some(expr) => printExpr(expr)
      case None => name(vc)
    }
  }

  def left[A,B](p: Pair[A,B]): A = { p match {
      case Pair(l, _) => l
    }
  }
  def right[A,B](p: Pair[A,B]): B = { p match {
      case Pair(_, r) => r
    }
  }

  // if given edge cares about given variable, returns true and action expresssion
  // otherwise returns false and None
  def careAndVal(edge: Node, vc: Node): Pair[Boolean, Option[ArExpr]] = {
    val guards = edge \ "GuardActionBlock" \ "Actions" \ "BinaryExpression"
    if(guards.isEmpty) {
      (false, None)
    } else {
      guards
        .map((g: Node) => parse(g) match {
            case BinExpr(l,r, op) if(op == "=") => l match {
              case SimpleId(v) if(v == name(vc)) => Pair(true, Some(r))
              case _ => Pair(false, None)
            }
            case _ => Pair(false, None)
          })
        .reduceRight((x:Pair[Boolean, Option[ArExpr]],
                      y:Pair[Boolean, Option[ArExpr]])=> {
            x match {
              case Pair(xc, xv) => if(xc){x} else {y}
            }
          })
    }
  }


  


  // v_a + v_b => v_a_b
  // v_a_b + v_c => v_a_b_c
  // v_a_b_c + v_d => v_a_b_c_d
  def combinationOfDesires(model: Node): Unit = {    
    for(vc <- model \ "ComponentList" \ "VariableComponent") {
      val ats = (model \ "ComponentList" \ "SimpleComponent").filter(sc=>everCaresAbout(vc, sc)).map(name)
      pw.println("\n-- " + name(vc) + " : " + interleaveWith(" ", ats) )
      val combinedNames = for( (accPrefix, nextVar) <-
          accumulateNames(ats.toList.map(List(_))).map(interleaveWith("__",_))
            .toList.zip(ats.toList.drop(1))
        ) yield {
              pw.println("DEFINE")
              combineTwo(name(vc), accPrefix, nextVar) // prints to screen and returns combined name
          }
      pw.println("TRANS next(" + name(vc) + ") = " +
        {
          if(ats.isEmpty) { // no automata cares about the variable - keep its value
            name(vc)        
          } else if(combinedNames.isEmpty){  // only one automaton in the system cares
            desiredValVar(name(vc)+"__"+ats(0))
          } else {          // more than one cares - use last combined name
            combinedNames.last
          }
        }
      )
    }
  }

  def prefixWith(p: List[String], list: List[List[String]]): List[List[String]] = {
    list.map(p ++ _)
  }

  // accumulateNames(List(List("a"), List("b"), List("c"))) = 
  //       = List(List("a"), List("a", "b"), List("a", "b", "c"))
  def accumulateNames(s: List[List[String]]): List[List[String]] = {
    if(s.length == 0){Nil}
    else if(s.length == 1) {s}
    else{List(s(0))++prefixWith(s(0), accumulateNames(s.drop(1)))}
  }

  
  def desireVar(name: String): String = name + "__desire"
  def desiredValVar(name: String): String = name + "__desiredVal"

  def combineTwo(varName: String, accPrefix: String, nextVar: String): String = {
    val a = varName + "__" + accPrefix
    val b = varName + "__" + nextVar
    val r = a + "__" + nextVar

    val i = "  "
    val lines:List[String] = List(
       desireVar(r) + " := case "
      ,i+ desireVar(a) + " = 0 : " + desireVar(b) + ";"
      ,i+ desireVar(a) + " = 2 : 2 ;"
      ,i+ desireVar(a) + " = 1 : case "
      ,i+i+ desireVar(b) + " = 0 : 1 ;"
      ,i+i+ desireVar(b) + " = 1 & " + desiredValVar(a) + " =  " + desiredValVar(b) + " : 1;"
      ,i+i+ "1 : 2;"        // if desiredValVar:s are not equal or desireVar(b)=conflict
      ,i+ "esac;"
      ,"esac;"
      ," "
      ,desiredValVar(r) + " := case "
      ,i+ desireVar(r) + " = 1 : case "
      ,i+i+ desireVar(a) + " = 1 : " + desiredValVar(a) + ";"
      ,i+i+ "1 : " + desiredValVar(b) + ";"
      ,i+ "esac;"
      ,i+ "1 : " + varName + ";"
      ,"esac;"
    )
    pw.println(interleaveWith("\n", lines))
    desiredValVar(r) // returns last combined name for future use
  }

  

  def printAcceptingReachableSpec(module: Node): Unit = {

    val automataCond = for(sc <- module \ "ComponentList" \ "SimpleComponent" if
      containsAcceptingNode(sc) && !containsOnlyAccepting(sc)) yield {
      val nodes = sc \ "Graph" \ "NodeList" \ "SimpleNode"
      val accNodes = nodes.filter(isAccepting)
      interleaveWith(" | ", accNodes.map(name).map(node => {automatonVarName(sc)+ "=" + node}))
    }

    val varCond = for(vc <- module \ "ComponentList" \ "VariableComponent" if
      (vc \ "VariableMarking" \ "SimpleIdentifier").map(name).contains(escape(":accepting"))) yield {
        interleaveWith(" & ",
          (vc \ "VariableMarking" \ "_")
            .filter(n => { n match {
              case <SimpleIdentifier>{ _* }</SimpleIdentifier> => false
              case _ => true
            }})
            .map(parse)
            .map(printExpr)
          )
    }

    val expr = "(" + interleaveWith(") & (", automataCond ++ varCond) + ")"

    pw.println("\n--Specification")
    pw.println("CTLSPEC AG(EF("+ expr + "))")
  }

  // returns true if the state (node) is accepting, i.e. contains ":accepting" proposition
  def isAccepting(node: Node): Boolean = {
    (node \ "EventList" \ "SimpleIdentifier").map(name).contains(escape(":accepting"))
  }
  
  def containsAcceptingNode(sc: Node): Boolean = {
    (sc \ "Graph" \ "NodeList" \ "SimpleNode" \ "EventList" \ "SimpleIdentifier")
      .map(name).contains(escape(":accepting"))
  }
  def containsOnlyAccepting(sc: Node): Boolean = {
    (sc \ "Graph" \ "NodeList" \ "SimpleNode").filter(
      node => {
        !((node \ "EventList" \ "SimpleIdentifier").map(name).contains(escape(":accepting")))
      }
    ).isEmpty
  }



}

  