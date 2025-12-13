# expected: FFFFFFFF,000000FF,0000000F,FFFFFC0F,FFFFFCA4,FFFFF948,FFFFE520,7FFFF290,1FFFFCA4,0FFFFE52,FFFFFFFF,FFFFFFFF,FFFFFFFF,00000001,00000000,00000000,00000001,00000001,00000000
.text
    nop

    addi x31, x0, -1 # x31 = -1 = 0xFFFFFFFF

    # Tests andi
    andi x31, x31, 0x0FF # 0xFFFFFFFF & 0x000000FF = 0x000000FF
    andi x31, x31, 0x00F # 0x000000FF & 0x0000000F = 0x0000000F

    # Tests ori
    ori  x31, x31, -1024 # 0x0000000F | 0xFFFFFC00 = 0xFFFFFC0F

    # Tests xori
    xori x31, x31, 0x0AB # 0xFFFFFC0F ^ 0x000000AB = 0xFFFFFCA4

    # Tests de décalages
    slli x31, x31, 1  # 0xFFFFFCA4 -> 0xFFFFF948
    slli x31, x31, 2  # 0xFFFFF948 -> 0xFFFFE520

    srli x31, x31, 1  # FFFFE520 -> 0x7FFFF290
    srli x31, x31, 2  # 0x7FFFF290 -> 0x1FFFFCA4

    srai x31, x31, 1  # 0x1FFFFCA4 -> 0x0FFFFE52
    # On vérifie que le décalage arithmétique recopie bien le bit de signe
    addi x31, x0, -1    # x31 = -1 = 0xFFFFFFFF
    srai x31, x31, 1    # 0xFFFFFFFF >> 1  = 0xFFFFFFFF
    srai x31, x31, 2    # 0xFFFFFFFF >> 2  = 0xFFFFFFFF

    # Tests slti et sltiu
    slti x31, x31, 0    # -1 < 0 -> 1
    slti x31, x0, 0     # 0 < 0 -> 0

    addi x31, x0, 0      # x31 = 0x00000000
    sltiu x31, x31, 1    # 0 < 1 (unsigned) -> 1
    sltiu x31, x0, -1    # 0 < |-1| les valeurs absolues viennes du fait qu'on est en unsigned -> 1
    sltiu x31, x31, 1    # 1 < 1 -> 0 
