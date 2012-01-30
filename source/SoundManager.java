package net.minecraft.src;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.src.CodecMus;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.GameSettings;
import net.minecraft.src.MathHelper;
import net.minecraft.src.SoundPool;
import net.minecraft.src.SoundPoolEntry;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecIBXM;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {

   private static SoundSystem sndSystem;
   private SoundPool soundPoolSounds = new SoundPool();
   private SoundPool soundPoolStreaming = new SoundPool();
   private SoundPool soundPoolMusic = new SoundPool();
   private SoundPool cave = new SoundPool();
   private int latestSoundID = 0;
   private GameSettings options;
   private static boolean loaded = false;
   private Random rand = new Random();
   private Minecraft mc;
   private static final int MUSINTERVAL = 6000;
   private int ticksBeforeMusic;
   
   public long lastSndPosUpdate = 0L;
   public long soundUpdateCount = 0L;


   public SoundManager() {
      this.ticksBeforeMusic = this.rand.nextInt(6000);
   }

   public void loadSoundSettings(GameSettings var1) {
      this.soundPoolStreaming.isGetRandomSound = false;
      this.options = var1;
      if(!loaded && (var1 == null || var1.soundVolume != 0.0F || var1.musicVolume != 0.0F)) {
         this.tryToSetLibraryAndCodecs();
      }

      loadModAudio("minecraft/resources/mod/sound", this.soundPoolSounds);
      loadModAudio("minecraft/resources/mod/streaming", this.soundPoolStreaming);
      loadModAudio("minecraft/resources/mod/music", this.soundPoolMusic);
      loadModAudio("minecraft/resources/mod/cavemusic", this.cave);

      try {
         Field var2 = Minecraft.class.getDeclaredFields()[1];
         var2.setAccessible(true);
         this.mc = (Minecraft)var2.get((Object)null);
      } catch (Throwable var3) {
         ;
      }

   }

   private static void loadModAudio(String var0, SoundPool var1) {
      File var2 = Minecraft.getAppDir(var0);

      try {
         walkFolder(var2, var2, var1);
      } catch (IOException var4) {
         var4.printStackTrace();
      }

   }

   private static void walkFolder(File var0, File var1, SoundPool var2) throws IOException {
      if(var1.exists() || var1.mkdirs()) {
         File[] var3 = var1.listFiles();
         if(var3 != null && var3.length > 0) {
            for(int var4 = 0; var4 < var3.length; ++var4) {
               if(!var3[var4].getName().startsWith(".")) {
                  if(var3[var4].isDirectory()) {
                     walkFolder(var0, var3[var4], var2);
                  } else if(var3[var4].isFile()) {
                     String var5 = var3[var4].getPath().substring(var0.getPath().length() + 1).replace('\\', '/');
                     var2.addSound(var5, var3[var4]);
                  }
               }
            }
         }
      }

   }

   private void tryToSetLibraryAndCodecs() {
      try {
         float var1 = this.options.soundVolume;
         float var2 = this.options.musicVolume;
         this.options.soundVolume = 0.0F;
         this.options.musicVolume = 0.0F;
         this.options.saveOptions();
         SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
         SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
         SoundSystemConfig.setCodec("mus", CodecMus.class);
         SoundSystemConfig.setCodec("wav", CodecWav.class);

         try {
            if(Class.forName("paulscode.sound.codecs.CodecIBXM") != null) {
               SoundSystemConfig.setCodec("xm", CodecIBXM.class);
               SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
               SoundSystemConfig.setCodec("mod", CodecIBXM.class);
            }
         } catch (ClassNotFoundException var4) {
            ;
         }

         sndSystem = new SoundSystem();
         this.options.soundVolume = var1;
         this.options.musicVolume = var2;
         this.options.saveOptions();
      } catch (Throwable var5) {
         var5.printStackTrace();
         System.err.println("error linking with the LibraryJavaSound plug-in");
      }

      loaded = true;
   }

   public void onSoundOptionsChanged() {
      if(!loaded && (this.options.soundVolume != 0.0F || this.options.musicVolume != 0.0F)) {
         this.tryToSetLibraryAndCodecs();
      }

      if(loaded) {
         if(this.options.musicVolume == 0.0F) {
            sndSystem.stop("BgMusic");
         } else {
            sndSystem.setVolume("BgMusic", this.options.musicVolume);
         }
      }

   }

   public void closeMinecraft() {
      if(loaded) {
         sndSystem.cleanup();
      }

   }

   public void addSound(String var1, File var2) {
      this.soundPoolSounds.addSound(var1, var2);
   }

   public void addStreaming(String var1, File var2) {
      this.soundPoolStreaming.addSound(var1, var2);
   }

   public void addMusic(String var1, File var2) {
      this.soundPoolMusic.addSound(var1, var2);
   }

   public void playRandomMusicIfReady() {
      if(loaded && this.options.musicVolume != 0.0F) {
         if(!sndSystem.playing("BgMusic") && !sndSystem.playing("streaming")) {
            if(this.ticksBeforeMusic > 0) {
               --this.ticksBeforeMusic;
               return;
            }

            SoundPoolEntry var1 = this.soundPoolMusic.getRandomSound();
            if(this.mc != null && this.mc.thePlayer != null && !this.mc.thePlayer.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.mc.thePlayer.posX), MathHelper.floor_double(this.mc.thePlayer.posY), MathHelper.floor_double(this.mc.thePlayer.posZ))) {
               var1 = this.cave.getRandomSound();
            } else {
               var1 = this.soundPoolMusic.getRandomSound();
            }

            if(var1 != null) {
               this.ticksBeforeMusic = this.rand.nextInt(6000) + 6000;
               sndSystem.backgroundMusic("BgMusic", var1.soundUrl, var1.soundName, false);
               sndSystem.setVolume("BgMusic", this.options.musicVolume);
               sndSystem.play("BgMusic");
            }
         }

      }
   }

   public void func_338_a(EntityLiving var1, float var2) {
	   if (System.currentTimeMillis() < this.lastSndPosUpdate) {
           return;
       }
       this.lastSndPosUpdate = System.currentTimeMillis() + 1000L;
       
      if(loaded && this.options.soundVolume != 0.0F) {
         if(var1 != null) {
            float var3 = var1.prevRotationYaw + (var1.rotationYaw - var1.prevRotationYaw) * var2;
            double var4 = var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var2;
            double var6 = var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var2;
            double var8 = var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var2;
            float var10 = MathHelper.cos(-var3 * 0.01745329F - 3.141593F);
            float var11 = MathHelper.sin(-var3 * 0.01745329F - 3.141593F);
            float var12 = -var11;
            float var13 = 0.0F;
            float var14 = -var10;
            float var15 = 0.0F;
            float var16 = 1.0F;
            float var17 = 0.0F;
            sndSystem.setListenerPosition((float)var4, (float)var6, (float)var8);
            sndSystem.setListenerOrientation(var12, var13, var14, var15, var16, var17);
         }
      }
   }

   public void playStreaming(String var1, float var2, float var3, float var4, float var5, float var6) {
      if(loaded && this.options.soundVolume != 0.0F) {
         String var7 = "streaming";
         if(sndSystem.playing("streaming")) {
            sndSystem.stop("streaming");
         }

         if(var1 != null) {
            SoundPoolEntry var8 = this.soundPoolStreaming.getRandomSoundFromSoundPool(var1);
            if(var8 != null && var5 > 0.0F) {
               if(sndSystem.playing("BgMusic")) {
                  sndSystem.stop("BgMusic");
               }

               float var9 = 16.0F;
               sndSystem.newStreamingSource(true, var7, var8.soundUrl, var8.soundName, false, var2, var3, var4, 2, var9 * 4.0F);
               sndSystem.setVolume(var7, 0.5F * this.options.soundVolume);
               sndSystem.play(var7);
            }

         }
      }
   }

   public void playSound(String var1, float var2, float var3, float var4, float var5, float var6) {
	   try {
           mod_PathingActivated.spawnSoundTrace(var1, var2, var3, var4, var5, var6);
       } catch (Exception ex) {
       }
      if(loaded && this.options.soundVolume != 0.0F) {
         SoundPoolEntry var7 = this.soundPoolSounds.getRandomSoundFromSoundPool(var1);
         if(var7 != null && var5 > 0.0F) {
            this.latestSoundID = (this.latestSoundID + 1) % 256;
            String var8 = "sound_" + this.latestSoundID;
            float var9 = 16.0F;
            if(var5 > 1.0F) {
               var9 *= var5;
            }

            sndSystem.newSource(var5 > 1.0F, var8, var7.soundUrl, var7.soundName, false, var2, var3, var4, 2, var9);
            sndSystem.setPitch(var8, var6);
            if(var5 > 1.0F) {
               var5 = 1.0F;
            }

            sndSystem.setVolume(var8, var5 * this.options.soundVolume);
            sndSystem.play(var8);
         }

      }
   }

   public void playSoundFX(String var1, float var2, float var3) {
      if(loaded && this.options.soundVolume != 0.0F) {
         SoundPoolEntry var4 = this.soundPoolSounds.getRandomSoundFromSoundPool(var1);
         if(var4 != null) {
            this.latestSoundID = (this.latestSoundID + 1) % 256;
            String var5 = "sound_" + this.latestSoundID;
            sndSystem.newSource(false, var5, var4.soundUrl, var4.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
            if(var2 > 1.0F) {
               var2 = 1.0F;
            }

            var2 *= 0.25F;
            sndSystem.setPitch(var5, var3);
            sndSystem.setVolume(var5, var2 * this.options.soundVolume);
            sndSystem.play(var5);
         }

      }
   }
}
