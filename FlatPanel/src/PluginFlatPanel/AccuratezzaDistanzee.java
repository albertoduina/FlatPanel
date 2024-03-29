package PluginFlatPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.PlugIn;

public class AccuratezzaDistanzee implements PlugIn {
	// fa riferimento al foglio AccuratezzaDistanza
//	public int setup(String arg, ImagePlus imp) {
//		this.imp = imp;
//		return DOES_ALL;
//	} // setup

	public void run(String arg) {

//		double[] valDose = { 0.5145, 1.0428, 2.1216, 4.2965, 8.6453, 17.3227 };
//
//		// ========= APRO CARTELLA PER METTERE REPORT001 ==============================
//		IJ.showMessage("Dove vuoi salvare Report001?");
//		String pathReport = ReadData.openWindowReport();
//		ReadData.initLog(pathReport);
//		ReadData.appendLog(pathReport, "Accuratezza distanze");
//		// ================================================================
//
//		// === FACCIO APRIRE FINESTRA DI DIALOGO PER SELEZIONARE MACCHINA
//		long[] datiMacchina = ReadData.selectFlatPanel();
//		int width = (int) datiMacchina[0];
//		int height = (int) datiMacchina[1];
//		long offset = datiMacchina[2];
//		double dimPixel = (double) datiMacchina[3];
//		// ==============================================================
		
		
		IJ.log("---------------------------------");
		IJ.log(ReadData.qui() + "START>");
		String[] aux5 = arg.split(";");
		for (String myArg : aux5) 
			IJ.log("arg= "+myArg);
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
		ReadData.appendLog(pathReport, "AccuratezzaDistanze");
		
		int width = 0;
		int height = 0;
		long offset = 0;
		float dimPixel = 0;
		double m=0;
		double q=0;
		String stringona = "";

		if (arg == "") {
			long[] datiMacchina = ReadData.selectFlatPanel();
			width = (int) datiMacchina[0];
			height = (int) datiMacchina[1];
			offset = datiMacchina[2];
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


		// ==== APRO IMMAGINE ======
		IJ.showMessage("ACCURATEZZA DISTANZE");
		IJ.showMessage("Selezionare l'immagine relativa alla sezione accuratezza distanze");
		String path1 = ReadData.chooseImageRaw();
		ImagePlus imp1 = ReadData.openRaw(path1, width, height, offset);

		// ====== CREO OVERLAY CHE E' UN LAYER SUL QUALE ATTACCO GLI OGGETTI ========
		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		// ==========================================================================
		//
		//
		// ============== SULL'OVERLAY CI METTO LE SCRITTE ==========================
		//double width = imp1.getWidth();
		//double height = imp1.getHeight();
		
		
		
		Font font;
		font = new Font("SansSerif", Font.BOLD, (int) width / 35);
		imp1.setRoi(new TextRoi(width / 2 - (width/2 - 200) , height / 2 - (height/2 - 200), "1", font));
		imp1.getRoi().setStrokeColor(Color.red);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(new TextRoi(width / 2 + (width/2 - 200), height / 2 - (height/2 - 200), "2", font));
		imp1.getRoi().setStrokeColor(Color.red);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(new TextRoi(width / 2 + (width/2 - 200), height / 2 + (height/2 - 200), "3", font));
		imp1.getRoi().setStrokeColor(Color.red);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(new TextRoi(width / 2 - (width/2 - 200), height / 2 + (height/2 - 200), "4", font));
		imp1.getRoi().setStrokeColor(Color.red);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		// ==========================================================
		//
		//
		// === CLICCO SULL'IMMAGINE I 4 PUNTI COL PUNTATORE =========
		String message = "Cliccare prima sul vertice in alto a sinistra,\npoi sul vertice in alto a destra,\npoi sul veritice in basso a destra, \npoi sul verfice in basso a sinistra.\nPOI premere OK";

		boolean check = false;

		int[] xPoints = null;
		int[] yPoints = null;

		while (!check) {
			Polygon dots = ReadData.selectionPointsClick(imp1, message);
			int nPunti;
			if (dots == null) {
				nPunti = 0;
			} else {
				nPunti = dots.npoints;
			}
			if (nPunti == 4) { // <- per vedere se cliccano davvero tutti i punti e non di più o meno
				check = true;
				xPoints = dots.xpoints;
				yPoints = dots.ypoints;
				int[][] tabPunti = new int[nPunti][2];
				for (int i1 = 0; i1 < nPunti; i1++) {
					tabPunti[i1][0] = xPoints[i1];
					tabPunti[i1][1] = yPoints[i1];
				}
			}
		}
		// ==========================================================
		//
		//
		// == UNISCO I 4 PUNTI CON DELLE LINEE E CREO OGGETTO LINEA ==
		Line line1 = new Line(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
		Line line2 = new Line(xPoints[1], yPoints[1], xPoints[2], yPoints[2]);
		Line line3 = new Line(xPoints[2], yPoints[2], xPoints[3], yPoints[3]);
		Line line4 = new Line(xPoints[3], yPoints[3], xPoints[0], yPoints[0]);
		Line line5 = new Line(xPoints[0], yPoints[0], xPoints[2], yPoints[2]);
		Line line6 = new Line(xPoints[1], yPoints[1], xPoints[3], yPoints[3]);
		// ==========================================================
		//
		//
		// == DISEGNO LINEE SU OVERLAY CREATO IN PRECEDENZA =========
		imp1.setRoi(line1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(line2);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(line3);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());

		imp1.setRoi(line4);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());

		imp1.killRoi();
		// ==========================================================
		//
		//
		// == MISURO LUNGHEZZA LINEA E LA CONVERTO IN CM ============

		double lenght1 = line1.getRawLength() * dimPixel / 10000;
		double lenght2 = line2.getRawLength() * dimPixel / 10000;
		double lenght3 = line3.getRawLength() * dimPixel / 10000;
		double lenght4 = line4.getRawLength() * dimPixel / 10000;
		double lenght5 = line5.getRawLength() * dimPixel / 10000;
		double lenght6 = line6.getRawLength() * dimPixel / 10000;
		

		// ==========================================================

		ReadData.appendLog(pathReport, "#F001# = Lato A = " + lenght1);
		ReadData.appendLog(pathReport, "#F002# = Lato B = " + lenght2);
		ReadData.appendLog(pathReport, "#F003# = Lato C = " + lenght3);
		ReadData.appendLog(pathReport, "#F004# = Lato D = " + lenght4);
		ReadData.appendLog(pathReport, "#F005# = Lato E = " + lenght5);
		ReadData.appendLog(pathReport, "#F006# = Lato F = " + lenght6);
		
		imp1.close();

	}
}
