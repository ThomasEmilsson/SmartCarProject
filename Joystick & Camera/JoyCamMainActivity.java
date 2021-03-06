package aras.JoyCam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * @author Axel Slättman and Aras Bazyan
 *04/05/2016

 * Version 0.0.0.5

 */

import aras.frameltest.R;


public class JoyCamMainActivity extends Activity implements View.OnTouchListener {

    MyView v;
    Bitmap joy;
    Bitmap joybg;
    int zeroX, zeroY, car, speed;
    float x, y, dx, dy, h, angle;
    Canvas c = new Canvas();
    Paint red = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.JoyCamLayout);


        String feedSource = "http://172.20.10.6/html/";
        WebView view = (WebView) this.findViewById(R.id.webView);
        view.getSettings().setJavaScriptEnabled(true);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                // view.loadUrl("javascript:document.getElementsByClassName('container-fluid text-center').style.display = 'none'");

                view.loadUrl("javascript:document.getElementById('toggle_display').style.display = 'none'");
                view.loadUrl("javascript:document.getElementById('main-buttons').style.display = 'none'");
                view.loadUrl("javascript:document.getElementById('secondary-buttons').style.display = 'none'");
                view.loadUrl("javascript:document.getElementsByClassName('navbar navbar-inverse navbar-fixed-top')[0].style.visibility='hidden'");
                view.loadUrl("javascript:document.getElementById('accordion').style.display = 'none'");
                view.loadUrl("javascript:document.getElementById('mjpeg_dest').onclick = null");

                //document.getElementById('righttbutton').onclick = null
            }
        });

        view.setInitialScale(240);
        view.loadUrl(feedSource);


        v = new MyView(this);
        v.setOnTouchListener(this);
        joy = BitmapFactory.decodeResource(getResources(), R.drawable.joy1);
        joybg = BitmapFactory.decodeResource(getResources(),R.drawable.joybg);

        RelativeLayout surface = (RelativeLayout) findViewById(R.id.joystick);
        surface.addView(v);
    }

    @Override
    protected void onPause(){
        super.onPause();
        v.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        v.resume();
    }

  /*  public static void setUrl(String input){
        try {
            url = new URL(input);
        }
        catch (IOException e){
            System.out.println(e);
        }
    }*/

    public static Bitmap getCameraImage(String url){
        try{
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
            return bitmap;
        }
        catch (IOException e){
            System.out.println(e);
            return null;
        }
    }

 /*   @Override
    public void run() {
     //   camerafeed = getCameraImage(imageUrl);
        try{
            Thread.sleep(700);
        }
        catch(InterruptedException e){
        }
    }*/


    public class MyView extends SurfaceView implements Runnable{

        Thread thread = null;
        SurfaceHolder holder;
        boolean check = false;
        float radius;
        int quadrant;
        int padding = 15;
        String xText, yText, angleText, hypo, speedText, carText;




        public MyView(Context context) {
            super(context);
            holder = getHolder();
            //holder.setFixedSize(133,133);
        }

        //Thread that repaints the canvas
        public void run(){

            while(check){

                if(!holder.getSurface().isValid())
                    continue;

                red.setColor(Color.RED);
                red.setStyle(Paint.Style.STROKE);
                red.setStrokeWidth(3);
                red.setTextSize(30);


                c = holder.lockCanvas();
                c.drawColor(Color.WHITE);
                // c.drawBitmap(camerafeed,0,0,null);
                c.drawBitmap(joybg, c.getWidth() / 2 - joybg.getWidth() / 2, c.getHeight() / 2 - joybg.getHeight() / 2, null);
                radius = joybg.getWidth()/2;
                if(x == 0 && y == 0)
                    c.drawBitmap(joy, c.getWidth() / 2 - joy.getWidth() / 2, c.getHeight() / 2 - joy.getHeight() / 2, null);

                else {
                    calc(x, y);
                    c.drawBitmap(joy, x - (joy.getWidth()/2), y - (joy.getHeight()/2), null);
                }
               /* xText = "X = " + (int)dx;
                yText = "Y = " + (int)dy;
                angleText = "angle = " + (int)(angle*180/Math.PI);
                hypo = "Hypo = " + (int)h;
                speedText = "Speed = " + speed;
                carText = "Angle for car = " + car;
                c.drawText(xText,100,100,red);
                c.drawText(yText,100,150,red);
                c.drawText(angleText,100,200,red);
                c.drawText(hypo,100,250,red);
                c.drawText(speedText,100,300,red);
                c.drawText(carText,100,350,red);*/

                holder.unlockCanvasAndPost(c);
            }
        }
        public void pause(){
            check = false;
            while(check){
                try{
                    thread.join();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            }
            thread = null;
        }
        public void resume(){
            check = true;
            thread = new Thread(this);
            thread.start();
        }

        //Limits the distance you can drag the joystick using math.
        public void calc(float xx, float yy){
            zeroX = c.getWidth()/2;
            zeroY = c.getHeight()/2;

            dx = xx - zeroX;
            dy = yy - zeroY;

            angle = (float)Math.atan(Math.abs(dy/dx));
            h = (float)Math.sqrt(dx*dx+dy*dy);
            if(h>151) h = 151;
            speed = (int)((h-1)*0.6666666667);

            if(dx > 0 && dy > 0) {
                if(h > radius) {  //Keep the joystick within limits
                    xx = (float) (zeroX + (radius * Math.cos(angle)));
                    yy = (float) (zeroY + (radius * Math.sin(angle)));
                }
                speed = -speed; //Reverse speed when pulling down on joystick
                quadrant = 1; //Assign quadrant so we can transform the angle into correct one on 0-360 scale
            }
            else if(dx>0&&dy<0){
                if(h > radius) {
                    xx = (float) (zeroX + (radius * Math.cos(angle)));
                    yy = (float) (zeroY - (radius * Math.sin(angle)));
                }
                quadrant = 0;
            }
            else if(dx<0&&dy<0){
                if(h > radius) {
                    xx = (float) (zeroX - (radius * Math.cos(angle)));
                    yy = (float) (zeroY - (radius * Math.sin(angle)));
                }
                quadrant = 3;
            }
            else if(dx < 0 && dy > 0){
                if(h > radius) {
                    xx = (float) (zeroX - (radius * Math.cos(angle)));
                    yy = (float) (zeroY + (radius * Math.sin(angle)));
                }
                speed = -speed;
                quadrant = 2;
            }

            else{
                xx = zeroX + dx;
                yy = zeroY + dy;
            }
            x = xx;
            y = yy;

            car = determineCarAngle(quadrant);
        }

        //Function to convert angle from 0-90 scale into 0-360 based on which quadrant the joystick is in
        public int determineCarAngle(int q){
            int a = 0;

            switch (q) {
                case 0:
                    a = (int) Math.abs((angle * 180 / Math.PI) - 90);
                    break;

                case 1:
                    a = (int) Math.abs((angle * 180 / Math.PI) - 90);
                    break;

                case 2:
                    a = (int) ((angle * 180 / Math.PI) - 90);
                    break;

                case 3:
                    a = (int) (angle * 180 / Math.PI) - 90;
                    break;
            }

            //Buffers to make joystick a bit more stable.
            if (a < 5 && a > -5)
                a = 0;
            if (a < 100 && a > 80) {
                a = 90;
                if (speed < 0)
                    speed = -speed;
            }
            if (a < -80 && a > -100) {
                a = -90;
                if (speed < 0)
                    speed = -speed;
            }

            return a;
        }





    }
    @Override
    public boolean onTouch(View v, MotionEvent me) {


        switch(me.getAction()){
            case MotionEvent.ACTION_DOWN:
                //Get x and y
                x = me.getX();
                y = me.getY();
                break;
            case MotionEvent.ACTION_UP:
                //Reset every value on release of joystick
                x = y = dx = dy = h = angle = 0;
                car = speed = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                //Update x and y
                x = me.getX();
                y = me.getY();

        }


        return true;
    }


}


