package com.example.storit;

public class Chunk {
    int chunkId;
    String chunkIv;

    public Chunk(int chunkId, String chunkIv) {
        this.chunkId = chunkId;
        this.chunkIv = chunkIv;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }

    public String getChunkIv() {
        return chunkIv;
    }

    public void setChunkIv(String chunkIv) {
        this.chunkIv = chunkIv;
    }
}
