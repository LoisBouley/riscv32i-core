package projet

import chisel3._

class RF extends Module {
    val io = IO(new Bundle {
        val rsa_addr = Input(UInt(5.W))
        val rsb_addr = Input(UInt(5.W))
        val rw_addr = Input(UInt(5.W))
        val rw_data = Input(UInt(32.W))
        val we = Input(Bool())
        val rsa_data = Output(UInt(32.W))
        val rsb_data = Output(UInt(32.W))
        val x31_out = Output(UInt(32.W))
    })

    // Banc de registres
    val banc_reg = Reg(Vec(32, UInt(32.W)))

    // lecture du registry file
    io.rsa_data := Mux(io.rsa_addr === 0.U, 0.U, banc_reg(io.rsa_addr))
    io.rsb_data := Mux(io.rsb_addr === 0.U, 0.U, banc_reg(io.rsb_addr))

    // écriture (synchrone) dans le registry file
    when (io.we && (io.rw_addr =/= 0.U)) {
        banc_reg(io.rw_addr) := io.rw_data
    }

    io.x31_out := banc_reg(31)
}
