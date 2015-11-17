package simulator.actors;

import static simulator.actors.Master.Events.Ping;
import static simulator.actors.Master.Events.Pong;
import static simulator.actors.Master.Events.Stop;
import static simulator.actors.Master.Events.Tick;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.core.async.AsyncLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import simulator.network.Network;
import simulator.network.UE;
import simulator.utils.WorkLoad;

public class Master extends UntypedActor {
    private static Logger log = LoggerFactory.getLogger(Master.class);
    private static ActorRef master;

    private long cellsNumber, devicesNumber, subscribersNumber;
    private List<ActorRef> cells = new ArrayList<ActorRef>();
    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private List<ActorRef> subscribers = new ArrayList<ActorRef>();
    private ActorRef network;

    private long step, startTime, duration;
    private WorkLoad workload = new WorkLoad();

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            log.info("Simulation started.");
            startTime = System.currentTimeMillis();

            master = getSelf();
            duration = ((Start) message).getDuration();
            cellsNumber = ((Start) message).getCellsNumber();
            devicesNumber = ((Start) message).getDevicesNumber();
            subscribersNumber = ((Start) message).getSubscribersNumber();

            addNetwork();
            addCells();
            addDevices();
            addSubscribers();

            initializeCells();
            initializeDevices();
            initializeSubscribers();
        } else if (message == Stop) {
            long stopTime = System.currentTimeMillis();
            log.info("Simulation completed after {} milliseconds.", stopTime - startTime);
            AsyncLogger.stop();
            getContext().system().terminate();
        } else if (message == Ping) {
            workload.removeWork();
            if (workload.isWorkDone()) {
                if (step < duration) {
                    step++;
                    getSelf().tell(Tick, getSelf());
                    getSelf().tell(Pong, getSelf());
                } else {
                    getSelf().tell(Stop, getSelf());
                }
            }
        } else if (message == Pong) {
            workload.addWork();
            getSender().tell(Ping, getSelf());
        } else if (message == Tick) {
            log.info("Step {}", step);
            Step simulationStep = new Step(step);

            workload.addWork(subscribersNumber);

            subscribers.stream().forEach(subscriber -> subscriber.tell(simulationStep, ActorRef.noSender()));
        } else {
            unhandled(message);
        }
    }

    public static ActorRef getMaster() {
        return master;
    }

    private void addNetwork() {
        network = context().system().actorOf(Props.create(Network.class), "Network");
    }

    private void addCells() {
        for (long i = 0L; i < cellsNumber; i++) {
            if (ThreadLocalRandom.current().nextInt(100) < 20) {
                if (ThreadLocalRandom.current().nextInt(100) < 20) {
                    cells.add(context().system().actorOf(Props.create(simulator.network._2G.GSM.Cell.class), "2G.GSM.Cell_" + i));
                } else if (ThreadLocalRandom.current().nextInt(100) < 30) {
                    cells.add(context().system().actorOf(Props.create(simulator.network._2G.GSM.GPRS.Cell.class), "2G.GPRS.Cell_" + i));
                } else {
                    cells.add(context().system().actorOf(Props.create(simulator.network._2G.GSM.GPRS.EDGE.Cell.class), "2G.EDGE.Cell_" + i));
                }
            } else if (ThreadLocalRandom.current().nextInt(100) < 40) {
                if (ThreadLocalRandom.current().nextInt(100) < 70) {
                    if (ThreadLocalRandom.current().nextInt(100) < 50) {
                        cells.add(context().system().actorOf(Props.create(simulator.network._3G.UMTS.HSPA.NodeB.class), "3G.HSPA.Cell_" + i));
                    } else {
                        cells.add(context().system().actorOf(Props.create(simulator.network._3G.UMTS.WCMDA.NodeB.class), "3G.WCMDA.Cell_" + i));
                    }
                } else {
                    cells.add(context().system().actorOf(Props.create(simulator.network._3G.CMDA2000.NodeB.class), "3G.CMDA2000.Cell_" + i));
                }
            } else {
                if (ThreadLocalRandom.current().nextInt(100) < 50) {
                    cells.add(context().system().actorOf(Props.create(simulator.network._4G.LTE.eNodeB.class), "4G.LTE.Cell_" + i));
                } else {
                    cells.add(context().system().actorOf(Props.create(simulator.network._4G.LTE.VoLTE.ENodeB.class), "4G.VoLTE.Cell_" + i));
                }
            }
        }
    }

    private void addDevices() {
        for (long i = 0L; i < devicesNumber; i++)
            devices.add(context().system().actorOf(Props.create(UE.class), "Device_" + i));
    }

    private void addSubscribers() {
        for (long i = 0L; i < subscribersNumber; i++)
            subscribers.add(context().system().actorOf(Props.create(Subscriber.class), "Subscriber_" + i));
    }

    private void initializeCells() {
        workload.addWork(cellsNumber);
        cells.stream().forEach(cell -> cell.tell(simulator.network.Cell.Events.ConnectToNetwork, network));
    }

    private void initializeDevices() {
        workload.addWork(devicesNumber);
        devices.stream().forEach(device -> device.tell(UE.Events.ConnectToCell, cells.get((int) ThreadLocalRandom.current().nextLong(cellsNumber))));
    }

    private void initializeSubscribers() {
        workload.addWork(devicesNumber);
        devices.stream().forEach(device -> subscribers.get((int) ThreadLocalRandom.current().nextLong(subscribersNumber)).tell(Subscriber.DeviceEvents.AddDevice, device));
    }

    public static final class Start implements Serializable {
        private static final long serialVersionUID = -5750159585853846166L;
        private long duration, cellsNumber, devicesNumber, subscribersNumber;

        public Start(long duration, long cellsNumber, long devicesNumber, long subscribersNumber) {
            setDuration(duration);
            setCellsNumber(cellsNumber);
            setDevicesNumber(devicesNumber);
            setSubscribersNumber(subscribersNumber);
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public long getCellsNumber() {
            return cellsNumber;
        }

        public void setCellsNumber(long cellsNumber) {
            this.cellsNumber = cellsNumber;
        }

        public long getDevicesNumber() {
            return devicesNumber;
        }

        public void setDevicesNumber(long devicesNumber) {
            this.devicesNumber = devicesNumber;
        }

        public long getSubscribersNumber() {
            return subscribersNumber;
        }

        public void setSubscribersNumber(long subscribersNumber) {
            this.subscribersNumber = subscribersNumber;
        }
    }

    public enum Events {
        Stop,
        Ping,
        Pong,
        Tick
    }

    public static class Step {
        private long step;

        public Step(long step) {
            this.step = step;
        }

        public long getStep() {
            return step;
        }

        public void setStep(long step) {
            this.step = step;
        }
    }
}
