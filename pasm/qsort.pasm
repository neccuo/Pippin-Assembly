LOD FF
SUB #1
STO F0
LOD @F0
STO F0 
STO F2 
LOD FF
SUB #2
STO F1
LOD @F1
STO F1 
ADD #1
STO F3 
LOD F0
SUB F1
SUB #1
STO F4
CMPL F4
NOT
JMPZ #41
LOD @F1 
STO F4 
LOD F2 
SUB F3
STO F5
CMPL F5
NOT
JMPZ #14 
LOD F4
SUB @F3 
STO F5
CMPL F5
JMPZ #B
LOD @F3 
STO F5
LOD @F2
STO @F3
LOD F5
STO @F2
LOD F2
SUB #1
STO F2 
JUMP #-14
LOD F3 
ADD #1
STO F3
JUMP #-18
LOD @F2 
STO @F1
LOD F4
STO @F2 
LOD #54
STO @FF
LOD FF
ADD #1
STO FF
LOD F1
STO @FF
LOD FF
ADD #1
STO FF
LOD F2
SUB #1
STO @FF
LOD FF
ADD #1
STO FF
LOD #53
STO @FF
LOD FF
ADD #1
STO FF
LOD F2
ADD #1
STO @FF
LOD FF
ADD #1
STO FF
LOD F0
STO @FF
LOD FF
ADD #1
STO FF 
JUMP #-53 
LOD FF 
SUB #3
STO FF
LOD @FF
STO F0
JUMP &F0
HALT
DATA
0 5A
1 A0
2 DF
FF 3
A0 47
A1 54
A2 39
A3 42
A4 5
A5 45
A6 63
A7 40
A8 20
A9 48
AA 60
AB 17
AC 52
AD 32
AE 12
AF 27
B0 51
B1 49
B2 50
B3 36
B4 6
B5 7
B6 28
B7 15
B8 19
B9 64
BA 30
BB 21
BC 56
BD 4
BE 43
BF 24
C0 44
C1 13
C2 59
C3 58
C4 8
C5 23
C6 14
C7 34
C8 3
C9 53
CA 18
CB 62
CC 33
CD 37
CE 61
CF 10
D0 2
D1 29
D2 46
D3 57
D4 38
D5 41
D6 1
D7 26
D8 16
D9 35
DA 11
DB 55
DC 22
DD 9
DE 25
DF 31
