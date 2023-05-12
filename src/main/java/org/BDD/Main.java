package org.BDD;

import com.opencsv.CSVWriter;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.CommonOps_MT_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.NormOps_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Sparse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws IOException {

        ArrayList<String> MatrixName = new ArrayList<>();
        ArrayList<Long> Size = new ArrayList<>();
        ArrayList<Long> MemoryPre = new ArrayList<>();
        ArrayList<Long> MemoryPost = new ArrayList<>();
        ArrayList<Long> MemoryDiff = new ArrayList<>();
        ArrayList<Double> Time = new ArrayList<>();
        ArrayList<Double> Error = new ArrayList<>();

        // Percorso della cartella contenente le matrici
        String path = "src/main/java/org/BDD/Matrici/";
        File matriciFolder = new File(path);
        // Lista di tutti i file nella cartella Matrici
        File[] files = matriciFolder.listFiles();

        //----------CICLO CHE LEGGE TUTTE LE MATRICI DELLA CARTELLA "Matrici"----------
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("-----------------------------------" + "Elaborazione della matrice " + file.getName() + "-----------------------------------");

                // Importazione della matrice sparsa simmetrica e definita positiva A
                Sparse value = Mat5.readFromFile(file.getAbsolutePath())
                        .getStruct("Problem")
                        .getSparse("A");

                DMatrixSparseCSC A = new DMatrixSparseCSC(value.getNumRows(), value.getNumCols());
                A = Mat5Ejml.convert(value, A);
                A.nz_length = value.getNumNonZero();

                System.out.println("Dimensioni matrice: " + A.numRows + " " + A.numCols);
                System.out.println("Numero di elementi: " + A.getNumElements());
                System.out.println("Numero di elementi non nulli: " + A.nz_length);

                File matriceA = new File("src/main/java/org/BDD/Matrici/" + file.getName());
                long dimensionA = matriceA.length();
                Size.add(dimensionA);
                System.out.println("Dimensioni matrice A: " + dimensionA / (1024 * 1024) + " MB.");

                System.out.println("\n---> Inizio elaborazione matrice " + file.getName() + " \n");
                MatrixName.add(file.getName());


                //----------CONTROLLO CHE LA MATRICE SIA DEFINITA POSITIVA E SIMMETRICA----------
                if (MatrixFeatures_DSCC.isPositiveDefinite(A)) {
                    System.out.println("La matrice A è definita positiva");
                } else {
                    System.out.println("La matrice A non è definita positiva");
                    //se si arriva qui va lanciata un eccezione throw new RuntimeException("La matrice A non è definita positiva");
                }

                if (MatrixFeatures_DSCC.isSymmetric(A, 1e-8)) {
                    System.out.println("La matrice A è simmetrica");
                } else {
                    System.out.println("La matrice A non è simmetrica");
                    //se si arriva qui va lanciata un eccezione throw new RuntimeException("La matrice A non è simmetrica");
                }

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
                DMatrixSparseCSC tmp = new DMatrixSparseCSC(n, 1);
                for (int j = 0; j < n; j++) {
                    tmp.set(j, 0, 1);
                }

                //moltiplicazione tra il vettore di tutti 1 tmp e la matrice A, il risultato viene salvato in B
                DMatrixSparseCSC B = CommonOps_MT_DSCC.mult(A, tmp, null); //B = A*tmp eseguito in mmulti-thread

                //Crea il vettore x
                DMatrixSparseCSC x = new DMatrixSparseCSC(n, 1);   //x è un vettore colonna con tutti gli elementi uguali a 0


                long startTime = System.currentTimeMillis(); //registra il tempo d'inizio

                LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
                solver.setA(A);
                solver.solveSparse(B, x);

                System.out.println("Sistema risolto");

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
                DMatrixSparseCSC xe = new DMatrixSparseCSC(n, 1);
                for (int z = 0; z < n; z++) {
                    xe.set(z, 0, 1);
                }

                //calcolo norma || x -xe||
                DMatrixSparseCSC x_diff_xe = new DMatrixSparseCSC(n, 1);
                x_diff_xe = CommonOps_DSCC.add(1, x, -1, xe, x_diff_xe, null, null);
                double norm_diff = NormOps_DSCC.normF(x_diff_xe);
                //calcolo norma || xe||
                double norm_xe = NormOps_DSCC.normF(xe);
                //calcolo errore relativo
                double relative_error = norm_diff / norm_xe;

                Error.add(relative_error);
                System.out.println("Errore relativo: " + relative_error);
                System.out.println("\n");
            }
        }
        //----------FINE CICLO FOR----------
        System.out.println("\n----------------------------------------------------------------------");

        //----------SCRITTURA FILE CSV----------
        write("src/main/java/org/BDD/dati_java.csv", MatrixName, Size, MemoryPre, MemoryPost, MemoryDiff, Time, Error);
        System.out.println("\nFile CSV creato");
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
}
