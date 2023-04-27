package org.BDD;


import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionCommon_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Array;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Sparse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Main {
    public static void main(String[] args) throws IOException {

        /* Read matrix from file fatto da Davide
        MatFileReader matReader = new MatFileReader("src/main/java/org/BDD/cfd1.mat");
        MLStructure mlStructure = (MLStructure) matReader.getMLArray("Problem");
        MLSparse mlSparse = (MLSparse) mlStructure.getField("A");
        int numRows = mlSparse.getM();
        int numCols = mlSparse.getN();
        int[] ir = mlSparse.getIR();
        int[] ic = mlSparse.getIC();
        Double[] pr = mlSparse.exportReal();

        for(int i = 0; i < ir.length; i++)
            System.out.println("IR: " + ir[i]);

        for(int i = 0; i < ic.length; i++)
            System.out.println("IC: " + ic[i]);

        /*for (Double aDouble : pr) {
            System.out.println("PR: " + aDouble);
        }*/

        /*System.out.println("pr.Lenght " + pr.length);
        System.out.println("ir.lenght " + ir.length);
        System.out.println("ic.Lenght " + ic.length);

        OpenMapRealMatrix sparseMatrix = new OpenMapRealMatrix(numRows, numCols);
        for(int i = 0; i < pr.length; i++) {
            sparseMatrix.setEntry(ir[i], ic[i], pr[i]);
            System.out.println("numero posizione nel vettore pr[]: " + i);
           }


        System.out.println(" numero di righe Sparse matrix: " + sparseMatrix.getRowDimension());

        System.out.println("Sparse matrix: " + sparseMatrix.getEntry(0, 0));

        double[][] valoriMatrice = sparseMatrix.getData();

        for(int i = 0; i < valoriMatrice.length; i++)
            for(int j = 0; j < valoriMatrice[i].length; j++)
                System.out.println("valoriMatrice: " + valoriMatrice[i][j]);*/










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
