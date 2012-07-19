package edu.vu.isis.ammo.dash.template.view;

import org.w3c.dom.Element;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;

public class HeaderView implements GuiField {

	private ViewGroup viewGroup;
	private TextView label;
	private String id;
	private String labelValue;
	
	public HeaderView(Context context, Element eNode) {
		viewGroup = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.template_header_view, null);
		label = (TextView) viewGroup.findViewById(R.id.headerText);
		id = eNode.getAttribute(AmmoParser.ID_FIELD);
		labelValue = eNode.getAttribute(AmmoParser.LABEL_FIELD);
		if(labelValue == null || labelValue.length() == 0) {
			labelValue = id;
		}
		label.setText(labelValue);
	}
	
	@Override
	public View getView() {
		return viewGroup;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTag() {
		return AmmoParser.HEADER_TAG;
	}

	@Override
	public String getLabel() {
		return labelValue;
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public void setValue(String value) {
		labelValue = value;
		label.setText(value);
	}

	@Override
	public void makeReadOnly() {
		viewGroup.setVisibility(View.GONE);
	}

}
