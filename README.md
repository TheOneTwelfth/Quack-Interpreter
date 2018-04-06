# Quack-Interpreter

An interpreter for Quack programming language written in Kotlin.

Quack is a programming language presented at the 2004 Internet Problem Solving Contest (https://ipsc.ksp.sk/2004/real/problems/g.html).

Quack Virtual Machine has a queue containing integer numbers ranging 0-65536. All operations are computed modulo 65536.
Additionally, the VM has 26 registers denoted by lowercase letters (a-z). Each register can hold one number.

Quack program is represented by a sequence of commands separated by whitespaces or newline symbols.
There is a total of 18 commands, including mathematical operations, queue/register operations, output operations
and various operations implementing labeling commands and later jumping to them (incl. conditional jumping).
