package simulator.actors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import simulator.actors.events.CellEvents.ConnectToNetwork;
import simulator.actors.events.DeviceEvents.AddDevice;
import simulator.actors.events.DeviceEvents.ConnectToCell;
import simulator.network.Network;
import simulator.network.UE;
import simulator.utils.WorkLoad;

public class Master extends AbstractActor {
    private static Logger log = LoggerFactory.getLogger(Master.class);
    private static ActorRef master;

    private long cellsNumber, devicesNumber, subscribersNumber;
    private final List<ActorRef> cells = new ArrayList<ActorRef>();
    private final List<ActorRef> devices = new ArrayList<ActorRef>();
    private final List<ActorRef> subscribers = new ArrayList<ActorRef>();
    private ActorRef network;

    private long step, startTime, duration;
    private final WorkLoad workload = new WorkLoad();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Start.class, message -> {
            log.info("Simulation started.");
            startTime = System.currentTimeMillis();

            master = getSelf();
            duration = message.getDuration();
            cellsNumber = message.getCellsNumber();
            devicesNumber = message.getDevicesNumber();
            subscribersNumber = message.getSubscribersNumber();

            addNetwork();
            addCells();
            addDevices();
            addSubscribers();

            initializeCells();
            initializeDevices();
            initializeSubscribers();
        }).match(Stop.class, message -> {
            final long stopTime = System.currentTimeMillis();
            log.info("Simulation completed after {} milliseconds.", stopTime - startTime);

            ((LifeCycle) LogManager.getContext(false)).stop();

            getContext().system().terminate();
        }).match(Ping.class, message -> {
            workload.removeWork();
            if (workload.isWorkDone()) {
                if (step < duration) {
                    step++;
                    getSelf().tell(new Tick(), getSelf());
                    getSelf().tell(new Pong(), getSelf());
                } else {
                    getSelf().tell(new Stop(), getSelf());
                }
            }
        }).match(Pong.class, message -> {
            workload.addWork();
            getSender().tell(new Ping(), getSelf());
        }).match(Tick.class, message -> {
            log.info("Step {}", step);
            final Step simulationStep = new Step(step);

            workload.addWork(subscribersNumber + devicesNumber);
            subscribers.stream().forEach(subscriber -> subscriber.tell(simulationStep, ActorRef.noSender()));
            devices.stream().forEach(device -> device.tell(simulationStep, ActorRef.noSender()));
        }).build();
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
                    cells.add(context().system().actorOf(Props.create(simulator.network._4G.LTE.ENodeB.class), "4G.LTE.Cell_" + i));
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
        cells.stream().forEach(cell -> cell.tell(new ConnectToNetwork(cell, network, null), ActorRef.noSender()));
    }

    private void initializeDevices() {
        workload.addWork(devicesNumber);
        devices.stream().forEach(device -> device.tell(new ConnectToCell(device, cells.get((int) ThreadLocalRandom.current().nextLong(cellsNumber)), null),
                ActorRef.noSender()));
    }

    private void initializeSubscribers() {
        workload.addWork(devicesNumber);
        devices.stream().forEach(device -> subscribers.get((int) ThreadLocalRandom.current().nextLong(subscribersNumber)).tell(new AddDevice(), device));
    }

    public static final class Start implements Serializable {
        private static final long serialVersionUID = -5750159585853846166L;
        private long duration, cellsNumber, devicesNumber, subscribersNumber;

        public Start(final long duration, final long cellsNumber, final long devicesNumber, final long subscribersNumber) {
            setDuration(duration);
            setCellsNumber(cellsNumber);
            setDevicesNumber(devicesNumber);
            setSubscribersNumber(subscribersNumber);
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(final long duration) {
            this.duration = duration;
        }

        public long getCellsNumber() {
            return cellsNumber;
        }

        public void setCellsNumber(final long cellsNumber) {
            this.cellsNumber = cellsNumber;
        }

        public long getDevicesNumber() {
            return devicesNumber;
        }

        public void setDevicesNumber(final long devicesNumber) {
            this.devicesNumber = devicesNumber;
        }

        public long getSubscribersNumber() {
            return subscribersNumber;
        }

        public void setSubscribersNumber(final long subscribersNumber) {
            this.subscribersNumber = subscribersNumber;
        }
    }

    public static final class Stop implements Serializable {
        private static final long serialVersionUID = -2875658030333261939L;
    }

    public static final class Ping implements Serializable {
        private static final long serialVersionUID = 20313252305815824L;
    }

    public static final class Pong implements Serializable {
        private static final long serialVersionUID = -4495093709702139364L;
    }

    public static final class Tick implements Serializable {
        private static final long serialVersionUID = 415624215899148915L;
    }

    public static final class Step {
        private long step;

        public Step(final long step) {
            this.step = step;
        }

        public long getStep() {
            return step;
        }

        public void setStep(final long step) {
            this.step = step;
        }
    }
}
