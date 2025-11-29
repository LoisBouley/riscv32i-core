package projet

import chisel3._

class Rv32i(sim: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val ibus = Flipped(new BusInterface)
    val dbus = Flipped(new BusInterface)
    val x31 = Output(UInt(32.W))
    val valid_x31  = if (sim)  Some(Output(Bool())) else None
  })


  //=============================================
  // Fetch : Lecture de l'instruction
  //=============================================

  //registre pc initialisé à 0
  val pc = RegInit(0.U(32.W))

  //mis à jour de pc pour lire l'instruction suivante à chaque cycle
  pc := pc + 4.U

  //connexion à la mémoire d'instruction IMEM via ibus
  io.ibus.addr := pc
  io.ibus.en := true.B //on lit en permanence
  io.ibus.be := VecInit(false.B, false.B, false.B, false.B) // pas d'écriture
  io.ibus.wdata := 0.U // pas d'écriture

  //ajout d'un registre de pipeline entre fetch et decode (pipeline du PC)
  //indispensable car la mémoire d'instruction répond avec un cycle de latence
  val pc_retarde = RegNext(pc)

  //=============================================
  // Decode : Décodage de l'instruction
  //=============================================

  val insn = io.ibus.rdata //récupération de l'instruction

  //On récupère les adresses des registres rs1, rs2 et rd
  val rs1 = insn(19,15)
  val rs2 = insn(24,20)
  val rd = insn(11,7)
  val funct3 = insn(14, 12)

  //détection du type de l'instruction via l'opcode
  val opcode = insn(6,0)
  val isLUI   = opcode === "b0110111".U  // LUI
  val isAUIPC = opcode === "b0010111".U  // AUIPC
  val isIIR  = opcode === "b0010011".U  // Instructions de type I/IR (ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI)
  val isR   = opcode === "b0110011".U  // Instructions de type R (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)


  // Génération de l'immédiat de type U
  val immU = Cat(insn(31,12), 0.U(12.W))

  // Génération de l'immédiat de type I
  val immI = Cat(Fill(20, insn(31)), insn(31,20)) //extension de signe

  val rf = Module(new RFLUTRAM(sim)) //création de RF avec le module de TP6

  //Connexion aux ports de RF pour la lecture
  rf.io.rs1_addr := rs1
  rf.io.rs2_addr := rs2
  rf.io.rd_addr := rd

  val rs1_data = rf.io.rs1_data
  val rs2_data = rf.io.rs2_data

  //=============================================
  // Execute : Instanciation de l'ALU
  //=============================================

  val alu = Module(new ALU)

  //Sélection de la source 1
  //Pour AUIPC, source 1 = PC retardé
  //Pour les instructions de type I/IR et R, source 1 = rs1_data
  alu.io.opA := Mux(isAUIPC, pc_retarde, rs1_data)

  //Sélection de la source 2
  //Pour AUIPC, source2 = immU
  //Pour les instructions de type I/IR, source2 = immI
  //Pour les instructions de type R, source2 = rs2_data
  alu.io.opB := MuxCase(0.U, Seq(
    isAUIPC -> immU,
    isIIR  -> immI,
    isR   -> rs2_data
  ))

  //Exception : pour ADDI, le bit30 ne fait pas sens et on risque de faire une soustraction si on le prend en compte
  val is_sra_srai = funct3 === "b101".U && insn(30) // Shift Right Arithmetic (I ou R)
  val is_sub = isR && insn(30) // Sub (pour R uniquement)
  val instru_bit30 = is_sra_srai || is_sub

  //Pour AUIPC, il n'y a pas de funct3 comme pour ADDI
  //On force donc l'opération à une addition ADDI (funct3 = 000, bit30 = 0)
  alu.io.funct3 := Mux(isAUIPC, "b000".U, funct3)
  alu.io.instru_bit30 := Mux(isAUIPC, false.B, instru_bit30)
  
  val res_alu = alu.io.result

  //==============================================
  // Writeback : Écriture du résultat dans RF
  //==============================================

  //sélection de la donnée à écrire dans RF
  rf.io.rd_data := Mux(isLUI, immU, res_alu)

  //on écrit ssi l'instruction est de type LUI, AUIPC I/IR ou R
  val weRf = isLUI || isAUIPC || isIIR || isR 
  rf.io.we := weRf





  //gestion des sorties (non utilisées pour l'instant)
  io.dbus.addr := 0.U
  io.dbus.en := false.B
  io.dbus.be := VecInit(false.B, false.B, false.B, false.B)
  io.dbus.wdata := 0.U


// Sorties de debug

  if (sim) {
    // Sortie de debug : valeur de x31
    io.x31 := rf.io.debug_x31.get
    // Signal indiquant qu'une écriture dans x31 a eu lieu
    io.valid_x31.get := rf.io.we && (rd === 31.U)
  } else {
    io.x31 := 0.U
  }

  dontTouch(rf.io.rs1_data)
  dontTouch(rf.io.rs2_data)
  dontTouch(immU)
  dontTouch(immI)
  dontTouch(pc_retarde)  

  locally(sim)
}
