package hpc.csr;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class SparseMatrixCSR {
    private final double[] val;       
    private final int[] colInd;      
    private final int[] rowPtr;      
    private final int numRows;
    private final int numCols;

    public SparseMatrixCSR(double[] val, int[] colInd, int[] rowPtr, int numRows, int numCols) {
        this.val = val;
        this.colInd = colInd;
        this.rowPtr = rowPtr;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public double[] multiplyParallel(double[] x) {
        if (x.length != numCols) {
            throw new IllegalArgumentException("Dimensiuni incompatibile pentru inmultire.");
        }
        double[] y = new double[numRows];
        
        // Folosim ForkJoinPool-ul nativ optimizat pentru arhitectura hardware curenta
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.invoke(new CSRMultiplyTask(0, numRows, x, y));
        
        return y;
    }

    // Task recursiv ForkJoinPool pentru optimizarea cache-ului procesorului
    private class CSRMultiplyTask extends RecursiveAction {
        private static final int THRESHOLD = 500_000; // Dimensiunea unui chunk de randuri
        private final int startRow;
        private final int endRow;
        private final double[] x;
        private final double[] y;

        CSRMultiplyTask(int startRow, int endRow, double[] x, double[] y) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.x = x;
            this.y = y;
        }

        @Override
        protected void compute() {
            if ((endRow - startRow) <= THRESHOLD) {
                // Calcul secvențial pe bucată cache-friendly
                for (int i = startRow; i < endRow; i++) {
                    double sum = 0.0;
                    int rowStart = rowPtr[i];
                    int rowEnd = rowPtr[i + 1];
                    for (int j = rowStart; j < rowEnd; j++) {
                        sum += val[j] * x[colInd[j]];
                    }
                    y[i] = sum;
                }
            } else {
                // Împărțire recursivă a spațiului de stări (Divide et Impera)
                int mid = startRow + (endRow - startRow) / 2;
                invokeAll(
                    new CSRMultiplyTask(startRow, mid, x, y),
                    new CSRMultiplyTask(mid, endRow, x, y)
                );
            }
        }
    }

    public int getNumRows() { return numRows; }
    public int getNumCols() { return numCols; }
}
