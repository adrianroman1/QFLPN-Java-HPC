import java.util.Arrays;

public class Main {
    // Algoritmul de Multiplicare Sparse Matrice-Vector (SpMV) în format CSR
    public static double[] multiplyCSR(double[] val, int[] colInd, int[] rowPtr, double[] x, int numRows) {
        double[] y = new double[numRows];
        
        // Paralelizare pe nuclee computaționale (HPC multi-threading nativ)
        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        int rowsPerThread = numRows / threads.length;

        for (int t = 0; t < threads.length; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == threads.length - 1) ? numRows : (t + 1) * rowsPerThread;

            threads[t] = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    double sum = 0.0;
                    int rowStart = rowPtr[i];
                    int rowEnd = rowPtr[i + 1];
                    for (int k = rowStart; k < rowEnd; k++) {
                        sum += val[k] * x[colInd[k]];
                    }
                    y[i] = sum;
                }
            });
            threads[t].start();
        }

        // Sincronizarea firelor de execuție active
        for (Thread thread : threads) {
            try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        return y;
    }

    public static void main(String[] args) {
        System.out.println("=== Pornire Motor Computațional Java HPC QFLPN ===");
        
        // Dimensiunea țintă: 3 * 10^7 stări
        int numRows = 30000000; 
        
        // Date de test Sparse (3 elemente nenule în tot sistemul gigantic)
        double[] val = {0.85, 0.90, -0.63};
        int[] colInd = {0, 1024, 29999999};
        int[] rowPtr = new int[numRows + 1];
        
        // Configurare vector de pointeri CSR
        Arrays.fill(rowPtr, 0);
        rowPtr[1] = 1;
        rowPtr[2] = 2;
        for(int i = 3; i <= numRows; i++) {
            rowPtr[i] = 3;
        }

        double[] x = new double[numRows];
        x[0] = 1.0; // Starea inițială de propagare

        long startTime = System.nanoTime();
        double[] rezultate = multiplyCSR(val, colInd, rowPtr, x, numRows);
        long endTime = System.nanoTime();

        double timpExecutieMs = (endTime - startTime) / 1e6;
        System.out.println("Multiplicare CSR finalizată cu succes!");
        System.out.printf("Timp total de răspuns nucleu Java HPC: %.2f ms\n", timpExecutieMs);
    }
  }
