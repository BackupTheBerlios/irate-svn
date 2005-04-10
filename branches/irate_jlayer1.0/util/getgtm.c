#include <stdio.h>
#include <assert.h>

int main(int argc, char* argv[]) 
{
  FILE* f;
  char s[1024];

  f=fopen("/home/tj/.gnome/gtm", "r");
  assert(f);
  while (fgets(s, sizeof(s), f)) {
    if (strncmp(s, "url=", 4) == 0) {
      int len = strlen(s);
      assert(len);
      if (s[len - 1] == '\n')
        s[len - 1] = 0;
      printf("  <Track artist=\"\" title=\"\" url=\"%s\"/>\n", s + 4);
    }
  }
  fclose(f);
  
  return 0;
}
