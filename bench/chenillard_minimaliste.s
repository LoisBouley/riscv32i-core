.text
ori  x1, x0, 21
ori  x2, x0, 1
addi x3, x3, 1 #Itérateur. Ne fonctionne que sur carte car x3 ne sera pas initialisé à 0 en simu.
srl  x4, x3, x1 # Compteur tous les 2 million d'itérations environ (fréquence à l'Hertz)
and  x4, x4, x2 
xor  x5, x4, x6 # détection de front (x6 étant la valeur précédente de x4)
or   x6, x0, x4 # Sauvegarde de la valeur précédente
# Le motif à afficher est dans x7.
# On le décale de x5 (0 ou 1) position vers la gauche
sll  x7, x7, x5
ori x7, x7, 1
or  x31, x0, x7
# Via l'instruction de garde que rajoute l'assembleur,  on réinitialise PC.
# Donc on reboucle sur la première instruction.
