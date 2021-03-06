package simulator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import simulator.actors.Master;

public class Simulator {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Arguments: <ticks> <cells> <devices> <subscribers>");
            return;
        }

        ActorSystem system = ActorSystem.create("simulation");
        ActorRef master = system.actorOf(Props.create(Master.class), "master");
        master.tell(new Master.Start(Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2]), Long.parseLong(args[3])), ActorRef.noSender());
    }
}
