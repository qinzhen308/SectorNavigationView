package com.paulz.sectornavigatorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;


/**
 * Created by paulz on 2016/8/31.
 */
public class NavigationBar extends View {
    float radius;
    float startAngle;
    float endAngle;
    float gap;
    int count;
    float DEFAULT_RADIUS=100f;
    int DEFAULT_START_ANGLE=180;
    int DEFAULT_END_ANGLE=90;
    int DEFAULT_ITEM_COUNT=4;
    float DEFAULT_GAP=0;

    public RectF rectF;
    public RectF rectFSamll;
    public RectF rectFMain;
    public PointF center;//极坐标中点

    float smallGapAngle;
    float bigGapAngle;

    Paint shapePaint;
    Paint textPaint;
    OnItemClickListener mOnItemClickListener;
    OnCheckedChangeListener mOnCheckedChangeListener;

    private float downX;
    private float downY;
    private int mTouchSlop;
    private Context mContext;

    int[] bgColors={getResources().getColor(android.R.color.holo_blue_bright)
            ,getResources().getColor(android.R.color.holo_blue_bright)
            ,getResources().getColor(android.R.color.holo_blue_bright)
            ,getResources().getColor(android.R.color.holo_blue_bright)};
    int[] bgColorsSelected={getResources().getColor(android.R.color.holo_blue_dark)
            ,getResources().getColor(android.R.color.holo_blue_dark)
            ,getResources().getColor(android.R.color.holo_blue_dark)
            ,getResources().getColor(android.R.color.holo_blue_dark)};;
    String[] texts={"1","2","3","4"};

    Area[] areas;

    int selectedPosition=-1;


    public NavigationBar(Context context) {
        super(context);
        mContext=context;
        init();
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavigationBar);
        radius=a.getDimension(R.styleable.NavigationBar_radiusn,DEFAULT_RADIUS);
        gap=a.getDimension(R.styleable.NavigationBar_gap,DEFAULT_GAP);
        startAngle=a.getInt(R.styleable.NavigationBar_startAngle,DEFAULT_START_ANGLE);
        endAngle=a.getInt(R.styleable.NavigationBar_endAngle,DEFAULT_END_ANGLE);
        count=a.getInt(R.styleable.NavigationBar_count,DEFAULT_ITEM_COUNT);
        a.recycle();
        init();
    }

    private void init(){
//        getLayoutParams().height=getLayoutParams().width=(int)Math.ceil((double) radius);
        final ViewConfiguration vc = ViewConfiguration.get(mContext);
        mTouchSlop = vc.getScaledTouchSlop();
        center=new PointF(radius,radius);
        rectF=new RectF(0,0,2*radius,2*radius);
        float smallRadius=(radius-gap)/2;
        rectFSamll=new RectF(radius-smallRadius,radius-smallRadius,radius+smallRadius+gap,radius+smallRadius+gap);
        rectFMain=new RectF(radius-smallRadius+gap,radius-smallRadius+gap,radius+smallRadius,radius+smallRadius);
        smallGapAngle=gapToGapAngle(gap,smallRadius+gap);
        bigGapAngle=gapToGapAngle(gap,radius);
        shapePaint=new Paint(Paint.ANTI_ALIAS_FLAG| Paint.DITHER_FLAG);
        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG| Paint.DITHER_FLAG);
        textPaint.setColor(getResources().getColor(android.R.color.white));
        computeAreas();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) Math.ceil((double) radius),(int) Math.ceil((double) radius));
    }

    private void computeAreas(){
        areas=new Area[count];
        areas[0]=new Sector();
        float bigGapVector=((endAngle-startAngle>0)?1:-1)*bigGapAngle;//向量，有正负的角度
        float smallGapVector=((endAngle-startAngle>0)?1:-1)*smallGapAngle;//向量，有正负的角度
        float deltaAngleBig=(endAngle-startAngle-bigGapVector*(count-2))/(count-1);
        float deltaAngleSmall=(endAngle-startAngle-smallGapVector*(count-2))/(count-1);
        for(int i=1;i<count;i++){
            float startAngleBig=startAngle+deltaAngleBig*(i-1)+(i-1)*bigGapVector;
            float startAngleSmall=startAngle+deltaAngleSmall*(i-1)+(i-1)*smallGapVector;
            float endAngleBig=startAngleBig+deltaAngleBig;
            float endAngleSmall=startAngleSmall+deltaAngleSmall;
            areas[i]=new ArcArea(startAngleBig,endAngleBig,startAngleSmall,endAngleSmall,(radius-gap)/2);
        }

    }

    public void setComponents(int itemCount, String[] texts, int[] bgColors, int[] bgColorsSelected) throws Exception {
        if(texts.length!=itemCount&&bgColors.length!=itemCount&&bgColorsSelected.length!=itemCount){
            throw new Exception("参数传入异常，请传入的各数组长度等于itemCount");
        }
        int oldCount=count;
        count=itemCount;
        this.texts=texts;
        this.bgColors=bgColors;
        this.bgColorsSelected=bgColorsSelected;
        if(oldCount!=count){
            computeAreas();
        }
        invalidate();
    }

    /**
     * 设置选中的索引
     * @param i <0 全部都不选   i>item数，选中最后一个
     */
    public void setChecked(int i){
        if(selectedPosition==i){
            return;
        }
        int old=selectedPosition;
        if(i>count-1){
            selectedPosition=count-1;
        }else if(i<0){
            selectedPosition=-1;
        }else {
            selectedPosition=i;
        }
        if(mOnCheckedChangeListener!=null){
            mOnCheckedChangeListener.onCheckedChange(selectedPosition,old,false);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(areas==null)return;
//        areas[1].onDraw(canvas,1);
        for(int i=0;i<areas.length;i++){
            areas[i].onDraw(canvas,i);
        }
    }


    public float gapToGapAngle(float gap,float r){
        return (float)(gap*180/(r* Math.PI));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX=event.getX();
                downY=event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:

                break;
            case MotionEvent.ACTION_UP:
                 if(judgeClickEvent(event.getX(),event.getY())){
                     return true;
                 }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean judgeClickEvent(float x,float y){
        if(Math.sqrt(Math.pow(x-downX,2)+ Math.pow(y-downY,2))>mTouchSlop)return false;
        for(int i=0;i<count;i++){
            if(areas[i].isInArea(x,y)){
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemClick(i);
                }
                if(selectedPosition!=i&&mOnCheckedChangeListener!=null){
                    mOnCheckedChangeListener.onCheckedChange(i,selectedPosition,true);
                }
                selectedPosition=i;
                invalidate();
                return true;
            }
        }
        return false;
    }

    public abstract class Area{
        public float textX;
        public float textY;
        public float textSize;

        public abstract boolean isInArea(float x,float y);
        public abstract void computeTextXY();
        public abstract void onDraw(Canvas canvas, int position);


        public void computeMaxX(){
        }
        public void computeMaxY(){

        }


        public float castXYToR(float x,float y){
            final float jx=x-radius;
            final float jy=radius-y;
            return (float) Math.sqrt(Math.pow(jx,2)+ Math.pow(jy,2));
        }

        public float castXYToTheta(float x,float y){
            float a=x-radius;
            float b=radius-y;
            if(a==0){
                if(b>0){
                    return 90;
                }else if(b<0){
                    return -90;
                }else {//代表点到了原点
                    return 1111;
                }
            }
            return (float)(Math.atan(b/a)*180/ Math.PI)+(a<0?180:0);
        }
        //xy是 view坐标系中的
        public float[] castRThetaToXY(float r,float t){
            float[] xy=new float[2];
            xy[0]=(float) (r* Math.cos(t* Math.PI/180)+radius);
            xy[1]=(float) (radius-r* Math.sin(t* Math.PI/180));
            return xy;
        }
    }

    //第一个的形状，扇形
    public class Sector extends Area{

        public Sector(){
            computeTextXY();
        }

        @Override
        public boolean isInArea(float x, float y) {
            float r=castXYToR(x,y);
            float theta=castXYToTheta(x,y);
            if(r<rectFMain.width()/2&&theta< Math.max(startAngle,endAngle)&&theta> Math.min(startAngle,endAngle)){
                return true;
            }
            return false;
        }

        @Override
        public void onDraw(Canvas canvas, int position) {
            int[] colors=selectedPosition==position?bgColorsSelected:bgColors;
            shapePaint.setColor(colors[position]);
            canvas.drawArc(rectFMain,360-startAngle,(360-endAngle)-(360-startAngle),true,shapePaint);
            textPaint.setTextSize(textSize);
            String text=texts[position];
            canvas.drawText(text,textX,textY,textPaint);
/*
            if(text.length()<=2){
                canvas.drawText(text,textX,textY,textPaint);
            }else {
                canvas.drawText(text,0,text.length()/2,textX,textY,textPaint);
                canvas.drawText(text,text.length()/2,text.length(),textX,textY+textSize,textPaint);
            }*/
        }

        @Override
        public void computeTextXY() {
            final float a1=startAngle;
            final float a2=endAngle;
            float r=rectFMain.width()/2;
            float xy1[]=castRThetaToXY(r,a1);
            float xy2[]=castRThetaToXY(r,a2);
            textSize=radius/10;
            textX=(xy1[0]+xy2[0])/2-textSize/2;
            textY=(xy1[1]+xy2[1])/2+textSize;
        }
    }

    //其余部分形状，楔形
    public class ArcArea extends Area{

        float maxR;//区域最大半径
        float minR;//区域最小半径值
        float pathTheta;//极坐标角度，逆时针，[-180,180]
        float pathR;//极坐标半径，大于0
        float startAngleBig;
        float endAngleBig;
        float startAngleSmall;
        float endAngleSmall;


        public ArcArea(float startAngleBig,float endAngleBig,float startAngleSmall,float endAngleSmall,float length){
            maxR=radius;
            minR=maxR-length;
            this.startAngleBig=startAngleBig;
            this.startAngleSmall=startAngleSmall;
            this.endAngleBig=endAngleBig;
            this.endAngleSmall=endAngleSmall;
//            pathX=castRThetaToXY();
            computeTextXY();
        }

        @Override
        public void computeTextXY() {
            //big
            float xy11[]=castRThetaToXY(maxR,startAngleBig);
            float xy12[]=castRThetaToXY(maxR,endAngleBig);
            //small
            float xy21[]=castRThetaToXY(minR,startAngleSmall);
            float xy22[]=castRThetaToXY(minR,endAngleSmall);
            float maxX= Math.max(Math.max(xy11[0],xy12[0]), Math.max(xy21[0],xy22[0]));
            float minX= Math.min(Math.min(xy11[0],xy12[0]), Math.min(xy21[0],xy22[0]));
            float maxY= Math.max(Math.max(xy11[1],xy12[1]), Math.max(xy21[1],xy22[1]));
            float minY= Math.min(Math.min(xy11[1],xy12[1]), Math.min(xy21[1],xy22[1]));
            textSize=radius/10;
            textY=(maxY+minY)/2+textSize/2;
//            Paint paint=new Paint();
//            paint.set(textPaint);
//            paint.setTextSize(textSize);
//            paint.getTextWidths(texts[1],);
            textX=(maxX+minX)/2-textSize/2;
        }



        @Override
        public boolean isInArea(float x, float y) {
            //转化到这里面的坐标系

            float r=castXYToR(x,y);
            float theta=castXYToTheta(x,y);
            if(theta>360){//点到原点了
                return false;
            }
            if(r<maxR&&r>minR&&theta< Math.max(startAngleSmall,endAngleSmall)&&theta> Math.min(startAngleSmall,endAngleSmall)){
                return true;
            }
            return false;
        }

        @Override
        public void onDraw(Canvas canvas, int position){
            Path path=new Path();
            path.arcTo(rectF,360-startAngleBig,(360-endAngleBig)-(360-startAngleBig));
            float pathX;//绘制的中间点
            float pathY;//绘制的中间点
            float[] pathMiddle=castRThetaToXY((gap/2+radius/2),endAngleSmall);
            path.lineTo(pathMiddle[0],pathMiddle[1]);
            path.arcTo(rectFSamll,360-endAngleSmall,(360-startAngleSmall)-(360-endAngleSmall));
            path.close();
            int[] colors=selectedPosition==position?bgColorsSelected:bgColors;
            shapePaint.setColor(colors[position]);
            canvas.drawPath(path,shapePaint);
            textPaint.setTextSize(textSize);
            String text=texts[position];
            if(text.length()<=2){
                canvas.drawText(text,textX,textY,textPaint);
            }else {
                canvas.drawText(text,0,text.length()/2,textX,textY,textPaint);
                canvas.drawText(text,text.length()/2,text.length(),textX,textY+textSize,textPaint);
            }

        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener=onItemClickListener;
    }

    public interface OnItemClickListener{
        public void onItemClick(int position);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener){
        mOnCheckedChangeListener=onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener{
        /**
         *
         * @param position -1 代表什么都没有选中,只有外部通过{@link NavigationBar setChecked(int i)}
         * @param oldPosition
         * @param changeBySelf true代表内部的改变引起的回调，比如点击导致的改变；false代表外部改变选中状态，也就是setcheck(int i)引起的变化。
         */
        public void onCheckedChange(int position, int oldPosition ,boolean changeBySelf);
    }


}
