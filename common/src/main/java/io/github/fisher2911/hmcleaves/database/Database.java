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
import java.util.UUID;

public interface Database {

    static Database create(HMCLeaves plugin, LeavesConfig config) {
        return switch (config.getDatabaseType()) {
            default -> new SQLiteDatabase(plugin);
        };
    }

    boolean isLayerLoaded(ChunkPosition smallChunk);

    Collection<Integer> getPossibleWorldDefaultLayers(ChunkPosition smallChunk);

    void load();

    void doDatabaseWriteAsync(Runnable runnable);

    void doDatabaseReadAsync(Runnable runnable);

    void close();

    boolean isChunkLoaded(ChunkPosition chunkPosition);

    void setChunkLoaded(ChunkPosition chunkPosition);

    List<Runnable> shutdownNow();

    void saveBlocksInChunk(ChunkBlockCache chunk);

    Map<Position, BlockData> getBlocksInChunk(ChunkPosition chunkPosition, LeavesConfig config);

    void saveDefaultDataLayers(UUID worldUUID, Collection<Integer> yLayers, ChunkPosition smallChunk) throws SQLException;

    void loadAllDefaultPossibleLayersInWorld(UUID worldUUID, ChunkPosition smallChunk);

}
