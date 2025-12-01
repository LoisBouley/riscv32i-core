package projet

import chisel3._

class RFLUTRAM(sim: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val rs1_addr = Input(UInt(5.W))
    val rs2_addr = Input(UInt(5.W))
    val rd_addr = Input(UInt(5.W))
    val rd_data = Input(UInt(32.W))
    val we = Input(Bool())
    val rs1_data = Output(UInt(32.W))
    val rs2_data = Output(UInt(32.W))
    val debug_x31 = if (sim) Some(Output(UInt(32.W))) else None
  })

  val regs = Mem(32, UInt(32.W))

  // Écriture dans le registre
  // On interdit toujours d'écrire en 0
  // Cela garantit que si la case 0 vaut 0, elle restera à 0.
  when(io.we && (io.rd_addr =/= 0.U)) {
    regs(io.rd_addr) := io.rd_data
  }

  // Lecture du port 1
  io.rs1_data := regs(io.rs1_addr)
  // Lecture du port 2
  io.rs2_data := regs(io.rs2_addr)

  //initialisation pour la simulation + debug x31
  if (sim) {
    io.debug_x31.get := regs(31)

    when(reset.asBool) {
      for (i <- 0 until 32) {
        regs(i) := 0.U
      }
    }
  }

  locally(sim) // Stub pour éviter les warnings avant implantation
}