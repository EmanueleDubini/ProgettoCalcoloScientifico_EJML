# Progetto Metodi del Calcolo Scientifico - Libreria EJML

>  Progetto JAVA per la risoluzione con il metodo di Choleski di sistemi lineari con matrici sparse e definite positive di grandi dimensioni

## Introduzione

Questo Repository contiene una delle 3 librerie analizzate per completare il progetto di calcolo scientifico. All'interno del progettp vengono utiliizzate le librerie:
- [MAT File Library](https://github.com/HebiRobotics/MFL) (MFL) per leggere e scrivere file MAT compatibili con il formato MAT-File di MATLAB.
- [Efficient Java Matrix Library](https://github.com/lessthanoptimal/ejml.git) (EJML) per risolvere il sistema associato ad una matrice sparsa, simmetrica e definita positiva utilizzando la decomposizione di cholesky

## Documentazione
Link utili per visionare la documentazione delle librerie utilizzate:
- Documentazione libreria EJML: http://ejml.org/wiki/index.php?title=Manual
- JavaDoc libreria EJML: http://ejml.org/javadoc/

Il file *Main.java* contiene il codice per eseguire la decomposizione di Cholesky e la risoluzione 
del sistema lineare *Ax=b* per gran parte delle matrici richieste dalla consegna.

### Matrici supportate

| Nomi Matrici | Stato | Dimensione (in KB) |
|-----------|-----------|-----------|
| ex15.mat  | :white_check_mark:   |  555 |
| shallow_water1.mat    | :white_check_mark:   | 2263 |
| apache2.mat   | :white_check_mark:    | 8302 |
| parabolic_fem.mat  | :white_check_mark:    | 13116 |
| G3_circuit.mat   | :white_check_mark:    | 13833 |
| cfd1.mat   | :white_check_mark:    | 14164 |
| cfd2.mat   | :white_check_mark:    | 23192 |
| StocF-1465.mat   | :x:    | 178368 |
| Flan_1565.mat   | :x:    | 292858 |
