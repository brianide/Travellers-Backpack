package com.tiviacz.travellersbackpack.handlers;

import com.tiviacz.travellersbackpack.TravellersBackpack;
import com.tiviacz.travellersbackpack.blocks.BlockSleepingBag;
import com.tiviacz.travellersbackpack.capability.CapabilityUtils;
import com.tiviacz.travellersbackpack.gui.GuiOverlay;
import com.tiviacz.travellersbackpack.gui.container.slots.SlotTool;
import com.tiviacz.travellersbackpack.items.ItemHose;
import com.tiviacz.travellersbackpack.items.ItemTravellersBackpack;
import com.tiviacz.travellersbackpack.network.CycleToolPacket;
import com.tiviacz.travellersbackpack.network.GuiPacket;
import com.tiviacz.travellersbackpack.proxy.ClientProxy;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = TravellersBackpack.MODID, value = Side.CLIENT)
public class ClientEventHandler 
{
	@SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) 
	{
        final EntityPlayer player = event.getEntityPlayer();
        
        if(player instanceof EntityOtherPlayerMP && player.isPlayerSleeping() && player.bedLocation != null) 
        {
            Block bed = player.world.getBlockState(player.bedLocation).getBlock();
            
            if(bed instanceof BlockSleepingBag) 
            {
                player.renderOffsetY = -0.375F;
            }
        }
    }

	@SubscribeEvent
	public static void stitcherEventPre(TextureStitchEvent.Pre event) 
	{
	    ResourceLocation milk_still = new ResourceLocation(TravellersBackpack.MODID, "blocks/milk_still");
	    ResourceLocation milk_flow = new ResourceLocation(TravellersBackpack.MODID, "blocks/milk_flow");
	    ResourceLocation potion_still = new ResourceLocation(TravellersBackpack.MODID, "blocks/potion_still");
	    ResourceLocation potion_flow = new ResourceLocation(TravellersBackpack.MODID, "blocks/potion_flow");
	    
	    event.getMap().registerSprite(milk_still);
	    event.getMap().registerSprite(milk_flow);
	    event.getMap().registerSprite(potion_still);
	    event.getMap().registerSprite(potion_flow);
	    
	    //Mushroom Stew
	//    ResourceLocation mushroom_stew_still = new ResourceLocation(TravellersBackpack.MODID + ":blocks/mushroom_stew_still");
	//    ResourceLocation mushroom_stew_flow = new ResourceLocation(TravellersBackpack.MODID + ":blocks/mushroom_stew_flow");
	    
	//    event.getMap().registerSprite(mushroom_stew_still);
	//    event.getMap().registerSprite(mushroom_stew_flow);
	}
	
	@SubscribeEvent
	public static void onRenderExperienceBar(RenderGameOverlayEvent.Post event)
	{
		if(ConfigHandler.enableOverlay)
        {
			if(event.getType() != ElementType.EXPERIENCE) return;
			
			if(CapabilityUtils.isWearingBackpack(Minecraft.getMinecraft().player))
		    {
				GuiOverlay gui = new GuiOverlay();
				gui.renderOverlay();
		    }
        }
	}
	
	@SubscribeEvent
    public static void handleKeyInputEvent(KeyInputEvent event)
    {
    	if(!ConfigHandler.enableToolCycling)
    		return;

		KeyBinding openBackpackKey = ClientProxy.openBackpack;
		KeyBinding toggleTankKey = ClientProxy.toggleTank;
		KeyBinding cycleToolKey = ClientProxy.cycleTool;
		EntityPlayer player = Minecraft.getMinecraft().player;

		if(player != null && CapabilityUtils.isWearingBackpack(player)) {

			if(openBackpackKey.isPressed())
			{
				TravellersBackpack.NETWORK.sendToServer(new GuiPacket(GuiPacket.Handler.BACKPACK_GUI, GuiPacket.Handler.FROM_KEYBIND));
			}

			if(toggleTankKey.isPressed())
			{
				TravellersBackpack.NETWORK.sendToServer(new CycleToolPacket(0, CycleToolPacket.Handler.TOGGLE_HOSE_TANK));
			}

			if(cycleToolKey.isPressed())
			{
				ItemStack heldItem = player.getHeldItemMainhand();

				if(SlotTool.isValid(heldItem))
				{
					TravellersBackpack.NETWORK.sendToServer(new CycleToolPacket(1, CycleToolPacket.Handler.CYCLE_TOOL_ACTION));
				}
				else if(heldItem.getItem() instanceof ItemHose)
				{
					TravellersBackpack.NETWORK.sendToServer(new CycleToolPacket(1, CycleToolPacket.Handler.SWITCH_HOSE_ACTION));
				}
			}

		}
    }
	
	@SubscribeEvent
    public static void mouseWheelDetect(MouseEvent event)
    {
    	if(!ConfigHandler.enableWheelCycling)
    		return;

		Minecraft mc = Minecraft.getMinecraft();
	    int dWheel = event.getDwheel();
	        
	    if(dWheel != 0)
	    {
	    	EntityPlayerSP player = mc.player;
	            
	    	if(player != null && !player.isDead && player.isSneaking())
	    	{
	    		ItemStack backpack = CapabilityUtils.getWearingBackpack(player);
	                
	    		if(backpack != null && backpack.getItem() instanceof ItemTravellersBackpack)
	    		{
	    			if(player.getHeldItemMainhand() != null)
	    			{
	    				ItemStack heldItem = player.getHeldItemMainhand();

	    				if(ConfigHandler.enableToolCycling)
	    				{
	    					if(SlotTool.isValid(heldItem))
	    					{
	    						TravellersBackpack.NETWORK.sendToServer(new CycleToolPacket(dWheel, CycleToolPacket.Handler.CYCLE_TOOL_ACTION));
	    						event.setCanceled(true);
	    					}
	    				}
	                        
	    				if(heldItem.getItem() instanceof ItemHose)
	    				{
	    					if(heldItem.getTagCompound() != null)
	    					{
	    						TravellersBackpack.NETWORK.sendToServer(new CycleToolPacket(dWheel, CycleToolPacket.Handler.SWITCH_HOSE_ACTION));
	    						event.setCanceled(true);
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
    }
}