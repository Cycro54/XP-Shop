package invoker54.xpshop.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModTimer {
    protected static final Map<String, ModTimer> timerMap = new HashMap<>();
    protected final List<String> allTimes = new ArrayList<>();

    public static ModTimer getTimer(String modID, String name){
        String fullName = modID + name + "watch";
        if (!timerMap.containsKey(fullName)) timerMap.put(fullName, new ModTimer());
        return timerMap.get(fullName);
    }

    public String record(MiniTicker<?> ticker, String whatItsFor){

        String timeElapsed = (ticker.getElapsedTime()/1000000000D) + "";
        String timeString = ("It took " + timeElapsed + " seconds: " + whatItsFor);
        this.allTimes.add(timeString);
//        if (logger != null) logger.info(timeString);
        ticker.reset();
        return timeString;
    }
    public MiniTicker<?> ticker(){return new MiniTicker<>(this);}

    public List<String> grabTimes(boolean clear){
        ArrayList<String> times = new ArrayList<>(this.allTimes);
        if (clear) this.allTimes.clear();
        return times;
    }
}
