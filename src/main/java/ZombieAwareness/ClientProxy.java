package ZombieAwareness;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
        
        
        //RenderingRegistry.registerEntityRenderingHandler(EntityScent.class, new RenderScent());
        
        
    }
}
