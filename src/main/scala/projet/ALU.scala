package projet

import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val opA = Input(UInt(32.W))
    val opB = Input(UInt(32.W))
    val opcode = Input(UInt(7.W))
    val funct3 = Input(UInt(3.W))
    val funct7 = Input(UInt(7.W))
    val result = Output(UInt(32.W))
  })


  val isAuipc = io.opcode === "b0010111".U

  // Instruction type I / IR
  val isIIRop = io.opcode === "b0010011".U

  val isAddi = isIIRop && (io.funct3 === "b000".U)
  val isAndi = isIIRop && (io.funct3 === "b111".U)
  val isOri  = isIIRop && (io.funct3 === "b110".U)
  val isXori = isIIRop && (io.funct3 === "b100".U)
  val isSlli = isIIRop && (io.funct3 === "b001".U)
  val isSrli = isIIRop && (io.funct3 === "b101".U) && (io.funct7 === "b0000000".U)
  val isSrai = isIIRop && (io.funct3 === "b101".U) && (io.funct7 === "b0100000".U)
  val isSlti  = isIIRop && (io.funct3 === "b010".U)
  val isSltiu = isIIRop && (io.funct3 === "b011".U)

  // Instruction type R
  val isRop = io.opcode === "b0110011".U

  val isAdd  = isRop && (io.funct3 === "b000".U) && (io.funct7 === "b0000000".U)
  val isSub  = isRop && (io.funct3 === "b000".U) && (io.funct7 === "b0100000".U)
  val isSll  = isRop && (io.funct3 === "b001".U)
  val isSlt  = isRop && (io.funct3 === "b010".U)
  val isSltu = isRop && (io.funct3 === "b011".U)
  val isXor  = isRop && (io.funct3 === "b100".U)
  val isSrl  = isRop && (io.funct3 === "b101".U) && (io.funct7 === "b0000000".U)
  val isSra  = isRop && (io.funct3 === "b101".U) && (io.funct7 === "b0100000".U)
  val isOr   = isRop && (io.funct3 === "b110".U)
  val isAnd  = isRop && (io.funct3 === "b111".U)


  // 5 bits pour les décalages
  val shamt = io.opB(4,0)

  io.result := MuxCase(0.U, Seq(

    (isAdd  || isAddi) -> (io.opA + io.opB),
    isSub -> (io.opA - io.opB),

    (isSlt || isSlti) -> (io.opA.asSInt < io.opB.asSInt).asUInt,
    (isSltu || isSltiu) -> (io.opA < io.opB),

    (isXor || isXori) -> (io.opA ^ io.opB),
    (isOr || isOri) -> (io.opA | io.opB),
    (isAnd || isAndi) -> (io.opA & io.opB),

    (isSll || isSlli) -> ((io.opA << shamt).asUInt),
    (isSrl || isSrli) -> ((io.opA >> shamt).asUInt),
    (isSra || isSrai) -> ((io.opA.asSInt >> shamt).asUInt)
  ))

}