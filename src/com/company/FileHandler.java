package com.company;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

public class FileHandler implements Serializable {

    public static void saveNetwork(Object obj, String file){
        //saves obj to file location
        File f = new File(file);

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(obj);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static NeuralNetwork loadNetwork(String url){

        try {
            java.net.URL saveFileURL = Main.class.getClassLoader().getResource(url);
            ObjectInputStream inputStream = new ObjectInputStream(saveFileURL.openStream());
            NeuralNetwork nn = (NeuralNetwork) inputStream.readObject();
            inputStream.close();
            return nn;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
