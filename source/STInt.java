package net.minecraft.src;

import java.util.HashMap;

public class STInt extends SettingInt {

    public STInt(String title) {
        super(title);
    }

    public STInt(String title, int _value) {
        super(title, _value);
    }

    public STInt(String title, int _value, int _min, int _max) {
        super(title,_value,_min,_max);
    }

    public STInt(String title, int _value, int _min, int _step, int _max) {
        super(title, _value, _min, _step, _max);
    }



    /*public int defvalue;
    public HashMap<String, Integer> values = new HashMap();
    public String backendname;

    public int step;
      public int min;
      public int max;

      public STInt(String title)
      {
        this(title, 0, 0, 1, 100);
      }

      public STInt(String title, int _value)
      {
        this(title, _value, 0, 1, 100);
      }

      public STInt(String title, int _value, int _min, int _max)
      {
        this(title, _value, _min, 1, _max);
      }

      public STInt(String title, int _value, int _min, int _step, int _max)
      {
        this.values.put("", Integer.valueOf(_value));
        this.defvalue = Integer.valueOf(_value);
        this.min = _min;
        this.step = _step;
        this.max = _max;
        this.backendname = title;

        if (this.min > this.max)
        {
          int t = this.min;
          this.min = this.max;
          this.max = t;
        }
      }

      public void set(Integer v, String context)
      {
    	  defvalue = v;
        if (this.step > 1)
          this.values.put(context, Integer.valueOf((int)(Math.round(v.intValue() / this.step) * this.step)));
        else
          this.values.put(context, v);

      }

      public Integer get()
      {

        return (Integer)this.defvalue;
      }*/
}