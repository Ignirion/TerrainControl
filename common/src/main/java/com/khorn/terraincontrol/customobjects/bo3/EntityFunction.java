package com.khorn.terraincontrol.customobjects.bo3;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.bo3.BO3Config;
import com.khorn.terraincontrol.customobjects.bo3.BO3Loader;
import com.khorn.terraincontrol.customobjects.bo3.BO3PlaceableFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.NamedBinaryTag;

import de.zofenia.server.BO3ToolsEntities;
import de.zofenia.server.NBTHelperEntities;
import net.minecraft.server.v1_10_R1.NBTBase;
import net.minecraft.server.v1_10_R1.NBTTagCompound;

/**
 * Represents a entity in a BO3.
 */
public class EntityFunction extends BO3PlaceableFunction
{

    public final EntityType entitytype;
    public NamedBinaryTag metaDataTag;
    public String metaDataName;
	private float yaw = 0.0f;
	private float pitch = 0.0f;

    public EntityFunction(BO3Config config, List<String> args) throws InvalidConfigException
    {
    	
        super(config);
        
        assureSize(4, args);
        // Those limits are arbitrary, LocalWorld.setBlock will limit it
        // correctly based on what chunks can be accessed
        x = readInt(args.get(0), -100, 100);
        y = readInt(args.get(1), -1000, 1000);
        z = readInt(args.get(2), -100, 100);
        entitytype = readEntityType(args.get(3));
        
        if (args.size() == 5)
        {
        	
        	try {
        	
            metaDataTag = BO3Loader.loadMetadata(args.get(4), getHolder().directory);
            
            
            
        	} catch (Exception e) {
				
			}
        	
        	if (metaDataTag != null)
            {
                metaDataName = args.get(4);
                try {
                yaw = (float)(((NamedBinaryTag)metaDataTag.getTag("Rotation").values()[0]).getValue());
                pitch = (float)(((NamedBinaryTag)metaDataTag.getTag("Rotation").values()[1]).getValue());
                } catch(Exception e) {}
            }
        }
        

        
    }

    private EntityType readEntityType(String string) {
    	
    	EntityType entityType = EntityType.PIG;
    	
    	entityType = EntityType.valueOf(string);
    	
		return entityType;
	}
	
	public EntityFunction(BO3Config config, int x, int y, int z, float yaw, float pitch, EntityType entitytype) {
        super(config);
        this.x = x;
        this.y = y;
        this.z = z;
        this.entitytype = entitytype;
        this.yaw = yaw;
        this.pitch = pitch;
        
	}

    @Override
    public String toString()
    {
        String start = "Entity(" + x + ',' + y + ',' + z + ',' + entitytype.name();
        if (metaDataName != null)
        {
            start += ',' + metaDataName;
        }
        return start + ')';
    }

    @Override
    public EntityFunction rotate()
    {
    	
    	EntityFunction rotatedBlock = new EntityFunction(getHolder(), z, y, -x, yaw - 90, pitch, entitytype);
    	
        rotatedBlock.metaDataTag = metaDataTag;
        rotatedBlock.metaDataName = metaDataName;
        
        return rotatedBlock;
        
    }

    @Override
    public void spawn(LocalWorld world, Random random, int x, int y, int z) {
    	
    	try {
    	
    	if(entitytype == EntityType.PLAYER)
    		return;
    	
    	World globalWorld = ((BukkitWorld)world).getWorld().getWorld();
    	
    	Entity entity = null;
    	
    	
    	
    	
    	
    	
    	if(entitytype == EntityType.PAINTING || entitytype == EntityType.ITEM_FRAME) {
    		
    		try {
    			entity = globalWorld.spawn(new Location(globalWorld, x, y, z, yaw, 0), this.entitytype.getEntityClass());
        		
    			
    			
    		} catch(Exception e) {
    			BO3ToolsEntities.plugin.getLogger().log(Level.WARNING, e.getMessage());
    	        
    		}
    		
    	} else {
    	
    		
    		entity = globalWorld.spawnEntity(new Location(globalWorld, x + 0.5d, y, z + 0.5d, yaw, 0.0f), entitytype);
    		
    		
    	}
        if ((metaDataTag != null) && (entity != null)) {
        	
        	NBTTagCompound tag = NBTHelperEntities.getNMSFromNBTTagCompound(metaDataTag);
        	
            net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            
            NBTTagCompound originalTag = new NBTTagCompound();
            nmsEntity.c(originalTag);
            
            NBTBase originalPos = originalTag.get("Pos");
            NBTBase originalRot = originalTag.get("Rotation");
            
            NBTBase originalUUIDLeast = originalTag.get("UUIDLeast");
            NBTBase originalUUIDMost = originalTag.get("UUIDMost");
            
            if(entitytype == EntityType.PAINTING || entitytype == EntityType.ITEM_FRAME) {
            	
            	tag.setInt("TileX", originalTag.getInt("TileX"));
            	tag.setInt("TileY", originalTag.getInt("TileY"));
            	tag.setInt("TileZ", originalTag.getInt("TileZ"));
            	
            	if(yaw == 0) {
            		tag.setByte("Facing", (byte) 0);
            	} else if(yaw % 360 == 90) {
            		tag.setByte("Facing", (byte) 1);
            	} else if(yaw % 360 == 180) {
            		tag.setByte("Facing", (byte) 2);
            	} else if(yaw % 360 == 270) {
            		tag.setByte("Facing", (byte) 3);
            	} else if(yaw % 360 == -90) {
            		tag.setByte("Facing", (byte) 3);
            	} else if(yaw % 360 == -180) {
            		tag.setByte("Facing", (byte) 2);
            	} else if(yaw % 360 == -270) {
            		tag.setByte("Facing", (byte) 1);
            	}
            }
            
            tag.set("Pos", originalPos);
            tag.set("Rotation", originalRot);
            tag.set("UUIDLeast", originalUUIDLeast);
            tag.set("UUIDMost", originalUUIDMost);
            
            nmsEntity.f(tag);
            nmsEntity.recalcPosition();
            
        } else {
        	if(metaDataTag == null)
        		BO3ToolsEntities.plugin.getLogger().log(Level.WARNING, entity.getType().name() + " at " + entity.getLocation() + " has no MetaData!");
        }
        
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BO3Config> other)
    {
        if(!getClass().equals(other.getClass())) {
            return false;
        }
        EntityFunction block = (EntityFunction) other;
        return block.x == x && block.y == y && block.z == z;
    }
    
}
