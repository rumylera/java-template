package edu.spbu.matrix;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix {
  int rows, cols;
  double[][] denseMatrix;

  public DenseMatrix(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.denseMatrix = new double[rows][cols];
  }


   /**
    * @param fileName
    */
  public DenseMatrix(String fileName) {
    ArrayList<double[]> tmp = new ArrayList<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
      String line = br.readLine();
      if (line == null) {
        throw new NullPointerException("пустая матрица");
      }
      String[] values = line.split(" ");
      this.cols = values.length;
      double[] array = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();
      tmp.add(array);
      this.rows = 1;
      while ((line = br.readLine()) != null) {
        values = line.split(" ");
        array = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();
        tmp.add(array);
        this.rows++;
      }
      this.denseMatrix = new double[this.rows][this.cols];
      for (int i = 0; i < this.rows; i++) {
        denseMatrix[i] = tmp.get(i);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   * @param o
   * @return
   */
  @Override
  public Matrix mul(Matrix o) {
    if (o instanceof DenseMatrix) {
      DenseMatrix m2 = (DenseMatrix) o;
      int resRows = this.getRows();
      int resCols = m2.getCols();
      DenseMatrix res = new DenseMatrix(resRows, resCols);
      for (int i = 0; i < resRows; i++) {
        for (int j = 0; j < resCols; j++) {
          for (int k = 0; k < this.cols; k++) {
            res.denseMatrix[i][j] += this.denseMatrix[i][k] * m2.denseMatrix[k][j];
          }
        }
      }
      return res;
    }
    if(o instanceof SparseMatrix){
      if(this.cols != ((SparseMatrix)o).rows) {
        throw new RuntimeException("Mistake 1");
      }
      HashMap<Point, Double> res = new HashMap<>();
      SparseMatrix result = new SparseMatrix(res, this.rows, ((SparseMatrix)o).cols);
      SparseMatrix transpSM = ((SparseMatrix) o).transpose();
      for(Point key: transpSM.SpMat.keySet()) {
        for (int i = 0; i < this.rows; i++) {
          if(denseMatrix[i][key.y] != 0) {
            Point pf = new Point(i, key.x);
            if(result.SpMat.containsKey(pf)) {
              double tmp = result.SpMat.get(pf) + denseMatrix[i][key.y] * transpSM.SpMat.get(key);
              result.SpMat.put(pf, tmp);
            }
            else{
              double tmp = denseMatrix[i][key.y] * transpSM.SpMat.get(key);
              result.SpMat.put(pf, tmp);
            }
          }
        }
      }
      return result;
    }
    return null;
  }
  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  class MultiDense implements Runnable{
      int start, end, rows, cols;
      DenseMatrix right, left,result;

      MultiDense(int begin, int fin, int cols, DenseMatrix right, DenseMatrix left, DenseMatrix result){
          this.start = begin;
          this.end = fin;
          this.rows = fin - begin;
          this.cols = cols;
          this.right = right;
          this.left = left;
          this.result = result;
      }

      @Override
      public void run(){
          for(int i=start;i<end;i++) {
              for (int j = 0; j < result.cols; j++) {
                  for (int k = 0; k < this.cols; k++) {
                      result.denseMatrix[i][j] += left.denseMatrix[i][k] * right.denseMatrix[j][k];
                  }
              }
          }
      }

  }
  @Override
  public Matrix dmul (Matrix o){
      if(o instanceof DenseMatrix){
          if(this.cols != ((DenseMatrix) o).rows){
              throw new RuntimeException("Mistake 1");
          }
          DenseMatrix result = new DenseMatrix(this.rows, ((DenseMatrix) o).cols);
          DenseMatrix transpDM = ((DenseMatrix) o).transpose();
          int thrds = Runtime.getRuntime().availableProcessors();
          Thread[] threads = new Thread[thrds];
          int r = rows%thrds;
          for(int i=0;i<thrds;i++){
              int j=rows/thrds;
              MultiDense multiDense;
              if(i == r && i != 0) {
                  multiDense = new MultiDense(i * j, j * (i + 1) + r, cols, transpDM,this, result);
              }
              else {
                  multiDense = new MultiDense(i * j, j * (i + 1), cols, transpDM,this, result);
              }
              threads[i]= new Thread(multiDense);
              threads[i].start();
          }
          for(int i=0;i<thrds;i++){
              try {
                  threads[i].join();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }

          return(result);
      }

      return null;
  }
  /**
   * спавнивает с обоими вариантами
   *
   * @param o
   * @return
   */
    @Override
    public boolean equals (Object o){
      if (o == this) {
        return true;
      }
      if (o instanceof DenseMatrix) {
        DenseMatrix m = (DenseMatrix) o;
        if (this.rows != m.rows || this.cols != m.cols) {
          return false;
        } else {
          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              if (this.denseMatrix[i][j] != m.denseMatrix[i][j]) {
                return false;
              }
            }
          }
          return true;
        }
      }
      return false;
    }
    @Override
    public String toString () {
      StringBuilder str = new StringBuilder();
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          str.append(denseMatrix[i][j]);
          str.append(" ");
        }
        str.append("\n");
      }
      return (str.toString());
    }

    public int getCols () {
      return this.cols;
    }

    public int getRows () {
      return this.rows;
    }

    public DenseMatrix transpose () {
      DenseMatrix transp = new DenseMatrix(cols, rows);
      for (int i = 0; i < cols; i++) {
        for (int j = 0; j < rows; j++) {
          transp.denseMatrix[i][j] = denseMatrix[j][i];
        }
      }
      return transp;
    }

  @Override
  public int hashCode () {
    int result = Objects.hash(rows, cols);
    result = 31 * result + Arrays.deepHashCode(denseMatrix);
    return result;
  }

}