# Progetto Metodi del Calcolo Scientifico - Libreria EJML

>  Progetto JAVA per la risoluzione con il metodo di Choleski di sistemi lineari con matrici sparse e definite positive di grandi dimensioni

## Introduzione

Questo Repository contiene una delle 3 librerie analizzate per completare il progetto di Metdi del Calcolo Scientifico. All'interno del progetto vengono utiliizzate le seguenti librerie:
- [MAT File Library](https://github.com/HebiRobotics/MFL) (MFL) per leggere e scrivere file MAT compatibili con il formato MAT-File di MATLAB.
- [Efficient Java Matrix Library](https://github.com/lessthanoptimal/ejml.git) (EJML) per risolvere il sistema associato ad una matrice sparsa, simmetrica e definita positiva utilizzando la decomposizione di cholesky

## Documentazione utilizzata
Link utili per visionare la documentazione delle librerie utilizzate durante l'implementazione del progetto:
- repository libreria MFL: https://github.com/HebiRobotics/MFL
- JavaDoc libreria MFL: https://javadoc.io/doc/us.hebi.matlab.mat/mfl-core/latest/index.html
- Documentazione libreria EJML: http://ejml.org/wiki/index.php?title=Manual
- JavaDoc libreria EJML: http://ejml.org/javadoc/

## Note Generali
Il file *Main.java* contiene il codice per eseguire la decomposizione di Cholesky e la risoluzione 
del sistema lineare *Ax=b* per gran parte delle matrici richieste dalla consegna.

### Matrici supportate
Le Matrici simmetriche e definite positive considerate fanno parte della SuiteSparse Matrix Collection che colleziona matrici sparse derivanti da applicazioni di problemi reali 
(ingegneria strutturale, fluidodinamica, elettromagnetismo, termodinamica, computer graphics/vision, network e grafi). Disponibili al seguente link: https://sparse.tamu.edu/

| Nomi Matrici | Stato | Dimensione (in KB) |
|-----------|-----------|-----------|
| [ex15.mat](https://sparse.tamu.edu/FIDAP/ex15)  | :white_check_mark:   |  555 |
| [shallow_water1.mat](https://sparse.tamu.edu/MaxPlanck/shallow_water1)    | :white_check_mark:   | 2263 |
| [apache2.mat](https://sparse.tamu.edu/GHS_psdef/apache2)   | Out of memory :x:    | 8302 |
| [parabolic_fem.mat](https://sparse.tamu.edu/Wissgott/parabolic_fem)  | Out of memory :x:    | 13116 |
| [G3_circuit.mat](https://sparse.tamu.edu/AMD/G3_circuit)   | Out of memory :x:    | 13833 |
| [cfd1.mat](https://sparse.tamu.edu/Rothberg/cfd1)   | :white_check_mark:    | 14164 |
| [cfd2.mat](https://sparse.tamu.edu/Rothberg/cfd2)   | :white_check_mark:    | 23192 |
| [StocF-1465.mat](https://sparse.tamu.edu/Janna/StocF-1465)   | Out of memory :x:    | 178368 |
| [Flan_1565.mat](https://sparse.tamu.edu/Janna/Flan_1565)   | Out of Memory :x:    | 292858 |

## File dati_java.csv
Durante l'esecuzione del file *Main.java* vengono analizzate una ad una le matrici in formato .mat contenute all'interno della cartella [Matrici](https://github.com/EmanueleDubini/ProgettoCalcoloScientifico_EJML/tree/master/src/main/java/org/BDD/Matrici). 
Per ognuna di esse si osserva:
- il tempo necessario per calcolare la soluzione x
- l’errore relativo tra la soluzione calcolata x e la soluzione esatta xe
- la memoria necessaria per risolvere il sistema

Per ciascuna matrice analizzata, queste quantità vengono salvate all'interno del file (dati_java.csv)[https://github.com/EmanueleDubini/ProgettoCalcoloScientifico_EJML/blob/master/src/main/java/org/BDD/dati_java.csv] usato poi da un programma Python per generarne i grafici: https://github.com/dbancora/ProgettoCalcoloScientifico_Grafici.git
