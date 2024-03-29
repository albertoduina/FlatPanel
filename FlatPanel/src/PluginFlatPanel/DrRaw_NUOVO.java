package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class DrRaw_NUOVO implements PlugIn {

//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================

		IJ.showMessage("Dove vuoi salvare Report001?");
		String pathReport = ReadData.openWindowReport();
		ReadData.initLog(pathReport);
		ReadData.appendLog(pathReport, "DrRaw");

		long[] datiMacchina = ReadData.selectFlatPanel();
		int width = (int) datiMacchina[0];
		int height = (int) datiMacchina[1];
		long offset = datiMacchina[2];
		int dimPixel = (int) datiMacchina[3];

		OpenDialog od = new OpenDialog("Seleziona immagine desiderata", null);
		String folder = od.getDirectory();
		String file = od.getFileName();
		String path = folder + file;
		ReadData.waitHere(path);
		ImagePlus imp1 = ReadData.openRaw(path, width, height, offset);

		float dimPixellone = 3;
		float bordoSalto = 3;

		GenericDialog gd2 = new GenericDialog("Seleziona grandezza pixellone");
		gd2.addNumericField("Quanto vuoi grande il pixellone? (in cm):", dimPixellone);
		gd2.addNumericField("Quanto bordo vuoi saltare? (in cm)", bordoSalto);
		gd2.showDialog();

		if (gd2.wasCanceled()) {
			return;
		}

		dimPixellone = (float) gd2.getNextNumber();
		bordoSalto = (float) gd2.getNextNumber();

		float dimPixel2 = (float) dimPixel;
		float width2 = (float) width;
		float height2 = (float) height;

		int pixelPixellone = (int) Math.ceil((dimPixellone * Math.pow(10, 4)) / dimPixel2);
		int pixelSaltare = (int) Math.ceil((bordoSalto * Math.pow(10, 4)) / dimPixel2);
		int ripetizioniX = (int) Math.floor((((width2 - (pixelSaltare * 2)) / pixelPixellone) * 2) - 1);
		int ripetizioniY = (int) Math.floor((((height2 - (pixelSaltare * 2)) / pixelPixellone) * 2) - 1);

		ReadData.waitHere("pixelPixellone = " + pixelPixellone + "pixelSaltare = " + pixelSaltare + "ripetizioniX = "
				+ ripetizioniX + "ripetizioniY = " + ripetizioniY);

		// boolean STEP = true;

		int lato;
		int bordoX;
		int ripetizX;
		int bordoY;
		int ripetizY;
		boolean bAbort;
		boolean demo;
		ImagePlus imp;

		/*
		 * measurements supporta i seguenti valori: AREA = 1 MEAN = 2 STD_DEV = 4 MODE =
		 * 8 MIN_MAX = 16 CENTROID = 32 CENTER_OF_MASS = 64 PERIMETER = 128 LIMIT = 256
		 * RECT = 512 LABELS = 1024 ELLIPSE = 2048
		 */

		int measurements = 2 + 4;

		demo = true;

		// --------------------

		int aa1, bb1, aa2, bb2;
		int c1, c2, r1;
		// UtilAyv2 u1 = new UtilAyv2();
//
//		lato = (int) Prefs.get("prefer.drLato", 150); //se nelle pref non c'� niente mi fa vedere 150
//		bordoX = (int) Prefs.get("prefer.drBordoX", 200);
//		ripetizX = (int) Prefs.get("prefer.drRipetizX", 10);
//		bordoY = (int) Prefs.get("prefer.drBordoY", 180);
//		ripetizY = (int) Prefs.get("prefer.drRipetizY", 8);

		demo = Prefs.get("prefer.drDemo", false);

		double aux1, aux2, aux3, aux4;

//		GenericDialog gd = new GenericDialog("Passo = lato / 2", IJ.getInstance());
//		gd.addNumericField("Lato desiderato:", lato, 0);
//		gd.addNumericField("Bordo X (orizzontale):", bordoX, 0);
//		gd.addNumericField("Ripetizioni X        :", ripetizX, 0);
//		gd.addNumericField("Bordo Y (verticale)  :", bordoY, 0);
//		gd.addNumericField("Ripetizioni Y        :", ripetizY, 0);
//
//		gd.addCheckbox("Demo", demo);
//
//		gd.showDialog();
//
//		if (gd.wasCanceled()) {
//			bAbort = true;
//			return;
//		}

		lato = pixelPixellone;
		bordoX = pixelSaltare;
		ripetizX = ripetizioniX;
		bordoY = pixelSaltare;
		ripetizY = ripetizioniY;

//		demo = gd.getNextBoolean();

//		Prefs.set("prefer.drLato", lato);
//		Prefs.set("prefer.drBordoX", bordoX);
//		Prefs.set("prefer.drRipetizX", ripetizX);
//		Prefs.set("prefer.drBordoY", bordoY);
//		Prefs.set("prefer.drRipetizY", ripetizY);
//
//		Prefs.set("prefer.drDemo", demo);

//		ImageWindow win1 = WindowManager.getCurrentWindow();
//		win1.setVisible(true);

		// imp1.hide();
		ResultsTable rt = new ResultsTable();
		if (rt.getCounter() == 0) {
			IJ.setColumnHeadings("Roi\tMedia\tDevStand\t");
		}
		Analyzer a = new Analyzer(imp1, measurements, rt);

		float[] pixels2 = new float[(ripetizX) * (ripetizY)];
		ImageProcessor ip3 = new FloatProcessor(ripetizX, ripetizY, pixels2, null);
		ImagePlus imp3 = new ImagePlus("Immagine sintetica", ip3);
		ip3 = imp3.getProcessor();
		aa1 = 0;
		bb1 = 0;

		float[][] matrixMean = new float[ripetizioniY][ripetizioniX];
		float[][] matrixSD = new float[ripetizioniY][ripetizioniX];

		for (int y1 = 0; y1 < ripetizY; y1++) {

			bb1 = bordoY + y1 * lato / 2;
			for (int x1 = 0; x1 < ripetizX; x1++) {
				aa1 = bordoX + x1 * lato / 2;

				IJ.log("aa1=" + aa1 + "   bb1=" + bb1);
				imp1.setRoi(aa1, bb1, lato, lato);
				Roi roi = imp1.getRoi();
				ImageStatistics stats1 = imp1.getStatistics();
				a.saveResults(stats1, roi);
				ip3.putPixelValue(x1, y1, stats1.mean);
				matrixMean[y1][x1] = (float) stats1.mean;
				matrixSD[y1][x1] = (float) stats1.stdDev;

				a.displayResults();
				aa1 += lato / 2;

				if (demo) {
					GenericDialog gd1 = new GenericDialog("Demo mode attivato");
					gd1.enableYesNoCancel("CONTINUA", "ANNULLA");
					gd1.showDialog();
					// r1 = u1.ModelessMsg("Demo mode attivato", "CONTINUA",
					// "ANNULLA");
					if (gd1.wasCanceled())
						return;
				}
			}
			IJ.showProgress((double) bb1 / imp1.getWidth());
		}

		imp3.show();
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("Enhance Contrast", "saturated=0.5");

		for (int i = 0; i < matrixMean.length; i++) {
			String pippo = "";
			for (int j = 0; j < matrixMean[i].length; j++) {
				pippo = pippo + (matrixMean[i][j] + " - ");
			}
			IJ.log(pippo);
		}

	} // run

	/**
	 * esegue l'autoAdjust del contrasto immagine
	 * 
	 * Author Terry Wu, Ph.D., University of Minnesota, <JavaPlugins@yahoo.com>
	 * (from ij.plugin.frame.ContrastAdjuster by Wayne Rasband
	 * <wayne@codon.nih.gov>)
	 * 
	 * @param imp ImagePlus da regolare
	 * @param ip  ImageProcessor dell'immagine
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
