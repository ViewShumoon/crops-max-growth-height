package cn.shumoon.cropsMaxGrowthHeight.mixin;

import cn.shumoon.cropsMaxGrowthHeight.CropsMaxGrowthHeight;
import cn.shumoon.cropsMaxGrowthHeight.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooStalkBlock.class)
public class BambooStalkBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void bambooMaxHeight$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        ci.cancel();

        // Same guards as vanilla randomTick: only young stalks (STAGE 0), 1/3 chance,
        // empty and lit space above.
        if (state.getValue(BambooStalkBlock.STAGE) != 0) {
            return;
        }
        if (random.nextInt(3) != 0) {
            return;
        }
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        if (level.getRawBrightness(pos.above(), 0) < 9) {
            return;
        }

        applyGrowthStep(level, pos, random, getMaxHeight());
    }

    private static int getMaxHeight() {
        Configuration config = CropsMaxGrowthHeight.getConfiguration();
        return config != null ? config.bambooMaxHeight : Configuration.DEFAULT_MAX_HEIGHT;
    }

    /**
     * Same as vanilla randomTick + growBamboo, but the hardcoded cap of 16 is replaced by
     * maxHeight. The count and the "done growing" thresholds scale with maxHeight so that a
     * maxHeight of 16 reproduces the vanilla behaviour exactly.
     */
    private static void applyGrowthStep(ServerLevel level, BlockPos pos, RandomSource random, int maxHeight) {
        BlockState currentState = level.getBlockState(pos);
        if (!currentState.is(Blocks.BAMBOO)) {
            return;
        }

        // Count how tall this bamboo column is, including the block at pos (uncapped).
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(Blocks.BAMBOO)) {
            height++;
        }

        if (height >= maxHeight) {
            return;
        }

        BlockState belowState = level.getBlockState(pos.below());
        BlockState below2State = level.getBlockState(pos.below(2));

        BambooLeaves leaves = BambooLeaves.NONE;
        if (belowState.is(Blocks.BAMBOO)) {
            if (belowState.getValue(BambooStalkBlock.LEAVES) == BambooLeaves.NONE) {
                leaves = BambooLeaves.SMALL;
            } else {
                leaves = BambooLeaves.LARGE;
                if (below2State.is(Blocks.BAMBOO)) {
                    level.setBlockAndUpdate(pos.below(), belowState.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL));
                    level.setBlockAndUpdate(pos.below(2), below2State.setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE));
                }
            }
        }

        int age = (currentState.getValue(BambooStalkBlock.AGE) == 1 || below2State.is(Blocks.BAMBOO)) ? 1 : 0;

        // Vanilla marks the new block as "done growing" once the column is near the cap:
        // randomly from (maxHeight - 5) and always at (maxHeight - 1). With maxHeight 16 this
        // reproduces the vanilla thresholds 11 and 15.
        boolean doneGrowing = (height >= maxHeight - 5 && random.nextFloat() < 0.25F) || height == maxHeight - 1;
        int stage = doneGrowing ? BambooStalkBlock.STAGE_DONE_GROWING : BambooStalkBlock.STAGE_GROWING;

        level.setBlockAndUpdate(pos.above(), Blocks.BAMBOO.defaultBlockState()
                .setValue(BambooStalkBlock.AGE, age)
                .setValue(BambooStalkBlock.LEAVES, leaves)
                .setValue(BambooStalkBlock.STAGE, stage));
    }
}
