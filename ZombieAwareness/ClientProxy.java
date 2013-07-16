package ZombieAwareness;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    public ClientProxy()
    {
        mc = ModLoader.getMinecraftInstance();
    }

    @Override
    public void init(ZombieAwareness pMod)
    {
        super.init(pMod);
        
        
        RenderingRegistry.registerEntityRenderingHandler(EntityScent.class, new RenderScent());
        
        
    }
    
    @Override
    public int getUniqueTextureLoc()
    {
        return RenderingRegistry.getUniqueTextureIndex("/terrain.png");
    }

    /**
     * This is for registering armor types, like ModLoader.addArmor used to do
     */
    public int getArmorNumber(String type)
    {
        return RenderingRegistry.addNewArmourRendererPrefix(type);
    }

    @Override
    public void displayRecordGui(String displayText)
    {
        //System.out.println("displayRecordGui");
        ModLoader.getMinecraftInstance().ingameGUI.setRecordPlayingMessage(displayText);
    }
}
