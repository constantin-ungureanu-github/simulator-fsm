package simulator;

import simulator.actors.Master;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Simulator {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage run <ticks> <cells> <devices> <subscribers>");
            return;
        }

        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        ActorSystem system = ActorSystem.create("simulation");
        ActorRef master = system.actorOf(Props.create(Master.class), "master");
        master.tell(new Master.Start(Long.parseLong(args[0]), Long.parseLong(args[1]), Long.parseLong(args[2]), Long.parseLong(args[3])), ActorRef.noSender());
    }
}
