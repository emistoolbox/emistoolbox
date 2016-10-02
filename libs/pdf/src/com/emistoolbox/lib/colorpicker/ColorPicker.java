package com.emistoolbox.lib.colorpicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ColorPicker extends JPanel {
	public static void main (String [] args) {
		JFrame frame = new JFrame ();
		frame.getContentPane ().add (new ColorPicker ());
		frame.setBounds (0,0,400,400);
		frame.setVisible (true);
	}
	
	public void paint (Graphics g) {
		Dimension size = getSize ();
		for (int y = 0;y < size.height;y++)
			for (int x = 0;x < size.width;x++) {
				g.setColor (getColor (x / (size.width - 1f),y / (size.height - 1f)));
				g.fillRect (x,y,1,1);
			}
	}

	final static float dx = 1/2f;
	final static float dy = 1/3f;
	final static float dx2 = 1/4f;
	
	final static float [] [] basisVectors = new float [] [] {
		{1,0,0},
		{0,1,0},
		{1,1,1}
	};
	
	static {
		normalize (2);
		orthogonalize (0,2);
		orthogonalize (1,2);
		normalize (1);
		orthogonalize (0,1);
		normalize (0);
	}
	
	private static void normalize (int j) {
		double sum = 0;
		for (float x : basisVectors [j])
			sum += x * x;
		sum = 1 / Math.sqrt (sum);
		for (int i = 0;i < 3;i++)
			basisVectors [j] [i] *= sum;
	}
	
	private static void orthogonalize (int i,int j) {
		double sum = 0;
		for (int k = 0;k < 3;k++)
			sum += basisVectors [i] [k] * basisVectors [j] [k];
		for (int k = 0;k < 3;k++)
			basisVectors [i] [k] -= sum * basisVectors [j] [k];
	}
	
	private float [] vector = new float [3];
	private Color getColor (float x,float y) {
		x *= 6;
		y *= 6;
		
		for (int i = 0;i < 3;i++)
			vector [i] = x * basisVectors [0] [i] + y * basisVectors [1] [i];
		int iz = (int) Math.floor (vector [2]);
		vector [2] -= iz;
		vector [0] += iz * dx;
		vector [1] += iz * dy;
		int iy = (int) Math.floor (vector [1]);
		vector [1] -= iy;
		vector [0] += iy * dx2;
		int ix = (int) Math.floor (vector [0]);
		vector [0] -= ix;
		return new Color (vector [0],vector [1],vector [2]);
	}
}
