package dev.saperate.elementals.blocks;

import dev.saperate.elementals.blocks.blockEntities.LitAirBlockEntity;
import dev.saperate.elementals.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ElementalsBlocks {

    public static final Supplier<Block> LIT_AIR = Services.REGISTRY.registerBlock(
            "lit_air",
            () -> new LitAir(BlockBehaviour.Properties.of()
                    .strength(0f).lightLevel(value -> 15).noOcclusion().noCollission()
                    .emissiveRendering(ElementalsBlocks::always).isViewBlocking(ElementalsBlocks::never)
            ));
    public static final Supplier<Block> SOUL_FIRE_CORE = Services.REGISTRY.registerBlock(
            "soul_fire_core",
            () -> new SoulFireCore(BlockBehaviour.Properties.of().strength(1f)));
    public static final Supplier<Block> WATER_RAPID = Services.REGISTRY.registerBlock(
            "water_rapid",
            () -> new WaterRapid(BlockBehaviour.Properties.of().strength(1f)));    public static final Supplier<BlockEntityType<LitAirBlockEntity>> LIT_AIR_BLOCK_ENTITY =
            Services.REGISTRY.registerBlockEntityType(
                    "lit_air_block_entity",
                    LIT_AIR, LitAirBlockEntity::new
            );
    public static final Supplier<Block> MOON_PEACH_LEAVES = Services.REGISTRY.registerBlock(
            "moon_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .strength(1f).noOcclusion()));
    public static final Supplier<Block> MOON_LOG = Services.REGISTRY.registerBlock(
            "moon_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(1f).noOcclusion().sound(SoundType.WOOD).ignitedByLava()));
    public static final Supplier<Block> MOON_STRIPPED_LOG = Services.REGISTRY.registerBlock(
            "moon_stripped_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(1f).noOcclusion().sound(SoundType.WOOD).ignitedByLava()));
    public static final Supplier<Block> MOON_PLANKS = Services.REGISTRY.registerBlock(
            "moon_planks",
            () -> new Block(BlockBehaviour.Properties.of().strength(1f).noOcclusion()));

    public static void register() {
        System.out.println("Registering elementals blocks..");
    }

    private static ToIntFunction<BlockState> always15() {
        return value -> 15;
    }

    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }




}
