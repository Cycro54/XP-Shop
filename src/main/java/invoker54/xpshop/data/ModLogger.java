package invoker54.xpshop.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModLogger {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private final Logger myLogger;
    private final AtomicBoolean debugMode;
    private double counter = 0;
    private static final List<ModLogger> loggers = new ArrayList<>();

    private ModLogger(Logger myLogger, AtomicBoolean debugMode) {
        this.myLogger = myLogger;
        this.debugMode = debugMode;
    }

    public static ModLogger getLogger(AtomicBoolean debugMode){
        ModLogger newLogger = new ModLogger(LoggerFactory.getLogger
                (STACK_WALKER.getCallerClass()), debugMode);
        loggers.add(newLogger);
        return newLogger;
    }

    public void debug(String s){
        double time = System.nanoTime();
        if (!this.debugMode.get()) return;
        this.myLogger.debug(s);
        counter += (System.nanoTime() - time);
    }

    public void info(String s){
        double time = System.nanoTime();
        if (!this.debugMode.get()) return;
        this.myLogger.info(s);
        counter += (System.nanoTime() - time);
    }

    public void warn(String s){
        double time = System.nanoTime();
        if (!this.debugMode.get()) return;
        this.myLogger.warn(s);
        counter += (System.nanoTime() - time);
    }

    public void error(String s){
        double time = System.nanoTime();
        if (!this.debugMode.get()) return;
        this.myLogger.error(s);
        counter += (System.nanoTime() - time);
    }

    public void timePassed(boolean resetTime){
        if (!this.debugMode.get()) return;
        this.myLogger.info(this.myLogger.getName()+"Time passed: " + (counter/1000000000F));
        if (resetTime) counter = 0;
    }

    public static void getAllTimePassed(){
        double totalTime = 0;
        for (ModLogger logger : loggers){
            totalTime += logger.counter;
            logger.timePassed(true);
        }
        loggers.get(0).error("Total time passed: " + (totalTime/1000000000F));
    }
}
