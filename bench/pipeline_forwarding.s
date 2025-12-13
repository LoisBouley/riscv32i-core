# expected: 00000001,00000002,00000003,00000004
.text
    lui x10, 0  # Adresse de base pour la mémoire

    # Test 1 : utiliser tout de suite le résultat d'un calcul
    addi x1, x0, 1
    add x31, x1, x0    # x31 = 1 (on utilise x1 juste après l'avoir calculé)

    # Test 2 : utiliser un load juste après
    sw x31, 0(x10)     # On écrit 1 en mémoire
    
    lw x2, 0(x10)      # On charge x2 depuis la mémoire
    add x31, x2, x1    # x31 = 2 (on utilise x2 direct après le load)

    # Test 3 : store juste après un calcul
    addi x3, x0, 3
    sw x3, 4(x10)      # On écrit x3 en mémoire directement
    
    # on relit pour vérifier
    nop
    nop
    lw x31, 4(x10)     # x31 = 3

    # Test 4 : branchement avec une valeur qui vient d'être calculée
    addi x4, x0, 4
    beq x4, x4, suite  # On compare x4 avec lui-même tout de suite
    
    lui x31, 0xDEAD    # Ne devrait pas passer ici
    lui x31, 0xDEAD
    lui x31, 0x1EAF
    lui x31, 0x1EAF
    lui x31, 0x1EAF
    lui x31, 0x1EAF
    lui x31, 0x1EAF
    lui x31, 0x1EAF

suite:
    addi x31, x0, 4    # x31 = 4