package smu.smc.jiaming.scheme2;

import smu.smc.jiaming.scheme2.UserClient;

public class UserClientThread extends smu.smc.jiaming.scheme1.UserClientThread {
    public UserClientThread(UserClient uc) {
        super(uc);
    }

    public void run(){
        int n, p, m;
        while(true){
            System.out.println("1.new TX 2.new RewritableTX 3.new MixedTXes 4.show generates 5.send 6.quit");
            if(s.hasNext()){
                String sin = s.next();
                int in = 0;
                try{
                    in = Integer.parseInt(sin);
                    switch (in){
                        case 1:
                            this.newTX();
                            this.preCom = in;
                            break;
                        case 2:
                            System.out.print("Please enter the candidate message number:");
                            n = s.nextInt();
                            this.newRewritableTX(n);
                            this.preCom = in;
                            break;
                        case 3:
                            System.out.print("Please enter the number of mixedtxes, " +
                                    "the number of rtxes, and the number of cm of each rtx:");
                            n = s.nextInt();
                            p = s.nextInt();
                            m = s.nextInt();
                            this.newMixedTXes(n, p, m);
                            this.preCom = in;
                            break;
                        case 4:
                            showGens();
                            break;
                        case 5:
                            send();
                            break;
                        case 6:
                            this.uc.getTx().stop();
                            return;
                        default:
                            break;
                    }
                }catch (NumberFormatException e){
                    System.out.println("Please Type in a Number!");
                }
                System.out.print("Press Enter to Continue...");
                if(s.hasNextLine()){
                    s.nextLine();
                    s.nextLine();
                    continue;
                }
            }
        }
    }

    protected void newRewritableTX(int n){
        this.tx = ((UserClient)this.uc).newRandomRTX(n);
        System.out.println("Success to Generate a RewritableTX!");
    }

    protected void newMixedTXes(int n, int p, int m){
        this.txes = ((UserClient)this.uc).newRandomMixedTXes(n, p, m);
        System.out.println("Success to Generate " + n + " TXes with " + p +
                " RewritableTXes, and the rtx has " + m + " candidate message");
    }
}
