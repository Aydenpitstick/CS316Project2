import java.util.concurrent.ThreadLocalRandom;

public class Agent implements Runnable{
    private final int ID;

    public Agent(int ID) {
        this.ID = ID;
    }

    public void run(){
        int customerID;
        int customerPerAgent = CallCenter.totalCustomers/CallCenter.totalAgents;
        for(int i =0; i<customerPerAgent; i++){
            try {
                customerID = CallCenter.takeCall();
                System.out.println("Agent " + ID + " starts serving customer " + customerID);
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 200));
                System.out.println("Agent " + ID + " finished serving customer " + customerID);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
