package com.game.world;

import com.game.entity.Entity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.joml.Vector3f;

public class World {

    private final Map<String, Chunk> chunks;
    private final List<Entity> entities;
    private final ExecutorService executor;
    private final File worldFolder;

    public World() {
        chunks = new ConcurrentHashMap<>();
        entities = new ArrayList<>();
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        worldFolder = new File("world");
        if (!worldFolder.exists()) worldFolder.mkdir();
    }

    public void updateChunks(Vector3f playerPos, int renderDistance) {
        int playerCX = (int) Math.floor(playerPos.x / (float) Chunk.SIZE);
        int playerCZ = (int) Math.floor(playerPos.z / (float) Chunk.SIZE);

        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                int cx = playerCX + x;
                int cz = playerCZ + z;
                String key = cx + "," + cz;

                if (!chunks.containsKey(key)) {
                    Chunk chunk = new Chunk(cx, cz);
                    chunks.put(key, chunk);
                    executor.submit(() -> {
                        if (!chunk.load(worldFolder)) {
                            chunk.generateTerrain();
                        }
                    });
                }
            }
        }
        
        // unload far chunks
        if (chunks.size() > 200) {
            chunks.entrySet().removeIf(entry -> {
                String[] parts = entry.getKey().split(",");
                int cx = Integer.parseInt(parts[0]);
                int cz = Integer.parseInt(parts[1]);
                if (Math.abs(cx - playerCX) > renderDistance + 2 || Math.abs(cz - playerCZ) > renderDistance + 2) {
                    if (entry.getValue().isGenerated()) {
                        entry.getValue().save(worldFolder);
                    }
                    entry.getValue().cleanup();
                    return true;
                }
                return false;
            });
        }
    }

    public void saveAll() {
        for (Chunk chunk : chunks.values()) {
            if (chunk.isGenerated()) {
                chunk.save(worldFolder);
            }
        }
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void update(float deltaTime) {
        for (Entity e : entities) {
            e.update(deltaTime);
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
        if (chunk == null || !chunk.isGenerated()) return 0;

        int localX = x - (chunkX * Chunk.SIZE);
        int localZ = z - (chunkZ * Chunk.SIZE);
        
        return chunk.getBlock(localX, y, localZ);
    }

    public void setBlock(int x, int y, int z, byte type) {
        if (y < 0 || y >= Chunk.SIZE) return;

        int chunkX = (int) Math.floor(x / (float) Chunk.SIZE);
        int chunkZ = (int) Math.floor(z / (float) Chunk.SIZE);
        
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null && chunk.isGenerated()) {
            int localX = x - (chunkX * Chunk.SIZE);
            int localZ = z - (chunkZ * Chunk.SIZE);
            chunk.setBlock(localX, y, localZ, type);
            
            if (localX == 0) invalidateChunk(chunkX - 1, chunkZ);
            if (localX == Chunk.SIZE - 1) invalidateChunk(chunkX + 1, chunkZ);
            if (localZ == 0) invalidateChunk(chunkX, chunkZ - 1);
            if (localZ == Chunk.SIZE - 1) invalidateChunk(chunkX, chunkZ + 1);
        }
    }

    private void invalidateChunk(int cx, int cz) {
        Chunk c = getChunk(cx, cz);
        if (c != null && c.isGenerated()) {
            c.setBlock(0, 0, 0, c.getBlock(0, 0, 0)); 
        }
    }

    public boolean checkCollision(Vector3f pos) {
        float padding = 0.3f;
        int minX = (int) Math.floor(pos.x - padding);
        int maxX = (int) Math.floor(pos.x + padding);
        int minY = (int) Math.floor(pos.y - 1.5f);
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
        saveAll();
        executor.shutdownNow();
        for (Chunk c : chunks.values()) {
            c.cleanup();
        }
    }
}
