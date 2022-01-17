package de.dfki.asr.ajan.stopwatch;
import java.util.LinkedHashMap;

public class Stopwatch {

    private long stopWatchStartTime = 0;
    private long stopWatchStopTime = 0;
    private boolean stopWatchRunning = false;

    private final LinkedHashMap<String, Long> stopWatchSteps;

    public Stopwatch(){
        this.stopWatchSteps = new LinkedHashMap<>();
    }



    public void start() {
        reset();
        this.stopWatchStartTime = System.nanoTime();
        this.stopWatchRunning = true;
    }

    public void step(String stepkey){
        if (stopWatchRunning){
            this.stopWatchSteps.put(stepkey, System.nanoTime());
            return;
        }
        throw new RuntimeException("Stopwatch tryed to be stepped whilst not running");
    }

    public void stop() {
        this.stopWatchStopTime = System.nanoTime();
        this.stopWatchRunning = false;
    }

    public void reset(){
        this.stopWatchRunning = false;
        this.stopWatchStartTime = 0;
        this.stopWatchStopTime = 0;
        this.stopWatchSteps.clear();
    }


    @Override
    public String toString() {
        String elapsedTotal = getElapsedFormated(getElapsed(stopWatchStartTime, stopWatchStopTime));
        String elapsedSteps = "";
        long prevStepTime = stopWatchStartTime;
        for(String s : stopWatchSteps.keySet()){
            long stepTime = stopWatchSteps.get(s);
            String formated = getElapsedFormated(stepTime-prevStepTime);
            prevStepTime = stepTime;
            elapsedSteps += "\t\n" + s + " " + formated;
        }
        return "Stopwatch Results: " + elapsedSteps + "\nTotal elapsed time: " + elapsedTotal;
    }

    private long getElapsed(long start, long stop){
        return stop - start;
    }

    private String getElapsedFormated(long elapsed){
        long seconds = elapsed/1000000000;
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }

}
