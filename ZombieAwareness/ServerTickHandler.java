package ZombieAwareness;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.GuiScreen;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
	
	mod_ZombieAwareness mod;
	
    public ServerTickHandler(mod_ZombieAwareness mod_ZombieAwareness) {
		// TODO Auto-generated constructor stub
    	mod = mod_ZombieAwareness;
	}

	@Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    	
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	mod.onTick(MinecraftServer.getServer());
        //System.out.println("onTickInGame");
        //TODO: Your Code Here
    }
}
