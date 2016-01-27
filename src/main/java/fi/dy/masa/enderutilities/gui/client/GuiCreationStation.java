package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.common.Optional;

import codechicken.nei.guihook.IGuiSlotDraw;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerCreationStation;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.ModRegistry;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.InventoryUtils;

@Optional.Interface(iface = "codechicken.nei.guihook.IGuiSlotDraw", modid = "NotEnoughItems")
public class GuiCreationStation extends GuiEnderUtilities implements IGuiSlotDraw, IButtonCallback
{
    protected static RenderItem itemRenderCustom = new RenderItemLargeStacks();
    protected TileEntityCreationStation tecs;
    protected ContainerCreationStation containerCS;
    protected ResourceLocation guiTextureWidgets;
    public static final int[] ACTION_BUTTON_POSX = new int[] { 41, 59, 77, 149, 167, 185 };
    public static final int[] CRAFTING_BUTTON_POSX = new int[] { 44, 57, 70, 186, 173, 160 };
    public static final String[] BUTTON_STRINGS = new String[] {
            "enderutilities.gui.label.moveallitems",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.leaveonefilledstack",
            "enderutilities.gui.label.fillstacks",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.moveallitems",
            "enderutilities.gui.label.slowfasttoggle",
            "enderutilities.gui.label.clearcraftinggrid",
            "enderutilities.gui.label.useoredictionary",
            "enderutilities.gui.label.leaveoneitemongrid",
            "enderutilities.gui.label.useitemsfrominventory"
    };

    public GuiCreationStation(ContainerCreationStation container, TileEntityCreationStation te)
    {
        super(container, 240, 256, "gui.container.creationstation");
        this.tecs = te;
        this.containerCS = container;
        this.guiTextureWidgets = ReferenceTextures.getGuiTexture("gui.widgets");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.createButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        if (ModRegistry.isModLoadedNEI() == false)
        {
            // Swap the RenderItem() instance for the duration of rendering the ItemStacks to the GUI
            RenderItem ri = this.setItemRender(itemRenderCustom);
            super.drawScreen(mouseX, mouseY, gameTicks);
            this.setItemRender(ri);
        }
        else
        {
            super.drawScreen(mouseX, mouseY, gameTicks);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.creationstation", new Object[0]), 80, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int invSize = this.tecs.getItemInventory().getSizeInventory();
        int modeMask = this.tecs.getModeMask();

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(this.guiLeft + 204, this.guiTop + 105 + this.tecs.getSelectedModule() * 18, 120, 0, 10, 10);

        // Draw the hilight background for the selected module slot
        this.drawTexturedModalRect(this.guiLeft + 215, this.guiTop + 101 + this.tecs.getSelectedModule() * 18, 102, 18, 18, 18);

        int x = 9;
        int y = 156;
        int mode = this.tecs.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            x = ACTION_BUTTON_POSX[mode];
        }

        // Draw the selection marker around the selected action button, ie. the "Quick Action"
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 120, 10, 14, 14);

        // Draw the selection marker around selected crafting mode buttons
        mode = modeMask & 0xFF;
        for (int i = 0, bit = 0x1; i < 6; i++, bit <<= 1)
        {
            if ((mode & bit) != 0)
            {
                this.drawTexturedModalRect(this.guiLeft + CRAFTING_BUTTON_POSX[i], this.guiTop + 87, 120, 0, 10, 10);
            }
        }

        // Draw the selection border around the selected crafting preset buttons
        mode = (modeMask >> 8) & 0x7;
        this.drawTexturedModalRect(this.guiLeft +  28, this.guiTop + 32 + mode * 11, 120, 0, 10, 10);
        mode = (modeMask >> 11) & 0x7;
        this.drawTexturedModalRect(this.guiLeft + 202, this.guiTop + 32 + mode * 11, 120, 0, 10, 10);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.tecs.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < invSize; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background icon over the disabled crafting slots (left side)
            for (int i = 31; i <= 39; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background icon over the disabled crafting slots (right side)
            for (int i = 41; i <= 49; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerCS.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.containerCS.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 18, 18, 18);
        }

        // Left side furnace progress icons

        boolean isFast = (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_FAST) != 0;

        // Draw the burn progress flame
        int vOff = (isFast == true ? 34 : 20);
        int h = (this.containerCS.fuelProgress & 0x7F) * 13 / 100;
        this.drawTexturedModalRect(this.guiLeft + 9, this.guiTop + 30 + 12 - h, 134, vOff + 13 - h, 14, h + 1);

        // Draw the smelting progress arrow
        if ((this.containerCS.smeltProgress & 0xFF) > 0)
        {
            vOff = isFast == true ? 10 : 0;
            int w = (this.containerCS.smeltProgress & 0xFF) * 11 / 100;
            this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 11, 134, vOff, w, 10);
        }

        // Right side furnace progress icons

        isFast = (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_FAST) != 0;

        // Draw the burn progress flame
        vOff = (isFast == true ? 34 : 20);
        h = ((this.containerCS.fuelProgress >> 8) & 0x7F) * 13 / 100;
        this.drawTexturedModalRect(this.guiLeft + 217, this.guiTop + 30 + 12 - h, 134, vOff + 13 - h, 14, h + 1);

        // Draw the smelting progress arrow
        if ((this.containerCS.smeltProgress >> 8) > 0)
        {
            vOff = isFast == true ? 10 : 0;
            int w = ((this.containerCS.smeltProgress >> 8) & 0x7F) * 11 / 100;
            this.drawTexturedModalRect(this.guiLeft + 203 + 10 - w, this.guiTop + 11, 144 + 10 - w, vOff, w, 10);
        }

        // Draw the red or purple background under non-matching crafting grid slots
        for (int i = 0; i < 2; i++)
        {
            if (this.tecs.getShowRecipe(i) == true)
            {
                for (int slotNum = 0; slotNum < 9; slotNum++)
                {
                    ItemStack gridStack = this.containerCS.craftMatrices[i].getStackInSlot(slotNum);
                    ItemStack recipeStack = this.tecs.getRecipeItems(i)[slotNum];

                    if (InventoryUtils.areItemStacksEqual(gridStack, recipeStack) == false)
                    {
                        Slot slot = this.containerCS.getSlot(31 + i * 10 + slotNum);
                        x = this.guiLeft + slot.xDisplayPosition - 1;
                        y = this.guiTop + slot.yDisplayPosition - 1;

                        // Missing items, red background
                        if (gridStack == null)
                        {
                            this.bindTexture(this.guiTextureWidgets);

                            GL11.glDisable(GL11.GL_LIGHTING);
                            this.drawTexturedModalRect(x, y, 102, 72, 18, 18);
                            GL11.glEnable(GL11.GL_LIGHTING);

                            RenderHelper.enableGUIStandardItemLighting();
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
                            this.zLevel = 100.0F;
                            itemRender.zLevel = 100.0F;
                            GL11.glEnable(GL11.GL_DEPTH_TEST);
                            GL11.glEnable(GL11.GL_BLEND);
                            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), recipeStack, x + 1, y + 1);

                            itemRender.zLevel = 0.0F;
                            this.zLevel = 0.0F;

                            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), recipeStack, x + 1, y + 1, "0");
                        }
                        // Extra items, purple background
                        else if (recipeStack == null)
                        {
                            this.bindTexture(this.guiTextureWidgets);
                            GL11.glDisable(GL11.GL_LIGHTING);
                            this.drawTexturedModalRect(x, y, 102, 36, 18, 18);
                            GL11.glEnable(GL11.GL_LIGHTING);
                            //itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), recipeStack, x, y, "+");
                        }
                        // Wrong items, red background
                        else
                        {
                            this.bindTexture(this.guiTextureWidgets);
                            GL11.glDisable(GL11.GL_LIGHTING);
                            this.drawTexturedModalRect(x, y, 102, 72, 18, 18);
                            GL11.glEnable(GL11.GL_LIGHTING);
                        }
                    }
                }
            }
        }

        //GL11.glEnable(GL11.GL_BLEND);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_DEPTH_TEST);
        //RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        IIcon icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0; icon != null && i < 4; i++)
        {
            if (this.tecs.getStackInSlot(i) == null)
            {
                this.drawTexturedModelRectFromIcon(this.guiLeft + 216, this.guiTop + 102 + i * 18, icon, 16, 16);
            }
        }

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8*/
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        super.drawTooltips(mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        x += 7;
        y += 89;

        if (mouseX >= x && mouseX <= x + 17 && mouseY >= y && mouseY <= y + 17)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.creationstation.info", list, false);
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Add the Memory Card selection buttons
        for (int i = 0; i < 4; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + 205, y + 106 + i * 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Add the quick-move-items buttons
        for (int i = 0; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(i + 4, x + ACTION_BUTTON_POSX[i] + 1, y + 157, 12, 12, 24, i * 12,
                    this.guiTextureWidgets, 12, 0, BUTTON_STRINGS[i]));
        }

        // Crafting grid clear buttons
        this.buttonList.add(new GuiButtonHoverText(10, x +  84, y + 88, 8, 8, 0,  8, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[7]));
        this.buttonList.add(new GuiButtonHoverText(11, x + 148, y + 88, 8, 8, 0,  8, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[7]));

        // Add other left side crafting grid buttons
        this.buttonList.add(new GuiButtonHoverText(12, x + 45, y + 88, 8, 8, 0, 32, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[8]));
        this.buttonList.add(new GuiButtonHoverText(13, x + 58, y + 88, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[9]));
        this.buttonList.add(new GuiButtonHoverText(14, x + 71, y + 88, 8, 8, 0, 16, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[10]));

        // Add other right side crafting grid buttons
        this.buttonList.add(new GuiButtonHoverText(15, x + 161, y + 88, 8, 8, 0, 16, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[10]));
        this.buttonList.add(new GuiButtonHoverText(16, x + 174, y + 88, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[9]));
        this.buttonList.add(new GuiButtonHoverText(17, x + 187, y + 88, 8, 8, 0, 32, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[8]));

        // Add the left and right side furnace mode buttons
        this.buttonList.add(new GuiButtonCallback(18, x +   9, y + 71, 14, 14, 60, 0, this.guiTextureWidgets, 14, 0,
                0, this, BUTTON_STRINGS[6]));
        this.buttonList.add(new GuiButtonCallback(19, x + 217, y + 71, 14, 14, 60, 0, this.guiTextureWidgets, 14, 0,
                1, this, BUTTON_STRINGS[6]));

        // Add the recipe recall buttons
        for (int i = 0; i < 5; i++)
        {
            this.buttonList.add(new GuiButtonIcon(20 + i, x +  29, y + 33 + i * 11, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
            this.buttonList.add(new GuiButtonIcon(25 + i, x + 203, y + 33 + i * 11, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }
    }

    @Override
    public int getButtonU(int callbackId)
    {
        return 60;
    }

    @Override
    public int getButtonV(int callbackId)
    {
        if (callbackId == 1)
        {
            return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_FAST) != 0 ? 14 : 0;
        }

        return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_FAST) != 0 ? 14 : 0;
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton)
    {
        super.actionPerformed(button);

        boolean valid = true;
        int action = 0;
        int element = 0;

        if (button.id >= 0 && button.id <= 3)
        {
            action = TileEntityCreationStation.GUI_ACTION_SELECT_MODULE;
            element = button.id - 0;
        }
        else if (button.id >= 4 && button.id <= 9)
        {
            if (isShiftKeyDown() == true || mouseButton != 0)
            {
                action = TileEntityCreationStation.GUI_ACTION_SET_QUICK_ACTION;
            }
            else
            {
                action = TileEntityCreationStation.GUI_ACTION_MOVE_ITEMS;
            }

            element = button.id - 4;
        }
        // Clear crafting grid buttons
        else if (button.id == 10 || button.id == 11)
        {
            action = TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID;
            element = button.id - 10;
        }
        // Crafting grid mode buttons and furnace mode buttons
        else if (button.id >= 12 && button.id <= 19)
        {
            action = TileEntityCreationStation.GUI_ACTION_TOGGLE_MODE;
            element = button.id - 12;
        }
        // Recipe recall buttons
        else if (button.id >= 20 && button.id <= 29)
        {
            // Left click: Load recipe or load items from recipe into crafting grid, or if sneaking, store the recipe
            if (mouseButton == 0)
            {
                if (isShiftKeyDown() == true)
                {
                    action = TileEntityCreationStation.GUI_ACTION_RECIPE_STORE;
                }
                else
                {
                    action = TileEntityCreationStation.GUI_ACTION_RECIPE_LOAD;
                }

                element = button.id - 20;
            }
            // Right click: Clear crafting grid
            else if (mouseButton == 1)
            {
                action = TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID;
                element = (button.id - 20) / 5;
            }
            // Middle click: Remove recipe
            else if (mouseButton == 2)
            {
                action = TileEntityCreationStation.GUI_ACTION_RECIPE_CLEAR;
                element = button.id - 20;
            }
        }
        else
        {
            valid = false;
        }

        if (valid == true)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, action, element));
        }
    }

    protected RenderItem setItemRender(RenderItem itemRenderIn)
    {
        RenderItem ri = itemRender;
        itemRender = itemRenderIn;
        return ri;
    }

    @Optional.Method(modid = "NotEnoughItems")
    @Override
    public void drawSlotItem(Slot slot, ItemStack stack, int x, int y, String quantity)
    {
        // Slot is in the external inventory, render using the smaller font for stack size
        if (slot.inventory == this.tecs.getItemInventory() || slot.inventory == this.tecs.getFurnaceInventory())
        {
            itemRenderCustom.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRenderCustom.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
        else
        {
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
    }
}