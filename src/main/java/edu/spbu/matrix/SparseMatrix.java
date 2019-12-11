package edu.spbu.matrix;

import java.awt.*;
import java.io.*;
import java.util.HashMap;

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
  @Override public Matrix mul(Matrix o)
  {
    if(o instanceof SparseMatrix)
    {
      return mul((SparseMatrix)o);
    }
    else if(o instanceof DenseMatrix)
    {
      return mul((DenseMatrix)o);
    }
    else throw new RuntimeException("time to cry");
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

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
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
