package ZombieAwareness;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import CoroUtil.util.CoroUtilBlock;
import ZombieAwareness.config.ZAConfig;

public class RenderScent extends Render {

    public static final boolean renderLine = true;
    public static final boolean renderBobber = false;
    public static float yoffset = 0.4F;
    public static float caughtOffset = 0.8F;
    public static int stringColor = 8947848;


    public void doRenderNode(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        //System.out.println("1");

        //System.out.println("2");
        if (((EntityScent)var1).type == 0) {
        	renderImage(var1, var2, var4, var6, var8, var9);
            //renderImage(var1, var2, var4, var6, var8, var9, "/misc/shadow.png");
        } else {
            
        }

        //}
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(ZombieAwareness.modID + ":textures/entities/blood.png");
	}

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
    	//call texture set
    	bindEntityTexture(var1);
    	//if (!ZAConfig.client_renderBlood) return;
    	
        //if (((EntityScent)var1).type == 0) {
        shadowSize = 1.0F;
        //System.out.println("!!!!!!!!!!!!!! - " + (double)(((double)((EntityScent)var1).strength)/100.0D));
        //}
        GL11.glPushMatrix();
        if (ZAConfig.client_renderBlood) {
        	this.doRenderNode(var1, var2, var4, var6, var8, var9);
        }
        if (ZAConfig.client_debugRenderSounds && ((EntityScent)var1).type != 0) {
        	renderOffsetAABB(var1.boundingBox, var2 - var1.lastTickPosX, var4 - var1.lastTickPosY, var6 - var1.lastTickPosZ, ((EntityScent)var1).type == 1, ((EntityScent)var1).getRange());
        }
        shadowSize = 0.0F;
        //
        GL11.glPopMatrix();
    }

    private void renderImage(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        GL11.glEnable(3042 /*GL_BLEND*/);
        GL11.glBlendFunc(770, 771);
        
        //RenderEngine var10 = this.renderManager.renderEngine;
        //this.loadTexture(img);
        //var10.bindTexture(var10.getTexture(img));
        World var11 = this.getWorldFromRenderManager();
        GL11.glDepthMask(false);
        float var12 = this.shadowSize;
        double var13 = var1.lastTickPosX + (var1.posX - var1.lastTickPosX) * (double)var9;
        double var15 = var1.lastTickPosY + (var1.posY - var1.lastTickPosY) * (double)var9 + (double)var1.getShadowSize();
        double var17 = var1.lastTickPosZ + (var1.posZ - var1.lastTickPosZ) * (double)var9;
        int var19 = MathHelper.floor_double(var13 - (double)var12);
        int var20 = MathHelper.floor_double(var13 + (double)var12);
        int var21 = MathHelper.floor_double(var15 - (double)var12);
        int var22 = MathHelper.floor_double(var15);
        int var23 = MathHelper.floor_double(var17 - (double)var12);
        int var24 = MathHelper.floor_double(var17 + (double)var12);
        double var25 = var2 - var13;
        double var27 = var4 - var15;
        double var29 = var6 - var17;
        Tessellator var31 = Tessellator.instance;
        var31.startDrawingQuads();
        double str = (double)(((double)((EntityScent)var1).strength)/100.0D);

        //System.out.println("hmmm? - " + var1 + " - " + str);
        for(int var32 = var19; var32 <= var20; ++var32) {
            for(int var33 = var21; var33 <= var22; ++var33) {
                for(int var34 = var23; var34 <= var24; ++var34) {
                    Block var35 = var11.getBlock(var32, var33 - 1, var34);

                    //System.out.println("1 - " + var35 + " - " + var11.getBlockLightValue(var32, var33, var34));
                    if(!CoroUtilBlock.isAir(var35) && var11.getBlockLightValue(var32, var33, var34) > 3) {
                        //System.out.println("2");
                        this.renderImageOnBlock(var35, var2, var4 + (double)var1.getShadowSize(), var6, var32, var33, var34, var8, var12, var25, var27 + (double)var1.getShadowSize(), var29, str);
                    }
                }
            }
        }

        var31.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042 /*GL_BLEND*/);
        GL11.glDepthMask(true);
    }

    private World getWorldFromRenderManager() {
        return this.renderManager.worldObj;
    }

    private void renderImageOnBlock(Block var1, double var2, double var4, double var6, int var8, int var9, int var10, float var11, float var12, double var13, double var15, double var17, double transparency) {
        Tessellator var19 = Tessellator.instance;

        //System.out.println("3");
        if(var1.renderAsNormalBlock()) {
            double var20 = transparency;//((double)var11 - (var4 - ((double)var9 + var15)) / 2.0D) * 0.5D * (double)this.getWorldFromRenderManager().getLightBrightness(var8, var9, var10);

            //System.out.println("4");
            //if(var20 >= 0.0D) {
            //System.out.println("5");
            if(var20 > 1.0D) {
                var20 = 1.0D;
            }

            //System.out.println(var20);
            var19.setColorRGBA_F(1.0F, 1.0F, 1.0F, (float)var20);
            double var22 = (double)var8 + var1.getBlockBoundsMinX() + var13;
            double var24 = (double)var8 + var1.getBlockBoundsMaxX() + var13;
            double var26 = (double)var9 + var1.getBlockBoundsMinY() + var15 + 0.015625D;
            double var28 = (double)var10 + var1.getBlockBoundsMinZ() + var17;
            double var30 = (double)var10 + var1.getBlockBoundsMaxZ() + var17;
            float var32 = (float)((var2 - var22) / 2.0D / (double)var12 + 0.5D);
            float var33 = (float)((var2 - var24) / 2.0D / (double)var12 + 0.5D);
            float var34 = (float)((var6 - var28) / 2.0D / (double)var12 + 0.5D);
            float var35 = (float)((var6 - var30) / 2.0D / (double)var12 + 0.5D);
            var19.addVertexWithUV(var22, var26, var28, (double)var32, (double)var34);
            var19.addVertexWithUV(var22, var26, var30, (double)var32, (double)var35);
            var19.addVertexWithUV(var24, var26, var30, (double)var33, (double)var35);
            var19.addVertexWithUV(var24, var26, var28, (double)var33, (double)var34);
            //}
        }
    }

    public static void renderOffsetAABB(AxisAlignedBB var0, double var1, double var3, double var5, boolean var7, float size) {
        GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
        Tessellator var8 = Tessellator.instance;

        if(var7) {
            GL11.glColor4f(0.0F, 0.0F, 1.0F, 1.0F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        size /= 80;
        
        var0 = var0.expand(size, size, size);
        
        //GL11.glScalef(size, size, size);
        
        var8.startDrawingQuads();
        var8.setTranslation(var1, var3, var5);
        var8.setNormal(0.0F, 0.0F, -1.0F);
        var8.addVertex(var0.minX, var0.maxY, var0.minZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.minZ);
        var8.addVertex(var0.maxX, var0.minY, var0.minZ);
        var8.addVertex(var0.minX, var0.minY, var0.minZ);
        var8.setNormal(0.0F, 0.0F, 1.0F);
        var8.addVertex(var0.minX, var0.minY, var0.maxZ);
        var8.addVertex(var0.maxX, var0.minY, var0.maxZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.maxZ);
        var8.addVertex(var0.minX, var0.maxY, var0.maxZ);
        var8.setNormal(0.0F, -1.0F, 0.0F);
        var8.addVertex(var0.minX, var0.minY, var0.minZ);
        var8.addVertex(var0.maxX, var0.minY, var0.minZ);
        var8.addVertex(var0.maxX, var0.minY, var0.maxZ);
        var8.addVertex(var0.minX, var0.minY, var0.maxZ);
        var8.setNormal(0.0F, 1.0F, 0.0F);
        var8.addVertex(var0.minX, var0.maxY, var0.maxZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.maxZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.minZ);
        var8.addVertex(var0.minX, var0.maxY, var0.minZ);
        var8.setNormal(-1.0F, 0.0F, 0.0F);
        var8.addVertex(var0.minX, var0.minY, var0.maxZ);
        var8.addVertex(var0.minX, var0.maxY, var0.maxZ);
        var8.addVertex(var0.minX, var0.maxY, var0.minZ);
        var8.addVertex(var0.minX, var0.minY, var0.minZ);
        var8.setNormal(1.0F, 0.0F, 0.0F);
        var8.addVertex(var0.maxX, var0.minY, var0.minZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.minZ);
        var8.addVertex(var0.maxX, var0.maxY, var0.maxZ);
        var8.addVertex(var0.maxX, var0.minY, var0.maxZ);
        var8.setTranslation(0.0D, 0.0D, 0.0D);
        var8.draw();
        GL11.glEnable(3553 /*GL_TEXTURE_2D*/);
    }

}
