package hpc.csr;

import java.util.Arrays;

public class MainApp {
    public static void main(String[] args) {
        System.out.println("=== Pornire Motor Computational Java HPC QFLPN (ForkJoinPool) ===");
        
        int numRows = 30_000_000; 
        int numCols = numRows;
        int nnzPerRow = 2;
        long totalNnz = (long) numRows * nnzPerRow;
        
        System.out.println("Configuratie: " + numRows + " randuri x " + numCols + " coloane.");
        System.out.println("Total elemente nenule (NNZ) in matricea CSR: " + totalNnz);

        System.gc(); // Solicitare curățare Heap

        double[] val = new double[(int) totalNnz];
        int[] colInd = new int[(int) totalNnz];
        int[] rowPtr = new int[numRows + 1];

        int index = 0;
        for (int i = 0; i < numRows; i++) {
            rowPtr[i] = index;
            val[index] = 0.95; colInd[index] = i; index++;
            val[index] = 0.31; colInd[index] = (i + 1) % numCols; index++;
        }
        rowPtr[numRows] = index;

        double[] x = new double[numCols];
        Arrays.fill(x, 1.0 / Math.sqrt(numCols));

        try {
            SparseMatrixCSR matrix = new SparseMatrixCSR(val, colInd, rowPtr, numRows, numCols);
            
            System.out.println("Se executa faza de warm-up standard a JVM...");
            for (int i = 0; i < 2; i++) {
                matrix.multiplyParallel(x);
            }
            
            System.out.println("Warm-up complet. Pornire test oficial sub ForkJoinPool...");
            
            long startTime = System.nanoTime();
            double[] y = matrix.multiplyParallel(x);
            long endTime = System.nanoTime();
            
            double durationMs = (endTime - startTime) / 1_000_000.0;

            System.out.println("\n------------------------------------------------");
            System.out.println("📊 REZULTAT BENCHMARK GITHUB ACTIONS (JDK 21):");
            System.out.printf("Timp total de executie: %.2f ms\n", durationMs);
            System.out.printf("Validare vector rezultat (Amplitudine i=0): %.6f\n", y[0]);
            System.out.println("------------------------------------------------");

        } catch (Exception e) {
            System.err.println("Eroare in timpul executiei: " + e.getMessage());
        }
    }
}
