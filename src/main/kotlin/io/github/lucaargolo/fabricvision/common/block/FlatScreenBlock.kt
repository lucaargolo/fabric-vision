package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.stream.Stream


class FlatScreenBlock(settings: Settings) : BlockWithEntity(settings){

    init {
        defaultState = defaultState.with(WALL, false).with(FACING, Direction.NORTH).with(PART, Part.CENTER).with(LAYER, Layer.DOWN)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(WALL, FACING, PART, LAYER)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val world = ctx.world
        val pos = ctx.blockPos

        val direction = ctx.horizontalPlayerFacing.opposite
        val left = direction.rotateYClockwise()
        val right = direction.rotateYCounterclockwise()

        listOf(pos.up(), pos.up().offset(left), pos.up().offset(right), pos.offset(left), pos.offset(right)).forEach {
            if(!world.getBlockState(it).canReplace(ctx)) {
                return null
            }
        }

        val wall = ctx.side.axis != Direction.Axis.Y
        return defaultState.with(WALL, wall).with(FACING, direction)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if(state[PART] == Part.CENTER && state[LAYER] == Layer.DOWN) {
            val wall = state[WALL]
            val direction = state[FACING]
            val left = direction.rotateYClockwise()
            val right = direction.rotateYCounterclockwise()

            listOf(pos.up(), pos.up().offset(left), pos.up().offset(right), pos.offset(left), pos.offset(right)).forEach {
                world.setBlockState(it, state
                    .with(WALL, wall)
                    .with(LAYER, if(it.y > pos.y) Layer.UP else Layer.DOWN)
                    .with(PART, when {
                        (it.y > pos.y && it.offset(left.opposite) == pos.up()) || it.offset(left.opposite) == pos -> Part.LEFT
                        (it.y > pos.y && it.offset(right.opposite) == pos.up()) || it.offset(right.opposite) == pos -> Part.RIGHT
                        else -> Part.CENTER
                    })
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if(!state.isOf(newState.block)) {
            val (originalPos, left, right) = getOriginalPos(state, pos)
            listOf(originalPos, originalPos.up(), originalPos.up().offset(left), originalPos.up().offset(right), originalPos.offset(left), originalPos.offset(right)).forEach {
                world.setBlockState(it, Blocks.AIR.defaultState)
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getShape(state[FACING], state[LAYER], state[PART])", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.getShape", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.FACING", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.LAYER", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.PART"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[WALL], state[FACING], state[LAYER], state[PART])
    }
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return if(state[PART] == Part.CENTER && state[LAYER] == Layer.DOWN) {
            MediaPlayerBlockEntity.FlatScreen(pos, state)
        }else{
            null
        }
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if(world.isClient) checkType(type, BlockEntityCompendium.FLAT_SCREEN, MediaPlayerBlockEntity::clientTick) else null
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(!world.isClient) {
            val (originalPos, _, _) = getOriginalPos(state, pos)
            world.getBlockEntity(originalPos, BlockEntityCompendium.FLAT_SCREEN).ifPresent {
                //if(player.isSneaking) {
                //    it.mrl = "C:\\Users\\Luca\\Downloads\\video.mp4"
                //}else{
                    it.playing = !it.playing
                //}
            }
        }
        return ActionResult.SUCCESS
    }

    @Suppress("LiftReturnOrAssignment")
    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState): BlockRenderType {
        if(state[PART] == Part.CENTER && state[LAYER] == Layer.DOWN) {
            return BlockRenderType.MODEL
        }else{
            return BlockRenderType.INVISIBLE
        }
    }

    private fun getOriginalPos(state: BlockState, pos: BlockPos): Triple<BlockPos, Direction, Direction> {
        val direction = state[FACING]
        val left = direction.rotateYClockwise()
        val right = direction.rotateYCounterclockwise()
        var originalPos = pos
        if(state[LAYER] == Layer.UP) {
            originalPos = originalPos.down()
        }
        if(state[PART] == Part.RIGHT) {
            originalPos = originalPos.offset(right.opposite)
        }else if(state[PART] == Part.LEFT) {
            originalPos = originalPos.offset(left.opposite)
        }
        return Triple(originalPos, left, right);
    }

    companion object {

        val WALL: BooleanProperty = BooleanProperty.of("wall")
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        enum class Part: StringIdentifiable {
            CENTER, LEFT, RIGHT;
            override fun asString() = name.lowercase()
        }

        val PART: EnumProperty<Part> = EnumProperty.of("part", Part::class.java)

        enum class Layer: StringIdentifiable {
            UP, DOWN;
            override fun asString() = name.lowercase()
        }

        val LAYER: EnumProperty<Layer> = EnumProperty.of("layer", Layer::class.java)

        private val MAIN_SHAPE = Stream.of(
            createCuboidShape(-14.0, 3.0, 7.0, 30.0, 5.0, 9.0),
            createCuboidShape(-14.0, 27.5, 7.0, 30.0, 29.5, 9.0),
            createCuboidShape(-14.0, 5.0, 7.0, -12.0, 27.5, 9.0),
            createCuboidShape(-11.0, 2.0, 6.0, -10.0, 3.0, 10.0),
            createCuboidShape(-11.0, 1.0, 9.0, -10.0, 2.0, 11.0),
            createCuboidShape(-11.0, 1.0, 5.0, -10.0, 2.0, 7.0),
            createCuboidShape(-11.0, 0.0, 4.0, -10.0, 1.0, 6.0),
            createCuboidShape(-11.0, 0.0, 10.0, -10.0, 1.0, 12.0),
            createCuboidShape(26.0, 0.0, 10.0, 27.0, 1.0, 12.0),
            createCuboidShape(26.0, 1.0, 9.0, 27.0, 2.0, 11.0),
            createCuboidShape(26.0, 0.0, 4.0, 27.0, 1.0, 6.0),
            createCuboidShape(26.0, 1.0, 5.0, 27.0, 2.0, 7.0),
            createCuboidShape(26.0, 2.0, 6.0, 27.0, 3.0, 10.0),
            createCuboidShape(28.0, 5.0, 7.0, 30.0, 27.5, 9.0),
            createCuboidShape(-12.0, 5.0, 7.5, 28.0, 27.5, 8.5),
            createCuboidShape(-12.0, 5.0, 8.5, 28.0, 27.5, 10.5)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        private val WALL_SHAPE = Stream.of(
            createCuboidShape(-12.0, 5.0, 13.5, 28.0, 27.5, 14.5),
            createCuboidShape(-12.0, 5.0, 14.5, 28.0, 27.5, 16.5),
            createCuboidShape(28.0, 5.0, 13.0, 30.0, 27.5, 15.0),
            createCuboidShape(-14.0, 27.5, 13.0, 30.0, 29.5, 15.0),
            createCuboidShape(-14.0, 5.0, 13.0, -12.0, 27.5, 15.0),
            createCuboidShape(-14.0, 3.0, 13.0, 30.0, 5.0, 15.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        class FacingShapeHolder(mainShape: VoxelShape, direction: Direction) {

            private val shapes: MutableMap<Pair<Layer, Part>, VoxelShape>
            init {
                val shape = mainShape.rotate(direction)
                val left = Vec3d(direction.rotateYClockwise().unitVector)
                val right = Vec3d(direction.rotateYCounterclockwise().unitVector)
                shapes = mutableMapOf(
                    Layer.DOWN to Part.LEFT to shape.offset(-left.x, -left.y, -left.z),
                    Layer.DOWN to Part.CENTER to shape,
                    Layer.DOWN to Part.RIGHT to shape.offset(-right.x, -right.y, -right.z),
                    Layer.UP to Part.LEFT to shape.offset(-left.x, -left.y-1.0, -left.z),
                    Layer.UP to Part.CENTER to shape.offset(0.0, -1.0, 0.0),
                    Layer.UP to Part.RIGHT to shape.offset(-right.x, -right.y-1.0, -right.z)
                )
            }

            fun getShape(layer: Layer, part: Part): VoxelShape {
                return shapes[layer to part]!!
            }

        }

        private val mainFacingShapes = mutableMapOf(
            Direction.NORTH to FacingShapeHolder(MAIN_SHAPE, Direction.NORTH),
            Direction.SOUTH to FacingShapeHolder(MAIN_SHAPE, Direction.SOUTH),
            Direction.EAST to FacingShapeHolder(MAIN_SHAPE, Direction.EAST),
            Direction.WEST to FacingShapeHolder(MAIN_SHAPE, Direction.WEST)
        )

        private val wallFacingShapes = mutableMapOf(
            Direction.NORTH to FacingShapeHolder(WALL_SHAPE, Direction.NORTH),
            Direction.SOUTH to FacingShapeHolder(WALL_SHAPE, Direction.SOUTH),
            Direction.EAST to FacingShapeHolder(WALL_SHAPE, Direction.EAST),
            Direction.WEST to FacingShapeHolder(WALL_SHAPE, Direction.WEST)
        )

        fun getShape(wall: Boolean, facing: Direction, layer: Layer, part: Part): VoxelShape {
            return if(wall) wallFacingShapes[facing]!!.getShape(layer, part) else mainFacingShapes[facing]!!.getShape(layer, part)
        }

    }


}