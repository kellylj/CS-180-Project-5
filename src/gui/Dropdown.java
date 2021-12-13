package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Dropdown extends JPanel {

	private String resultKey;
	private JLabel label;
	private JComboBox<String> jComboBox;
	private List<String> options;
	private List<Integer> optionsIds;
	
	public Dropdown(String resultKey, List<String> options, List<Integer> optionsIds) {
		this.resultKey = resultKey;
		String[] arr = new String[options.size()];
		for(int i = 0; i < options.size(); i++)
			arr[i] = options.get(i);
		this.jComboBox = new JComboBox<String>(arr);
		this.options = options;
		this.optionsIds = optionsIds;

		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(jComboBox);

		this.jComboBox.setForeground(Aesthetics.TEXT_FIELD_FOREGROUND);
		this.jComboBox.setBackground(Aesthetics.TEXT_FIELD_BACKGROUND);
		
		this.setBackground(Aesthetics.GENERAL_BACKGROUND);
	}
	
	public Dropdown(String resultKey, String[] options) {
		this.resultKey = resultKey;
		this.jComboBox = new JComboBox<String>(options);

		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(jComboBox);

		this.jComboBox.setForeground(Aesthetics.TEXT_FIELD_FOREGROUND);
		this.jComboBox.setBackground(Aesthetics.TEXT_FIELD_BACKGROUND);
		
		this.setBackground(Aesthetics.GENERAL_BACKGROUND);
	}
	
	public Dropdown(String resultKey, List<String> options) {
		this.resultKey = resultKey;
		String[] arr = new String[options.size()];
		for(int i = 0; i < options.size(); i++)
			arr[i] = options.get(i);
		this.jComboBox = new JComboBox<String>(arr);

		this.setLayout(new GridLayout(1, 1));
		this.add(jComboBox);

		this.jComboBox.setForeground(Aesthetics.TEXT_FIELD_FOREGROUND);
		this.jComboBox.setBackground(Aesthetics.TEXT_FIELD_BACKGROUND);
		
		this.setBackground(Aesthetics.GENERAL_BACKGROUND);
	}
	
	public Dropdown(String name, String resultKey, String[] options) {
		this.label = new JLabel(name);
		this.resultKey = resultKey;
		this.jComboBox = new JComboBox<String>(options);
		
		this.setLayout(new GridLayout(2, 1));
		this.add(label);
		this.add(jComboBox);

		this.label.setForeground(Aesthetics.GENERAL_FOREGROUND);
		this.jComboBox.setForeground(Aesthetics.TEXT_FIELD_FOREGROUND);
		this.jComboBox.setBackground(Aesthetics.TEXT_FIELD_BACKGROUND);
		
		this.setBackground(Aesthetics.GENERAL_BACKGROUND);
	}
	
	@Override
	public void setSize(int x, int y) {
		super.setPreferredSize(new Dimension(x, y));
		super.setMinimumSize(new Dimension(x, y));
		super.setMaximumSize(new Dimension(x, y));
		super.setSize(x, y);
		if(label == null) {
			this.jComboBox.setSize(x, y);
		} else {
			this.label.setSize(x, y/2);
			this.jComboBox.setSize(x, y/2);
		}
	}
	
	public String getSelection() {
		String item = (String) this.jComboBox.getSelectedItem();
		if(this.optionsIds == null) {
			return item;
		}
		for(int i = 0; i < options.size(); i++) {
			if(options.get(i).equals(item))
				return Integer.toString(optionsIds.get(i));
		}
		return item;
	}
	
	public String getResultKey() {
		return this.resultKey;
	}
	
	public Dropdown margin(int margin) {
		this.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
		return this;
	}
	
	public Dropdown onChange(Consumer<String> consumer) {
		this.jComboBox.addActionListener((ActionEvent e) -> {
			consumer.accept(getSelection());
		});
		return this;
	}

	public Panel panelize(int width, int height, int marginX, int marginY) {
		this.setSize(width-marginX, height-marginY);
		return new Panel()
			.add(this)
			.setPanelSize(width, height)
			.alignLeft()
			.setMargin(marginX, marginY);
	}
	
	public void select(String questionType) {
		this.jComboBox.setSelectedItem(questionType);
	}
	
}
