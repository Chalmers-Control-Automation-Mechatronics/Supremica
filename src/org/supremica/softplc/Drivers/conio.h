#include <termio.h>

void restore( struct termio* tty_save );
char getch(void);
char kb_hit(void);
