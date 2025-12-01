#On ne peut pas faire d'auto test car on ne peut pas encore écrire dans le registre 31
#pour tester l'étape 2, on regarde dans GTKWave que ImmU est bien construit et que RA, RB et RW sont bien connectés et décodés

# expected: 00000000,00123000,00abc000

    .text
    #ici on vérifie qu'on a ImmU bien construit (imm << 12). On execute donc des instructions de type U.
    lui x31, 0xfffff
    lui x31, 0x123
    lui x31, 0xabc

    #ici on vérifie que RA, RB et RW recoivent bien les bonnes informations
    add x1, x2, x3
    add x4, x5, x6
