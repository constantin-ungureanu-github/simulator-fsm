// Clean and Compile

sbt clean compile

// Run simulator for 100 ticks with 1.000 cells and 100.000 devices and 120.000 subscribers

sbt "run 100 1000 100000 120000" [Java Options]

// Run simulator for 100 ticks with 10.000 cells and 1.000.000 devices and 1.000.000 subscribers

sbt "run 100 10000 1000000 1000000" [Java Options]

// Build Eclipse Project

sbt eclipse

// Build executable package

sbt stage universal:packageBin

// Run executable package

./target/universal/stage/bin/simulator 100 1000 100000  [Java Options]

// Options to JVM

-J-server -J-XX:+UseNUMA -J-XX:+UseCondCardMark -J-XX:+UseBiasedLocking -J-XX:+UseParallelGC -J-XX:CompileCommand=dontinline -J-Xss4M -J-Xms4G -J-Xmx4G

-J-server -J-XX:+UseNUMA -J-XX:+UseCondCardMark -J-XX:+UseBiasedLocking -J-XX:+UseParallelGC -J-XX:CompileCommand=dontinline -J-Xss4M -J-Xms28G -J-Xmx28G
