# expected: 00069000,FFFFF000,0000000C,00001010,FFFFF014,00000018
.text
    nop #pc = 0x0

    # Test lui : vérifie que lui marche toujours
    lui x31, 0x69 #pc = 0x4
    
    # Test lui avec valeur max 
    lui x31, 0xFFFFF #pc = 0x8
    
    # Test auipc avec valeur nulle : auipc ajoute 0
    auipc x31, 0 #pc = 0xC
    
    # Test auipc avec petite valeur : auipc ajoute 0x1000
    auipc x31, 1 #pc = 0x10
    
    # Test auipc avec une valeur max : auipc ajoute 0xFFFFF000
    auipc x31, 0xFFFFF #pc = 0x14
    
    # Test auipc final : vérifie que PC continue d'avancer
    auipc x31, 0 #pc = 0x18