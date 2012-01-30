package net.minecraft.src;

import java.util.HashMap;
import org.lwjgl.input.Keyboard;

public class STText extends SettingText {

    public STText(String title, String defaulttext) {
        super(title, defaulttext);
    }



    /*public int defvalue;
    public HashMap<String, Integer> values = new HashMap();
    public String backendname;

    public STKey(String title, int key)
      {
        this.defvalue = Integer.valueOf(key);
        this.values.put("", Integer.valueOf(key));
        this.backendname = title;
      }

      public STKey(String title, String key)
      {
        this(title, Keyboard.getKeyIndex(key));
      }



      public void set(Integer v, String context)
      {
    	  defvalue = v;
        this.values.put(context, v);

      }

      public Integer get()
      {


        return (Integer)this.defvalue;
      }*/
}