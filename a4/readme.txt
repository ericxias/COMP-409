Open Develepor Command Prompt for Visual Studio 2022

Change working directory to root of the assignment folder

To compile: use wsl for linux: Ubuntu wsl -> cd /mnt/c/Users/.../COMP-409/a4 
gcc q1.c -o q1 -fopenmp
gcc q2.c -o q2 -fopenmp

To run: 
./q1 <n> <p> <s>
./q2 <t> <n>


for sequential:
gcc q1sequential.c -o q1sequential
./q1sequential.c -o <n> <p> <s>