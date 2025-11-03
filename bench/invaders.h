#ifndef __SPRITE_H__
#define __SPRITE_H__
#include <stdint.h>

#define OBJECT_SIZE 8

struct color_t {
   uint8_t b:3;
   uint8_t g:3;
   uint8_t r:2;
} __attribute__((packed));
typedef struct color_t color_t;

typedef union {
    color_t pixel[4];
    uint32_t pixels;
} pixels_t;


/* Objet pour représenter les aliens, le vaisseau et le laser */
typedef struct object_t {
   uint32_t alive;
   uint32_t period;
   uint32_t deadline;
   int x, y;
   int dx, dy;
   uint8_t *pattern;
   color_t color;
   color_t bg[OBJECT_SIZE][OBJECT_SIZE];
   int ax, ay;
} object_t;

/* État pour implanter la mécanique du jeu sous forme de machine à états */
typedef struct state_t {
   int dx;
   int dy;
   int next_state;
} state_t;

/* Prototype des fonctions définies dans invaders.c */
color_t read_pixel(uint32_t x, uint32_t y);
void write_pixel_scaling(color_t pixel, uint32_t x, uint32_t y);
void clear_screen(color_t color);
void initialize(void);
void display_pattern_line(uint8_t m, uint32_t x, uint32_t y, color_t color);
void display_pattern(uint8_t pattern[OBJECT_SIZE], uint32_t x, uint32_t y, color_t color);
void display_sprite(object_t *object);
void display_timer(void);
#endif
