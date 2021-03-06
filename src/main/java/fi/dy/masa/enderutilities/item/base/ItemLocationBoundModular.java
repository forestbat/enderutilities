package fi.dy.masa.enderutilities.item.base;

import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemLocationBoundModular extends ItemLocationBound implements IModular, IKeyBound
{
    public ItemLocationBoundModular(String name)
    {
        super(name);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking())
        {
            if (world.isRemote == false)
            {
                ItemStack stack = player.getHeldItem(hand);

                if (this.useBindLocking(stack) == false || this.isBindLocked(stack) == false)
                {
                    boolean adjustPosHit = UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION;
                    this.setTarget(stack, player, pos, side, hitX, hitY, hitZ, adjustPosHit, false);
                    player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.itemboundtolocation"), true);
                }
                else
                {
                    player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.itembindlocked"), true);
                }
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        int dim = world.provider.getDimension();

        if (world.isRemote == false &&
            EnergyBridgeTracker.dimensionHasEnergyBridge(dim) &&
            (dim == 1 || EnergyBridgeTracker.dimensionHasEnergyBridge(1)))
        {
            UtilItemModular.addEnderCharge(stack, ItemEnderCapacitor.CHARGE_RATE_FROM_ENERGY_BRIDGE, true);
        }
    }

    @Override
    public String getTargetDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (moduleStack.isEmpty() == false && moduleStack.getItem() instanceof ILocationBound)
        {
            if (moduleStack.hasDisplayName())
            {
                // We need to get the name here directly, if we call ItemStack#getDisplayName(), it will recurse back to getItemStackDisplayName ;_;
                NBTTagCompound tag = moduleStack.getTagCompound().getCompoundTag("display");
                return TextFormatting.ITALIC.toString() + tag.getString("Name") + TextFormatting.RESET.toString();
            }

            return ((ILocationBound) moduleStack.getItem()).getTargetDisplayName(moduleStack);
        }

        return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (this.shouldDisplayTargetName(stack))
        {
            String preGreen = TextFormatting.GREEN.toString();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
            return this.getBaseItemDisplayName(stack) + " " + preGreen + this.getTargetDisplayName(stack) + rst;
        }

        return super.getBaseItemDisplayName(stack);
    }

    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(I18n.format("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

        String preBlue = TextFormatting.BLUE.toString();
        String preWhiteIta = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        // Link Crystals installed
        if (linkCrystalStack.isEmpty() == false)
        {
            // Valid target set in the currently selected Link Crystal
            if (TargetData.itemHasTargetTag(linkCrystalStack))
            {
                super.addTooltipLines(linkCrystalStack, player, list, verbose);
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.notargetset"));
            }

            if (verbose)
            {
                int num = 0;
                int sel = 0;

                if (this.useAbsoluteModuleIndexing(linkCrystalStack))
                {
                    sel = UtilItemModular.getStoredModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
                    num = this.getMaxModules(stack, ModuleType.TYPE_LINKCRYSTAL);
                }
                else
                {
                    sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
                    num = this.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
                }

                String dName = (linkCrystalStack.hasDisplayName() ? preWhiteIta + linkCrystalStack.getDisplayName() + rst + " " : "");
                list.add(I18n.format("enderutilities.tooltip.item.selectedlinkcrystal.short") +
                        String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
            }
        }
        else if (this.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL) > 0)
        {
            if (verbose)
            {
                int num = 0;

                if (this.useAbsoluteModuleIndexing(linkCrystalStack))
                {
                    num = this.getMaxModules(stack, ModuleType.TYPE_LINKCRYSTAL);
                }
                else
                {
                    num = this.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
                }

                list.add(I18n.format("enderutilities.tooltip.item.selectedlinkcrystal.short") +
                        String.format(" (%s-%s / %s%d%s)", preBlue, rst, preBlue, num, rst));
            }
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.nolinkcrystals"));
        }

        if (verbose)
        {
            // Item supports Jailer modules, show if one is installed
            if (this.getMaxModules(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                String s;

                if (this.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                {
                    s = I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.GREEN + I18n.format("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    s = I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.RED + I18n.format("enderutilities.tooltip.item.no") + rst;
                }

                list.add(s);
            }

            if (this.useBindLocking(stack))
            {
                String s;
                if (this.isBindLocked(stack))
                {
                    s = I18n.format("enderutilities.tooltip.item.bindlocked") + ": " +
                            TextFormatting.GREEN + I18n.format("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    s = I18n.format("enderutilities.tooltip.item.bindlocked") + ": " +
                            TextFormatting.RED + I18n.format("enderutilities.tooltip.item.no") + rst;
                }

                list.add(s);
            }

            // Ender Capacitor charge, if one has been installed
            ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);

            if (capacitorStack.isEmpty() == false && capacitorStack.getItem() instanceof ItemEnderCapacitor)
            {
                ((ItemEnderCapacitor) capacitorStack.getItem()).addTooltipLines(capacitorStack, player, list, verbose);
            }
        }
    }

    @Override
    public void setTarget(ItemStack stack, EntityPlayer player, boolean storeRotation)
    {
        UtilItemModular.setTarget(stack, player, storeRotation);
    }

    @Override
    public void setTarget(ItemStack toolStack, EntityPlayer player, BlockPos pos, EnumFacing side, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeRotation)
    {
        UtilItemModular.setTarget(toolStack, player, pos, side, hitX, hitY, hitZ, doHitOffset, storeRotation);
    }

    public boolean useAbsoluteModuleIndexing(ItemStack stack)
    {
        return false;
    }

    public boolean useBindLocking(ItemStack stack)
    {
        return false;
    }

    public boolean isBindLocked(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, null, "BindLocked");
    }

    @Override
    public void changePrivacyMode(ItemStack containerStack, EntityPlayer player)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            UtilItemModular.changePrivacyModeOnSelectedModuleAbs(containerStack, player, ModuleType.TYPE_LINKCRYSTAL);
        }
        else
        {
            OwnerData.togglePrivacyModeOnSelectedModule(containerStack, ModuleType.TYPE_LINKCRYSTAL, player);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Ctrl + (Shift + ) Toggle mode
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
            EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
        }
        // Shift + Toggle: Toggle bind lockd state
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            if (this.useBindLocking(stack))
            {
                NBTUtils.toggleBoolean(stack, null, "BindLocked");
            }
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getInstalledModuleCount(containerStack, moduleType);
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        return 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.isEmpty() || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the in-world/location type Link Crystals by default
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION)
        {
            return this.getMaxModules(containerStack, moduleType);
        }

        return 0;
    }

    @Override
    public int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(containerStack, moduleType);
    }

    @Override
    public int getSelectedModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            UtilItemModular.getSelectedModuleTierAbs(containerStack, moduleType);
        }

        return UtilItemModular.getSelectedModuleTier(containerStack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            return UtilItemModular.getSelectedModuleStackAbs(containerStack, moduleType);
        }

        return UtilItemModular.getSelectedModuleStack(containerStack, moduleType);
    }

    @Override
    public boolean setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack moduleStack)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            UtilItemModular.setSelectedModuleStackAbs(containerStack, moduleType, moduleStack);
        }

        return UtilItemModular.setSelectedModuleStack(containerStack, moduleType, moduleStack);
    }

    @Override
    public boolean changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            return UtilItemModular.changeSelectedModuleAbs(containerStack, moduleType, reverse);
        }

        return UtilItemModular.changeSelectedModule(containerStack, moduleType, reverse);
    }
}
