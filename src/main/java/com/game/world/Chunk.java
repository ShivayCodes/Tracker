package com.game.world;

import com.game.Mesh;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public static final int SIZE = 16;
    private final byte[][][] blocks;
    private Mesh mesh;
    private final int chunkX;
    private final int chunkZ;
    private boolean isDirty = true;
    private boolean isGenerated = false;

    private static final float TEX_WIDTH = 0.2f; 
    
    private static final PerlinNoise surfaceNoise = new PerlinNoise(12345);
    private static final PerlinNoise caveNoise = new PerlinNoise(54321);

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        blocks = new byte[SIZE][SIZE][SIZE];
    }

    public void generateTerrain() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;
                double noiseVal = surfaceNoise.noise(worldX * 0.05, 0, worldZ * 0.05);
                int height = 8 + (int)(noiseVal * 6);
                if (height >= SIZE) height = SIZE - 1;
                if (height < 2) height = 2;
                for (int y = 0; y < SIZE; y++) {
                    if (y < height - 1) blocks[x][y][z] = 3;
                    else if (y == height - 1) blocks[x][y][z] = 1;
                    else if (y == height) blocks[x][y][z] = 2;
                    else blocks[x][y][z] = 0;
                    if (y < height - 1 && blocks[x][y][z] != 0) {
                        double caveVal = caveNoise.noise(worldX * 0.12, y * 0.12, worldZ * 0.12);
                        if (caveVal > 0.45) blocks[x][y][z] = 0;
                    }
                }
            }
        }
        decorate();
        isGenerated = true;
    }

    private void decorate() {
        java.util.Random rand = new java.util.Random((long)chunkX * 12345 + (long)chunkZ * 67890);
        for (int x = 2; x < SIZE - 2; x++) {
            for (int z = 2; z < SIZE - 2; z++) {
                for (int y = SIZE - 2; y >= 2; y--) {
                    if (blocks[x][y][z] == 2) {
                        if (rand.nextFloat() > 0.98f) spawnTree(x, y + 1, z);
                        break;
                    }
                }
            }
        }
    }

    private void spawnTree(int x, int y, int z) {
        for (int i = 0; i < 4; i++) if (y + i < SIZE) blocks[x][y + i][z] = 4;
        for (int ix = -2; ix <= 2; ix++) {
            for (int iy = 2; iy <= 4; iy++) {
                for (int iz = -2; iz <= 2; iz++) {
                    if (Math.abs(ix) == 2 && Math.abs(iz) == 2) continue; 
                    if (x + ix >= 0 && x + ix < SIZE && y + iy >= 0 && y + iy < SIZE && z + iz >= 0 && z + iz < SIZE) {
                        if (blocks[x + ix][y + iy][z + iz] == 0) blocks[x + ix][y + iy][z + iz] = 5;
                    }
                }
            }
        }
    }

    public void save(File folder) {
        File file = new File(folder, "chunk_" + chunkX + "_" + chunkZ + ".dat");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) fos.write(blocks[x][y]);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean load(File folder) {
        File file = new File(folder, "chunk_" + chunkX + "_" + chunkZ + ".dat");
        if (!file.exists()) return false;
        try (FileInputStream fis = new FileInputStream(file)) {
            for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) fis.read(blocks[x][y]);
            isGenerated = true; isDirty = true; return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public boolean isGenerated() { return isGenerated; }

    public void setBlock(int x, int y, int z, byte blockType) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE) {
            blocks[x][y][z] = blockType; isDirty = true;
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) return 0;
        return blocks[x][y][z];
    }

    public void rebuildMesh() {
        if (!isGenerated) return;
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> aoValues = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int indexCount = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte blockId = blocks[x][y][z];
                    if (blockId == 0) continue;
                    float wx = chunkX * SIZE + x; float wy = y; float wz = chunkZ * SIZE + z;
                    float uStart = (blockId - 1) * TEX_WIDTH; float uEnd = uStart + TEX_WIDTH;

                    // +Z Front
                    if (getBlock(x, y, z + 1) == 0) {
                        addVertices(positions, wx, wy, wz+1, wx+1, wy, wz+1, wx+1, wy+1, wz+1, wx, wy+1, wz+1);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, 0, 0, 1);
                        addAO(aoValues, x, y, z + 1, 0, 0, 1);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                    // -Z Back
                    if (getBlock(x, y, z - 1) == 0) {
                        addVertices(positions, wx+1, wy, wz, wx, wy, wz, wx, wy+1, wz, wx+1, wy+1, wz);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, 0, 0, -1);
                        addAO(aoValues, x, y, z - 1, 0, 0, -1);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                    // +Y Top
                    if (getBlock(x, y + 1, z) == 0) {
                        addVertices(positions, wx, wy+1, wz+1, wx+1, wy+1, wz+1, wx+1, wy+1, wz, wx, wy+1, wz);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, 0, 1, 0);
                        addAO(aoValues, x, y + 1, z, 0, 1, 0);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                    // -Y Bottom
                    if (getBlock(x, y - 1, z) == 0) {
                        addVertices(positions, wx, wy, wz, wx+1, wy, wz, wx+1, wy, wz+1, wx, wy, wz+1);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, 0, -1, 0);
                        addAO(aoValues, x, y - 1, z, 0, -1, 0);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                    // +X Right
                    if (getBlock(x + 1, y, z) == 0) {
                        addVertices(positions, wx+1, wy, wz+1, wx+1, wy, wz, wx+1, wy+1, wz, wx+1, wy+1, wz+1);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, 1, 0, 0);
                        addAO(aoValues, x + 1, y, z, 1, 0, 0);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                    // -X Left
                    if (getBlock(x - 1, y, z) == 0) {
                        addVertices(positions, wx, wy, wz, wx, wy, wz+1, wx, wy+1, wz+1, wx, wy+1, wz);
                        addTexCoords(textCoords, uStart, 1.0f, uEnd, 1.0f, uEnd, 0.0f, uStart, 0.0f);
                        addNormals(normals, -1, 0, 0);
                        addAO(aoValues, x - 1, y, z, -1, 0, 0);
                        addIndices(indices, indexCount); indexCount += 4;
                    }
                }
            }
        }
        float[] posArr = toFloatArray(positions); float[] texArr = toFloatArray(textCoords);
        float[] normArr = toFloatArray(normals); float[] aoArr = toFloatArray(aoValues);
        int[] idxArr = new int[indices.size()]; for (int i = 0; i < indices.size(); i++) idxArr[i] = indices.get(i);
        if (mesh != null) mesh.cleanup();
        mesh = posArr.length > 0 ? new Mesh(posArr, texArr, normArr, aoArr, idxArr) : null;
        isDirty = false;
    }

    private void addAO(List<Float> aoList, int x, int y, int z, int nx, int ny, int nz) {
        if (nx != 0) {
            aoList.add(calcAO(x, y-1, z-1, x, y-1, z, x, y, z-1));
            aoList.add(calcAO(x, y-1, z+1, x, y-1, z, x, y, z+1));
            aoList.add(calcAO(x, y+1, z+1, x, y+1, z, x, y, z+1));
            aoList.add(calcAO(x, y+1, z-1, x, y+1, z, x, y, z-1));
        } else if (ny != 0) {
            aoList.add(calcAO(x-1, y, z+1, x-1, y, z, x, y, z+1));
            aoList.add(calcAO(x+1, y, z+1, x+1, y, z, x, y, z+1));
            aoList.add(calcAO(x+1, y, z-1, x+1, y, z, x, y, z-1));
            aoList.add(calcAO(x-1, y, z-1, x-1, y, z, x, y, z-1));
        } else {
            aoList.add(calcAO(x-1, y-1, z, x-1, y, z, x, y-1, z));
            aoList.add(calcAO(x+1, y-1, z, x+1, y, z, x, y-1, z));
            aoList.add(calcAO(x+1, y+1, z, x+1, y, z, x, y+1, z));
            aoList.add(calcAO(x-1, y+1, z, x-1, y, z, x, y+1, z));
        }
    }

    private float calcAO(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3) {
        int side1 = getBlock(x1, y1, z1) != 0 ? 1 : 0;
        int side2 = getBlock(x2, y2, z2) != 0 ? 1 : 0;
        int corner = getBlock(x3, y3, z3) != 0 ? 1 : 0;
        if (side1 == 1 && side2 == 1) return 0.2f; // Much darker corners
        return 1.0f - (side1 + side2 + corner) * 0.25f; // Steeper gradient for "smooth" feel
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()]; for (int i = 0; i < list.size(); i++) arr[i] = list.get(i); return arr;
    }

    private void addVertices(List<Float> positions, float... verts) { for (float v : verts) positions.add(v); }
    private void addNormals(List<Float> normals, float nx, float ny, float nz) { for (int i = 0; i < 4; i++) { normals.add(nx); normals.add(ny); normals.add(nz); } }
    private void addTexCoords(List<Float> textCoords, float... coords) { for (float c : coords) textCoords.add(c); }
    private void addIndices(List<Integer> indices, int offset) { indices.add(offset); indices.add(offset + 1); indices.add(offset + 2); indices.add(offset + 2); indices.add(offset + 3); indices.add(offset); }

    public Mesh getMesh() { if (isDirty) rebuildMesh(); return mesh; }
    public void cleanup() { if (mesh != null) mesh.cleanup(); }
}
