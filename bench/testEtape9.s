# expected: 00000001,00000002,00000003,00000004,00000005,00000006,00000007,00000008,00000009,0000000A,0000000B,0000000C,0000000D,0000000E,0000000F,00000010,00000011,00000012,00030000
.text
    nop

    # ==========================
    # Tests beq (avant/arrière)
    # ==========================

    # beq non pris
    addi x1, x0, 5
    addi x2, x0, 6              
    beq  x1, x2, beq_fwd_erreur  # x1 != x2 donc beq non pris

    addi x31, x0, 0x01          # non pris on écrit 1 dans x31

    # beq avant pris
    beq  x0, x0, beq_fwd        # x0 == x0 donc beq pris

beq_fwd_erreur:
    addi x31, x0, 0xFF          # ne devrait jamais arriver ici

beq_back:
    addi x31, x0, 0x03          # on écrit 3 dans x31 pour témoigner de l'arrivée
    jal  x0, bne_start

beq_fwd:
    addi x31, x0, 0x02          # on écrit 2 dans x31 pour témoigner de l'arrivée
    addi x1, x0, 1              # on égalise x1 et x2
    addi x2, x0, 1
    beq  x1, x2, beq_back       # x1 == x2 donc beq pris

    # ==========================
    # Tests bne
    # ==========================

bne_start:
    # bne non pris
    addi x3, x0, 7
    addi x4, x0, 7
    bne  x3, x4, bne_fwd_erreur  # x3 == x4 donc bne non pris

    addi x31, x0, 0x04          # non pris on écrit 4 dans x31

    # bne avant pris
    addi x3, x0, 8
    addi x4, x0, 9
    bne  x3, x4, bne_fwd        # x3 != x4 donc bne pris

bne_fwd_erreur:
    addi x31, x0, 0xFF          # ne devrait jamais arriver ici

bne_back:
    addi x31, x0, 0x06          # on écrit 6 dans x31 pour témoigner de l'arrivée
    jal  x0, blt_start

bne_fwd:
    addi x31, x0, 0x05          # on écrit 5 dans x31 pour témoigner de l'arrivée
    addi x3, x0, 10
    addi x4, x0, 11
    bne  x3, x4, bne_back       # x3 != x4 donc bne pris

    # ==========================
    # Tests blt (signed)
    # ==========================

blt_start:
    # blt non pris erreur stricte 10 > 5
    addi x5, x0, 10
    addi x6, x0, 5
    blt  x5, x6, blt_fwd_erreur  # x5 >= x6 donc blt non pris

    #blt non pris erreur souple 5==5
    addi x5, x0, 5
    blt x5, x6, blt_fwd_erreur  # x5 == x6 donc blt non pris

    addi x31, x0, 0x07          # non pris on écrit 7 dans x31

    # blt avant pris
    addi x5, x0, 2
    addi x6, x0, 10
    blt  x5, x6, blt_fwd        # x5 < x6 donc blt pris

blt_fwd_erreur:
    addi x31, x0, 0x0FF          # ne devrait jamais arriver ici

blt_back:
    addi x31, x0, 0x09          # on écrit 9 dans x31 pour témoigner de l'arrivée
    jal  x0, bge_start

blt_fwd:
    addi x31, x0, 0x08          # on écrit 8 dans x31 pour témoigner de l'arrivée
    addi x5, x0, -1             # x5 = -1 
    addi x6, x0, 0
    blt  x5, x6, blt_back       # -1 < 0 donc blt pris

    # ==========================
    # Tests bge (signed)
    # ==========================

bge_start:
    # bge non pris (-5 < 1)
    addi x7, x0, -5             # x7 = -5 
    addi x8, x0, 1
    bge  x7, x8, bge_fwd_erreur  # x7 < x8 donc bge non pris

    addi x31, x0, 0x0A          # non pris on écrit A dans x31

    # bge avant pris (10 >= 5)
    addi x7, x0, 10
    addi x8, x0, 5
    bge  x7, x8, bge_fwd        # x7 >= x8 donc bge pris

bge_fwd_erreur:
    addi x31, x0, 0xFF          # ne devrait jamais arriver ici

bge_back:
    addi x31, x0, 0x0C          # on écrit C dans x31 pour témoigner de l'arrivée
    jal  x0, bltu_start

bge_fwd:
    addi x31, x0, 0x0B          # on écrit B dans x31 pour témoigner de l'arrivée
    addi x7, x0, 0
    addi x8, x0, 0
    bge  x7, x8, bge_back       # 0 >= 0 donc bge pris

    # ==========================
    # Tests bltu (unsigned)
    # ==========================

bltu_start:
    # bltu non pris (0xFFFFFFFF >= 0 en non signé)
    addi x9, x0, -1             # x9 = 0xFFFFFFFF (non signé: grand nombre)
    addi x10, x0, 0
    bltu x9, x10, bltu_fwd_erreur # x9 >= x10 donc bltu non pris

    addi x31, x0, 0x0D          # non pris on écrit D dans x31

    # bltu avant pris (1 < 100 en non signé)
    addi x9, x0, 1
    addi x10, x0, 100
    bltu x9, x10, bltu_fwd      # x9 < x10 donc bltu pris

bltu_fwd_erreur:
    addi x31, x0, 0xFF          # ne devrait jamais arriver ici

bltu_back:
    addi x31, x0, 0x0F          # on écrit F dans x31 pour témoigner de l'arrivée
    jal  x0, bgeu_start

bltu_fwd:
    addi x31, x0, 0x0E          # on écrit E dans x31 pour témoigner de l'arrivée
    addi x9, x0, 5
    addi x10, x0, 10
    bltu x9, x10, bltu_back     # 5 < 10 donc bltu pris

    # ==========================
    # Tests bgeu (unsigned)
    # ==========================

bgeu_start:
    # bgeu non pris (1 < 2 en non signé)
    addi x11, x0, 1
    addi x12, x0, 2
    bgeu x11, x12, bgeu_fwd_erreur # x11 < x12 donc bgeu non pris

    addi x31, x0, 0x10          # non pris on écrit 10 dans x31

    # bgeu avant pris (0xFFFFFFFF >= 0 en non signé)
    addi x11, x0, -1            # x11 = 0xFFFFFFFF
    addi x12, x0, 0
    bgeu x11, x12, bgeu_fwd     # x11 >= x12 donc bgeu pris

bgeu_fwd_erreur:
    addi x31, x0, 0xFF          # ne devrait jamais arriver ici

bgeu_back:
    addi x31, x0, 0x12          # on écrit 12 dans x31 pour témoigner de l'arrivée
    jal  x0, end

bgeu_fwd:
    addi x31, x0, 0x11          # on écrit 11 dans x31 pour témoigner de l'arrivée
    addi x11, x0, 10
    addi x12, x0, 10
    bgeu x11, x12, bgeu_back    # 10 >= 10 donc bgeu pris


end:
    lui x31, 0x30               # x31 = 0x00030000