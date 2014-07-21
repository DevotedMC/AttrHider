package com.civpvp.attrhider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
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

