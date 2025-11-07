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
  io.dbus.be := VecInit(Seq.fill(4)(false.B))
  io.dbus.en := false.B

  io.x31 := 0.U(32.W)
  io.valid_x31.foreach(_ := false.B)

  val pc = RegInit(0.U(32.W))

  io.ibus.addr := pc
  io.ibus.en := true.B
  io.ibus.be := VecInit(Seq.fill(4)(false.B))
  val insn = WireDefault(io.ibus.rdata)
  
  pc := pc + 4.U(32.W)

  // ########################

  class RF(sim: Boolean = false) extends Module {
    val io = IO(new Bundle {
      val rsa_addr = Input(UInt(5.W))
      val rsb_addr = Input(UInt(5.W))
      val rw_addr = Input(UInt(5.W))
      val rw_data = Input(UInt(32.W))
      val we = Input(Bool())
      val rsa_data = Output(UInt(32.W))
      val rsb_data = Output(UInt(32.W))
    })

    val banc_reg = Reg(Vec(32, UInt(32.W)))
    io.rsa_data := Mux(io.rsa_addr === 0.U, 0.U, regs(io.rsa_addr)) 
    io.rsb_data := Mux(io.rsb_addr === 0.U, 0.U, regs(io.rsb_addr))
    when (io.we && io.rw_addr =/= 0.U) {
        regs(io.rw_addr) := io.rw_data
    }

    io.rw_data := insn

    val rf = 
  }


  
  // locally(sim)
}
