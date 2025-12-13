# pas de test automatique
# On vérifie les signaux dans GTKWave
.text
    lui x1, 0
    addi x2, x0, 0xAA   # Pattern AA
    addi x3, x0, 0xBB   # Pattern BB
    
    # Test SW
    # Doit écrire 0x000000AA à l'adresse 0
    # wdata=000000AA, be=1111
    sw x2, 0(x1)

    # Test SH
    # Doit écrire 00AA à l'adresse 2
    # wdata=00AA00AA, be=1100
    sh x2, 2(x1)


    # Test SB
    # Doit écrire BB à l'adresse 4
    # wdata=BBBBBBBB (réplication, c'est be qui sélectionnera où on écrit), be=0001
    sb x3, 4(x1)

    # Doit écrire BB à l'adresse 5
    # wdata=BBBBBBBB, be=0010
    sb x3, 5(x1)

