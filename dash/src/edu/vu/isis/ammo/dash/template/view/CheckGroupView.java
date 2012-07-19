package edu.vu.isis.ammo.dash.template.view;

import org.w3c.dom.Element;

import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class CheckGroupView extends CompoundGroupView {
	
	public CheckGroupView(Context context, Element eNode) {
		super(context, eNode);
		for (String value : mValues) {
			CheckBox cb = new CheckBox(context);
			cb.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
			cb.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
			cb.setText(value);
			cb.setChecked(false);
			mLinearLayout.addView(cb);
			mCompoundButtons.add(cb);
		}
	}
	
	@Override
	public String getTag() {
		return AmmoParser.CHECKGROUP_TAG;
	}

}
