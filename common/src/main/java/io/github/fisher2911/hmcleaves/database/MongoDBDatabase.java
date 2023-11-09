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
import com.google.common.collect.Multimaps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.fisher2911.hmcleaves.HMCLeaves;
import io.github.fisher2911.hmcleaves.cache.ChunkBlockCache;
import io.github.fisher2911.hmcleaves.config.LeavesConfig;
import io.github.fisher2911.hmcleaves.data.AgeableData;
import io.github.fisher2911.hmcleaves.data.BlockData;
import io.github.fisher2911.hmcleaves.data.CaveVineData;
import io.github.fisher2911.hmcleaves.data.LeafData;
import io.github.fisher2911.hmcleaves.data.LogData;
import io.github.fisher2911.hmcleaves.data.SaplingData;
import io.github.fisher2911.hmcleaves.world.ChunkPosition;
import io.github.fisher2911.hmcleaves.world.Position;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoDBDatabase implements Database {

    private static final UpdateOptions UPDATE_OPTIONS = new UpdateOptions().upsert(true);

    private final HMCLeaves plugin;
    private final MongoClient mongoClient;
    private final MongoDatabase worldsDatabase;
    private final MongoDatabase worldDefaultLayersDatabase;
    private final LeavesConfig config;
    private final ExecutorService writeExecutor;
    private final ExecutorService readExecutor;
    private final LeafDatabase leafDatabase;

    protected MongoDBDatabase(HMCLeaves plugin) {
        this.plugin = plugin;
        this.config = plugin.getLeavesConfig();
        this.writeExecutor = Executors.newSingleThreadExecutor();
        this.readExecutor = Executors.newFixedThreadPool(5);
        this.mongoClient = MongoClients.create(config.getMongoDbUri());
        this.worldsDatabase = mongoClient.getDatabase("worlds");
        this.worldDefaultLayersDatabase = mongoClient.getDatabase("worldDefaultLayers");
        this.leafDatabase = new LeafDatabase();
    }

    @Override
    public boolean isLayerLoaded(ChunkPosition smallChunk) {
        return this.leafDatabase.isLayerLoaded(smallChunk);
    }

    @Override
    public Collection<Integer> getPossibleWorldDefaultLayers(ChunkPosition smallChunk) {
        return this.leafDatabase.getPossibleWorldDefaultLayers(smallChunk);
    }

    @Override
    public void load() {

    }

    @Override
    public void doDatabaseWriteAsync(Runnable runnable) {
        if (this.writeExecutor.isShutdown() || this.writeExecutor.isTerminated()) {
            runnable.run();
            return;
        }
        this.writeExecutor.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void doDatabaseReadAsync(Runnable runnable) {
        this.readExecutor.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    private static final String ID_KEY = "_id";
    private static final String CHUNK_POSITION_X_KEY = "chunkX";
    private static final String CHUNK_POSITION_Z_KEY = "chunkZ";
    private static final String CHUNK_VERSION_KEY = "chunkVersion";
    private static final String LAYERS_KEY = "layers";
    private static final String BLOCK_X_KEY = "blockX";
    private static final String BLOCK_Y_KEY = "blockY";
    private static final String BLOCK_Z_KEY = "blockZ";
    private static final String BLOCK_ID_KEY = "blockId";
    private static final String BLOCKS_KEY = "blocks";
    private static final String BLOCK_TYPE_KEY = "blockType";
    private static final String WATERLOGGED_KEY = "waterlogged";
    private static final String GLOW_BERRY_KEY = "glowBerry";
    private static final String STRIPPED_KEY = "stripped";

    private static final String AGEABLE_BLOCK_TYPE = "ageable";
    private static final String CAVE_VINE_BLOCK_TYPE = "caveVine";
    private static final String LEAF_BLOCK_TYPE = "leaf";
    private static final String LOG_BLOCK_TYPE = "log";
    private static final String SAPLING_BLOCK_TYPE = "sapling";

    @Override
    public boolean isChunkLoaded(ChunkPosition chunkPosition) {
        final MongoCollection<Document> worldCollection = this.worldsDatabase.getCollection(chunkPosition.world().toString());
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkPosition.x()));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkPosition.z()));
        final Document found = worldCollection.find(id).first();
        if (found == null) {
            return false;
        }
        return found.getInteger(CHUNK_VERSION_KEY) == this.config.getChunkVersion();
    }

    @Override
    public void setChunkLoaded(ChunkPosition chunkPosition) {
        final MongoCollection<Document> worldCollection = this.worldsDatabase.getCollection(chunkPosition.world().toString());
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkPosition.x()));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkPosition.z()));
//        final Document document = new Document(Map.of(
//                ID_KEY, id,
//                CHUNK_VERSION_KEY, this.config.getChunkVersion()
//        ));
        final Bson filter = Filters.eq(ID_KEY, id);
//        worldCollection.updateOne(filter, document, UPDATE_OPTIONS);
        worldCollection.updateOne(
                filter,
                List.of(
                        Updates.set(ID_KEY, id),
                        Updates.set(CHUNK_VERSION_KEY, this.config.getChunkVersion())
                ),
                UPDATE_OPTIONS
        );
    }

    @Override
    public List<Runnable> shutdownNow() {
        this.readExecutor.shutdown();
        return this.writeExecutor.shutdownNow();
    }

    @Override
    public void saveBlocksInChunk(ChunkBlockCache chunk) {
        chunk.setSaving(true);
        this.deleteRemovedBlocksInChunk(chunk);
        if (chunk.getBlockDataMap().isEmpty()) return;
        final ChunkPosition chunkPosition = chunk.getChunkPosition();
        final int chunkX = chunkPosition.x();
        final int chunkZ = chunkPosition.z();
        final MongoCollection<Document> chunkDocuments = this.worldsDatabase.getCollection(chunkPosition.world().toString());
//        final Document chunkDocument = new Document();
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkX));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkZ));
//        chunkDocument.put(ID_KEY, id);
        final List<Document> blockDocuments = new ArrayList<>();
        for (var entry : chunk.getBlockDataMap().entrySet()) {
            final Position position = entry.getKey();
            final int blockX = position.x();
            final int blockY = position.y();
            final int blockZ = position.z();
            final BlockData blockData = entry.getValue();
            if (!blockData.shouldSave()) continue;
            final BsonDocument blockId = new BsonDocument();
            blockId.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkX));
            blockId.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkZ));
            blockId.put(BLOCK_X_KEY, new BsonInt32(blockX));
            blockId.put(BLOCK_Y_KEY, new BsonInt32(blockY));
            blockId.put(BLOCK_Z_KEY, new BsonInt32(blockZ));
            final Document blockDocument = new Document();
            blockDocument.put(ID_KEY, blockId);
            final String type = blockTypeKeyFromBlockDataClass(blockData);
            if (type == null) continue;
            blockDocument.put(BLOCK_TYPE_KEY, type);
            switch (type) {
                case CAVE_VINE_BLOCK_TYPE -> blockDocument.put(GLOW_BERRY_KEY, ((CaveVineData) blockData).glowBerry());
                case LEAF_BLOCK_TYPE -> blockDocument.put(WATERLOGGED_KEY, ((LeafData) blockData).waterlogged());
                case LOG_BLOCK_TYPE -> blockDocument.put(STRIPPED_KEY, ((LogData) blockData).stripped());
            }
            blockDocuments.add(blockDocument);
        }
//        chunkDocument.put(BLOCKS_KEY, blockDocuments);
        final Bson filter = Filters.eq(ID_KEY, id);
//        chunkDocuments.updateOne(filter, chunkDocument, UPDATE_OPTIONS);
        chunkDocuments.updateOne(filter, Updates.set(BLOCKS_KEY, blockDocuments), UPDATE_OPTIONS);
        chunk.setSaving(false);
        chunk.markClean();
        chunk.setSafeToMarkClean(true);
    }

    @Override
    public Map<Position, BlockData> getBlocksInChunk(ChunkPosition chunkPosition, LeavesConfig config) {
        final MongoCollection<Document> leafBlocks = this.worldsDatabase.getCollection(chunkPosition.world().toString());
        final int chunkX = chunkPosition.x();
        final int chunkZ = chunkPosition.z();
        final Map<Position, BlockData> blocks = new HashMap<>();
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkX));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkZ));
        final BsonDocument query = new BsonDocument();
        query.put(ID_KEY, id);
        final Document document = leafBlocks.find(query).first();
        if (document == null) return blocks;
        final List<Document> blockDocuments = document.getList(BLOCKS_KEY, Document.class);
        for (var blockDocument : blockDocuments) {
            final int blockX = blockDocument.getInteger(BLOCK_X_KEY);
            final int blockY = blockDocument.getInteger(BLOCK_Y_KEY);
            final int blockZ = blockDocument.getInteger(BLOCK_Z_KEY);
            final String blockType = blockDocument.getString(BLOCK_TYPE_KEY);
            final Position position = new Position(chunkPosition.world(), blockX, blockY, blockZ);
            final BlockData blockData = config.getBlockData(blockType);
            if (blockData == null) {
                this.plugin.getLogger().warning("Could not find block data for block type " + blockType + " at position " +
                        blockX + ", " + blockY + ", " + blockZ + "!");
                continue;
            }
            switch (blockType) {
                case CAVE_VINE_BLOCK_TYPE -> blocks.put(position, ((CaveVineData) blockData).withGlowBerry(blockDocument.getBoolean(GLOW_BERRY_KEY)));
                case LEAF_BLOCK_TYPE -> blocks.put(position, ((LeafData) blockData).waterlog(blockDocument.getBoolean(WATERLOGGED_KEY)));
                case LOG_BLOCK_TYPE -> blocks.put(position, ((LogData) blockData).stripped(blockDocument.getBoolean(STRIPPED_KEY)));
            }
        }
        return blocks;
    }

    @Override
    public void saveDefaultDataLayers(UUID worldUUID, Collection<Integer> yLayers, ChunkPosition smallChunk) {
        if (yLayers.isEmpty()) return;
        final ChunkPosition largeChunk = smallChunk.toLargeChunk();
        final Multimap<ChunkPosition, Integer> multimap = this.leafDatabase.getPossibleWorldDefaultLayers()
                .computeIfAbsent(worldUUID, uuid -> Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet));
        multimap.putAll(largeChunk, yLayers);
        final MongoCollection<Document> layers = this.worldDefaultLayersDatabase.getCollection(worldUUID.toString());
//        final Document document = new Document();
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(largeChunk.x()));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(largeChunk.z()));
//        Bson update = Updates.set(ID_KEY, id);

//        document.put(ID_KEY, id);
//        document.put(LAYERS_KEY, yLayers);
        final Bson filter = Filters.eq(ID_KEY, id);
        layers.updateMany(
                filter,
                List.of(Updates.set(ID_KEY, id), Updates.set(LAYERS_KEY, yLayers)),
//                document,
                UPDATE_OPTIONS
        );
    }

    @Override
    public void loadAllDefaultPossibleLayersInWorld(UUID worldUUID, ChunkPosition smallChunk) {
        final ChunkPosition largeChunk = smallChunk.toLargeChunk();
        this.leafDatabase.getCurrentlyLoadingChunks().add(largeChunk);
        final MongoCollection<Document> layers = this.worldDefaultLayersDatabase.getCollection(worldUUID.toString());
        final BsonDocument id = new BsonDocument();
        id.put(CHUNK_POSITION_X_KEY, new BsonInt32(largeChunk.x()));
        id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(largeChunk.z()));
        final Document found = layers.find(id).first();
        if (found == null) {
            this.leafDatabase.getCurrentlyLoadingChunks().remove(largeChunk);
            return;
        }
        final List<Integer> layersList = found.getList(LAYERS_KEY, Integer.class);
        if (layersList == null) {
            this.leafDatabase.getCurrentlyLoadingChunks().remove(largeChunk);
            return;
        }
        final List<Integer> yLevels = new ArrayList<>(layersList);
        final Multimap<ChunkPosition, Integer> multimap = this.leafDatabase.getPossibleWorldDefaultLayers()
                .computeIfAbsent(worldUUID, uuid -> Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet));
        multimap.putAll(largeChunk, yLevels);
        this.leafDatabase.getCurrentlyLoadingChunks().remove(largeChunk);
    }

    private void deleteRemovedBlocksInChunk(ChunkBlockCache chunkBlockCache) {
        final ChunkPosition chunkPosition = chunkBlockCache.getChunkPosition();
        final int chunkX = chunkPosition.x();
        final int chunkZ = chunkPosition.z();
        final MongoCollection<Document> documents = this.worldsDatabase.getCollection(chunkPosition.world().toString());
        final List<Document> toDelete = new ArrayList<>();
        chunkBlockCache.clearRemovedPositions(entry -> {
            final Position position = entry.getKey();
            final int blockX = position.x();
            final int blockY = position.y();
            final int blockZ = position.z();
            final BsonDocument id = new BsonDocument();
            id.put(CHUNK_POSITION_X_KEY, new BsonInt32(chunkX));
            id.put(CHUNK_POSITION_Z_KEY, new BsonInt32(chunkZ));
            id.put(BLOCK_X_KEY, new BsonInt32(blockX));
            id.put(BLOCK_Y_KEY, new BsonInt32(blockY));
            id.put(BLOCK_Z_KEY, new BsonInt32(blockZ));
            final Document document = new Document();
            document.put(ID_KEY, id);
            toDelete.add(document);
            return true;
        });
        if (toDelete.isEmpty()) {
            return;
        }
        documents.deleteMany(new Document("$or", toDelete));
    }

    private @Nullable
    String blockTypeKeyFromBlockDataClass(BlockData blockData) {
        if (blockData instanceof AgeableData) return AGEABLE_BLOCK_TYPE;
        if (blockData instanceof CaveVineData) return CAVE_VINE_BLOCK_TYPE;
        if (blockData instanceof LeafData) return LEAF_BLOCK_TYPE;
        if (blockData instanceof LogData) return LOG_BLOCK_TYPE;
        if (blockData instanceof SaplingData) return SAPLING_BLOCK_TYPE;
        return null;
    }

}
