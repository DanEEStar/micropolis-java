// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import micropolisj.engine.*;
import static micropolisj.gui.ColorParser.parseColor;

public class NotificationPane extends JPanel
{
	JLabel headerLbl;
	JViewport mapViewport;
	MicropolisDrawingArea mapView;
	JPanel mainPane;
	JComponent infoPane;

	static final Dimension VIEWPORT_SIZE = new Dimension(160,160);
	static final Color QUERY_COLOR = new Color(255,165,0);
	static final ResourceBundle strings = MainWindow.strings;
	static final ResourceBundle mstrings = ResourceBundle.getBundle("micropolisj.CityMessages");
	static final ResourceBundle s_strings = ResourceBundle.getBundle("micropolisj.StatusMessages");

	public NotificationPane(Micropolis engine)
	{
		super(new BorderLayout());
		setVisible(false);

		headerLbl = new JLabel();
		headerLbl.setOpaque(true);
		headerLbl.setHorizontalAlignment(SwingConstants.CENTER);
		headerLbl.setBorder(BorderFactory.createRaisedBevelBorder());
		add(headerLbl, BorderLayout.NORTH);

		JButton dismissBtn = new JButton(strings.getString("notification.dismiss"));
		dismissBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onDismissClicked();
			}});
		add(dismissBtn, BorderLayout.SOUTH);

		mainPane = new JPanel(new BorderLayout());
		add(mainPane, BorderLayout.CENTER);

		JPanel viewportContainer = new JPanel(new BorderLayout());
		viewportContainer.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(8,4,8,4),
				BorderFactory.createLineBorder(Color.BLACK)
				));
		mainPane.add(viewportContainer, BorderLayout.WEST);

		mapViewport = new JViewport();
		mapViewport.setPreferredSize(VIEWPORT_SIZE);
		mapViewport.setMaximumSize(VIEWPORT_SIZE);
		mapViewport.setMinimumSize(VIEWPORT_SIZE);
		viewportContainer.add(mapViewport, BorderLayout.CENTER);

		mapView = new MicropolisDrawingArea(engine);
		mapViewport.setView(mapView);
	}

	private void onDismissClicked()
	{
		setVisible(false);
	}

	void setPicture(Micropolis engine, int xpos, int ypos)
	{
		Dimension sz = VIEWPORT_SIZE;

		mapView.setEngine(engine);
		Rectangle r = mapView.getTileBounds(xpos,ypos);

		mapViewport.setViewPosition(new Point(
			r.x + r.width/2 - sz.width/2,
			r.y + r.height/2 - sz.height/2
			));
	}

	public void showMessage(Micropolis engine, MicropolisMessage msg, int xpos, int ypos)
	{
		setPicture(engine, xpos, ypos);

		if (infoPane != null) {
			mainPane.remove(infoPane);
			infoPane = null;
		}

		headerLbl.setText(mstrings.getString(msg.name()+".title"));
		headerLbl.setBackground(parseColor(mstrings.getString(msg.name()+".color")));

		JLabel myLabel = new JLabel("<html><p>"+
			mstrings.getString(msg.name()+".detail") + "</p></html>");
		myLabel.setPreferredSize(new Dimension(1,1));

		infoPane = myLabel;
		mainPane.add(myLabel, BorderLayout.CENTER);

		setVisible(true);
	}

	static class ZoneStatusPane extends JPanel implements AncestorListener
	{
		final Micropolis city;

		ZoneStatusPane(Micropolis city)
		{
			super(new GridBagLayout());
			this.city = city;
			this.addAncestorListener(this);
		}

		//implements AncestorListener
		public void ancestorAdded(AncestorEvent evt)
		{
			System.out.printf("%s: ancestor added\n", toString());
		}

		//implements AncestorListener
		public void ancestorRemoved(AncestorEvent evt)
		{
			System.out.printf("%s: ancestor removed\n", toString());
		}

		//implements AncestorListener
		public void ancestorMoved(AncestorEvent evt) {}
	}

	public void showZoneStatus(Micropolis engine, int xpos, int ypos, ZoneStatus zone)
	{
		headerLbl.setText(strings.getString("notification.query_hdr"));
		headerLbl.setBackground(QUERY_COLOR);

		String buildingStr = zone.building != -1 ? s_strings.getString("zone."+zone.building) : "";
		String popDensityStr = s_strings.getString("status."+zone.popDensity);
		String landValueStr = s_strings.getString("status."+zone.landValue);
		String crimeLevelStr = s_strings.getString("status."+zone.crimeLevel);
		String pollutionStr = s_strings.getString("status."+zone.pollution);
		String growthRateStr = s_strings.getString("status."+zone.growthRate);

		setPicture(engine, xpos, ypos);

		if (infoPane != null) {
			mainPane.remove(infoPane);
			infoPane = null;
		}

		ZoneStatusPane p = new ZoneStatusPane(engine);
		mainPane.add(p, BorderLayout.CENTER);
		infoPane = p;

		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();

		c1.gridx = 0;
		c2.gridx = 1;
		c1.gridy = c2.gridy = 0;
		c1.anchor = GridBagConstraints.WEST;
		c2.anchor = GridBagConstraints.WEST;
		c1.insets = new Insets(0,0,0,8);
		c2.weightx = 1.0;

		p.add(new JLabel(strings.getString("notification.zone_lbl")), c1);
		p.add(new JLabel(buildingStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.density_lbl")), c1);
		p.add(new JLabel(popDensityStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.value_lbl")), c1);
		p.add(new JLabel(landValueStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.crime_lbl")), c1);
		p.add(new JLabel(crimeLevelStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.pollution_lbl")), c1);
		p.add(new JLabel(pollutionStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.growth_lbl")), c1);
		p.add(new JLabel(growthRateStr), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.stock_lbl")), c1);
		p.add(makeStockPanel(engine, xpos, ypos, StockPanelType.STOCK), c2);

		c1.gridy = ++c2.gridy;
		p.add(new JLabel(strings.getString("notification.offers_lbl")), c1);
		p.add(makeStockPanel(engine, xpos, ypos, StockPanelType.OFFERS), c2);

		c1.gridy++;
		c1.gridwidth = 2;
		c1.weighty = 1.0;
		p.add(new JLabel(), c1);

		setVisible(true);
	}

	static class StockInfo
	{
		Map<Commodity,Integer> quantities = new HashMap<Commodity,Integer>();
		Map<Commodity,Integer> prices = new HashMap<Commodity,Integer>();
	}

	static enum StockPanelType
	{
		STOCK,
		OFFERS;
	}

	static JPanel makeStockPanel(Micropolis city, int xpos, int ypos, StockPanelType type)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		StockInfo si = new StockInfo();

		Tile t = city.getTileStack(xpos, ypos);
		while (t != null) {
			if (t instanceof CommodityTile) {
				CommodityTile ct = (CommodityTile) t;
				si.quantities.put(ct.commodity, ct.quantity);
			}
			else if (t instanceof PriceTile) {
				PriceTile pt = (PriceTile) t;
				si.prices.put(pt.commodity, pt.price);

				if (!si.quantities.containsKey(pt.commodity)) {
					si.quantities.put(pt.commodity, 0);
				}
			}
			t = t.next;
		}

		for (Commodity c : si.quantities.keySet()) {

			if (si.prices.containsKey(c) && type != StockPanelType.OFFERS) { continue; }
			if (!si.prices.containsKey(c) && type != StockPanelType.STOCK) { continue; }

			ImageIcon ii = new ImageIcon(MainWindow.class.getResource(String.format("/commodity_icons/%s.png", c.name().toLowerCase())));
			JLabel lbl1 = new JLabel(ii);
			lbl1.setToolTipText(c.name().toLowerCase());
			p.add(lbl1);

			p.add(new JLabel(String.format("x%d ", si.quantities.get(c))));
			if (si.prices.containsKey(c)) {
				p.add(new JLabel(String.format("($%d) ",
					si.prices.get(c)
					)));
			}
		}

		return p;
	}
}
