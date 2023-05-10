package org.BDD;

import com.opencsv.CSVWriter;
import org.ejml.EjmlParameters;
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
import java.lang.instrument.Instrumentation;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws IOException {

        ArrayList<String> MatrixName = new ArrayList<>();
        ArrayList<Long> Size = new ArrayList<>();
        ArrayList<Long> MemoryPre = new ArrayList<>();
        ArrayList<Long> MemoryPost = new ArrayList<>();
        ArrayList<Long> MemoryDiff = new ArrayList<>();
        ArrayList<Double> Time = new ArrayList<>();
        ArrayList<Double> Error = new ArrayList<>(); //todo mettere in notazione scientifica

        // Percorso della cartella contenente le matrici
        String path = "src/main/java/org/BDD/Matrici/";
        File matriciFolder = new File(path);
        // Lista di tutti i file nella cartella Matrici
        File[] files = matriciFolder.listFiles();

        //----------CICLO CHE LEGGE TUTTE LE MATRICI DELLA CARTELLA "Matrici"----------
        for (int i=0; i<files.length; i++) {
            if (files[i].isFile()) {
                System.out.println("-----------------------------------" + "Elaborazione della matrice " + files[i].getName() + "-----------------------------------");

                // Importazione della matrice sparsa simmetrica e definita positiva A
                Sparse value = Mat5.readFromFile(files[i].getAbsolutePath())
                        .getStruct("Problem")
                        .getSparse("A");

                DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
                A = Mat5Ejml.convert(value, A);
                A.nz_length = value.getNumNonZero();

                System.out.println("Dimensioni matrice: " + A.numRows + " " + A.numCols);
                System.out.println("Numero di elementi: " + A.getNumElements());
                System.out.println("Numero di elementi non nulli: " + A.nz_length);

                File matriceA = new File("src/main/java/org/BDD/Matrici/" + files[i].getName());
                long dimensionA = matriceA.length();
                Size.add(dimensionA);
                System.out.println("Dimensioni matrice A: " + dimensionA / (1024 * 1024) + " MB.");

                System.out.println("\n---> Inizio elaborazione matrice " + files[i].getName() + " \n");
                MatrixName.add(files[i].getName());


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
                MemoryPre.add(memoriaIniziale);

                //calcola la dimensione della matrice A
                int n = A.numCols; //la matrice è simmetrica quindi n = m

                //Crea il vettore B di modo che x = [1,1,....,1]
                //tmp è un vettore colonna, va creato il vettore di tutti 1 e poi moltiplicato per la matrice A per creare B
                DMatrixSparseCSC tmp  = new DMatrixSparseCSC (n,1);
                for(int j = 0; j < n; j++){
                    tmp.set(j,0,1);
                }

                //moltiplicazione tra il vettore di tutti 1 tmp e la matrice A, il risultato viene salvato in B
                DMatrixSparseCSC B = CommonOps_MT_DSCC.mult(A,tmp,null); //B = A*tmp eseguito in mmulti-thread

                //Crea il vettore x
                DMatrixSparseCSC x = new DMatrixSparseCSC(n,1);   //x è un vettore colonna con tutti gli elementi uguali a 0


                long startTime = System.currentTimeMillis(); //registra il tempo d'inizio

                LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
                solver.setA(A);
                solver.solveSparse(B,x);

                System.out.println("Sistema risolto");

                //risoluzione alternatiiva del sistema lineare
                /*LinearSolverCholesky_DSCC solver2 = new LinearSolverCholesky_DSCC(new CholeskyUpLooking_DSCC(), null);
                solver2.setA(A);
                solver2.solve(B,x);
                // Stampa della soluzione
                //System.out.println("Soluzione del sistema:");
                //x.print();*/

                // Misura la memoria finale
                long memoriaFinale = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                MemoryPost.add(memoriaFinale);

                // Calcola la memoria utilizzata
                long memoriaUtilizzata = memoriaFinale - memoriaIniziale;
                MemoryDiff.add(memoriaUtilizzata);
                System.out.println("Memoria iniziale: " + (memoriaIniziale / (1024F * 1024F)) + " MB");
                System.out.println("Memoria finale: " + (memoriaFinale / (1024F * 1024F)) + " MB");
                System.out.println("Memoria utilizzata nella risoluzione: " + (memoriaUtilizzata / (1024F * 1024F)) + " MB");

                //registra il tempo di fine
                long stopTime = System.currentTimeMillis();
                //calcola il tempo impiegato in millisecondi
                double elapsedTimeSeconds = (stopTime - startTime) / 1000.0;
                Time.add(elapsedTimeSeconds);
                System.out.println("Tempo di esecuzione: " + elapsedTimeSeconds + " s");

                //----------CALCOLO ERRORE RELATIVO----------
                //definisco vettore soluzione xe esatta di modo che xe = [1,1,....,1]
                DMatrixSparseCSC xe  = new DMatrixSparseCSC (n,1);
                for(int z = 0; z < n; z++){
                    xe.set(z,0,1);
                }

                double norm_x = NormOps_DSCC.normF(x);
                double norm_xe = NormOps_DSCC.normF(xe);
                double norm_diff = norm_x - norm_xe;
                double relative_error = norm_diff / norm_xe;
                //System.out.println("Norma di x: " + norm_x);
                //System.out.println("Norma di xe: " + norm_xe);
                Error.add(relative_error);
                System.out.println("Errore relativo: " + relative_error);
                System.out.println("\n");
            }
        }
        //----------FINE CICLO FOR----------
        System.out.println("\n----------------------------------------------------------------------");

        //----------SCRITTURA FILE CSV----------
        write("src/main/java/org/BDD/Dati_Java.csv", MatrixName, Size, MemoryPre, MemoryPost, MemoryDiff, Time, Error);
        System.out.println("\nFile CSV creato");


        // importazione della matrice sparsa simmetrica e definita positiva A
        /*Sparse value = Mat5.readFromFile("src/main/java/org/BDD/Matrici/ex15.mat")
                .getStruct("Problem")
                .getSparse("A");

        DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
        A = Mat5Ejml.convert(value, A);
        A.nz_length = value.getNumNonZero();*/
       }

    public static void write(String filePath, ArrayList<String> MatrixName, ArrayList<Long> Size, ArrayList<Long> MemoryPre,
                             ArrayList<Long> MemoryPost, ArrayList<Long> MemoryDiff, ArrayList<Double> Time,
                             ArrayList<Double> Error) throws IOException {
        FileWriter outputFile = new FileWriter(filePath);
        CSVWriter writer = new CSVWriter(outputFile);

        String[] header = {"MatrixName", "Size", "MemoryPre", "MemoryPost", "MemoryDiff", "Time", "Error"};
        writer.writeNext(header, false);

        int n = MatrixName.size();
        for (int i = 0; i < n; i++) {
            String[] row = {MatrixName.get(i), Size.get(i).toString(), MemoryPre.get(i).toString(),
                    MemoryPost.get(i).toString(), MemoryDiff.get(i).toString(),
                    Time.get(i).toString(), Error.get(i).toString()};
            writer.writeNext(row, false);
        }

        writer.close();
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
