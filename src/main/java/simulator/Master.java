package simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.core.async.AsyncLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.network.Device;
import simulator.network.Network;
import simulator.utils.WorkLoad;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class Master extends UntypedActor {
    private static ActorRef master;

    private long step, startTime, duration, cellsNumber, devicesNumber;
    private List<ActorRef> devices = new ArrayList<ActorRef>();
    private List<ActorRef> cells = new ArrayList<ActorRef>();
    private ActorRef network;
    private WorkLoad workload = new WorkLoad();
    private static Logger log = LoggerFactory.getLogger(Master.class);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            log.info("Simulation started.");
            startTime = System.currentTimeMillis();

            master = getSelf();
            duration = ((Start) message).getDuration();
            cellsNumber = ((Start) message).getCellsNumber();
            devicesNumber = ((Start) message).getDevicesNumber();

            addNetwork();
            addCells();
            addSubscribers();

            initializeCells();
            initializeSubscribers();
        } else if (message == Events.Stop) {
            long stopTime = System.currentTimeMillis();
            log.info("Simulation completed after {} milliseconds.", stopTime - startTime);
            AsyncLogger.stop();
            getContext().system().terminate();
        } else if (message == Events.Ping) {
            workload.removeWork();
            if (workload.isWorkDone()) {
                if (step < duration) {
                    step++;
                    getSelf().tell(Events.Tick, getSelf());
                    getSelf().tell(Events.Pong, getSelf());
                } else {
                    getSelf().tell(Events.Stop, getSelf());
                }
            }
        } else if (message == Events.Pong) {
            workload.addWork();
            getSender().tell(Events.Ping, getSelf());
        } else if (message == Events.Tick) {
            log.info("{}", step);
            workload.addWork(devicesNumber);
            devices.stream().forEach(subscriber -> subscriber.tell(Device.Events.MakeVoiceCall, devices.get(ThreadLocalRandom.current().nextInt((int) devicesNumber))));
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
                        cells.add(context().system().actorOf(Props.create(simulator.network._3G.UMTS.HSPA.Cell.class), "3G.HSPA.Cell_" + i));
                    } else {
                        cells.add(context().system().actorOf(Props.create(simulator.network._3G.UMTS.WCMDA.Cell.class), "3G.WCMDA.Cell_" + i));
                    }
                } else {
                    cells.add(context().system().actorOf(Props.create(simulator.network._3G.CMDA2000.Cell.class), "3G.CMDA2000.Cell_" + i));
                }
            } else {
                if (ThreadLocalRandom.current().nextInt(100) < 50) {
                    cells.add(context().system().actorOf(Props.create(simulator.network._4G.LTE.Cell.class), "4G.LTE.Cell_" + i));
                } else {
                    cells.add(context().system().actorOf(Props.create(simulator.network._4G.LTE.VoLTE.Cell.class), "4G.VoLTE.Cell_" + i));
                }
            }
        }
    }

    private void addSubscribers() {
        for (long i = 0L; i < devicesNumber; i++)
            devices.add(context().system().actorOf(Props.create(Device.class), "Device_" + i));
    }

    private void initializeCells() {
        workload.addWork(cellsNumber);
        cells.stream().forEach(cell -> cell.tell(simulator.network.Cell.Events.ConnectToNetwork, network));
    }

    private void initializeSubscribers() {
        workload.addWork(devicesNumber);
        devices.stream().forEach(device -> device.tell(Device.Events.ConnectToCell, cells.get((int) ThreadLocalRandom.current().nextLong(cellsNumber))));
    }

    public static final class Start implements Serializable {
        private static final long serialVersionUID = -5750159585853846166L;
        private long duration, cellsNumber, devicesNumber;

        public Start(long duration, long cellsNumber, long subscribersNumber) {
            setDuration(duration);
            setCellsNumber(cellsNumber);
            setDevicesNumber(subscribersNumber);
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
    }

    public enum Events {
        Stop,
        Ping,
        Pong,
        Tick
    }
}
