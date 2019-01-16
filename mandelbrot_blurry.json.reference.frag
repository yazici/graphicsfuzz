#version 100

#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
precision highp int;
#else
precision mediump float;
precision mediump int;
#endif
#endif

#ifndef REDUCER
#define _GLF_ZERO(X, Y)          (Y)
#define _GLF_ONE(X, Y)           (Y)
#define _GLF_FALSE(X, Y)         (Y)
#define _GLF_TRUE(X, Y)          (Y)
#define _GLF_IDENTITY(X, Y)      (Y)
#define _GLF_DEAD(X)             (X)
#define _GLF_FUZZED(X)           (X)
#define _GLF_WRAPPED_LOOP(X)     X
#define _GLF_WRAPPED_IF_TRUE(X)  X
#define _GLF_WRAPPED_IF_FALSE(X) X
#define _GLF_SWITCH(X)           X
#endif

// END OF GENERATED HEADER
uniform vec2 resolution;

vec3 pickColor(int i)
{
 return vec3(float(i) / 100.0);
}
vec3 mand(float xCoord, float yCoord)
{
 float height = resolution.y;
 float width = resolution.x;
 float c_re = (xCoord - width / 2.0) * 4.0 / width;
 float c_im = (yCoord - height / 2.0) * 4.0 / width;
 float x = 0.0, y = 0.0;
 int iteration = 0;
 for(
     int k = 0;
     k < 1000;
     k ++
 )
  {
   if(x * x + y * y > 4.0)
    {
     break;
    }
   float x_new = x * x - y * y + c_re;
   y = 2.0 * x * y + c_im;
   x = x_new;
   iteration ++;
  }
 if(iteration < 1000)
  {
   return pickColor(iteration);
  }
 else
  {
   return vec3(0.0);
  }
}
void main()
{
 vec3 data[9];
 for(
     int i = 0;
     i < 3;
     i ++
 )
  {
   for(
       int j = 0;
       j < 3;
       j ++
   )
    {
     data[3 * j + i] = mand(gl_FragCoord.x + float(i - 1), gl_FragCoord.y + float(j - 1));
    }
  }
 vec3 sum = vec3(0.0);
 for(
     int i = 0;
     i < 9;
     i ++
 )
  {
   sum += data[i];
  }
 sum /= vec3(9.0);
 gl_FragColor = vec4(sum, 1.0);
}
