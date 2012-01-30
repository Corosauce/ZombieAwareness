package net.minecraft.src;

import java.util.HashMap;

public class STBoolean extends SettingBoolean {

    public STBoolean(String name, Boolean _defvalue) {
        super(name, _defvalue);
    }

    public STBoolean(String name) {
        super(name);
    }


    /*public Boolean defvalue;
    public HashMap<String, Boolean> values = new HashMap();
    public String backendname;

    	public STBoolean() {

    	}

      public STBoolean(String name, Boolean _defvalue)
      {
        this.defvalue = _defvalue;
        this.values.put("", this.defvalue);
        this.backendname = name;
      }

      public STBoolean(String name)
      {
        this(name, Boolean.valueOf(false));
      }

      public Boolean get()
      {

        return (Boolean)this.defvalue;
      }

      public void set(Boolean v, String context)
      {
    	  defvalue = v;
        this.values.put(context, v);
      }*/
}