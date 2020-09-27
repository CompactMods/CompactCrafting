Fully Padded Example (3x3 recipe)
==================================================

```
    0    1    2
0 [" ", " ", " "]
1 [" ", "C", " "]
2 [" ", " ", " "]
```

1. Dimensions without air: 1x1
2. Trim whitespace (1x1)
3. Padding: +1 on all sides
4. Match to template with padding

No Whitespace Example (1x1 recipe)
==================================================

```
    0
0 ["I"]
```

1. Dimensions without air: 1x1
2. Trim whitespace (1x1)
3. Padding: None
4. Direct match

Whitespace Example (4x4 recipe)
==================================================

```
    0    1    2    3
0 [" ", " ", " ", " "]
1 [" ", "C", " ", " "]
2 [" ", " ", " ", " "]
3 [" ", " ", " ", "X"]
```

1. Dimensions without air: 3x1x3 (min 1,0,1  max 3,0,3)
2. Trim whitespace (3x3)
3. Padding:
    - North 1
    - West 1

Dimensions with air: 4x1x4