package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.FlatScreenBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class LargeSpeakerBlock(settings: Settings) : HorizontalFacingSpeakerBlock(12.0, settings) {

    init {
        defaultState = defaultState.with(PART, Part.LEFT).with(LAYER, Layer.DOWN)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(PART, LAYER)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val world = ctx.world
        val pos = ctx.blockPos

        val direction = ctx.horizontalPlayerFacing.opposite
        val right = direction.rotateYCounterclockwise()

        listOf(pos.up(), pos.up().offset(right), pos.offset(right)).forEach {
            if(!world.getBlockState(it).canReplace(ctx)) {
                return null
            }
        }

        return defaultState.with(FACING, direction)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if(state[PART] == Part.LEFT && state[LAYER] == Layer.DOWN) {
            val direction = state[FACING]
            val right = direction.rotateYCounterclockwise()

            listOf(pos.up(), pos.up().offset(right), pos.offset(right)).forEach {
                world.setBlockState(it, state
                    .with(LAYER, if(it.y > pos.y) Layer.UP else Layer.DOWN)
                    .with(PART, when {
                        (it.y > pos.y && it.offset(right.opposite) == pos.up()) || it.offset(right.opposite) == pos -> Part.RIGHT
                        else -> Part.LEFT
                    })
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if(!state.isOf(newState.block)) {
            val (originalPos, right) = getInternalOriginalPos(state, pos)
            listOf(originalPos, originalPos.up(), originalPos.up().offset(right), originalPos.offset(right)).forEach {
                world.setBlockState(it, Blocks.AIR.defaultState)
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getShape(state[FACING], state[LAYER], state[PART])", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.getShape", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.FACING", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.LAYER", "io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock.Companion.PART"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[FACING], state[LAYER], state[PART])
    }

    @Suppress("LiftReturnOrAssignment")
    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState): BlockRenderType {
        if(state[PART] == Part.LEFT && state[LAYER] == Layer.DOWN) {
            return BlockRenderType.MODEL
        }else{
            return BlockRenderType.INVISIBLE
        }
    }

    private fun getInternalOriginalPos(state: BlockState, pos: BlockPos): Pair<BlockPos, Direction> {
        val direction = state[FACING]
        val right = direction.rotateYCounterclockwise()
        var originalPos = pos
        if(state[LAYER] == Layer.UP) {
            originalPos = originalPos.down()
        }
        if(state[PART] == Part.RIGHT) {
            originalPos = originalPos.offset(right.opposite)
        }
        return originalPos to right
    }

    companion object {

        enum class Part: StringIdentifiable {
            LEFT, RIGHT;
            override fun asString() = name.lowercase()
        }

        val PART: EnumProperty<Part> = EnumProperty.of("part", Part::class.java)

        enum class Layer: StringIdentifiable {
            UP, DOWN;
            override fun asString() = name.lowercase()
        }

        val LAYER: EnumProperty<Layer> = EnumProperty.of("layer", Layer::class.java)

        private val MAIN_SHAPE = VoxelShapes.combineAndSimplify(
            createCuboidShape(-14.0, 0.0, 2.0, 14.0, 28.0, 4.0),
            createCuboidShape(-14.0, 0.0, 4.0, 14.0, 28.0, 14.0),
        BooleanBiFunction.OR)
        class FacingShapeHolder(mainShape: VoxelShape, direction: Direction) {

            private val shapes: MutableMap<Pair<Layer, Part>, VoxelShape>
            init {
                val shape = mainShape.rotate(direction)
                val right = Vec3d(direction.rotateYCounterclockwise().unitVector)
                shapes = mutableMapOf(
                    Layer.DOWN to Part.LEFT to shape,
                    Layer.DOWN to Part.RIGHT to shape.offset(-right.x, -right.y, -right.z),
                    Layer.UP to Part.LEFT to shape.offset(0.0, -1.0, 0.0),
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

        fun getShape(facing: Direction, layer: Layer, part: Part): VoxelShape {
            return mainFacingShapes[facing]!!.getShape(layer, part)
        }

    }


}