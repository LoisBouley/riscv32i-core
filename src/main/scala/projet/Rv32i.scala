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

  //connexion à la mémoire d'instruction IMEM via ibus
  io.ibus.addr := pc
  io.ibus.en := true.B //on lit en permanence
  io.ibus.be := VecInit(false.B, false.B, false.B, false.B) // pas d'écriture
  io.ibus.wdata := 0.U // pas d'écriture

  //ajout d'un registre de pipeline entre fetch et decode (pipeline du PC)
  //indispensable car la mémoire d'instruction répond avec un cycle de latence
  val pc_retarde = RegNext(pc)

  //Registre pour indiquer si un saut a été pris
  val jumpTaken = RegInit(false.B) 

  //=============================================
  // Decode : Décodage de l'instruction
  //=============================================

  // Si on a pris un saut au cycle précédent, l'instruction qui arrive est fausse.
  // On la remplace par un NOP = instruction qui fait rien
  // On choisit arbitrairement (ADDI x0, x0, 0 -> 0x00000013)
  val insn = Mux(jumpTaken, "h00000013".U, io.ibus.rdata) //récupération de l'instruction

  //On récupère les adresses des registres rs1, rs2 et rd
  val rs1 = insn(19,15)
  val rs2 = insn(24,20)
  val rd = insn(11,7)
  val funct3 = insn(14, 12)

  //détection du type de l'instruction via l'opcode
  val opcode = insn(6,0)
  val isLui   = opcode === "b0110111".U  // Lui
  val isAuipc = opcode === "b0010111".U  // Auipc
  val isIIR  = opcode === "b0010011".U  // Instructions de type I/IR (ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI)
  val isR   = opcode === "b0110011".U  // Instructions de type R (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)
  val isJal = opcode === "b1101111".U // Jal 
  val isJalr = opcode === "b1100111".U // Jalr (de type I mais cas particulier)
  val isBranch = opcode === "b1100011".U // Instructions de type B (BEQ, BNE, BLT, BGE, BLTU, BGEU)
  val isStore = opcode === "b0100011".U // Instructions de type S (SB, SH, SW)
  val isLoad = opcode === "b0000011".U // Instructions de type Load (LB, LH, LW, LBU, LHU)


  //On met 1 dans jumpTaken si on a un saut pour executer NOP au cycle suivant
  jumpTaken := isJal || isJalr 

  // Génération des immédiats
  val immU = Cat(insn(31,12), 0.U(12.W))
  val immI = Cat(Fill(20, insn(31)), insn(31,20))
  val immJ = Cat(Fill(12, insn(31)), insn(19, 12), insn(20), insn(30, 21), 0.U(1.W))
  val immB = Cat(Fill(20, insn(31)), insn(7), insn(30,25), insn(11,8), 0.U(1.W))
  val immS = Cat(Fill(20, insn(31)), insn(31,25), insn(11,7))

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
  //Pour Auipc, source 1 = PC retardé
  //Pour les instructions de type I/IR et R, source 1 = rs1_data
  alu.io.opA := Mux(isAuipc, pc_retarde, rs1_data)

  //Sélection de la source 2
  //Pour Auipc, source2 = immU
  //Pour les instructions de type I/IR, source2 = immI
  //Pour les instructions de type R, source2 = rs2_data
  alu.io.opB := MuxCase(0.U, Seq(
    isAuipc -> immU,
    (isIIR || isJalr) -> immI,
    isR   -> rs2_data,
    isStore -> immS
  ))

  //Exception : pour ADDI, le bit30 ne fait pas sens et on risque de faire une soustraction si on le prend en compte
  //on répertorie les cas où on a besoin de mettre le bit30 à 1
  val is_sra_srai = funct3 === "b101".U && insn(30) // Shift Right Arithmetic (I ou R)
  val is_sub = isR && insn(30) // Sub (pour R uniquement)
  val is_beq_bne = isBranch && (funct3(2) === 0.U) // BEQ ou BNE
  val instru_bit30 = is_sra_srai || is_sub || is_beq_bne

  //Pour les instructions de type B, on fait une traduction pour que l'ALU comprenne quelle opération faire
  //BEQ/BNE (0xx) -> 000 (Arith)
  //BLT/BGE (10x) -> 010 (SLT)
  //BLTU/BGEU (11x) -> 011 (SLTU)
  val funct3_branch = Mux(funct3(2),Cat("b01".U, funct3(1)), "b000".U)

  //Pour Auipc, il n'y a pas de funct3
  //On force donc l'opération à une addition ADDI (funct3 = 000, bit30 = 0)
  //Pour JALR, il a bien le même funct3 que ADDI (000) donc on a bien la somme voulue
  alu.io.funct3 := MuxCase(funct3, Seq(
    isAuipc  -> 0.U,
    isBranch -> funct3_branch
  ))
  alu.io.instru_bit30 := Mux(isAuipc, false.B, instru_bit30)
  val res_alu = alu.io.result

  ///////////////////////////////////////////////////////////////////////
  // On prend la branche ? 
  val alu_zero = res_alu === 0.U
  val takeBranch = MuxCase(false.B, Seq(
    (isBranch && (funct3 === "b000".U)) -> alu_zero, // BEQ
    (isBranch && (funct3 === "b001".U)) -> !alu_zero, // BNE
    (isBranch && (funct3 === "b100".U)) -> !alu_zero, // BLT
    (isBranch && (funct3 === "b101".U)) -> alu_zero, // BGE
    (isBranch && (funct3 === "b110".U)) -> !alu_zero, // BLTU
    (isBranch && (funct3 === "b111".U)) -> alu_zero // BGEU
  ))

  ///////////////////////////////////////////////////////////////////////
  //mise à jour du PC
  //pour JAL : pc = pc + immJ
  //pour JALR : pc = (rs1 + immI) & ~1
  //sinon pc = pc + 4
  val target_jalr = res_alu & "hfffffffe".U //on force le LSB à 0
  val immediat_jump = Mux(isJal, immJ, immB)
  val jump_with_imm = isJal || takeBranch
  pc := Mux(isJalr,target_jalr, Mux(jump_with_imm,immediat_jump, 4.U) + Mux(jump_with_imm,pc_retarde,pc))
  

  //===========================================
  // Memory : Accès à la mémoire de données
  //===========================================

  io.dbus.addr := res_alu //adresse calculée par l'ALU

  //Données à écrire en mémoire avec l'astuce de réplication
  val sb_data = Fill(4, rs2_data(7,0))
  val sh_data = Cat(Fill(2, rs2_data(15,0)))
  val sw_data = rs2_data 

  io.dbus.wdata := MuxCase(0.U, Seq(
    (funct3 === "b000".U) -> sb_data, // SB
    (funct3 === "b001".U) -> sh_data, // SH
    (funct3 === "b010".U) -> sw_data  // SW
  ))

  val addr_lsb = res_alu(1,0)
  io.dbus.be := MuxCase("b0000".U, Seq(
    (funct3 === "b000".U) -> ("b0001".U << addr_lsb), // SB : 00->0001, 01->0010, 10->0100, 11->1000
    (funct3 === "b001".U) -> ( "b0011".U << addr_lsb), // SH : 00->0011, 10->1100
    (funct3 === "b010".U) -> "b1111".U  // SW
  )).asTypeOf(Vec(4, Bool()))

  io.dbus.en := isStore || isLoad //activation de la mémoire de données pour les instructions Store et Load


  //==============================================
  // Writeback : Écriture dans RF
  //==============================================

  //On ajoute le registre de pipeline entre Decode/Execute/Memory et Writeback
  //On conserve les signaux nécessaires pour l'écriture dans RF sur un cycle avec RegNext

  //syntaxe : RegNext(signal, init), on initialise à false par défaut par sécurité pour avoir un false au reset
  val wb_reg_we = RegNext(isLui || isAuipc || isIIR || isR || isJal || isJalr || isLoad, false.B)
  val wb_reg_rd = RegNext(rd)
  //adresse de l'instruction suivante (pc_retarde + 4.U) = pc
  val wb_reg_data = RegNext(Mux(isJal || isJalr,  pc, Mux(isLui,immU,res_alu)))
  val wb_reg_load = RegNext(isLoad)
  val wb_reg_funct3 = RegNext(funct3) // pour le type de load
  val wb_reg_align = RegNext(res_alu(1,0)) // pour l'alignement en load

  
  //Traitement de la donnée en sortie de la mémoire (pour les instructions Load)
  val load_data = io.dbus.rdata

  /*
  On effectue un décalage pour ramener l'octet/halfword voulu en position 0
  Pour cela on utilise wb_reg_align sauvegardé
  
  Pour un byte : 
  switch (wb_reg_align){
    case 00 : rdata_shifted = load_data >> 0    (=load_data >> wb_reg_align * 0)
    case 01 : rdata_shifted = load_data >> 8    (=load_data >> wb_reg_align * 8)
    case 10 : rdata_shifted = load_data >> 16   (=load_data >> wb_reg_align * 16)
    case 11 : rdata_shifted = load_data >> 24   (=load_data >> wb_reg_align * 24) (c'est le cat qui fait le *)
  }

  Pour un halfword : ça marche aussi (le cas 01 et le cas 11 ne sont pas utilisés)
  */
  val rdata_shifted = load_data >> Cat(wb_reg_align,0.U(3.W)) //décalage en fonction de l'alignement
  val load_byte = rdata_shifted(7,0)
  val load_halfword = rdata_shifted(15,0)

  val load_data_formatted = MuxCase(0.U, Seq(
    (wb_reg_funct3 === "b000".U) -> Cat(Fill(24, load_byte(7)), load_byte), // LB
    (wb_reg_funct3 === "b001".U) -> Cat(Fill(16, load_halfword(15)), load_halfword), // LH
    (wb_reg_funct3 === "b010".U) -> load_data, // LW
    (wb_reg_funct3 === "b100".U) -> Cat(Fill(24, 0.U), load_byte), // LBU
    (wb_reg_funct3 === "b101".U) -> Cat(Fill(16, 0.U), load_halfword) // LHU
  ))

  //sélection finale (le mux tout à droite) 
  val rd_data_final = Mux(wb_reg_load, load_data_formatted, wb_reg_data)

  rf.io.we := wb_reg_we
  rf.io.rd_addr := wb_reg_rd
  rf.io.rd_data := rd_data_final

  //on écrit ssi l'instruction est Lui, Auipc I/IR ou R
  val weRf = isLui || isAuipc || isIIR || isR 
  rf.io.we := weRf



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
