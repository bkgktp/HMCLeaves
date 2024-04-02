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

package io.github.fisher2911.hmcleaves.data;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.fisher2911.hmcleaves.config.LeavesConfig;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public record AgeableData(
        String id,
        Material realBlockType,
        int sendBlockId,
        int tippedSendBlockId,
        String modelPath,
        Sound placeSound,
        Predicate<Material> worldTypeSamePredicate,
        Material defaultLowerMaterial,
        int stackLimit,
        Material breakReplacement,
        Set<BlockFace> supportableFaces,
        @Nullable BlockDataSound blockDataSound
) implements BlockData, LimitedStacking {

    private static final Set<Material> TIP_MATERIALS = Set.of(
            Material.CAVE_VINES,
            Material.KELP,
            Material.TWISTING_VINES,
            Material.WEEPING_VINES
    );

    @Override
    public WrappedBlockState getNewState(@Nullable Material worldMaterial) {

        final WrappedBlockState state;
        if (worldMaterial == this.defaultLowerMaterial && LeavesConfig.getDefaultAgeableStringId(this.realBlockType).equals(this.id)) {
            state = SpigotConversionUtil.fromBukkitBlockData(this.defaultLowerMaterial.createBlockData());
        } else if (worldMaterial != null && TIP_MATERIALS.contains(worldMaterial)) {
            state = WrappedBlockState.getByGlobalId(this.tippedSendBlockId);
        } else {
            state = WrappedBlockState.getByGlobalId(this.sendBlockId);
        }
        return state;
    }

    public int getAge() {
        return WrappedBlockState.getByGlobalId(this.sendBlockId).getAge();
    }

    @Override
    public Material worldBlockType() {
        return this.realBlockType;
    }

    @Override
    public boolean isWorldTypeSame(Material worldMaterial) {
        return this.worldTypeSamePredicate.test(worldMaterial);
    }

    @Override
    public boolean shouldSave() {
        return !this.id().equals(LeavesConfig.getDefaultAgeableStringId(this.realBlockType()));
    }

}
