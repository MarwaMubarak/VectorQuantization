package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class VectorQuantization {
    public static int imageHeight, imageWidth, vectorHeight, vectorWidth, numOfCodebook, compressedHeight, compressedWidth;
    // the original image
    public static float[][] originalImage;
    // the compressed image
    public static String[][] compressedImage;
    // the collected image
    public static float[][] collectedImage;
    // the original blocks of the image
    public static ArrayList<float[][]> originalBlocks = new ArrayList<>();
    // the blocks of the image after operations(spilt&average)
    public static ArrayList<float[][]> blocks = new ArrayList<>();
    // the vector nearest to what of the original vectors of the image
    public static Map<float[][], ArrayList<float[][]>> nearestVectors = new HashMap<>();
    // the codebook
    public static HashMap<String, float[][]> codeBook = new HashMap<>();

    // to read the image
    public static float[][] readImage(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedImage image;
        image = ImageIO.read(file);
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        float[][] pixels = new float[imageHeight][imageWidth];
        int rgb;
        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                rgb = image.getRGB(i, j);
                int red = (rgb & 0x00ff0000) >> 16;
                int green = (rgb & 0x0000ff00) >> 8;
                int blue = (rgb & 0x000000ff);
               // pixels[j][i] = Math.max(Math.max(red, green), blue);
                pixels[j][i]=red;
            }
        }
        return pixels;
    }

    // to write the image
    public static void writeImage(float[][] pixels, String filePath) {
        File fileout = new File(filePath);
        int w, h;
        h = pixels.length;
        w = pixels[0].length;
        BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

        for (int x = 0; x < h; x++)
            for (int y = 0; y < w; y++) {
                int value = 0xff000000 | ((int) pixels[x][y] << 16) | ((int) pixels[x][y] << 8) | (int) pixels[x][y];
                image2.setRGB(y, x, value);
            }
        try {
            ImageIO.write(image2, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void divideImage() {
        imageHeight = originalImage.length;
        imageWidth = originalImage[0].length;
        // to know if the vector w&h if dividable by image h&w
        int h = imageHeight % vectorHeight;
        int w = imageWidth % vectorWidth;
        // this condition to resize the original image with the new size
        // after removing the non-dividable part of the image

        if ((h != 0) || (w != 0)) {
            imageHeight -= h;
            imageWidth -= w;
            float[][] newImage = new float[imageHeight][imageWidth];
            for (int i = 0; i < imageHeight; i++)
                for (int j = 0; j < imageWidth; j++)
                    newImage[i][j] = originalImage[i][j];
            // resize the image with the new size
            originalImage = newImage;
        }

        // loop overall the image matrix and put all the vector size with the image grb color then add it to the original blocks
        for (int i = 0; i < imageHeight; i += vectorHeight) {
            for (int j = 0; j < imageWidth; j += vectorWidth) {
                float[][] vector = new float[vectorHeight][vectorWidth];
                h = i;
                for (int x = 0; x < vectorHeight; x++, h++) {
                    w = j;
                    for (int y = 0; y < vectorWidth; y++, w++) {
                        vector[x][y] = originalImage[h][w];
                    }
                }
                originalBlocks.add(vector);
            }
        }
    }

    //get the average of the vectors of the image and return the vector of it
    public static float[][] getAverage(ArrayList<float[][]> vecs) {
        float[][] average = new float[vectorHeight][vectorWidth];
        for (int i = 0; i < vectorHeight; i++)
            for (int j = 0; j < vectorWidth; j++) {
                average[i][j] = 0;
                for (int x = 0; x < vecs.size(); x++)
                    average[i][j] += vecs.get(x)[i][j];
                average[i][j] /= (float) vecs.size();
            }
        return average;
    }

    // split each vector to 2 vectors (up&down)
    public static void splitVector() {
        int size = blocks.size();
        // for each block divide it into up and down vector and put it into the blocks
        for (int i = 0; i < size; i++) {
            float[][] down = new float[vectorHeight][vectorWidth];
            float[][] up = new float[vectorHeight][vectorWidth];
            for (int x = 0; x < vectorHeight; x++) {
                for (int y = 0; y < vectorWidth; y++) {
                    float d = (float) Math.floor(blocks.get(i)[x][y]);
                    float u = (float) Math.ceil(blocks.get(i)[x][y]);
                    if (d == u) {
                        d--;
                        u++;
                    }
                    down[x][y] = d;
                    up[x][y] = u;
                }
            }
            blocks.add(down);
            blocks.add(up);
        }
        // to remove the old blocks of the array and let the new in it
        while (size != 0) {
            blocks.remove(0);
            size--;
        }
    }


    public static void getNearestVectorsOfTheOriginalImage() {
        // for each original book find the smallest difference between it and all the blocks by get the abs between them
        for (int i = 0; i < originalBlocks.size(); i++) {
            ArrayList<Double> differences = new ArrayList<>();
            for (int j = 0; j < blocks.size(); j++) {
                double difference = 0;
                for (int x = 0; x < vectorHeight; x++) {
                    for (int y = 0; y < vectorWidth; y++) {
                        difference += Math.pow(((double) blocks.get(j)[x][y] - (double) originalBlocks.get(i)[x][y]), 2);
                    }
                }
                differences.add(difference);
            }
            // get the min value the get its index
            double min = Collections.min(differences);
            int index = differences.indexOf(min);
            //then put the original block at the nearest vector of it
            if (nearestVectors.containsKey(blocks.get(index)))
                nearestVectors.get(blocks.get(index)).add(originalBlocks.get(i));
            else {
                ArrayList<float[][]> nearestVectorsOfThisBlock = new ArrayList<>();
                nearestVectorsOfThisBlock.add(originalBlocks.get(i));
                nearestVectors.put(blocks.get(index), nearestVectorsOfThisBlock);
            }
        }
    }


    public static void Compression() {
        divideImage();// divide the image into vectors and put them into the original image
        blocks.add(getAverage(originalBlocks));// get the average of the original image to split the image into to sides
        nearestVectors.put(blocks.get(0), originalBlocks);// the block that represent the whole of the image
        // split until find the number of codebook that we need
        while (blocks.size() < numOfCodebook) {
            splitVector();
            nearestVectors.clear();// clear the nearest old vectors
            getNearestVectorsOfTheOriginalImage();// get the nearest new vectors
            blocks.clear();// clear the old blocks
            // get the average of the new blocks(the nearest vectors) and put them in the block array
            for (float[][] vec : nearestVectors.keySet()) {
                float[][] average = getAverage(nearestVectors.get(vec));
                blocks.add(average);
            }
        }
        //while true until the vectors be the same
        while (true) {
            int counter = 0;
            for (int i = 0; i < blocks.size(); i++) {
                float[][] vec = blocks.get(i); // for each block
                //loop over all nearest blocks if found
                for (float[][] vector : nearestVectors.keySet()) {
                    int c = 0;
                    for (int x = 0; x < vectorHeight; x++) {
                        for (int y = 0; y < vectorWidth; y++) {
                            if (vector[x][y] == vec[x][y])
                                c++;
                        }
                    }
                    // the two vectors are equal
                    if (c == (vectorHeight * vectorWidth))
                        counter++;
                }
            }

            if (counter == blocks.size())// they are the same so finish
                break;
            // else -> repeat.. get the nearest vectors and the blocks average then check again
            nearestVectors.clear();
            getNearestVectorsOfTheOriginalImage();
            blocks.clear();
            for (float[][] vec : nearestVectors.keySet())
                blocks.add(getAverage(nearestVectors.get(vec)));
        }
        generateCompressedCode();
    }


    public static void generateCompressedCode() {
        // get the number of bits to write the encoded code
        // log2(numOfCodeBook)
        int nBits = (int) Math.ceil((Math.log(numOfCodebook) / Math.log(2)));

        for (int i = 0; i < blocks.size(); i++) {
            // convert int to binary
            String binary = Integer.toBinaryString(i);
            // put zeros to left if it's written in bits less than nBits
            while (binary.length() != nBits)
                binary = "0" + binary;
            // put it at the codebook
            codeBook.put(binary, blocks.get(i));
        }
        // to know the length and the height of the compressed image
        compressedHeight = imageHeight / vectorHeight;
        compressedWidth = imageWidth / vectorWidth;

        compressedImage = new String[compressedHeight][compressedWidth];
        int index = 0;
        //for each cell of the compressed image put the code of it
        for (int i = 0; i < compressedHeight; i++) {
            for (int j = 0; j < compressedWidth; j++) {
                for (float[][] vec : nearestVectors.keySet()) {

                    if (nearestVectors.get(vec).contains(originalBlocks.get(index))) {
                        compressedImage[i][j] = getCode(vec);
                    }
                }
                index++;
            }
        }
    }

    // get the code of the compressed image vector
    public static String getCode(float[][] vector) {
        for (String code : codeBook.keySet()) {
            int counter = 0;
            for (int i = 0; i < vectorHeight; i++) {
                for (int j = 0; j < vectorWidth; j++) {
                    if (vector[i][j] == codeBook.get(code)[i][j])
                        counter++;
                }
            }
            if (counter == (vectorHeight * vectorWidth))
                return code;
        }
        return null;
    }

    public static void Decompression() {
        // get the length and the width of the vector by loop on the codebook one time to get the vector length and width
        for (String code : codeBook.keySet()) {
            vectorHeight = codeBook.get(code).length;
            vectorWidth = codeBook.get(code)[0].length;
            break;
        }
        imageHeight = compressedHeight * vectorHeight;
        imageWidth = compressedWidth * vectorWidth;
        collectedImage = new float[imageHeight][imageWidth];
        int h, w;
        for (int i = 0, a = 0; i < imageHeight; i += vectorHeight, a++) {
            for (int j = 0, b = 0; j < imageWidth; j += vectorWidth, b++) {
                h = i;
                float[][] vector = codeBook.get(compressedImage[a][b]);
                for (int x = 0; x < vectorHeight; x++, h++) {
                    w = j;
                    for (int y = 0; y < vectorWidth; y++, w++) {
                        collectedImage[h][w] = vector[x][y];
                    }
                }
            }
        }
    }






    // the following to write the compressed code of the image and the codebook at file
    public static void writeToFile(String fileName) {
        String image = "";
        for (int i = 0; i < compressedHeight; i++) {
            for (int j = 0; j < compressedWidth; j++)
                image += compressedImage[i][j] + " ";
            image += "\n";
        }
        // to remove the end line
        image = image.substring(0, image.length() - 1);
        for (String code : codeBook.keySet()) {
            image += code + "\n";
            String temp = code;
            temp.replaceAll("0", "1");
            for (int i = 0; i < vectorHeight; i++) {
                for (int j = 0; j < vectorWidth; j++) {
                    image += codeBook.get(code)[i][j] + " ";
                }
                image += "\n";
            }
        }

        // to remove the end line
        image = image.substring(0, image.length() - 2);

        File file = new File(fileName);
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(image);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // to reconstruct the compressed image and the codebook from the file
    public static void readFromFile(String fileName) throws FileNotFoundException {
        compressedImage = new String[compressedHeight][compressedWidth];
        codeBook.clear();
        File file = new File(fileName);
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            for (int i = 0; i < compressedHeight; i++) {
                for (int j = 0; j < compressedWidth; j++) {
                    compressedImage[i][j] = scanner.next();
                }
            }
            while (scanner.hasNextLine()) {
                String code = scanner.next();

                try {
                    float[][] vector = new float[vectorHeight][vectorWidth];
                    for (int i = 0; i < vectorHeight; i++) {
                        for (int j = 0; j < vectorWidth; j++) {
                            vector[i][j] = Float.parseFloat(scanner.next());
                        }
                    }
                    codeBook.put(code, vector);
                } catch (Exception e) {
                    break;
                }
            }
        } else {
            System.out.println("File not found");
        }
    }

}
