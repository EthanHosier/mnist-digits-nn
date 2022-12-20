package com.company;

public class MnistMatrix {

    private double [][] data;

    private int nRows;
    private int nCols;

    private int label;

    public MnistMatrix(int nRows, int nCols) {
        this.nRows = nRows;
        this.nCols = nCols;

        data = new double[nRows][nCols];
    }

    public double getValue(int y, int x) {
        return data[y][x];
    }

    public void setValue(int row, int col, double value) {
        //value is between 0 and 255 but we want between 0.0 and 1.0
        data[row][col] = value;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getNumberOfRows() {
        return nRows;
    }

    public int getNumberOfColumns() {
        return nCols;
    }

    public void printMatrix(){
        System.out.println("Number of columns: " + getNumberOfColumns());
        System.out.println("Number of rows: " + getNumberOfRows());

        //y
        for (int i = 0; i < getNumberOfRows(); i ++ ){
            //x
            for (int j = 0; j < getNumberOfColumns(); j ++){
                System.out.print(getValue(i,j) + " ");
            }
            System.out.println();
        }

        System.out.println("Label: " + getLabel());
    }
}

