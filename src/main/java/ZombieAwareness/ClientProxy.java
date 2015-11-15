package ZombieAwareness;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    
    public ClientProxy()
    {
        
    }

    @Override
    public void init(ZombieAwareness pMod)
    {
        super.init(pMod);
        
        
        RenderingRegistry.registerEntityRenderingHandler(EntityScent.class, new RenderScent());
        
        
    }
}
