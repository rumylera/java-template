package edu.spbu.matrix;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

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
      return this.mul((DenseMatrix) o);
    }
    else if (o instanceof SparseMatrix) {
      return this.mul((SparseMatrix) o);
    }
    else throw new RuntimeException("time to cry");

  }

  public DenseMatrix mul(DenseMatrix DMat) {
    int resRows = this.getRows();
    int resCols = DMat.getCols();
    DenseMatrix res = new DenseMatrix(resRows, resCols);
    for (int i = 0; i < resRows; i++) {
      for (int j = 0; j < resCols; j++) {
        for (int k = 0; k < this.cols; k++) {
          res.denseMatrix[i][j] += this.denseMatrix[i][k] * DMat.denseMatrix[k][j];
        }
      }
    }
    return res;
  }

  public DenseMatrix mul(SparseMatrix SMat) {
    if(cols == 0 || rows == 0 || SMat.cols == 0 || SMat.rows == 0){
      return null;
    }
    else if(cols == SMat.rows) {
      int resRows = rows;
      int resCols = SMat.cols;
      DenseMatrix res = new DenseMatrix(resRows, resCols);
      for(int i = 0; i < resRows; i++ ) {
        for (Point p : SMat.SpMat.keySet()) {
          for (int k = 0; k < resRows; k++) {
            if (p.x == k) {
              res.denseMatrix[i][p.y] += denseMatrix[i][k] * SMat.SpMat.get(p);
            }
          }
        }
      }
    return res;
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override
  public Matrix dmul(Matrix o) {
    return null;
  }
  /**
   * спавнивает с обоими вариантами
   *
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if(o instanceof DenseMatrix){
      DenseMatrix m = (DenseMatrix) o;
      if(this.rows != m.rows || this.cols != m.cols ){
        return false;
      }
      else {
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
  public String toString() {
    StringBuilder str = new StringBuilder();
    for(int i=0; i<rows;i++) {
      for (int j = 0; j < cols; j++) {
        str.append(denseMatrix[i][j]);
        str.append(" ");
      }
      str.append("\n");
    }
    return (str.toString());
  }

  public int getCols(){
    return this.cols;
  }

  public int getRows(){
    return this.rows;
  }

  public DenseMatrix transpose() {
    DenseMatrix transp = new DenseMatrix(cols, rows);
    for (int i = 0; i < cols; i++) {
      for (int j = 0; j < rows; j++) {
        transp.denseMatrix[i][j] = denseMatrix[j][i];
      }
    }
    return transp;
  }


}