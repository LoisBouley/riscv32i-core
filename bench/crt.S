    .section .text,"ax",@progbits
    .equ   STACK_SIZE,   1024
    .globl _start

_start:
    # réservation d'une (petite) zone de mémoire pour la pile
    la   sp, stack + STACK_SIZE
    # appel de main, sans espoir de retour !
    jal  main
1:  j 1b

    .bss
    .align 4
    .global stack
stack:
    .skip STACK_SIZE
