# expected: 000000AA,FFFFFFBB,000000BB,0000BBAA,000000CD
.text
    lui x1, 0
    lui x4, 0
    addi x4, x4, 4
    addi x2, x0, 0xAA   
    addi x3, x0, 0xBB
    addi x5, x0, 0xCD   
    
    sb x2, 0(x1)  # [0] = AA
    sb x3, 1(x1)  # [1] = BB
    sw x5, 8(x1)
    # On a écrit : 0000BBAA en mémoire
    nop

    # Test LBU à l'adresse 0 -> 000000AA
    lbu x31, 0(x1)
    nop # Attente WriteBack

    # Test LB à l'adresse 1 -> 000000BB
    # BB = 10111011 -> étendu -> FFFFFFBB
    lb x31, 1(x1)
    nop

    # Test LBU à l'adresse 1 -> 000000BB
    lbu x31, 1(x1)
    nop

    # Test LHU à l'adresse 0 -> 0000BBAA
    lhu x31, 0(x1)
    nop

    # Test LW à l'adresse 8 -> 000000CD
    lw x31, 4(x4)