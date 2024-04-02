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
import io.github.fisher2911.hmcleaves.util.ChunkUtil;
import io.github.fisher2911.hmcleaves.world.ChunkPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LeafDatabase {

    // 528*528 chunks, not 16x16 chunks
    private final Map<UUID, Multimap<ChunkPosition, Integer>> possibleWorldDefaultLayers;
    private final Set<ChunkPosition> currentlyLoadingChunks;

    LeafDatabase() {
        this.possibleWorldDefaultLayers = new ConcurrentHashMap<>();
        this.currentlyLoadingChunks = ConcurrentHashMap.newKeySet();
    }

    boolean isLayerLoaded(ChunkPosition smallChunk) {
        return !this.getPossibleWorldDefaultLayers(smallChunk).isEmpty() &&
                !this.currentlyLoadingChunks.contains(smallChunk.toLargeChunk());
    }

    Collection<Integer> getPossibleWorldDefaultLayers(ChunkPosition smallChunk) {
        final int largeChunkX = ChunkUtil.getLargeChunkCoordFromChunkCoord(smallChunk.x());
        final int largeChunkZ = ChunkUtil.getLargeChunkCoordFromChunkCoord(smallChunk.z());
        final UUID worldUUID = smallChunk.world();
        final ChunkPosition largeChunk = new ChunkPosition(worldUUID, largeChunkX, largeChunkZ);
        final Multimap<ChunkPosition, Integer> layers = possibleWorldDefaultLayers.get(largeChunk.world());
        if (layers == null) return Collections.emptyList();
        return layers.get(largeChunk);
    }

    Map<UUID, Multimap<ChunkPosition, Integer>> getPossibleWorldDefaultLayers() {
        return possibleWorldDefaultLayers;
    }

    Set<ChunkPosition> getCurrentlyLoadingChunks() {
        return currentlyLoadingChunks;
    }

}
