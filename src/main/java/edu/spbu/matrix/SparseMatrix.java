package edu.spbu.matrix;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
      cols = len;
      rows = h;
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
      if(this.cols != ((DenseMatrix)o).rows) {
        throw new RuntimeException("Mistake 2");
      }
      HashMap<Point, Double> res = new HashMap<>();
      SparseMatrix result = new SparseMatrix(res, this.rows, ((DenseMatrix)o).cols);
      DenseMatrix transpDM = ((DenseMatrix) o).transpose();
      for(Point key: SpMat.keySet()) {
        for(int i = 0; i < transpDM.rows; i++) {
          if(transpDM.denseMatrix[i][key.y] != 0) {
            Point pf = new Point(key.x, i);
            if(result.SpMat.containsKey(pf)) {
              double tmp = result.SpMat.get(pf) + SpMat.get(key) * transpDM.denseMatrix[i][key.y];
              result.SpMat.put(pf, tmp);
            }
            else {
              double tmp = SpMat.get(key) * transpDM.denseMatrix[i][key.y];
              result.SpMat.put(pf, tmp);
            }
          }
        }
      }
      return result;
    }
    return null;
  }
    @Override
    public int hashCode() {
    int result = Objects.hash(rows, cols);
    for(Point key: SpMat.keySet()){
      result += 31 + (SpMat.get(key).hashCode()<<2);
    }
    return result;
  }



  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  class MultiSparse implements Runnable {

      int start, step;
      SparseMatrix left, right, result;

      MultiSparse(int begin, int incr, SparseMatrix left, SparseMatrix right, SparseMatrix result) {
          this.start = begin;
          this.step = incr;
          this.left = left;
          this.right = right;
          this.result = result;
      }

      @Override
      public void run(){
          for(Point key: left.SpMat.keySet()) {
              for (int i = start; i < start + step; i++) {
                  Point p1 = new Point(i, key.y);
                  if (right.SpMat.containsKey(p1)) {
                      Point p2 = new Point(key.x, i);
                      result.refresh(p2, key, p1, result, left, right);
                  }
              }
          }
      }
  }
  synchronized public void refresh(Point p2, Point key, Point p1, SparseMatrix result, SparseMatrix left, SparseMatrix right) {
        if(result.SpMat.containsKey(p2)){
            double t = result.SpMat.get(p2) + left.SpMat.get(key) * right.SpMat.get(p1);
            result.SpMat.put(p2, t);
        }
        else{
            double t = left.SpMat.get(key) * right.SpMat.get(p1);
            result.SpMat.put(p2, t);
        }
  }
  @Override public Matrix dmul(Matrix o)
  {
      if(o instanceof SparseMatrix){
          if (this.cols != ((SparseMatrix)o).rows) {
              throw new RuntimeException("Mistake 1");
          }
          HashMap<Point, Double> res = new HashMap<>();
          SparseMatrix result = new SparseMatrix(res, this.rows, ((SparseMatrix)o).cols);
          SparseMatrix transpSM = ((SparseMatrix) o).transpose();
          int thrds = Runtime.getRuntime().availableProcessors();
          int step = transpSM.rows/thrds + 1;
          ArrayList<Thread> threads = new ArrayList<>();
          for(int i=0; i<transpSM.rows; i+=step){
              MultiSparse multiSparse = new MultiSparse( i, step,this, transpSM, result) ;
              Thread t = new Thread(multiSparse);
              threads.add(t);
              t.start();
          }
          for(Thread th:threads){
              try {
                  th.join();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          return (result);
      }
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

