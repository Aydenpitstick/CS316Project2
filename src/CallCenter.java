import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CallCenter {
    public static final int totalCustomers=30;
    public static final int totalAgents=3;
    //shared data
    private final static Queue<Integer> arrivalQueue = new LinkedList<>();
    private final static Queue<Integer> serviceQueue = new LinkedList<>();
    private final static ReentrantLock arrivalLock = new ReentrantLock();
    private final static ReentrantLock serviceLock = new ReentrantLock();
    private final static Condition serviceNotEmpty = serviceLock.newCondition();
    private final static Condition arrivalNotEmpty = arrivalLock.newCondition();

    public static void addCall(int customerID){
        arrivalLock.lock();
        try {
            //critical section
            arrivalQueue.add(customerID);
            arrivalNotEmpty.signal();
        }finally{
            arrivalLock.unlock();
        }
    }
    public static int greetArrival() throws Exception{
        int customerID;
        arrivalLock.lock();
        try{
            while(arrivalQueue.isEmpty()) {
                //await() releases the qLock
                //puts thread to sleep
                arrivalNotEmpty.await();
            }
            customerID = arrivalQueue.remove();
        }finally{
            arrivalLock.unlock();
        }
        return customerID;
    }
    public static void addService(int customerID){
        serviceLock.lock();
        try {
            //critical section
            serviceQueue.add(customerID);
            serviceNotEmpty.signal();
        }finally{
            serviceLock.unlock();
        }
    }
    public static int takeCall() throws Exception{
        int customerID;
        serviceLock.lock();
        try{
            while(serviceQueue.isEmpty()) {
                //await() releases the qLock
                //puts thread to sleep
                serviceNotEmpty.await();
            }
            customerID = serviceQueue.remove();
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

