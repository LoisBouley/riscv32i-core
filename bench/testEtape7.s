# expected: FFFFFFFF,000000FF,0000000F,FFFFFC0F,FFFFFCA4,FFFFF948,FFFFE520,7FFFF290,1FFFFCA4,0FFFFE52,FFFFFFFF,FFFFFFFF,FFFFFFFF,00000001,00000000,00000000,00000001,00000001,00000000
.text
    nop

    # initialisation
    addi x31, x0, -1    # x31 = -1 = 0xFFFFFFFF

    # Tests and
    addi x30, x0, 0x0FF
    and  x31, x31, x30  # 0xFFFFFFFF & 0x000000FF = 0x000000FF
    addi x29, x0, 0x00F
    and  x31, x31, x29  # 0x000000FF & 0x0000000F = 0x0000000F

    # Tests or
    addi x28, x0, -1024
    or   x31, x31, x28  # 0x0000000F | 0xFFFFFC00 = 0xFFFFFC0F

    # Tests xor
    addi x27, x0, 0x0AB
    xor  x31, x31, x27  # 0xFFFFFC0F ^ 0x000000AB = 0xFFFFFCA4

    # Tests de décalages 
    addi x26, x0, 1
    sll  x31, x31, x26  # 0xFFFFFCA4 -> 0xFFFFF948
    addi x26, x0, 2
    sll  x31, x31, x26  # 0xFFFFF948 -> 0xFFFFE520

    addi x26, x0, 1
    srl  x31, x31, x26  # 0xFFFFE520 -> 0x7FFFF290
    addi x26, x0, 2
    srl  x31, x31, x26  # 0x7FFFF290 -> 0x1FFFFCA4

    addi x26, x0, 1
    sra  x31, x31, x26  # 0x1FFFFCA4 -> 0x0FFFFE52

    # on vérifie que le décalage arithmétique recopie bien le bit de signe
    addi x31, x0, -1    # x31 = -1 = 0xFFFFFFFF
    addi x26, x0, 1
    sra  x31, x31, x26  # 0xFFFFFFFF >> 1  = 0xFFFFFFFF
    addi x26, x0, 2
    sra  x31, x31, x26  # 0xFFFFFFFF >> 2  = 0xFFFFFFFF

    # Tests slt et sltu
    slt  x31, x31, x0    # -1 < 0 -> 1
    slt  x31, x0, x0     # 0 < 0 -> 0

    addi x31, x0, 0      # x31 = 0x00000000
    addi x26, x0, 1
    sltu x31, x31, x26   # 0 < 1  -> 1
    addi x27, x0, -1
    sltu x31, x0, x27    # 0 < 0xFFFFFFFF  -> 1 car test unsigned
    sltu x31, x31, x26   # 1 < 1 -> 0
