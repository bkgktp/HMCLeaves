package io.github.fisher2911.hmcleaves.data;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.fisher2911.hmcleaves.config.LeavesConfig;
import io.github.fisher2911.hmcleaves.hook.Hooks;
import io.github.fisher2911.hmcleaves.packet.BlockBreakManager;
import io.github.fisher2911.hmcleaves.packet.BlockBreakModifier;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public record NoteBlockData(
        String id,
        int sendBlockId,
        Material realBlockType,
        String modelPath,
        Set<BlockFace> supportableFaces,
        @Nullable BlockDataSound blockDataSound

) implements BlockData {

    @Override
    public Material worldBlockType() {
        //if (this.stripped) return this.strippedBlockType;
        return this.realBlockType;
    }

    @Override
    public WrappedBlockState getNewState(@Nullable Material worldMaterial) {
        final int sendId = Objects.requireNonNullElse(
                Hooks.getBlockId(this.getCurrentId()), this.sendBlockId()
        );
        return this.create(sendId);
    }

    private WrappedBlockState create(int blockId) {
        final WrappedBlockState state = WrappedBlockState.getByGlobalId(blockId);
        //state.setAxis(this.convertBlockAxis());
        return state;
    }

    @Override
    public Sound placeSound() {
        return Sound.BLOCK_WOOD_PLACE;
    }

    @Override
    public int sendBlockId() {
        //if (this.stripped) return this.strippedSendBlockId;
        return this.sendBlockId;
    }

    public int getSendBlockId() {
        return this.sendBlockId;
    }

    public String getCurrentId() {
        //if (this.stripped) return this.strippedLogId;
        return this.id;
    }

    @Override
    public boolean isWorldTypeSame(Material worldMaterial) {
        final Material sendMaterial = SpigotConversionUtil.toBukkitBlockData(this.getNewState(null)).getMaterial();
        return worldMaterial == sendMaterial || this.worldBlockType() == worldMaterial;
    }

    @Override
    public Material breakReplacement() {
        return Material.AIR;
    }

    @Override
    public boolean shouldSave() {
        return !this.id().equals(LeavesConfig.getDefaultNoteBlockStringId(this.realBlockType()));
    }


}

