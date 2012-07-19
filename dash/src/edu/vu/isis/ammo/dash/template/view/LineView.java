package edu.vu.isis.ammo.dash.template.view;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;

/**
 * GuiField that draws a horizontal line but contains no data
 * 
 * @author Nick King, Marian Rushdy
 * 
 */
public class LineView implements GuiField {

	private static volatile int sNumCreated = 0;

	private ImageView mImageView;
	private String mId;

	public LineView(Context context, Element eNode) {
		sNumCreated++;
		mImageView = new ImageView(context);
		mId = "LineView" + sNumCreated;
		Drawable image = context.getResources().getDrawable(
				R.drawable.trans_white_trans_gradient);
		if(image == null) return;
		
		mImageView.setImageDrawable(image);
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mImageView.setLayoutParams(params);
		mImageView.setPadding(0, 5, 0, 5);
	}

	@Override
	public View getView() {
		return mImageView;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public String getTag() {
		return AmmoParser.LINE_TAG;
	}

	/**
	 * Not supported by this class
	 */
	@Override
	public String getLabel() {
		return "";
	}

	/**
	 * Not supported by this class
	 */
	@Override
	public String getValue() {
		return "";
	}

	/**
	 * Not supported by this class
	 */
	@Override
	public void setValue(String value) {
		// does not apply
	}

	/**
	 * Not supported by this class
	 */
	@Override
	public void makeReadOnly() {
		// does not apply
	}

}
