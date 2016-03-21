#!/bin/sh
javac Pitch.java
javah Pitch
gcc -I/usr/lib/jvm/java-1.8.0-openjdk/include -I/usr/lib/jvm/java-1.8.0-openjdk/include/linux pitch.cpp -o libpitch.so -shared
java Pitch

