package invoker54.xpshop.util;

import java.util.ArrayList;
import java.util.List;

public class ModStopWatch extends ModTimer {

    private double bestTime;
    private final Time time;
    private final List<Double> times;

    public enum Time {
        SHORTEST,
        LONGEST,
        ACCUMULATE
    }

    public static ModStopWatch getTimer(String modID, String name, Time time) {
        String fullName = modID + name + "watch";
        if (!timerMap.containsKey(fullName)) timerMap.put(fullName, new ModStopWatch(time));
        return (ModStopWatch) timerMap.get(fullName);
    }

    protected ModStopWatch(Time time) {
        super();
        this.bestTime = 0;
        this.time = time;
        this.times = new ArrayList<>();
    }

    @Override
    public String record(MiniTicker<?> ticker, String whatItsFor) {
        double timeElapsed = ticker.getElapsedTime();

        if (this.time == Time.LONGEST && timeElapsed > this.bestTime) {
            super.record(ticker, whatItsFor);
            this.bestTime = timeElapsed;
            times.add(timeElapsed);
        } else if (this.time == Time.SHORTEST && timeElapsed < this.bestTime) {
            super.record(ticker, whatItsFor);
            this.bestTime = timeElapsed;
            times.add(timeElapsed);
        } else if (this.time == Time.ACCUMULATE) {
            super.record(ticker, whatItsFor);
            times.add(timeElapsed);
        }
        return "";
    }

    public String bestTime(){
        if (this.allTimes.isEmpty()) return "";
        return this.allTimes.get(this.allTimes.size()-1);
    }

    public String compileTime(String whatItsFor, boolean average, boolean clear) {
        long sum = 0;
        for (Double number : this.times) {
            sum += number;
        }
        if (average) sum = sum / this.times.size();

        String timeElapsed = (sum / 1000000000D) + "";
        String timeString = ("It took " + timeElapsed + " seconds: " + whatItsFor);

        if (clear) this.times.clear();

        return timeString;
    }
}
