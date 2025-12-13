# expected: 00ABC000,FFFFF000,0000000C,00001010,00000001,00000002,FFFFFF00,000007FF,FFFFFFFF,000007FE,00000FFD
.text
    nop #pc = 0x0

    # Test lui : vérifie que lui marche toujours
    lui x31, 0xABC  #pc = 0x4

    # Test lui avec valeur max 
    lui x31, 0xFFFFF  #pc = 0x8

    #Test auipc : vérifie que auipc marche toujours
    auipc x31, 0  #pc = 0xC
    auipc x31, 1  #pc = 0x10

    # Tests addi simple
    addi x31, x0, 1   # 0x0 + 0x00000001 = 0x00000001
    
    # Test addi avec écriture sur un registre et lecture du même registre
    addi x31, x31, 1  # 0x00000001 + 0x00000001 = 0x00000002

    # Test extension de signe
    addi x31, x0, -256  # 0x00000000 + 0xFFFFFF00 = 0xFFFFFF00


    # Test max min et dépassement
    addi x31, x0, 0x7FF # Valeur maximale d'immédiat   0x00000000 + 0x000007FF
    addi x31, x31, -2048 # Valeur minimale d'immédiat   2047 - 2048 = -1  ie 0x000007FF + 0xFFFFF800 = FFFFFFFF
    addi x31, x31, 0x7FF  # -1 + 2047 = 2046  ie 0xFFFFFFFF + 0x000007FF = 0x1000007FE donc 0x000007FE avec dépassement
    
    # Test où on atteint une valeur dans x31 pas atteignable avec un seul addi si x31 est initialisé à 0
    addi x31, x31, 0x7FF #  0x000007FE + 0x000007FF = 0x00000FFD