package com.shoxie.audiocassettes.block;


import com.shoxie.audiocassettes.entity.BoomBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BoomBoxBlock extends Block implements EntityBlock {
	public static String name = "boombox";
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public BoomBoxBlock(Properties properties) {
        super(properties);
    }

    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoomBoxEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BoomBoxEntity) {
            if (player instanceof ServerPlayer sp) {
                sp.openMenu((MenuProvider) blockEntity);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide()) {
            BlockEntity _entity = level.getBlockEntity(pos);
            if (_entity instanceof BoomBoxEntity entity) {
                entity.stopMusic();
                var cassette = entity.getCassette();
                if (!cassette.isEmpty())
                    level.addFreshEntity(
                            new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                    cassette
                            )
                    );
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
