package cn.shumoon.cropsMaxGrowthHeight.mixin;

import cn.shumoon.cropsMaxGrowthHeight.CropsMaxGrowthHeight;
import cn.shumoon.cropsMaxGrowthHeight.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    private static final int VANILLA_AGE_UPDATE_FLAGS = 260;

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void sugarcaneMaxHeight$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        ci.cancel();
        applyGrowthStep(level, pos, getMaxHeight());
    }

    private static int getMaxHeight() {
        Configuration config = CropsMaxGrowthHeight.getConfiguration();
        return config != null ? config.sugarCaneMaxHeight : Configuration.DEFAULT_MAX_HEIGHT;
    }

    /**
     * Same as vanilla randomTick, but the hardcoded cap of 3 is replaced by maxHeight.
     */
    private static void applyGrowthStep(ServerLevel level, BlockPos pos, int maxHeight) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }

        // Count how tall this sugar cane column is, including the block at pos.
        int heightFromBase = 1;
        while (level.getBlockState(pos.below(heightFromBase)).is(Blocks.SUGAR_CANE)) {
            heightFromBase++;
        }

        if (heightFromBase >= maxHeight) {
            return;
        }

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.is(Blocks.SUGAR_CANE)) {
            return;
        }

        int age = currentState.getValue(SugarCaneBlock.AGE);
        if (age == 15) {
            level.setBlockAndUpdate(pos.above(), Blocks.SUGAR_CANE.defaultBlockState());
            level.setBlock(pos, currentState.setValue(SugarCaneBlock.AGE, 0), VANILLA_AGE_UPDATE_FLAGS);
        } else {
            level.setBlock(pos, currentState.setValue(SugarCaneBlock.AGE, age + 1), VANILLA_AGE_UPDATE_FLAGS);
        }
    }
}