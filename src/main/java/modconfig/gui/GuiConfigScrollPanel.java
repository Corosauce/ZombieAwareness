package modconfig.gui;

import java.util.List;

import modconfig.ConfigEntryInfo;
import modconfig.ConfigMod;
import modconfig.ModConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiConfigScrollPanel extends GuiBetterSlot
{
    private GuiConfigEditor config;
    private Minecraft mc;
    private String[] message;
    private int _mouseX;
    private int _mouseY;
    private int selected = -1;
    public ResourceLocation resGUI = new ResourceLocation("/gui/gui.png");

    public GuiConfigScrollPanel(GuiConfigEditor controls, Minecraft mc, int startX, int startY, int height, int slotSize)
    {
        //super(mc, controls.width, controls.height, 16, (controls.height - 32) + 4, 25);
        super(mc, controls.width + 100, controls.height, startY + 8 + slotSize, height + slotSize - 2, slotSize);
        this.config = controls;
        this.mc = mc;
    }

    @Override
    protected int getSize()
    {
    	ModConfigData data = config.getData();
        return data.configData.size();
    }

    @Override
    protected void elementClicked(int i, boolean flag)
    {
    	//HostileWorlds.dbg("wew: " + flag);
        if (!flag)
        {
        	selected = i;
        	KeyBinding.resetKeyBindingArrayAndHash();
        	/*if (selected == -1)
            {
                selected = i;
                
                //config.configData.get(selected).editBox.textboxKeyTyped(c, i);
            }
            else
            {
                //options.setKeyBinding(selected, -100);
                selected = -1;
                KeyBinding.resetKeyBindingArrayAndHash();
            }*/
        }
    }
    
    protected void mouseClicked(int par1, int par2, int eventButton)
    {
    	boolean anyHasFocus = false;
    	for (int i = 0; i < config.getData().configData.size(); i++) {
    		
    		try {
	    		config.getData().configData.get(i).editBox.mouseClicked(par1, par2, eventButton);
	    		if (config.getData().configData.get(i).editBox.isFocused()) {
	    			anyHasFocus = true;
	    		}
    		} catch (Exception ex) {
    			//mouseClicked NPE'd on me once, NEVER AGAIN!
                //silence it
    			//ex.printStackTrace();
    		}
    		/*if (check && !config.configData.get(i).editBox.isFocused()) {
    			String str1 = config.configData.get(i).editBox.text;
    			String str2 = config.configData.get(i).value.toString();
    			if (!config.configData.get(i).editBox.text.equals(config.configData.get(i).value.toString())) {
    				config.configData.get(i).markForUpdate = true;
    			}
    		}*/
    				
    		//}
    	}
    	if (!anyHasFocus) {
    		//selected = -1;
    	}
    	/*if (selected != -1)
        {
    		config.configData.get(selected).editBox.mouseClicked(par1, par2, par3);
        }*/
    }

    @Override
    protected boolean isSelected(int i)
    {
        return false;
    }

    @Override
    protected void drawBackground() {}
    
    @Override
    protected void drawContainerBackground(Tessellator tess) {
    	/*this.mc.renderEngine.bindTexture(BACKGROUND_IMAGE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float height = 32.0F;
        
        int startX = (this.width - config.xSize) / 2;
        int startY = (this.height - config.ySize) / 2;
        int left = startX - 50 + 3;
        int right = left + config.xSize - 6;
        
        tess.startDrawingQuads();
        tess.setColorOpaque_I(2105376);
        tess.addVertexWithUV((double)left,  (double)bottom, 0.0D, (double)(left  / height), (double)((bottom + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)right, (double)bottom, 0.0D, (double)(right / height), (double)((bottom + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)right, (double)top,    0.0D, (double)(right / height), (double)((top    + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)left,  (double)top,    0.0D, (double)(left  / height), (double)((top    + (int)amountScrolled) / height));
        tess.draw();*/
    }

    @Override
    public void drawScreen(int mX, int mY, float f)
    {
        _mouseX = mX;
        _mouseY = mY;

        if (selected != -1 && !Mouse.isButtonDown(0) && Mouse.getDWheel() == 0)
        {
            if (Mouse.next() && Mouse.getEventButtonState())
            {
                //System.out.println(Mouse.getEventButton());
                //options.setKeyBinding(selected, -100 + Mouse.getEventButton());
                selected = -1;
                KeyBinding.resetKeyBindingArrayAndHash();
            }
        }

        try {
        	super.drawScreen(mX, mY, f);
        } catch (Exception ex) {
            //eat error until harmless bug found
        	//ex.printStackTrace();
        	//ConfigMod.dbg("exception drawing screen elements");
        }
    }

    @Override
    protected void drawSlot(int index, int xPosition, int yPosition, int l, Tessellator tessellator)
    {
        int width = 70;
        int height = slotHeight;
        xPosition -= 20;
        boolean flag = _mouseX >= xPosition && _mouseY >= yPosition && _mouseX < xPosition + width && _mouseY < yPosition + height;
        int k = (flag ? 2 : 1);

        //mc.renderEngine.bindTexture("/gui/gui.png");
        //mc.getTextureManager().bindTexture(resGUI);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //config.drawTexturedModalRect(xPosition, yPosition, 0, 46 + k * 20, width / 2, height);
        //config.drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + k * 20, width / 2, height);

        List<ConfigEntryInfo> data = config.getData().configData;

        if (data.get(index) == null || data.get(index).editBox == null) return;

        int stringWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(data.get(index).name);
        config.drawString(mc.fontRenderer, data.get(index).name/*options.getKeyBindingDescription(index)*/, xPosition - stringWidth + 15/* + width + 4*/, yPosition + 3, 0xFFFFFFFF);

        boolean conflict = false;
        /*for (int x = 0; x < options.keyBindings.length; x++)
        {
            if (x != index && options.keyBindings[x].keyCode == options.keyBindings[index].keyCode)
            {
                conflict = true;
                break;
            }
        }*/
        
        String value = data.get(index).value.toString();
        int maxWidth = (config.xSize / 2) - 45;
        //int valWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(value);
        
        value = Minecraft.getMinecraft().fontRenderer.trimStringToWidth(value, maxWidth);

        String str = (conflict ? TextFormatting.RED : "") + value;//options.getOptionDisplayString(index);
        str = (index == selected ? TextFormatting.WHITE + "> " + TextFormatting.YELLOW + "??? " + TextFormatting.WHITE + "<" : str);
        //config.drawString(mc.fontRenderer, str, xPosition + 20/* + (width / 2)*/, yPosition + (height - 8) / 2, 0xFFFFFFFF);
        List<ConfigEntryInfo> configDataTest = data;

        data.get(index).editBox.xPos = xPosition + 20;
        data.get(index).editBox.yPos = yPosition/* + (height - 8) / 2*/;
        //config.configData.get(index).editBox.text = config.configData.get(index).value.toString();
        data.get(index).editBox.drawTextBox();
        
        int hover_x_min = xPosition - stringWidth + 15;
        int hover_y_min = yPosition;
        int hover_x_max = xPosition - 15;
        int hover_y_max = yPosition + height;

        boolean hover_string = _mouseX >= hover_x_min && _mouseY >= hover_y_min && _mouseX < hover_x_max && _mouseY < hover_y_max;
        String s = ConfigMod.getComment(config.getData().configID, data.get(index).name);

        // Draw a tooltip with the description associated with the config value
        if (hover_string && s != null) {
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            int l2 = Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
            int i2 = hover_x_min;
            int k2 = hover_y_min - 10;
            drawGradientRect(i2 - 3, k2 - 3, i2 + l2 + 3, k2 + 8 + 3, 0xc0000000, 0xc0000000);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(s, i2, k2, -1);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
    }
    
    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     */
    protected void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float f = (float)(par5 >> 24 & 255) / 255.0F;
        float f1 = (float)(par5 >> 16 & 255) / 255.0F;
        float f2 = (float)(par5 >> 8 & 255) / 255.0F;
        float f3 = (float)(par5 & 255) / 255.0F;
        float f4 = (float)(par6 >> 24 & 255) / 255.0F;
        float f5 = (float)(par6 >> 16 & 255) / 255.0F;
        float f6 = (float)(par6 >> 8 & 255) / 255.0F;
        float f7 = (float)(par6 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        
        /*tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex((double)par3, (double)par2, (double)0);
        tessellator.addVertex((double)par1, (double)par2, (double)0);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double)par1, (double)par4, (double)0);
        tessellator.addVertex((double)par3, (double)par4, (double)0);
        tessellator.draw();*/
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double)par3, (double)par2, (double)0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)par1, (double)par2, (double)0).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)par1, (double)par4, (double)0).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double)par3, (double)par4, (double)0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public boolean keyTyped(char c, int i)
    {
    	//System.out.println(i);
        if (selected != -1 && config.getData().configData.get(selected).editBox.isFocused()/* && i != 28*/)
        {
        	config.getData().configData.get(selected).editBox.textboxKeyTyped(c, i);
        	if (i == 28) {
        		selected = -1;
        		return true;
        	}
        	//config.configData.get(selected).value = config.configData.get(selected).editBox.text;
            //options.setKeyBinding(selected, i);
            //selected = -1;
            //KeyBinding.resetKeyBindingArrayAndHash();
            return false;
        } else {
        	
        }
        return true;
    }
}
