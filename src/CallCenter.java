import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CallCenter {
    public static final int totalCustomers=20;
    public static final int totalAgents=2;
    //shared data
    private final static Queue<Integer> queue = new LinkedList<>();
    private final static ReentrantLock arrivalLock = new ReentrantLock();
    private final static ReentrantLock serviceLock = new ReentrantLock();
    private final static Condition serviceNotEmpty = serviceLock.newCondition();
    private final static Condition arrivalNotEmpty = arrivalLock.newCondition();

    public static void addCall(int customerID){
        arrivalLock.lock();
        try {
            //critical section
            queue.add(customerID);
            arrivalNotEmpty.signal();
        }finally{
            arrivalLock.unlock();
        }
    }
    public static int greetCustomer(){
        int customerID;
        arrivalLock.lock();
        try{
            while(queue.isEmpty()) {
                //await() releases the qLock
                //puts thread to sleep
                arrivalNotEmpty.await();
            }
            customerID = queue.remove();
        }finally{
            arrivalLock.unlock();
        }
        return customerID;
    }
    public static int takeCall() throws Exception{
        int customerID;
        serviceLock.lock();
        try{
            while(queue.isEmpty()) {
                //await() releases the qLock
                //puts thread to sleep
                serviceNotEmpty.await();
            }
            customerID = queue.remove();
        }finally{
            serviceLock.unlock();
        }
        return customerID;
    }

    static void main() throws InterruptedException{
        //for long-lived tasks
        ExecutorService agentPool = Executors.newFixedThreadPool(4);

        // for short-lived tasks
        ExecutorService customerPool = Executors.newCachedThreadPool();

        for(int i=1; i<=totalAgents; i++){
            agentPool.submit(new Agent(i));
        }
        for(int i=1; i<=totalCustomers; i++){
            customerPool.submit(new Customer(i));
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
        }
        agentPool.shutdown();
        customerPool.shutdown();
    }
}

