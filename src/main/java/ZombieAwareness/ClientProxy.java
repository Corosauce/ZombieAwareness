package ZombieAwareness;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    
    public ClientProxy()
    {
        
    }

    @Override
    public void init(ZombieAwareness pMod)
    {
        super.init(pMod);
        
        
        RenderingRegistry.registerEntityRenderingHandler(EntityScent.class, new RenderScent(Minecraft.getMinecraft().getRenderManager()));
        
        
    }
}
