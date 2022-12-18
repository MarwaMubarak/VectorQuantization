package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

public class Main {

    static Vector<CodeBook> codeBooks =new Vector<CodeBook>();
    static Vector<CodeBook> allCodeBooks =new Vector<CodeBook>();

    public static  int[][] getAverage(Vector<CodeBook> allCodeBooks) {
        int vectorSize = allCodeBooks.get(0).vector.length;
        int[][] average = new int[vectorSize][vectorSize];
        for (int i = 0; i < vectorSize; i++)
            for (int j = 0; j < vectorSize; j++)
                average[i][j] = 0;
        for (int k = 0; k < allCodeBooks.size(); k++)
            for (int i = 0; i < vectorSize; i++)
                for (int j = 0; j < vectorSize; j++)
                    average[i][j] += allCodeBooks.get(k).vector[i][j];
        for (int i = 0; i < vectorSize; i++)
            for (int j = 0; j < vectorSize; j++)
                average[i][j] /= allCodeBooks.size();
        return average;
    }
    public static  int getIdx(CodeBook codeBook, Vector<Vector<CodeBook>>spiltTree) {
        int idx = -1;
        for (int i = 0; i < spiltTree.size(); i++)
        {
            for (int j = 0; j < spiltTree.get(i).size(); j++) {
                if (spiltTree.get(i).get(j).vector == codeBook.vector)
                    idx = i;
            }
        }
        return idx;
    }
    public static  int LeftOrRight(CodeBook codeBook, Vector<Vector<CodeBook>>spiltTree) { //compare
        int idx = -1,mn =Integer.MAX_VALUE,diff=0;
        for(int k=0;k<spiltTree.size();k++) {
            diff=0;
            for (int i = 0; i < codeBook.vector.length; i++) {
                for (int j = 0; j < codeBook.vector.length; j++) {
                    diff += Math.abs(codeBook.vector[i][j]-(spiltTree.get(k).get(0).vector[i][j]));
                }
            }
            if(diff<mn)
            {
                mn = diff;
                idx = k;
                //diff = 0;
            }
        }
        return idx;
    }
    public static void Compression(int numOfCodeBook) throws IOException {

       int n=(int)Math.sqrt(allCodeBooks.size());
       Vector<Vector<CodeBook>> spiltTree=new Vector<>();
       int [][] averageCodeBooks=getAverage(allCodeBooks);
       int vectorSize =allCodeBooks.get(0).vector.length;
       int leftAverage[][]=new int[vectorSize][vectorSize];
       int rightAverage[][]=new int[vectorSize][vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            for (int j = 0; j < vectorSize; j++) {
                leftAverage[i][j]=averageCodeBooks[i][j]-1;
                rightAverage[i][j]=averageCodeBooks[i][j]+1;
            }
        }
        Vector<CodeBook>rightCode=new Vector<>();
        rightCode.add(new CodeBook(rightAverage));
        Vector<CodeBook>leftCode=new Vector<>();
        leftCode.add(new CodeBook(leftAverage));
        spiltTree.add(leftCode);
        spiltTree.add(rightCode);
        int f=0;
        int spilt=(int)(Math.log10(numOfCodeBook)/Math.log10(2));
        for (int i = 0; i < spilt; i++) {
            for (int j = 0; j < allCodeBooks.size(); j++) {
                int idx=LeftOrRight(allCodeBooks.get(j),spiltTree);
                spiltTree.get(idx).add(allCodeBooks.get(j));
            }
            Vector<CodeBook>midPoints=new Vector<>();
            for (int j = 0; j < spiltTree.size(); j++) {
                averageCodeBooks=getAverage(spiltTree.get(j));
                midPoints.add(new CodeBook(averageCodeBooks));
                // update the average point in the tree
                spiltTree.get(j).get(0).vector=averageCodeBooks;

            }

            if(i!=spilt-1){
                spiltTree.clear();
                for (int j = 0; j < midPoints.size(); j++) {
                    for (int k = 0; k < vectorSize; k++) {
                        for (int l = 0; l < vectorSize; l++) {
                            leftAverage[k][l] = midPoints.elementAt(j).vector[k][l] - 1;
                            rightAverage[k][l] = midPoints.elementAt(j).vector[k][l] + 1;

                        }
                    }
                    rightCode.clear();
                    rightCode.add(new CodeBook(rightAverage));
                    leftCode.clear();
                    leftCode.add(new CodeBook(leftAverage));
                    spiltTree.add(leftCode);
                    spiltTree.add(rightCode);
                }
            }else if(f==0){
                spilt++;
                f=1;

            }


        }
        for (int i = 0; i < spiltTree.size(); i++) {
            CodeBook codeBook=spiltTree.get(i).get(0);
            codeBook.code=i;
            codeBooks.add(codeBook);
        }
        //compress the image
        int compressedCodeBooks[][]=new int[n][n];
        for (int i = 0; i < compressedCodeBooks.length; i++) {
            for (int j = 0; j < compressedCodeBooks.length; j++) {
                compressedCodeBooks[i][j] = getIdx(allCodeBooks.get(j), spiltTree);
            }
        }

        // write image at the file
        FileWriter fileWriter= null;
        try {
            fileWriter = new FileWriter("x.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i <compressedCodeBooks.length ; i++) {
            for (int j = 0; j < compressedCodeBooks.length; j++) {
                fileWriter.write(compressedCodeBooks[i][j]+" ");
            }
            fileWriter.write("\n");
        }
        fileWriter.close();






    }

    public static void Decompression(){

    }
    public static void divideImage(int pixels[][], int vectorSize){
       int n= pixels.length/vectorSize;
        for (int i = 0; i <= n*vectorSize; i+=vectorSize) {
            for (int j = 0; j < n*vectorSize; j+=vectorSize) {
                int vector[][] =new int[vectorSize][vectorSize];
                for (int k = i,x=0; k < vectorSize+i;x++, k++) {
                    for (int l = j,y=0; l < vectorSize+j;y++, l++) {
                        vector[x][y]=pixels[i][j];
                    }
                }
                CodeBook codeBook=new CodeBook(vector);
                allCodeBooks.add(codeBook);

            }
        }

    }

    public static Scanner input =new Scanner(System.in);
    public static void main(String[] args) throws IOException {
        int vectorSize=input.nextInt();
        int numOfCodeBook=input.nextInt();
        ReadWriteImage readWriteImage=new ReadWriteImage();
        int pixels[][]=readWriteImage.readImage("img.jpg");
        divideImage(pixels,vectorSize);
        Compression(numOfCodeBook);

    }
}
