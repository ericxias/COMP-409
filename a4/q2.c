#include <omp.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {
    if (argc != 3) {
        printf("Usage: <t> <n>\n");
        return 1;
    }

    int t = atoi(argv[1]);
    int n = atoi(argv[2]);
}