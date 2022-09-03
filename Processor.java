import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Processor1 {
    private String originalRGB;
    private String logoRe;
    private String imageinterval;
    private static final int WIDTH = 480;
    private static final int HEIGHT = 270;
    private static final int videoFPS = 30;
    private OutputStream outRGB;

    // assume logoRe is like below format:
    // newRGB1.rgb path_to_AD1.rgb newRGB2.rgb path_to_AD2.rgb
    public Processor1(String originalRGB, String logoRe, String imageinterval, String outputPath) throws FileNotFoundException {
        this.originalRGB = originalRGB;
        this.logoRe = logoRe;
        this.imageinterval = imageinterval;
        outRGB = new FileOutputStream(outputPath);
    }

    public void rgbCombiner() throws IOException {
        Scanner scanner = new Scanner(new File(logoRe));
        List<String> listOfPaths = new ArrayList<>();
        byte[] bytes = new byte[3*WIDTH*HEIGHT];
        String[] paths = scanner.nextLine().split(" ");
        for(String path : paths) {
            listOfPaths.add(path);
        }

        for(int i = 0; i < listOfPaths.size(); i++) {
            String pathToFile = listOfPaths.get(i);
            if(!pathToFile.endsWith(".rgb")){
                pathToFile = pathToFile + ".rgb";
            }
            File path = new File(pathToFile);
            InputStream input = new FileInputStream(path);
            int length;
            int frames = 0;
            while ((length = input.read(bytes)) != -1) {
                outRGB.write(bytes, 0, length);
                frames++;
            }
            System.out.println("Total frames is: " + frames);
        }

        //check if the last shot exist
        File img = new File(imageinterval);
        scanner = new Scanner(img);
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(!scanner.hasNextLine()) {
                if(Integer.parseInt(line.split(" ")[1]) != 9000) {
                    int start = Integer.parseInt(line.split(" ")[1]);
                    // retrieve original rgb file
                    String pathToFile = originalRGB;
                    File file = new File(pathToFile);
                    InputStream input = new FileInputStream(file);
                    int counter = 0;
                    int length;
                    while ((length = input.read(bytes)) != -1 && counter != start) {
                        counter++;
                    }
                    while ((length = input.read(bytes)) != -1) {
                        outRGB.write(bytes, 0, length);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String originalRGB = args[0];
        String logoR = args[1];
        String intervals = "imageinterval.txt";
        Processor1 processor1 = new Processor1(originalRGB,logoR,intervals,args[2]);
        processor1.rgbCombiner();


    }


}
