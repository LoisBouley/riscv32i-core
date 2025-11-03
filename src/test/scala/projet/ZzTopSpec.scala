package projet

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import scala.io.Source
import scala.collection.mutable.ListBuffer

class ZzTopSpec extends AnyFreeSpec with Matchers with ChiselSim {

def expectedValuesFromAsm(path: String): Seq[BigInt] = {
  val line = Source.fromFile(path).getLines().find(_.startsWith("# expected:"))
  line match {
    case Some(l) =>
      l.stripPrefix("# expected:")
        .split(",")
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(str => BigInt(str, 16))
        .toSeq
    case None =>
      Seq.empty
  }
}

def failTrace(msg: String = "Erreur",trace: ListBuffer[String]): Unit = {
  val prefix = s"🛑 $msg\n" 
  val traceStr =
    if (trace.nonEmpty) "Trace:\n" + trace.map("  - " + _).mkString("\n") + "\n"
    else ""
  fail(prefix + traceStr)
}

def expectNextValue(dut: ZzTop, expected: BigInt, timeout: Int, trace: ListBuffer[String]): Unit = {
  var cycles = 0
  dut.clock.step(12) // Attente d'un cycle proc (12 cycles systeme)
  // Attendre une écriture dans x31   
  while (!dut.io.valid_x31.get.peek().litToBoolean && cycles < timeout) {
    dut.clock.step(12) // Attente d'un cycle proc
    cycles += 1
    trace += f"Attente $cycles cycles"
  }

  if (cycles < timeout){
    // Vérifier la nouvelle valeur
    val got = dut.io.x31.get.peek().litValue
    trace += f"Valeur attendue = 0x$expected%08X, reçue = 0x$got%08X"
    if ( got != expected ) failTrace("Mauvaise sortie",trace)
  } else {
    trace += f"Valeur attendue = 0x$expected%08X, reçue = ⏰"
    failTrace("Simulation bloquée",trace)
  } 
}


 // Fonction de test commune
  def testWith(file: String): Unit = {
    val path = file.replace("/mem/","/").replace(".mem",".s")
    if (Files.exists(Paths.get(path))){
      val expected = expectedValuesFromAsm(path)
      simulate(new ZzTop(file,true)) { dut =>
      	val trace = ListBuffer[String]()
          expected.foreach { value =>
            expectNextValue(dut, value, timeout = 100,trace)
          }
      }
    }
  }

  val dir = sys.env("PROOT")+"/bench/mem/"
  val suffix = ".mem"
  val programFile = sys.env.get("PROG")
  if (programFile.isDefined) {
    val name = programFile.get
    val path = dir+name+suffix

    "Simulation pour gtkwave" in {
      simulate(new ZzTop(path,true)) { dut =>
        dut.io.switch.poke(8) // Paramétrage de l'affichage
        dut.io.led.expect(dut.io.led.peek()) // test dummy
        dut.clock.step(sys.env("CYCLES").toInt)
      }
    }
    
    s"Test unitaire $name" in testWith(path)

  } else {
    // Récupération des fichiers .mem et génération de la table de programmes
    val programs = Files.list(Paths.get(dir)).iterator().asScala.filter(_.toString.endsWith(suffix))
    val p2 = programs.map(path => (path.getFileName.toString.stripSuffix(suffix), path.toString)).toSeq
    val table = Table(("name", "path"), p2: _*)
  
    // Déclaration des tests à partir de la table
    forAll(table) { (name, path) =>
      s"Programme $name" in testWith(path)
    }
  }
}
