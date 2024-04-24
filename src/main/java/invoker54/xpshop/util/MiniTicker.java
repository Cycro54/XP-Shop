package invoker54.xpshop.util;

public class MiniTicker<T extends ModTimer> {
    protected long startTime;
    protected final T owner;

    public MiniTicker(T owner){
        this.owner = owner;
        this.startTime = System.nanoTime();
    }

    public T getOwner(){
        return this.owner;
    }

    public String record(String whatItsFor){
       return this.getOwner().record(this, whatItsFor);
    }

    public void reset(){
        this.startTime = System.nanoTime();
    }

    public Long getElapsedTime(){
        return System.nanoTime() - this.startTime;
    }
}
