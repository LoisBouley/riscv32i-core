    .text

# ------------------------------------------------------------
# 1. Test LUI
# ------------------------------------------------------------

    lui x31, 0x00000        # x31 = 0x00000000
    # expected: 00000000

    lui x31, 0xFFFFF        # x31 = 0xFFFFF000
    # expected: FFFFF000

    lui x31, 0x12345        # x31 = 0x12345000
    # expected: 12345000


# ------------------------------------------------------------
# 2. Test AUIPC  (x31 = PC + immediate << 12)
# PC starts at 0 and increments by 4 each instruction
# ------------------------------------------------------------

    auipc x31, 0            # x31 = PC = 0x0000000C (approx depending on pipeline)
    # expected: (PC value at execution)

    auipc x31, 0x1          # x31 = PC + 0x00001000
    # expected: PC + 1000

    auipc x31, 0xABCDE >> 12  # arbitrary test
    # expected: PC + ABCDE000


# ------------------------------------------------------------
# 3. Tests ADDI
# ------------------------------------------------------------

    lui x1, 0               # x1 = 0
    addi x31, x1, 123       # x31 = 123
    # expected: 0000007B

    addi x31, x1, -1        # x31 = 0xFFFFFFFF
    # expected: FFFFFFFF

    lui x1, 1
    addi x31, x1, -2048     # smallest negative immediate
    # expected: 00000??? depends on x1


# ------------------------------------------------------------
# 4. Tests logical immediates ANDI, ORI, XORI
# ------------------------------------------------------------

    lui x1, 0xFF00F
    andi x31, x1, 0x0F0     # mask low bits
    # expected: 000000F0

    ori x31, x1, 0x123      # OR immediate
    # expected: FF00F123

    xori x31, x1, 0xFFF     # XOR immediate
    # expected: FF00F??? (depends)


# ------------------------------------------------------------
# 5. Tests SLLI, SRLI, SRAI
# ------------------------------------------------------------

    lui x1, 1               # x1 = 0x00010000

    slli x31, x1, 4         # shift left by 4
    # expected: 000100000 >> 4 = 0x00100000

    srli x31, x1, 4         # shift right logical
    # expected: 00001000

    srai x31, x1, 4         # arithmetic right shift 
    # expected: 00001000


# ------------------------------------------------------------
# 6. Tests SLTI, SLTIU
# ------------------------------------------------------------

    addi x1, x0, 5          # x1 = 5
    slti x31, x1, 10        # signed: 5 < 10 → x31 = 1
    # expected: 00000001

    slti x31, x1, -1        # signed: 5 < -1 → false
    # expected: 00000000

    sltiu x31, x1, -1       # unsigned: 5 < 0xFFFFFFFF → true
    # expected: 00000001
