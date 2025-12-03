# expected: 000000010,0000002C,0000003C,00000088,00000098,00000060,00030000
.text
    nop                      # 0x00
    
    # Charger l'adresse cible dans x1, puis sauter
    lui x1, 0x0              # 0x04    x1 = 0x00000000
    addi x1, x1, 0x2C        # 0x08    x1 = 0x0000002C (adresse de target1)
    jalr x31, x1, 0          # 0x0C    Saute à x1+0 = 0x2C, x31 = 0x10
    
    nop                      # 0x10
    nop                      # 0x14
    nop                      # 0x18
    nop                      # 0x1C
    nop                      # 0x20
    nop                      # 0x24
    addi x0, x0, 0           # 0x28

target1:
    auipc x31, 0             # 0x2C     x31 = 0x2C
    
    # Charger l'adresse de target2 dans x2
    lui x2, 0x0              # 0x30     x2 = 0x00000000
    addi x2, x2, 0x88        # 0x34     x2 = 0x00000088 (adresse de target2)
    jalr x31, x2, 0          # 0x38     Saute à x2+0 = 0x88, x31 = 0x3C
    
    nop                      # 0x3C
    nop                      # 0x40
    nop                      # 0x44
    nop                      # 0x48
    nop                      # 0x4C
    nop                      # 0x50

retourArriere:
    # Charger l'adresse de fin dans x3
    lui x3, 0x0              # 0x54 - x3 = 0x00000000
    addi x3, x3, 0x98        # 0x58 - x3 = 0x00000090 (adresse de fin)
    jalr x31, x4, 0x44          # 0x5C - Saute à x3+0 = 0x98, x31 = 0x60
    
    nop                      # 0x60
    nop                      # 0x64
    nop                      # 0x68
    nop                      # 0x6C
    nop                      # 0x70
    nop                      # 0x74
    nop                      # 0x78
    nop                      # 0x7C
    nop                      # 0x80
    addi x0, x0, 0           # 0x84

target2:
    auipc x31, 0             # 0x88 - x31 = 0x88
    
    # Saut en arrière vers retourArriere
    lui x4, 0x0              # 0x8C - x4 = 0x00000000
    addi x4, x4, 0x54        # 0x90 - x4 = 0x00000054 (adresse de retourArriere)
    jalr x31, x4, 0          # 0x94 - Saute à x4+0 = 0x54, x31 = 0x98

fin:
    lui x31, 0x30            # 0x98 - x31 = 0x00030000