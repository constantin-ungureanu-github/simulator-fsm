// Clean and Compile

sbt clean compile


// Build clean Eclipse project

sbt eclipse:clean eclipse


// Run simulator for 100 ticks with 1.000 cells and 100.000 devices and 120.000 subscribers

sbt "run 100 1000 100000 120000" [Java Options]


// Run simulator for 10 ticks with 10.000 cells and 2.000.000 devices and 2.000.000 subscribers

sbt "run 10 10000 2000000 2000000" [Java Options]


// Build and run local executable package

sbt stage universal:packageBin

./target/universal/stage/bin/simulator 100 1000 100000 120000 [Java Options]


// Build and run Docker image

sbt stage docker:publishLocal

docker run simulator:0.10 100 1000 100000 120000 [Java Options]


// Options to JVM (~ 8GB RAM / 1M actors)

-J-server -J-XX:CompileCommand=dontinline -J-XX:+UseNUMA -J-XX:+UseTLAB -J-XX:+UseBiasedLocking -J-XX:+UseCondCardMark -J-XX:+UseParallelGC -J-Xss4M -J-Xms4G -J-Xmx4G

-J-server -J-XX:CompileCommand=dontinline -J-XX:+UseNUMA -J-XX:+UseTLAB -J-XX:+UseBiasedLocking -J-XX:+UseCondCardMark -J-XX:+UseParallelGC -J-Xss4M -J-Xms28G -J-Xmx28G

-J-server -J-XX:CompileCommand=dontinline -J-XX:+UseNUMA -J-XX:+UseTLAB -J-XX:+UseBiasedLocking -J-XX:+UseCondCardMark -J-XX:+UseParNewGC -J-XX:+UseConcMarkSweepGC -J-Xss1M -J-Xms4G -J-Xmx4G

-J-server -J-XX:CompileCommand=dontinline -J-XX:+UseNUMA -J-XX:+UseTLAB -J-XX:+UseBiasedLocking -J-XX:+UseCondCardMark -J-XX:+UseParNewGC -J-XX:+UseConcMarkSweepGC -J-Xss4M -J-Xms28G -J-Xmx28G
