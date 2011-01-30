package su.geocaching.android.controller.compass;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Default appearance of the compass
 * 
 * @author Nikita Bumakov
 */
public class StandartCompassDrawning extends CompassDrawningHelper {

	private Paint bitmapPaint;
	private Bitmap roseBitmap;
	private Bitmap needleBitmap;

	public StandartCompassDrawning(Context context) {
		super(context);
		bitmapPaint = new Paint();
		bitmapPaint.setAntiAlias(true); // It is not useful
		roseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass);
	}

	public void onSizeChanged(int w, int h) {
		int size = Math.min(h, w);
		center = size / 2;
		roseBitmap = Bitmap.createScaledBitmap(roseBitmap, size, size, true);
		needleBitmap = createNeedle();
	}

	public void draw(Canvas canvas, float northDirection, float cacheDirection) {
		canvas.drawColor(bgColor);
		canvas.drawBitmap(roseBitmap, 0, 0, bitmapPaint);
		drawNeedle(canvas, northDirection);
	}

	private void drawNeedle(Canvas canvas, float direction) {
		canvas.translate(center, center);
		canvas.rotate(direction);
		canvas.drawBitmap(needleBitmap, -needleBitmap.getWidth() / 2, -needleBitmap.getHeight() / 2, bitmapPaint);
		canvas.rotate(-direction);
	}

	private Bitmap createNeedle() {
		Bitmap bitmap = Bitmap.createBitmap(needleWidth * 3, center * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Path needlePath = new Path();

		float top = center * 0.85f;
		canvas.translate(needleWidth * 1.5f, center);
		needlePath.moveTo(-needleWidth, 0);
		needlePath.lineTo(0, -top);
		needlePath.lineTo(needleWidth, 0);
		needlePath.close();

		paint.setColor(Color.BLUE);
		canvas.drawPath(needlePath, paint);

		canvas.rotate(180);
		paint.setColor(Color.RED);
		canvas.drawPath(needlePath, paint);

		paint.setColor(Color.rgb(255, 230, 110));
		canvas.drawCircle(0, 0, needleWidth * 1.5f, paint);

		return bitmap;
	}
}