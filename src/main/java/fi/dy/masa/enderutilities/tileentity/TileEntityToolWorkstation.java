package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiTileEntityInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesSided
{
    public static final int SLOT_TOOL = 0;
    public static final int SLOT_MODULES_START = 1;

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.itemStacks = new ItemStack[10];
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        ItemStack[] stacksToKeep = this.compatibilityInventoryHandlingFrom04x(nbt);

        super.readFromNBTCustom(nbt);

        if (stacksToKeep != null)
        {
            this.itemStacks = stacksToKeep;
        }
    }

    /**
     * In 0.4.x the tool's installed modules were also actually stored in the TE's inventory.
     * Now, in 0.5.x, they are only stored in the modular item via the InventoryItem class.
     * So if we are loading an old TE from 0.4.x, then we need to discard the tool's modules and
     * adjust the module storage slots to be in the new slot range.
     */
    private ItemStack[] compatibilityInventoryHandlingFrom04x(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Version", Constants.NBT.TAG_STRING) == true)
        {
            //String version = nbt.getString("Version");
            //if (version.compareTo("0.5") >= 0)
            {
                return null;
            }
        }

        String str = String.format("Tool Workstation compatibility item handling from 0.4.x to 0.5.0 starting for TE at: x: %d y: %d z: %d",
                this.xCoord, this.yCoord, this.zCoord);
        EnderUtilities.logger.warn(str);

        NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        int numSlots = nbtTagList.tagCount();
        ItemStack[] stacks = new ItemStack[20];

        for (int i = 0; i < numSlots; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            byte slotNum = tag.getByte("Slot");

            if (slotNum >= 0 && slotNum < stacks.length)
            {
                stacks[slotNum] = ItemStack.loadItemStackFromNBT(tag);
            }
            else
            {
                String className = this.getClass().getSimpleName();
                EnderUtilities.logger.warn("Invalid slot number when reading inventory from NBT: " +
                                            slotNum + " (max: " + (stacks.length - 1) + ") in " + className);
            }
        }

        // Set the modular item itself
        this.itemStacks[SLOT_TOOL] = stacks[0];
        if (this.itemStacks[SLOT_TOOL] != null)
        {
            UtilItemModular.compatibilityAdjustInstalledModulePositions(this.itemStacks[SLOT_TOOL]);
        }

        // Move the stored modules in the Tool Workstation
        for (int i = 0; i < 9; i++)
        {
            this.itemStacks[i + 1] = stacks[i + 11];
        }

        nbt.removeTag("Items");

        return this.itemStacks;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack)
    {
        super.setInventorySlotContents(slotNum, itemStack);

        // FIXME Compatibility transfer of old target data from items before the change to modular. Remove at some point.
        if (this.worldObj.isRemote == false && slotNum == SLOT_TOOL && this.itemStacks[SLOT_TOOL] != null)
        {
            NBTHelperTarget.compatibilityTransferTargetData(this.itemStacks[SLOT_TOOL]);
            UtilItemModular.compatibilityAdjustInstalledModulePositions(this.itemStacks[SLOT_TOOL]);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return true;
        }

        if (slotNum == SLOT_TOOL)
        {
            return stack == null || stack.getItem() instanceof IModular;
        }

        return (stack.getItem() instanceof IModule) && (UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false);
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
    {
        return false;
    }

    @Override
    public ContainerToolWorkstation getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerToolWorkstation(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiTileEntityInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
    }
}
