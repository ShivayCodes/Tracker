package com.game.world;

import com.game.Mesh;
import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public static final int SIZE = 16;
    private final byte[][][] blocks;
    private Mesh mesh;
    private final int chunkX;
    private final int chunkZ;
    private boolean isDirty = true;

    // UV coordinates for the texture atlas (48x16 pixels total)
    // 3 tiles: Dirt (0), Grass (1), Stone (2)
    private static final float TEX_WIDTH = 1.0f / 3.0f;
    
    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        blocks = new byte[SIZE][SIZE][SIZE];
        generateTerrain();
    }

    private void generateTerrain() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;
                
                int height = 8 + (int)(Math.sin(worldX * 0.1) * 3 + Math.cos(worldZ * 0.1) * 3);
                if (height >= SIZE) height = SIZE - 1;
                if (height < 1) height = 1;

                for (int y = 0; y < SIZE; y++) {
                    if (y < height - 1) {
                        blocks[x][y][z] = 3; // Stone
                    } else if (y == height - 1) {
                        blocks[x][y][z] = 1; // Dirt
                    } else if (y == height) {
                        blocks[x][y][z] = 2; // Grass
                    } else {
                        blocks[x][y][z] = 0; // Air
                    }
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, byte blockType) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE) {
            blocks[x][y][z] = blockType;
            isDirty = true;
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return 0; // Air outside bounds
        }
        return blocks[x][y][z];
    }

    public void rebuildMesh() {
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int indexCount = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte blockId = blocks[x][y][z];
                    if (blockId == 0) continue;

                    float wx = chunkX * SIZE + x;
                    float wy = y;
                    float wz = chunkZ * SIZE + z;

                    float uStart = (blockId - 1) * TEX_WIDTH;
                    float uEnd = uStart + TEX_WIDTH;
                    float vStart = 0.0f;
                    float vEnd = 1.0f;

                    // +Z Front
                    if (getBlock(x, y, z + 1) == 0) {
                        addVertices(positions, wx, wy, wz+1, wx+1, wy, wz+1, wx+1, wy+1, wz+1, wx, wy+1, wz+1);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                    // -Z Back
                    if (getBlock(x, y, z - 1) == 0) {
                        addVertices(positions, wx+1, wy, wz, wx, wy, wz, wx, wy+1, wz, wx+1, wy+1, wz);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                    // +Y Top
                    if (getBlock(x, y + 1, z) == 0) {
                        addVertices(positions, wx, wy+1, wz+1, wx+1, wy+1, wz+1, wx+1, wy+1, wz, wx, wy+1, wz);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                    // -Y Bottom
                    if (getBlock(x, y - 1, z) == 0) {
                        addVertices(positions, wx, wy, wz, wx+1, wy, wz, wx+1, wy, wz+1, wx, wy, wz+1);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                    // +X Right
                    if (getBlock(x + 1, y, z) == 0) {
                        addVertices(positions, wx+1, wy, wz+1, wx+1, wy, wz, wx+1, wy+1, wz, wx+1, wy+1, wz+1);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                    // -X Left
                    if (getBlock(x - 1, y, z) == 0) {
                        addVertices(positions, wx, wy, wz, wx, wy, wz+1, wx, wy+1, wz+1, wx, wy+1, wz);
                        addTexCoords(textCoords, uStart, vStart, uEnd, vEnd);
                        addIndices(indices, indexCount);
                        indexCount += 4;
                    }
                }
            }
        }

        float[] posArr = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) posArr[i] = positions.get(i);

        float[] texArr = new float[textCoords.size()];
        for (int i = 0; i < textCoords.size(); i++) texArr[i] = textCoords.get(i);

        int[] idxArr = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) idxArr[i] = indices.get(i);

        if (mesh != null) mesh.cleanup();
        mesh = posArr.length > 0 ? new Mesh(posArr, texArr, idxArr) : null;
        isDirty = false;
    }

    private void addVertices(List<Float> positions, float... verts) {
        for (float v : verts) positions.add(v);
    }

    private void addTexCoords(List<Float> textCoords, float uStart, float vStart, float uEnd, float vEnd) {
        textCoords.add(uStart); textCoords.add(vEnd);
        textCoords.add(uEnd);   textCoords.add(vEnd);
        textCoords.add(uEnd);   textCoords.add(vStart);
        textCoords.add(uStart); textCoords.add(vStart);
    }

    private void addIndices(List<Integer> indices, int offset) {
        indices.add(offset);
        indices.add(offset + 1);
        indices.add(offset + 2);
        indices.add(offset + 2);
        indices.add(offset + 3);
        indices.add(offset);
    }

    public Mesh getMesh() {
        if (isDirty) rebuildMesh();
        return mesh;
    }

    public void cleanup() {
        if (mesh != null) mesh.cleanup();
    }
}
