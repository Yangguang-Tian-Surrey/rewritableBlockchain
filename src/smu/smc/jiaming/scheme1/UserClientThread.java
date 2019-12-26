package smu.smc.jiaming.scheme1;

import java.util.Scanner;

public class UserClientThread extends Thread{
    protected UserClient uc;
    protected TX tx;
    protected TX[] txes;
    protected int preCom = 0;
    protected Scanner s = new Scanner(System.in);

    public UserClientThread(UserClient uc) {
        this.uc = uc;
    }

    public void run(){
        while(true){
            System.out.println("1.new TX 2.new TXes 3.show generates 4.send 5.quit");
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
                            int n = s.nextInt();
                            this.newTXes(n);
                            this.preCom = in;
                            break;
                        case 3:
                            showGens();
                            break;
                        case 4:
                            send();
                            break;
                        case 5:
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

    protected void newTX(){
        this.tx = this.uc.newRandomTX();
        System.out.println("Success to Generate a TX!");
    }

    protected void newTXes(int n){
        this.txes = this.uc.newRandomTXes(n);
        System.out.println("Success to Generate " + n + " TXes!");
    }

    protected void showGens(){
        if(this.preCom == 0)
            System.out.println("Nothing to show!");
        if(this.preCom == 1 || this.preCom == 2)
            System.out.println(this.tx.toString(true));
        if(this.preCom == 3) {
            System.out.println("TX Array in size " + this.txes.length + " : {");
            for (int i = 0; i < this.txes.length; i++) {
                System.out.print(this.txes[i].toString("    ", true));
                if(i < this.txes.length - 1)
                    System.out.println(",");
            }
            System.out.println("\n}");
        }
    }

    protected void send(){
        if(this.preCom == 0)
            System.out.println("Nothing to send!");
        if(this.preCom == 1 || this.preCom == 2) {
            this.uc.sendTX(tx);
            System.out.println("Send one TX!");
        }
        if(this.preCom == 3) {
            this.uc.sendTXes(txes);
            System.out.println("Send " + txes.length + " TXes!");
        }
    }


}
