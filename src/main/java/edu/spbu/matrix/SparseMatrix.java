package edu.spbu.matrix;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Objects;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{
  public HashMap<Point, Double> SpMat;
  public int cols, rows;


  /**
   * загружает матрицу из файла
   * @param fileName
   */
  public SparseMatrix(String fileName) {
    try{
      BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
      SpMat = new HashMap<>();
      String[] curr;
      String line = br.readLine();
      int len = 0;
      int h = 0;
      double elem;
      while(line!= null){
        curr = line.split(" ");
        len = curr.length;
        for(int i = 0; i<len; i++){
          if(!curr[0].isEmpty()){
            elem = Double.parseDouble(curr[i]);
            if(elem!=0){
              Point p = new Point(h, i);
              SpMat.put(p, elem);
            }
          }
        }
        h++;
        line = br.readLine();
      }
      cols = h;
      rows = len;
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public SparseMatrix(HashMap<Point, Double> SpMat, int rows, int cols)
  {
    this.SpMat = SpMat;
    this.rows = rows;
    this.cols = cols;
  }

  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   * @param o
   * @return
   */
  @Override public Matrix mul(Matrix o) {
    if (o instanceof SparseMatrix) {
      if (this.cols != ((SparseMatrix)o).rows) {
        throw new RuntimeException("Mistake 1");
      }
      HashMap<Point, Double> res = new HashMap<>();
      SparseMatrix result = new SparseMatrix(res, this.rows, ((SparseMatrix)o).cols);
      SparseMatrix transpSM = ((SparseMatrix) o).transpose();

      for (Point key : SpMat.keySet()) {
        for (int i = 0; i < transpSM.rows; i++) {
          Point p1 = new Point(i, key.y);
          if (transpSM.SpMat.containsKey(p1)) {
            Point p2 = new Point(key.x, i);
            if (result.SpMat.containsKey(p2)) {
              double t = result.SpMat.get(p2) + SpMat.get(key) * transpSM.SpMat.get(p1);
              result.SpMat.put(p2, t);
            } else {
              double t = SpMat.get(key) * transpSM.SpMat.get(p1);
              result.SpMat.put(p2, t);
            }
          }
        }
      }
      return result;
    }
    else if (o instanceof DenseMatrix) {
      DenseMatrix DMat = (DenseMatrix) o;
      if (this.cols == DMat.cols && this.SpMat != null && DMat.denseMatrix != null) {
        double[][] res = new double[this.rows][DMat.cols];
        DenseMatrix transpDM = DMat.transpose();
        for (Point p : this.SpMat.keySet()) {
          for (int j = 0; j < transpDM.rows; j++) {
            for (int k = 0; k < this.cols; k++) {
              if (p.y == k) {
                res[p.x][j] += this.SpMat.get(p) * transpDM.denseMatrix[j][k];
              }
            }
          }
        }
        DenseMatrix result = new DenseMatrix(this.rows, DMat.cols);
        result.denseMatrix = res;
        return result;
      } else throw new RuntimeException("time to cry");
    } else throw new RuntimeException("time to cry");
  }



  public DenseMatrix mul(DenseMatrix DMat){
    if(cols == DMat.cols && SpMat!=null && DMat.denseMatrix!=null)
    {
      double[][] res=new double[rows][DMat.cols];
      DenseMatrix transpDM = DMat.transpose();
      for(Point p: SpMat.keySet())
      {
        for(int j = 0; j < transpDM.rows; j++)
        {
          for(int k = 0; k < cols; k++)
          {
            if(p.y == k)
            {
              res[p.x][j]+=SpMat.get(p)*transpDM.denseMatrix[j][k];
            }
          }
        }
      }
      DenseMatrix result =  new DenseMatrix(rows, DMat.cols);
      result.denseMatrix = res;
      return result;
    }else throw new RuntimeException("time to cry");
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o)
  {
    return null;
  }

  @Override
  public int getRows() {
    return 0;
  }

  @Override
  public int getCols() {
    return 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rows, cols, SpMat);
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    if(o instanceof DenseMatrix)
    {
      DenseMatrix DMat=(DenseMatrix) o;
      if (SpMat == null || DMat.denseMatrix == null) {
        return false;
      }
      if (rows == DMat.rows && cols == DMat.cols) {
        int actval = 0;
        for(int i = 0; i < DMat.rows; i++)
        {
          for(int j = 0; j < DMat.cols; j++)
          {
            if(DMat.denseMatrix[i][j] != 0)
            {
              actval++;
            }
          }
        }
        if(actval!=SpMat.size()) {
          return false;
        }
        for (Point key: SpMat.keySet()) {
          if(DMat.denseMatrix[key.x][key.y]==0) {
            return false;
          }
          if (DMat.denseMatrix[key.x][key.y]!=SpMat.get(key)) {
            return false;
          }
        }
        return true;
      }
    }
    else if(o instanceof SparseMatrix)
    {
      SparseMatrix SMat = (SparseMatrix)o;
      if (SpMat == null || SMat.SpMat == null) {
        return false;
      }
      if (SMat.SpMat == SpMat) {
        return true;
      }
      if (this.hashCode() != SMat.hashCode()) {
        return false;
      }
      if (rows != SMat.rows || cols != SMat.cols) {
        return false;
      }
      if (SpMat.size()!=SMat.SpMat.size()) {
        return false;
      }
      for (Point p:SpMat.keySet()) {
        if (SpMat.get(p)-(SMat.SpMat.get(p))!=0) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public SparseMatrix transpose()
  {
    HashMap<Point,Double> transp =new HashMap<>();
    Point p;
    for(Point key:SpMat.keySet())
    {
      p=new Point(key.y, key.x);
      transp.put(p, SpMat.get(key));
    }
    return new SparseMatrix(transp, rows, cols);
  }

}
