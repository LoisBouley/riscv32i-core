package projet

import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val opA = Input(UInt(32.W))
    val opB = Input(UInt(32.W))
    val funct3 = Input(UInt(3.W))
    val instru_bit30 = Input(Bool()) // Juste ce bit pour distinguer SRL/SRA, SRLI/SRAI et ADD/SUB
    val result = Output(UInt(32.W))
  })


  val isArith  = (io.funct3 === "b000".U)
  val isSll  = (io.funct3 === "b001".U)
  val isSlt  = (io.funct3 === "b010".U)
  val isSltu = (io.funct3 === "b011".U)
  val isSrl  = (io.funct3 === "b101".U) && (io.instru_bit30 === false.B)
  val isSra  = (io.funct3 === "b101".U) && (io.instru_bit30 === true.B)
  val isXor  = (io.funct3 === "b100".U)
  val isOr   = (io.funct3 === "b110".U)
  val isAnd  = (io.funct3 === "b111".U)


  // Addition et soustraction
  val arithRes  = Mux(io.instru_bit30, io.opA - io.opB, io.opA + io.opB)
  
  // Opérations logiques
  val resLogic = MuxCase(0.U, Seq(
    isOr  -> (io.opA | io.opB),
    isAnd -> (io.opA & io.opB),
    isXor -> (io.opA ^ io.opB)
  ))

  // Décalages
  val shamt = io.opB(4,0) // nombre de positions à décaler
  val resShift = MuxCase(0.U, Seq(
    isSll -> (io.opA << shamt).asUInt,
    isSrl -> (io.opA >> shamt).asUInt,
    isSra -> (io.opA.asSInt >> shamt).asUInt
  ))

  // Comparaisons
  val resCompare = MuxCase(0.U, Seq(
    isSlt  -> (io.opA.asSInt < io.opB.asSInt).asUInt,
    isSltu -> (io.opA < io.opB)
  ))

  io.result := MuxCase(0.U, Seq(
    isArith -> arithRes,

    isOr   -> resLogic,
    isAnd  -> resLogic,
    isXor  -> resLogic,

    isSll  -> resShift,
    isSrl  -> resShift,
    isSra  -> resShift,

    isSlt  -> resCompare,
    isSltu -> resCompare
  ))
}