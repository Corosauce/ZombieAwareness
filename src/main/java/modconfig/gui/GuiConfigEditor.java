package modconfig.gui;

import java.util.Iterator;

import CoroUtil.forge.CULog;
import modconfig.ConfigEntryInfo;
import modconfig.ConfigMod;
import modconfig.ModConfigData;
import modconfig.forge.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiConfigEditor extends GuiScreen
{
    /*private int updateCounter2 = 0;
    private int updateCounter = 0;
    private int inventoryRows = 0;*/
    
    public int ySize;
    public int xSize;
    public int xOffset;
    
    public static int G_RESET = 0;
    public static int G_SAVE = 1;
    public static int G_MODNEXT = 2;
    public static int G_MODPREV = 3;
    public static int G_CONFIGMODE = 4;
    public static int G_CLOSE = 9;
    
    //config stuff
    /*public List<String> configString = new ArrayList<String>();
    public List<GuiTextField> configField = new ArrayList<GuiTextField>();*/
    
    public static int curIndex = 0;
    
    public GuiConfigScrollPanel scrollPane;
    
    public static boolean clientMode = false;
    
    public ResourceLocation resGUI = new ResourceLocation("modconfig:textures/gui/gui512.png");
    
    public GuiConfigEditor() {
    	super();
    	mc = Minecraft.getMinecraft();
    }
    
    //Change this to using client synced data when that part is done
    public ModConfigData getConfigData(String modID) {
    	ModConfigData data = ConfigMod.configLookup.get(modID);
    	
    	return data;
    }
    
    public String getCategory() {
    	if (ConfigMod.liveEditConfigs.size() <= 0) return "<NULL>";
    	return ConfigMod.liveEditConfigs.get(curIndex).configID;
    	//return "weather";
    }
    
    public ModConfigData getData() {
    	return getConfigData(getCategory());
    }
    
    @Override
    public void updateScreen()
    {
    	try {
	    	updateStates();
	        
	    	if (ConfigMod.liveEditConfigs.size() > 0) {
		    	for (int i = 0; i < getData().configData.size(); i++) {
		    		if (getData().configData.get(i).editBox != null && getData().configData.get(i).editBox.isFocused()) {
		    			getData().configData.get(i).editBox.updateCursorCounter();
		    		}
		    	}
	    	}
    	} catch (Exception ex) {
            //eat error until harmless bug found
    		//ex.printStackTrace();
    	}
        super.updateScreen();
        //++this.updateCounter;
    }

    @Override
    public void drawScreen(int var1, int var2, float var3)
    {
        //this.drawDefaultBackground();

        try {
            int startX = (this.width - this.xSize) / 2;
            int startY = (this.height - this.ySize) / 2;

            drawGuiContainerBackgroundLayer(0, 0, 0);

            scrollPane.drawScreen(var1, var2, var3);

            drawGuiContainerClippingScrollLayer(0, 0, 0);

            this.drawString(this.fontRenderer, "Config for: " + getCategory(), startX + 10, startY + 10, 16777215);
            this.drawString(this.fontRenderer, (curIndex + 1) + "/" + ConfigMod.liveEditConfigs.size(), startX + xSize - 60, startY + 10, 16777215);
        } catch (Exception ex) {
            //ignore
        }
    	
    	//this.textboxWorldName.drawTextBox();
        super.drawScreen(var1, var2, var3);
    }
    
    protected void drawGuiContainerClippingScrollLayer(float var1, int var2, int var3) {
    	
    	int startX = 4 + (this.width - this.xSize) / 2;
        int startY = 4 + (this.height - this.ySize) / 2;
        
        int x1 = xSize - 8;
        
        int y1 = startY;
        int y2 = startY + 23;
    	
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    	Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
    	/*tessellator.startDrawingQuads();
    	tessellator.setColorRGBA_I(13027014, 255);
        tessellator.addVertexWithUV((double)startX + x1, (double)y1, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)startX, (double)y1, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV((double)startX, (double)y2, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)startX + x1, (double)y2, 0.0D, 0.0D, 0.0D);*/
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos((double)startX + x1, (double)y1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)startX, (double)y1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)startX, (double)y2, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        vertexbuffer.pos((double)startX + x1, (double)y2, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        
        y1 = startY + ySize - 14 - 23;
        y2 = startY + ySize - 14;
        
       /* tessellator.addVertexWithUV((double)startX + x1, (double)y1, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)startX, (double)y1, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV((double)startX, (double)y2, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)startX + x1, (double)y2, 0.0D, 0.0D, 0.0D);
        tessellator.draw();*/
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos((double)startX + x1, (double)y1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)startX, (double)y1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)startX, (double)y2, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        vertexbuffer.pos((double)startX + x1, (double)y2, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
    	this.xSize = (int)(256 * 1.0);
    	this.ySize = (int)(256 * 1.0);
    	
    	xSize = 372;
    	ySize = 250;
    	
    	//inventoryRows = 5;
    	
        //int var4 = this.mc.renderEngine.getTexture("/mods/ZombieCraft/textures/textures/menus/editorCP.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //this.mc.renderEngine.bindTexture("/mods/ModConfig/textures/gui/gui512.png");
        mc.getTextureManager().bindTexture(resGUI);
        //this.mc.renderEngine.bindTexture("/mods/HostileWorlds/textures/gui/demo_bg.png");
        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;
        
        
        
        //this.drawTexturedModalRect(var5 + xOffset, var6, 0, 0, this.xSize, ySize);
        //this.drawTexturedModalRect(-2, var6, 0, 0, this.xSize-76, ySize);
        this.drawTexturedModalRect(startX, startY/* + 1 * 18 + 17*/, 0, 0, 512, 512);
        //this.drawCenteredString(this.fontRenderer, "ZC Menu", ((this.width - this.xSize) / 2) + xOffset + 50, startY - 10, 16777215);
        
        
        /*ModConfigData data = getConfigData(getCategory());
        
        if (data != null) {
        	
        	this.drawString(this.fontRenderer, "Config for: " + data.configInstance.getCategory(), startX + 10, startY - 10, 16777215);
        	
        	Iterator it = data.valsInteger.entrySet().iterator();
        	int pos = 0;
    	    while (it.hasNext()) {
    	        Map.Entry pairs = (Map.Entry)it.next();
    	        String name = (String)pairs.getKey();
    	        Object val = pairs.getValue();
    	        int wat = this.fontRenderer.getStringWidth(name);
    	        this.drawString(this.fontRenderer, name, startX + 10, startY + 10 + (pos * 12), 16777215);
    	        this.drawString(this.fontRenderer, val.toString(), startX + 10 + xSize / 2, startY + 10 + (pos * 12), 16777215);
    	        
    	        pos++;
    	    }
        } else {
        	HostileWorlds.dbg("error: cant find config data for gui");
        }*/
        //for ()
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }
    
    //this part needs to check against the client data, not the reflection lookup on the config class
    public void updateChangedValues() {
    	for (int i = 0; i < getData().configData.size(); i++) {
    		ConfigEntryInfo info = getData().configData.get(i);
    		
    		if (!getData().configData.get(i).editBox.text.equals(getData().configData.get(i).value.toString())/*info.markForUpdate*//*!realVal.toString().equals(info.value.toString())*/) {
    			
    			if (!clientMode) {
    				//this.mc.player.sendChatMessage("/config" + " set " + getCategory() + " " + getData().configData.get(i).name + " " + getData().configData.get(i).editBox.text);
    				ConfigMod.eventChannel.sendToServer(PacketHelper.getModConfigPacketForClientToServer("set " + getCategory() + " " + getData().configData.get(i).name + " " + getData().configData.get(i).editBox.text));
    			} else {
    				if (ConfigMod.updateField(getCategory(), getData().configData.get(i).name, getData().configData.get(i).editBox.text)) {
    					CULog.dbg("Updated config settings in client mode");
    				}
    			}
    		}
    	}
    }

    @Override
    public void initGui()
    {
    	xSize = 372;
    	ySize = 250;
    	int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;
        
        ScaledResolution var8 = new ScaledResolution(mc);
        int scaledWidth = var8.getScaledWidth();
        int scaledHeight = var8.getScaledHeight();
        
    	scrollPane = new GuiConfigScrollPanel(this, mc, startX, startY, startY + ySize - 50/*ySize*/, 20);
    	scrollPane.registerScrollButtons(null, 7, 8);
    	
    	if (!clientMode) {
    		this.mc.player.sendChatMessage("/config update " + getCategory());
    	} else {
    		ConfigMod.populateData(getCategory());
    	}
    	
        int buttonWidth = 90;
        int buttonHeight = 20;
        int paddingSize = 8;
        int navWidth = 20;
        int navHeight = 20;
        
    	//xOffset = (int)(this.width / 4 * 1.92);
    	
        //this.updateCounter2 = 0;
        this.buttonList.clear();
        //byte var1 = -16;
        
        //System.out.println(width);
    	//System.out.println(xSize);
        
        /*int startX = ((this.width - this.xSize) / 2) + xOffset + 6;
        int startY = (this.height - this.ySize) / 2 + 73;*/
        
        
        //int div = 22;
        
        /*String original = new String("\u00A7");
        String roundTrip = "";
        try {
        	roundTrip = new String(original.getBytes("UTF8"), "UTF8");
        } catch (Exception ex) {
        	
        }*/
        
        //this.buttonList.add(new GuiButton(G_EDITMODE, startX, startY + 0 + var1, 90, 20, (/*ZCGame.instance().mapMan.editMode*/true ? "\u00A7" + '4' : "") + "Edit Mode"));
        
        //this.buttonList.add(new GuiButton(G_RESET, startX + xSize - (buttonWidth + paddingSize) * 3, startY + ySize - buttonHeight - paddingSize, buttonWidth, buttonHeight, "Reset"));
        this.buttonList.add(new GuiButton(G_MODPREV, startX + xSize - (navWidth + 22) * 2, startY + paddingSize - 3, navWidth, navHeight, "<"));
        this.buttonList.add(new GuiButton(G_MODNEXT, startX + xSize - (navWidth + paddingSize) * 1, startY + paddingSize - 3, navWidth, navHeight, ">"));
        this.buttonList.add(new GuiButton(G_SAVE, startX + xSize - (buttonWidth + paddingSize) * 2, startY + ySize - buttonHeight - paddingSize, buttonWidth, buttonHeight, "Save"));
        this.buttonList.add(new GuiButton(G_CLOSE, startX + xSize - (buttonWidth + paddingSize) * 1, startY + ySize - buttonHeight - paddingSize, buttonWidth, buttonHeight, "Close"));
        
        if (mc.isSingleplayer()) {
        	clientMode = false;
        } else {
        	this.buttonList.add(new GuiButton(G_CONFIGMODE, startX + xSize - (buttonWidth + paddingSize) * 3, startY + ySize - buttonHeight - paddingSize, buttonWidth, buttonHeight, "Mode: " + (clientMode ? "Local" : "Remote")));
        }
        
        
        //int startX2 = 4;//((this.width - this.xSize) / 2) - (xOffset/4*1);
        //int startY2 = (this.height - this.ySize) / 2 + 23;
        
        //this.textboxWorldName = new GuiTextField(this.fontRenderer, startX + xSize - buttonWidth - paddingSize, startY + 10 + paddingSize, buttonWidth, buttonHeight);
        //this.textboxWorldName.setFocused(true);
        //this.textboxWorldName.setText("derp");
        
    }
    
    @Override
    protected void keyTyped(char par1, int par2)
    {
    	try {
    		if (scrollPane.keyTyped(par1, par2))
            {
        		super.keyTyped(par1, par2);
        		

                if (par1 == 13)
                {
                	//save on enter?, nah lets not
                    //this.actionPerformed((GuiButton)this.buttonList.get(7));
                }

                //lock save button if no name
                //((GuiButton)this.buttonList.get(7)).enabled = this.textboxWorldName.getText().length() > 0;
                //this.makeUseableName();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
    	try {
            super.mouseClicked(par1, par2, par3);
            scrollPane.mouseClicked(par1, par2, par3);
            
            ///*if (ZCGame.instance().mapMan.editMode) */this.textboxWorldName.mouseClicked(par1, par2, par3);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    protected void actionPerformed(GuiButton var1)
    {
        if (var1.id == G_RESET) {
            //ZCClientTicks.iMan.toggleEditMode();
            //ZCGame.instance().mapMan.editMode = !ZCGame.instance().mapMan.editMode; //this is here for client side prediction, the server is already sending in a packet for this
        } else if (var1.id == G_MODPREV) {
        	curIndex--;
        	if (curIndex < 0) curIndex = ConfigMod.liveEditConfigs.size() - 1;
        	//initGui();
        } else if (var1.id == G_MODNEXT) {
        	curIndex++;
        	if (curIndex >= ConfigMod.liveEditConfigs.size()) curIndex = 0;
        	//initGui();
        } else if (var1.id == G_SAVE) {
        	updateChangedValues();
        } else if (var1.id == G_CONFIGMODE) {
        	clientMode = !clientMode;
        } else if (var1.id == G_CLOSE) {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
        initGui();
    }
    
    public void updateStates() {
    	GuiButton var2 = null;
    	
    	//if (!this.textboxWorldName.isFocused()) {
    		//HostileWorlds.dbg("this code needs to get the value to auto updated from data");
    		//this.textboxWorldName.setText(ZCGame.instance().mapMan.curLevel);
    	//}

        for (Iterator var1 = this.buttonList.iterator(); var1.hasNext();)
        {
            var2 = (GuiButton)var1.next();
            /*if (var2 instanceof GuiButtonZC) {
        		var2.enabled = false;
        		if (((GuiButtonZC) var2).id == this.G_TOOLMODE_LEVEL && ZCGame.instance().mapMan.editToolMode == 1) {
        			var2.enabled = true;
        		} else if (((GuiButtonZC) var2).id == this.G_TOOLMODE_LINK && ZCGame.instance().mapMan.editToolMode == 0) {
        			var2.enabled = true;
        		} else if (((GuiButtonZC) var2).id == this.G_TOOLMODE_SPAWN && ZCGame.instance().mapMan.editToolMode == 2) {
        			var2.enabled = true;
        		}
        	} else {
        		if (var2.id == this.G_EDITMODE) {
        			var2.displayString = (ZCGame.instance().mapMan.editMode ? "\u00A7" + '4' : "") + "Edit Mode";
        		} else if (var2.id == this.G_TOGGLEGAME) {
        			var2.displayString = (!ZCGame.instance().gameActive ? "Start Game" : "\u00A72Stop Game");
        		} else if (var2.id == this.G_DOORNOCLIP) {
        			var2.displayString = (!ZCGame.instance().mapMan.doorNoClip ? "Door No-Clip" : "\u00A74Door No-Clip");
        		}
        	}*/
        }
    }
    
    @Override
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F / 2F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f)).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f)).endVertex();
        tessellator.draw();
    }
}
