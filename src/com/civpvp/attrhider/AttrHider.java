package com.civpvp.attrhider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

/**
 * Uses ProtocolLib to strip away stuff that should never have been sent in the first place
 * such as enchantment, durability and potion duration information.
 * @author Squeenix
 *
 */
public class AttrHider extends JavaPlugin implements Listener {
	private ProtocolManager protocolManager;

	@Override
	public void onEnable() {
	    registerPacketListeners();
	    Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	private void registerPacketListeners(){
		protocolManager = ProtocolLibrary.getProtocolManager();

        //Strips armour
        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent e) {
                try {
                    PacketContainer p = e.getPacket();
                    StructureModifier<ItemStack> items = p.getItemModifier();
                    ItemStack i = items.read(0);
                    if (i != null) {
                        adjustEnchantment(i);
                        items.write(0, i);
                    }
                } catch (FieldAccessException exception) { //Should catch if the packet is the wrong type
                    exception.printStackTrace();
                }
            }
        });

        //Strips potion duration length and sets it to 420 ticks so you can blaze it
	    protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.ENTITY_EFFECT){
	    	@Override
	    	public void onPacketSending(PacketEvent e){
	    		try{
		    		PacketContainer p = e.getPacket();
		    		if(e.getPlayer().getEntityId()!=p.getIntegers().read(0)){ //Make sure it's not the player
		    			p.getShorts().write(0, (short)420);
		    		}
		    		
	    		} catch (FieldAccessException exception){ 
	    			exception.printStackTrace();
	    		}
	    	}
	    });
	    
	    //Make reported health random
	    ProtocolLibrary.getProtocolManager().addPacketListener(
	    	      new PacketAdapter(this, ListenerPriority.NORMAL, new PacketType[] { PacketType.Play.Server.ENTITY_METADATA }) {
	    	      public void onPacketSending(PacketEvent event) {
	    	        try {
	    	          Player observer = event.getPlayer();
	    	          StructureModifier entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
	    	          Entity entity = (Entity)entityModifer.read(0);
	    	          if ((entity != null) && (observer != entity) && ((entity instanceof LivingEntity)) &&
	    	            ((!(entity instanceof EnderDragon)) || (!(entity instanceof Wither))) && (
	    	            (entity.getPassenger() == null) || (entity.getPassenger() != observer))) {
	    	            event.setPacket(event.getPacket().deepClone());
	    	            StructureModifier watcher = event.getPacket()
	    	              .getWatchableCollectionModifier();
	    	            for (WrappedWatchableObject watch : (List<WrappedWatchableObject>)watcher.read(0)) {
	    	              if ((watch.getIndex() == 6) && 
	    	                (((Float)watch.getValue()).floatValue() > 0.0F))
	    	                watch.setValue(
	    	                  Float.valueOf(new Random().nextInt((int)((Damageable)(LivingEntity)entity).getMaxHealth()) + 
	    	                  new Random().nextFloat()));
	    	            }
	    	          }
	    	        }
	    	        catch (Exception e)
	    	        {
	    	          e.printStackTrace();
	    	        }
	    	      }
	    	    });
	        
	}

    private ItemStack adjustEnchantment(ItemStack i) {
        if (i != null) {
            Material type = i.getData().getItemType();
            /* Only applying to commonly enchanted items because
             * Items such as potions and wood rely on damage values for appearance
             */
            if (type == Material.DIAMOND_HELMET
                    || type == Material.DIAMOND_CHESTPLATE
                    || type == Material.DIAMOND_LEGGINGS
                    || type == Material.DIAMOND_BOOTS
                    || type == Material.IRON_HELMET
                    || type == Material.IRON_CHESTPLATE
                    || type == Material.IRON_LEGGINGS
                    || type == Material.IRON_BOOTS
                    || type == Material.GOLD_HELMET
                    || type == Material.GOLD_CHESTPLATE
                    || type == Material.GOLD_LEGGINGS
                    || type == Material.GOLD_BOOTS
                    || type == Material.LEATHER_HELMET
                    || type == Material.LEATHER_CHESTPLATE
                    || type == Material.LEATHER_LEGGINGS
                    || type == Material.LEATHER_BOOTS
                    || type == Material.DIAMOND_SWORD
                    || type == Material.GOLD_SWORD
                    || type == Material.IRON_SWORD
                    || type == Material.STONE_SWORD
                    || type == Material.WOOD_SWORD
                    || type == Material.DIAMOND_AXE
                    || type == Material.GOLD_AXE
                    || type == Material.IRON_AXE
                    || type == Material.STONE_AXE
                    || type == Material.WOOD_AXE
                    || type == Material.DIAMOND_PICKAXE
                    || type == Material.GOLD_PICKAXE
                    || type == Material.IRON_PICKAXE
                    || type == Material.STONE_PICKAXE
                    || type == Material.WOOD_PICKAXE
                    || type == Material.DIAMOND_SPADE
                    || type == Material.GOLD_SPADE
                    || type == Material.IRON_SPADE
                    || type == Material.STONE_SPADE
                    || type == Material.WOOD_SPADE) {
                Object[] copy = i.getEnchantments().keySet().toArray();

                for (Object enchantment : copy) {
                    i.removeEnchantment((Enchantment) enchantment);
                }
                i.setDurability((short) 1);
                if (copy.length > 0) {
                    i.addEnchantment(Enchantment.DURABILITY, 1);
                }
            }
        }
        return i;
    }

    @EventHandler
	public void onMount(final VehicleEnterEvent event) {
		if ((event.getEntered() instanceof Player))
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	        public void run() {
	        	if ((event.getVehicle().isValid()) && (event.getEntered().isValid())) {
	        		protocolManager.updateEntity(event.getVehicle(), Arrays.asList(new Player[] { (Player)event.getEntered() }));
	        	}
	        }
		});
	}
}

