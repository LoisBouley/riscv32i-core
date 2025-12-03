# expected: 00000008,00000028,00000030,00000084,0000008C,00000058,00030000
.text
    nop                      # 0x00

    jal x31, target1         # 0x04 - Saute à target1, sauvegarde PC+4 (0x08) dans x31

    nop                      # 0x08
    nop                      # 0x0C
    nop                      # 0x10
    nop                      # 0x14
    nop                      # 0x18
    nop                      # 0x1C
    nop                      # 0x20
    addi x0, x0, 0           # 0x24

target1:
    auipc x31, 0             # 0x28      x31 = 0x28
    jal x31, target2         # 0x2C - Saute à target2, sauvegarde PC+4 (0x30) dans x31

    nop                      # 0x30
    nop                      # 0x34
    nop                      # 0x38
    nop                      # 0x3C
    nop                      # 0x40
    nop                      # 0x44
    nop                      # 0x48
    nop                      # 0x4C
    nop                      # 0x50 

retourArriere:
    jal x31, fin             # 0x54
    nop                      # 0x58
    nop                      # 0x5C
    nop                      # 0x60
    nop                      # 0x64
    nop                      # 0x68
    nop                      # 0x6C
    nop                      # 0x70
    nop                      # 0x74
    nop                      # 0x78
    nop                      # 0x7C
    addi x0, x0, 0           # 0x80

target2:
    auipc x31, 0             # 0x84      x31 = 0x84
    jal x31, retourArriere       # 0x88 - Saut en arrière vers backward, sauvegarde PC+4 (0x8C) dans x31

fin:
    lui x31, 0x30            # 0x8C    x31 = 0x30000