package com.kamesuta.yasuhati;

import javax.sound.sampled.*;

public class MicTester extends Thread {
    public static AudioFormat defaultFormat = new AudioFormat(11025f, 8, 1, true, true); //11.025khz, 8bit, mono, signed, big endian (changes nothing in 8 bit) ~8kb/s
    public static int defaultDataLenght = 1200; //send 1200 samples/packet by default
    public static double amplification = 1.0;
    public static double micLev = 1;

    private TargetDataLine mic = null;
    private boolean isRunning = true;

    public MicTester() throws LineUnavailableException {
        AudioFormat af = defaultFormat;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(af);
        mic.start();
    }

    @Override
    public void run() {
        while (isRunning) {
            Utils.sleep(10);
            if (mic.available() > 0) {
                byte[] buff = new byte[defaultDataLenght];
                mic.read(buff, 0, buff.length);
                double tot = 0;
                for (byte b : buff)
                    tot += amplification * Math.abs(b);
                tot *= 2.5;
                tot /= buff.length;
                //micLev.setValue((int) tot);
                micLev = tot;
            }
        }
    }

    private void close() {
        if (mic != null) mic.close();
        isRunning = false;
    }

    private static class Utils {
        public static void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ex) {
            }
        }
    }
}
