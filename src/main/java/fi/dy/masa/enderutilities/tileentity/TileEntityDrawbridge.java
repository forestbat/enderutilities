package fi.dy.masa.enderutilities.tileentity;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiDrawbridge;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerDrawbridge;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.TileUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityDrawbridge extends TileEntityEnderUtilitiesInventory
{
    public static final int MAX_LENGTH_NORMAL = 64;
    public static final int MAX_LENGTH_ADVANCED = 32;
    private ItemStackHandlerDrawbridge itemHandlerDrawbridge;
    private boolean advanced;
    private boolean redstoneState;
    private State state = State.IDLE;
    private RedstoneMode redstoneMode = RedstoneMode.EXTEND;
    private int position;
    private int delay = 4;
    private int maxLength = 1;
    private BlockInfo[] blockInfoTaken = new BlockInfo[MAX_LENGTH_NORMAL];
    private IBlockState[] blockStatesPlaced = new IBlockState[MAX_LENGTH_NORMAL];
    private FakePlayer fakePlayer;

    public TileEntityDrawbridge()
    {
        super(ReferenceNames.NAME_TILE_DRAW_BRIDGE);

        this.initStorage();
    }

    private void initStorage()
    {
        this.itemHandlerDrawbridge  = new ItemStackHandlerDrawbridge(0, MAX_LENGTH_ADVANCED, 1, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerDrawbridge;
        this.itemHandlerExternal    = new ItemHandlerWrapperDrawbridge(this.itemHandlerDrawbridge);
    }

    public ItemStackHandlerDrawbridge getInventoryDrawbridge()
    {
        return this.itemHandlerDrawbridge;
    }

    public boolean isAdvanced()
    {
        return this.advanced;
    }

    public void setIsAdvanced(boolean advanced)
    {
        this.advanced = advanced;
    }

    public int getSlotCount()
    {
        return this.isAdvanced() ? this.maxLength : 1;
    }

    public int getMaxLength()
    {
        return this.maxLength;
    }

    public void setMaxLength(int length)
    {
        this.maxLength = MathHelper.clamp(length, 1, this.isAdvanced() ? MAX_LENGTH_ADVANCED : MAX_LENGTH_NORMAL);

        if (this.isAdvanced() == false)
        {
            this.setStackLimit(length);
        }
    }

    public int getDelay()
    {
        return this.delay;
    }

    public void setDelayFromByte(byte delay)
    {
        this.setDelay(((int) delay) & 0xFF);
    }

    public void setDelay(int delay)
    {
        this.delay = MathHelper.clamp(delay, 1, 255);
    }

    public void setRedstoneMode(int id)
    {
        id = MathHelper.clamp(id, 0, 2);
        this.redstoneMode = RedstoneMode.fromId(id);
    }

    public int getRedstoneModeIntValue()
    {
        return this.redstoneMode.getId();
    }

    public void setStackLimit(int limit)
    {
        this.getBaseItemHandler().setStackLimit(MathHelper.clamp(limit, 1, MAX_LENGTH_NORMAL));
    }

    @Override
    public void setPlacementProperties(World world, BlockPos pos, ItemStack stack, NBTTagCompound tag)
    {
        if (tag.hasKey("drawbridge.delay", Constants.NBT.TAG_BYTE))
        {
            this.setDelayFromByte(tag.getByte("drawbridge.delay"));
        }

        if (tag.hasKey("drawbridge.length", Constants.NBT.TAG_BYTE))
        {
            this.setMaxLength(tag.getByte("drawbridge.length"));
        }

        if (tag.hasKey("drawbridge.redstone_mode", Constants.NBT.TAG_BYTE))
        {
            this.setRedstoneMode(tag.getByte("drawbridge.redstone_mode"));
        }

        this.markDirty();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setIsAdvanced(nbt.getBoolean("Advanced"));
        this.redstoneState = nbt.getBoolean("Powered");
        this.position = nbt.getByte("Position");
        this.setDelayFromByte(nbt.getByte("Delay"));
        this.setMaxLength(nbt.getByte("Length"));
        this.state = State.fromId(nbt.getByte("State"));
        this.setRedstoneMode(nbt.getByte("RedstoneMode"));

        this.readBlockInfoFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        nbt.setBoolean("Advanced", this.isAdvanced());
        nbt.setBoolean("Powered", this.redstoneState);
        nbt.setByte("Position", (byte) this.position);
        nbt.setByte("Delay", (byte) this.delay);
        nbt.setByte("Length", (byte) this.maxLength);
        nbt.setByte("State", (byte) this.state.getId());
        nbt.setByte("RedstoneMode", (byte) this.redstoneMode.getId());

        this.writeBlockInfoToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);
        nbt.setByte("len", (byte) this.maxLength);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        this.setMaxLength(tag.getByte("len"));
    }

    private void writeBlockInfoToNBT(NBTTagCompound nbt)
    {
        NBTTagList listTaken = new NBTTagList();
        NBTTagList listPlaced = new NBTTagList();

        int length = Math.min(this.maxLength, this.blockInfoTaken.length);

        // Taken blocks' BlockInfo
        for (int i = 0; i < length; i++)
        {
            BlockInfo info = this.blockInfoTaken[i];

            if (info != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                NBTUtils.writeBlockStateToTag(info.getState(), tag);

                if (info.getTileEntityNBT() != null)
                {
                    tag.setTag("nbt", info.getTileEntityNBT());
                }

                tag.setByte("pos", (byte) i);
                listTaken.appendTag(tag);
            }
        }

        length = Math.min(this.maxLength, this.blockStatesPlaced.length);

        // Placed block states
        for (int i = 0; i < length; i++)
        {
            if (this.blockStatesPlaced[i] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                NBTUtils.writeBlockStateToTag(this.blockStatesPlaced[i], tag);
                tag.setByte("pos", (byte) i);
                listPlaced.appendTag(tag);
            }
        }

        nbt.setTag("Blocks", listTaken);
        nbt.setTag("Placed", listPlaced);
    }

    private void readBlockInfoFromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Blocks", Constants.NBT.TAG_LIST))
        {
            NBTTagList list = nbt.getTagList("Blocks", Constants.NBT.TAG_COMPOUND);
            int count = list.tagCount();
            int length = Math.min(this.maxLength, this.blockInfoTaken.length);

            for (int i = 0; i < count; i++)
            {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                IBlockState state = NBTUtils.readBlockStateFromTag(tag);
                int pos = tag.getByte("pos");

                if (pos >= 0 && pos < length && state != null)
                {
                    NBTTagCompound teTag = null;

                    if (tag.hasKey("nbt", Constants.NBT.TAG_COMPOUND))
                    {
                        teTag = tag.getCompoundTag("nbt");
                    }

                    this.blockInfoTaken[pos] = new BlockInfo(state, teTag);
                }
            }
        }

        if (nbt.hasKey("Placed", Constants.NBT.TAG_LIST))
        {
            NBTTagList list = nbt.getTagList("Placed", Constants.NBT.TAG_COMPOUND);
            int count = list.tagCount();
            int length = Math.min(this.maxLength, this.blockStatesPlaced.length);

            for (int i = 0; i < count; i++)
            {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                IBlockState state = NBTUtils.readBlockStateFromTag(tag);
                int pos = tag.getByte("pos");

                if (pos >= 0 && pos < length && state != null)
                {
                    this.blockStatesPlaced[pos] = state;
                }
            }
        }
    }

    @Nullable
    private IBlockState getPlacementStateForPosition(int invPosition, World world, BlockPos pos, FakePlayer player)
    {
        if (this.blockInfoTaken[invPosition] != null)
        {
            return this.blockInfoTaken[invPosition].getState();
        }

        ItemStack stack = this.itemHandlerDrawbridge.getStackInSlot(invPosition);

        if (stack.isEmpty() == false && stack.getItem() instanceof ItemBlock)
        {
            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            Block block = itemBlock.getBlock();

            if (block != null)
            {
                int meta = itemBlock.getMetadata(stack.getMetadata());
                player.rotationYaw = this.getFacing().getHorizontalAngle();

                return block.getStateForPlacement(world, pos, EnumFacing.UP, 0.5f, 1f, 0.5f, meta, player, EnumHand.MAIN_HAND);
            }
        }

        return null;
    }

    @Nullable
    private NBTTagCompound getPlacementTileNBT(int invPosition)
    {
        NBTTagCompound nbt = null;

        if (this.blockInfoTaken[invPosition] != null)
        {
            nbt = this.blockInfoTaken[invPosition].getTileEntityNBT();
        }

        // This extract will also clear the blockInfoTaken, if the slot becomes empty.
        // This will prevent item duping exploits by swapping the item to something else
        // after the drawbridge has taken the state and TE data from the world for a given block.
        ItemStack stack = this.itemHandlerDrawbridge.extractItem(invPosition, 1, false);

        // This fixes TE data loss on the placed blocks in case blocks with stored TE data
        // were manually placed into the slots, and not taken from the world by the drawbridge
        if (nbt == null && stack.isEmpty() == false && stack.getTagCompound() != null &&
            stack.getTagCompound().hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            nbt = stack.getTagCompound().getCompoundTag("BlockEntityTag");
        }

        return nbt;
    }

    private boolean extendOneBlock(int position, FakePlayer player, boolean playPistonSoundInsteadOfPlaceSound)
    {
        int invPosition = this.isAdvanced() ? position : 0;
        World world = this.getWorld();
        BlockPos pos = this.getPos().offset(this.getFacing(), position + 1);
        IBlockState placementState = this.getPlacementStateForPosition(invPosition, world, pos, player);
        ItemStack stack = this.itemHandlerDrawbridge.getStackInSlot(invPosition);

        if (placementState != null &&
            stack.isEmpty() == false &&
            world.isBlockLoaded(pos, world.isRemote == false) &&
            world.getBlockState(pos).getBlock().isReplaceable(world, pos) &&
            world.mayPlace(placementState.getBlock(), pos, true, EnumFacing.UP, null) &&
            ((playPistonSoundInsteadOfPlaceSound && world.setBlockState(pos, placementState)) ||
             (playPistonSoundInsteadOfPlaceSound == false && BlockUtils.setBlockStateWithPlaceSound(world, pos, placementState, 3))))
        {
            this.blockStatesPlaced[position] = placementState;
            NBTTagCompound nbt = this.getPlacementTileNBT(invPosition);

            if (nbt != null && placementState.getBlock().hasTileEntity(placementState))
            {
                TileUtils.createAndAddTileEntity(world, pos, nbt);
            }

            if (playPistonSoundInsteadOfPlaceSound)
            {
                world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, 0.8f);
            }

            return true;
        }

        return false;
    }

    private boolean retractOneBlock(int position, FakePlayer player, boolean takeNonPlaced, boolean playPistonSound)
    {
        World world = this.getWorld();
        BlockPos pos = this.getPos().offset(this.getFacing(), position + 1);

        if (world.isBlockLoaded(pos, world.isRemote == false) == false)
        {
            return false;
        }

        int invPosition = this.isAdvanced() ? position : 0;
        IBlockState state = world.getBlockState(pos);

        if ((takeNonPlaced || state == this.blockStatesPlaced[position]) &&
            state.getBlock().isAir(state, world, pos) == false &&
            state.getBlockHardness(world, pos) >= 0f)
        {
            ItemStack stack = BlockUtils.getPickBlockItemStack(world, pos, player, EnumFacing.UP);

            if (stack.isEmpty() == false)
            {
                NBTTagCompound nbt = null;

                if (state.getBlock().hasTileEntity(state))
                {
                    TileEntity te = world.getTileEntity(pos);

                    if (te != null)
                    {
                        TileUtils.storeTileEntityInStack(stack, te, false);
                        nbt = te.writeToNBT(new NBTTagCompound());
                        NBTUtils.removePositionFromTileEntityNBT(nbt);
                    }
                }

                if (this.itemHandlerDrawbridge.insertItem(invPosition, stack, false).isEmpty())
                {
                    // This check is for the Normal variant, where the same BlockInfo
                    // position 0 is used for all block positions
                    if (this.blockInfoTaken[invPosition] == null)
                    {
                        this.blockInfoTaken[invPosition] = new BlockInfo(state, nbt);
                    }

                    if (playPistonSound)
                    {
                        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, 0.7f);
                    }
                    else
                    {
                        BlockUtils.playBlockBreakSound(world, pos);
                    }

                    BlockUtils.setBlockToAirWithoutSpillingContents(world, pos);

                    this.blockStatesPlaced[position] = null;

                    return true;
                }
            }
        }

        return false;
    }

    private boolean takeAllBlocksFromWorld()
    {
        if (this.state == State.IDLE)
        {
            for (int offset = 0; offset < this.maxLength; offset++)
            {
                this.retractOneBlock(offset, this.getPlayer(), true, false);
            }

            return true;
        }

        return false;
    }

    @Nonnull
    private FakePlayer getPlayer()
    {
        if (this.fakePlayer == null)
        {
            int dim = this.getWorld().provider.getDimension();

            this.fakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(),
                    new GameProfile(new UUID(dim, dim), Reference.MOD_ID + ":drawbridge"));
        }

        return this.fakePlayer;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean powered = false;

        for (EnumFacing facing : EnumFacing.values())
        {
            if (facing != this.getFacing() && worldIn.isSidePowered(pos.offset(facing), facing))
            {
                powered = true;
                break;
            }
        }

        if (powered != this.redstoneState)
        {
            switch (this.redstoneMode)
            {
                case EXTEND:
                    this.state = powered ? State.EXTEND : State.RETRACT;
                    break;

                case RETRACT:
                    this.state = powered ? State.RETRACT : State.EXTEND;
                    break;

                case TOGGLE:
                    if (powered)
                    {
                        if (this.state == State.EXTEND)
                        {
                            this.state = State.RETRACT;
                        }
                        else if (this.state == State.RETRACT)
                        {
                            this.state = State.EXTEND;
                        }
                        else
                        {
                            if (this.position == (this.maxLength - 1))
                            {
                                this.state = State.RETRACT;
                            }
                            else
                            {
                                this.state = State.EXTEND;
                            }
                        }
                    }

                    break;
            }

            this.redstoneState = powered;
            this.scheduleBlockUpdate(this.delay, true);
        }
    }

    @Override
    public void onScheduledBlockUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this.state == State.EXTEND)
        {
            while (this.position < this.maxLength)
            {
                boolean result = this.extendOneBlock(this.position, this.getPlayer(), true);
                this.position++;

                if (result)
                {
                    break;
                }
            }

            if (this.position >= this.maxLength)
            {
                this.position = this.maxLength - 1;
                this.state = State.IDLE;
            }
            else
            {
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
        else if (this.state == State.RETRACT)
        {
            final int len = Math.min(this.maxLength, this.blockStatesPlaced.length);
            int numPlaced = 0;

            for (int i = 0; i < len; i++)
            {
                if (this.blockStatesPlaced[i] != null)
                {
                    numPlaced++;
                    break;
                }
            }

            while (this.position >= 0)
            {
                boolean result = this.retractOneBlock(this.position, this.getPlayer(), numPlaced == 0, true);
                this.position--;

                if (result)
                {
                    break;
                }
            }

            if (this.position < 0)
            {
                this.position = 0;
                this.state = State.IDLE;
            }
            else
            {
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
    }

    private void changeInventorySize(int changeAmount)
    {
        int newSize = MathHelper.clamp(this.getSlotCount() + changeAmount, 1, MAX_LENGTH_ADVANCED);

        // Shrinking the inventory, only allowed if there are no items in the slots-to-be-removed
        if (changeAmount < 0)
        {
            int changeFinal = 0;

            for (int slot = this.getSlotCount() - 1; slot >= newSize && slot >= 1; slot--)
            {
                if (this.itemHandlerDrawbridge.getStackInSlot(slot).isEmpty())
                {
                    changeFinal--;
                }
                else
                {
                    break;
                }
            }

            newSize = MathHelper.clamp(this.getSlotCount() + changeFinal, 1, MAX_LENGTH_ADVANCED);
        }

        if (newSize >= 1 && newSize <= MAX_LENGTH_ADVANCED)
        {
            this.setMaxLength(newSize);
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        switch (action)
        {
            // Take block states from world
            case 0:
                this.takeAllBlocksFromWorld();
                break;

            // Change the delay
            case 1:
                this.setDelay(this.delay + element);
                break;

            // Change the max length
            case 2:
                if (this.state == State.IDLE)
                {
                    int oldMaxLength = this.maxLength;

                    if (this.isAdvanced())
                    {
                        this.changeInventorySize(element);
                    }
                    else
                    {
                        this.setMaxLength(this.maxLength + element);
                    }

                    // If the device is in the extended idle state, set the position to the end of
                    // the newly set length.
                    if (this.position >= oldMaxLength)
                    {
                        this.position = (this.maxLength - 1);
                    }
                }
                break;

            // Change the redstone mode
            case 3:
                if (this.state == State.IDLE)
                {
                    this.setRedstoneMode(this.redstoneMode.getId() + (element > 0 ? 1 : -1));
                }
                break;
        }

        this.markDirty();
    }

    private class ItemStackHandlerDrawbridge extends ItemStackHandlerTileEntity
    {
        public ItemStackHandlerDrawbridge(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes,
                String tagName, TileEntityEnderUtilitiesInventory te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);
        }

        @Override
        public int getSlots()
        {
            return TileEntityDrawbridge.this.getSlotCount();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            ItemStack stack = super.extractItem(slot, amount, simulate);

            // Clear the stored block info to prevent duplicating or transmuting stuff
            if (simulate == false && this.getStackInSlot(slot).isEmpty())
            {
                TileEntityDrawbridge.this.blockInfoTaken[slot] = null;
            }

            return stack;
        }
    }

    private class ItemHandlerWrapperDrawbridge extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperDrawbridge(IItemHandler baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getItem() instanceof ItemBlock;
        }
    }

    private static class BlockInfo
    {
        private final IBlockState blockState;
        private final NBTTagCompound tileEntityData;

        BlockInfo(IBlockState state, NBTTagCompound tileEntityNBT)
        {
            this.blockState = state;
            this.tileEntityData = tileEntityNBT;
        }

        public IBlockState getState()
        {
            return this.blockState;
        }

        @Nullable
        public NBTTagCompound getTileEntityNBT()
        {
            return this.tileEntityData;
        }
    }

    private enum State
    {
        IDLE        (0),
        EXTEND      (1),
        RETRACT    (2);

        private final int id;

        private State(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        public static State fromId(int id)
        {
            return values()[id % values().length];
        }
    }

    private enum RedstoneMode
    {
        EXTEND  (0),
        RETRACT (1),
        TOGGLE  (2);

        private final int id;

        private RedstoneMode(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        public static RedstoneMode fromId(int id)
        {
            return values()[id % values().length];
        }
    }
    @Override
    protected boolean hasCamouflageAbility()
    {
        return true;
    }

    @Override
    public ContainerDrawbridge getContainer(EntityPlayer player)
    {
        return new ContainerDrawbridge(player, this);
    }

    @Override
    public Object getGui(EntityPlayer player)
    {
        return new GuiDrawbridge(this.getContainer(player), this);
    }
}
