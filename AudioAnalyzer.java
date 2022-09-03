import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

import java.io.*;
import java.util.*;


/**
 * This class is used to find all breakpoints between shots from wav audio file
 *
 * Reference:
 * musicg: https://sites.google.com/site/musicgapi/home
 * To download: https://code.google.com/archive/p/musicg/downloads
 *
 * To run the program in commend shell (make sure all jars are in the some folder):
 * compile:
 *      javac -cp .:javacpp-1.5.7.jar:musicg-1.4.2.0.jar:musicg-graphic-1.3.0.0.jar:musicg-sound-api-1.2.0.1.jar:musicg-sound-api-graphic-1.2.0.1.jar AudioAnalyzer.java
 * run:
 *      java -cp .:javacpp-1.5.7.jar:musicg-1.4.2.0.jar:musicg-graphic-1.3.0.0.jar:musicg-sound-api-1.2.0.1.jar:musicg-sound-api-graphic-1.2.0.1.jar AudioAnalyzer [wav file]
 *
 * This program will find all intervals that might contains ads.
 * The interval is represented by frame indexes
 * Result will be written into file "audiointerval.txt" && "imageinterval.txt"
 * Each line will has one single interval
 * For result [0, 630], [9380, 9889], the output will be:
 * 0 630
 * 9380 9389
 *
 */
public class AudioAnalyzer {

    static class Interval{
        double start;
        double end;
        int imgStart;
        int imgEnd;
        int wavStart;
        int wavEnd;
        double diff;

        @Override
        public String toString() {
            return "Interval{" +
                    "start=" + start +
                    ", end=" + end +
                    ", imgStart=" + imgStart +
                    ", imgEnd=" + imgEnd +
                    ", wavStart=" + wavStart +
                    ", wavEnd=" + wavEnd +
                    ", diff=" + diff +
                    '}';
        }
    }

    private Wave wave;
    private Spectrogram spectrogram;
    private static int totalNumFrames;
    private static int framePerSecond;

    private static double[][] data;
    private static double[][] absData;
    private static double[] avg;
    private static double[] absAvg;


    public AudioAnalyzer(String wavpath){
        wave = new Wave(wavpath);
        spectrogram = wave.getSpectrogram();
        totalNumFrames = spectrogram.getNumFrames();
        framePerSecond = spectrogram.getFramesPerSecond();

        data = spectrogram.getNormalizedSpectrogramData();
        absData = spectrogram.getAbsoluteSpectrogramData();
        avg = getAvg(data, 0, data.length - 1);
        absAvg = getAvg(absData, 0, data.length - 1);
        if(framePerSecond != 46){
            framePerSecond = 46;
        }
        //printWaveInfo();
    }

    /**
     * print information about the input wav file
     */
    public void printWaveInfo(){

        System.out.println("Wave length = " + wave.length());
        System.out.println("sample amp length = " + wave.getSampleAmplitudes().length);
        System.out.println("normal amp length = " + wave.getNormalizedAmplitudes().length);
        System.out.println("finger print length = " + wave.getFingerprint().length);
        System.out.println("byte print length = " + wave.getBytes().length);
        System.out.println("unit freq = " + spectrogram.getUnitFrequency());
        System.out.println("num freq unit = " + spectrogram.getNumFrequencyUnit());
        System.out.println("Fft sample size = " + spectrogram.getFftSampleSize());
        System.out.println("Overlap factor = " + spectrogram.getOverlapFactor());
        System.out.println("Frame per second = " + spectrogram.getFramesPerSecond());
        System.out.println("Total number of frames = " + spectrogram.getNumFrames());

    }


    private static double[] getAvg(double[][] input, int start, int end){
        int col = input[0].length;
        double[] avg = new double[col];

        for(int idx = start; idx <= end; idx++) {
            double[] r = input[idx];
            for (int i = 0; i < col; i++) {
                avg[i] += r[i];
            }
        }
        for(int i = 0; i < avg.length; i++){
            avg[i] /= (end - start + 1);
        }
        return avg;
    }


    private void getSortedIntervals(List<Interval> intervals){
        int numOfAds = 3;
        for(Interval interval: intervals){
            interval.wavStart = (int) Math.round(interval.start * framePerSecond);
            interval.wavEnd = (int) Math.round(interval.end * framePerSecond);
            interval.diff = getDiff(interval.wavStart, interval.wavEnd);
        }

        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return -Double.compare(o1.diff, o2.diff);
            }
        });
        while(intervals.size() > numOfAds){
            intervals.remove(intervals.size() - 1);
        }
    }

    private static double getDiff(int start, int end){
        end = Math.min(end, data.length - 1);
        double[] currAvg = new double[data[0].length];
        for(int i = start; i <= end; i++){
            for(int j = 0; j < currAvg.length; j++){

                currAvg[j] += data[i][j];
            }
        }
        double diff = 0.0;
        for(int i = 0; i < currAvg.length; i++){
            currAvg[i] /= (end - start + 1);
            diff += Math.abs(currAvg[i] - avg[i]);
        }
        return diff;
    }


    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.err.println("ERROR: there must be exactly one parameter!");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader("breakpoints"));
        String line = null;
        List<Double> breakPointInSecond = new ArrayList<>();
        List<Integer> breakPointInFrameNum = new ArrayList<>();
        List<List<Integer>> intervalsInSecond = new ArrayList<>();
        List<List<Integer>> intervalsInFrameNum = new ArrayList<>();
        int lineNum = 1;
        while((line = br.readLine())!= null){
            switch (lineNum){
                case 1:
                    String[] tokens1 = line.split(",");
                    for(String token: tokens1){
                        breakPointInSecond.add(Double.parseDouble(token));
                    }
                    break;
                case 2:
                    String[] tokens2 = line.split(",");
                    for(String token: tokens2){
                        breakPointInFrameNum.add((int)Math.round(Double.parseDouble(token)));
                    }
                    break;

                case 3:
                    line = line.substring(1, line.length() - 1);
                    String[] tokens3 = line.split("\",\"");
                    for(String token: tokens3){
                        String[] temp = token.substring(1,token.length() -1).split(",");
                        List<Integer> interval = new ArrayList<>();
                        interval.add((int)Math.round(Double.parseDouble(temp[0])));
                        interval.add((int)Math.round(Double.parseDouble(temp[1])));
                        intervalsInSecond.add(interval);
                    }
                    break;

                case 4:
                    line = line.substring(1, line.length() - 1);
                    String[] tokens4 = line.split("\",\"");
                    for(String token: tokens4){
                        String[] temp = token.substring(1,token.length() -1).split(",");
                        List<Integer> interval = new ArrayList<>();
                        interval.add((int)Math.round(Double.parseDouble(temp[0])));
                        interval.add((int)Math.round(Double.parseDouble(temp[1])));
                        intervalsInFrameNum.add(interval);
                    }
                    break;

                default:
                    //do nothing
            }
            lineNum++;
        }
        System.out.println(breakPointInSecond);
        System.out.println(breakPointInFrameNum);
        System.out.println(intervalsInSecond);
        System.out.println(intervalsInFrameNum);
        if(intervalsInSecond.size() > 3){
            findFifteenFromIntervals(args[0], intervalsInSecond, intervalsInFrameNum);
            return;
        }

        fromImageShotFindFifteen(args[0], breakPointInSecond, breakPointInFrameNum, intervalsInSecond);
    }

    private static void findFifteenFromIntervals(String fileName,
                                                 List<List<Integer>> intervalsInSecond, List<List<Integer>> intervalsInFrameNum) throws IOException {
        List<Interval> res = new ArrayList<>();
        for(int idx = 0; idx < intervalsInSecond.size(); idx++){
            List<Integer> interval = intervalsInSecond.get(idx);
            Interval i = new Interval();
            i.start = interval.get(0);
            if(interval.get(1) - interval.get(0) < 14.0){
                continue;
            }
            i.end = interval.get(1);
            i.imgStart = intervalsInFrameNum.get(idx).get(0);
            i.imgEnd = intervalsInFrameNum.get(idx).get(1);
            i.wavStart = (int) Math.round(i.start * 46);
            i.wavEnd = (int) Math.round(i.end * 46);
            res.add(i);
        }
        AudioAnalyzer audioAnalyzer = new AudioAnalyzer(fileName);
        audioAnalyzer.getSortedIntervals(res);
        Collections.sort(res, new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return Double.compare(o1.start, o2.start);
            }
        });
        res.get(1).wavEnd+=15;
        System.out.println(res);
        BufferedWriter bw = new BufferedWriter(new FileWriter("audiointerval.txt"));
        for(Interval interval: res){
            bw.write(interval.wavStart + " " + interval.wavEnd + "\n");
        }
        bw.flush();
        bw.close();

        bw = new BufferedWriter(new FileWriter("imageinterval.txt"));
        for(Interval interval: res){
            bw.write(interval.imgStart + " " + interval.imgEnd + "\n");
        }
        bw.flush();
        bw.close();

        bw = new BufferedWriter(new FileWriter("intervalForLogoDetection.txt"));
        for(int i = 0; i < res.size(); i++){
            Interval interval = res.get(i);
            bw.write("\"[" + interval.imgStart + ", " + interval.imgEnd + "]\"");
            if(i < res.size() - 1){
                bw.write(",");
            }
        }
        bw.flush();
        bw.close();

    }



    private static void fromImageShotFindFifteen(String fileName,
                                                 List<Double> breakPointInSecond,
                                                 List<Integer> breakPointInFrameNum,
                                                 List<List<Integer>> intervalsInSecond) throws IOException {
        int error = 2;
        int len = 15;
        int[] s = convertToIntArray(breakPointInSecond);
        Map<Integer, Integer> m = new HashMap<>(); //breakpoint -> array index
        for(int idx = 0; idx < s.length; idx++){
            m.put(s[idx], idx);
        }

        List<Interval> res = new ArrayList<>();
        int start = 0;
        while(start < s.length){
            int idx = -1;
            for(int delta = -error; delta <= error; delta++){
                if(m.containsKey(s[start] + len + delta)){
                    idx = m.get(s[start] + len + delta);
                }
            }
            if(idx != -1){
                Interval interval = new Interval();
                interval.start = breakPointInSecond.get(start);
                interval.end = breakPointInSecond.get(idx);
                interval.imgStart = breakPointInFrameNum.get(start);
                interval.imgEnd = breakPointInFrameNum.get(idx);
                res.add(interval);
                //System.out.println(interval);
                start = idx+1;
            }
            else{
                start++;
            }
        }

        AudioAnalyzer audioAnalyzer = new AudioAnalyzer(fileName);
        audioAnalyzer.getSortedIntervals(res);
        System.out.println(res);
        finalizeResult(res, intervalsInSecond);
        System.out.println(res);

        Collections.sort(res, new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                return Double.compare(o1.start, o2.start);
            }
        });
        BufferedWriter bw = new BufferedWriter(new FileWriter("audiointerval.txt"));
        for(Interval interval: res){
            bw.write(interval.wavStart + " " + interval.wavEnd + "\n");
        }
        bw.flush();
        bw.close();

        bw = new BufferedWriter(new FileWriter("imageinterval.txt"));
        for(Interval interval: res){
            bw.write(interval.imgStart + " " + interval.imgEnd + "\n");
        }
        bw.flush();
        bw.close();

        bw = new BufferedWriter(new FileWriter("intervalForLogoDetection.txt"));
        for(int i = 0; i < res.size(); i++){
            Interval interval = res.get(i);
            bw.write("\"[" + interval.imgStart + ", " + interval.imgEnd + "]\"");
            if(i < res.size() - 1){
                bw.write(",");
            }
        }
        bw.flush();
        bw.close();
    }

    private static void finalizeResult(List<Interval> res, List<List<Integer>> intervalsInSecond){
        List<Integer> intervalIdxToRemove = new ArrayList<>();
        for(int idx = 0; idx < res.size(); idx++){
            Interval interval = res.get(idx);
            int closestIdx = -1;
            for(int i = 0; i < intervalsInSecond.size(); i++){
                List<Integer> seconds = intervalsInSecond.get(i);
                if(interval.start >= seconds.get(0) && interval.start <= seconds.get(1)){
                    closestIdx = i;
                    break;
                }
            }
            if(closestIdx == -1){
                intervalIdxToRemove.add(idx);
                continue;
            }
            double diff = interval.start - intervalsInSecond.get(closestIdx).get(0);
            if(diff >= 1.0){
                interval.start -= diff;
                interval.end  =interval.start + 15.0;
                interval.wavStart = (int) Math.round(interval.start * 46);
                interval.wavEnd = (int) Math.round(interval.end * 46);
                interval.imgStart = (int) Math.round(interval.start * 30);
                interval.imgEnd = (int) Math.round(interval.end * 30);
            }
        }
        Collections.sort(intervalIdxToRemove, Collections.reverseOrder());
        for(int idx: intervalIdxToRemove){
            res.remove(idx);
        }

    }

    private static int[] convertToIntArray(List<Double> arr){
        int[] res = new int[arr.size()];
        for(int i = 0; i < res.length; i++){
            res[i] = (int) Math.round(arr.get(i));
        }
        return res;
    }

}
