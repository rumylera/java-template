package edu.spbu.matrix;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatrixTest
{
  /**
   * ожидается 4 таких теста
   */
  @Test
  public void mulDD() {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix expected = new DenseMatrix("result.txt");
    assertEquals(expected, m1.mul(m2));
  }
  @Test
  public void NulDD(){
    Matrix m1 = new DenseMatrix(0,0);
    Matrix m2 = new DenseMatrix(0,0);
    Matrix expected = m2.mul(m1);
    assertEquals(0, expected.getCols());
    assertEquals(0, expected.getRows());
  }
  @Test
  public void mulSS() {
    Matrix m1 = new SparseMatrix("sm1.txt");
    Matrix m2 = new SparseMatrix("sm2.txt");
    Matrix expected = new SparseMatrix("sresult.txt");
    Matrix actual = m1.mul(m2);
    assertEquals(expected, actual);
  }
  @Test
  public void mulDS() {
    Matrix m1 = new SparseMatrix("m1.txt");
    Matrix m2 = new SparseMatrix("sm2.txt");
    Matrix expected = new SparseMatrix("dsresult.txt");
    Matrix actual = m1.mul(m2);
    assertEquals(expected, actual);

  }
  @Test
  public void mulSD() {
    Matrix m1 = new SparseMatrix("sm1.txt");
    Matrix m2 = new SparseMatrix("m2.txt");
    Matrix expected = new SparseMatrix("sdresult.txt");
    Matrix actual = m1.mul(m2);
    assertEquals(expected, actual);
  }
}
