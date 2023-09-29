/*
 *
 *  *     HMCLeaves
 *  *     Copyright (C) 2022  Hibiscus Creative Studios
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package io.github.fisher2911.hmcleaves.database;

import com.google.common.collect.Multimap;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.fisher2911.hmcleaves.HMCLeaves;
import io.github.fisher2911.hmcleaves.cache.ChunkBlockCache;
import io.github.fisher2911.hmcleaves.config.LeavesConfig;
import io.github.fisher2911.hmcleaves.data.BlockData;
import io.github.fisher2911.hmcleaves.world.ChunkPosition;
import io.github.fisher2911.hmcleaves.world.Position;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoDBDatabase implements Database {

    private final HMCLeaves plugin;
    private MongoClient mongoClient;
    private MongoDatabase worldsDatabase;
    private final LeavesConfig config;
    private final ExecutorService writeExecutor;
    private final ExecutorService readExecutor;
    // 528*528 chunks, not 16x16 chunks
    private final Map<UUID, Multimap<ChunkPosition, Integer>> possibleWorldDefaultLayers;
    private final Set<ChunkPosition> currentlyLoadingChunks;

    protected MongoDBDatabase(HMCLeaves plugin) {
        this.plugin = plugin;
        this.config = plugin.getLeavesConfig();
        this.writeExecutor = Executors.newSingleThreadExecutor();
        this.readExecutor = Executors.newFixedThreadPool(5);
        this.possibleWorldDefaultLayers = new ConcurrentHashMap<>();
        this.currentlyLoadingChunks = ConcurrentHashMap.newKeySet();
        this.mongoClient = MongoClients.create(config.getMongoDbUri());
        this.worldsDatabase = mongoClient.getDatabase("worlds");
    }

    @Override
    public boolean isLayerLoaded(ChunkPosition smallChunk) {
        return false;
    }

    @Override
    public Collection<Integer> getPossibleWorldDefaultLayers(ChunkPosition smallChunk) {
        return null;
    }

    @Override
    public void load() {

    }

    @Override
    public void doDatabaseWriteAsync(Runnable runnable) {

    }

    @Override
    public void doDatabaseReadAsync(Runnable runnable) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isChunkLoaded(ChunkPosition chunkPosition) {
        return false;
    }

    @Override
    public void setChunkLoaded(ChunkPosition chunkPosition) {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public void saveBlocksInChunk(ChunkBlockCache chunk) {

    }

    @Override
    public Map<Position, BlockData> getBlocksInChunk(ChunkPosition chunkPosition, LeavesConfig config) {
        return null;
    }

    @Override
    public void saveDefaultDataLayers(UUID worldUUID, Collection<Integer> yLayers, ChunkPosition smallChunk) throws SQLException {

    }

    @Override
    public void loadAllDefaultPossibleLayersInWorld(UUID worldUUID, ChunkPosition smallChunk) {

    }

}
