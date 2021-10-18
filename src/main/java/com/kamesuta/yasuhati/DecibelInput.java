package com.kamesuta.yasuhati;

import javax.sound.sampled.*;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * デシベルを取得するクラス
 */
public class DecibelInput implements Closeable {
    /**
     * 音声入力ライン
     */
    private final TargetDataLine targetDataLine;
    /**
     * 前回のデシベル
     */
    private Decibel lastDecibel = new Decibel(-100, -100);

    /**
     * 初期化
     *
     * @throws LineUnavailableException マイクが使用不可能である場合
     */
    public DecibelInput() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(16000, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
    }

    /**
     * 終了
     */
    @Override
    public void close() {
        targetDataLine.close();
    }

    /**
     * デシベルを測定し取得する
     *
     * @return デシベル
     */
    public Decibel getDecibel() {
        byte[] buffer = new byte[1024];
        boolean stopRecording = false;

        int numBytesRead = targetDataLine.read(buffer, 0, buffer.length);
        //short[] samples = encodeToSample(buffer, buffer.length);
        // process samples - calculate decibels

        if (numBytesRead > 0) {
            short[] sample = encodeToSample(buffer, numBytesRead);
            lastDecibel = Decibel.calculatePeakAndRms(sample);
        }

        return lastDecibel;
    }

    private static short[] encodeToSample(byte[] srcBuffer, int numBytes) {
        byte[] tempBuffer = new byte[2];
        int nSamples = numBytes / 2;
        short[] samples = new short[nSamples];  // 16-bit signed value

        for (int i = 0; i < nSamples; i++) {
            tempBuffer[0] = srcBuffer[2 * i];
            tempBuffer[1] = srcBuffer[2 * i + 1];
            samples[i] = bytesToShort(tempBuffer);
        }

        return samples;
    }

    private static short bytesToShort(byte[] buffer) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(buffer[0]);
        bb.put(buffer[1]);
        return bb.getShort(0);
    }

    public static class Decibel {
        public final double average;
        public final double peak;

        private Decibel(double average, double peek) {
            this.average = average;
            this.peak = peek;
        }

        public static Decibel calculatePeakAndRms(short[] samples) {
            double sumOfSampleSq = 0.0;    // sum of square of normalized samples.
            double peakSample = 0.0;     // peak sample.

            for (short sample : samples) {
                double normSample = (double) sample / 32767;  // normalized the sample with maximum value.
                sumOfSampleSq += (normSample * normSample);
                if (Math.abs(sample) > peakSample) {
                    peakSample = Math.abs(sample);
                }
            }

            double rms = 10 * Math.log10(sumOfSampleSq / samples.length);
            double peak = 20 * Math.log10(peakSample / 32767);

            return new Decibel(rms, peak);
        }
    }

    public static void main(String... args) throws LineUnavailableException {
        try (DecibelInput input = new DecibelInput()) {
            boolean stopRecording = false;

            while (!stopRecording) {
                DecibelInput.Decibel decibel = input.getDecibel();
                System.out.printf("ave: %f, max: %f%n", decibel.average, decibel.peak);
            }
        }
    }
}
