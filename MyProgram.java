import java.io.IOException;


/**
 * This is the entry of the program.
 * This program will process input audio/img file accordingly, then produce new rgb/wav file.
 * Usage:
 *      java MyProgram [rgb file name] [wav file name] [output rgb] [output wav]
 */
public class MyProgram {

    private static final int REST_TIME = 50000;

    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length != 4){
            System.err.println("ERROR: there must be exact 4 arguments!");
            System.err.println("Usage: java MyProgram [rgb file name] [wav file name] [output rgb] [output wav]");
            return;
        }

        long startTime = 0L, endTime = 0L;
        startTime = System.currentTimeMillis();
        System.out.println("****** shot detection start ******");
        Process p = Runtime.getRuntime().exec("python3" + " shots_detection.py " + args[0]);
        p.waitFor();
        while(p.isAlive());
        System.out.println("****** shot detection complete ******");

        Thread.sleep(REST_TIME);

        System.out.println("****** audio analysis start ******");
        Process p1 = Runtime.getRuntime().exec("java -cp .:javacpp-1.5.7.jar:musicg-1.4.2.0.jar:musicg-graphic-1.3.0.0.jar:musicg-sound-api-1.2.0.1.jar:musicg-sound-api-graphic-1.2.0.1.jar AudioAnalyzer " + args[1]);
        p1.waitFor();
        while(p1.isAlive());
        System.out.println("****** audio analysis complete ******");
        endTime = System.currentTimeMillis();
        System.out.println("Running time = " + ((endTime - startTime) / 1000.0));

        Thread.sleep(REST_TIME);

        System.out.println("****** logo detection start ******");
        String imgInterval = "intervalForLogoDetection.txt";
        String datasetName = args[0].replaceAll(".rgb", "").replaceAll("_test", "set");
        System.out.println("dataset name = " + datasetName);
        Process p2 = Runtime.getRuntime().exec("python3" + " LogoDetector.py " + datasetName + " " + args[0]+ " " + imgInterval);
        p2.waitFor();
        while(p2.isAlive());
        System.out.println("****** logo detection complete ******");
        endTime = System.currentTimeMillis();
        System.out.println("Running time = " + ((endTime - startTime) / 1000.0));

        Thread.sleep(REST_TIME);

        System.out.println("****** merge rgb start ******");
        String logoOutput = datasetName + "_output.txt";
        Process p3 = Runtime.getRuntime().exec("java" + " ImgProcessor " + args[0] + " " + logoOutput + " " + args[2]);
        p3.waitFor();
        while(p3.isAlive());
        Thread.sleep(REST_TIME);
        System.out.println("****** merge rgb end ******");
        endTime = System.currentTimeMillis();
        System.out.println("Running time = " + ((endTime - startTime) / 1000.0));

        Thread.sleep(REST_TIME);

        System.out.println("****** process audio start ******");
        Process p4 = Runtime.getRuntime().exec("java" + " AudioProcessor " + logoOutput + " " + args[1] + " " + args[3]);
        p4.waitFor();
        while(p4.isAlive());
        Thread.sleep(REST_TIME);
        System.out.println("****** process audio end ******");
        endTime = System.currentTimeMillis();
        System.out.println("Running time = " + ((endTime - startTime) / 1000.0));
    }
}
