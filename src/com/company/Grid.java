package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

//square grid
public class Grid extends JPanel {

    private JLabel[][] squares;
    private int[][] gridArray;
    private boolean drawable;

    public Grid(int rows, int panelSideLengthInPixels){

        drawable = true;

        squares = new JLabel[rows][rows];
        gridArray = new int[rows][rows];

        this.setBounds(1,1,panelSideLengthInPixels,panelSideLengthInPixels);
        this.setLayout(new GridLayout(28,28,1,1));
        this.setBackground(new Color(0xE2E1E1));



        //initiate empty grid for start
        for (int i = 0; i < gridArray.length; i ++){
            for (int j = 0; j < gridArray[i].length; j ++) {
                gridArray[i][j] = 0;
                squares[i][j] = new JLabel(){
                    { this.setText("0");
                    this.setFont(new Font("Serif Sans",Font.BOLD,14));
                    this.setForeground(new Color(0x05F609));
                    this.setSize(panelSideLengthInPixels/rows - 2, panelSideLengthInPixels/rows - 2 );
                    this.setBackground(Color.white);
                    this.setOpaque(true);
                }};
                this.add(squares[i][j]);
            }
        }


        //mouse listener for dragging mouse
        this.addMouseMotionListener(new MouseMotionListener() {


            int xPosition;
            int yPosition;

            @Override
            public void mouseDragged(MouseEvent e) {

                //if grid is not enabled for drawing, method exited straight away
                if (!drawable){
                    return;
                }

                yPosition = (int) Math.floor((e.getY())/ (panelSideLengthInPixels/rows));
                xPosition = (int) Math.floor((e.getX())/ (panelSideLengthInPixels/rows));

                //method exited if location clicked outside of grid
                if (!((xPosition < rows && xPosition >= 0) && (yPosition < rows && yPosition >= 0))){
                    return;
                }

                    //left click = black
                if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {

                    paint(255, xPosition, yPosition);
                    return;

                }
                    //right click = white
                if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {

                    paint(0, xPosition, yPosition);

                    return;

                    }



            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        });

        //mouse listener for clicking mouse
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                //if grid is not enabled for drawing, method exited straight away
                if (!drawable){
                    return;
                }


                int yPosition = (int) Math.floor((e.getY())/ (panelSideLengthInPixels/rows));
                int xPosition = (int) Math.floor((e.getX())/ (panelSideLengthInPixels/rows));

                //method exited if location clicked outside of grid
                if (!((xPosition < rows && xPosition >= 0) && (yPosition < rows && yPosition >= 0))){
                    return;
                }

                    //left click = black
                if (e.getButton() == 1) {
                    paint(255, xPosition, yPosition);
                    return;
                }

                    //right click = white
                if (e.getButton() == 3) {

                    paint(0, xPosition, yPosition);
                    return;

                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });


    }

    private void updateSquare(int gsVal, int x, int y){

        try {
            gridArray[y][x] = gsVal;
            squares[y][x].setBackground(new Color(255 - gsVal,255 - gsVal,255 - gsVal));
            squares[y][x].setText(Integer.toString(gsVal));
            this.repaint();


        }catch ( Exception ArrayIndexOutOfBoundsException){};

    }


    public int[][] getGridArray(){
        return gridArray;
    }

    public void clearGrid(){
        for (int i = 0; i < gridArray.length; i ++){
            for (int j = 0; j < gridArray[i].length; j ++){
                updateSquare(0,j,i);
            }
        }
    }

    private void paint( int centreVal,int centreX, int centreY) throws ArrayIndexOutOfBoundsException{

        //if white
        if (centreVal == 0) {
            /*
            try {
                updateSquare(centreVal, centreX, centreY);

                updateSquare(clamp(gridArray[centreY][centreX - 1] - 50), centreX - 1, centreY);
                updateSquare(clamp(gridArray[centreY][centreX + 1] - 50), centreX + 1, centreY);

                updateSquare(clamp(gridArray[centreY + 1][centreX] - 50), centreX, centreY + 1);
                updateSquare(clamp(gridArray[centreY - 1][centreX] - 50), centreX, centreY - 1);
            }catch (ArrayIndexOutOfBoundsException e){};
             */

            try {
                updateSquare(0, centreX, centreY);

                updateSquare(0, centreX - 1, centreY);
                updateSquare(0, centreX + 1, centreY);

                updateSquare(0, centreX, centreY + 1);
                updateSquare(0, centreX, centreY - 1);
                
            }catch (ArrayIndexOutOfBoundsException e){};
        } else if (centreVal == 255){

            /*
            try {
                updateSquare(gridArray[centreY][centreX] + 100, centreX,centreY);

                updateSquare(clamp(gridArray[centreY][centreX - 1] + 50), centreX - 1, centreY);
                updateSquare(clamp(gridArray[centreY][centreX + 1] + 50), centreX + 1, centreY);

                updateSquare(clamp(gridArray[centreY + 1][centreX] + 50), centreX, centreY + 1);
                updateSquare(clamp(gridArray[centreY - 1][centreX] + 50), centreX, centreY - 1);
            }catch (ArrayIndexOutOfBoundsException e){};
*/
            try {
                updateSquare(255, centreX,centreY);

                updateSquare(clamp(gridArray[centreY][centreX - 1] + 22), centreX - 1, centreY);
                updateSquare(clamp(gridArray[centreY][centreX + 1] + 22), centreX + 1, centreY);

                updateSquare(clamp(gridArray[centreY + 1][centreX] + 22), centreX, centreY + 1);
                updateSquare(clamp(gridArray[centreY - 1][centreX] + 22), centreX, centreY - 1);
            }catch (ArrayIndexOutOfBoundsException e){};
        }
    }

    private int clamp(int x){
        if (x > 255){
            return 255;
        }

        if (x < 0){
            return 0;
        }
        else{
            return x;
        }
    }

    public void setAs(MnistMatrix matrix){
        for (int i = 0; i < matrix.getNumberOfRows(); i ++){
            for (int j = 0; j < matrix.getNumberOfColumns(); j ++){
                updateSquare((int)matrix.getValue(i,j),j,i);
            }
        }
    }
}
