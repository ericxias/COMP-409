#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main(int argc, char *argv[]) {
    // check args
    if (argc != 4) {
        printf("Usage: <n> <p> <s>\n");
        return 1;
    }

    // set vars
    int i, j;
    int n = atoi(argv[1]);
    double p = atof(argv[2]);
    int s = atoi(argv[3]);

    // allocate memory for initial array construction and csr arrays
    int **matrix = (int **)malloc(n * sizeof(int *));
    for (int i = 0; i < n; i++) {
        matrix[i] = (int *)malloc(n * sizeof(int));
    }
    
    int *rowptr = (int *)malloc((n + 1) * sizeof(int));
    int *cols = (int *)malloc(n * n * sizeof(int));
    int *vals = (int *)malloc(n * n * sizeof(int));

    // counters for nonzero cols and vals
    int total_nonzero_col = 0;
    int total_nonzero_val = 0;
    srand(s);

    // openmp api to set number of threads, each thread deals with a n grids
    omp_set_dynamic(0);
    omp_set_num_threads(n);

    // parallel for loop to construct the initial matrix
    /* 
    * 
    * currently every row (outside the first) of the matrix is the same, ask TA if this is correct
    * 
    */

    clock_t start_time = clock();

#pragma omp parallel for private(j)
for (i = 0; i < n; i++) {
    unsigned int rand_val;
    for (j = 0; j < n; j++) {
        
        // critical section to generate random number
        // rand_r not available in windows, use critical section instead
        int rand_val;
        #pragma omp critical
        {
            rand_val = rand();
        }

        // if random number is less than p, set to 0, else set to 1
        if (rand_val % 100 < p * 100) {
            matrix[i][j] = 0;
        } else {
            matrix[i][j] = 1;
        }
    }
}

    // print the initial matrix
    printf("Initial matrix:\n");

    for (i = 0; i < n; i++) {
        for (j = 0; j < n; j++) {
            printf("%d", matrix[i][j]);
        }
        printf("\n");
    }

    // parallel section for CSR formation
#pragma omp parallel sections
    {
#pragma omp section
        {   
            rowptr[0] = 0;

            // iterate through the matrix to find the number of 1s in each row and increment the total for each 1
            for (i = 0; i < n; i++) {
                rowptr[i + 1] = rowptr[i];

                for (j = 0; j < n; j++) {
                    if (matrix[i][j] == 1) {
                        rowptr[i + 1]++;
                    }
                }
            }
        }

#pragma omp section
        {
            // set local variables
            int x, y;
            int local_nonzero_col = 0;
            
            // iterate through the matrix to find the non-zero grids, and store the column number
            for (x = 0; x < n; x++) {
                for (y = 0; y < n; y++) {

                    if (matrix[x][y] == 1) {
                        cols[local_nonzero_col] = y;
                        local_nonzero_col++;
                    }
                }
            }

            // set the total number of non-zero columns
            total_nonzero_col += local_nonzero_col;
        }

#pragma omp section
        {
            // set local variables
            int a, b;
            int local_nonzero_val = 0;

            // iterate through the matrix to find the non-zero grids, and store the value
            for (a = 0; a < n; a++) {
                for (b = 0; b < n; b++) {

                    if (matrix[a][b] == 1) {
                        vals[local_nonzero_val] = matrix[a][b];
                        local_nonzero_val++;
                    }
                }
            }

            // set the total number of non-zero values
            total_nonzero_val += local_nonzero_val;
        }
    }

    clock_t end_time = clock();
    double total_time = ((double)end_time - start_time) * 1000 / CLOCKS_PER_SEC;

    // print output
    printf("Rowptr array: ");
    for (i = 0; i < n + 1; i++) {
        printf("%d ", rowptr[i]);
    }
    printf("\n");

    printf("Cols array: ");
    for (i = 0; i < total_nonzero_col; i++) {
        printf("%d ", cols[i]);
    }
    printf("\n");

    printf("Vals array: ");
    for (i = 0; i < total_nonzero_val; i++) {
        printf("%d ", vals[i]);
    }
    printf("\n");

    printf("Time taken: %f ms\n", total_time);

    // free memory
    free(vals);
    free(cols);
    free(rowptr);
    for (i = 0; i < n; i++) {
        free(matrix[i]);
    }
    free(matrix);

    return 0;
}