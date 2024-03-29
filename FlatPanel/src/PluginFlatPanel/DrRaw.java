package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class DrRaw implements PlugInFilter {
	public boolean STEP = true;

	int lato;

	int bordoX;

	int ripetizX;

	int bordoY;

	int ripetizY;

	boolean bAbort;

	static boolean demo;

	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	} // setup

	public void run(ImageProcessor ip) {

		/*
		 * measurements supporta i seguenti valori: AREA = 1 MEAN = 2 STD_DEV =
		 * 4 MODE = 8 MIN_MAX = 16 CENTROID = 32 CENTER_OF_MASS = 64 PERIMETER =
		 * 128 LIMIT = 256 RECT = 512 LABELS = 1024 ELLIPSE = 2048
		 */

		int measurements = 2 + 4;

		demo = true;

		// --------------------

		int aa1, bb1, aa2, bb2;
		int c1, c2, r1;
		//UtilAyv2 u1 = new UtilAyv2();

		lato = (int) Prefs.get("prefer.drLato", 150);
		bordoX = (int) Prefs.get("prefer.drBordoX", 200);
		ripetizX = (int) Prefs.get("prefer.drRipetizX", 10);
		bordoY = (int) Prefs.get("prefer.drBordoY", 180);
		ripetizY = (int) Prefs.get("prefer.drRipetizY", 8);

		demo = Prefs.get("prefer.drDemo", false);
		getData1(imp.getWidth(), imp.getHeight());
		Prefs.set("prefer.drLato", lato);
		Prefs.set("prefer.drBordoX", bordoX);
		Prefs.set("prefer.drRipetizX", ripetizX);
		Prefs.set("prefer.drBordoY", bordoY);
		Prefs.set("prefer.drRipetizY", ripetizY);

		Prefs.set("prefer.drDemo", demo);

		ImageWindow win1 = WindowManager.getCurrentWindow();
		win1.setVisible(true);

		// imp.hide();
		ResultsTable rt = new ResultsTable();
		if (rt.getCounter() == 0) {
			IJ.setColumnHeadings("Roi\tMedia\tDevStand\t");
		}
		Analyzer a = new Analyzer(imp, measurements, rt);

		float[] pixels2 = new float[(ripetizX) * (ripetizY)];
		ImageProcessor ip3 = new FloatProcessor(ripetizX, ripetizY, pixels2,
				null);
		ImagePlus imp3 = new ImagePlus("Immagine sintetica", ip3);
		ip3 = imp3.getProcessor();
		aa1 = 0;
		bb1 = 0;

		for (int y1 = 0; y1 < ripetizY; y1++) {

			bb1 = bordoY + y1 * lato / 2;
			for (int x1 = 0; x1 < ripetizX; x1++) {
				aa1 = bordoX + x1 * lato / 2;

				IJ.log("aa1=" + aa1 + "   bb1=" + bb1);
				imp.setRoi(aa1, bb1, lato, lato);
				Roi roi = imp.getRoi();
				ImageStatistics stats1 = imp.getStatistics();
				a.saveResults(stats1, roi);
				ip3.putPixelValue(x1, y1, stats1.mean);

				a.displayResults();
				aa1 += lato / 2;

				if (demo) {
					GenericDialog gd1 = new GenericDialog("Demo mode attivato");
					gd1.enableYesNoCancel("CONTINUA", "ANNULLA");
					gd1.showDialog();
					//r1 = u1.ModelessMsg("Demo mode attivato", "CONTINUA",
					//		"ANNULLA");
					if (gd1.wasCanceled())
						return;
				}
			}
			IJ.showProgress((double) bb1 / imp.getWidth());
		}

		imp3.show();
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("Enhance Contrast", "saturated=0.5");

	} // run

	/**
	 * Creates a dialog box, allowing the user to enter the requested width,
	 * height, x & y coordinates, slice number for a Region Of Interest, option
	 * for oval, and option for whether x & y coordinates to be centered.
	 */
	void getData1(int width, int height) {
		double aux1, aux2, aux3, aux4;

		GenericDialog gd = new GenericDialog("Passo = lato / 2", IJ
				.getInstance());
		gd.addNumericField("Lato desiderato:", lato, 0);
		gd.addNumericField("Bordo X (orizzontale):", bordoX, 0);
		gd.addNumericField("Ripetizioni X        :", ripetizX, 0);
		gd.addNumericField("Bordo Y (verticale)  :", bordoY, 0);
		gd.addNumericField("Ripetizioni Y        :", ripetizY, 0);

		gd.addCheckbox("Demo", demo);

		gd.showDialog();

		if (gd.wasCanceled()) {
			bAbort = true;
			return;
		}

		lato = (int) gd.getNextNumber();
		bordoX = (int) gd.getNextNumber();
		ripetizX = (int) gd.getNextNumber();
		bordoY = (int) gd.getNextNumber();
		ripetizY = (int) gd.getNextNumber();

		demo = gd.getNextBoolean();
	} // run

	/**
	 * esegue l'autoAdjust del contrasto immagine
	 * 
	 * Author Terry Wu, Ph.D., University of Minnesota, <JavaPlugins@yahoo.com>
	 * (from ij.plugin.frame.ContrastAdjuster by Wayne Rasband
	 * <wayne@codon.nih.gov>)
	 * 
	 * @param imp
	 *            ImagePlus da regolare
	 * @param ip
	 *            ImageProcessor dell'immagine
	 * 
	 */
	void autoAdjust(ImagePlus imp, ImageProcessor ip) {
		double min, max;

		Calibration cal = imp.getCalibration();
		imp.setCalibration(null);
		ImageStatistics stats = imp.getStatistics();
		imp.setCalibration(cal);
		int[] histogram = stats.histogram;
		int threshold = stats.pixelCount / 5000;
		int i = -1;
		boolean found = false;
		do {
			i++;
			found = histogram[i] > threshold;
		} while (!found && i < 255);
		int hmin = i;
		i = 256;
		do {
			i--;
			found = histogram[i] > threshold;
		} while (!found && i > 0);
		int hmax = i;
		if (hmax > hmin) {
			imp.killRoi();
			min = stats.histMin + hmin * stats.binSize;
			max = stats.histMin + hmax * stats.binSize;
			ip.setMinAndMax(min, max);
		}
		Roi roi = imp.getRoi();
		if (roi != null) {
			ImageProcessor mask = roi.getMask();
			if (mask != null)
				ip.reset(mask);
		}

	}

}// DrRaw

