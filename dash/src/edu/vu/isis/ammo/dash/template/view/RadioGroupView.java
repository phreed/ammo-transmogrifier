package edu.vu.isis.ammo.dash.template.view;

import org.w3c.dom.Element;

import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RadioGroupView extends CompoundGroupView {

	public RadioGroupView(Context context, Element eNode) {
		super(context, eNode);
		RadioGroup radioGroup = new RadioGroup(context);
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		radioGroup.setLayoutParams(params);
		mLinearLayout.addView(radioGroup);

		for (String value : mValues) {
			RadioButton rb = new RadioButton(context);
			rb.setLayoutParams(params);
			rb.setText(value);
			radioGroup.addView(rb);
			mCompoundButtons.add(rb);
		}
	}
	
	@Override
	public String getTag() {
		return AmmoParser.RADIOGROUP_TAG;
	}

}
