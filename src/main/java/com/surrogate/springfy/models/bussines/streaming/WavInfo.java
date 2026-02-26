package com.surrogate.springfy.models.bussines.streaming;



public class WavInfo {
    int sampleRate;
    int channels;
    int bitsPerSample;
    long dataSize;

    public WavInfo(int sampleRate, int channels, int bitsPerSample, long dataSize) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.dataSize = dataSize;
    }
   public  double getDurationSeconds() {
        long byteRate = (long) sampleRate * channels * bitsPerSample / 8;
        return (double) dataSize / byteRate;
    }
}