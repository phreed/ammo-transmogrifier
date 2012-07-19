package edu.vu.isis.ammo.dash.template.view;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;
import edu.vu.isis.ammo.dash.template.parsing.ViewParsers;

public class CompoundGroupView implements GuiField {

	// This class is meant to be subclassed, so these fields are package private.
	List<CompoundButton> mCompoundButtons = new ArrayList<CompoundButton>();
	List<String> mValues;
	String mId;
	String mLabelValue;

	Context mContext;
	ViewGroup mViewGroup;
	LinearLayout mLinearLayout;
	TextView mTextView;
	
	private static final Logger logger = LoggerFactory.getLogger("dash.template.CompoundGroupView");

	
	public CompoundGroupView(Context context, Element eNode) {
		mValues = ViewParsers.getTextFromChildNodes(eNode);

		mContext = context;

		mViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(
				R.layout.template_compoundgroup_view, null);
		TextView label = (TextView) mViewGroup
				.findViewById(R.id.compoundgroup_view_label);

		mId = eNode.getAttribute(AmmoParser.ID_FIELD);
		mLabelValue = eNode.getAttribute(AmmoParser.LABEL_FIELD);
		if (mLabelValue == null || mLabelValue.length() == 0) {
			mLabelValue = mId;
		}

		label.setText(mLabelValue.endsWith(":") ? mLabelValue : mLabelValue
				+ ":");

		if (mValues.isEmpty())
			return;

		mLinearLayout = (LinearLayout) mViewGroup
				.findViewById(R.id.compoundgroup_view_linearlayout);
		
	}
	
	@Override
	public View getView() {
		return mViewGroup;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public String getTag() {
		return AmmoParser.CHECKGROUP_TAG;
	}

	@Override
	public String getLabel() {
		return mLabelValue;
	}

	@Override
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		logger.trace("GetValue called for {}", this);
		for (CompoundButton cb : mCompoundButtons) {
			if (cb.isChecked()) {
				logger.trace("CompoundButton {} is checked.", cb.getText());
				sb.append(cb.getText()).append('\n');
			}
		}
		if(sb.length() > 0) {
			sb.delete(sb.length()-1, sb.length());
		}
		logger.trace("Returning String: {}", sb.toString());
		return sb.toString();
	}

	/**
	 * Sets the CompoundButtons whose text case-insensitive equals the values in the
	 * newline-delimited String to be checked. All other CompoundButtons are set
	 * unchecked. For example, the String "Foo\nBar" would set the CompoundButtons
	 * with the text "foo" and "bar" to be checked, while the CompoundButton with the
	 * text "baz" would be left unchecked.
	 */
	@Override
	public void setValue(String value) {
		logger.trace("setValue called");
		
		if(mTextView != null) {
			mTextView.setText(value);
			return;
		}
		
		String[] values = value.split("\n");

		// Clear the checkboxes
		for (CompoundButton cb : mCompoundButtons) {
			cb.setChecked(false);
		}

		// Iterate over the values and check the appropriate
		// checkboxes
		for (String val : values) {
			val = val.trim();
			for (CompoundButton cb : mCompoundButtons) {
				if (cb.getText().toString().equalsIgnoreCase(val)) {
					logger.trace("Checking CompoundButton {}", cb.getText());
					cb.setChecked(true);
				}
			}
		}
	}

	@Override
	public void makeReadOnly() {
		if(mTextView != null) return;
		String value = getValue();
		mLinearLayout.removeAllViews();
		mTextView = new TextView(mContext);
		setValue(value);
		logger.trace("TextView text: {}");
		mViewGroup.addView(mTextView);
	}

}
