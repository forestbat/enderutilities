package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityInteractEventHandler
{
	@SubscribeEvent
	public void onEntityInteractEvent(EntityInteractEvent event)
	{
		ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

		if (stack == null)
		{
			return;
		}

		if(stack.getItem() == EnderUtilitiesItems.enderLasso)
		{
			if (event.entity.worldObj.isRemote == false)
			{
				if (event.target instanceof EntityLivingBase && event.entity instanceof EntityPlayer)
				{
					if (EUConfigs.enderLassoAllowPlayers.getBoolean(false) == true || EntityUtils.doesEntityStackHavePlayers(event.target) == false)
					{
						if (TeleportEntity.teleportEntityUsingItem(event.target, stack) != null)
						{
							event.setCanceled(true);
						}
					}
				}
			}
		}

		if(stack.getItem() == EnderUtilitiesItems.mobHarness)
		{
			if (event.entity.worldObj.isRemote == false)
			{
				if (event.target instanceof EntityLivingBase && event.entity instanceof EntityPlayer)
				{
					((ItemMobHarness)stack.getItem()).handleInteraction(stack, (EntityPlayer)event.entity, event.target);
					event.setCanceled(true);
				}
			}
		}
	}
}
