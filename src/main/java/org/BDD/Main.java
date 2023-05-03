package org.BDD;


import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Sparse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {

        // Read scalar from nested struct
        Sparse value = Mat5.readFromFile("src/main/java/org/BDD/ex15.mat")
                .getStruct("Problem")
                .getSparse("A");

        DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
        A = Mat5Ejml.convert(value, A);
        A.nz_length = value.getNumNonZero();

        matrixTXT(A, "matrixA");



        //System.out.println("Value: " + value);
        //A.printNonZero();

        System.out.println("Dimensioni matrice A: " + A.numRows + " " + A.numCols);
        System.out.println("Numero di elementi non nulli di A: " + A.nz_length);
        System.out.println("Valore dell'elemento 0,0 di A: " + A.get(0,0));
        System.out.println("Valore dell'elemento 1,6866 di A: " + A.get(1,6866));
        System.out.println("A ha tutti i valori diversi da zero?: " + A.isFull());

        System.out.println("-----------------------------------");


        //CONTROLLO CHE LA MATRICE SIA DEFINITA POSITIVA E SIMMETRICA
        if(MatrixFeatures_DSCC.isPositiveDefinite(A)){
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
        }

        //CALCOLO SOLUZIONE CON DECOMPOSIZIONE DI CHOLESKY

        //calcola la dimensione della matrice A
        int n = A.numCols; //la matrice è simmetrica quindi n = m

        //Crea il vettore B di modo che x = [1,1,....,1]
        //tmp è un vettore colonna, va creato il vettore di tutti 1 e poi moltiplicato per la matrice A per creare B
        DMatrixRMaj tmp  = new DMatrixRMaj(n,1);
        for(int i = 0; i < n; i++){
            tmp.set(i,0,1);
        }

        //moltiplicazione tra il vettore di tutti 1 tmp e la matrice A, il risultato viene salvato in B
        DMatrixRMaj B = CommonOps_DSCC.mult(A,tmp,null);
        //B.print();


        //Crea il vettore x
        DMatrixRMaj x = new DMatrixRMaj(n,1);   //x è un vettore colonna con tutti gli elementi uguali a 0

        //DecompositionFactory_DDRM.chol(A.numCols,false);
        CholeskyUpLooking_DSCC chol = new CholeskyUpLooking_DSCC();
        boolean result = chol.decompose(A);

        if(!result)
            throw new RuntimeException("Cholesky failed");

        DMatrixSparseCSC R = chol.getT(A);

        matrixTXT(R, "matrixR");

        //R.printNonZero();  // R is upper triangular
        System.out.println("Dimensioni matrice R: " + R.numRows + " " + R.numCols);
        System.out.println("Numero di elementi non nulli di R: " + R.nz_length);
        System.out.println("Valore dell'elemento 0,0 di R: " + R.get(0,0));
        System.out.println("Valore dell'elemento 1,6866 di R: " + R.get(1,6866));
        System.out.println("R ha tutti i valori diversi da zero?: " + R.isFull());


        if(CommonOps_DSCC.solve(R, B, x)){
            System.out.println("La soluzione è stata trovata");
        }
        else{
            System.out.println("La soluzione non è stata trovata");
        }

        x.print();



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
