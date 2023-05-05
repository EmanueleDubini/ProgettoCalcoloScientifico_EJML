package org.BDD;


import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_MT_DSCC;
import org.ejml.sparse.csc.NormOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Sparse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {

        // Percorso della cartella contenente le matrici
        String path = "src/main/java/org/BDD/Matrici/";
        // Lista di tutti i file nella cartella Matrici
        File[] files = new File(path).listFiles();

        //----------CICLO CHE LEGGE TUTTE LE MATRICI DELLA CARTELLA "Matrici"----------
        /*for (File file : files) {
            if (file.isFile()) {
                System.out.println("Elaborazione della matrice " + file.getName());

                // Importazione della matrice sparsa simmetrica e definita positiva A
                Sparse value = Mat5.readFromFile(file.getAbsolutePath())
                        .getStruct("Problem")
                        .getSparse("A");

                DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
                A = Mat5Ejml.convert(value, A);
                A.nz_length = value.getNumNonZero();


            }
        }*/


        // Read scalar from nested struct
        Sparse value = Mat5.readFromFile("src/main/java/org/BDD/Matrici/ex15.mat")
                .getStruct("Problem")
                .getSparse("A");

        DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
        A = Mat5Ejml.convert(value, A);
        A.nz_length = value.getNumNonZero();

        //matrixTXT(A, "matrixA");
        //System.out.println("Value: " + value);
        //A.printNonZero();

        System.out.println("Dimensioni matrice A: " + A.numRows + " " + A.numCols);
        System.out.println("Numero di elementi non nulli di A: " + A.nz_length);
        System.out.println("Valore dell'elemento 0,0 di A: " + A.get(0,0));
        System.out.println("Valore dell'elemento 1,6866 di A: " + A.get(1,6866));
        System.out.println("A ha tutti i valori diversi da zero?: " + A.isFull());

        System.out.println("-----------------------------------");


        //----------CONTROLLO CHE LA MATRICE SIA DEFINITA POSITIVA E SIMMETRICA---------- todo rimettere per consegna
        /*if(MatrixFeatures_DSCC.isPositiveDefinite(A)){
            System.out.println("La matrice A è definita positiva");
        }
        else{
            System.out.println("La matrice A non è definita positiva");
            //se si arriva qui va lanciata un eccezione throw new RuntimeException("La matrice A non è definita positiva");
        }

        if(MatrixFeatures_DSCC.isSymmetric(A,1e-8)){
            System.out.println("La matrice A è simmetrica");
        }
        else{
            System.out.println("La matrice A non è simmetrica");
            //se si arriva qui va lanciata un eccezione throw new RuntimeException("La matrice A non è simmetrica");
        }*/

        //----------CALCOLO SOLUZIONE CON DECOMPOSIZIONE DI CHOLESKY----------
        // Libera la memoria non utilizzata
        System.gc();
        // Misura la memoria iniziale
        long memoriaIniziale = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        //calcola la dimensione della matrice A
        int n = A.numCols; //la matrice è simmetrica quindi n = m

        //Crea il vettore B di modo che x = [1,1,....,1]
        //tmp è un vettore colonna, va creato il vettore di tutti 1 e poi moltiplicato per la matrice A per creare B
        DMatrixSparseCSC tmp  = new DMatrixSparseCSC (n,1);
        for(int i = 0; i < n; i++){
            tmp.set(i,0,1);
        }

        //moltiplicazione tra il vettore di tutti 1 tmp e la matrice A, il risultato viene salvato in B
        DMatrixSparseCSC B = CommonOps_MT_DSCC.mult(A,tmp,null); //B = A*tmp eseguito in mmulti-thread
        //B.print();


        //Crea il vettore x
        DMatrixSparseCSC x = new DMatrixSparseCSC(n,1);   //x è un vettore colonna con tutti gli elementi uguali a 0


        long startTime = System.currentTimeMillis(); //registra il tempo d'inizio

        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
        solver.setA(A);
        solver.solveSparse(B,x);
        // Stampa della soluzione
        System.out.println("Soluzione del sistema:");
        //x.print();
        System.out.println("R ha tutti i valori diversi da zero?: " + x.isFull());

        //risoluzione alternatiiva del sistema lineare
        /*LinearSolverCholesky_DSCC solver2 = new LinearSolverCholesky_DSCC(new CholeskyUpLooking_DSCC(), null);
        solver2.setA(A);
        solver2.solve(B,x);
        // Stampa della soluzione
        //System.out.println("Soluzione del sistema:");
        //x.print();*/

        // Misura la memoria finale
        long memoriaFinale = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calcola la memoria utilizzata
        long memoriaUtilizzata = memoriaFinale - memoriaIniziale;
        System.out.println("Memoria iniziale: " + (memoriaIniziale / (1024F * 1024F)) + " MB");
        System.out.println("Memoria finale: " + (memoriaFinale / (1024F * 1024F)) + " MB");
        System.out.println("Memoria utilizzata: " + (memoriaUtilizzata / (1024F * 1024F)) + " MB");

        //registra il tempo di fine
        long stopTime = System.currentTimeMillis();
        //calcola il tempo impiegato in millisecondi
        double elapsedTimeSeconds = (stopTime - startTime) / 1000.0;
        System.out.println("Tempo di esecuzione: " + elapsedTimeSeconds + " s");

        //----------CALCOLO ERRORE RELATIVO----------
        //definisco vettore soluzione xe esatta di modo che xe = [1,1,....,1]
        DMatrixSparseCSC xe  = new DMatrixSparseCSC (n,1);
        for(int i = 0; i < n; i++){
            xe.set(i,0,1);
        }

        double norm_x = NormOps_DSCC.normF(x);
        double norm_xe = NormOps_DSCC.normF(xe);
        double norm_diff = norm_x - norm_xe;
        double relative_error = norm_diff / norm_xe;
        System.out.println("Norma di x: " + norm_x);
        System.out.println("Norma di xe: " + norm_xe);
        System.out.println("Errore relativo: " + relative_error);

       }





       public static void matrixTXT(DMatrixSparseCSC A, String matrixName) {
           /*try {
               // Open a file for writing
               PrintWriter writer = new PrintWriter("src/main/java/org/BDD/" + matrixName + ".txt");

               // Loop through the rows and columns of the matrix
               for (int i = 0; i < A.numRows; i++) {
                   for (int j = 0; j < A.numCols; j++) {
                       // Check if the (i, j) entry is nonzero
                       boolean nonzero = false;
                       for (int k = A.col_idx[j]; k < A.col_idx[j+1]; k++) {
                           if (A.nz_rows[k] == i) {
                               nonzero = true;
                               writer.print(A.nz_values[k] + " ");
                               break;
                           }
                       }
                       if (!nonzero) {
                           writer.print("0 ");
                       }
                   }
                   writer.println();
               }

               // Close the file
               writer.close();
           } catch (IOException e) {
               e.printStackTrace();
           }*/

           // Open a file writer and write the matrix to a file
           try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/BDD/" + matrixName + ".txt"))) {
               // Write the matrix dimensions to the file
               writer.write(A.numRows + " " + A.numCols + "\n");

               // Write the matrix values to the file
               for (int i = 0; i < A.numRows; i++) {
                   for (int j = 0; j < A.numCols; j++) {
                       writer.write(String.format("%f ", A.get(i, j)));
                   }
                   writer.write("\n");
               }
           } catch (IOException e) {
               System.err.println("Error writing matrix to file: " + e.getMessage());
           }
       }


}
