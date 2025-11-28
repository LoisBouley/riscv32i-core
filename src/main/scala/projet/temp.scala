package projet

import chisel3._

class Rv32i(sim: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val ibus = Flipped(new BusInterface)
    val dbus = Flipped(new BusInterface)
    val x31 = Output(UInt(32.W))
    val valid_x31  = if (sim)  Some(Output(Bool())) else None
  })

  io.ibus.wdata := 0.U

  io.dbus.addr := 0.U
  io.dbus.wdata := 0.U
  io.dbus.be := VecInit(false.B, false.B, false.B, false.B)
  io.dbus.en := false.B

  if (sim) io.valid_x31.get := false.B
  io.x31 := 0.U

  val pc = RegInit(0.U(32.W))
  val sautReg = RegInit(false.B)

  val rf  = Module(new RF())
  val alu = Module(new ALU())

  // Etape fetch
  io.ibus.addr := pc
  io.ibus.en := true.B
  io.ibus.be := VecInit(false.B, false.B, false.B, false.B)

  // Registres pipeline 
  // Capture l'instruction courante et son adresse de l'instruction
  val insn_dec = RegNext(io.ibus.rdata) // instruction visible lors de l'étape de décodage
  val pc_dec   = RegNext(pc) // adresse correspondant à insn_dec

  // Prochaine instruction
  pc := pc + 4.U

  // Décodage de l'instruction
  alu.io.opcode := insn_dec(6,0)
  alu.io.funct3 := insn_dec(14,12)
  alu.io.funct7 := insn_dec(31,25)

  rf.io.rsb_addr := insn_dec(24,20)
  rf.io.rsa_addr := insn_dec(19,15)
  rf.io.rw_addr  := insn_dec(11,7)

  // Signaux de contrôle
  val isLui = alu.io.opcode === "b0110111".U
  val isAuipc = alu.io.opcode === "b0010111".U
  val isIIRop = alu.io.opcode === "b0010011".U // opération type I / IR
  val isRop = alu.io.opcode === "b0110011".U // opération type R

  // Génération de l'immédiat
  val imm_u = insn_dec(31,12) ## 0.U(12.W)
  val imm_i = insn_dec(31, 20).asSInt.pad(32).asUInt // extension de signe
  val imm   = Mux(isLui || isAuipc, imm_u, imm_i)

  // Write-enable activé si opération avec écriture
  rf.io.we := isLui || isAuipc || isIIRop || isRop

  // Inputs pour l'ALU
  // - opA: rs1 or pc (pour auipc)
  // - opB: imm_i ou imm_u (pour auipc)
  // c'est l'ALU qui s'occupe de prendre uniquement les bits nécessaires pour les décalages
  alu.io.opA := Mux(isAuipc, pc_dec, rf.io.rsa_data)
  alu.io.opB := Mux(isIIRop, imm, rf.io.rsb_data)

  // Ecriture du résultat de l'instruction dans le Registry File
  rf.io.rw_data := Mux(isLui, imm_u, alu.io.result)

  // Outputs pour les simulations
  io.x31 := rf.io.x31_out
  if (sim) {
    io.valid_x31.get := (rf.io.we && (rf.io.rw_addr === 31.U))
  }

  dontTouch(io.x31)
}
