package com.shoxie.audiocassettes.block;


import com.shoxie.audiocassettes.entity.TapeDeckEntity;
import com.shoxie.audiocassettes.init.Init;
import com.shoxie.audiocassettes.inventory.StackInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TapeDeckBlock extends Block implements EntityBlock {
	public static String name = "tapedeck";
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public TapeDeckBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TapeDeckEntity(pos, state);
    }

    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == Init.TAPEDECK_ENTITY ? TapeDeckEntity::tick : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TapeDeckEntity) {
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
            if (_entity instanceof TapeDeckEntity entity) {
                StackInventory inv = entity.getInventory();
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty())
                        level.addFreshEntity(
                                new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack)
                        );
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
