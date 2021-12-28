package modconfig.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class GuiBetterSlot
{
    public final Minecraft mc;

    /**
     * The width of the GuiScreen. Affects the container rendering, but not the overlays.
     */
    public int width;

    /**
     * The height of the GuiScreen. Affects the container rendering, but not the overlays or the scrolling.
     */
    public int height;

    /** The top of the slot container. Affects the overlays and scrolling. */
    public int top;

    /** The bottom of the slot container. Affects the overlays and scrolling. */
    public int bottom;
    public int right;
    public int left;

    /** The height of a slot. */
    public final int slotHeight;

    /** button id of the button used to scroll up */
    public int scrollUpButtonID;

    /** the buttonID of the button used to scroll down */
    public int scrollDownButtonID;

    /** X axis position of the mouse */
    public int mouseX;

    /** Y axis position of the mouse */
    public int mouseY;

    /** where the mouse was in the window when you first clicked to scroll */
    public float initialClickY = -2.0F;

    /**
     * what to multiply the amount you moved your mouse by(used for slowing down scrolling when over the items and no on
     * scroll bar)
     */
    public float scrollMultiplier;

    /** how far down this slot has been scrolled */
    public float amountScrolled;

    /** the element in the list that was selected */
    public int selectedElement = -1;

    /** the time when this button was last clicked. */
    public long lastClicked = 0L;

    /** true if a selected element in this gui will show an outline box */
    public boolean showSelectionBox = true;
    public boolean field_77243_s;
    public int field_77242_t;

    //public String BACKGROUND_IMAGE = "/gui/background.png";
    public ResourceLocation resBG = new ResourceLocation("/gui/background.png");

    public GuiBetterSlot(Minecraft par1Minecraft, int par2, int par3, int par4, int par5, int par6)
    {
        this.mc = par1Minecraft;
        this.width = par2;
        this.height = par3;
        this.top = par4;
        this.bottom = par5;
        this.slotHeight = par6;
        this.left = 0;
        this.right = par2;
    }

    public void func_77207_a(int par1, int par2, int par3, int par4)
    {
        this.width = par1;
        this.height = par2;
        this.top = par3;
        this.bottom = par4;
        this.left = 0;
        this.right = par1;
    }

    public void setShowSelectionBox(boolean par1)
    {
        this.showSelectionBox = par1;
    }

    protected void func_77223_a(boolean par1, int par2)
    {
        this.field_77243_s = par1;
        this.field_77242_t = par2;

        if (!par1)
        {
            this.field_77242_t = 0;
        }
    }

    /**
     * Gets the size of the current slot list.
     */
    protected abstract int getSize();

    /**
     * the element in the slot that was clicked, boolean for wether it was double clicked or not
     */
    protected abstract void elementClicked(int i, boolean flag);

    /**
     * returns true if the element passed in is currently selected
     */
    protected abstract boolean isSelected(int i);

    /**
     * return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.field_77242_t;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int i, int j, int k, int l, Tessellator tessellator);

    protected void func_77222_a(int par1, int par2, Tessellator par3Tessellator) {}

    protected void func_77224_a(int par1, int par2) {}

    protected void func_77215_b(int par1, int par2) {}

    public int func_77210_c(int par1, int par2)
    {
        int k = this.width / 2 - 110;
        int l = this.width / 2 + 110;
        int i1 = par2 - this.top - this.field_77242_t + (int)this.amountScrolled - 4;
        int j1 = i1 / this.slotHeight;
        return par1 >= k && par1 <= l && j1 >= 0 && i1 >= 0 && j1 < this.getSize() ? j1 : -1;
    }

    /**
     * Registers the IDs that can be used for the scrollbar's buttons.
     */
    public void registerScrollButtons(List par1List, int par2, int par3)
    {
        this.scrollUpButtonID = par2;
        this.scrollDownButtonID = par3;
    }

    /**
     * stop the thing from scrolling out of bounds
     */
    private void bindAmountScrolled()
    {
        int i = this.func_77209_d();

        if (i < 0)
        {
            i /= 2;
        }

        if (this.amountScrolled < 0.0F)
        {
            this.amountScrolled = 0.0F;
        }

        if (this.amountScrolled > (float)i)
        {
            this.amountScrolled = (float)i;
        }
    }

    public int func_77209_d()
    {
        return this.getContentHeight() - (this.bottom - this.top - 4);
    }

    public void func_77208_b(int par1)
    {
        this.amountScrolled += (float)par1;
        this.bindAmountScrolled();
        this.initialClickY = -2.0F;
    }

    public void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id == this.scrollUpButtonID)
            {
                this.amountScrolled -= (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2.0F;
                this.bindAmountScrolled();
            }
            else if (par1GuiButton.id == this.scrollDownButtonID)
            {
                this.amountScrolled += (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2.0F;
                this.bindAmountScrolled();
            }
        }
    }

    /**
     * draws the slot to the screen, pass in mouse's current x and y and partial ticks
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.mouseX = par1;
        this.mouseY = par2;
        this.drawBackground();
        int k = this.getSize();
        
        //System.out.println("SIZE: " + k);
        
        int l = this.getScrollBarX();
        int i1 = l + 6;
        int j1;
        int k1;
        int l1;
        int i2;
        int j2;

        if (Mouse.isButtonDown(0))
        {
            if (this.initialClickY == -1.0F)
            {
                boolean flag = true;

                if (par2 >= this.top && par2 <= this.bottom)
                {
                    int k2 = this.width / 2 - 110;
                    j1 = this.width / 2 + 110;
                    k1 = par2 - this.top - this.field_77242_t + (int)this.amountScrolled - 4;
                    l1 = k1 / this.slotHeight;

                    if (par1 >= k2 && par1 <= j1 && l1 >= 0 && k1 >= 0 && l1 < k)
                    {
                        boolean flag1 = l1 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                        this.elementClicked(l1, flag1);
                        this.selectedElement = l1;
                        this.lastClicked = Minecraft.getSystemTime();
                    }
                    else if (par1 >= k2 && par1 <= j1 && k1 < 0)
                    {
                        this.func_77224_a(par1 - k2, par2 - this.top + (int)this.amountScrolled - 4);
                        flag = false;
                    }

                    if (par1 >= l && par1 <= i1)
                    {
                        this.scrollMultiplier = -1.0F;
                        j2 = this.func_77209_d();

                        if (j2 < 1)
                        {
                            j2 = 1;
                        }

                        i2 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());

                        if (i2 < 32)
                        {
                            i2 = 32;
                        }

                        if (i2 > this.bottom - this.top - 8)
                        {
                            i2 = this.bottom - this.top - 8;
                        }

                        this.scrollMultiplier /= (float)(this.bottom - this.top - i2) / (float)j2;
                    }
                    else
                    {
                        this.scrollMultiplier = 1.0F;
                    }

                    if (flag)
                    {
                        this.initialClickY = (float)par2;
                    }
                    else
                    {
                        this.initialClickY = -2.0F;
                    }
                }
                else
                {
                    this.initialClickY = -2.0F;
                }
            }
            else if (this.initialClickY >= 0.0F)
            {
                this.amountScrolled -= ((float)par2 - this.initialClickY) * this.scrollMultiplier;
                this.initialClickY = (float)par2;
            }
        }
        else
        {
            while (!this.mc.gameSettings.touchscreen && Mouse.next())
            {
                int l2 = Mouse.getEventDWheel();

                if (l2 != 0)
                {
                    if (l2 > 0)
                    {
                        l2 = -1;
                    }
                    else if (l2 < 0)
                    {
                        l2 = 1;
                    }

                    this.amountScrolled += (float)(l2 * this.slotHeight / 2);
                }
            }

            this.initialClickY = -1.0F;
        }

        this.bindAmountScrolled();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        drawContainerBackground(tessellator);
        j1 = this.width / 2/* - 92*/ - 16;
        k1 = this.top + 4 - (int)this.amountScrolled;

        //System.out.println(height + " - " + bottom);
        
        if (this.field_77243_s)
        {
            this.func_77222_a(j1, k1, tessellator);
        }

        int i3;

        for (l1 = 0; l1 < k; ++l1)
        {
            j2 = k1 + l1 * this.slotHeight + this.field_77242_t;
            i2 = this.slotHeight - 4;

            if (j2 <= this.bottom && j2 + i2 >= this.top)
            {
                if (this.showSelectionBox && this.isSelected(l1))
                {
                    i3 = this.width / 2 - 110;
                    int j3 = this.width / 2 + 110;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    /*tessellator.startDrawingQuads();
                    tessellator.setColorOpaque_I(8421504);
                    tessellator.addVertexWithUV((double)i3, (double)(j2 + i2 + 2), 0.0D, 0.0D, 1.0D);
                    tessellator.addVertexWithUV((double)j3, (double)(j2 + i2 + 2), 0.0D, 1.0D, 1.0D);
                    tessellator.addVertexWithUV((double)j3, (double)(j2 - 2), 0.0D, 1.0D, 0.0D);
                    tessellator.addVertexWithUV((double)i3, (double)(j2 - 2), 0.0D, 0.0D, 0.0D);
                    tessellator.setColorOpaque_I(0);
                    tessellator.addVertexWithUV((double)(i3 + 1), (double)(j2 + i2 + 1), 0.0D, 0.0D, 1.0D);
                    tessellator.addVertexWithUV((double)(j3 - 1), (double)(j2 + i2 + 1), 0.0D, 1.0D, 1.0D);
                    tessellator.addVertexWithUV((double)(j3 - 1), (double)(j2 - 1), 0.0D, 1.0D, 0.0D);
                    tessellator.addVertexWithUV((double)(i3 + 1), (double)(j2 - 1), 0.0D, 0.0D, 0.0D);
                    tessellator.draw();*/
                    
                    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    vertexbuffer.pos((double)i3, (double)(j2 + i2 + 2), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
                    vertexbuffer.pos((double)j3, (double)(j2 + i2 + 2), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
                    vertexbuffer.pos((double)j3, (double)(j2 - 2), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    vertexbuffer.pos((double)i3, (double)(j2 - 2), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    tessellator.draw();
                    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    vertexbuffer.pos((double)(i3 + 1), (double)(j2 + i2 + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    vertexbuffer.pos((double)(j3 - 1), (double)(j2 + i2 + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    vertexbuffer.pos((double)(j3 - 1), (double)(j2 - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
                    vertexbuffer.pos((double)(i3 + 1), (double)(j2 - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
                    tessellator.draw();
                    
                    
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                this.drawSlot(l1, j1, j2, i2, tessellator);
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        byte b0 = 4;
        //this.overlayBackground(0, this.top, 255, 255);
        //this.overlayBackground(this.bottom, this.height, 255, 255);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        /*tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double)this.left, (double)(this.top + b0), 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)this.right, (double)(this.top + b0), 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double)this.right, (double)this.top, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.left, (double)this.top, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(0, 255);
        tessellator.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, 1.0D, 1.0D);
        tessellator.setColorRGBA_I(0, 0);
        tessellator.addVertexWithUV((double)this.right, (double)(this.bottom - b0), 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV((double)this.left, (double)(this.bottom - b0), 0.0D, 0.0D, 0.0D);
        tessellator.draw();*/
        j2 = this.func_77209_d();

        if (j2 > 0)
        {
            i2 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();

            if (i2 < 32)
            {
                i2 = 32;
            }

            if (i2 > this.bottom - this.top - 8)
            {
                i2 = this.bottom - this.top - 8;
            }

            i3 = (int)this.amountScrolled * (this.bottom - this.top - i2) / j2 + this.top;

            if (i3 < this.top)
            {
                i3 = this.top;
            }

            /*tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(0, 255);
            tessellator.addVertexWithUV((double)l, (double)this.bottom, 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double)i1, (double)this.bottom, 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double)i1, (double)this.top, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double)l, (double)this.top, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(8421504, 255);
            tessellator.addVertexWithUV((double)l, (double)(i3 + i2), 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double)i1, (double)(i3 + i2), 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double)i1, (double)i3, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double)l, (double)i3, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(12632256, 255);
            tessellator.addVertexWithUV((double)l, (double)(i3 + i2 - 1), 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double)(i1 - 1), (double)(i3 + i2 - 1), 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double)(i1 - 1), (double)i3, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double)l, (double)i3, 0.0D, 0.0D, 0.0D);
            tessellator.draw();*/
            
            
            
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)l, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            vertexbuffer.pos((double)i1, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            vertexbuffer.pos((double)i1, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)l, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)l, (double)(i3 + i2), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)i1, (double)(i3 + i2), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)i1, (double)i3, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            vertexbuffer.pos((double)l, (double)i3, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)l, (double)(i3 + i2 - 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)(i1 - 1), (double)(i3 + i2 - 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            vertexbuffer.pos((double)(i1 - 1), (double)i3, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            vertexbuffer.pos((double)l, (double)i3, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
        }

        this.func_77215_b(par1, par2);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    protected int getScrollBarX()
    {
        return this.width / 2 + 124;
    }

    /**
     * Overlays the background to hide scrolled items
     */
    protected void overlayBackground(int par1, int par2, int par3, int par4)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        //this.mc.renderEngine.bindTexture(BACKGROUND_IMAGE);
        mc.getTextureManager().bindTexture(resBG);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        /*tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(4210752, par4);
        tessellator.addVertexWithUV(0.0D, (double)par2, 0.0D, 0.0D, (double)((float)par2 / f));
        tessellator.addVertexWithUV((double)this.width, (double)par2, 0.0D, (double)((float)this.width / f), (double)((float)par2 / f));
        tessellator.setColorRGBA_I(4210752, par3);
        tessellator.addVertexWithUV((double)this.width, (double)par1, 0.0D, (double)((float)this.width / f), (double)((float)par1 / f));
        tessellator.addVertexWithUV(0.0D, (double)par1, 0.0D, 0.0D, (double)((float)par1 / f));
        tessellator.draw();*/
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(0.0D, (double)par2, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)this.width, (double)par2, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)this.width, (double)par1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        vertexbuffer.pos(0.0D, (double)par1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }

    protected void drawContainerBackground(Tessellator tessellator)
    {
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        //this.mc.renderEngine.bindTexture(BACKGROUND_IMAGE);
        mc.getTextureManager().bindTexture(resBG);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float height = 32.0F;
        /*tess.startDrawingQuads();
        tess.setColorOpaque_I(2105376);
        tess.addVertexWithUV((double)left,  (double)bottom, 0.0D, (double)(left  / height), (double)((bottom + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)right, (double)bottom, 0.0D, (double)(right / height), (double)((bottom + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)right, (double)top,    0.0D, (double)(right / height), (double)((top    + (int)amountScrolled) / height));
        tess.addVertexWithUV((double)left,  (double)top,    0.0D, (double)(left  / height), (double)((top    + (int)amountScrolled) / height));
        tess.draw();*/
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos((double)left,  (double)bottom, 0.0D).tex((double)(left  / height), (double)((bottom + (int)amountScrolled) / height)).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, 0.0D).tex((double)(right / height), (double)((bottom + (int)amountScrolled) / height)).color(0, 0, 0, 255).endVertex();
        vertexbuffer.pos((double)right, (double)top, 0.0D).tex((double)(right / height), (double)((top    + (int)amountScrolled) / height)).color(0, 0, 0, 0).endVertex();
        vertexbuffer.pos((double)left,  (double)top, 0.0D).tex((double)(left  / height), (double)((top    + (int)amountScrolled) / height)).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }
}
