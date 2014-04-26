package com.nit.smartbicycle;

import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class ScaleView extends View {
	private Paint mPaint;
	/**
	 * �����ߵĿ��
	 */
	private int scaleWidth;
	/**
	 * �����ߵĸ߶�
	 */
	private int scaleHeight = 4;
	/**
	 * �����������������ɫ
	 */
	private int textColor = Color.BLACK;
	/**
	 * �������ϱߵ�����
	 */
	private String text;
	/**
	 * �����С
	 */
	private int textSize = 16;
	/**
	 * �������������ľ���
	 */
	private int scaleSpaceText = 8;
	/**
	 * �ٶȵ�ͼ������ż���
	 */
	private static final int MAX_LEVEL = 19;
	/**
	 * ���������߷�ĸֵ����
	 */
	private static final int[] SCALES = {20, 50, 100, 200, 500, 1000, 2000,
			5000, 10000, 20000, 25000, 50000, 100000, 200000, 500000, 1000000,
			2000000 };
	/**
	 * �����������������������
	 */
	private static final String[] SCALE_DESCS = { "20��", "50��", "100��", "200��",
			"500��", "1����", "2����", "5����", "10����", "20����", "25����", "50����",
			"100����", "200����", "500����", "1000����", "2000����" };
	
	private MapView mapView;
	
	

	/**
	 * ��MapView���ù���
	 * @param mapView
	 */
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}

	public ScaleView(Context context) {
		this(context, null);
	}
	
	public ScaleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScaleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	/**
	 * ������������ֺ�����ı����ߣ���Ϊ��������.9.png��������Ҫ����drawNinepath�������Ʊ�����
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = scaleWidth;
		
		mPaint.setColor(textColor);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		float textWidth = mPaint.measureText(text);
		
		canvas.drawText(text, (width - textWidth) / 2, textSize, mPaint);
		
		Rect scaleRect = new Rect(0, textSize + scaleSpaceText, scaleWidth, textSize + scaleSpaceText + scaleHeight);
		drawNinepath(canvas, R.drawable.icon_scale, scaleRect);
	}
	
	/**
	 * �ֶ�����.9.pngͼƬ
	 * @param canvas
	 * @param resId
	 * @param rect
	 */
	private void drawNinepath(Canvas canvas, int resId, Rect rect){  
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), resId);  
        NinePatch patch = new NinePatch(bmp, bmp.getNinePatchChunk(), null);  
        patch.draw(canvas, rect);  
    }

	

	/**
	 * ����ScaleView�ķ�����
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = getWidthSize(widthMeasureSpec);
		int heightSize = getHeightSize(heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}
	
	/**
	 * ����ScaleView�Ŀ��
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getWidthSize(int widthMeasureSpec){
		return MeasureSpec.getSize(widthMeasureSpec);
	}
	
	/**
	 * ����ScaleView�ĸ߶�
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getHeightSize(int heightMeasureSpec){
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		int height = 0;
		switch (mode) {
		case MeasureSpec.AT_MOST:
			height = textSize + scaleSpaceText + scaleHeight;
			break;
		case MeasureSpec.EXACTLY:{
			height = MeasureSpec.getSize(heightMeasureSpec);
			break;
		}
		case MeasureSpec.UNSPECIFIED:{
			height = Math.max(textSize + scaleSpaceText + scaleHeight, MeasureSpec.getSize(heightMeasureSpec));
			break;
		}
		}
		
		return height;
	}
	
	/**
	 * �������ż��𣬵õ���Ӧ��������SCALES�����е�λ�ã�������
	 * @param zoomLevel
	 * @return
	 */
	private static int getScaleIndex(int zoomLevel) {
		return MAX_LEVEL - zoomLevel;
	}

	/**
	 * �������ż��𣬵õ���Ӧ������
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public static int getScale(int zoomLevel) {
		return SCALES[getScaleIndex(zoomLevel)];
	}

	/**
	 *  �������ż��𣬵õ���Ӧ����������
	 * @param zoomLevel
	 * @return
	 */
	public static String getScaleDesc(int zoomLevel) {
		return SCALE_DESCS[getScaleIndex(zoomLevel)];
	}

	
	/**
	 * ���ݵ�ͼ��ǰ����λ�õ�γ�ȣ���ǰ�����ߣ��ó�������ͼ��Ӧ����ʾ�೤���������أ�
	 * @param map
	 * @param scale
	 * @return
	 */
	public static int meterToPixels(MapView map, int scale) {
		// �õ���ǰ����λ�ö���
		GeoPoint geoPoint = map.getMapCenter();
		// �õ���ǰ����λ��γ��
		double latitude = geoPoint.getLatitudeE6() / 1E6;
		// �õ������������統ǰ��������1/10000������scale=10000����Ӧ�ڸ�γ��Ӧ�ڵ�ͼ�л��������
		// �ο�http://rainbow702.iteye.com/blog/1124244
		return (int) (map.getProjection().metersToEquatorPixels(scale) / (Math
				.cos(Math.toRadians(latitude))));
		
		
	}

	/**
	 * ���ñ����ߵĿ��
	 * @param scaleWidth
	 */
	public  void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
	}

	/**
	 * ���ñ����ߵ������ text ���� 200����
	 * @param text
	 */
	private void setText(String text) {
		this.text = text;
	}

	/**
	 * ���������С
	 * @param textSize
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
		invalidate();
	}
	
	
	/**
	 * �������ż������ScaleView�������Լ������ߵĳ���
	 * @param level
	 */
	public void refreshScaleView(int level) {
		if(mapView == null){
			throw new NullPointerException("you can call setMapView(MapView mapView) at first");
		}
		setText(getScaleDesc(level));
		setScaleWidth(meterToPixels(mapView, getScale(level)));
		invalidate();
	}

}
