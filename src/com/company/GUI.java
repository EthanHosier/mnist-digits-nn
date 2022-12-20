package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class GUI extends JFrame implements ActionListener{

    private final int frameWidth = 950;
    private final int frameHeight = 800;
    private Grid grid;

    private NeuralNetwork nn;
    private MnistMatrix[] exampleImages;

    private JLabel answerLabel;
    private JLabel[] outputLabels;

    public GUI() throws IOException, URISyntaxException {

        outputLabels = new JLabel[10];

        nn = FileHandler.loadNetwork("NNSaveFile19Epochs.txt");
        exampleImages = new MnistDataReader().readData("train-images.idx3-ubyte", "train-labels.idx1-ubyte");


        this.setLayout(null);
        this.setResizable(false);
        this.setSize(frameWidth,frameHeight);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        grid = new Grid(28,700);
        this.add(grid);

        //get centre screen position:
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frameWidth) / 2);
        int y = (int) ((dimension.getHeight() - frameHeight) / 2);
        this.setLocation(x, y);

        this.add(new JButton("Clear Grid"){{
            this.setBounds(750,30,150,70);
            this.setFocusable(false);
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    grid.clearGrid();
                }
            });
        }});

        this.setVisible(true);

        this.add(new JButton("Random Example"){{
            this.setBounds(750,120,150,70);
            this.setFocusable(false);
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    grid.setAs(exampleImages[ThreadLocalRandom.current().nextInt(0, 50000 + 1)]);
                }
            });
        }});


        this.add(new JButton("Get Number"){{
            this.setBounds(750,210,150,70);
            this.setFocusable(false);
            this.addActionListener(GUI.this::actionPerformed);
        }});


        answerLabel = new JLabel(){{
            this.setFont(new Font("Sarif Sans", Font.BOLD, 50));
            this.setBounds(800,315,50,50);
            this.setForeground(Color.green);
        }};
        this.add(answerLabel);

        //outputLabels
        for (int i = 0; i < outputLabels.length; i ++){
            int finalI = i;
            outputLabels[i] = new JLabel(){{
                this.setBounds(720,388 + 32 * finalI, 200,30 );
                this.setFont(new Font("Sarif Sans", Font.BOLD,18));
                GUI.this.add(this);
            }};
        }


    }

    @Override
    public void actionPerformed(ActionEvent e) {

        DecimalFormat df = new DecimalFormat("#.##########");

        MnistMatrix matrix = array2DToMnistMatrix(grid.getGridArray());
        nn.feedData(matrix);
        nn.forwardPropagate();
        double[] outputs = nn.getAllOutputs();

        for (int i = 0; i < outputs.length; i ++){
            outputLabels[i].setText(i + ": " + df.format(outputs[i]));
            outputLabels[i].setForeground(Color.BLACK);
        }

        //set correct to green
        outputLabels[nn.getAnswer()].setForeground(Color.GREEN);
        answerLabel.setText(Integer.toString(nn.getAnswer()));



    }

    private MnistMatrix array2DToMnistMatrix(int[][] array){
        MnistMatrix mnistMatrix = new MnistMatrix(array.length, array[0].length);

        for (int i = 0; i < array.length; i ++){
            for (int j = 0; j < array[i].length; j++){
                mnistMatrix.setValue(i,j,array[i][j]);
            }
        }

        return mnistMatrix;

    }
}
