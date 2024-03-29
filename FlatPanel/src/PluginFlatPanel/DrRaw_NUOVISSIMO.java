package PluginFlatPanel;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class DrRaw_NUOVISSIMO implements PlugIn {
	// fa riferimento al foglio NonUniformita'
//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double m = Double.parseDouble(Prefs.get("FlatPanel.prefM", ""));
//		double q = Double.parseDouble(Prefs.get("FlatPanel.prefQ", ""));

		// pathReport = Prefs.get("FlatPanel.Report001", "")

		IJ.log("---------------------------------");
		IJ.log(ReadData.qui() + "START>");
		String[] aux5 = arg.split(";");
		for (String myArg : aux5)
			IJ.log("arg= " + myArg);
		IJ.log("---------------------------------");

		// ########### APRO CARTELLA PER METTERE REPORT001 ##########################
		String pathReport = "";
		if (arg == "") {
			IJ.showMessage("Dove vuoi salvare Report001?");
			pathReport = ReadData.openWindowReport();
			ReadData.initLog(pathReport);
		} else {
			pathReport = Prefs.get("FlatPanel.Report001", "") + "Report001.txt";
		}
		ReadData.appendLog(pathReport, "DrRaw");

		int width = 0;
		int height = 0;
		long offset = 0;
		float dimPixel = 0;
		double m = 0.0073;
		double q = -0.2767;
		String stringona = "";

		if (arg == "") {
			long[] datiMacchina = ReadData.selectFlatPanel();
			width = (int) datiMacchina[0];
			height = (int) datiMacchina[1];
			offset = (long) datiMacchina[2];
			dimPixel = (float) datiMacchina[3];
		} else {
			stringona = Prefs.get("FlatPanel.myPref", ""); // <- legge stringona dalla stringa delle preferenze
			String[] valori = stringona.split(";");
			width = Integer.parseInt(valori[0]);
			height = Integer.parseInt(valori[1]);
			offset = Long.parseLong(valori[2]);
			dimPixel = Float.parseFloat(valori[3]);
			m = Double.parseDouble(valori[4]);
			q = Double.parseDouble(valori[5]);
		}

		// ###########################################################################

//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "DrRaw");
//		// ================================================================

		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
//		long[] datiMacchina = ReadData.selectFlatPanel();
//		int width = (int) datiMacchina[0];
//		int height = (int) datiMacchina[1];
//		long offset = datiMacchina[2];
//		int dimPixel = (int) datiMacchina[3];
		// ==============================================================

		// ==== APRO IMMAGINE ======
		IJ.showMessage("DrRaw - Non uniformita'");
		IJ.showMessage("Selezionare l'immagine presa in 'funzione di conversione' con dose piu' vicina a 2,5 uGy");
		String path1 = ReadData.chooseImageRaw();
		IJ.log("path1= " + path1 + " width= " + width + " height= " + height + " offset= " + offset);
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);

		// ReadData.waitHere("m= " + m + " q= " + q);

		ReadData.convertImageToDose(imp1, m, q);

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

		//ReadData.waitHere("pixelPixellone = " + pixelPixellone + "pixelSaltare = " + pixelSaltare + "ripetizioniX = "
		//		+ ripetizioniX + "ripetizioniY = " + ripetizioniY);

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
		
		
		//ATTENZIONE!!!///
		ResultsTable rt = new ResultsTable();
		if (rt.getCounter() == 0) {
			IJ.setColumnHeadings("Roi\tMedia\tDevStand\t");
		}
		Analyzer a = new Analyzer(imp1, measurements, rt);

		float[] pixels2 = new float[(ripetizX) * (ripetizY)];
		ImageProcessor ip3 = new FloatProcessor(ripetizX, ripetizY, pixels2, null);
		ImagePlus imp3 = new ImagePlus("Immagine sintetica Mean", ip3);
		ip3 = imp3.getProcessor();

		float[] pixels4 = new float[(ripetizX) * (ripetizY)];
		ImageProcessor ip4 = new FloatProcessor(ripetizX, ripetizY, pixels4, null);
		ImagePlus imp4 = new ImagePlus("Immagine sintetica SD", ip4);
		ip4 = imp4.getProcessor();

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
				double mean1=stats1.mean;
				double stdev1=stats1.stdDev;
				ip3.putPixelValue(x1, y1, mean1);
				ip4.putPixelValue(x1, y1, stdev1);
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

	
		String path = pathReport.substring(0, pathReport.lastIndexOf('\\'));
		
		
		imp3.updateAndDraw();
		imp3.show();
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("Enhance Contrast", "saturated=0.5");
		String imp3Name = path + "//media.png";
		new FileSaver(imp3).saveAsPng(imp3Name);

		

		imp4.updateAndDraw();
		imp4.show();
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("In");
		IJ.run("Enhance Contrast", "saturated=0.5");
		String imp4Name = path + "//stdDev.png";
		new FileSaver(imp4).saveAsPng(imp4Name);


//		// -- SCRIVO SU LOG MATRICE DELLE MEDIE
//		for (int i = 0; i < matrixMean.length; i++) {
//			String pippo = "";
//			for (int j = 0; j < matrixMean[i].length; j++) {
//				pippo = pippo + (matrixMean[i][j] + " - ");
//			}
//			IJ.log(pippo);
//			ReadData.appendLog(pathReport, pippo);
//		}
//		// ----------

		// ---- CREO MATRICE NON UNIFORMITA' LOCALE
		float[][] matrixNonUnifLocal = new float[ripetizioniY - 2][ripetizioniX - 2];

		for (int i = 0; i < matrixNonUnifLocal.length; i++) {
			for (int j = 0; j < matrixNonUnifLocal[i].length; j++) {

				double valAlto = Math.abs(matrixMean[i + 1][j + 1] - matrixMean[i][j + 1]);
				double valBasso = Math.abs(matrixMean[i + 1][j + 1] - matrixMean[i + 2][j + 1]);
				double valSx = Math.abs(matrixMean[i + 1][j + 1] - matrixMean[i + 1][j]);
				double valDx = Math.abs(matrixMean[i + 1][j + 1] - matrixMean[i + 1][j + 2]);
				double valCentro = Math.abs(matrixMean[i + 1][j + 1]);

				double maxValCroce = (Math.max(valBasso, Math.max(valAlto, Math.max(valSx, valDx)))) / valCentro;

				matrixNonUnifLocal[i][j] = (float) maxValCroce;
			}

		}
		// -----------------------------------

//		// ---- SCRIVO SU LOG E REPORT MATRICE NON UNIF LOCALE
//		IJ.log("Matrice non unif locale");
//		ReadData.appendLog(pathReport, "Matrice non unif locale");
//
//		for (int i = 0; i < matrixNonUnifLocal.length; i++) {
//			String pippo = "";
//			for (int j = 0; j < matrixNonUnifLocal[i].length; j++) {
//				pippo = pippo + (matrixNonUnifLocal[i][j] + " - ");
//			}
//			IJ.log(pippo);
//			ReadData.appendLog(pathReport, pippo);
//		}
//		// ---------------------------------
		float NULS = ReadData.findMAXMatrix(matrixNonUnifLocal);


		float MAXMean = ReadData.findMAXMatrix(matrixMean);
		float MINMean = ReadData.findMINMatrix(matrixMean);

		// IJ.showMessage("Massimo matrice media = " + MAXMean);
		// IJ.showMessage("Minimo matrice media = " + MINMean);

		float NUGS = (MAXMean - MINMean) / (MAXMean + MINMean);

		// ---- CREO MATRICE SNR ------
		float[][] matrixSNR = new float[ripetizioniY][ripetizioniX];
		for (int i = 0; i < matrixSNR.length; i++) {
			for (int j = 0; j < matrixSNR[i].length; j++) {
				matrixSNR[i][j] = matrixMean[i][j] / matrixSD[i][j];
			}

		}
		// -----------------------------------

		// ---- CREO MATRICE NON UNIFORMITA' LOCALE SIGNAL NOISE RATIO
		float[][] matrixNonUnifLocalSNR = new float[ripetizioniY - 2][ripetizioniX - 2];

		for (int i = 0; i < matrixNonUnifLocalSNR.length; i++) {
			for (int j = 0; j < matrixNonUnifLocalSNR[i].length; j++) {

				double valAltoSNR = Math.abs(matrixSNR[i + 1][j + 1] - matrixSNR[i][j + 1]);
				double valBassoSNR = Math.abs(matrixSNR[i + 1][j + 1] - matrixSNR[i + 2][j + 1]);
				double valSxSNR = Math.abs(matrixSNR[i + 1][j + 1] - matrixSNR[i + 1][j]);
				double valDxSNR = Math.abs(matrixSNR[i + 1][j + 1] - matrixSNR[i + 1][j + 2]);
				double valCentroSNR = Math.abs(matrixSNR[i + 1][j + 1]);

				double maxValCroceSNR = (Math.max(valBassoSNR, Math.max(valAltoSNR, Math.max(valSxSNR, valDxSNR))))
						/ valCentroSNR;

				matrixNonUnifLocalSNR[i][j] = (float) maxValCroceSNR;
			}

		}
		// -----------------------------------

		float NULSNR = ReadData.findMAXMatrix(matrixNonUnifLocalSNR);

		float MAXsnr = ReadData.findMAXMatrix(matrixSNR);
		float MINsnr = ReadData.findMINMatrix(matrixSNR);

		//IJ.showMessage("Massimo matrice SNR = " + MAXsnr);
		//IJ.showMessage("Minimo matrice SNR = " + MINsnr);

		float NUGSNR = (MAXsnr - MINsnr) / (MAXsnr + MINsnr);
		IJ.showMessage("NULS = " + NULS + "%\n" + "NUGS = " + NUGS + "%\n" + "NULSNR = " + NULSNR + "%\n" + "NUGSNR = "
				+ NUGSNR + "%");
		ReadData.appendLog(pathReport, "#D001# = NULS = " + NULS);
		ReadData.appendLog(pathReport, "#D002# = NUGS = " + NUGS);
		ReadData.appendLog(pathReport, "#D003# = NULSNR = " + NULSNR);
		ReadData.appendLog(pathReport, "#D004# = NUGSNR = " + NUGSNR);
		
		imp1.close();
		imp3.close();
		imp4.close();

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
