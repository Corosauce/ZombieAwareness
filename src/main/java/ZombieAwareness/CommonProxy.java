package ZombieAwareness;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy implements IGuiHandler
{
    private int entityId = 0;

    public ZombieAwareness mod;

    public CommonProxy()
    {
    }

    public void init(ZombieAwareness pMod)
    {
        mod = pMod;
    	EntityRegistry.registerModEntity(new ResourceLocation(ZombieAwareness.modID,"EntityScent"), EntityScent.class, "EntityScent", entityId++, pMod, 32, 20, false);
    }

    public int getUniqueTextureLoc()
    {
        return 0;
    }

    public int getArmorNumber(String type)
    {
        return 0;
    }

    public int getUniqueTropicraftLiquidID()
    {
        return 0;
    }

    public void loadSounds()
    {
    }

    public void registerRenderInformation()
    {
    }

    public void registerTileEntitySpecialRenderer()
    {
    }

    public void displayRecordGui(String displayText)
    {
    }

    public World getClientWorld()
    {
        return null;
    }

    public World getServerWorld()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null)
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        }
        else
        {
            return null;
        }
    }

    public World getSidesWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    public void weatherDbg()
    {

    }

    public void windDbg()
    {

    }

    public Entity getEntByID(int id) {
		System.out.println("common getEntByID being used, this is bad");
		return null;
	}
}
