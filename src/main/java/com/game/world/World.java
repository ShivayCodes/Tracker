package com.game.world;

import java.util.HashMap;
import java.util.Map;
import org.joml.Vector3f;

public class World {

    private final Map<String, Chunk> chunks;

    public World() {
        chunks = new HashMap<>();
        // Generate a 4x4 chunk area
        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                Chunk chunk = new Chunk(x, z);
                chunks.put(x + "," + z, chunk);
            }
        }
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(chunkX + "," + chunkZ);
    }

    public Iterable<Chunk> getChunks() {
        return chunks.values();
    }

    public byte getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE) return 0;
        
        int chunkX = (int) Math.floor(x / (float) Chunk.SIZE);
        int chunkZ = (int) Math.floor(z / (float) Chunk.SIZE);
        
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return 0;

        int localX = x - (chunkX * Chunk.SIZE);
        int localZ = z - (chunkZ * Chunk.SIZE);
        
        return chunk.getBlock(localX, y, localZ);
    }

    public void setBlock(int x, int y, int z, byte type) {
        if (y < 0 || y >= Chunk.SIZE) return;

        int chunkX = (int) Math.floor(x / (float) Chunk.SIZE);
        int chunkZ = (int) Math.floor(z / (float) Chunk.SIZE);
        
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            int localX = x - (chunkX * Chunk.SIZE);
            int localZ = z - (chunkZ * Chunk.SIZE);
            chunk.setBlock(localX, y, localZ, type);
            
            // If on chunk border, neighbors might need remeshing
            if (localX == 0) invalidateChunk(chunkX - 1, chunkZ);
            if (localX == Chunk.SIZE - 1) invalidateChunk(chunkX + 1, chunkZ);
            if (localZ == 0) invalidateChunk(chunkX, chunkZ - 1);
            if (localZ == Chunk.SIZE - 1) invalidateChunk(chunkX, chunkZ + 1);
        }
    }

    private void invalidateChunk(int cx, int cz) {
        Chunk c = getChunk(cx, cz);
        if (c != null) {
            // A lazy hack to trigger remesh
            c.setBlock(0, 0, 0, c.getBlock(0, 0, 0)); 
        }
    }

    // AABB Collision Check
    public boolean checkCollision(Vector3f pos) {
        // Player is roughly 0.6 wide and 1.8 tall
        float padding = 0.3f;
        int minX = (int) Math.floor(pos.x - padding);
        int maxX = (int) Math.floor(pos.x + padding);
        int minY = (int) Math.floor(pos.y - 1.5f); // eye level is 1.5
        int maxY = (int) Math.floor(pos.y + 0.3f);
        int minZ = (int) Math.floor(pos.z - padding);
        int maxZ = (int) Math.floor(pos.z + padding);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (getBlock(x, y, z) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void cleanup() {
        for (Chunk c : chunks.values()) {
            c.cleanup();
        }
    }
}
