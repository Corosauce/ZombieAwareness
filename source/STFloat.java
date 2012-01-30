package net.minecraft.src;

import java.util.HashMap;

public class STFloat extends SettingFloat {

    public STFloat(String title) {
        super(title);
    }

    public STFloat(String title, float _value) {
        super(title, _value);
    }

    public STFloat(String title, float _value, float _min, float _max) {
        super(title,_value,_min,_max);
    }

    public STFloat(String title, float _value, float _min, float _step, float _max) {
        super(title, _value, _min, _step, _max);
    }


    /*public float defvalue;
    public HashMap<String, Float> values = new HashMap();
    public String backendname;

    public float step;
      public float min;
      public float max;

      public STFloat(String title)
      {
        this(title, 0.0F, 0.0F, 0.1F, 1.0F);
      }

      public STFloat(String title, float _value)
      {
        this(title, _value, 0.0F, 0.1F, 1.0F);
      }

      public STFloat(String title, float _value, float _min, float _max)
      {
        this(title, _value, _min, 0.1F, _max);
      }

      public STFloat(String title, float _value, float _min, float _step, float _max)
      {
        this.values.put("", Float.valueOf(_value));
        this.defvalue = Float.valueOf(_value);
        this.min = _min;
        this.step = _step;
        this.max = _max;
        this.backendname = title;

        if (this.min > this.max)
        {
          float t = this.min;
          this.min = this.max;
          this.max = t;
        }
      }

      public void set(Float v, String context)
      {
    	  defvalue = v;
        if (this.step > 0.0F)
          this.values.put(context, Float.valueOf(Math.round(v.floatValue() / this.step) * this.step));
        else
          this.values.put(context, v);
      }

      public Float get()
      {

        return (Float)this.defvalue;
      }*/
}